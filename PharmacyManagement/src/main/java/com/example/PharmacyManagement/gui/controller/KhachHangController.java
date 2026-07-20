package com.example.PharmacyManagement.gui.controller;

//Java imports
import java.util.List;
import java.util.Optional;
//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
//JavaFX imports
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
//Models and services imports
import com.example.PharmacyManagement.model.KhachHang;
import com.example.PharmacyManagement.service.KhachHangService;
//Component imports

//Utils imports
import com.example.PharmacyManagement.gui.util.AlertUtils;
@Controller
public class KhachHangController {

    @FXML
    private TableView<KhachHang> tableKhachHang;
    @FXML
    private TableColumn<KhachHang, Long> colId;
    @FXML
    private TableColumn<KhachHang, String> colTen;
    @FXML
    private TableColumn<KhachHang, String> colSdt;

    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblTongKhachHang;

    @FXML
    private Label lblCoSoDienThoai;

    @FXML
    private Label lblKhachMoi;

    @FXML
    private Label lblKetQua;

    @Autowired
    private KhachHangService khachHangService;

    @FXML
    public void initialize() {
        // Ánh xạ dữ liệu từ Model KhachHang vào các cột hiển thị trên TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang"));
        colSdt.setCellValueFactory(new PropertyValueFactory<>("sdt"));

        // Tải danh sách khách hàng lên bảng khi màn hình được mở
        refreshTable();
        // Tìm kiếm tự động khi người dùng nhập vào ô tìm kiếm
        cauHinhTimKiemTuDong();
    }

    private void refreshTable() {
        List<KhachHang> danhSach = khachHangService.layTatCaKhachHang();

        tableKhachHang.setItems(FXCollections.observableArrayList(danhSach));

        capNhatThongTinTongQuan(danhSach);
    }

    @FXML
    public void xuLyTimKiem() {
        String keyword = txtSearch.getText();

        List<KhachHang> ketQua = khachHangService.timKiemKhachHang(keyword);

        tableKhachHang.setItems(FXCollections.observableArrayList(ketQua));

        capNhatThongTinTongQuan(ketQua);
    }

    @FXML
    public void xuLyLamMoi() {
        txtSearch.clear();
        refreshTable();
    }

    /**
     * CHỨC NĂNG: THÊM KHÁCH HÀNG MỚI
     */
    @FXML
    public void xuLyThem() {
        // Gọi hàm hiển thị Dialog nhập liệu trống
        Optional<KhachHang> ketQua = hienThiDialogNhapLieu(null);

        // Nếu người dùng nhấn "Lưu" và nhập đầy đủ thông tin
        ketQua.ifPresent(khachHangMoi -> {
            try {
                if (!kiemTraNhapLieu(khachHangMoi)) {
                    return; // Nếu dữ liệu không hợp lệ, dừng lại
                }
                khachHangService.themKhachHang(khachHangMoi);
                AlertUtils.hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm khách hàng mới thành công!", null);
                refreshTable();
            } catch (Exception e) {
                AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể thêm khách hàng: " + e.getMessage(), null);
            }
        });
    }

    /**
     * CHỨC NĂNG: SỬA THÔNG TIN KHÁCH HÀNG ĐÃ CHỌN
     */
    @FXML
    public void xuLySua() {
        // Lấy đối tượng khách hàng đang được chọn trên TableView
        KhachHang khachHangDuocChon = tableKhachHang.getSelectionModel().getSelectedItem();

        if (khachHangDuocChon == null) {
            AlertUtils.hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một khách hàng trong bảng để sửa!", null);
            return;
        }

        // Gọi hàm hiển thị Dialog nhập liệu và truyền thông tin hiện tại vào
        Optional<KhachHang> ketQua = hienThiDialogNhapLieu(khachHangDuocChon);

        ketQua.ifPresent(khachHangCapNhat -> {
            try {
                // Giữ nguyên ID cũ để thực hiện cập nhật (Update) dưới Database
                khachHangCapNhat.setId(khachHangDuocChon.getId());

                if (!kiemTraNhapLieu(khachHangCapNhat)) {
                    return; // Nếu dữ liệu không hợp lệ, dừng lại
                }

                // Giả định hàm lưu của Service tự động hiểu Update khi thực thể có ID sẵn
                khachHangService.themKhachHang(khachHangCapNhat);

                AlertUtils.hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin thành công!", null);
                refreshTable();
            } catch (Exception e) {
                AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể cập nhật: " + e.getMessage(), null);
            }
        });
    }

