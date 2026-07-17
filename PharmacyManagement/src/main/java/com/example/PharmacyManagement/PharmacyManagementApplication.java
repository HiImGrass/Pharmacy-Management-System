package com.example.PharmacyManagement;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.PharmacyManagement.gui.component.FxmlLoaderService;
import com.example.PharmacyManagement.SpringContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@SpringBootApplication
public class PharmacyManagementApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // 1. Khởi chạy ngầm Spring Boot để quét DB, JPA, Repository, Service...
        springContext = new SpringApplicationBuilder(PharmacyManagementApplication.class)
                .web(WebApplicationType.NONE) // Chạy Spring Boot ở chế độ không web để tránh xung đột với JavaFX
                .run();

        // 2. Nạp cục Context vừa chạy vào class SpringContext trung gian của chúng ta
        SpringContext.setContext(springContext);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FxmlLoaderService fxmlLoaderService = new FxmlLoaderService();
        Parent root = fxmlLoaderService.load("/fxml/ManHinhChinh.fxml");

        primaryStage.setTitle("Hệ thống quản lý nhà thuốc");
        ganIconChoUngDung(primaryStage);
        primaryStage.setScene(new Scene(root, 1150, 720));
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Đóng Spring context khi tắt giao diện app
        springContext.close();
    }

    public static void main(String[] args) {
        // Kich hoạt vòng đời của JavaFX
        launch(args);
    }

    private void ganIconChoUngDung(Stage stage) {
        InputStream iconStream = getClass()
                .getResourceAsStream(
                        "/images/pharmacy_management-app.png");

        if (iconStream == null) {
            System.err.println(
                    "Không tìm thấy icon tại: "
                            + "/images/pharmacy_management-app.png");
            return;
        }

        stage.getIcons().add(
                new Image(iconStream));
    }
}
