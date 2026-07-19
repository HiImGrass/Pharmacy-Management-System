package com.example.PharmacyManagement.gui.controller;

//Java imports
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

//JavaFX imports
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;

//Models, services and DTO imports
import com.example.PharmacyManagement.dto.ChiTietHoaDonRequestDTO;
import com.example.PharmacyManagement.dto.HoaDonRequestDTO;
import com.example.PharmacyManagement.model.ChiTietHoaDon;
import com.example.PharmacyManagement.model.HoaDon;
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.service.HoaDonInService;
import com.example.PharmacyManagement.service.HoaDonService;

//Component imports

//Utils imports

import com.example.PharmacyManagement.gui.util.MoneyFormatter;

@Controller
@Scope("prototype")
public class ChiTietToaController {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final long ID_DONG_TONG_CONG = -99L;

    @FXML
    private TableView<ChiTietHoaDon> tableChiTiet;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colTenThuoc;

    @FXML
    private TableColumn<ChiTietHoaDon, Integer> colSoLuong;

    @FXML
    private TableColumn<ChiTietHoaDon, BigDecimal> colDonGia;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colDonVi;

    @FXML
    private TableColumn<ChiTietHoaDon, BigDecimal> colThanhTien;

    @FXML
    private Label lblTongTien;

    @Autowired
    private HoaDonInService hoaDonInService;

    @Autowired
    private HoaDonService hoaDonService;

