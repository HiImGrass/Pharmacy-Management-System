package com.example.PharmacyManagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PhieuNhapInAnDTO {
    private String tenNhaCungCap;
    private LocalDate ngayNhap;
    private BigDecimal tongTien;
    private String moTa;
    
    private List<ChiTietPhieuNhapInAnDTO> chiTietPhieuNhap;
}
