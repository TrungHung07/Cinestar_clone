package com.cinestar.dto.response;

import lombok.Getter;
import lombok.Setter;

/** Chi tiet phim - ke thua ban tom tat, bo sung mo ta + trailer */
@Getter
@Setter
@lombok.NoArgsConstructor
public class MovieDetailResponse extends MovieResponse {

    private String originalTitle;
    private String description;
    private String backdropUrl;
    private String trailerUrl;
    private String director;
    private String actors;

    public MovieDetailResponse(MovieResponse base, String originalTitle, String description,
                               String backdropUrl, String trailerUrl, String director, String actors) {
        // copy cac truong chung tu ban tom tat
        this.setId(base.getId());
        this.setTitle(base.getTitle());
        this.setSlug(base.getSlug());
        this.setPosterUrl(base.getPosterUrl());
        this.setDuration(base.getDuration());
        this.setCountry(base.getCountry());
        this.setReleaseDate(base.getReleaseDate());
        this.setAgeRating(base.getAgeRating());
        this.setFormat(base.getFormat());
        this.setSubtitleType(base.getSubtitleType());
        this.setRating(base.getRating());
        this.setIsFeatured(base.getIsFeatured());
        this.setStatus(base.getStatus());
        this.setGenres(base.getGenres());
        this.originalTitle = originalTitle;
        this.description = description;
        this.backdropUrl = backdropUrl;
        this.trailerUrl = trailerUrl;
        this.director = director;
        this.actors = actors;
    }
}
