package com.example.PharmacyManagement.repository;

import com.example.PharmacyManagement.model.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Long> {
    List<ChiTietHoaDon> findByHoaDonId(Long hoaDonId);
    List<ChiTietHoaDon> findByThuocId(Long thuocId);
    void deleteByHoaDonId(Long hoaDonId);
}
