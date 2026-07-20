package com.example.PharmacyManagement.service;

import com.example.PharmacyManagement.dto.ChiTietPhieuNhapRequestDTO;
import com.example.PharmacyManagement.dto.PhieuNhapRequestDTO;
import com.example.PharmacyManagement.model.ChiTietPhieuNhap;
import com.example.PharmacyManagement.model.PhieuNhap;
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.repository.ChiTietPhieuNhapRepository;
import com.example.PharmacyManagement.repository.PhieuNhapRepository;
import com.example.PharmacyManagement.repository.ThuocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PhieuNhapService {

    @Autowired
    private PhieuNhapRepository phieuNhapRepository;

    @Autowired
    private ChiTietPhieuNhapRepository chiTietPhieuNhapRepository;

    @Autowired
    private ThuocRepository thuocRepository;

    /**
     * 1. CREATE - Nghiệp vụ Nhập Kho (Bấm nút "Xác nhận nhập hàng" trên GUI)
     * Thêm số lượng vào kho, cập nhật giá nhập mới cho Thuốc, tự động tính tổng
     * tiền phiếu nhập
     */
    @Transactional
    public PhieuNhap nhapHangVaoKho(PhieuNhapRequestDTO request) {
        // Bước 1: Khởi tạo thực thể Phiếu nhập cha
        PhieuNhap phieuNhap = new PhieuNhap();
        phieuNhap.setNhaCungCap(request.getNhaCungCap());
        phieuNhap.setGhiChu(request.getGhiChu());

        // Lưu nháp phiếu nhập cha để lấy ID sinh tự động trước
        PhieuNhap phieuNhapDaLuu = phieuNhapRepository.save(phieuNhap);

        // Bước 2: Duyệt danh sách thuốc nhập động từ GUI
        for (ChiTietPhieuNhapRequestDTO item : request.getDanhSachThuocNhap()) {
            Thuoc thuocTrongKho = thuocRepository.findById(item.getThuocId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc có ID: " + item.getThuocId()));

            // NGHIỆP VỤ TĂNG KHO: Cộng dồn số lượng vừa nhập vào số lượng tồn hiện tại
            thuocTrongKho.setSoLuongTon(thuocTrongKho.getSoLuongTon() + item.getSoLuong());

            thuocRepository.save(thuocTrongKho);

            // Bước 3: Tạo dòng chi tiết phiếu nhập
            ChiTietPhieuNhap chiTiet = new ChiTietPhieuNhap();
            chiTiet.setPhieuNhap(phieuNhapDaLuu);
            chiTiet.setThuoc(thuocTrongKho);
            chiTiet.setTenThuoc(thuocTrongKho.getTenThuoc());
            chiTiet.setDonVi(thuocTrongKho.getDonVi());
            chiTiet.setDonGia(item.getDonGia());
            chiTiet.setMoTa(thuocTrongKho.getMoTa());
            chiTiet.setSoLuong(item.getSoLuong());

            // Lưu dòng chi tiết xuống DB
            chiTietPhieuNhapRepository.save(chiTiet);
        }

        // Bước 4: Cập nhật lại tổng tiền chuẩn cuối cùng cho Phiếu nhập cha và lưu lại
        return phieuNhapRepository.save(phieuNhapDaLuu);
    }

    /**
     * 2.1 READ (Danh sách) - Lấy lịch sử tất cả các phiếu nhập hàng
     */
    public List<PhieuNhap> getAllPhieuNhap() {
        return phieuNhapRepository.findAll();
    }

    /**
     * 2.2 READ (Chi tiết) - Xem các dòng mặt hàng cụ thể bên trong 1 phiếu nhập cũ
     */
    public List<ChiTietPhieuNhap> layChiTietCuaPhieuNhap(Long phieuNhapId) {
        // Khớp đúng với hàm findByPhieuNhapId sẵn có của bạn
        return chiTietPhieuNhapRepository.findByPhieuNhapId(phieuNhapId);
    }

    /**
     * 3. UPDATE - Cập nhật thông tin phiếu nhập hàng
     * Lưu ý nghiệp vụ: Không thể sửa đổi phiếu nhập đã tạo vì đã ảnh hưởng đến tồn
     * kho,
     * giá vốn, và báo cáo thống kê.
     * Nếu cần chỉnh sửa, phải xóa phiếu nhập cũ và tạo lại mới.
     */
    /**
     * 4. DELETE - Hủy/Xóa một phiếu nhập hàng
     * Lưu ý nghiệp vụ: Phải dọn sạch các dòng chi tiết trước để tránh lỗi khóa
     * ngoại Foreign Key giống như Hóa đơn.
     */
    @Transactional
    public void xoaPhieuNhap(Long id) {
        if (!phieuNhapRepository.existsById(id)) {
            throw new RuntimeException("Phiếu nhập cần xóa không tồn tại!");
        }
        // Gọi hàm deleteByPhieuNhapId đã viết sẵn trong Repository của bạn để xóa dữ
        // liệu con trước
        chiTietPhieuNhapRepository.deleteByPhieuNhapId(id);

        // Sau đó xóa phiếu nhập cha
        phieuNhapRepository.deleteById(id);
    }

    @Transactional
    public PhieuNhap capNhatAnhPhieuNhap(
            Long phieuNhapId,
            String duongDanAnh) {
        if (phieuNhapId == null) {
            throw new IllegalArgumentException(
                    "Mã phiếu nhập không được để trống.");
        }

        if (duongDanAnh == null || duongDanAnh.isBlank()) {
            throw new IllegalArgumentException(
                    "Đường dẫn ảnh phiếu nhập không được để trống.");
        }

        PhieuNhap phieuNhap = phieuNhapRepository
                .findById(phieuNhapId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phiếu nhập PN"
                                + phieuNhapId));

        phieuNhap.setAnhHoaDonNhapPath(duongDanAnh);

        return phieuNhapRepository.save(phieuNhap);
    }
}