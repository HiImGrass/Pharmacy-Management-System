package com.example.PharmacyManagement.gui.controller;

//Java imports
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
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

//Models and services imports
import com.example.PharmacyManagement.service.HoaDonService;
import com.example.PharmacyManagement.model.HoaDon;

//Utils imports
import com.example.PharmacyManagement.gui.util.DatePickerFormatter;
import com.example.PharmacyManagement.gui.util.MoneyFormatter;

@Controller
public class LichSuBanThuocController {

        @FXML
        private TableView<HoaDon> tableLichSuBanHang;

        @FXML
        private Label lblKetQua;

        @FXML
        private Label lblPhanTrang;

        @FXML
        private Label lblTongHoaDon;

        @FXML
        private Label lblDoanhThu;

        @FXML
        private Label lblTrungBinhDon;

        @FXML
        private Label lblHomNay;

        @FXML
        private TextField txtSearch;

        @FXML
        private DatePicker dpTuNgay;

        @FXML
        private DatePicker dpDenNgay;

        @FXML
        private TableColumn<HoaDon, Integer> colStt;
        @FXML
        private TableColumn<HoaDon, String> colMaHoaDon;
        @FXML
        private TableColumn<HoaDon, String> colKhachHang;
        @FXML
        private TableColumn<HoaDon, BigDecimal> colTongTien;
        @FXML
        private TableColumn<HoaDon, LocalDateTime> colNgayBan;
        @FXML
        private TableColumn<HoaDon, Void> colChiTiet;

        @Autowired
        private HoaDonService hoaDonService;

        private final ObservableList<HoaDon> danhSachHoaDonGoc = FXCollections.observableArrayList();

        @FXML
        public void initialize() {
                System.out.println("Khởi tạo module Lịch Sử Bán Thuốc");

                // Cấu hình cách hiển thị các cột
                cauHinhBangLichSuHoaDon();

                // Lấy dữ liệu từ database
                taiDuLieuLenBang();

                // Hiển thị dữ liệu ban đầu
                apDungBoLoc();
                // Cấu hình DatePicker hiển thị theo định dạng Việt Nam
                DatePickerFormatter.formatDatePickerToVn(dpTuNgay);
                DatePickerFormatter.formatDatePickerToVn(dpDenNgay);
                // Tìm kiếm theo tên
                txtSearch.textProperty().addListener(
                                (observable, oldValue, newValue) -> apDungBoLoc());

                // Lọc từ ngày
                dpTuNgay.valueProperty().addListener(
                                (observable, oldValue, newValue) -> apDungBoLoc());

                // Lọc đến ngày
                dpDenNgay.valueProperty().addListener(
                                (observable, oldValue, newValue) -> apDungBoLoc());
        }

        @FXML
        private void xuLyLamMoi(ActionEvent event) {
                txtSearch.clear();
                dpTuNgay.setValue(null);
                dpDenNgay.setValue(null);
                apDungBoLoc();
        }

        // MỞ RỘNG TRONG TƯƠNG LAI NẾU CẦN THIẾT
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

        private void cauHinhBangLichSuHoaDon() {
                System.out.println("Đang cấu hình bảng lịch sử bán thuốc...");
                colStt.setCellValueFactory(
                                cellData -> new SimpleObjectProperty<>(
                                                tableLichSuBanHang.getItems().indexOf(cellData.getValue()) + 1));
                colMaHoaDon.setCellValueFactory(cellData -> {
                        HoaDon hoaDon = cellData.getValue();

                        String maHoaDon = hoaDon == null || hoaDon.getId() == null
                                        ? ""
                                        : "HD" + hoaDon.getId();

                        return new SimpleObjectProperty<>(maHoaDon);
                });
                colKhachHang.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
                colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTien"));
                MoneyFormatter.formatTableColumnToVN(colTongTien);
                colNgayBan.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
                DatePickerFormatter.formatTableColumnLocalDateTimeToVN(colNgayBan);

                cauHinhCotXemAnhHoaDon();
        }

