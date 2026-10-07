package com.cinestar.service.impl;

import com.cinestar.dto.request.ComboRequest;
import com.cinestar.dto.response.ComboResponse;
import com.cinestar.entity.Combo;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.repository.ComboRepository;
import com.cinestar.service.ComboService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboRepository comboRepository;

    @Override
    @Cacheable(value = "combos", key = "'all'")
    @Transactional(readOnly = true)
    public List<ComboResponse> getCombos() {
        return comboRepository.findAllByActiveTrue().stream()
                .map(combo -> ComboResponse.builder()
                        .id(combo.getId())
                        .name(combo.getName())
                        .description(combo.getDescription())
                        .price(combo.getPrice())
                        .imageUrl(combo.getImageUrl())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "combos", allEntries = true)
    public ComboResponse create(ComboRequest request) {
        Combo combo = Combo.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() == null || request.getActive())
                .build();
        combo = comboRepository.save(combo);
        return toResponse(combo);
    }

    @Override
    @Transactional
    @CacheEvict(value = "combos", allEntries = true)
    public ComboResponse update(Long id, ComboRequest request) {
        Combo combo = comboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy combo id=" + id));
        combo.setName(request.getName());
        combo.setDescription(request.getDescription());
        combo.setPrice(request.getPrice());
        combo.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            combo.setActive(request.getActive());
        }
        combo = comboRepository.save(combo);
        return toResponse(combo);
    }

    @Override
    @Transactional
    @CacheEvict(value = "combos", allEntries = true)
    public void delete(Long id) {
        comboRepository.deleteById(id);
    }

    private ComboResponse toResponse(Combo combo) {
        return ComboResponse.builder()
                .id(combo.getId())
                .name(combo.getName())
                .description(combo.getDescription())
                .price(combo.getPrice())
                .imageUrl(combo.getImageUrl())
                .build();
    }
}
