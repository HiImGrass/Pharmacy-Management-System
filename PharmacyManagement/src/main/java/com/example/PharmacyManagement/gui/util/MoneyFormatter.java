package com.example.PharmacyManagement.gui.util;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.UnaryOperator;

public final class MoneyFormatter {

    private static final Locale LOCALE_VN =
            Locale.forLanguageTag("vi-VN");

    private MoneyFormatter() {
        // Utility class không cho phép khởi tạo
    }

    /**
     * Tạo formatter mới vì DecimalFormat không thread-safe.
     */
    private static DecimalFormat createFormatter() {
        DecimalFormatSymbols symbols =
                DecimalFormatSymbols.getInstance(LOCALE_VN);

        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat formatter =
                new DecimalFormat("#,##0 'đ'", symbols);

        // Tiền Việt Nam thông thường không hiển thị phần thập phân
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setParseBigDecimal(true);

        return formatter;
    }

    /**
     * Format Number thành tiền Việt Nam.
     *
     * Ví dụ:
     * 745000 -> 745.000 đ
     */
    public static String format(Number amount) {
        if (amount == null) {
            return "";
        }

        return createFormatter().format(amount);
    }

    /**
     * Null sẽ được hiển thị thành 0 đ.
     */
    public static String formatOrZero(Number amount) {
        if (amount == null) {
            return createFormatter().format(BigDecimal.ZERO);
        }

        return createFormatter().format(amount);
    }

    /**
     * Chuyển chuỗi tiền tệ thành BigDecimal.
     *
     * Ví dụ:
     * "745.000 đ" -> 745000
     */
    public static BigDecimal parse(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        String normalized = value
                .replace("₫", "")
                .replace("đ", "")
                .replace(".", "")
                .replace(",", "")
                .replaceAll("\\s+", "")
                .replaceAll("[^0-9-]", "");

        if (normalized.isBlank() || normalized.equals("-")) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(normalized);
    }

    /**
     * Format một TableColumn có dữ liệu dạng Number.
     *
     * Hỗ trợ:
     * BigDecimal, Long, Integer, Double...
     */
    public static <S, T extends Number> void formatTableColumnToVN(
            TableColumn<S, T> column
    ) {
        if (column == null) {
            return;
        }

        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format(item));
                }
            }
        });
    }

    /**
     * Áp dụng định dạng tiền cho TextField.
     *
     * Khi focus:
     * 745.000 đ -> 745000
     *
     * Khi rời focus:
     * 745000 -> 745.000 đ
     */
    public static void formatTextFieldToVN(TextField textField) {
        if (textField == null) {
            return;
        }

        textField.setAlignment(Pos.CENTER_RIGHT);

        UnaryOperator<TextFormatter.Change> filter = change -> {
            // Chỉ kiểm tra khi người dùng đang nhập
            if (!textField.isFocused()) {
                return change;
            }

            String newValue = change.getControlNewText();

            // Chỉ cho phép số nguyên dương
            if (newValue.matches("\\d*")) {
                return change;
            }

            return null;
        };

        textField.setTextFormatter(
                new TextFormatter<>(filter)
        );

        textField.focusedProperty().addListener(
                (observable, oldValue, isFocused) -> {
                    String currentText = textField.getText();

                    if (currentText == null || currentText.isBlank()) {
                        return;
                    }

                    if (isFocused) {
                        // Khi click vào thì chuyển về số thuần để sửa
                        BigDecimal value = parse(currentText);
                        textField.setText(value.toPlainString());
                        textField.selectAll();
                    } else {
                        // Khi rời khỏi thì format thành tiền
                        BigDecimal value = parse(currentText);
                        textField.setText(format(value));
                    }
                }
        );
    }

    /**
     * Lấy giá trị tiền từ TextField.
     */
    public static BigDecimal getValue(TextField textField) {
        if (textField == null) {
            return BigDecimal.ZERO;
        }

        return parse(textField.getText());
    }
}