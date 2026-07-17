package com.example.PharmacyManagement.gui.controller;

import com.example.PharmacyManagement.gui.component.FxmlLoaderService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ManHinhChinhController {

    @FXML 
    private StackPane contentArea;

    @Autowired
    private FxmlLoaderService fxmlLoaderService;

    @FXML
    public void initialize() {
        // Tự động hiển thị trang chủ mặc định từ thư mục modules khi ứng dụng vừa mở lên
        hienThiView("/fxml/modules/TrangChu.fxml");
    }

    /**
     * Hàm dùng chung để tải và hiển thị nội dung Module bất kỳ vào vùng Center
     */
    public void hienThiView(String fxmlPath) {
        try {
            Parent view = fxmlLoaderService.load(fxmlPath);
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("Không thể chuyển đổi sang Module: " + fxmlPath);
            e.printStackTrace();
        }
    }
}