    private final ObservableList<ChiTietHoaDon> danhSachGoc = FXCollections.observableArrayList();
    private final NumberFormat tienVietNamFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        cauHinhBangChiTiet();
        cauHinhCotTenThuoc();
        cauHinhCotSoLuong();
        cauHinhCotDonGia();
        cauHinhCotHienThi();
        cauHinhTrangTriDongTongCong();
        lamMoiBangVaTinhTong();
    }

    private void cauHinhBangChiTiet() {
        tableChiTiet.setItems(danhSachGoc);
        tableChiTiet.setEditable(true);
        tableChiTiet.getSelectionModel().setCellSelectionEnabled(true);
    }

    private void cauHinhCotTenThuoc() {
        colTenThuoc.setCellValueFactory(cellData -> {
            Thuoc thuoc = cellData.getValue().getThuoc();
            return new SimpleStringProperty(thuoc != null ? thuoc.getTenThuoc() : "");
        });

        colTenThuoc.setCellFactory(TextFieldTableCell.forTableColumn());
        colTenThuoc.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongDangSua(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            if (chiTiet.getThuoc() == null) {
                chiTiet.setThuoc(new Thuoc());
            }

            chiTiet.getThuoc().setTenThuoc(event.getNewValue());
            lamMoiBangVaTinhTong();
        });
    }

    private void cauHinhCotSoLuong() {
        colSoLuong.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getSoLuong()).asObject());

        colSoLuong.setCellFactory(column -> new TextFieldTableCell<>(new IntegerStringConverter()) {
            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                ChiTietHoaDon rowData = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || item == null || laDongTongCong(rowData)) {
                    setText(null);
                    return;
                }

                setText(item.toString());
            }
        });

        colSoLuong.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongDangSua(event.getTablePosition().getRow());
            Integer soLuongMoi = event.getNewValue();

            if (chiTiet == null) {
                return;
            }

            if (soLuongMoi == null || soLuongMoi <= 0) {
                danhSachGoc.remove(chiTiet);
            } else {
                chiTiet.setSoLuong(soLuongMoi);
                capNhatThanhTien(chiTiet);
            }

            lamMoiBangVaTinhTong();
        });
    }

    private void cauHinhCotDonGia() {
        colDonGia.setCellValueFactory(cellData -> new SimpleObjectProperty<>(layDonGiaAnToan(cellData.getValue())));

        colDonGia.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        MoneyFormatter.formatTableColumnToVN(colDonGia);

        colDonGia.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongDangSua(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            BigDecimal donGiaMoi = event.getNewValue();
            chiTiet.setDonGia(donGiaMoi == null || donGiaMoi.compareTo(ZERO) < 0 ? ZERO : donGiaMoi);
            capNhatThanhTien(chiTiet);
            lamMoiBangVaTinhTong();
        });
    }

    private void cauHinhCotHienThi() {
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        MoneyFormatter.formatTableColumnToVN(colThanhTien);
    }

    private void cauHinhTrangTriDongTongCong() {
        tableChiTiet.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ChiTietHoaDon item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                    return;
                }

                if (laDongTongCong(item)) {
                    setStyle("-fx-background-color: #eff6ff; -fx-font-weight: bold;");
                    getChildren().forEach(node -> node.setStyle("-fx-text-fill: #1d4ed8; -fx-font-size: 14px;"));
                } else {
                    setStyle("");
                    getChildren().forEach(node -> node.setStyle(""));
                }
            }
        });
    }

    public void themThuocVaoToa(ChiTietHoaDon chiTietMoi) {
        if (chiTietMoi == null) {
            return;
        }

        capNhatThanhTien(chiTietMoi);
        danhSachGoc.add(chiTietMoi);
        lamMoiBangVaTinhTong();
    }

    public void themThuocVaoToa(Thuoc thuoc) {
        if (thuoc == null) {
            return;
        }

        ChiTietHoaDon chiTietTonTai = timChiTietTheoThuocId(thuoc.getId());
        if (chiTietTonTai != null) {
            tangSoLuongChiTiet(chiTietTonTai, thuoc);
            lamMoiBangVaTinhTong();
            return;
        }

        danhSachGoc.add(taoChiTietTuThuoc(thuoc));
        lamMoiBangVaTinhTong();
    }

    public void xoaDongDuocChon() {
        ChiTietHoaDon hangDuocChon = tableChiTiet.getSelectionModel().getSelectedItem();
        if (hangDuocChon == null || laDongTongCong(hangDuocChon)) {
            return;
        }

        danhSachGoc.remove(hangDuocChon);
        lamMoiBangVaTinhTong();
    }

    public boolean xuLyThanhToan(Long khachHangId, String tenKhachHang) {
        if (danhSachGoc.isEmpty()) {
            hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Không có thuốc nào trong toa để thanh toán!");
            return false;
        }

        if (khachHangId == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khách hàng trước khi thanh toán!");
            return false;
        }

        if (tenKhachHang == null || tenKhachHang.isBlank()) {
            hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập tên khách hàng trước khi thanh toán!");
            return false;
        }

        BigDecimal tongTienPhaiTra = tinhTongTien();
        Optional<BigDecimal> tienKhachDuaOptional = hoiTienKhachDua(tongTienPhaiTra);
        if (tienKhachDuaOptional.isEmpty()) {
            return false;
        }

        BigDecimal tienKhachDua = tienKhachDuaOptional.get();

        /*
         * Cho phép khách đưa ít hơn tổng tiền.
         * Khi đó HoaDonInService sẽ tính:
         * tienThoiLai = tienKhachDua - tongTienPhaiTra
         * và kết quả âm được hiểu là số tiền khách còn nợ.
         */
        if (tienKhachDua.compareTo(ZERO) < 0) {
            hienThiThongBao(
                    Alert.AlertType.ERROR,
                    "Lỗi nhập liệu",
                    "Số tiền khách đưa không được nhỏ hơn 0.");
            return false;
        }

        if (!kiemTraTonKho()) {
            return false;
        }

        try {
            List<ChiTietHoaDon> danhSachIn = new ArrayList<>(danhSachGoc);

            HoaDonRequestDTO request = taoHoaDonRequest(khachHangId);

            // Lưu hóa đơn và lấy ID tự tăng
            HoaDon hoaDonDaTao = hoaDonService.createHoaDon(request);

            String canhBaoIn = null;
            try {
                File fileAnh = hoaDonInService.taoAnhHoaDon(
                        hoaDonDaTao.getId(),
                        danhSachIn,
                        tenKhachHang,
                        tienKhachDua);

                if (fileAnh == null || !fileAnh.exists()) {
                    throw new RuntimeException(
                            "Không thể tạo ảnh hóa đơn.");
                }

                // Lưu đường dẫn ảnh
                hoaDonService.capNhatAnhHoaDon(
                        hoaDonDaTao.getId(),
                        fileAnh.getAbsolutePath());

                try {
                    hoaDonInService.inAnhHoaDon(fileAnh);
                } catch (Exception loiIn) {
                    // In ảnh hóa đơn
                    loiIn.printStackTrace();
                    canhBaoIn = "Hóa đơn đã được lưu nhưng không thể in: "
                            + loiIn.getMessage();
                }
            } catch (Exception loiTaoAnh) {
                loiTaoAnh.printStackTrace();
                canhBaoIn = "Có lỗi xảy ra khi in hóa đơn: " + loiTaoAnh.getMessage();
            }

            danhSachGoc.clear();
            lamMoiBangVaTinhTong();

            if (canhBaoIn == null) {
                hienThiThongBao(
                        Alert.AlertType.INFORMATION,
                        "Thành công",
                        "Thanh toán và in hóa đơn thành công!");
            } else {
                hienThiThongBao(
                        Alert.AlertType.WARNING,
                        "Cảnh báo",
                        canhBaoIn);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            hienThiThongBao(
                    Alert.AlertType.ERROR,
                    "Lỗi hệ thống",
                    "Có lỗi xảy ra khi thanh toán hoặc in hóa đơn: "
                            + e.getMessage());

            return false;
        }
    }

    public void lamMoiBangVaTinhTong() {
        if (tableChiTiet != null) {
            tableChiTiet.refresh();
        }

        if (lblTongTien != null) {
            lblTongTien.setText(dinhDangTien(tinhTongTien()));
        }
    }

    private Optional<BigDecimal> hoiTienKhachDua(BigDecimal tongTienPhaiTra) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Xác nhận tiền mặt");
        dialog.setHeaderText("Tổng tiền cần thanh toán: " + dinhDangTien(tongTienPhaiTra) + " VND");
        dialog.setContentText("Nhập số tiền khách đưa (VND):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return Optional.empty();
        }

        String input = result.get().trim();
        if (input.isEmpty()) {
            return Optional.of(tongTienPhaiTra);
        }

        String inputDaLoc = input.replace(".", "").replace(",", "").replaceAll("[^0-9]", "");
        if (inputDaLoc.isBlank()) {
            hienThiThongBao(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                    "Số tiền nhập vào không hợp lệ! Vui lòng chỉ nhập số.");
            return Optional.empty();
        }

        try {
            return Optional.of(new BigDecimal(inputDaLoc));
        } catch (NumberFormatException e) {
            hienThiThongBao(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                    "Số tiền nhập vào không hợp lệ! Vui lòng chỉ nhập số.");
            return Optional.empty();
        }
    }

    private HoaDonRequestDTO taoHoaDonRequest(Long khachHangId) {
        return new HoaDonRequestDTO(
                khachHangId,
                null,
                LocalDate.now(),
                danhSachGoc.stream()
                        .filter(this::laChiTietCoTheLuuVaoHoaDon)
                        .map(ct -> new ChiTietHoaDonRequestDTO(ct.getThuoc().getId(), ct.getSoLuong(), ct.getDonGia()))
                        .collect(Collectors.toList()));
    }

    private boolean laChiTietCoTheLuuVaoHoaDon(ChiTietHoaDon chiTiet) {
        return chiTiet != null
                && chiTiet.getThuoc() != null
                && chiTiet.getThuoc().getId() != null
                && chiTiet.getSoLuong() > 0;
    }

    private boolean kiemTraTonKho() {
        for (ChiTietHoaDon chiTiet : danhSachGoc) {
            Thuoc thuoc = chiTiet.getThuoc();
            if (thuoc == null || thuoc.getId() == null) {
                continue;
            }

            Integer tonKho = thuoc.getSoLuongTon();
            if (tonKho == null || tonKho < chiTiet.getSoLuong()) {
                hienThiThongBao(
                        Alert.AlertType.ERROR,
                        "Lỗi xuất kho",
                        "Thuốc '" + thuoc.getTenThuoc() + "' không đủ số lượng trong kho! Còn tồn: " + tonKho);
                return false;
            }
        }

        return true;
    }

    private ChiTietHoaDon taoChiTietTuThuoc(Thuoc thuoc) {
        BigDecimal donGia = thuoc.getGiaBanSi() != null ? thuoc.getGiaBanSi() : ZERO;

        ChiTietHoaDon chiTiet = new ChiTietHoaDon();
        chiTiet.setThuoc(thuoc);
        chiTiet.setSoLuong(1);
        chiTiet.setDonVi(thuoc.getDonVi() != null ? thuoc.getDonVi() : "");
        chiTiet.setDonGia(donGia);
        chiTiet.setThanhTien(donGia);
        return chiTiet;
    }

    private void tangSoLuongChiTiet(ChiTietHoaDon chiTiet, Thuoc thuoc) {
        chiTiet.setSoLuong(chiTiet.getSoLuong() + 1);
        chiTiet.setDonVi(thuoc.getDonVi() != null ? thuoc.getDonVi() : "");

        if (chiTiet.getDonGia() == null) {
            chiTiet.setDonGia(thuoc.getGiaBanSi() != null ? thuoc.getGiaBanSi() : ZERO);
        }

        capNhatThanhTien(chiTiet);
    }

    private ChiTietHoaDon timChiTietTheoThuocId(Long thuocId) {
        if (thuocId == null) {
            return null;
        }

        return danhSachGoc.stream()
                .filter(ct -> ct.getThuoc() != null && thuocId.equals(ct.getThuoc().getId()))
                .findFirst()
                .orElse(null);
    }

    private void capNhatThanhTien(ChiTietHoaDon chiTiet) {
        if (chiTiet == null) {
            return;
        }

        chiTiet.setThanhTien(layDonGiaAnToan(chiTiet).multiply(BigDecimal.valueOf(Math.max(chiTiet.getSoLuong(), 0))));
    }

    private BigDecimal tinhTongTien() {
        return danhSachGoc.stream()
                .filter(ct -> !laDongTongCong(ct))
                .map(ChiTietHoaDon::getThanhTien)
                .filter(thanhTien -> thanhTien != null)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal layDonGiaAnToan(ChiTietHoaDon chiTiet) {
        return chiTiet != null && chiTiet.getDonGia() != null ? chiTiet.getDonGia() : ZERO;
    }

    private ChiTietHoaDon layDongDangSua(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= tableChiTiet.getItems().size()) {
            return null;
        }

        ChiTietHoaDon chiTiet = tableChiTiet.getItems().get(rowIndex);
        return laDongTongCong(chiTiet) ? null : chiTiet;
    }

    private boolean laDongTongCong(ChiTietHoaDon chiTiet) {
        return chiTiet != null && chiTiet.getId() != null && chiTiet.getId() == ID_DONG_TONG_CONG;
    }

    private String dinhDangTien(BigDecimal soTien) {
        return tienVietNamFormatter.format(soTien != null ? soTien : ZERO);
    }

    public void hienThiThongBao(Alert.AlertType loaiThongBao, String tieuDe, String noiDung) {
        Alert alert = new Alert(loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
