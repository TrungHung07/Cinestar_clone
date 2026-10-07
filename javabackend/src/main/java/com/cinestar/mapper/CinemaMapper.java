package com.cinestar.mapper;

import com.cinestar.dto.response.CinemaResponse;
import com.cinestar.entity.Cinema;
import org.springframework.stereotype.Component;

@Component
public class CinemaMapper {

    public CinemaResponse toResponse(Cinema cinema, long roomCount) {
        return CinemaResponse.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .slug(cinema.getSlug())
                .address(cinema.getAddress())
                .city(cinema.getCity())
                .hotline(cinema.getHotline())
                .imageUrl(cinema.getImageUrl())
                .roomCount((int) roomCount)
                .build();
    }
}
