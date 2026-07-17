package com.example.PharmacyManagement.config;

import com.example.PharmacyManagement.model.KhachHang;
import com.example.PharmacyManagement.repository.KhachHangRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("sqlite")
public class DuLieuMacDinhInitializer
        implements CommandLineRunner {

    private static final String TEN_KHACH_LE = "Khách lẻ";
    private static final String SDT_KHACH_LE = "";

    private final KhachHangRepository khachHangRepository;

    public DuLieuMacDinhInitializer(
            KhachHangRepository khachHangRepository) {
        this.khachHangRepository = khachHangRepository;
    }

    @Override
    public void run(String... args) {
        KhachHang khachLe = khachHangRepository
                .findBySdtContaining(SDT_KHACH_LE)
                .stream()
                .findFirst()
                .orElse(new KhachHang());

        boolean canLuu = khachLe.getId() == null;

        if (!TEN_KHACH_LE.equals(khachLe.getTenKhachHang())) {
            khachLe.setTenKhachHang(TEN_KHACH_LE);
            canLuu = true;
        }

        if (khachLe.getSdt() == null) {
            khachLe.setSdt(SDT_KHACH_LE);
            canLuu = true;
        }

        if (canLuu) {
            khachHangRepository.save(khachLe);

            System.out.println(
                    "Đã tạo khách hàng mặc định: "
                            + TEN_KHACH_LE);
        }
    }
}