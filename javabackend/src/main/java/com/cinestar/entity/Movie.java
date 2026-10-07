package com.cinestar.entity;

import com.cinestar.entity.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(length = 200)
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 500)
    private String backdropUrl;

    @Column(length = 200)
    private String trailerUrl;

    /** Thoi luong (phut) */
    private Integer duration;

    @Column(length = 50)
    private String country;

    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovieStatus status;

    /** Nhan do tuoi: P, K, T13, T16, T18 */
    @Column(length = 10)
    private String ageRating;

    /** Dinh dang chieu: 2D, 3D... */
    @Column(length = 10)
    private String format;

    /** PHU_DE hoac LONG_TIENG */
    @Column(name = "subtitle_type", length = 20)
    private String subtitleType;

    @Column(length = 100)
    private String director;

    @Column(length = 1000)
    private String actors;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    private Boolean isFeatured = false;

    /**
     * @BatchSize: khi lay danh sach phim, Hibernate se fetch genres theo loch
     * (1 query cho 20 phim) thay vi 1 query/phim => chong N+1 tren collection
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
}
