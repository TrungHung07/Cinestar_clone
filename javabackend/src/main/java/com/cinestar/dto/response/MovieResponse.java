package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Ban tom tat phim dung cho danh sach (nhe, khong kem mo ta dai) */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    private Long id;
    private String title;
    private String slug;
    private String posterUrl;
    private Integer duration;
    private String country;
    private LocalDate releaseDate;
    private String ageRating;
    private String format;
    private String subtitleType;
    private BigDecimal rating;
    private Boolean isFeatured;
    private String status;
    private List<GenreResponse> genres;
}
