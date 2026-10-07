package com.cinestar.repository;

import com.cinestar.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findAllByActiveTrue();

    Optional<Cinema> findBySlug(String slug);
}
