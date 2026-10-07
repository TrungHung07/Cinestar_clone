package com.cinestar.service.impl;

import com.cinestar.dto.request.PromotionRequest;
import com.cinestar.dto.response.PromotionResponse;
import com.cinestar.entity.Promotion;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.repository.PromotionRepository;
import com.cinestar.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    @Override
    @Cacheable(value = "promotions", key = "'all'")
    @Transactional(readOnly = true)
    public List<PromotionResponse> getPromotions() {
        return promotionRepository.findAllByActiveTrue().stream()
                .map(p -> PromotionResponse.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .imageUrl(p.getImageUrl())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public PromotionResponse create(PromotionRequest request) {
        Promotion promotion = Promotion.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() == null || request.getActive())
                .build();
        promotion = promotionRepository.save(promotion);
        return toResponse(promotion);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public PromotionResponse update(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi id=" + id));
        promotion.setTitle(request.getTitle());
        promotion.setDescription(request.getDescription());
        promotion.setImageUrl(request.getImageUrl());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        if (request.getActive() != null) {
            promotion.setActive(request.getActive());
        }
        promotion = promotionRepository.save(promotion);
        return toResponse(promotion);
    }

    @Override
    @Transactional
    @CacheEvict(value = "promotions", allEntries = true)
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }

    private PromotionResponse toResponse(Promotion p) {
        return PromotionResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .imageUrl(p.getImageUrl())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .build();
    }
}
