package com.example.PharmacyManagement.gui.controller;

import com.example.PharmacyManagement.gui.component.FxmlLoaderService;
import com.example.PharmacyManagement.gui.component.FxmlLoaderService.LoadedView;
import com.example.PharmacyManagement.model.ChiTietHoaDon;
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.service.PhieuNhapInService;
import com.example.PharmacyManagement.model.PhieuNhap;
import com.example.PharmacyManagement.service.PhieuNhapService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Controller
public class NhapHangController {

    private static final String CHI_TIET_NHAP_FXML = "/fxml/modules/ChiTietNhap.fxml";
    private static final String TEN_TOA_MAC_DINH = "Phiếu nhập ";
    private static final String NHA_CUNG_CAP_MAC_DINH = "Nhà cung cấp chưa xác định";

    @FXML
    private Button btnTaoToa;

    @FXML
    private Button btnXoaChiTiet;

    @FXML
    private TabPane tabPane;

    @FXML
    private Button btnThanhToan;

    @Autowired
    private FxmlLoaderService fxmlLoaderService;

    @Autowired
    private PhieuNhapInService phieuNhapInService;

    @Autowired
    private PhieuNhapService phieuNhapService;

    private int demSoPhieuNhap = 1;

    private final Map<Tab, ChiTietToaNhapController> toaControllers = new HashMap<>();
    private final Map<Tab, String> nhaCungCapCuaToa = new HashMap<>();

    @FXML
    public void initialize() {
        toaControllers.clear();
        nhaCungCapCuaToa.clear();
    }

    @FXML
    public void xuLyTaoToaMoi(ActionEvent event) {
        try {
            Optional<String> nhaCungCapOptional = showDialogNhapNhaCungCap();
            if (nhaCungCapOptional.isEmpty()) {
                return;
            }

            String nhaCungCap = layGiaTriMacDinhNeuRong(nhaCungCapOptional.get(), NHA_CUNG_CAP_MAC_DINH);
            Tab tabMoi = taoTabToaNhapMoi(nhaCungCap);

            tabPane.getTabs().add(tabMoi);
            tabPane.getSelectionModel().select(tabMoi);
        } catch (Exception e) {
            e.printStackTrace();
            hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tạo phiếu nhập mới: " + e.getMessage());
        }
    }

    @FXML
    public void xuLyInHoaDon(ActionEvent event) {
        ChiTietToaNhapController controller = layControllerToaDangChon();

        Tab tabDangChon = layTabDangChon();

        if (controller == null || tabDangChon == null) {
            hienThiThongBao(
                    Alert.AlertType.WARNING,
                    "Chưa có phiếu nhập",
                    "Vui lòng tạo phiếu nhập trước khi in.");
            return;
        }

        List<ChiTietHoaDon> danhSachIn = new ArrayList<>(
                controller.layDanhSachNhapDeIn());

        if (danhSachIn.isEmpty()) {
            hienThiThongBao(
                    Alert.AlertType.WARNING,
                    "Phiếu nhập trống",
                    "Không có thuốc nào trong phiếu nhập để in.");
            return;
        }

        String nhaCungCap = nhaCungCapCuaToa.getOrDefault(
                tabDangChon,
                NHA_CUNG_CAP_MAC_DINH);

        String ghiChu = "";

        /*
         * Bước 1: Lưu database trước để lấy ID.
         */
        PhieuNhap phieuNhapDaLuu = controller.luuPhieuNhapVaTraVe(
                nhaCungCap,
                ghiChu);

        if (phieuNhapDaLuu == null
                || phieuNhapDaLuu.getId() == null) {
            return;
        }

        String maPhieuNhap = "PN" + phieuNhapDaLuu.getId();

        String canhBaoIn = null;

        try {
            /*
             * Bước 2: Tạo ảnh theo ID phiếu nhập.
             */
            File fileAnh = phieuNhapInService.taoAnhPhieuNhap(
                    danhSachIn,
                    maPhieuNhap,
                    nhaCungCap,
                    ghiChu);

            if (fileAnh == null || !fileAnh.exists()) {
                throw new RuntimeException(
                        "Không thể tạo ảnh phiếu nhập.");
            }

            /*
             * Bước 3: Lưu đường dẫn ảnh vào database.
             */
            phieuNhapService.capNhatAnhPhieuNhap(
                    phieuNhapDaLuu.getId(),
                    fileAnh.getAbsolutePath());

            /*
             * Bước 4: In ảnh vừa lưu.
             */
            try {
                phieuNhapInService.inAnhPhieuNhap(
                        fileAnh);

            } catch (Exception loiIn) {
                loiIn.printStackTrace();

                canhBaoIn = "Phiếu nhập đã được lưu nhưng "
                        + "không thể in: "
                        + loiIn.getMessage();
            }

        } catch (Exception loiTaoAnh) {
            loiTaoAnh.printStackTrace();

            canhBaoIn = "Phiếu nhập đã được lưu nhưng "
                    + "không thể tạo ảnh: "
                    + loiTaoAnh.getMessage();
        }

        /*
         * Phiếu đã được lưu và tồn kho đã tăng,
         * nên vẫn đóng tab để tránh nhập lại lần hai.
         */
        dongTab(tabDangChon);

        if (canhBaoIn == null) {
            hienThiThongBao(
                    Alert.AlertType.INFORMATION,
                    "Thành công",
                    "Đã lưu và in phiếu nhập "
                            + maPhieuNhap
                            + " thành công!");

        } else {
            hienThiThongBao(
                    Alert.AlertType.WARNING,
                    "Đã lưu phiếu nhập",
                    canhBaoIn);
        }
    }

