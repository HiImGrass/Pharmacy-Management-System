package com.example.PharmacyManagement.service;

import com.example.PharmacyManagement.model.ChiTietHoaDon;

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

import java.math.BigDecimal;
import java.text.DecimalFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.springframework.stereotype.Service;

@Service
public class HoaDonInService {

    // Khổ in thực tế của máy bill 80mm thường khoảng 72mm = 576 dots ở 203dpi
    private static final int WIDTH = 576;

    private static final int LEFT = 8;
    private static final int RIGHT = WIDTH - 8;

    // Tọa độ các cột
    private static final int COL_STT_X = 14;
    private static final int COL_TEN_X = 53;
    private static final int COL_DVT_X = 270;
    private static final int COL_SL_X = 315;
    private static final int COL_DON_GIA_RIGHT_X = 450;
    private static final int COL_THANH_TIEN_RIGHT_X = 563;

    // Đường kẻ dọc bảng
    private static final int[] TABLE_LINES_X = {
            LEFT,
            50, // STT | Tên thuốc
            265, // Tên thuốc | Đơn vị
            314, // Đơn vị | Số lượng
            350, // Số lượng | Đơn giá
            455, // Đơn giá | Thành tiền
            RIGHT
    };

    /**
     * Hàm này dùng khi bạn muốn:
     * 1. Tạo ảnh hóa đơn
     * 2. In trực tiếp ra máy in Xprinter
     *
     * Quan trọng: dùng hàm này thì không cần mở file PNG/JPG bằng Windows Photos
     * nữa.
     */
    public File inHoaDonTrucTiep(
            Long hoaDonId,
            List<ChiTietHoaDon> danhSachGoc,
            String tenKhachHang,
            BigDecimal tienKhachDua) throws IOException, PrinterException {

        File fileAnh = taoAnhHoaDon(
                hoaDonId,
                danhSachGoc,
                tenKhachHang,
                tienKhachDua);

        if (fileAnh == null || !fileAnh.exists()) {
            throw new IOException(
                    "Không tạo được ảnh hóa đơn để in.");
        }

        inAnhHoaDon(fileAnh);

        return fileAnh;
    }

