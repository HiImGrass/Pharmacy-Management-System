package com.example.PharmacyManagement.gui.util;

import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public class DatePickerFormatter {

    private static final String VN_DATE_PATTERN = "dd/MM/yyyy";
    private static final Locale localeVN = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(VN_DATE_PATTERN, localeVN);

    public static void formatDatePickerToVn(DatePicker datePicker) {
        if(datePicker == null){
            // Sửa nó thành mở dialog thông báo sau
            return;
        }

        datePicker.setPromptText(VN_DATE_PATTERN.toLowerCase());

        datePicker.setChronology(java.time.chrono.IsoChronology.INSTANCE);
        // Set the converter for the DatePicker to handle the Vietnamese date format
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });

        //Auto dropdown calendar when clicking on the text field
        datePicker.getEditor().setOnMouseClicked(event -> {
            if (!datePicker.isShowing()) {
                datePicker.show();
            }
        });
    }

    public static <S> void formatTableColumnLocalDateToVN(TableColumn<S, LocalDate> column) {
        if (column == null) return;

        column.setCellFactory(col -> new TableCell<S, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                
                // Nếu cell trống hoặc dữ liệu null thì không hiển thị gì
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Hiển thị chuỗi theo định dạng dd/MM/yyyy
                    setText(formatter.format(item));
                }
            }
        });
    }

    public static <S> void formatTableColumnLocalDateTimeToVN(TableColumn<S, LocalDateTime> column) {
        if (column == null) return;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", localeVN);
        column.setCellFactory(col -> new TableCell<S, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateTimeFormatter.format(item));
                }
            }
        });
    }
    
}
