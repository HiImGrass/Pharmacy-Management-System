package com.example.PharmacyManagement.gui.util;

import javafx.scene.control.Alert;

public class AlertUtils {
    public static void hienThiThongBao(Alert.AlertType loaiThongBao, String tieuDe, String headerText, String noiDung) {
        Alert alert = new Alert(loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(headerText);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
