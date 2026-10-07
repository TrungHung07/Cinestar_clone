package com.cinestar.service;

import com.cinestar.dto.request.PromotionRequest;
import com.cinestar.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {

    List<PromotionResponse> getPromotions();

    PromotionResponse create(PromotionRequest request);

    PromotionResponse update(Long id, PromotionRequest request);

    void delete(Long id);
}
