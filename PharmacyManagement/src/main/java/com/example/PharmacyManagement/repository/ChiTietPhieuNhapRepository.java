package com.example.PharmacyManagement.repository;

import com.example.PharmacyManagement.model.ChiTietPhieuNhap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietPhieuNhapRepository extends JpaRepository<ChiTietPhieuNhap, Long> {
    List<ChiTietPhieuNhap> findByPhieuNhapId(Long phieuNhapId);

    List<ChiTietPhieuNhap> findByThuocId(Long thuocId);

    void deleteByPhieuNhapId(Long phieuNhapId);

    // 4️⃣ NÂNG CAO (Tùy chọn): Lấy giá nhập gần đây nhất của một loại thuốc
    // Chức năng: Khi nhân viên tạo phiếu nhập mới và chọn thuốc A, hệ thống sẽ tự
    // động gợi ý "Giá nhập lần trước là bao nhiêu" để họ tham khảo.
    @Query("SELECT c FROM ChiTietPhieuNhap c WHERE c.thuoc.id = :thuocId ORDER BY c.phieuNhap.createdAt DESC, c.id DESC")
    List<ChiTietPhieuNhap> findLatestImportByThuocId(@Param("thuocId") Long thuocId);
}
