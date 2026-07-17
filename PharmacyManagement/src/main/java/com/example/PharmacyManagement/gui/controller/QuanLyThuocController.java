package com.example.PharmacyManagement.gui.controller;

//Folders
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.service.ThuocService;
//JavaFX
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

//Spring
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
//Java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
public class QuanLyThuocController {
    // Table and columns
    @FXML
    private TableView<Thuoc> tableThuoc;
    @FXML
    private TableColumn<Thuoc, Long> colId;
    @FXML
    private TableColumn<Thuoc, String> colTen;
    @FXML
    private TableColumn<Thuoc, String> colDonVi;
    @FXML
    private TableColumn<Thuoc, BigDecimal> colGiaNhap;
    @FXML
    private TableColumn<Thuoc, BigDecimal> colGiaBanSi;
    @FXML
    private TableColumn<Thuoc, Integer> colSoLuongTon;
    @FXML
    private TableColumn<Thuoc, LocalDate> colHanSuDung;
    @FXML
    private TableColumn<Thuoc, String> colMoTa;
    // TextField
    @FXML
    private TextField txtSearch;
    // Button
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnThem;
    @FXML
    private Button btnSua;
    @FXML
    private Button btnXoa;
    @FXML
    private Button btnXuatFile;
    @FXML
    private Button btnNhapFile;

    // Summary Labels
    @FXML
    private Label lblTongMatHang;
    @FXML
    private Label lblSapHetHang;
    @FXML
    private Label lblGanHetHan;
    @FXML
    private Label lblHetHan;

    @Autowired
    private ThuocService thuocService;

