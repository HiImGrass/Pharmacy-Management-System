package com.example.PharmacyManagement.gui.controller;

//Java imports
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.io.File;

//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

//JavaFX imports
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

//Component imports
import com.example.PharmacyManagement.model.PhieuNhap;
import com.example.PharmacyManagement.service.PhieuNhapService;

//Utils imports
import com.example.PharmacyManagement.gui.util.DatePickerFormatter;

@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LichSuNhapThuocController {

        @FXML
        private TableView<PhieuNhap> tableLichSuNhapHang;

        @FXML
        private Label lblKetQua;

        @FXML
        private Label lblPhanTrang;

        @FXML
        private Label lblTongHoaDon;

        @FXML
        private Label lblHomNay;

        @FXML
        private TextField txtSearch;

        @FXML
        private DatePicker dpTuNgay;

        @FXML
        private DatePicker dpDenNgay;

        @FXML
        private TableColumn<PhieuNhap, Integer> colStt;

        @FXML
        private TableColumn<PhieuNhap, String> colMaPhieuNhap;

        @FXML
        private TableColumn<PhieuNhap, String> colNhaCungCap;

        @FXML
        private TableColumn<PhieuNhap, LocalDateTime> colNgayNhap;

        @FXML
        private TableColumn<PhieuNhap, Void> colChiTiet;

        @Autowired
        private PhieuNhapService phieuNhapService;

        private final ObservableList<PhieuNhap> danhSachPhieuNhapGoc = FXCollections.observableArrayList();

        @FXML
        public void initialize() {
                System.out.println("Khởi tạo module Lịch Sử Nhập Thuốc");

                // Cấu hình cách hiển thị các cột.
                cauHinhBangLichSuPhieuNhap();

                // Lấy dữ liệu từ database một lần khi mở module.
                taiDuLieuLenBang();

                // Hiển thị danh sách ban đầu.
                apDungBoLoc();

                // Cấu hình DatePicker hiển thị theo định dạng Việt Nam.
                DatePickerFormatter.formatDatePickerToVn(dpTuNgay);
                DatePickerFormatter.formatDatePickerToVn(dpDenNgay);
                // Khi gõ tìm kiếm chỉ lọc danh sách đang có, không gọi database mỗi ký tự.
                txtSearch.textProperty().addListener(
                                (observable, oldValue, newValue) -> apDungBoLoc());

                dpTuNgay.valueProperty().addListener(
                                (observable, oldValue, newValue) -> apDungBoLoc());

                dpDenNgay.valueProperty().addListener(
                                (observable, oldValue, newValue) -> apDungBoLoc());
        }

        @FXML
        private void xuLyLamMoi(ActionEvent event) {
                txtSearch.clear();
                dpTuNgay.setValue(null);
                dpDenNgay.setValue(null);

                // Lấy lại dữ liệu mới nhất từ database.
                taiDuLieuLenBang();
                apDungBoLoc();
        }

        // MỞ RỘNG TRONG TƯƠNG LAI NẾU CẦN THIẾT.
        // @FXML
        // private void xuLyTrangTruoc(ActionEvent event) {
        // System.out.println("Đang xử lý chuyển về trang trước...");
        // }

        // @FXML
        // private void xuLyTrangSau(ActionEvent event) {
        // System.out.println("Đang xử lý chuyển sang trang sau...");
        // }

        // @FXML
        // private void xuLyChonTrang1(ActionEvent event) {
        // System.out.println("Đang xử lý chọn trang 1...");
        // }

        // @FXML
        // private void xuLyChonTrang2(ActionEvent event) {
        // System.out.println("Đang xử lý chọn trang 2...");
        // }

        // @FXML
        // private void xuLyTrangCuoi(ActionEvent event) {
        // System.out.println("Đang xử lý chuyển đến trang cuối...");
        // }

        private void cauHinhBangLichSuPhieuNhap() {
                System.out.println("Đang cấu hình bảng lịch sử nhập thuốc...");

                colStt.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                                tableLichSuNhapHang.getItems()
                                                .indexOf(cellData.getValue()) + 1));

                colMaPhieuNhap.setCellValueFactory(cellData -> {
                        PhieuNhap phieuNhap = cellData.getValue();

                        String maPhieuNhap = phieuNhap == null || phieuNhap.getId() == null
                                        ? ""
                                        : "PN" + phieuNhap.getId();

                        return new SimpleObjectProperty<>(maPhieuNhap);
                });

                colNhaCungCap.setCellValueFactory(
                                new PropertyValueFactory<>("nhaCungCap"));

                colNgayNhap.setCellValueFactory(
                                new PropertyValueFactory<>("createdAt"));

                DatePickerFormatter.formatTableColumnLocalDateTimeToVN(colNgayNhap);
                cauHinhCotXemAnhPhieuNhap();

                tableLichSuNhapHang.setItems(
                                FXCollections.observableArrayList());
        }

        /**
         * Lấy toàn bộ phiếu nhập từ database và lưu vào danh sách gốc.
         */
        private void taiDuLieuLenBang() {
                List<PhieuNhap> danhSach = phieuNhapService.getAllPhieuNhap();
                danhSachPhieuNhapGoc.setAll(danhSach);
        }

        /**
         * Tìm theo mã phiếu nhập, nhà cung cấp hoặc ghi chú.
         */
        private List<PhieuNhap> timKiemPhieuNhap(
                        List<PhieuNhap> phieuNhaps,
                        String tuKhoa) {
                if (tuKhoa == null || tuKhoa.isBlank()) {
                        return phieuNhaps;
                }

                String keyword = tuKhoa.trim().toLowerCase(Locale.ROOT);

                return phieuNhaps.stream()
                                .filter(phieuNhap -> phieuNhap != null)
                                .filter(phieuNhap -> {
                                        String id = phieuNhap.getId() == null
                                                        ? ""
                                                        : String.valueOf(phieuNhap.getId());

                                        String maPhieuNhap = "pn" + id;

                                        String nhaCungCap = phieuNhap.getNhaCungCap() == null
                                                        ? ""
                                                        : phieuNhap.getNhaCungCap()
                                                                        .toLowerCase(Locale.ROOT);

                                        String ghiChu = phieuNhap.getGhiChu() == null
                                                        ? ""
                                                        : phieuNhap.getGhiChu()
                                                                        .toLowerCase(Locale.ROOT);

                                        return id.contains(keyword)
                                                        || maPhieuNhap.contains(keyword)
                                                        || nhaCungCap.contains(keyword)
                                                        || ghiChu.contains(keyword);
                                })
                                .collect(Collectors.toList());
        }

        /**
         * Lọc phiếu nhập theo createdAt và sắp xếp mới nhất trước.
         */
        private List<PhieuNhap> locTheoKhoangNgayNhap(
                        List<PhieuNhap> phieuNhaps) {
                LocalDate tuNgay = dpTuNgay.getValue();
                LocalDate denNgay = dpDenNgay.getValue();

                return phieuNhaps.stream()
                                .filter(phieuNhap -> phieuNhap != null)
                                .filter(phieuNhap -> {
                                        LocalDateTime createdAt = phieuNhap.getCreatedAt();

                                        if (createdAt == null) {
                                                return tuNgay == null && denNgay == null;
                                        }

                                        LocalDate ngayNhap = createdAt.toLocalDate();

                                        boolean sauHoacBangTuNgay = tuNgay == null || !ngayNhap.isBefore(tuNgay);

                                        boolean truocHoacBangDenNgay = denNgay == null || !ngayNhap.isAfter(denNgay);

                                        return sauHoacBangTuNgay && truocHoacBangDenNgay;
                                })
                                .sorted(Comparator.comparing(
                                                PhieuNhap::getCreatedAt,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .collect(Collectors.toList());
        }

        /**
         * Áp dụng tìm kiếm, lọc ngày, sau đó cập nhật bảng và các card.
         */
        private void apDungBoLoc() {
                List<PhieuNhap> ketQua = new ArrayList<>(danhSachPhieuNhapGoc);

                ketQua = timKiemPhieuNhap(
                                ketQua,
                                txtSearch.getText());

                ketQua = locTheoKhoangNgayNhap(ketQua);

                tableLichSuNhapHang.setItems(
                                FXCollections.observableArrayList(ketQua));

                tableLichSuNhapHang.refresh();
                capNhatThongTinTongQuan(ketQua);
        }

        private void capNhatThongTinTongQuan(List<PhieuNhap> phieuNhaps) {
                int tongPhieuNhap = phieuNhaps == null
                                ? 0
                                : phieuNhaps.size();

                LocalDate homNay = LocalDate.now();

                long soPhieuNhapHomNay = phieuNhaps == null
                                ? 0
                                : phieuNhaps.stream()
                                                .filter(phieuNhap -> phieuNhap != null)
                                                .filter(phieuNhap -> phieuNhap.getCreatedAt() != null)
                                                .filter(phieuNhap -> homNay.equals(
                                                                phieuNhap.getCreatedAt().toLocalDate()))
                                                .count();

                lblKetQua.setText(tongPhieuNhap + " kết quả");
                lblTongHoaDon.setText(String.valueOf(tongPhieuNhap));
                lblHomNay.setText(String.valueOf(soPhieuNhapHomNay));

                if (tongPhieuNhap == 0) {
                        lblPhanTrang.setText(
                                        "Hiển thị 0-0 trong 0 phiếu nhập");
                } else {
                        lblPhanTrang.setText(
                                        "Hiển thị 1-" + tongPhieuNhap
                                                        + " trong " + tongPhieuNhap
                                                        + " phiếu nhập");
                }
        }

        private void cauHinhCotXemAnhPhieuNhap() {
                colChiTiet.setCellFactory(column -> new TableCell<PhieuNhap, Void>() {

                        private final Button btnXemAnh = new Button("\uD83D\uDC41");

                        {
                                btnXemAnh.setFocusTraversable(false);

                                btnXemAnh.setStyle("""
                                                -fx-background-color: #eff6ff;
                                                -fx-text-fill: #2563eb;
                                                -fx-background-radius: 8;
                                                -fx-border-color: #bfdbfe;
                                                -fx-border-radius: 8;
                                                -fx-cursor: hand;
                                                -fx-font-family: "Segoe UI Emoji";
                                                -fx-font-size: 17px;
                                                -fx-padding: 4px 10px;
                                                """);

                                btnXemAnh.setOnAction(event -> {
                                        PhieuNhap phieuNhap = getTableRow() == null
                                                        ? null
                                                        : getTableRow().getItem();

                                        if (phieuNhap != null) {
                                                hienThiAnhPhieuNhap(
                                                                phieuNhap);
                                        }
                                });
                        }

                        @Override
                        protected void updateItem(
                                        Void item,
                                        boolean empty) {
                                super.updateItem(item, empty);

                                PhieuNhap phieuNhap = getTableRow() == null
                                                ? null
                                                : getTableRow().getItem();

                                if (empty || phieuNhap == null) {
                                        setGraphic(null);
                                        return;
                                }

                                String duongDanAnh = phieuNhap
                                                .getAnhHoaDonNhapPath();

                                boolean coAnh = duongDanAnh != null
                                                && !duongDanAnh.isBlank();

                                btnXemAnh.setDisable(!coAnh);
                                btnXemAnh.setOpacity(
                                                coAnh ? 1.0 : 0.35);

                                btnXemAnh.setTooltip(
                                                new Tooltip(
                                                                coAnh
                                                                                ? "Xem ảnh phiếu nhập"
                                                                                : "Phiếu nhập chưa có ảnh"));

                                setText(null);
                                setAlignment(Pos.CENTER);
                                setGraphic(btnXemAnh);
                        }
                });
        }

        private void hienThiAnhPhieuNhap(
                        PhieuNhap phieuNhap) {
                if (phieuNhap == null) {
                        return;
                }

                String duongDanAnh = phieuNhap.getAnhHoaDonNhapPath();

                if (duongDanAnh == null
                                || duongDanAnh.isBlank()) {

                        hienThiThongBaoAnh(
                                        Alert.AlertType.INFORMATION,
                                        "Chưa có ảnh",
                                        "Phiếu nhập này chưa được lưu ảnh.");
                        return;
                }

                File fileAnh = new File(duongDanAnh);

                /*
                 * Dự phòng trường hợp lưu đường dẫn tương đối.
                 */
                if (!fileAnh.isAbsolute()) {
                        fileAnh = new File(
                                        System.getProperty("user.dir"),
                                        duongDanAnh);
                }

                if (!fileAnh.exists() || !fileAnh.isFile()) {
                        hienThiThongBaoAnh(
                                        Alert.AlertType.ERROR,
                                        "Không tìm thấy ảnh",
                                        "Không tìm thấy file ảnh tại:\n"
                                                        + fileAnh.getAbsolutePath());
                        return;
                }

                try {
                        Image image = new Image(
                                        fileAnh.toURI().toString(),
                                        false);

                        if (image.isError()) {
                                throw new RuntimeException(
                                                "File ảnh bị lỗi hoặc "
                                                                + "không đúng định dạng.");
                        }

                        ImageView imageView = new ImageView(image);

                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        imageView.setFitWidth(500);

                        StackPane khungAnh = new StackPane(imageView);

                        khungAnh.setAlignment(Pos.TOP_CENTER);
                        khungAnh.setPadding(new Insets(15));

                        ScrollPane scrollPane = new ScrollPane(khungAnh);

                        scrollPane.setFitToWidth(true);
                        scrollPane.setPannable(true);

                        scrollPane.setHbarPolicy(
                                        ScrollPane.ScrollBarPolicy.AS_NEEDED);

                        scrollPane.setVbarPolicy(
                                        ScrollPane.ScrollBarPolicy.AS_NEEDED);

                        Dialog<Void> dialog = new Dialog<>();

                        String maPhieuNhap = phieuNhap.getId() == null
                                        ? ""
                                        : "PN" + phieuNhap.getId();

                        dialog.setTitle(
                                        "Ảnh phiếu nhập " + maPhieuNhap);

                        dialog.setHeaderText(
                                        phieuNhap.getNhaCungCap() == null
                                                        ? maPhieuNhap
                                                        : maPhieuNhap
                                                                        + " - "
                                                                        + phieuNhap.getNhaCungCap());

                        dialog.setResizable(true);

                        if (tableLichSuNhapHang.getScene() != null
                                        && tableLichSuNhapHang
                                                        .getScene()
                                                        .getWindow() != null) {

                                dialog.initOwner(
                                                tableLichSuNhapHang
                                                                .getScene()
                                                                .getWindow());
                        }

                        dialog.getDialogPane()
                                        .getButtonTypes()
                                        .add(ButtonType.CLOSE);

                        dialog.getDialogPane().setContent(
                                        scrollPane);

                        dialog.getDialogPane().setPrefSize(
                                        650,
                                        750);

                        dialog.showAndWait();

                } catch (Exception e) {
                        e.printStackTrace();

                        hienThiThongBaoAnh(
                                        Alert.AlertType.ERROR,
                                        "Lỗi mở ảnh",
                                        "Không thể hiển thị ảnh phiếu nhập: "
                                                        + e.getMessage());
                }
        }

        private void hienThiThongBaoAnh(
                        Alert.AlertType loai,
                        String tieuDe,
                        String noiDung) {
                Alert alert = new Alert(loai);

                alert.setTitle(tieuDe);
                alert.setHeaderText(null);
                alert.setContentText(noiDung);

                if (tableLichSuNhapHang.getScene() != null) {
                        alert.initOwner(
                                        tableLichSuNhapHang
                                                        .getScene()
                                                        .getWindow());
                }

                alert.showAndWait();
        }
}
