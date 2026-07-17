package com.example.PharmacyManagement.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuNhapRequestDTO {
    private String nhaCungCap; // Tên nhà cung cấp nhập từ Form
    private String ghiChu;      // Ghi chú phiếu nhập
    private List<ChiTietPhieuNhapRequestDTO> danhSachThuocNhap; // Bảng nhập động các loại thuốc
}