    /**
     * Hàm tạo ảnh hóa đơn PNG.
     * Chiều cao ảnh được tính động theo số dòng thuốc.
     */
    public File taoAnhHoaDon(
            Long hoaDonId,
            List<ChiTietHoaDon> danhSachGoc,
            String tenKhachHang,
            BigDecimal tienKhachDua) throws IOException {

        if (danhSachGoc == null) {
            danhSachGoc = Collections.emptyList();
        }

        DecimalFormat df = new DecimalFormat("#,##0");

        Font fontShopName = new Font("Arial", Font.BOLD, 30);
        Font fontPhone = new Font("Arial", Font.PLAIN, 17);
        Font fontInvoiceTitle = new Font("Arial", Font.BOLD, 31);
        Font fontInfo = new Font("Arial", Font.PLAIN, 19);
        Font fontTableHeader = new Font("Arial", Font.BOLD, 18);
        Font fontTable = new Font("Arial", Font.PLAIN, 19);
        Font fontMoney = new Font("Arial", Font.BOLD, 22);
        Font fontFooter = new Font("Arial", Font.ITALIC, 18);

        int tenThuocMaxWidth = COL_DVT_X - COL_TEN_X - 10;

        // Tính chiều cao bảng thuốc trước khi tạo ảnh thật
        BufferedImage tempImage = new BufferedImage(WIDTH, 1, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D tempG2d = tempImage.createGraphics();
        tempG2d.setFont(fontTable);

        int tableRowsHeight = 0;

        for (ChiTietHoaDon ct : danhSachGoc) {
            String tenThuoc = layTenThuoc(ct);
            List<String> lines = tachDong(tenThuoc, tempG2d.getFontMetrics(), tenThuocMaxWidth);

            int rowHeight = Math.max(48, lines.size() * 24 + 18);
            tableRowsHeight += rowHeight;
        }

        tempG2d.dispose();

        // Chiều cao hóa đơn = đầu hóa đơn + bảng thuốc + phần tổng tiền/footer
        int height = 330 + tableRowsHeight + 270;

        BufferedImage bufferedImage = new BufferedImage(WIDTH, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, height);
        g2d.setColor(Color.BLACK);

        // Tắt anti-aliasing để chữ không bị xám/mờ trên máy in nhiệt
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Nét kẻ đậm hơn một chút cho máy in nhiệt
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
        veChuCanGiua("HÓA ĐƠN BÁN HÀNG", WIDTH, y, g2d);

        y += 38;
        g2d.setFont(fontInfo);

        String thoiGianLap = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        g2d.drawString("Ngày lập: " + thoiGianLap, LEFT + 10, y);

        y += 28;
        String khachHang = tenKhachHang != null && !tenKhachHang.trim().isEmpty()
                ? tenKhachHang.trim()
                : "Khách lẻ tại quầy";

        g2d.drawString("Khách hàng: " + khachHang, LEFT + 10, y);

        // Bảng thuốc
        y += 35;
        int tableTopY = y;

        g2d.drawLine(LEFT, y, RIGHT, y);

        y += 28;
        g2d.setFont(fontTableHeader);
        g2d.drawString("STT", COL_STT_X, y);
        g2d.drawString("Tên hàng", COL_TEN_X, y);
        g2d.drawString("Đ.Vị", COL_DVT_X + 4, y);
        g2d.drawString("SL", COL_SL_X + 4, y);
        g2d.drawString("Đ.Giá", 375, y);
        g2d.drawString("T.Tiền", 475, y);

        y += 12;
        g2d.drawLine(LEFT, y, RIGHT, y);
        y += 8;

        g2d.setFont(fontTable);

        int stt = 1;
        BigDecimal tongTienHoaDon = BigDecimal.ZERO;

        for (ChiTietHoaDon ct : danhSachGoc) {
            String tenThuoc = layTenThuoc(ct);
            String donVi = ct != null && ct.getDonVi() != null ? ct.getDonVi() : "-";
            int soLuong = ct != null ? ct.getSoLuong() : 0;

            BigDecimal donGia = ct != null && ct.getDonGia() != null
                    ? ct.getDonGia()
                    : BigDecimal.ZERO;

            BigDecimal thanhTien = ct != null && ct.getThanhTien() != null
                    ? ct.getThanhTien()
                    : BigDecimal.ZERO;

            tongTienHoaDon = tongTienHoaDon.add(thanhTien);

            List<String> tenThuocLines = tachDong(tenThuoc, g2d.getFontMetrics(), tenThuocMaxWidth);
            int rowHeight = Math.max(48, tenThuocLines.size() * 24 + 18);

            int rowTopY = y;
            int textY = rowTopY + 31;

            g2d.drawString(String.valueOf(stt++), COL_STT_X, textY);

            for (int i = 0; i < tenThuocLines.size(); i++) {
                g2d.drawString(tenThuocLines.get(i), COL_TEN_X, textY + i * 24);
            }

            g2d.drawString(donVi, COL_DVT_X + 4, textY);
            g2d.drawString(String.valueOf(soLuong), COL_SL_X + 8, textY);

            veChuCanPhai(df.format(donGia), COL_DON_GIA_RIGHT_X, textY, g2d);
            veChuCanPhai(df.format(thanhTien), COL_THANH_TIEN_RIGHT_X, textY, g2d);

            y += rowHeight;
            g2d.drawLine(LEFT, y, RIGHT, y);
        }

        int tableBottomY = y;

        for (int x : TABLE_LINES_X) {
            g2d.drawLine(x, tableTopY, x, tableBottomY);
        }

        // Tổng tiền
        BigDecimal phaiThanhToan = tongTienHoaDon;
        BigDecimal khachDuaThucTe = tienKhachDua != null ? tienKhachDua : phaiThanhToan;
        BigDecimal tienThoiLai = khachDuaThucTe.subtract(phaiThanhToan);

        int labelX = 95;
        int moneyRightX = RIGHT - 5;

        y += 40;
        g2d.setFont(fontMoney);

        g2d.drawString("TỔNG TIỀN THANH TOÁN:", labelX, y);
        veChuCanPhai(df.format(phaiThanhToan) + " VND", moneyRightX, y, g2d);

        y += 34;
        g2d.drawString("SỐ TIỀN NHẬN ĐƯỢC:", labelX, y);
        veChuCanPhai(df.format(khachDuaThucTe) + " VND", moneyRightX, y, g2d);

        y += 34;
        g2d.drawString("TIỀN THỐI LẠI:", labelX, y);
        veChuCanPhai(df.format(tienThoiLai) + " VND", moneyRightX, y, g2d);

        // Footer
        y += 48;
        g2d.setFont(fontFooter);
        veChuCanGiua("Cảm ơn Quý khách đã tin tưởng!!", WIDTH, y, g2d);

        y += 26;
        String ghiChu = "Hóa đơn có giá trị đổi trả trong vòng 24h kèm theo phôi in này.";
        List<String> ghiChuLines = tachDong(ghiChu, g2d.getFontMetrics(), WIDTH - 40);

        for (String line : ghiChuLines) {
            veChuCanGiua(line, WIDTH, y, g2d);
            y += 24;
        }

        g2d.dispose();

        String folderPath = System.getProperty("user.home")
                + File.separator
                + "PharmacyReceipts";

        File directory = new File(folderPath);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException(
                    "Không thể tạo thư mục lưu ảnh hóa đơn.");
        }

        String thoiGian = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(
                        "yyyyMMdd_HHmmss"));

