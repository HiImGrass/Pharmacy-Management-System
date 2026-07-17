package com.example.PharmacyManagement.gui.controller;

import com.example.PharmacyManagement.dto.ChiTietPhieuNhapRequestDTO;
import com.example.PharmacyManagement.dto.PhieuNhapRequestDTO;
import com.example.PharmacyManagement.model.ChiTietHoaDon;
import com.example.PharmacyManagement.model.PhieuNhap;
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.repository.ThuocRepository;
import com.example.PharmacyManagement.service.PhieuNhapService;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Consumer;

@Controller
@Scope("prototype")
public class ChiTietToaNhapController {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @FXML
    private TableView<ChiTietHoaDon> tableChiTiet;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colTenThuoc;

    @FXML
    private TableColumn<ChiTietHoaDon, Integer> colSoLuong;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colDonVi;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colDonGia;

    @FXML
    private TableColumn<ChiTietHoaDon, String> colMoTa;

    @FXML
    private TableColumn<ChiTietHoaDon, Void> colThaoTac;

    @Autowired
    private PhieuNhapService phieuNhapService;

    @Autowired
    private ThuocRepository thuocRepository;

    private final ObservableList<ChiTietHoaDon> danhSachGoc = FXCollections.observableArrayList();
    private final NumberFormat tienVietNamFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private Consumer<ChiTietHoaDon> yeuCauSuaChiTiet;

    @FXML
    public void initialize() {
        cauHinhBangChiTiet();
        cauHinhCotTenThuoc();
        cauHinhCotSoLuong();
        cauHinhCotDonVi();
        cauHinhCotDonGia();
        cauHinhCotMoTa();
        cauHinhCotThaoTac();
    }

    /**
     * Cho màn hình cha xử lý nút Sửa bằng hộp thoại đang dùng sẵn.
     */
    public void datXuLySuaChiTiet(Consumer<ChiTietHoaDon> callback) {
        this.yeuCauSuaChiTiet = callback;
    }

