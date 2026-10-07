package com.cinestar.service;

import com.cinestar.dto.request.CinemaRequest;
import com.cinestar.dto.response.CinemaResponse;

import java.util.List;

public interface CinemaService {

    List<CinemaResponse> getCinemas();

    CinemaResponse getBySlug(String slug);

    CinemaResponse create(CinemaRequest request);

    CinemaResponse update(Long id, CinemaRequest request);

    void delete(Long id);
}
