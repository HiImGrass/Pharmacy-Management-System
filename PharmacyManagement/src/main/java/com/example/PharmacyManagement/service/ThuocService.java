package com.example.PharmacyManagement.service;

import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.repository.ThuocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThuocService {

    @Autowired
    private ThuocRepository thuocRepository;

    /**
     * 1. READ - Lấy toàn bộ danh sách thuốc để hiển thị lên bảng GUI
     * Phục vụ các cột: Tên thuốc, Số lượng tồn, Đơn vị, Giá nhập, Giá bán sỉ, Hạn sử dụng, Mô tả
     */
    public List<Thuoc> getAllThuoc() {
        return thuocRepository.findAll();
    }
    
    /**
     * 2. CREATE - Nút "Thêm mới thuốc" vào danh mục quầy
     */
    @Transactional
    public Thuoc themThuoc(Thuoc thuoc) {
        return thuocRepository.save(thuoc);
    }

    /**
     * 3. UPDATE - Nút "Sửa" thông tin một loại thuốc đang có
     */
    @Transactional
    public Thuoc suaThuoc(Long id, Thuoc thuocCapNhat) {
        // Kiểm tra xem thuốc cần sửa có tồn tại trong DB không
        Thuoc thuocHienTai = thuocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc có ID: " + id));

        // Tiến hành cập nhật toàn bộ các trường thông tin từ form GUI truyền xuống
        thuocHienTai.setTenThuoc(thuocCapNhat.getTenThuoc());
        thuocHienTai.setDonVi(thuocCapNhat.getDonVi());
        thuocHienTai.setGiaNhap(thuocCapNhat.getGiaNhap());
        thuocHienTai.setGiaBanSi(thuocCapNhat.getGiaBanSi());
        thuocHienTai.setSoLuongTon(thuocCapNhat.getSoLuongTon());
        thuocHienTai.setHanSuDung(thuocCapNhat.getHanSuDung());
        thuocHienTai.setMoTa(thuocCapNhat.getMoTa());

        // Lưu lại trạng thái mới xuống Database
        return thuocRepository.save(thuocHienTai);
    }

    /**
     * 4. DELETE - Nút "Xóa" thuốc khỏi hệ thống
     */
    @Transactional
    public void xoaThuoc(Long id) {
        // Kiểm tra xem ID thuốc có tồn tại hay không trước khi thực hiện lệnh xóa
        if (!thuocRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy mặt hàng thuốc này để xóa!");
        }
        thuocRepository.deleteById(id);
    }

    /**
     * 5. TÌM KIẾM - Hàm tìm kiếm thuốc theo tên hoặc các tiêu chí khác (nếu có)
     * Phục vụ chức năng tìm kiếm nhanh trên giao diện quản lý thuốc
     */
    @Transactional
    public List<Thuoc> timKiemThuoc(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return thuocRepository.findAll(); // Nếu ô tìm kiếm trống, tự động trả về tất cả
        }
        return thuocRepository.findByTenThuocContainingIgnoreCase(tuKhoa);
    }
}