    @FXML
    public void initialize() {
        // Ánh xạ dữ liệu từ Model Thuoc vào các cột hiển thị trên TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        colGiaNhap.setCellValueFactory(new PropertyValueFactory<>("giaNhap"));
        colGiaBanSi.setCellValueFactory(new PropertyValueFactory<>("giaBanSi"));
        colSoLuongTon.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colHanSuDung.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));

        // Make 'Mô Tả' column expand to fill remaining table width
        tableThuoc.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colMoTa.setMaxWidth(Double.MAX_VALUE);

        // Tải danh sách thuốc lên bảng khi màn hình được mở
        refreshTable();

        // Thêm listener để tự động lọc khi người dùng nhập vào ô tìm kiếm
        cauHinhTimKiemTuDong();
        // Cập nhật thông tin tổng quan
        List<Thuoc> danhSachThuoc = thuocService.getAllThuoc();
        capNhatThongTinTongQuan(danhSachThuoc);
    }

    private void refreshTable() {
        tableThuoc.setItems(FXCollections.observableArrayList(thuocService.getAllThuoc()));
        capNhatThongTinTongQuan(thuocService.getAllThuoc());
    }

    @FXML
    public void xuLyTimKiem() {
        String keyword = txtSearch.getText();
        tableThuoc.setItems(FXCollections.observableArrayList(thuocService.timKiemThuoc(keyword)));
    }

    @FXML
    public void xuLyLamMoi() {
        txtSearch.clear();
        refreshTable();
        capNhatThongTinTongQuan(thuocService.getAllThuoc());
    }

    /**
     * CHỨC NĂNG: THÊM THUỐC MỚI
     */
    @FXML
    public void xuLyThem() {
        // Gọi hàm hiển thị Dialog nhập liệu trống
        Optional<Thuoc> ketQua = hienThiDialogNhapLieu(null);

        // Nếu người dùng nhấn "Lưu" và nhập đầy đủ thông tin
        ketQua.ifPresent(thuocMoi -> {
            try {
                thuocService.themThuoc(thuocMoi);
                hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm thuốc mới!");
                refreshTable();
            } catch (Exception e) {
                hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể thêm thuốc: " + e.getMessage());
            }
        });
        capNhatThongTinTongQuan(thuocService.getAllThuoc());
    }

    /**
     * CHỨC NĂNG: SỬA THÔNG TIN THUỐC ĐÃ CHỌN
     */
    @FXML
    public void xuLySua() {
        // Lấy đối tượng thuốc đang được chọn trên TableView
        Thuoc thuocDuocChon = tableThuoc.getSelectionModel().getSelectedItem();

        if (thuocDuocChon == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thuốc trong bảng để sửa!");
            return;
        }

        // Gọi hàm hiển thị Dialog nhập liệu và truyền thông tin hiện tại vào
        Optional<Thuoc> ketQua = hienThiDialogNhapLieu(thuocDuocChon);

        ketQua.ifPresent(thuocCapNhat -> {
            try {
                // Giữ nguyên ID cũ để thực hiện cập nhật (Update) dưới Database
                thuocCapNhat.setId(thuocDuocChon.getId());

                // Giả định hàm lưu của Service tự động hiểu Update khi thực thể có ID sẵn
                thuocService.suaThuoc(thuocDuocChon.getId(), thuocCapNhat);

                hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin thành công!");
                refreshTable();
            } catch (Exception e) {
                hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể cập nhật: " + e.getMessage());
            }
        });
        System.out.println("Bắt sự kiện sửa");
        capNhatThongTinTongQuan(thuocService.getAllThuoc());
    }

    /**
     * CHỨC NĂNG: XÓA THUỐC ĐÃ CHỌN
     */
    @FXML
    public void xuLyXoa() {
        Thuoc thuocDuocChon = tableThuoc.getSelectionModel().getSelectedItem();

        if (thuocDuocChon == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một thuốc trong bảng để xóa!");
            return;
        }

        // Tạo hộp thoại xác nhận chắc chắn muốn xóa trước khi gọi DB
        Alert alertXacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        alertXacNhan.setTitle("Xác nhận xóa");
        alertXacNhan.setHeaderText("Bạn có chắc chắn muốn xóa thuốc này không?");
        alertXacNhan.setContentText(
                "Tên thuốc: " + thuocDuocChon.getTenThuoc() + "\nSố lượng tồn kho: " + thuocDuocChon.getSoLuongTon());

        Optional<ButtonType> ketQuaBamNut = alertXacNhan.showAndWait();
        if (ketQuaBamNut.isPresent() && ketQuaBamNut.get() == ButtonType.OK) {
            try {
                // Bạn cần kiểm tra xem ThuocService của mình tên hàm xóa là gì (Ví dụ:
                // xoaThuoc hoặc deleteById)
                thuocService.xoaThuoc(thuocDuocChon.getId());

                hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa thuốc thành công!");
                refreshTable();
            } catch (Exception e) {
                hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống",
                        "Không thể xóa thuốc! (Lưu ý: Có thể thuốc đã có lịch sử hóa đơn bán hàng, không được phép xóa)");
            }
        }
        capNhatThongTinTongQuan(thuocService.getAllThuoc());
    }
    /**
     * HÀM TIỆN ÍCH: Tạo hộp thoại nhập thông tin Thuốc (Dùng chung cho cả Thêm và
     * Sửa)
     */
    private Optional<Thuoc> hienThiDialogNhapLieu(Thuoc thuocHienTai) {
        System.out.println("Bắt sự kiện hiển thị dialog");
        Dialog<Thuoc> dialog = new Dialog<>();
        dialog.setTitle(thuocHienTai == null ? "Thêm Thuốc Mới" : "Sửa Thông Tin Thuốc");
        dialog.setHeaderText("Vui lòng điền thông tin chi tiết:");

        ButtonType nutLuuType = new ButtonType("Lưu thông tin", ButtonBar.ButtonData.OK_DONE);
        System.out.println("Bắt sự kiện hiển thị dialog 2");
        dialog.getDialogPane().getButtonTypes().addAll(nutLuuType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtTen = new TextField();
        txtTen.setPromptText("Nhập tên thuốc...");
        TextField txtSoLuong = new TextField();
        txtSoLuong.setPromptText("Nhập số lượng...");
        TextField txtDonVi = new TextField();
        txtDonVi.setPromptText("Nhập đơn vị...");
        TextField txtGiaNhap = new TextField();
        txtGiaNhap.setPromptText("Nhập giá nhập...");
        TextField txtGiaBanSi = new TextField();
        txtGiaBanSi.setPromptText("Nhập giá bán sỉ...");

        // SỬ DỤNG DATEPICKER CHO NGÀY THÁNG
        DatePicker dpHanSuDung = new DatePicker();
        dpHanSuDung.setPromptText("MM/DD/YYYY");

        TextField txtMoTa = new TextField();
        txtMoTa.setPromptText("Nhập mô tả...");

        // Nếu là tác vụ SỬA, đổ thông tin cũ lên form
        if (thuocHienTai != null) {
            txtTen.setText(thuocHienTai.getTenThuoc());
            txtSoLuong.setText(String.valueOf(thuocHienTai.getSoLuongTon()) == null ? "0"
                    : String.valueOf(thuocHienTai.getSoLuongTon()));
            txtDonVi.setText(thuocHienTai.getDonVi() == null ? "" : thuocHienTai.getDonVi());
            txtGiaNhap.setText(String.valueOf(thuocHienTai.getGiaNhap()) == null ? "0"
                    : String.valueOf(thuocHienTai.getGiaNhap()));
            txtGiaBanSi.setText(String.valueOf(thuocHienTai.getGiaBanSi()) == null ? "0"
                    : String.valueOf(thuocHienTai.getGiaBanSi()));
            dpHanSuDung.setValue(thuocHienTai.getHanSuDung() == null ? null : thuocHienTai.getHanSuDung());
            txtMoTa.setText(thuocHienTai.getMoTa() == null ? "" : thuocHienTai.getMoTa());
        }

        grid.add(new Label("Tên thuốc:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Số lượng tồn kho:"), 0, 1);
        grid.add(txtSoLuong, 1, 1);
        grid.add(new Label("Đơn vị:"), 0, 2);
        grid.add(txtDonVi, 1, 2);
        grid.add(new Label("Giá nhập:"), 0, 3);
        grid.add(txtGiaNhap, 1, 3);
        grid.add(new Label("Giá bán sỉ:"), 0, 4);
        grid.add(txtGiaBanSi, 1, 4);
        grid.add(new Label("Hạn sử dụng:"), 0, 5);
        grid.add(dpHanSuDung, 1, 5);
        grid.add(new Label("Mô tả:"), 0, 6);
        grid.add(txtMoTa, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // LOGIC XỬ LÝ KHI BẤM LƯU ĐÃ ĐƯỢC BỌC TRY-CATCH AN TOÀN
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == nutLuuType) {
                // Kiểm tra rỗng trước tiên
                if (txtTen.getText().trim().isEmpty() || txtSoLuong.getText().trim().isEmpty() ||
                        txtDonVi.getText().trim().isEmpty() || txtGiaNhap.getText().trim().isEmpty() ||
                        txtGiaBanSi.getText().trim().isEmpty()) {

                    hienThiThongBao(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                            "Vui lòng nhập đầy đủ các trường bắt buộc (Tên, Số lượng, Đơn vị, Giá, Hạn sử dụng)!");
                    return null;
                }

                try {
                    Thuoc thuoc = new Thuoc();
                    thuoc.setTenThuoc(txtTen.getText().trim());
                    // Ép kiểu an toàn bên trong try
                    thuoc.setSoLuongTon(Integer.parseInt(txtSoLuong.getText().trim()));
                    thuoc.setDonVi(txtDonVi.getText().trim());
                    thuoc.setGiaNhap(new BigDecimal(txtGiaNhap.getText().trim()));
                    thuoc.setGiaBanSi(new BigDecimal(txtGiaBanSi.getText().trim()));
                    thuoc.setHanSuDung(dpHanSuDung.getValue());
                    thuoc.setMoTa(txtMoTa.getText().trim());

                    return thuoc;

                } catch (NumberFormatException ex) {
                    // Nếu nhập chữ vào ô số lượng hoặc ô giá, sẽ thông báo thay vì crash app
                    hienThiThongBao(Alert.AlertType.ERROR, "Lỗi định dạng",
                            "Vui lòng nhập ĐÚNG ĐỊNH DẠNG SỐ cho phần Số lượng và Giá cả!");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * HÀM TIỆN ÍCH: Hiển thị nhanh một popup thông báo lên màn hình
     */
    private void hienThiThongBao(Alert.AlertType loaiThongBao, String tieuDe, String noiDung) {
        Alert alert = new Alert(loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    /**
     * CHỨC NĂNG: XUẤT FILE EXCEL
     */
    @FXML
    public void xuLyXuatFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nơi lưu file Excel thuốc");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("DanhSachThuoc_" + LocalDate.now() + ".xlsx");

        // Lấy Stage hiện tại từ TableView để làm cha cho Dialog
        Stage stage = (Stage) tableThuoc.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null)
            return; // Người dùng hủy chọn

        // Chạy ngầm (Background Thread) để không làm đơ giao diện nhà thuốc
        new Thread(() -> {
            try (Workbook workbook = new XSSFWorkbook();
                    FileOutputStream fileOut = new FileOutputStream(file)) {

                Sheet sheet = workbook.createSheet("Danh Sách Thuốc");

                // 1. Tạo Header phong cách chuyên nghiệp
                Row headerRow = sheet.createRow(0);
                String[] columns = { "Mã Thuốc (ID)", "Tên Thuốc", "Đơn Vị Tính", "Giá Nhập", "Giá Bán Sỉ",
                        "Số Lượng Tồn", "Hạn Sử Dụng", "Mô Tả" };

                // Style cho header (In đậm)
                CellStyle headerCellStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerCellStyle.setFont(headerFont);

                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }

                // 2. Điền dữ liệu lấy từ DB
                List<Thuoc> dsThuoc = thuocService.getAllThuoc();
                int rowIdx = 1;

                // Style định dạng ngày cho Excel
                CellStyle dateCellStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

                for (Thuoc thuoc : dsThuoc) {
                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(thuoc.getId() != null ? thuoc.getId() : 0);
                    row.createCell(1).setCellValue(thuoc.getTenThuoc());
                    row.createCell(2).setCellValue(thuoc.getDonVi());
                    row.createCell(3).setCellValue(thuoc.getGiaNhap() != null ? thuoc.getGiaNhap().doubleValue() : 0.0);
                    row.createCell(4)
                            .setCellValue(thuoc.getGiaBanSi() != null ? thuoc.getGiaBanSi().doubleValue() : 0.0);
                    row.createCell(5).setCellValue(thuoc.getSoLuongTon());
                    // Xử lý ô Ngày Tháng
                    Cell cellDate = row.createCell(6);
                    if (thuoc.getHanSuDung() != null) {
                        cellDate.setCellValue(thuoc.getHanSuDung());
                        cellDate.setCellStyle(dateCellStyle);
                    }

                    row.createCell(7).setCellValue(thuoc.getMoTa());
                }

                // Tự động căn rộng các cột theo nội dung
                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(fileOut);

                // Cập nhật lại UI thành công thông qua FX Thread
                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công",
                            "Đã xuất dữ liệu ra file Excel thành công!");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.ERROR, "Lỗi xuất file", "Có lỗi xảy ra: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * CHỨC NĂNG: NHẬP FILE EXCEL (Chấp nhận mọi định dạng chữ/số/date tự động)
     */
    @FXML
    public void xuLyNhapFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file Excel thuốc để nhập dữ liệu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));

        Stage stage = (Stage) tableThuoc.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file == null)
            return; // Người dùng hủy chọn

        new Thread(() -> {
            int soDongThanhCong = 0;
            int soDongLoi = 0;

            try (FileInputStream fileIn = new FileInputStream(file);
                    Workbook workbook = WorkbookFactory.create(fileIn)) {

                Sheet sheet = workbook.getSheetAt(0); // Lấy sheet đầu tiên

                // Duyệt từ dòng 1 (bỏ qua dòng 0 là tiêu đề)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    try {
                        Thuoc thuoc = new Thuoc();

                        // CỘT 0: ID -> Bỏ qua không set (để DB tự động tăng INCREMENT)

                        // CỘT 1: Tên thuốc
                        thuoc.setTenThuoc(docCellKieuChuoi(row.getCell(0)));
                        if (thuoc.getTenThuoc().isEmpty())
                            continue; // Bỏ qua dòng trống tên

                        // CỘT 2: Đơn vị
                        thuoc.setDonVi(docCellKieuChuoi(row.getCell(1)));

                        // CỘT 3: Giá nhập (Xử lý ép sang BigDecimal an toàn)
                        thuoc.setGiaNhap(docCellKieuBigDecimal(row.getCell(2)));

                        // CỘT 4: Giá bán sỉ
                        thuoc.setGiaBanSi(docCellKieuBigDecimal(row.getCell(3)));

                        // CỘT 5: Số lượng tồn (Xử lý sang Integer an toàn)
                        thuoc.setSoLuongTon(docCellKieuInteger(row.getCell(4)));

                        // CỘT 6: Hạn sử dụng (*ĐỌC MỌI ĐỊNH DẠNG TEXT/DATE TỰ ĐỘNG*)
                        thuoc.setHanSuDung(docCellKieuNgayThang(row.getCell(5)));

                        // CỘT 7: Mô tả
                        thuoc.setMoTa(docCellKieuChuoi(row.getCell(6)));

                        // Lưu xuống Database bằng hàm đã có sẵn của bạn
                        thuocService.themThuoc(thuoc);
                        soDongThanhCong++;

                    } catch (Exception ex) {
                        soDongLoi++; // Gặp dòng lỗi định dạng cụ thể, bỏ qua dòng đó và chạy tiếp
                        System.err.println("Lỗi đọc dữ liệu tại dòng " + (i + 1) + ": " + ex.getMessage());
                    }
                }

                final int thanhCong = soDongThanhCong;
                final int loi = soDongLoi;

                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.INFORMATION, "Hoàn tất nhập dữ liệu",
                            "Đã thêm thành công: " + thanhCong + " loại thuốc.\nSố dòng lỗi định dạng bị bỏ qua: "
                                    + loi);
                    refreshTable(); // Làm mới lại TableView hiển thị nhà thuốc
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống",
                            "Không thể đọc cấu trúc file Excel: " + e.getMessage());
                });
            }
        }).start();
    }

    // =========================================================================
    // CÁC HÀM TRỢ GIÚP ĐỌC AN TOÀN - MIỄN NHIỄM VỚI SAI ĐỊNH DẠNG EXCEL
    // =========================================================================

    private String docCellKieuChuoi(Cell cell) {
        if (cell == null)
            return "";
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            if (numericValue == (long) numericValue) {
                return String.format("%d", (long) numericValue);
            } else {
                return String.valueOf(numericValue);
            }
        }
        return "";
    }

    private Integer docCellKieuInteger(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return 0;
        if (cell.getCellType() == CellType.NUMERIC)
            return (int) cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            return val.isEmpty() ? 0 : Integer.parseInt(val);
        }
        return 0;
    }

    private BigDecimal docCellKieuBigDecimal(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC)
            return BigDecimal.valueOf(cell.getNumericCellValue());
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            return val.isEmpty() ? BigDecimal.ZERO : new BigDecimal(val);
        }
        return BigDecimal.ZERO;
    }

    private LocalDate docCellKieuNgayThang(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return null;

        // Trường hợp 1: Excel tự nhận diện và format ô đó thành dạng DATE (Kịch bản của
        // bạn)
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        // Trường hợp 2: Người dùng copy-paste hoặc ép ô đó thành TEXT chuỗi thường
        if (cell.getCellType() == CellType.STRING) {
            String dateStr = cell.getStringCellValue().trim();
            if (dateStr.isEmpty())
                return null;

            // Hỗ trợ linh hoạt các kiểu gõ phổ biến ở Việt Nam
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } catch (Exception e2) {
                    throw new RuntimeException("Ngày tháng gõ sai định dạng (Hợp lệ: yyyy-MM-dd hoặc dd/MM/yyyy)");
                }
            }
        }

        // Trường hợp lỗi hy hữu: Nhập số thô không format date
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        return null;
    }

    private void capNhatThongTinTongQuan(List<Thuoc> danhSachThuoc) {
        int tongSoLuong = danhSachThuoc.size(); // Tính tống số lượng thuốc.
        lblTongMatHang.setText(String.valueOf(tongSoLuong));
        LocalDate homNay = LocalDate.now();

        int tongSoLuongSapHetHang = (int) danhSachThuoc.stream()
                .filter(t -> t.getSoLuongTon() <= 3)
                .count();

        lblSapHetHang.setText(String.valueOf(tongSoLuongSapHetHang));

        long tongSoLuongGanHetHan = danhSachThuoc.stream()
                .filter(t -> t != null)
                .filter(t -> t.getHanSuDung() != null)
                .filter(t -> {
                    long soNgayConLai = ChronoUnit.DAYS.between(homNay, t.getHanSuDung());
                    return soNgayConLai >= 0 && soNgayConLai <= 180;
                })
                .count();
        lblGanHetHan.setText(String.valueOf(tongSoLuongGanHetHan));

        long tongSoLuongHetHan = danhSachThuoc.stream()
                .filter(t -> t != null)
                .filter(t -> t.getHanSuDung() != null)
                .filter(t -> t.getHanSuDung().isBefore(homNay))
                .count();
        lblHetHan.setText(String.valueOf(tongSoLuongHetHan));
    }

    private void cauHinhTimKiemTuDong() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                refreshTable();
                return;
            }

            List<Thuoc> ketQua = thuocService.timKiemThuoc(newValue.trim());
            tableThuoc.setItems(FXCollections.observableArrayList(ketQua));
        });
    }
}