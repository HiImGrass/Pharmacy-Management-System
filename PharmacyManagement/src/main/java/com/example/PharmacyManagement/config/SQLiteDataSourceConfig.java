package com.example.PharmacyManagement.config;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@Profile("sqlite")
public class SQLiteDataSourceConfig {

        @Bean
        @Primary
        public DataSource dataSource(
                        @Value("${pharmacy.data-dir}") String dataDirectory) throws IOException {

                Path thuMucDuLieu = Path.of(dataDirectory)
                                .toAbsolutePath()
                                .normalize();

                Files.createDirectories(thuMucDuLieu);

                Path fileDatabase = thuMucDuLieu.resolve(
                                "pharmacy.db");

                SQLiteConfig config = new SQLiteConfig();

                // SQLite mặc định không bắt buộc kiểm tra khóa ngoại
                config.enforceForeignKeys(true);

                // Chờ tối đa 5 giây khi database đang bị khóa
                config.setBusyTimeout(5000);

                // Phù hợp hơn khi ứng dụng vừa đọc vừa ghi dữ liệu
                config.setJournalMode(
                                SQLiteConfig.JournalMode.WAL);

                SQLiteDataSource dataSource = new SQLiteDataSource(config);

                String duongDanJdbc = "jdbc:sqlite:"
                                + fileDatabase
                                                .toString()
                                                .replace('\\', '/');

                dataSource.setUrl(duongDanJdbc);

                System.out.println(
                                "SQLite database: "
                                                + fileDatabase);

                return dataSource;
        }
}