package com.example.PharmacyManagement.gui.controller.sidebar;

import com.example.PharmacyManagement.gui.controller.ManHinhChinhController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Controller
public class SidebarController {

    @FXML
    private Button btnTrangChu;
    @FXML
    private Button btnKhachHang;
    @FXML
    private Button btnQuanLyThuoc;
    @FXML
    private Button btnBanHang;
    @FXML
    private Button btnNhapHang;
    @FXML
    private Button btnLichSuBanHang;
    @FXML
    private Button btnLichSuNhapHang;

    @Autowired
    @Lazy // Dùng @Lazy để tránh lỗi vòng lặp phụ thuộc (Circular Dependency) khi hai
    //    // controller tham chiếu nhau
    private ManHinhChinhController manHinhChinhController;

    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String fxmlPath = ""; 

        // Tạo hiệu ứng đổi màu nút được chọn
        clearButtonStyles();
        clickedButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-cursor: hand;");

        // Định tuyến sang thư mục chứa các Modules
        if (clickedButton == btnTrangChu) {
            fxmlPath = "/fxml/modules/TrangChu.fxml";
        } else if (clickedButton == btnQuanLyThuoc) {
            fxmlPath = "/fxml/modules/QuanLyThuoc.fxml";
        } else if (clickedButton == btnKhachHang) {
            fxmlPath = "/fxml/modules/KhachHang.fxml";
        } else if (clickedButton == btnBanHang) {
            fxmlPath = "/fxml/modules/BanHang.fxml";
        } else if (clickedButton == btnNhapHang) {
            fxmlPath = "/fxml/modules/NhapHang.fxml";
        } else if (clickedButton == btnLichSuBanHang) {
            fxmlPath = "/fxml/modules/LichSuBanThuoc.fxml";
        } else if (clickedButton == btnLichSuNhapHang) {
            fxmlPath = "/fxml/modules/LichSuNhapThuoc.fxml";
        }

        if (!fxmlPath.isEmpty()) {
            // Ra lệnh cho màn hình chính load module tương ứng vào Center
            manHinhChinhController.hienThiView(fxmlPath);
        }
    }

    private void clearButtonStyles() {
        Button[] buttons = { btnTrangChu, btnKhachHang, btnQuanLyThuoc, btnBanHang, btnNhapHang, btnLichSuBanHang, btnLichSuNhapHang };
        for (Button btn : buttons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        }
    }
}