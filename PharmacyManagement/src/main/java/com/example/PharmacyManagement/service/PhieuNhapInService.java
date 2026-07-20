package com.example.PharmacyManagement.service;

import com.example.PharmacyManagement.model.ChiTietPhieuNhap;
import com.example.PharmacyManagement.model.ChiTietPhieuNhap;
import com.example.PharmacyManagement.model.Thuoc;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.File;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PhieuNhapInService {

    // Khổ in thực tế của máy bill 80mm thường khoảng 72mm = 576 dots ở 203dpi
    private static final int WIDTH = 576;

    private static final int LEFT = 8;
    private static final int RIGHT = WIDTH - 8;

    // Tọa độ các cột: STT | Tên hàng | Đơn vị | Số lượng
    private static final int COL_STT_X = 14;
    private static final int COL_TEN_X = 53;
    private static final int COL_DVT_X = 385;
    private static final int COL_SL_X = 470;

    // Đường kẻ dọc bảng: không còn cột Đơn giá và Thành tiền
    private static final int[] TABLE_LINES_X = {
            LEFT,
            50, // STT | Tên thuốc
            380, // Tên thuốc | Đơn vị
            465, // Đơn vị | Số lượng
            RIGHT
    };

    /**
     * Dùng khi module nhập hàng chưa có mã phiếu nhập.
     */
    public void inPhieuNhapTrucTiep(
            List<ChiTietPhieuNhap> danhSachGoc,
            String nhaCungCap,
            String ghiChu) throws IOException, PrinterException {
        inPhieuNhapTrucTiep(danhSachGoc, null, nhaCungCap, ghiChu);
    }

    /**
     * Dùng khi module nhập hàng đã lưu phiếu nhập và có mã/id phiếu nhập.
     */
    public void inPhieuNhapTrucTiep(
            List<ChiTietPhieuNhap> danhSachGoc,
            String maPhieuNhap,
            String nhaCungCap,
            String ghiChu) throws IOException, PrinterException {
        File fileAnh = taoAnhPhieuNhap(danhSachGoc, maPhieuNhap, nhaCungCap, ghiChu);

        if (fileAnh == null || !fileAnh.exists()) {
            throw new IOException("Không tạo được ảnh phiếu nhập để in.");
        }

        inAnhPhieuNhap(fileAnh);
    }

    /**
     * Dùng khi chỉ muốn tạo ảnh phiếu nhập, chưa in ngay.
     */
    public File taoAnhPhieuNhap(
            List<ChiTietPhieuNhap> danhSachGoc,
            String nhaCungCap,
            String ghiChu) throws IOException {
        return taoAnhPhieuNhap(danhSachGoc, null, nhaCungCap, ghiChu);
    }

    /**
     * Tạo ảnh phiếu nhập PNG.
     * Chiều cao ảnh được tính động theo số dòng thuốc.
     */
    public File taoAnhPhieuNhap(
            List<ChiTietPhieuNhap> danhSachIn,
            String maPhieuNhap,
            String nhaCungCap,
            String ghiChu) throws IOException {
        if (danhSachIn == null) {
            danhSachIn = Collections.emptyList();
        }

        Font fontShopName = new Font("Arial", Font.BOLD, 30);
        Font fontPhone = new Font("Arial", Font.PLAIN, 17);
        Font fontInvoiceTitle = new Font("Arial", Font.BOLD, 31);
        Font fontInfo = new Font("Arial", Font.PLAIN, 19);
        Font fontTableHeader = new Font("Arial", Font.BOLD, 18);
        Font fontTable = new Font("Arial", Font.PLAIN, 19);
        Font fontFooter = new Font("Arial", Font.ITALIC, 18);

        int tenThuocMaxWidth = COL_DVT_X - COL_TEN_X - 10;

        BufferedImage tempImage = new BufferedImage(WIDTH, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D tempG2d = tempImage.createGraphics();
        tempG2d.setFont(fontTable);

        int tableRowsHeight = 0;
        for (ChiTietPhieuNhap ct : danhSachIn) {
            String tenThuoc = layTenThuoc(ct);
            List<String> lines = tachDong(tenThuoc, tempG2d.getFontMetrics(), tenThuocMaxWidth);
            int rowHeight = Math.max(48, lines.size() * 24 + 18);
            tableRowsHeight += rowHeight;
        }
        tempG2d.dispose();

        int ghiChuHeight = tinhChieuCaoGhiChu(ghiChu, fontFooter);

        // Chiều cao phiếu nhập = đầu phiếu + bảng thuốc + tổng số lượng + chữ ký/footer
        // + ghi chú
        int height = 340 + tableRowsHeight + 230 + ghiChuHeight;

        BufferedImage bufferedImage = new BufferedImage(WIDTH, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, height);
        g2d.setColor(Color.BLACK);

        // Tắt anti-aliasing để chữ không bị xám/mờ trên máy in nhiệt
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setStroke(new BasicStroke(2));

        int y = 48;

        // Header
        g2d.setFont(fontShopName);
        veChuCanGiua("QUẦY THUỐC", WIDTH, y, g2d);

        y += 32;
        g2d.setFont(fontPhone);
        veChuCanGiua("SĐT1: 0937.762.068 - SĐT2: 0937.865.668", WIDTH, y, g2d);

        y += 22;
        g2d.drawLine(LEFT, y, RIGHT, y);

        y += 42;
        g2d.setFont(fontInvoiceTitle);
        veChuCanGiua("PHIẾU NHẬP KHO", WIDTH, y, g2d);

        y += 38;
        g2d.setFont(fontInfo);

        String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        g2d.drawString("Ngày lập: " + thoiGian, LEFT + 10, y);

        if (maPhieuNhap != null && !maPhieuNhap.trim().isEmpty()) {
            y += 28;
            g2d.drawString("Mã phiếu nhập: " + maPhieuNhap.trim(), LEFT + 10, y);
        }

        y += 28;
        String nhaCungCapHienThi = nhaCungCap != null && !nhaCungCap.trim().isEmpty()
                ? nhaCungCap.trim()
                : "Chưa rõ nhà cung cấp";
        g2d.drawString("Nhà cung cấp: " + nhaCungCapHienThi, LEFT + 10, y);

        // Bảng thuốc nhập
        y += 35;
        int tableTopY = y;

        g2d.drawLine(LEFT, y, RIGHT, y);

        y += 28;
        g2d.setFont(fontTableHeader);
        g2d.drawString("STT", COL_STT_X, y);
        g2d.drawString("Tên hàng", COL_TEN_X, y);
        g2d.drawString("Đ.Vị", COL_DVT_X + 4, y);
        g2d.drawString("SL", COL_SL_X + 20, y);

        y += 12;
        g2d.drawLine(LEFT, y, RIGHT, y);
        y += 8;

        g2d.setFont(fontTable);

        int stt = 1;
        int tongSoLuong = 0;

        for (ChiTietPhieuNhap ct : danhSachIn) {
            String tenThuoc = layTenThuoc(ct);
            String donVi = layDonVi(ct);
            int soLuong = laySoLuong(ct);

            tongSoLuong += soLuong;

            List<String> tenThuocLines = tachDong(tenThuoc, g2d.getFontMetrics(), tenThuocMaxWidth);
            int rowHeight = Math.max(48, tenThuocLines.size() * 24 + 18);

            int rowTopY = y;
            int textY = rowTopY + 31;

            g2d.drawString(String.valueOf(stt++), COL_STT_X, textY);

            for (int i = 0; i < tenThuocLines.size(); i++) {
                g2d.drawString(tenThuocLines.get(i), COL_TEN_X, textY + i * 24);
            }

            g2d.drawString(donVi, COL_DVT_X + 4, textY);
            g2d.drawString(String.valueOf(soLuong), COL_SL_X + 25, textY);

            y += rowHeight;
            g2d.drawLine(LEFT, y, RIGHT, y);
        }

        int tableBottomY = y;
        for (int x : TABLE_LINES_X) {
            g2d.drawLine(x, tableTopY, x, tableBottomY);
        }

        // Ghi chú
        if (ghiChu != null && !ghiChu.trim().isEmpty()) {
            y += 38;
            g2d.setFont(fontInfo);
            g2d.drawString("Ghi chú:", LEFT + 10, y);

            y += 26;
            List<String> ghiChuLines = tachDong(ghiChu.trim(), g2d.getFontMetrics(), WIDTH - 40);
            for (String line : ghiChuLines) {
                g2d.drawString(line, LEFT + 20, y);
                y += 24;
            }
        }

        // Footer
        y += 62;
        g2d.setFont(fontFooter);
        veChuCanGiua("Phiếu nhập được in từ hệ thống quản lý quầy thuốc.", WIDTH, y, g2d);

        g2d.dispose();

        String folderPath = System.getProperty("user.home")
                + File.separator
                + "PharmacyReceipts"
                + File.separator
                + "PhieuNhap";
        File directory = new File(folderPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }
        String thoiGianTaoFile = LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd_HHmmss"));

        String maPhieuAnToan = maPhieuNhap == null
                || maPhieuNhap.isBlank()
                        ? "PhieuNhap"
                        : maPhieuNhap.trim()
                                .replaceAll(
                                        "[^a-zA-Z0-9_-]",
                                        "_");

        String tenFile = maPhieuAnToan
                + "_"
                + thoiGianTaoFile
                + ".png";

        File fileAnh = new File(directory, tenFile);
        
        boolean isSaved = ImageIO.write(
                bufferedImage,
                "png",
                fileAnh);

        if (!isSaved) {
            throw new IOException(
                    "Không thể lưu ảnh phiếu nhập.");
        }

        return isSaved ? fileAnh : null;
    }

    /**
     * In ảnh phiếu nhập trực tiếp ra máy in.
     */
    public void inAnhPhieuNhap(File fileAnh) throws IOException, PrinterException {
        BufferedImage image = ImageIO.read(fileAnh);

        if (image == null) {
            throw new IOException("Không đọc được file ảnh phiếu nhập.");
        }

        PrintService selectedService = timMayInXprinter();

        if (selectedService == null) {
            throw new PrinterException("Không tìm thấy máy in Xprinter XP-N160II.");
        }

        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintService(selectedService);

        PageFormat pageFormat = new PageFormat();
        Paper paper = new Paper();

        // Giấy 80mm nhưng vùng in thực tế khoảng 72mm
        double paperWidthPoint = mmToPoint(72);

        // Tự tính chiều dài giấy theo chiều cao ảnh
        double scale = paperWidthPoint / image.getWidth();
        double paperHeightPoint = image.getHeight() * scale + mmToPoint(8);

        paper.setSize(paperWidthPoint, paperHeightPoint);
        paper.setImageableArea(0, 0, paperWidthPoint, paperHeightPoint);

        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);

        printerJob.setPrintable((Graphics graphics, PageFormat pf, int pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;

            g2d.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);

            double drawWidth = pf.getImageableWidth();
            double drawHeight = image.getHeight() * (drawWidth / image.getWidth());

            g2d.drawImage(
                    image,
                    0,
                    0,
                    (int) drawWidth,
                    (int) drawHeight,
                    null);

            return Printable.PAGE_EXISTS;
        }, pageFormat);

        printerJob.print();
    }

    private PrintService timMayInXprinter() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService service : services) {
            String printerName = service.getName().toLowerCase();

            if (printerName.contains("xprinter")
                    || printerName.contains("xp-n160")
                    || printerName.contains("posprinter")
                    || printerName.contains("pos80")) {
                return service;
            }
        }

        return null;
    }

    private double mmToPoint(double mm) {
        return mm * 72.0 / 25.4;
    }

    private String layTenThuoc(ChiTietPhieuNhap ct) {
        Thuoc thuoc = ct != null ? ct.getThuoc() : null;

        if (thuoc != null && thuoc.getTenThuoc() != null && !thuoc.getTenThuoc().trim().isEmpty()) {
            return thuoc.getTenThuoc().trim();
        }

        return "Thuốc chưa rõ tên";
    }

    private String layDonVi(ChiTietPhieuNhap ct) {
        if (ct != null && ct.getDonVi() != null && !ct.getDonVi().trim().isEmpty()) {
            return ct.getDonVi().trim();
        }

        Thuoc thuoc = ct != null ? ct.getThuoc() : null;
        if (thuoc != null && thuoc.getDonVi() != null && !thuoc.getDonVi().trim().isEmpty()) {
            return thuoc.getDonVi().trim();
        }

        return "-";
    }

    private int laySoLuong(ChiTietPhieuNhap ct) {
        return ct != null ? Math.max(ct.getSoLuong(), 0) : 0;
    }

    private void veChuCanGiua(String text, int widthTong, int y, Graphics2D g2d) {
        int stringWidth = g2d.getFontMetrics().stringWidth(text);
        int x = (widthTong - stringWidth) / 2;
        g2d.drawString(text, x, y);
    }

    private void veChuCanGiuaTheoKhoang(String text, int leftX, int rightX, int y, Graphics2D g2d) {
        int stringWidth = g2d.getFontMetrics().stringWidth(text);
        int width = rightX - leftX;
        int x = leftX + (width - stringWidth) / 2;
        g2d.drawString(text, x, y);
    }

    private int tinhChieuCaoGhiChu(String ghiChu, Font fontFooter) {
        if (ghiChu == null || ghiChu.trim().isEmpty()) {
            return 0;
        }

        BufferedImage tempImage = new BufferedImage(WIDTH, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = tempImage.createGraphics();
        g2d.setFont(fontFooter);
        int lineCount = tachDong(ghiChu.trim(), g2d.getFontMetrics(), WIDTH - 40).size();
        g2d.dispose();

        return lineCount * 24 + 42;
    }

    private List<String> tachDong(String text, FontMetrics fontMetrics, int maxWidth) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0
                    ? word
                    : currentLine + " " + word;

            if (fontMetrics.stringWidth(testLine) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }

                if (fontMetrics.stringWidth(word) <= maxWidth) {
                    currentLine.append(word);
                } else {
                    StringBuilder part = new StringBuilder();

                    for (char c : word.toCharArray()) {
                        String testPart = part.toString() + c;

                        if (fontMetrics.stringWidth(testPart) <= maxWidth) {
                            part.append(c);
                        } else {
                            if (part.length() > 0) {
                                lines.add(part.toString());
                            }

                            part = new StringBuilder(String.valueOf(c));
                        }
                    }

                    currentLine = part;
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
