package com.example.PharmacyManagement.gui.controller;

//Java imports
import java.util.Set;

//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

//JavaFX imports
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

//Component imports
import com.example.PharmacyManagement.gui.component.FxmlLoaderService;
import com.example.PharmacyManagement.gui.component.ModuleViewCacheService;
//Utils imports

@Controller
public class ManHinhChinhController {

    @FXML
    private StackPane contentArea;

    @Autowired
    private FxmlLoaderService fxmlLoaderService;

    @Autowired
    private ModuleViewCacheService moduleViewCacheService;

    private static final String BAN_HANG_FXML_PATH = "/fxml/modules/BanHang.fxml";
    private static final String NHAP_HANG_FXML_PATH = "/fxml/modules/NhapHang.fxml";

    private static final Set<String> MODULE_CAN_CACHE = Set.of(
            BAN_HANG_FXML_PATH,
            NHAP_HANG_FXML_PATH);

    @FXML
    public void initialize() {
        // Tự động hiển thị trang chủ mặc định từ thư mục modules khi ứng dụng vừa mở
        // lên
        hienThiView("/fxml/modules/TrangChu.fxml");
    }

    /**
     * Hàm dùng chung để tải và hiển thị nội dung Module bất kỳ vào vùng Center
     */
    public void hienThiView(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isBlank()) {
            return;
        }

        try {
            Parent view;

            if (MODULE_CAN_CACHE.contains(fxmlPath)) {
                view = moduleViewCacheService.getOrLoad(fxmlPath);
            } else {
                view = fxmlLoaderService.load(fxmlPath);
            }

            if (contentArea.getChildren().size() == 1 && contentArea.getChildren().get(0) == view) {
                // Nếu view đã được hiển thị, không cần làm gì cả
                return;
            }

            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCacheBanHang() {
        moduleViewCacheService.evict(BAN_HANG_FXML_PATH);
    }

    public void clearCacheNhapHang() {
        moduleViewCacheService.evict(NHAP_HANG_FXML_PATH);
    }

    public void clearAllCache() {
        moduleViewCacheService.clear();
    }
}