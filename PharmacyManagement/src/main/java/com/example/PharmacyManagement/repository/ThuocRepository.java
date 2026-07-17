package com.example.PharmacyManagement.repository;

import com.example.PharmacyManagement.model.Thuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ThuocRepository extends JpaRepository<Thuoc, Long> {
    // Các phương thức truy vấn tùy chỉnh có thể được thêm vào đây nếu cần
    List<Thuoc> findByTenThuocContainingIgnoreCase(String tenThuoc);

    boolean existsByTenThuocAndDonViIgnoreCase(String tenThuoc, String donVi);

    boolean existsByTenThuocAndDonViIgnoreCaseAndGiaNhapAndMoTaIgnoreCase(String tenThuoc, String donVi, BigDecimal giaNhap, String moTa);

    Optional<Thuoc> findFirstByTenThuocAndDonViAndGiaNhapAndMoTaIgnoreCase(String tenThuoc, String donVi, BigDecimal giaNhap, String moTa);

}