    public List<ChiTietHoaDon> layDanhSachNhapDeIn() {
        return danhSachGoc.stream()
                .filter(this::laChiTietCoTheLuu)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Thêm một dòng nhập trống trực tiếp vào bảng.
     * Người dùng có thể nhập ngay tên thuốc, số lượng và đơn vị bằng cách sửa ô.
     */
    @FXML
    public void xuLyThemDong() {
        ChiTietHoaDon dongMoi = taoDongNhapTrong();
        danhSachGoc.add(dongMoi);

        int rowIndex = danhSachGoc.size() - 1;
        tableChiTiet.getSelectionModel().clearAndSelect(rowIndex);
        tableChiTiet.scrollTo(rowIndex);

        // Chờ TableView cập nhật xong rồi đưa con trỏ vào ô tên thuốc.
        Platform.runLater(() -> tableChiTiet.edit(rowIndex, colTenThuoc));
    }

    private void cauHinhBangChiTiet() {
        tableChiTiet.setItems(danhSachGoc);
        tableChiTiet.setEditable(true);
        tableChiTiet.getSelectionModel().setCellSelectionEnabled(true);
        tableChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableChiTiet.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ChiTietHoaDon item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(empty || item == null ? "" : "");
            }
        });
    }

    private void cauHinhCotTenThuoc() {
        colTenThuoc.setCellValueFactory(cellData -> {
            Thuoc thuoc = cellData.getValue().getThuoc();
            return new SimpleStringProperty(thuoc == null ? "" : layChuoiAnToan(thuoc.getTenThuoc()));
        });

        colTenThuoc.setCellFactory(TextFieldTableCell.forTableColumn());
        colTenThuoc.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            damBaoCoThuoc(chiTiet).setTenThuoc(event.getNewValue());
            lamMoiBang();
        });
    }

    private void cauHinhCotSoLuong() {
        colSoLuong.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getSoLuong()).asObject());

        colSoLuong.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colSoLuong.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
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

            lamMoiBang();
        });
    }

    private void cauHinhCotDonVi() {
        colDonVi.setCellValueFactory(
                cellData -> new SimpleStringProperty(layChuoiAnToan(cellData.getValue().getDonVi())));

        colDonVi.setCellFactory(TextFieldTableCell.forTableColumn());
        colDonVi.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            String donViMoi = event.getNewValue() == null ? "" : event.getNewValue().trim();
            chiTiet.setDonVi(donViMoi);
            damBaoCoThuoc(chiTiet).setDonVi(donViMoi);
            lamMoiBang();
        });
    }

    private void cauHinhCotDonGia() {
        colDonGia.setCellValueFactory(cellData -> {
            BigDecimal donGia = cellData.getValue().getDonGia();
            return new SimpleStringProperty(donGia == null ? "" : dinhDangTien(donGia));
        });

        colDonGia.setCellFactory(TextFieldTableCell.forTableColumn());
        colDonGia.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            String giaNhapStr = event.getNewValue();
            BigDecimal giaNhapMoi;
            try {
                giaNhapMoi = new BigDecimal(giaNhapStr.replaceAll("[^\\d.]", ""));
                if (giaNhapMoi.compareTo(ZERO) < 0) {
                    throw new NumberFormatException("Giá nhập không được âm.");
                }
            } catch (NumberFormatException e) {
                hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Giá nhập không hợp lệ: " + giaNhapStr);
                lamMoiBang();
                return;
            }

            chiTiet.setDonGia(giaNhapMoi);
            damBaoCoThuoc(chiTiet).setGiaNhap(giaNhapMoi);
            capNhatThanhTien(chiTiet);
            lamMoiBang();
        });
    }

    private void cauHinhCotMoTa() {
        colMoTa.setCellValueFactory(cellData -> {
            Thuoc thuoc = cellData.getValue().getThuoc();
            return new SimpleStringProperty(thuoc == null ? "" : layChuoiAnToan(thuoc.getMoTa()));
        });

        colMoTa.setCellFactory(TextFieldTableCell.forTableColumn());
        colMoTa.setOnEditCommit(event -> {
            ChiTietHoaDon chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            damBaoCoThuoc(chiTiet).setMoTa(event.getNewValue());
            lamMoiBang();
        });
    }

    private void cauHinhCotThaoTac() {
        colThaoTac.setCellFactory(column -> new TableCell<ChiTietHoaDon, Void>() {
            private final Button btnThem = taoNutThaoTac("＋", "#16a34a", "Thêm dòng thuốc");
            private final Button btnSua = taoNutThaoTac("✎", "#f97316", "Sửa thuốc");
            private final Button btnXoa = taoNutThaoTac("×", "#dc2626", "Xóa thuốc");
            private final HBox hopThaoTac = new HBox(4, btnThem, btnSua, btnXoa);

            {
                hopThaoTac.setAlignment(Pos.CENTER);

                btnThem.setOnAction(event -> {
                    ChiTietHoaDon dong = layDongCuaO();
                    themDongSauDong(dong);
                });

                btnSua.setOnAction(event -> {
                    ChiTietHoaDon dong = layDongCuaO();
                    if (dong != null) {
                        if (yeuCauSuaChiTiet != null) {
                            yeuCauSuaChiTiet.accept(dong);
                        } else {
                            batDauSuaDongInline(dong);
                        }
                    }
                });

                btnXoa.setOnAction(event -> xoaDong(layDongCuaO()));
            }

            private ChiTietHoaDon layDongCuaO() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hopThaoTac);
            }
        });
    }

    private Button taoNutThaoTac(String kyHieu, String mauNen, String moTa) {
        Button button = new Button(kyHieu);
        button.setMnemonicParsing(false);
        button.setPrefSize(28, 28);
        button.setMinSize(28, 28);
        button.setMaxSize(28, 28);
        button.setTooltip(new javafx.scene.control.Tooltip(moTa));
        button.setStyle(
                "-fx-background-color: " + mauNen + ";"
                        + "-fx-background-radius: 7;"
                        + "-fx-cursor: hand;"
                        + "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 0;"
                        + "-fx-text-fill: white;");
        return button;
    }

    private void themDongSauDong(ChiTietHoaDon dongHienTai) {
        if (dongHienTai == null) {
            xuLyThemDong();
            return;
        }

        int index = danhSachGoc.indexOf(dongHienTai);
        if (index < 0) {
            xuLyThemDong();
            return;
        }

        ChiTietHoaDon dongMoi = taoDongNhapTrong();
        danhSachGoc.add(index + 1, dongMoi);
        chonVaMoNhapDong(danhSachGoc.indexOf(dongMoi));
    }

    private void batDauSuaDongInline(ChiTietHoaDon dong) {
        int index = danhSachGoc.indexOf(dong);
        if (index >= 0) {
            chonVaMoNhapDong(index);
        }
    }

    private void chonVaMoNhapDong(int rowIndex) {
        tableChiTiet.getSelectionModel().clearAndSelect(rowIndex);
        tableChiTiet.scrollTo(rowIndex);
        Platform.runLater(() -> tableChiTiet.edit(rowIndex, colTenThuoc));
    }

    private void xoaDong(ChiTietHoaDon dong) {
        if (dong == null) {
            return;
        }

        danhSachGoc.remove(dong);
        lamMoiBang();
    }

    public void themChiTietNhap(ChiTietHoaDon chiTietMoi) {
        if (chiTietMoi == null || chiTietMoi.getThuoc() == null) {
            return;
        }

        ChiTietHoaDon chiTietTonTai = timChiTietTrung(chiTietMoi);
        if (chiTietTonTai != null) {
            chiTietTonTai.setSoLuong(chiTietTonTai.getSoLuong() + chiTietMoi.getSoLuong());
            chiTietTonTai.setDonGia(layDonGiaAnToan(chiTietMoi));
            capNhatThongTinThuoc(chiTietTonTai, chiTietMoi.getThuoc());
            capNhatThanhTien(chiTietTonTai);
            lamMoiBang();
            return;
        }

        capNhatThanhTien(chiTietMoi);
        danhSachGoc.add(chiTietMoi);
        lamMoiBang();
    }

    public void capNhatChiTietNhap(ChiTietHoaDon chiTietCu, ChiTietHoaDon chiTietMoi) {
        if (chiTietCu == null || chiTietMoi == null) {
            return;
        }

        chiTietCu.setThuoc(chiTietMoi.getThuoc());
        chiTietCu.setSoLuong(chiTietMoi.getSoLuong());
        chiTietCu.setDonVi(chiTietMoi.getDonVi());
        chiTietCu.setDonGia(layDonGiaAnToan(chiTietMoi));
        capNhatThanhTien(chiTietCu);
        lamMoiBang();
    }

    public ChiTietHoaDon layDongDuocChon() {
        return tableChiTiet == null ? null : tableChiTiet.getSelectionModel().getSelectedItem();
    }

    public void xoaDongDuocChon() {
        ChiTietHoaDon hangDuocChon = layDongDuocChon();
        if (hangDuocChon == null) {
            hienThiThongBao(Alert.AlertType.WARNING, "Chưa chọn thuốc", "Vui lòng chọn dòng thuốc cần xóa.");
            return;
        }

        danhSachGoc.remove(hangDuocChon);
        lamMoiBang();
    }

    public PhieuNhap xuLyNhapHang(String nhaCungCap, String ghiChu) {
        return luuNhapHangNoiBo(nhaCungCap, ghiChu, true);
    }

    public PhieuNhap luuPhieuNhap(String nhaCungCap, String ghiChu) {
        return luuNhapHangNoiBo(nhaCungCap, ghiChu, false);
    }

    public PhieuNhap luuPhieuNhapVaTraVe(String nhaCungCap, String ghiChu) {
        return luuNhapHangNoiBo(
                nhaCungCap,
                ghiChu,
                false);
    }

    private PhieuNhap luuNhapHangNoiBo(String nhaCungCap, String ghiChu, boolean hienThiThongBaoThanhCong) {
        if (danhSachGoc.stream().noneMatch(this::laChiTietCoTheLuu)) {
            hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", "Không có thuốc nào trong phiếu nhập.");
            return null;
        }

        if (!kiemTraDuLieuHopLe()) {
            return null;
        }

        try {
            PhieuNhapRequestDTO request = taoPhieuNhapRequest(nhaCungCap, ghiChu);

            PhieuNhap phieuNhapDaLuu = phieuNhapService.nhapHangVaoKho(request);
            String tongTien = dinhDangTien(tinhTongTien());
            danhSachGoc.clear();
            lamMoiBang();

            if (hienThiThongBaoThanhCong) {
                hienThiThongBao(
                        Alert.AlertType.INFORMATION,
                        "Thành công",
                        "Đã lưu phiếu nhập kho thành công. Tổng tiền nhập: "
                                + tongTien + " VND");
            }

            return phieuNhapDaLuu;

        } catch (Exception e) {
            e.printStackTrace();
            hienThiThongBao(
                    Alert.AlertType.ERROR,
                    "Lỗi hệ thống",
                    "Có lỗi xảy ra khi lưu phiếu nhập: " + e.getMessage());
            return null;
        }
    }

    private PhieuNhapRequestDTO taoPhieuNhapRequest(String nhaCungCap, String ghiChu) {
        List<ChiTietPhieuNhapRequestDTO> danhSachThuocNhap = danhSachGoc.stream()
                .filter(this::laChiTietCoTheLuu)
                .map(this::taoChiTietPhieuNhapRequest)
                .collect(Collectors.toList());

        PhieuNhapRequestDTO request = new PhieuNhapRequestDTO();
        request.setNhaCungCap(nhaCungCap);
        request.setGhiChu(ghiChu);
        request.setDanhSachThuocNhap(danhSachThuocNhap);
        return request;
    }

    private ChiTietPhieuNhapRequestDTO taoChiTietPhieuNhapRequest(ChiTietHoaDon chiTiet) {
        Thuoc thuocDaCoId = damBaoThuocDaCoTrongDatabase(chiTiet);
        chiTiet.setThuoc(thuocDaCoId);

        ChiTietPhieuNhapRequestDTO item = new ChiTietPhieuNhapRequestDTO();
        item.setThuocId(thuocDaCoId.getId());
        item.setSoLuong(chiTiet.getSoLuong());
        item.setDonGia(layDonGiaAnToan(chiTiet));
        return item;
    }

    private Thuoc damBaoThuocDaCoTrongDatabase(ChiTietHoaDon chiTiet) {
        Thuoc thuoc = chiTiet.getThuoc();
        if (thuoc == null) {
            throw new IllegalArgumentException("Dòng nhập thiếu thông tin thuốc.");
        }

        if (thuoc.getId() != null) {
            return thuoc;
        }

        Optional<Thuoc> thuocDaTonTai = thuocRepository
                .findFirstByTenThuocAndDonViAndGiaNhapAndMoTaIgnoreCase(thuoc.getTenThuoc(), thuoc.getDonVi(), thuoc.getGiaNhap(), thuoc.getMoTa());

        if (thuocDaTonTai.isPresent()) {
            Thuoc thuocHienCo = thuocDaTonTai.get();
            chiTiet.setThuoc(thuocHienCo);
            return thuocHienCo;
        }

        Thuoc thuocMoi = new Thuoc();
        thuocMoi.setTenThuoc(thuoc.getTenThuoc());
        thuocMoi.setDonVi(thuoc.getDonVi());
        thuocMoi.setGiaNhap(thuoc.getGiaNhap() == null ? ZERO : thuoc.getGiaNhap());
        thuocMoi.setGiaBanSi(thuoc.getGiaBanSi() == null ? ZERO : thuoc.getGiaBanSi());
        thuocMoi.setSoLuongTon(0);
        thuocMoi.setHanSuDung(thuoc.getHanSuDung());
        thuocMoi.setMoTa(thuoc.getMoTa());

        return thuocRepository.save(thuocMoi);
    }

    private boolean kiemTraDuLieuHopLe() {
        for (ChiTietHoaDon chiTiet : danhSachGoc) {
            // Dòng được tạo bằng nút "+" nhưng chưa nhập gì được bỏ qua.
            if (laDongNhapTrong(chiTiet)) {
                continue;
            }

            Thuoc thuoc = chiTiet.getThuoc();
            String tenThuoc = thuoc == null ? "" : layChuoiAnToan(thuoc.getTenThuoc());
            String donVi = thuoc == null ? "" : layChuoiAnToan(thuoc.getDonVi());
            String moTa = thuoc == null ? "" : layChuoiAnToan(thuoc.getMoTa());

            if (thuoc == null || tenThuoc.isBlank()) {
                hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Có dòng chưa nhập tên thuốc.");
                return false;
            }

            if (donVi.isBlank()) {
                hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu",
                        "Vui lòng nhập đơn vị cho thuốc '" + tenThuoc.trim() + "'.");
                return false;
            }

            if (thuocRepository.existsByTenThuocAndDonViIgnoreCaseAndGiaNhapAndMoTaIgnoreCase(tenThuoc.trim(), donVi.trim(), thuoc.getGiaNhap(), moTa.trim())) {
                hienThiThongBao(Alert.AlertType.INFORMATION, "Thuốc đã tồn tại",
                        "Thuốc '" + tenThuoc.trim()
                                + "' đã tồn tại. Hệ thống sẽ cộng thêm vào số lượng tồn hiện có khi lưu.");
            }

            if (chiTiet.getSoLuong() <= 0) {
                hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu",
                        "Số lượng nhập của thuốc '" + tenThuoc.trim() + "' phải lớn hơn 0.");
                return false;
            }

        }

        return true;
    }

    private boolean laChiTietCoTheLuu(ChiTietHoaDon chiTiet) {
        return chiTiet != null
                && chiTiet.getThuoc() != null
                && chiTiet.getSoLuong() > 0
                && chiTiet.getThuoc().getTenThuoc() != null
                && !chiTiet.getThuoc().getTenThuoc().isBlank()
                && chiTiet.getThuoc().getDonVi() != null
                && !chiTiet.getThuoc().getDonVi().isBlank();
    }

    private boolean laDongNhapTrong(ChiTietHoaDon chiTiet) {
        if (chiTiet == null) {
            return true;
        }

        Thuoc thuoc = chiTiet.getThuoc();
        String tenThuoc = thuoc == null ? "" : layChuoiAnToan(thuoc.getTenThuoc());
        String donVi = thuoc == null ? "" : layChuoiAnToan(thuoc.getDonVi());
        String moTa = thuoc == null ? "" : layChuoiAnToan(thuoc.getMoTa());

        return tenThuoc.isBlank()
                && donVi.isBlank()
                && moTa.isBlank()
                && chiTiet.getSoLuong() <= 0;
    }

    private ChiTietHoaDon taoDongNhapTrong() {
        Thuoc thuoc = new Thuoc();
        thuoc.setGiaNhap(ZERO);
        thuoc.setGiaBanSi(ZERO);
        thuoc.setSoLuongTon(0);

        ChiTietHoaDon chiTiet = new ChiTietHoaDon();
        chiTiet.setThuoc(thuoc);
        chiTiet.setSoLuong(0);
        chiTiet.setDonVi("");
        chiTiet.setDonGia(ZERO);
        chiTiet.setThanhTien(ZERO);
        return chiTiet;
    }

    private ChiTietHoaDon timChiTietTrung(ChiTietHoaDon chiTietMoi) {
        return danhSachGoc.stream()
                .filter(chiTietHienTai -> laCungThuoc(chiTietHienTai, chiTietMoi))
                .findFirst()
                .orElse(null);
    }

    private boolean laCungThuoc(ChiTietHoaDon chiTietA, ChiTietHoaDon chiTietB) {
        if (chiTietA == null || chiTietB == null || chiTietA.getThuoc() == null || chiTietB.getThuoc() == null) {
            return false;
        }

        Long idA = chiTietA.getThuoc().getId();
        Long idB = chiTietB.getThuoc().getId();
        if (idA != null && idB != null) {
            return Objects.equals(idA, idB);
        }

        return Objects.equals(chuanHoaChuoi(chiTietA.getThuoc().getTenThuoc()),
                chuanHoaChuoi(chiTietB.getThuoc().getTenThuoc()))
                && Objects.equals(chuanHoaChuoi(chiTietA.getDonVi()), chuanHoaChuoi(chiTietB.getDonVi()))
                && layDonGiaAnToan(chiTietA).compareTo(layDonGiaAnToan(chiTietB)) == 0;
    }

    private void capNhatThongTinThuoc(ChiTietHoaDon chiTiet, Thuoc thuocMoi) {
        if (chiTiet == null || thuocMoi == null) {
            return;
        }

        Thuoc thuocHienTai = damBaoCoThuoc(chiTiet);
        thuocHienTai.setTenThuoc(thuocMoi.getTenThuoc());
        thuocHienTai.setDonVi(thuocMoi.getDonVi());
        thuocHienTai.setGiaNhap(thuocMoi.getGiaNhap());
        thuocHienTai.setGiaBanSi(thuocMoi.getGiaBanSi());
        thuocHienTai.setHanSuDung(thuocMoi.getHanSuDung());
        thuocHienTai.setMoTa(thuocMoi.getMoTa());

        chiTiet.setDonVi(thuocMoi.getDonVi());
    }

    private Thuoc damBaoCoThuoc(ChiTietHoaDon chiTiet) {
        if (chiTiet.getThuoc() == null) {
            chiTiet.setThuoc(new Thuoc());
        }
        return chiTiet.getThuoc();
    }

    private ChiTietHoaDon layDongTheoIndex(int rowIndex) {
        if (tableChiTiet == null || rowIndex < 0 || rowIndex >= tableChiTiet.getItems().size()) {
            return null;
        }
        return tableChiTiet.getItems().get(rowIndex);
    }

    private void capNhatThanhTien(ChiTietHoaDon chiTiet) {
        if (chiTiet == null) {
            return;
        }
        chiTiet.setThanhTien(layDonGiaAnToan(chiTiet).multiply(BigDecimal.valueOf(Math.max(chiTiet.getSoLuong(), 0))));
    }

    private BigDecimal tinhTongTien() {
        return danhSachGoc.stream()
                .map(ChiTietHoaDon::getThanhTien)
                .filter(thanhTien -> thanhTien != null)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal layDonGiaAnToan(ChiTietHoaDon chiTiet) {
        return chiTiet != null && chiTiet.getDonGia() != null ? chiTiet.getDonGia() : ZERO;
    }

    private void lamMoiBang() {
        if (tableChiTiet != null) {
            tableChiTiet.refresh();
        }
    }

    private String dinhDangTien(BigDecimal soTien) {
        return tienVietNamFormatter.format(soTien == null ? ZERO : soTien);
    }

    private String chuanHoaChuoi(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String layChuoiAnToan(String value) {
        return value == null ? "" : value;
    }

    public void hienThiThongBao(Alert.AlertType loaiThongBao, String tieuDe, String noiDung) {
        Alert alert = new Alert(loaiThongBao);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
