package com.cinestar.repository;

import com.cinestar.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @EntityGraph(attributePaths = "cinema")
    List<Room> findAllByCinemaId(Long cinemaId);

    long countByCinemaId(Long cinemaId);
}
