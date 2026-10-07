package com.cinestar.service;

import com.cinestar.dto.request.ComboRequest;
import com.cinestar.dto.response.ComboResponse;

import java.util.List;

public interface ComboService {

    List<ComboResponse> getCombos();

    ComboResponse create(ComboRequest request);

    ComboResponse update(Long id, ComboRequest request);

    void delete(Long id);
}
