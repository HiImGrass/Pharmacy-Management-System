package com.example.PharmacyManagement.repository;

import com.example.PharmacyManagement.model.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {
    List<HoaDon> findByNgayBan(LocalDate ngayBan);

    // Lấy hóa đơn theo khoảng ngày
    List<HoaDon> findByNgayBanBetween(LocalDate start, LocalDate end);

    List<HoaDon> findByKhachHangId(Long khachHangId);

    List<HoaDon> findByTenKhachHangContainingIgnoreCase(String tenKhachHang);
    // Tổng tiền bán theo khoảng ngày
    @Query("SELECT SUM(h.tongTien) FROM HoaDon h WHERE h.ngayBan BETWEEN :start AND :end")
    BigDecimal sumTongTienByNgayBanBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
