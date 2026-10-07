package com.cinestar.mapper;

import com.cinestar.dto.response.ShowtimeResponse;
import com.cinestar.entity.Showtime;
import org.springframework.stereotype.Component;

@Component
public class ShowtimeMapper {

    public ShowtimeResponse toResponse(Showtime s) {
        return ShowtimeResponse.builder()
                .id(s.getId())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .priceBase(s.getPriceBase())
                .format(s.getFormat())
                .subtitleType(s.getSubtitleType())
                .movieId(s.getMovie().getId())
                .movieTitle(s.getMovie().getTitle())
                .movieSlug(s.getMovie().getSlug())
                .moviePosterUrl(s.getMovie().getPosterUrl())
                .movieDuration(s.getMovie().getDuration())
                .movieAgeRating(s.getMovie().getAgeRating())
                .roomId(s.getRoom().getId())
                .roomName(s.getRoom().getName())
                .cinemaId(s.getRoom().getCinema().getId())
                .cinemaName(s.getRoom().getCinema().getName())
                .cinemaCity(s.getRoom().getCinema().getCity())
                .build();
    }
}
