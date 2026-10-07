package com.cinestar.service;

import com.cinestar.dto.request.MovieRequest;
import com.cinestar.dto.response.MovieDetailResponse;
import com.cinestar.dto.response.MovieResponse;
import com.cinestar.entity.enums.MovieStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovieService {

    /** Danh sach phim trang chu (CACHE Redis 10 phut) */
    List<MovieResponse> getNowShowing();

    List<MovieResponse> getUpcoming();

    List<MovieResponse> getFeatured();

    /** Danh sach co phan trang + tim kiem (dung cho admin) */
    Page<MovieResponse> getMovies(MovieStatus status, String search, int page, int size);

    MovieDetailResponse getBySlug(String slug);

    // ===== Quan tri (ADMIN) =====
    MovieResponse create(MovieRequest request);

    MovieResponse update(Long id, MovieRequest request);

    void delete(Long id);
}
