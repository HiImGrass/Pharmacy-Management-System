package com.example.PharmacyManagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "phieu_nhap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nha_cung_cap", nullable = false)
    private String nhaCungCap;

    @Column(name = "ghi_chu", nullable = true)
    private String ghiChu;

    @Column(name = "anh_hoa_don_nhap_path", nullable = true)
    private String anhHoaDonNhapPath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
