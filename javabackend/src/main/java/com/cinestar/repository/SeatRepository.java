package com.cinestar.repository;

import com.cinestar.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByRoomIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(Long roomId);

    /**
     * KHOA BI QUAN (SELECT ... FOR UPDATE): dung khi xac nhan thanh toan,
     * dam bao 2 request xac nhan cung mot ghe bi tuan tu hoa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids")
    List<Seat> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
