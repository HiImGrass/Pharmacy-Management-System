package com.example.PharmacyManagement.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietPhieuNhapRequestDTO {
    
    private Long thuocId;    // ID của thuốc được chọn để nhập kho
    
    private int soLuong;     // Số lượng hộp/vỉ nhập thêm vào kho
    
    private BigDecimal donGia; // Giá nhập của đợt này (để cập nhật lại giá vốn trong DB)
}