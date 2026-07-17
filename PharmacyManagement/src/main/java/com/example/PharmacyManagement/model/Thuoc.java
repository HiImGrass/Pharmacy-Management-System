package com.example.PharmacyManagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "thuoc")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Thuoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_thuoc", nullable = false)
    private String tenThuoc;

    @Column(name = "don_vi", nullable = false)
    private String donVi;

    @Column(name = "gia_nhap", nullable = false, precision = 19, scale = 2)
    private BigDecimal giaNhap;

    @Column(name = "gia_ban_si", nullable = false, precision = 19, scale = 2)
    private BigDecimal giaBanSi;

    @Column(name = "so_luong_ton", nullable = false)
    private int soLuongTon;

    @Column(name = "han_su_dung", nullable = true)
    private LocalDate hanSuDung;

    @Column(name = "mo_ta")
    private String moTa;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
