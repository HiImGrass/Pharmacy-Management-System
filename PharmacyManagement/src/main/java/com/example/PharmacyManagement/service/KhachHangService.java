package com.example.PharmacyManagement.service;

import com.example.PharmacyManagement.model.KhachHang;
import com.example.PharmacyManagement.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    /**
     * 1. READ - Lấy toàn bộ danh sách khách hàng để hiển thị lên bảng GUI
     */
    public List<KhachHang> layTatCaKhachHang() {
        return khachHangRepository.findAll();
    }

    /**
     * 2. CREATE - Nút "Thêm mới" khách hàng
     */
    @Transactional
    public KhachHang themKhachHang(KhachHang khachHang) {
        // Bạn có thể thêm logic check trùng số điện thoại ở đây nếu muốn
        return khachHangRepository.save(khachHang);
    }

    /**
     * 3. UPDATE - Nút "Sửa" thông tin khách hàng
     */
    @Transactional
    public KhachHang suaKhachHang(Long id, KhachHang khachHangCapNhat) {
        // Kiểm tra xem khách hàng có tồn tại trong hệ thống không
        KhachHang khachHangHienTai = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có ID: " + id));

        // Tiến hành ghi đè thông tin mới lên dòng cũ
        khachHangHienTai.setTenKhachHang(khachHangCapNhat.getTenKhachHang());
        khachHangHienTai.setSdt(khachHangCapNhat.getSdt());

        // Lưu lại vào Database
        return khachHangRepository.save(khachHangHienTai);
    }

    /**
     * 4. DELETE - Nút "Xóa" khách hàng
     */
    @Transactional
    public void xoaKhachHang(Long id) {
        // Kiểm tra sự tồn tại trước khi xóa để tránh lỗi sập app
        if (!khachHangRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khách hàng để xóa!");
        }
        khachHangRepository.deleteById(id);
    }

    /**
     * 🔍 SEARCH - Xử lý dữ liệu khi người dùng gõ vào ô "Field Search"
     */
    public List<KhachHang> timKiemKhachHang(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return khachHangRepository.findAll(); // Nếu ô tìm kiếm trống, tự động trả về tất cả
        }
        // Gọi hàm tìm kiếm đa năng đã viết ở Repository
        return khachHangRepository.findByTenKhachHangContainingIgnoreCaseOrSdtContaining(tuKhoa, tuKhoa);
    }
}