        String maHoaDon = hoaDonId == null
                ? "HoaDon"
                : "HD" + hoaDonId;

        String tenFile = maHoaDon
                + "_"
                + thoiGian
                + ".png";

        File fileAnh = new File(directory, tenFile);

        boolean isSaved = ImageIO.write(
                bufferedImage,
                "png",
                fileAnh);

        if (!isSaved) {
            throw new IOException(
                    "Không thể lưu ảnh hóa đơn.");
        }

        return fileAnh;
    }

    /**
     * In ảnh hóa đơn trực tiếp ra máy in.
     * Hàm này tự tính chiều dài giấy theo chiều cao ảnh.
     */
    public void inAnhHoaDon(File fileAnh) throws IOException, PrinterException {
        BufferedImage image = ImageIO.read(fileAnh);

        if (image == null) {
            throw new IOException("Không đọc được file ảnh hóa đơn.");
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

        // Không để margin để tránh bị cắt
        paper.setImageableArea(0, 0, paperWidthPoint, paperHeightPoint);

        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);

        printerJob.setPrintable((Graphics graphics, PageFormat pf, int pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
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

    private String layTenThuoc(ChiTietHoaDon ct) {
        if (ct != null
                && ct.getThuoc() != null
                && ct.getThuoc().getTenThuoc() != null
                && !ct.getThuoc().getTenThuoc().trim().isEmpty()) {
            return ct.getThuoc().getTenThuoc().trim();
        }

        return "Thuốc chưa rõ tên";
    }

    private void veChuCanGiua(String text, int widthTong, int y, Graphics2D g2d) {
        int stringWidth = g2d.getFontMetrics().stringWidth(text);
        int x = (widthTong - stringWidth) / 2;
        g2d.drawString(text, x, y);
    }

    private void veChuCanPhai(String text, int rightX, int y, Graphics2D g2d) {
        int stringWidth = g2d.getFontMetrics().stringWidth(text);
        int x = rightX - stringWidth;
        g2d.drawString(text, x, y);
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