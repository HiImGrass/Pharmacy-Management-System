package com.example.PharmacyManagement.gui.util;

import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TableRowFormatter {

        private static final PseudoClass EXPIRED = PseudoClass.getPseudoClass("expired");

        private static final PseudoClass EXPIRING_SOON = PseudoClass.getPseudoClass("expiring-soon");

        private TableRowFormatter() {
        }

        /**
         * Tô màu dòng theo hạn sử dụng.
         */
        public static <T> void applyExpiryRowStyle(
                        TableView<T> tableView,
                        Function<T, LocalDate> expiryDateExtractor,
                        int warningDays) {
                applyExpiryRowStyle(
                                tableView,
                                expiryDateExtractor,
                                warningDays,
                                null);
        }

        /**
         * Tô màu dòng theo hạn sử dụng và hỗ trợ sự kiện double-click.
         *
         * @param tableView           bảng cần định dạng
         * @param expiryDateExtractor hàm lấy hạn sử dụng từ model
         * @param warningDays         số ngày cảnh báo
         * @param onDoubleClick       hành động khi double-click vào dòng
         */
        public static <T> void applyExpiryRowStyle(
                        TableView<T> tableView,
                        Function<T, LocalDate> expiryDateExtractor,
                        int warningDays,
                        Consumer<T> onDoubleClick) {
                if (tableView == null || expiryDateExtractor == null) {
                        return;
                }

                tableView.setRowFactory(table -> new TableRow<T>() {

                        /*
                         * Khối khởi tạo của TableRow.
                         */
                        {
                                setOnMouseClicked(event -> {
                                        if (event.getClickCount() == 2
                                                        && !isEmpty()
                                                        && getItem() != null
                                                        && onDoubleClick != null) {

                                                onDoubleClick.accept(getItem());
                                        }
                                });
                        }

                        @Override
                        protected void updateItem(T item, boolean empty) {
                                super.updateItem(item, empty);

                                ExpiryStatus status = ExpiryStatus.NORMAL;

                                if (!empty && item != null) {
                                        LocalDate expiryDate = expiryDateExtractor.apply(item);

                                        status = ExpiryUtils.getStatus(
                                                        expiryDate,
                                                        warningDays);
                                }

                                /*
                                 * Phải luôn cập nhật cả true và false vì JavaFX
                                 * tái sử dụng TableRow khi người dùng cuộn bảng.
                                 */
                                pseudoClassStateChanged(
                                                EXPIRED,
                                                !empty
                                                                && status == ExpiryStatus.EXPIRED);

                                pseudoClassStateChanged(
                                                EXPIRING_SOON,
                                                !empty
                                                                && status == ExpiryStatus.EXPIRING_SOON);
                        }
                });
        }
}