        private void taiDuLieuLenBang() {
                List<HoaDon> danhSach = hoaDonService.getAllHoaDon();
                danhSachHoaDonGoc.setAll(danhSach);
        }

        private void capNhatThongTinTongQuan(List<HoaDon> hoaDons) {
                int tongHoaDon = hoaDons.size();
                BigDecimal tongDoanhThu = hoaDons.stream()
                                .map(HoaDon::getTongTien)
                                .filter(value -> value != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal trungBinh = tongHoaDon == 0
                                ? BigDecimal.ZERO
                                : tongDoanhThu.divide(BigDecimal.valueOf(tongHoaDon), 0,
                                                java.math.RoundingMode.HALF_UP);
                LocalDate homNay = LocalDate.now();
                long soHoaDonHomNay = hoaDons.stream()
                                .filter(hd -> homNay.equals(hd.getCreatedAt().toLocalDate()))
                                .count();

                if (lblKetQua != null) {
                        lblKetQua.setText(tongHoaDon + " kết quả");
                }
                if (lblPhanTrang != null) {
                        lblPhanTrang.setText("Hiển thị 1-" + tongHoaDon + " trong " + tongHoaDon + " hóa đơn");
                }
                if (lblTongHoaDon != null) {
                        lblTongHoaDon.setText(String.valueOf(tongHoaDon));
                }
                if (lblDoanhThu != null) {
                        lblDoanhThu.setText(MoneyFormatter.formatOrZero(tongDoanhThu));
                }
                if (lblTrungBinhDon != null) {
                        lblTrungBinhDon.setText(MoneyFormatter.formatOrZero(trungBinh));
                }
                if (lblHomNay != null) {
                        lblHomNay.setText(String.valueOf(soHoaDonHomNay));
                }
        }

        private List<HoaDon> sapXepTheoKhoangNgayBan(List<HoaDon> hoaDons) {
                LocalDate tuNgay = dpTuNgay.getValue();
                LocalDate denNgay = dpDenNgay.getValue();

                return hoaDons.stream()
                                .filter(hd -> {
                                        LocalDate ngayBan = hd.getCreatedAt().toLocalDate();
                                        boolean sauHoacBangTuNgay = (tuNgay == null) || !ngayBan.isBefore(tuNgay);
                                        boolean truocHoacBangDenNgay = (denNgay == null) || !ngayBan.isAfter(denNgay);
                                        return sauHoacBangTuNgay && truocHoacBangDenNgay;
                                })
                                .sorted(Comparator.comparing(HoaDon::getCreatedAt).reversed())
                                .collect(Collectors.toList());
        }

        private List<HoaDon> timKiemTheoTenKhachHang(List<HoaDon> hoaDons, String tenKhachHang) {
                if (tenKhachHang == null || tenKhachHang.isEmpty()) {
                        return hoaDons;
                }
                String tenKhachHangLower = tenKhachHang.toLowerCase();
                return hoaDons.stream()
                                .filter(hd -> hd.getTenKhachHang() != null
                                                && hd.getTenKhachHang().toLowerCase().contains(tenKhachHangLower))
                                .collect(Collectors.toList());
        }

        private void apDungBoLoc() {
                List<HoaDon> ketQua = new ArrayList<>(danhSachHoaDonGoc);

                ketQua = timKiemTheoTenKhachHang(
                                ketQua,
                                txtSearch.getText());

                ketQua = sapXepTheoKhoangNgayBan(ketQua);

                tableLichSuBanHang.setItems(
                                FXCollections.observableArrayList(ketQua));

                capNhatThongTinTongQuan(ketQua);
        }

        private void cauHinhCotXemAnhHoaDon() {
                colChiTiet.setCellFactory(column -> new TableCell<HoaDon, Void>() {

                        private final Button btnXemAnh = new Button("\uD83D\uDC41");

                        {
                                btnXemAnh.setTooltip(
                                                new Tooltip("Xem ảnh hóa đơn"));

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
                                        HoaDon hoaDon = getTableRow() == null
                                                        ? null
                                                        : getTableRow().getItem();

                                        if (hoaDon != null) {
                                                hienThiAnhHoaDon(hoaDon);
                                        }
                                });
                        }

                        @Override
                        protected void updateItem(
                                        Void item,
                                        boolean empty) {
                                super.updateItem(item, empty);

                                HoaDon hoaDon = getTableRow() == null
                                                ? null
                                                : getTableRow().getItem();

                                if (empty || hoaDon == null) {
                                        setGraphic(null);
                                        return;
                                }

                                String duongDan = hoaDon.getAnhHoaDonPath();

                                boolean coAnh = duongDan != null
                                                && !duongDan.isBlank();

                                btnXemAnh.setDisable(!coAnh);
                                btnXemAnh.setOpacity(coAnh ? 1.0 : 0.35);

                                btnXemAnh.setTooltip(
                                                new Tooltip(
                                                                coAnh
                                                                                ? "Xem ảnh hóa đơn"
                                                                                : "Hóa đơn chưa có ảnh"));

                                setText(null);
                                setAlignment(Pos.CENTER);
                                setGraphic(btnXemAnh);
                        }
                });
        }

        private void hienThiAnhHoaDon(HoaDon hoaDon) {
                if (hoaDon == null) {
                        return;
                }

                String duongDanAnh = hoaDon.getAnhHoaDonPath();

                if (duongDanAnh == null || duongDanAnh.isBlank()) {
                        hienThiThongBao(
                                        Alert.AlertType.INFORMATION,
                                        "Chưa có ảnh",
                                        "Hóa đơn này chưa được lưu ảnh.");
                        return;
                }

                File fileAnh = new File(duongDanAnh);

                /*
                 * Dự phòng trường hợp database lưu đường dẫn tương đối.
                 */
                if (!fileAnh.isAbsolute()) {
                        fileAnh = new File(
                                        System.getProperty("user.dir"),
                                        duongDanAnh);
                }

                if (!fileAnh.exists() || !fileAnh.isFile()) {
                        hienThiThongBao(
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
                                                "File ảnh bị lỗi hoặc không đúng định dạng.");
                        }

                        ImageView imageView = new ImageView(image);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);

                        /*
                         * Ảnh hóa đơn gốc rộng 576 px,
                         * hiển thị khoảng 500 px để vừa màn hình.
                         */
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

                        String maHoaDon = hoaDon.getId() == null
                                        ? ""
                                        : "HD" + hoaDon.getId();

                        dialog.setTitle("Ảnh hóa đơn " + maHoaDon);
                        dialog.setHeaderText(
                                        hoaDon.getTenKhachHang() == null
                                                        ? maHoaDon
                                                        : maHoaDon + " - "
                                                                        + hoaDon.getTenKhachHang());

                        dialog.setResizable(true);

                        if (tableLichSuBanHang.getScene() != null
                                        && tableLichSuBanHang
                                                        .getScene()
                                                        .getWindow() != null) {

                                dialog.initOwner(
                                                tableLichSuBanHang
                                                                .getScene()
                                                                .getWindow());
                        }

                        dialog.getDialogPane()
                                        .getButtonTypes()
                                        .add(ButtonType.CLOSE);

                        dialog.getDialogPane().setContent(scrollPane);
                        dialog.getDialogPane().setPrefSize(650, 750);

                        dialog.showAndWait();

                } catch (Exception e) {
                        e.printStackTrace();

                        hienThiThongBao(
                                        Alert.AlertType.ERROR,
                                        "Lỗi mở ảnh",
                                        "Không thể hiển thị ảnh hóa đơn: "
                                                        + e.getMessage());
                }
        }

        private void hienThiThongBao(
                        Alert.AlertType loai,
                        String tieuDe,
                        String noiDung) {
                Alert alert = new Alert(loai);
                alert.setTitle(tieuDe);
                alert.setHeaderText(null);
                alert.setContentText(noiDung);

                if (tableLichSuBanHang.getScene() != null) {
                        alert.initOwner(
                                        tableLichSuBanHang
                                                        .getScene()
                                                        .getWindow());
                }

                alert.showAndWait();
        }
}