package com.example.PharmacyManagement.repository;

import com.example.PharmacyManagement.model.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    List<KhachHang> findByTenKhachHangContainingIgnoreCaseOrSdtContaining(String tenKhachHang, String sdt);

    List<KhachHang> findByTenKhachHangContainingIgnoreCase(String tenKhachHang);

    List<KhachHang> findBySdtContaining(String sdt);
}
