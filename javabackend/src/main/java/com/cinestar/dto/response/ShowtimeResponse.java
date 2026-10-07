package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Suat chieu - truong phang de frontend de su dung, da join-fetch movie/room/cinema */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal priceBase;
    private String format;
    private String subtitleType;

    private Long movieId;
    private String movieTitle;
    private String movieSlug;
    private String moviePosterUrl;
    private Integer movieDuration;
    private String movieAgeRating;

    private Long roomId;
    private String roomName;

    private Long cinemaId;
    private String cinemaName;
    private String cinemaCity;
}
