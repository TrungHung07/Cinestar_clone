package com.cinestar.repository;

import com.cinestar.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    /**
     * CHONG N+1: 1 query duy nhat lay suat chieu + phim + phong + rap
     * thay vi 1 + 3N query khi duyet danh sach.
     */
    @Query("""
            select distinct s from Showtime s
            join fetch s.movie m
            join fetch s.room r
            join fetch r.cinema c
            where (:movieId is null or m.id = :movieId)
              and (:cinemaId is null or c.id = :cinemaId)
              and s.startTime >= :from
              and s.startTime < :to
            order by s.startTime
            """)
    List<Showtime> findWithDetails(@Param("movieId") Long movieId,
                                   @Param("cinemaId") Long cinemaId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);

    @EntityGraph(attributePaths = {"movie", "room", "room.cinema"})
    Optional<Showtime> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"movie", "room", "room.cinema"})
    Optional<Showtime> findById(Long id);
}
