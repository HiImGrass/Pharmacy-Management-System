package com.example.PharmacyManagement.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonRequestDTO {
    private Long khachHangId; // ID khách chọn từ ô Dropdown (có thể null nếu là khách lẻ)
    private String ghiChu;     // Ô nhập ghi chú trên giao diện
    private LocalDate ngayBan;    // Ngày bán chọn từ DatePicker
    private List<ChiTietHoaDonRequestDTO> danhSachThuocMua; // Danh sách các dòng thuốc trong bảng nhập động
}