package com.example.PharmacyManagement.gui.component;

//Java imports
import java.util.HashMap;
import java.util.Map;

//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//JavaFX imports
import javafx.scene.Parent;

//Component imports

//Utils imports
@Component
public class ModuleViewCacheService {

    private final FxmlLoaderService fxmlLoaderService;

    /*
     * Key: đường dẫn FXML.
     * Value: giao diện và controller đã được load.
     */
    private final Map<String, Parent> viewCache = new HashMap<>();

    @Autowired
    public ModuleViewCacheService(FxmlLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService;
    }

    /**
     * Trả về module đã cache.
     * Chỉ load FXML khi module chưa tồn tại trong cache.
     */
    public Parent getOrLoad(String fxmlPath) throws Exception {
        if (fxmlPath == null || fxmlPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Đường dẫn FXML không được để trống.");
        }

        Parent cachedView = viewCache.get(fxmlPath);

        if (cachedView != null) {
            return cachedView;
        }

        Parent newView = fxmlLoaderService.load(fxmlPath);

        viewCache.put(fxmlPath, newView);

        return newView;
    }

    /**
     * Xóa cache của một module.
     * Lần mở tiếp theo module sẽ được tạo mới hoàn toàn.
     */
    public synchronized void evict(String fxmlPath) {
        viewCache.remove(fxmlPath);
    }

    /**
     * Xóa toàn bộ cache, thường dùng khi đăng xuất.
     */
    public synchronized void clear() {
        viewCache.clear();
    }

    public synchronized boolean contains(String fxmlPath) {
        return viewCache.containsKey(fxmlPath);
    }
}