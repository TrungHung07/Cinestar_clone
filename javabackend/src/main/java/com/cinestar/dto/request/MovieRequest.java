package com.cinestar.dto.request;

import com.cinestar.entity.enums.MovieStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Body CRUD phim cho ADMIN */
@Getter
@Setter
public class MovieRequest {

    @NotBlank(message = "Tên phim không được để trống")
    private String title;

    @NotBlank(message = "Slug không được để trống")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug chỉ gồm chữ thường, số và dấu gạch ngang")
    private String slug;

    private String originalTitle;

    private String description;

    private String posterUrl;

    private String backdropUrl;

    private String trailerUrl;

    @Min(value = 1, message = "Thời lượng không hợp lệ")
    private Integer duration;

    private String country;

    private LocalDate releaseDate;

    @NotNull(message = "Trạng thái phim không được để trống")
    private MovieStatus status;

    @Pattern(regexp = "^$|^(P|K|T13|T16|T18)$", message = "Nhãn độ tuổi phải là P, K, T13, T16 hoặc T18")
    private String ageRating;

    @Pattern(regexp = "^$|^(2D|3D)$", message = "Định dạng phải là 2D hoặc 3D")
    private String format;

    @Pattern(regexp = "^$|^(PHU_DE|LONG_TIENG)$", message = "Kiểu phụ đề phải là PHU_DE hoặc LONG_TIENG")
    private String subtitleType;

    private String director;

    private String actors;

    @DecimalMin("0.0") @DecimalMax("10.0")
    private BigDecimal rating;

    private Boolean isFeatured;

    private List<String> genreNames;
}
