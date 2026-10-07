package com.cinestar.service;

import com.cinestar.dto.request.ShowtimeRequest;
import com.cinestar.dto.response.SeatLayoutResponse;
import com.cinestar.dto.response.ShowtimeResponse;

import java.util.List;

public interface ShowtimeService {

    /**
     * Lich chieu theo phim/rap/ngay.
     * Ket qua duoc CACHE Redis 2 phut voi key movieId:cinemaId:date.
     */
    List<ShowtimeResponse> getShowtimes(Long movieId, Long cinemaId, String dateIso);

    /** So do ghe cua mot suat chieu + trang thai tung ghe */
    SeatLayoutResponse getSeatLayout(Long showtimeId, Long currentUserId);

    // ===== Quan tri (ADMIN) =====
    ShowtimeResponse create(ShowtimeRequest request);

    void delete(Long id);
}
