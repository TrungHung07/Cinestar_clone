package com.cinestar.service.impl;

import com.cinestar.dto.request.CinemaRequest;
import com.cinestar.dto.response.CinemaResponse;
import com.cinestar.entity.Cinema;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.mapper.CinemaMapper;
import com.cinestar.repository.CinemaRepository;
import com.cinestar.repository.RoomRepository;
import com.cinestar.service.CinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaServiceImpl implements CinemaService {

    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final CinemaMapper cinemaMapper;

    @Override
    @Cacheable(value = "cinemas", key = "'all'")
    @Transactional(readOnly = true)
    public List<CinemaResponse> getCinemas() {
        // roomRepository dem theo tung rap - so rap it nen khong gay N+1 dang ke
        return cinemaRepository.findAllByActiveTrue().stream()
                .map(cinema -> cinemaMapper.toResponse(cinema, roomRepository.countByCinemaId(cinema.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CinemaResponse getBySlug(String slug) {
        Cinema cinema = cinemaRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy rạp: " + slug));
        return cinemaMapper.toResponse(cinema, roomRepository.countByCinemaId(cinema.getId()));
    }

    @Override
    @Transactional
    @CacheEvict(value = "cinemas", allEntries = true)
    public CinemaResponse create(CinemaRequest request) {
        Cinema cinema = Cinema.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .address(request.getAddress())
                .city(request.getCity())
                .hotline(request.getHotline())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        cinema = cinemaRepository.save(cinema);
        return cinemaMapper.toResponse(cinema, 0);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cinemas", allEntries = true)
    public CinemaResponse update(Long id, CinemaRequest request) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy rạp id=" + id));
        cinema.setName(request.getName());
        cinema.setSlug(request.getSlug());
        cinema.setAddress(request.getAddress());
        cinema.setCity(request.getCity());
        cinema.setHotline(request.getHotline());
        cinema.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            cinema.setActive(request.getActive());
        }
        cinema = cinemaRepository.save(cinema);
        return cinemaMapper.toResponse(cinema, roomRepository.countByCinemaId(id));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cinemas", allEntries = true),
            @CacheEvict(value = "showtimes", allEntries = true)
    })
    public void delete(Long id) {
        cinemaRepository.deleteById(id);
    }
}
