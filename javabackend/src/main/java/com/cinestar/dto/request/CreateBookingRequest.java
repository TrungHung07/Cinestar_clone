package com.cinestar.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateBookingRequest {

    @NotNull(message = "Thiếu thông tin suất chiếu")
    private Long showtimeId;

    @NotEmpty(message = "Vui lòng chọn ít nhất một ghế")
    @Size(max = 10, message = "Tối đa 10 ghế mỗi đơn")
    private List<Long> seatIds;

    /** Bap nuoc di kem (khong bat buoc) */
    @Valid
    private List<BookingItemRequest> items;

    @Getter
    @Setter
    public static class BookingItemRequest {

        @NotNull(message = "Thiếu thông tin combo")
        private Long comboId;

        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        @Max(value = 10, message = "Số lượng tối đa là 10")
        private Integer quantity;
    }
}
