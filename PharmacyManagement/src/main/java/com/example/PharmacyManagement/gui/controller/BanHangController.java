package com.example.PharmacyManagement.gui.controller;

import com.example.PharmacyManagement.gui.component.FxmlLoaderService;
import com.example.PharmacyManagement.gui.component.FxmlLoaderService.LoadedView;
import com.example.PharmacyManagement.model.KhachHang;
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.service.KhachHangService;
import com.example.PharmacyManagement.service.ThuocService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class BanHangController {

    private static final String CHI_TIET_TOA_FXML = "/fxml/modules/ChiTietToa.fxml";
    private static final String TEN_TOA_MAC_DINH = "Toa Thuốc Mới ";
    private static final String PROMPT_CHON_KHACH_HANG = "Chọn khách hàng...";
    private static final String TEN_KHACH_LE = "Khách lẻ";

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField txtSearchThuoc;

    @FXML
    private Button btnThemThuoc;

    @FXML
    private Button btnSuaThongTin;

    @FXML
    private Button btnXoaChiTiet;

    @FXML
    private ComboBox<KhachHang> cbKhachHang;

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

    @Autowired
    private FxmlLoaderService fxmlLoaderService;

    @Autowired
    private ThuocService thuocService;

    @Autowired
    private KhachHangService khachHangService;

    private int demSoToa = 1;
    private boolean dangDongBoComboBox = false;

    private final Map<Tab, ChiTietToaController> toaControllers = new HashMap<>();
    private final Map<Tab, KhachHang> khachHangCuaToa = new HashMap<>();
    private final ObservableList<KhachHang> danhSachKhachHang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        toaControllers.clear();
        khachHangCuaToa.clear();

        cauHinhComboBoxKhachHang();
        cauHinhBangThuoc();
        cauHinhSuKienChonKhachHang();
        cauHinhSuKienChuyenTab();

        taiDanhSachKhachHang();
        refreshTable();
        cauHinhTimKiemTuDong();

    }

    private void cauHinhComboBoxKhachHang() {
        cbKhachHang.setPromptText(PROMPT_CHON_KHACH_HANG);
        cbKhachHang.setConverter(new StringConverter<>() {
            @Override
            public String toString(KhachHang khachHang) {
                return layTenKhachHang(khachHang);
            }

            @Override
            public KhachHang fromString(String tenKhachHang) {
                return timKhachHangTheoTen(tenKhachHang);
            }
        });

        cbKhachHang.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(KhachHang item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : layTenKhachHang(item));
            }
        });

        cbKhachHang.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(KhachHang item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : layTenKhachHang(item));
            }
        });
    }

    private void cauHinhBangThuoc() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        colGiaNhap.setCellValueFactory(new PropertyValueFactory<>("giaNhap"));
        colGiaBanSi.setCellValueFactory(new PropertyValueFactory<>("giaBanSi"));
        colSoLuongTon.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colHanSuDung.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));

        tableThuoc.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableThuoc.setRowFactory(tv -> {
            TableRow<Thuoc> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    themThuocVaoToaDangChon(row.getItem());
                }
            });
            return row;
        });
    }

    private void cauHinhSuKienChonKhachHang() {
        cbKhachHang.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (dangDongBoComboBox) {
                return;
            }

            Tab tabDangChon = layTabDangChon();
            if (tabDangChon != null) {
                if (newValue == null) {
                    khachHangCuaToa.remove(tabDangChon);
                } else {
                    khachHangCuaToa.put(tabDangChon, newValue);
                }
            }
        });
    }

    private void cauHinhSuKienChuyenTab() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            dangDongBoComboBox = true;
            try {
                if (newTab == null) {
                    cbKhachHang.getSelectionModel().clearSelection();
                    return;
                }

                KhachHang khachHangDaChon = khachHangCuaToa.get(newTab);
                if (khachHangDaChon == null) {
                    cbKhachHang.getSelectionModel().clearSelection();
                } else {
                    cbKhachHang.getSelectionModel().select(khachHangDaChon);
                }
            } finally {
                dangDongBoComboBox = false;
            }
        });
    }

    private void taiDanhSachKhachHang() {
        danhSachKhachHang.setAll(khachHangService.layTatCaKhachHang());
        cbKhachHang.setItems(danhSachKhachHang);
    }

    private void refreshTable() {
        tableThuoc.setItems(FXCollections.observableArrayList(thuocService.getAllThuoc()));
    }

    @FXML
    public void xuLyTaoToaMoi() {
        try {
            Tab tabMoi = taoTabToaMoi();
            tabPane.getTabs().add(tabMoi);
            tabPane.getSelectionModel().select(tabMoi);
        } catch (Exception e) {
            hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tạo toa mới: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Tab taoTabToaMoi() {
        String tenToa = TEN_TOA_MAC_DINH + demSoToa++;
        LoadedView<ChiTietToaController> loadedView = fxmlLoaderService.loadWithController(
                CHI_TIET_TOA_FXML,
                ChiTietToaController.class
        );

        Tab tabMoi = new Tab(tenToa);
        tabMoi.setContent(loadedView.getView());
        toaControllers.put(tabMoi, loadedView.getController());

        tabMoi.setOnClosed(event -> {
            toaControllers.remove(tabMoi);
            khachHangCuaToa.remove(tabMoi);
        });

        return tabMoi;
    }

    private void themThuocVaoToaDangChon(Thuoc thuoc) {
        if (thuoc == null) {
            return;
        }

        ChiTietToaController controller = layControllerToaDangChon();
        if (controller == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa có toa", "Vui lòng tạo toa mới trước khi thêm thuốc.");
            return;
        }

        controller.themThuocVaoToa(thuoc);
    }

    @FXML
    public void xuLyXoaChiTiet() {
        ChiTietToaController controller = layControllerToaDangChon();
        if (controller != null) {
            controller.xoaDongDuocChon();
        }
    }

    @FXML
    public void xuLyThanhToan() {
        ChiTietToaController controller = layControllerToaDangChon();
        Tab tabDangChon = layTabDangChon();

        if (controller == null || tabDangChon == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa có toa", "Vui lòng tạo toa trước khi thanh toán.");
            return;
        }

        KhachHang khachHang = cbKhachHang.getValue();
        Long khachHangId = khachHang != null ? khachHang.getId() : null;
        String tenKhachHang = khachHang != null ? layTenKhachHang(khachHang) : TEN_KHACH_LE;

        boolean thanhToanThanhCong = controller.xuLyThanhToan(khachHangId, tenKhachHang);
        if (thanhToanThanhCong) {
            dongTab(tabDangChon);
            refreshTable();
        }
    }

    private void dongTab(Tab tab) {
        toaControllers.remove(tab);
        khachHangCuaToa.remove(tab);
        tabPane.getTabs().remove(tab);
    }

    private Tab layTabDangChon() {
        return tabPane == null ? null : tabPane.getSelectionModel().getSelectedItem();
    }

    private ChiTietToaController layControllerToaDangChon() {
        Tab tabDangChon = layTabDangChon();
        return tabDangChon == null ? null : toaControllers.get(tabDangChon);
    }

    private KhachHang timKhachHangTheoTen(String tenKhachHang) {
        if (tenKhachHang == null || tenKhachHang.isBlank()) {
            return null;
        }

        return danhSachKhachHang.stream()
                .filter(kh -> Objects.equals(layTenKhachHang(kh), tenKhachHang))
                .findFirst()
                .orElse(null);
    }

    private String layTenKhachHang(KhachHang khachHang) {
        if (khachHang == null || khachHang.getTenKhachHang() == null || khachHang.getTenKhachHang().isBlank()) {
            return TEN_KHACH_LE;
        }
        return khachHang.getTenKhachHang();
    }

    public void hienThiThongBao(Alert.AlertType loaiThongBao, String tieuDe, String noiDung) {
        Alert alert = new Alert(loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void cauHinhTimKiemTuDong() {
        txtSearchThuoc.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                refreshTable();
                return;
            }

            List<Thuoc> ketQua = thuocService.timKiemThuoc(newValue.trim());
            tableThuoc.setItems(FXCollections.observableArrayList(ketQua));
        });
    }
}
