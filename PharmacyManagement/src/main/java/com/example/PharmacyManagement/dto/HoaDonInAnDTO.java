package com.example.PharmacyManagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class HoaDonInAnDTO {
    private String tenKhachHang;
    private LocalDate ngayBan;
    private BigDecimal tongTien;
    private String ghiChu;

    private List<ChiTietHoaDonInAnDTO> chiTietHoaDon;
}
