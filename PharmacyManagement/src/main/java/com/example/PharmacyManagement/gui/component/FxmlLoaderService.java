package com.example.PharmacyManagement.gui.component;

import com.example.PharmacyManagement.SpringContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;

@Component
public class FxmlLoaderService {

    public static class LoadedView<T> {
        private final Parent view;
        private final T controller;

        public LoadedView(Parent view, T controller) {
            this.view = view;
            this.controller = controller;
        }

        public Parent getView() {
            return view;
        }

        public T getController() {
            return controller;
        }
    }

    /**
     * Tải file FXML động và tự động liên kết các Bean của Spring Boot vào
     * Controller tương ứng
     */
    public Parent load(String fxmlPath) {
        return loadWithController(fxmlPath, Object.class).getView();
    }

    public <T> LoadedView<T> loadWithController(String fxmlPath, Class<T> controllerType) {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IllegalArgumentException("Không tìm thấy file FXML: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setControllerFactory(controllerClass -> {
            try {
                return SpringContext.getBean(controllerClass);
            } catch (BeansException | IllegalStateException ex) {
                try {
                    Constructor<?> constructor = controllerClass.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                } catch (ReflectiveOperationException reflectionException) {
                    throw new RuntimeException("Không thể khởi tạo controller: " + controllerClass.getName(),
                            reflectionException);
                }
            }
        });

        try {
            Parent view = loader.load();
            return new LoadedView<>(view, controllerType.cast(loader.getController()));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi nghiêm trọng khi tải giao diện FXML: " + fxmlPath, e);
        }
    }
}
