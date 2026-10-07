package com.cinestar.service.impl;

import com.cinestar.dto.request.MovieRequest;
import com.cinestar.dto.response.MovieDetailResponse;
import com.cinestar.dto.response.MovieResponse;
import com.cinestar.entity.Genre;
import com.cinestar.entity.Movie;
import com.cinestar.entity.enums.MovieStatus;
import com.cinestar.exception.BadRequestException;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.mapper.MovieMapper;
import com.cinestar.repository.GenreRepository;
import com.cinestar.repository.MovieRepository;
import com.cinestar.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMapper movieMapper;

    // ============ Doc du lieu (co CACHE Redis) ============

    @Override
    @Cacheable(value = "movies", key = "'now-showing'")
    @Transactional(readOnly = true)
    public List<MovieResponse> getNowShowing() {
        log.info("[CACHE MISS] Truy van DB: phim dang chieu");
        return movieRepository.findByStatus(MovieStatus.NOW_SHOWING,
                        PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "isFeatured")))
                .map(movieMapper::toResponse)
                .getContent();
    }

    @Override
    @Cacheable(value = "movies", key = "'upcoming'")
    @Transactional(readOnly = true)
    public List<MovieResponse> getUpcoming() {
        log.info("[CACHE MISS] Truy van DB: phim sap chieu");
        return movieRepository.findByStatus(MovieStatus.UPCOMING,
                        PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "releaseDate")))
                .map(movieMapper::toResponse)
                .getContent();
    }

    @Override
    @Cacheable(value = "movies", key = "'featured'")
    @Transactional(readOnly = true)
    public List<MovieResponse> getFeatured() {
        log.info("[CACHE MISS] Truy van DB: phim noi bat");
        return movieRepository.findByStatus(MovieStatus.NOW_SHOWING,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "rating")))
                .map(movieMapper::toResponse)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> getMovies(MovieStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Movie> movies;
        if (search != null && !search.isBlank()) {
            MovieStatus effective = status != null ? status : MovieStatus.NOW_SHOWING;
            movies = movieRepository.findByTitleContainingIgnoreCaseAndStatus(search.trim(), effective, pageable);
        } else if (status != null) {
            movies = movieRepository.findByStatus(status, pageable);
        } else {
            movies = movieRepository.findAll(pageable);
        }
        return movies.map(movieMapper::toResponse);
    }

    @Override
    @Cacheable(value = "movie-details", key = "#slug")
    @Transactional(readOnly = true)
    public MovieDetailResponse getBySlug(String slug) {
        log.info("[CACHE MISS] Truy van DB: chi tiet phim {}", slug);
        // findBySlug da EntityGraph fetch genres -> 1 query
        Movie movie = movieRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim: " + slug));
        return movieMapper.toDetailResponse(movie);
    }

    // ============ Quan tri (ADMIN) - luon xoa cache sau khi sua ============

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", allEntries = true),
            @CacheEvict(value = "movie-details", allEntries = true)
    })
    public MovieResponse create(MovieRequest request) {
        if (movieRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Slug đã tồn tại");
        }
        Movie movie = applyRequest(new Movie(), request);
        movie = movieRepository.save(movie);
        log.info("ADMIN tao phim moi: {}", movie.getTitle());
        return movieMapper.toResponse(movie);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", allEntries = true),
            @CacheEvict(value = "movie-details", allEntries = true)
    })
    public MovieResponse update(Long id, MovieRequest request) {
        Movie movie = movieRepository.findWithGenresById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim id=" + id));
        applyRequest(movie, request);
        movie = movieRepository.save(movie);
        log.info("ADMIN cap nhat phim: {}", movie.getTitle());
        return movieMapper.toResponse(movie);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", allEntries = true),
            @CacheEvict(value = "movie-details", allEntries = true)
    })
    public void delete(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phim id=" + id);
        }
        movieRepository.deleteById(id);
        log.info("ADMIN xoa phim id={}", id);
    }

    private Movie applyRequest(Movie movie, MovieRequest request) {
        movie.setTitle(request.getTitle());
        movie.setSlug(request.getSlug());
        movie.setOriginalTitle(request.getOriginalTitle());
        movie.setDescription(request.getDescription());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setBackdropUrl(request.getBackdropUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setDuration(request.getDuration());
        movie.setCountry(request.getCountry());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(request.getStatus());
        movie.setAgeRating(request.getAgeRating());
        movie.setFormat(request.getFormat());
        movie.setSubtitleType(request.getSubtitleType());
        movie.setDirector(request.getDirector());
        movie.setActors(request.getActors());
        movie.setRating(request.getRating());
        movie.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));

        if (request.getGenreNames() != null) {
            Set<Genre> genres = new HashSet<>();
            for (String name : request.getGenreNames()) {
                if (name == null || name.isBlank()) {
                    continue;
                }
                Genre genre = genreRepository.findByNameIgnoreCase(name.trim())
                        .orElseGet(() -> genreRepository.save(Genre.builder()
                                .name(name.trim())
                                .slug(name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-"))
                                .build()));
                genres.add(genre);
            }
            movie.getGenres().clear();
            movie.getGenres().addAll(genres);
        }
        return movie;
    }
}
