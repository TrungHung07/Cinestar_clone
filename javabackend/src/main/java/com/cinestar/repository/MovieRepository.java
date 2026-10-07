package com.cinestar.repository;

import com.cinestar.entity.Movie;
import com.cinestar.entity.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * DANH SACH + PHAN TRANG: khong dung @EntityGraph join fetch collection voi
     * Pageable (Hibernate se pagination in-memory - warning HHH90003004). N+1
     * genres duoc chong bang @BatchSize tren Movie.genres.
     */
    Page<Movie> findAll(Pageable pageable);

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    // KHONG phan trang - join fetch genres trong 1 query
    @EntityGraph(attributePaths = "genres")
    Optional<Movie> findBySlug(String slug);

    @EntityGraph(attributePaths = "genres")
    Optional<Movie> findWithGenresById(Long id);

    Page<Movie> findByTitleContainingIgnoreCaseAndStatus(String title, MovieStatus status, Pageable pageable);

    boolean existsBySlug(String slug);

    /** Dem so suat chieu toi thieu cua mot phim (dung trang chu) */
    @Query("select count(s) from Showtime s where s.movie.id = :movieId and s.startTime > current_timestamp")
    long countUpcomingShowtimes(@Param("movieId") Long movieId);
}
