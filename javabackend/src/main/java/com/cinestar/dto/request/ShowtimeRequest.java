package com.cinestar.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Body CRUD suat chieu cho ADMIN */
@Getter
@Setter
public class ShowtimeRequest {

    @NotNull(message = "Thiếu thông tin phim")
    private Long movieId;

    @NotNull(message = "Thiếu thông tin phòng chiếu")
    private Long roomId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Future(message = "Suất chiếu phải ở tương lai")
    private LocalDateTime startTime;

    @NotNull(message = "Giá vé không được để trống")
    @DecimalMin(value = "1000", message = "Giá vé không hợp lệ")
    private BigDecimal priceBase;

    @Pattern(regexp = "^$|^(2D|3D)$", message = "Định dạng phải là 2D hoặc 3D")
    private String format;

    @Pattern(regexp = "^$|^(PHU_DE|LONG_TIENG)$", message = "Kiểu phụ đề phải là PHU_DE hoặc LONG_TIENG")
    private String subtitleType;
}