    @FXML
    public void xuLyThemChiTiet(ActionEvent event) {
        ChiTietToaNhapController controller = layControllerToaDangChon();
        if (controller == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa có phiếu nhập",
                    "Vui lòng tạo phiếu nhập trước khi thêm thuốc.");
            return;
        }

        Optional<ChiTietHoaDon> ketQua = hienThiDialogNhapLieu(null);
        ketQua.ifPresent(controller::themChiTietNhap);
    }

    @FXML
    public void xuLySuaChiTiet(ActionEvent event) {
        ChiTietToaNhapController controller = layControllerToaDangChon();
        if (controller == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa có phiếu nhập",
                    "Vui lòng tạo phiếu nhập trước khi sửa chi tiết.");
            return;
        }

        ChiTietHoaDon chiTietDangChon = controller.layDongDuocChon();
        if (chiTietDangChon == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa chọn thuốc", "Vui lòng chọn dòng thuốc cần sửa.");
            return;
        }

        Optional<ChiTietHoaDon> ketQua = hienThiDialogNhapLieu(chiTietDangChon);
        ketQua.ifPresent(chiTietMoi -> controller.capNhatChiTietNhap(chiTietDangChon, chiTietMoi));
    }

    @FXML
    public void xuLyXoaChiTiet(ActionEvent event) {
        ChiTietToaNhapController controller = layControllerToaDangChon();
        if (controller != null) {
            controller.xoaDongDuocChon();
        }
    }

    private Tab taoTabToaNhapMoi(String nhaCungCap) {
        LoadedView<ChiTietToaNhapController> loadedView = fxmlLoaderService.loadWithController(
                CHI_TIET_NHAP_FXML,
                ChiTietToaNhapController.class);

        ChiTietToaNhapController controller = loadedView.getController();
        controller.datXuLySuaChiTiet(chiTietDangSua -> {
            Optional<ChiTietHoaDon> ketQua = hienThiDialogNhapLieu(chiTietDangSua);
            ketQua.ifPresent(chiTietMoi -> controller.capNhatChiTietNhap(chiTietDangSua, chiTietMoi));
        });

        String tenTab = TEN_TOA_MAC_DINH + demSoPhieuNhap++ + " - " + nhaCungCap;
        Tab tabMoi = new Tab(tenTab);
        tabMoi.setContent(loadedView.getView());

        toaControllers.put(tabMoi, controller);
        nhaCungCapCuaToa.put(tabMoi, nhaCungCap);

        tabMoi.setOnClosed(event -> {
            toaControllers.remove(tabMoi);
            nhaCungCapCuaToa.remove(tabMoi);
        });

        return tabMoi;
    }

    private Optional<String> showDialogNhapNhaCungCap() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Nhập nhà cung cấp");
        dialog.setHeaderText("Vui lòng nhập nhà cung cấp / nơi mua thuốc:");

        TextField txtNhaCungCap = new TextField();
        txtNhaCungCap.setPromptText("Ví dụ: Công ty Dược ABC");

        dialog.getDialogPane().setContent(txtNhaCungCap);

        ButtonType nutLuuType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(nutLuuType, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == nutLuuType) {
                return txtNhaCungCap.getText();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Optional<ChiTietHoaDon> hienThiDialogNhapLieu(ChiTietHoaDon chiTietHienTai) {
        Dialog<ChiTietHoaDon> dialog = new Dialog<>();
        dialog.setTitle(chiTietHienTai == null ? "Thêm thuốc nhập" : "Sửa thuốc nhập");
        dialog.setHeaderText("Vui lòng điền thông tin thuốc nhập kho:");

        ButtonType nutLuuType = new ButtonType("Lưu thông tin", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(nutLuuType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtTen = new TextField();
        txtTen.setPromptText("Nhập tên thuốc...");

        TextField txtSoLuong = new TextField();
        txtSoLuong.setPromptText("Nhập số lượng nhập...");

        TextField txtDonVi = new TextField();
        txtDonVi.setPromptText("Nhập đơn vị...");

        TextArea txtMoTa = new TextArea();
        txtMoTa.setPromptText("Nhập mô tả, có thể để trống...");
        txtMoTa.setPrefRowCount(3);
        txtMoTa.setWrapText(true);

        doDuLieuCuLenDialog(chiTietHienTai, txtTen, txtSoLuong, txtDonVi, txtMoTa);

        grid.add(new Label("Tên thuốc:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Số lượng nhập:"), 0, 1);
        grid.add(txtSoLuong, 1, 1);
        grid.add(new Label("Đơn vị:"), 0, 2);
        grid.add(txtDonVi, 1, 2);
        grid.add(new Label("Mô tả:"), 0, 6);
        grid.add(txtMoTa, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != nutLuuType) {
                return null;
            }

            if (txtTen.getText().trim().isEmpty()
                    || txtSoLuong.getText().trim().isEmpty()
                    || txtDonVi.getText().trim().isEmpty()) {
                hienThiThongBao(
                        Alert.AlertType.ERROR,
                        "Lỗi nhập liệu",
                        "Vui lòng nhập đầy đủ Tên thuốc, Số lượng nhập, Đơn vị và Giá nhập.");
                return null;
            }

            try {
                int soLuongNhap = Integer.parseInt(txtSoLuong.getText().trim());

                if (soLuongNhap <= 0) {
                    hienThiThongBao(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số lượng nhập phải lớn hơn 0.");
                    return null;
                }

                return taoChiTietNhap(
                        chiTietHienTai,
                        txtTen.getText().trim(),
                        soLuongNhap,
                        txtDonVi.getText().trim(),
                        txtMoTa.getText().trim());
            } catch (NumberFormatException ex) {
                hienThiThongBao(
                        Alert.AlertType.ERROR,
                        "Lỗi định dạng",
                        "Vui lòng nhập đúng định dạng số cho Số lượng, Giá nhập và Giá bán sỉ.");
                return null;
            }
        });

        return dialog.showAndWait();
    }

    private void doDuLieuCuLenDialog(
            ChiTietHoaDon chiTietHienTai,
            TextField txtTen,
            TextField txtSoLuong,
            TextField txtDonVi,
            TextArea txtMoTa) {
        if (chiTietHienTai == null) {
            return;
        }

        Thuoc thuoc = chiTietHienTai.getThuoc();
        if (thuoc != null) {
            txtTen.setText(layChuoiAnToan(thuoc.getTenThuoc()));
            txtDonVi.setText(layChuoiAnToan(thuoc.getDonVi()));
            txtMoTa.setText(layChuoiAnToan(thuoc.getMoTa()));
        }

        txtSoLuong.setText(String.valueOf(chiTietHienTai.getSoLuong()));
    }

    private ChiTietHoaDon taoChiTietNhap(
            ChiTietHoaDon chiTietHienTai,
            String tenThuoc,
            int soLuongNhap,
            String donVi,
            String moTa) {
        Thuoc thuoc = new Thuoc();

        if (chiTietHienTai != null && chiTietHienTai.getThuoc() != null) {
            thuoc.setId(chiTietHienTai.getThuoc().getId());
        }

        thuoc.setTenThuoc(tenThuoc);
        thuoc.setDonVi(donVi);
        thuoc.setGiaNhap(BigDecimal.ZERO); // Giá nhập sẽ được cập nhật sau khi người dùng nhập
        thuoc.setGiaBanSi(BigDecimal.ZERO); // Giá bán sỉ sẽ được cập
        thuoc.setSoLuongTon(0);
        thuoc.setHanSuDung(null);
        thuoc.setMoTa(moTa);

        ChiTietHoaDon chiTiet = new ChiTietHoaDon();
        chiTiet.setThuoc(thuoc);
        chiTiet.setSoLuong(soLuongNhap);
        chiTiet.setDonVi(donVi);
        chiTiet.setDonGia(BigDecimal.ZERO);
        chiTiet.setThanhTien(BigDecimal.ZERO); // Thành tiền sẽ được tính sau khi người dùng nhập giá nhập

        return chiTiet;
    }

    private Tab layTabDangChon() {
        return tabPane == null ? null : tabPane.getSelectionModel().getSelectedItem();
    }

    private ChiTietToaNhapController layControllerToaDangChon() {
        Tab tabDangChon = layTabDangChon();
        return tabDangChon == null ? null : toaControllers.get(tabDangChon);
    }

    private void dongTab(Tab tab) {
        toaControllers.remove(tab);
        nhaCungCapCuaToa.remove(tab);
        tabPane.getTabs().remove(tab);
    }

    private String layGiaTriMacDinhNeuRong(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String layChuoiAnToan(String value) {
        return value == null ? "" : value;
    }

    private void hienThiThongBao(Alert.AlertType loaiThongBao, String tieuDe, String noiDung) {
        Alert alert = new Alert(loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
