package com.example.PharmacyManagement.gui.util;

import java.time.LocalDate;

public final class ExpiryUtils {

    public static final int DEFAULT_WARNING_DAYS = 180;

    private ExpiryUtils() {
    }

    /**
     * Xác định trạng thái hạn sử dụng.
     *
     * @param expiryDate  ngày hết hạn
     * @param warningDays số ngày cảnh báo trước
     */
    public static ExpiryStatus getStatus(LocalDate expiryDate, int warningDays) {
        if (expiryDate == null) {
            return ExpiryStatus.NORMAL;
        }

        LocalDate today = LocalDate.now();

        /*
         * Quy ước:
         * - Trước hôm nay: đã hết hạn.
         * - Từ hôm nay đến warningDays ngày tới: sắp hết hạn.
         */
        if (expiryDate.isBefore(today)) {
            return ExpiryStatus.EXPIRED;
        }

        if (!expiryDate.isAfter(today.plusDays(warningDays))) {
            return ExpiryStatus.EXPIRING_SOON;
        }

        return ExpiryStatus.NORMAL;
    }

    public static boolean isExpired(LocalDate expiryDate) {
        return getStatus(expiryDate, DEFAULT_WARNING_DAYS) == ExpiryStatus.EXPIRED;
    }

    public static boolean isExpiringSoon(
            LocalDate expiryDate,
            int warningDays) {
        return getStatus(expiryDate, warningDays) == ExpiryStatus.EXPIRING_SOON;
    }
}