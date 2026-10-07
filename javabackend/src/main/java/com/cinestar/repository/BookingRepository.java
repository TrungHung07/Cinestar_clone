package com.cinestar.repository;

import com.cinestar.entity.Booking;
import com.cinestar.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Ve cua user - fetch day du thong tin hien thi trong 1 query */
    @EntityGraph(attributePaths = {"seats", "seats.seat", "showtime", "showtime.movie", "payment"})
    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "seats", "seats.seat", "items", "items.combo",
            "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema", "payment"})
    Optional<Booking> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"user", "seats", "seats.seat", "items", "items.combo",
            "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema", "payment"})
    Optional<Booking> findWithDetailsByBookingCode(String bookingCode);

    @EntityGraph(attributePaths = {"seats", "seats.seat", "items", "items.combo", "showtime", "showtime.movie", "payment"})
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime expiresAt);

    /** Bao cao doanh thu - native query, tra ve [ngay, so ve, doanh thu] */
    @Query(value = """
            select date(b.created_at)          as booking_date,
                   count(distinct b.id)        as total_bookings,
                   sum(bs.price)               as revenue
            from bookings b
            join booking_seats bs on bs.booking_id = b.id
            where b.status = 'CONFIRMED'
              and b.created_at >= :from and b.created_at < :to
            group by date(b.created_at)
            order by booking_date
            """, nativeQuery = true)
    List<Object[]> revenueByDate(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Top phim co doanh thu cao nhat trong khoang thoi gian */
    @Query(value = """
            select m.title                     as movie_title,
                   count(distinct b.id)        as total_bookings,
                   sum(bs.price)               as revenue
            from bookings b
            join booking_seats bs on bs.booking_id = b.id
            join showtimes st   on st.id = b.showtime_id
            join movies m       on m.id = st.movie_id
            where b.status = 'CONFIRMED'
              and b.created_at >= :from and b.created_at < :to
            group by m.title
            order by revenue desc
            limit :limit
            """, nativeQuery = true)
    List<Object[]> topMovies(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("limit") int limit);
}
