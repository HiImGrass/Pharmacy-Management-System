package com.example.PharmacyManagement.repository;

import com.example.PharmacyManagement.model.PhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuNhapRepository extends JpaRepository<PhieuNhap, Long> {
    // Tạo phiếu nhập mới
    // Xóa phi nhập
    // Sửa phiếu nhập
    // Lấy danh sách phiếu nhập
    // Lấy phiếu nhập theo ID
    // Lấy danh sách phiếu nhập theo ngày nhập
    List<PhieuNhap> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    default List<PhieuNhap> findByNgayTao(LocalDate ngay) {
        LocalDateTime start = ngay.atStartOfDay();
        LocalDateTime end = ngay.plusDays(1).atStartOfDay();
        return findByCreatedAtBetween(start, end);
    }
}
