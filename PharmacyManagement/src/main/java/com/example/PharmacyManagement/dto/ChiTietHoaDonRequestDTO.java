package com.example.PharmacyManagement.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietHoaDonRequestDTO {
    private Long thuocId; // ID thuốc chọn trên dòng của bảng GUI
    private int soLuong; // Số lượng khách mua trên dòng đó

    private BigDecimal donGia; // Giá bán của thuốc trên dòng đó
}