    /**
     * CHỨC NĂNG: XÓA KHÁCH HÀNG ĐÃ CHỌN
     */
    @FXML
    public void xuLyXoa() {
        KhachHang khachHangDuocChon = tableKhachHang.getSelectionModel().getSelectedItem();

        if (khachHangDuocChon == null) {
            AlertUtils.hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một khách hàng trong bảng để xóa!", null);
            return;
        }

        // Tạo hộp thoại xác nhận chắc chắn muốn xóa trước khi gọi DB
        Alert alertXacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        alertXacNhan.setTitle("Xác nhận xóa");
        alertXacNhan.setHeaderText("Bạn có chắc chắn muốn xóa khách hàng này không?");
        alertXacNhan.setContentText(
                "Họ tên: " + khachHangDuocChon.getTenKhachHang() + "\nSDT: " + khachHangDuocChon.getSdt());

        Optional<ButtonType> ketQuaBamNut = alertXacNhan.showAndWait();
        if (ketQuaBamNut.isPresent() && ketQuaBamNut.get() == ButtonType.OK) {
            try {
                // Bạn cần kiểm tra xem KhachHangService của mình tên hàm xóa là gì (Ví dụ:
                // xoaKhachHang hoặc deleteById)
                khachHangService.xoaKhachHang(khachHangDuocChon.getId());

                AlertUtils.hienThiThongBao(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa khách hàng thành công!", null);
                refreshTable();
            } catch (Exception e) {
                AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi hệ thống",
                        "Không thể xóa khách hàng! (Lưu ý: Có thể khách hàng đã có lịch sử hóa đơn bán hàng, không được phép xóa)", null);
            }
        }
    }

    /**
     * HÀM TIỆN ÍCH: Tạo hộp thoại nhập thông tin Khách Hàng (Dùng chung cho cả Thêm
     * và Sửa)
     */
    private Optional<KhachHang> hienThiDialogNhapLieu(KhachHang khachHangHienTai) {
        Dialog<KhachHang> dialog = new Dialog<>();
        dialog.setTitle(khachHangHienTai == null ? "Thêm Khách Hàng Mới" : "Sửa Thông Tin Khách Hàng");
        dialog.setHeaderText("Vui lòng điền thông tin chi tiết:");

        // Tạo nút Lưu và Hủy
        ButtonType nutLuuType = new ButtonType("Lưu thông tin", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(nutLuuType, ButtonType.CANCEL);

        // Thiết kế Form Layout nhập liệu bằng GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtTen = new TextField();
        txtTen.setPromptText("Nhập họ và tên...");
        TextField txtSdt = new TextField();
        txtSdt.setPromptText("Nhập số điện thoại...");

        // Nếu là tác vụ SỬA, đổ thông tin cũ lên các ô text field
        if (khachHangHienTai != null) {
            txtTen.setText(khachHangHienTai.getTenKhachHang());
            txtSdt.setText(khachHangHienTai.getSdt());
        }

        grid.add(new Label("Họ tên khách hàng:"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Số điện thoại:"), 0, 1);
        grid.add(txtSdt, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Logic kiểm tra dữ liệu trước khi cho phép lưu kết quả
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == nutLuuType) {
                String ten = txtTen.getText().trim();
                String sdt = txtSdt.getText().trim();

                // Validation cơ bản chống bỏ trống dữ liệu
                if (ten.isEmpty()) {
                    AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Họ tên không được để trống!", null);
                    return null;
                }

                KhachHang kh = new KhachHang();
                kh.setTenKhachHang(ten);
                kh.setSdt(sdt);
                return kh;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * HÀM TIỆN ÍCH: Hiển thị nhanh một popup thông báo lên màn hình
     */

    private void capNhatThongTinTongQuan(List<KhachHang> danhSachKhachHang) {
        int tongKhachHang = danhSachKhachHang == null ? 0 : danhSachKhachHang.size();

        long soKhachCoSdt = danhSachKhachHang == null ? 0
                : danhSachKhachHang.stream()
                        .filter(kh -> kh.getSdt() != null && !kh.getSdt().trim().isEmpty())
                        .count();

        if (lblTongKhachHang != null) {
            lblTongKhachHang.setText(String.valueOf(tongKhachHang));
        }

        if (lblCoSoDienThoai != null) {
            lblCoSoDienThoai.setText(String.valueOf(soKhachCoSdt));
        }

        if (lblKetQua != null) {
            lblKetQua.setText(tongKhachHang + " kết quả");
        }
    }

    private void cauHinhTimKiemTuDong() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                refreshTable();
                return;
            }

            List<KhachHang> ketQua = khachHangService.timKiemKhachHang(newValue.trim());
            tableKhachHang.setItems(FXCollections.observableArrayList(ketQua));
        });
    }

    private boolean kiemTraNhapLieu(KhachHang khachHangMoi) {
        // Kiểm tra dữ liệu nhập liệu trước khi lưu
        // Ví dụ: kiểm tra tên khách hàng không được để trống, số điện thoại hợp lệ,
        // v.v.
        if (khachHangMoi.getTenKhachHang() == null || khachHangMoi.getTenKhachHang().trim().isEmpty()) {
            AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Họ tên khách hàng không được để trống!", null);
            return false;
        } else if (khachHangMoi.getSdt() != null && !khachHangMoi.getSdt().matches("\\d+")) {
            AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số điện thoại phải là số!", null);
            return false;
        }

        return true;
    }
}