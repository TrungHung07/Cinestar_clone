package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SeatLayoutResponse {

    private ShowtimeResponse showtime;
    private List<SeatResponse> seats;
    private Integer holdMinutes; // so phut he thong giu ghe sau khi chon
}
