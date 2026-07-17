package com.example.PharmacyManagement;

import org.springframework.context.ConfigurableApplicationContext;

public class SpringContext {
    
    // Nơi lưu trữ "Bộ não" quản lý tất cả các Bean của Spring Boot
    private static ConfigurableApplicationContext context;

    /**
     * Hàm này sẽ được gọi 1 lần duy nhất lúc khởi động ở class Main
     */
    public static void setContext(ConfigurableApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * Hàm tiện ích dùng để lôi bất kỳ Service hoặc Repository nào ra sử dụng
     * @param beanClass Truyền vào class muốn lấy (Ví dụ: HoaDonService.class)
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException("Spring Context chưa được khởi tạo! Hãy kiểm tra lại hàm main.");
        }
        return context.getBean(beanClass);
    }
}