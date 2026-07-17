package com.example.PharmacyManagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "chi_tiet_hoa_don")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietHoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "thuoc_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Thuoc thuoc;

    @Column(name = "ten_thuoc", nullable = false)
    private String tenThuoc;

    @Column(name = "so_luong", nullable = false)
    private int soLuong;

    @Column(name = "don_vi", nullable = false)
    private String donVi;

    @Column(name = "don_gia", nullable = false, precision = 19, scale = 2)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", nullable = false, precision = 19, scale = 2)
    private BigDecimal thanhTien;
}
