package com.example.PharmacyManagement.gui.controller.sidebar;

// Import statements
import com.example.PharmacyManagement.gui.controller.ManHinhChinhController;

// Import statements
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

//Spring Framework annotations
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
    // // controller tham chiếu nhau

    private ManHinhChinhController manHinhChinhController;

    private static final String BAN_HANG_FXML_PATH = "/fxml/modules/BanHang.fxml";
    private static final String NHAP_HANG_FXML_PATH = "/fxml/modules/NhapHang.fxml";
    private static final String TRANG_CHU_FXML_PATH = "/fxml/modules/TrangChu.fxml";
    private static final String KHACH_HANG_FXML_PATH = "/fxml/modules/KhachHang.fxml";
    private static final String QUAN_LY_THUOC_FXML_PATH = "/fxml/modules/QuanLyThuoc.fxml";
    private static final String LICH_SU_BAN_HANG_FXML_PATH = "/fxml/modules/LichSuBanThuoc.fxml";
    private static final String LICH_SU_NHAP_HANG_FXML_PATH = "/fxml/modules/LichSuNhapThuoc.fxml";

    
    @FXML
    public void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String fxmlPath = "";

        // Tạo hiệu ứng đổi màu nút được chọn
        clearButtonStyles();
        clickedButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-cursor: hand;");

        fxmlPath = layDuongDanModule(clickedButton);

        if (fxmlPath == null || fxmlPath.isBlank()){
            return;
        }

        manHinhChinhController.hienThiView(fxmlPath);
    }

    private String layDuongDanModule(Button clickedButton) {
        if (clickedButton == btnTrangChu) {
            return TRANG_CHU_FXML_PATH;
        }

        if (clickedButton == btnQuanLyThuoc) {
            return QUAN_LY_THUOC_FXML_PATH;
        }

        if (clickedButton == btnKhachHang) {
            return KHACH_HANG_FXML_PATH;
        }

        if (clickedButton == btnBanHang) {
            return BAN_HANG_FXML_PATH;
        }

        if (clickedButton == btnNhapHang) {
            return NHAP_HANG_FXML_PATH;
        }

        if (clickedButton == btnLichSuBanHang) {
            return LICH_SU_BAN_HANG_FXML_PATH;
        }

        if (clickedButton == btnLichSuNhapHang) {
            return LICH_SU_NHAP_HANG_FXML_PATH;
        }

        return null;
    }

    private void clearButtonStyles() {
        Button[] buttons = { btnTrangChu, btnKhachHang, btnQuanLyThuoc, btnBanHang, btnNhapHang, btnLichSuBanHang,
                btnLichSuNhapHang };
        for (Button btn : buttons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
        }
    }
}