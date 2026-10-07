package com.cinestar.mapper;

import com.cinestar.dto.response.GenreResponse;
import com.cinestar.dto.response.MovieDetailResponse;
import com.cinestar.dto.response.MovieResponse;
import com.cinestar.entity.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public MovieResponse toResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .slug(movie.getSlug())
                .posterUrl(movie.getPosterUrl())
                .duration(movie.getDuration())
                .country(movie.getCountry())
                .releaseDate(movie.getReleaseDate())
                .ageRating(movie.getAgeRating())
                .format(movie.getFormat())
                .subtitleType(movie.getSubtitleType())
                .rating(movie.getRating())
                .isFeatured(movie.getIsFeatured())
                .status(movie.getStatus() != null ? movie.getStatus().name() : null)
                .genres(movie.getGenres().stream()
                        .map(g -> new GenreResponse(g.getId(), g.getName(), g.getSlug()))
                        .toList())
                .build();
    }

    public MovieDetailResponse toDetailResponse(Movie movie) {
        MovieResponse base = toResponse(movie);
        return new MovieDetailResponse(base, movie.getOriginalTitle(), movie.getDescription(),
                movie.getBackdropUrl(), movie.getTrailerUrl(), movie.getDirector(), movie.getActors());
    }
}
