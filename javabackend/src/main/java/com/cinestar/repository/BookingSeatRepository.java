package com.cinestar.repository;

import com.cinestar.entity.BookingSeat;
import com.cinestar.entity.enums.SeatBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    /** Cac ghe dang giu / da ban cua mot suat chieu (dung ve so do ghe) */
    @Query("""
            select bs from BookingSeat bs
            where bs.showtime.id = :showtimeId
              and bs.status in :statuses
            """)
    List<BookingSeat> findActiveByShowtimeId(@Param("showtimeId") Long showtimeId,
                                             @Param("statuses") Collection<SeatBookingStatus> statuses);

    /** Giong tren nhung join fetch luon booking + user de biet AI dang giu ghe (1 query, chong N+1) */
    @Query("""
            select bs from BookingSeat bs
            join fetch bs.booking b
            join fetch b.user u
            where bs.showtime.id = :showtimeId
              and bs.status in :statuses
            """)
    List<BookingSeat> findActiveByShowtimeIdWithHolder(@Param("showtimeId") Long showtimeId,
                                                       @Param("statuses") Collection<SeatBookingStatus> statuses);

    /** Kiem tra ghe co bi giu/ban boi DON KHAC chua (chong trung ghe) */
    @Query("""
            select count(bs) > 0 from BookingSeat bs
            where bs.showtime.id = :showtimeId
              and bs.seat.id in :seatIds
              and bs.status in :statuses
              and (:excludeBookingId is null or bs.booking.id <> :excludeBookingId)
            """)
    boolean existsActiveSeats(@Param("showtimeId") Long showtimeId,
                              @Param("seatIds") Collection<Long> seatIds,
                              @Param("statuses") Collection<SeatBookingStatus> statuses,
                              @Param("excludeBookingId") Long excludeBookingId);

    List<BookingSeat> findByBookingId(Long bookingId);
}
