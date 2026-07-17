package com.example.PharmacyManagement.service;

//Model
import com.example.PharmacyManagement.model.ChiTietHoaDon;
import com.example.PharmacyManagement.model.HoaDon;
import com.example.PharmacyManagement.model.KhachHang;
import com.example.PharmacyManagement.model.Thuoc;
//Repository
import com.example.PharmacyManagement.repository.ChiTietHoaDonRepository;
import com.example.PharmacyManagement.repository.HoaDonRepository;
import com.example.PharmacyManagement.repository.KhachHangRepository;
import com.example.PharmacyManagement.repository.ThuocRepository;
//DTO
import com.example.PharmacyManagement.dto.ChiTietHoaDonRequestDTO;
import com.example.PharmacyManagement.dto.HoaDonRequestDTO;
//Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class HoaDonService {
    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ThuocRepository thuocRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    /*
     * 1. CREATE - Tạo mới một hóa đơn bán hàng
     * Lưu ý: Không thể tạo hóa đơn bán hàng với khách hàng chưa tạo.
     */
    @Transactional
    public HoaDon createHoaDon(HoaDonRequestDTO hoaDonRequest) {
        if (hoaDonRequest == null || hoaDonRequest.getDanhSachThuocMua() == null
                || hoaDonRequest.getDanhSachThuocMua().isEmpty()) {
            throw new RuntimeException("Hóa đơn không có chi tiết thuốc");
        }

        // Tạo hóa đơn mới
        HoaDon hoaDon = new HoaDon();
        hoaDon.setNgayBan(hoaDonRequest.getNgayBan() != null ? hoaDonRequest.getNgayBan() : LocalDate.now());
        hoaDon.setTongTien(BigDecimal.ZERO);

        // Lấy thông tin khách hàng
        KhachHang khachHang = khachHangRepository.findById(hoaDonRequest.getKhachHangId())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
        hoaDon.setKhachHang(khachHang);
        hoaDon.setTenKhachHang(khachHang.getTenKhachHang());

        // Cần lưu hóa đơn trước để có ID cho bảng chi tiết
        hoaDon = hoaDonRepository.save(hoaDon);

        // Tính tổng tiền và lưu chi tiết hóa đơn
        BigDecimal tongTien = BigDecimal.ZERO;
        for (ChiTietHoaDonRequestDTO chiTietRequest : hoaDonRequest.getDanhSachThuocMua()) {
            Thuoc thuoc = thuocRepository.findById(chiTietRequest.getThuocId())
                    .orElseThrow(() -> new RuntimeException("Thuốc không tồn tại"));

            if (thuoc.getSoLuongTon() < chiTietRequest.getSoLuong()) {
                throw new RuntimeException("Số lượng thuốc không đủ");
            }

            // Cập nhật số lượng tồn của thuốc
            thuoc.setSoLuongTon(thuoc.getSoLuongTon() - chiTietRequest.getSoLuong());
            thuocRepository.save(thuoc);

            // Tạo chi tiết hóa đơn
            ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
            chiTietHoaDon.setHoaDon(hoaDon);
            chiTietHoaDon.setThuoc(thuoc);
            chiTietHoaDon.setTenThuoc(thuoc.getTenThuoc() != null ? thuoc.getTenThuoc() : "");
            chiTietHoaDon.setSoLuong(chiTietRequest.getSoLuong());
            chiTietHoaDon.setDonVi(thuoc.getDonVi() != null ? thuoc.getDonVi() : "");
            BigDecimal donGiaThucTe = chiTietRequest.getDonGia();

            if (donGiaThucTe == null || donGiaThucTe.compareTo(BigDecimal.ZERO) < 0) {
                donGiaThucTe = thuoc.getGiaBanSi() != null
                        ? thuoc.getGiaBanSi()
                        : BigDecimal.ZERO;
            }

            chiTietHoaDon.setDonGia(donGiaThucTe);

            BigDecimal thanhTien = donGiaThucTe.multiply(
                    BigDecimal.valueOf(chiTietRequest.getSoLuong()));

            chiTietHoaDon.setThanhTien(thanhTien);
            chiTietHoaDonRepository.save(chiTietHoaDon);

            tongTien = tongTien.add(thanhTien);
        }

        hoaDon.setTongTien(tongTien);
        return hoaDonRepository.save(hoaDon);
    }

    /*
     * 2.1 READ - Lấy thông tin chi tiết một hóa đơn bán hàng theo ID
     * Lưu ý: Không thể lấy thông tin hóa đơn nếu ID không tồn tại.
     */
    public HoaDon getHoaDonById(Long id) {
        return hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
    }

    /*
     * 2.2 READ - Lấy danh sách tất cả hóa đơn bán hàng
     * Lưu ý: Có thể lọc hóa đơn theo ngày bán hoặc theo khách hàng nếu cần.
     */
    public List<HoaDon> getAllHoaDon() {
        return hoaDonRepository.findAll();
    }

    public List<HoaDon> getHoaDonByKhachHangName(String tenKhachHang) {
        return hoaDonRepository.findByTenKhachHangContainingIgnoreCase(tenKhachHang);
    }

    @Transactional
    public HoaDon capNhatAnhHoaDon(
            Long hoaDonId,
            String duongDanAnh) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy hóa đơn có ID: "
                                + hoaDonId));

        hoaDon.setAnhHoaDonPath(duongDanAnh);

        return hoaDonRepository.save(hoaDon);
    }

}
