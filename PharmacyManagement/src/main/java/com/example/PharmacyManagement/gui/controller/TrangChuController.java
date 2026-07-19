package com.example.PharmacyManagement.gui.controller;

//Java imports
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

//JavaFX imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

//Models and services imports
import com.example.PharmacyManagement.model.HoaDon;
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.service.HoaDonService;
import com.example.PharmacyManagement.service.ThuocService;

//Component imports

//Utils imports

@Controller
public class TrangChuController {

    @FXML
    private Button btnQuickBanHang;

    @FXML
    private Button btnQuickNhapHang;

    @FXML
    private Button btnQuickQuanLyThuoc;

    @FXML
    private Button btnQuickKhachHang;

    @FXML
    private Button btnQuickLichSuBan;

    @FXML
    private Button btnQuickLichSuNhap;

    @FXML
    private Label lblHoaDonHomNay;

    @FXML
    private Label lblDoanhThuHomNay;

    @FXML
    private Label lblTonKhoThap;

    @FXML
    private Label lblGanHetHan;

    @FXML
    private Label lblHetHan;

    @Autowired
    @Lazy
    private ManHinhChinhController manHinhChinhController;

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private ThuocService thuocService;

    @FXML
    private void initialize() {
        List<HoaDon> danhSachHoaDon = hoaDonService.getAllHoaDon();
        List<Thuoc> danhSachThuoc = thuocService.getAllThuoc();
        capNhatThongTinTongQuan(danhSachHoaDon, danhSachThuoc);
    }

    @FXML
    private void handleQuickNavigation(ActionEvent event) {
        Button buttonClicked = (Button) event.getSource();
        String fxmlPath = "";

        if (buttonClicked == btnQuickBanHang) {
            fxmlPath = "/fxml/modules/BanHang.fxml";
        } else if (buttonClicked == btnQuickNhapHang) {
            fxmlPath = "/fxml/modules/NhapHang.fxml";
        } else if (buttonClicked == btnQuickQuanLyThuoc) {
            fxmlPath = "/fxml/modules/QuanLyThuoc.fxml";
        } else if (buttonClicked == btnQuickKhachHang) {
            fxmlPath = "/fxml/modules/KhachHang.fxml";
        } else if (buttonClicked == btnQuickLichSuBan) {
            fxmlPath = "/fxml/modules/LichSuBanThuoc.fxml";
        } else if (buttonClicked == btnQuickLichSuNhap) {
            fxmlPath = "/fxml/modules/LichSuNhapThuoc.fxml";
        }

        if (!fxmlPath.isEmpty()) {
            // Ra lệnh cho màn hình chính load module tương ứng vào Center
            manHinhChinhController.hienThiView(fxmlPath);
        }
    }

    private void capNhatThongTinTongQuan(List<HoaDon> danhSachHoaDon, List<Thuoc> danhSachThuoc) {
        LocalDate homNay = LocalDate.now();
        long soHoaDonHomNay = danhSachHoaDon.stream()
                .filter(hd -> homNay.equals(hd.getNgayBan()))
                .count();
        lblHoaDonHomNay.setText(String.valueOf(soHoaDonHomNay));

        BigDecimal doanhThuHomNay = danhSachHoaDon.stream()
                .filter(hd -> homNay.equals(hd.getNgayBan()))
                .map(HoaDon::getTongTien)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblDoanhThuHomNay.setText(dinhDangTien(doanhThuHomNay));

        long soThuocThap = danhSachThuoc.stream()
                .filter(thuoc -> thuoc.getSoLuongTon() <= 3)
                .count();
        lblTonKhoThap.setText(String.valueOf(soThuocThap));

        long tongSoLuongGanHetHan = danhSachThuoc.stream()
                .filter(t -> t != null)
                .filter(t -> t.getHanSuDung() != null)
                .filter(t -> {
                    long soNgayConLai = ChronoUnit.DAYS.between(homNay, t.getHanSuDung());
                    return soNgayConLai >= 0 && soNgayConLai <= 180;
                })
                .count();
        lblGanHetHan.setText(String.valueOf(tongSoLuongGanHetHan));

        long tongSoLuongHetHan = danhSachThuoc.stream()
                .filter(t -> t != null)
                .filter(t -> t.getHanSuDung() != null)
                .filter(t -> t.getHanSuDung().isBefore(homNay))
                .count();
        lblHetHan.setText(String.valueOf(tongSoLuongHetHan));
    }

    private String dinhDangTien(BigDecimal soTien) {
        if (soTien == null) {
            return "0 đ";
        }
        return String.format("%,.0f đ", soTien);
    }

}
