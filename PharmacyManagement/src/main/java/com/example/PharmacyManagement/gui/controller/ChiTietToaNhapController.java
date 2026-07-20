package com.example.PharmacyManagement.gui.controller;

import com.example.PharmacyManagement.gui.controller.BanHangController;
//Java imports
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Consumer;

import org.hibernate.mapping.Table;
//Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

//JavaFX imports
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

//Models, services and DTO imports
import com.example.PharmacyManagement.dto.ChiTietPhieuNhapRequestDTO;
import com.example.PharmacyManagement.dto.PhieuNhapRequestDTO;
import com.example.PharmacyManagement.model.ChiTietPhieuNhap;
import com.example.PharmacyManagement.model.PhieuNhap;
import com.example.PharmacyManagement.model.Thuoc;
import com.example.PharmacyManagement.repository.ThuocRepository;
import com.example.PharmacyManagement.service.PhieuNhapService;

//Component imports

//Utils imports
import com.example.PharmacyManagement.gui.util.MoneyFormatter;
import com.example.PharmacyManagement.gui.util.AlertUtils;
import com.example.PharmacyManagement.gui.util.DatePickerFormatter;

@Controller
@Scope("prototype")
public class ChiTietToaNhapController {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @FXML
    private TableView<ChiTietPhieuNhap> tableChiTiet;

    @FXML
    private TableColumn<ChiTietPhieuNhap, String> colTenThuoc;

    @FXML
    private TableColumn<ChiTietPhieuNhap, Integer> colSoLuong;

    @FXML
    private TableColumn<ChiTietPhieuNhap, String> colDonVi;

    @FXML
    private TableColumn<ChiTietPhieuNhap, String> colDonGia;

    @FXML
    private TableColumn<ChiTietPhieuNhap, LocalDate> colHanSuDung;

    @FXML
    private TableColumn<ChiTietPhieuNhap, String> colMoTa;

    @FXML
    private TableColumn<ChiTietPhieuNhap, Void> colThaoTac;

    @Autowired
    private PhieuNhapService phieuNhapService;

    @Autowired
    private ThuocRepository thuocRepository;

    private final ObservableList<ChiTietPhieuNhap> danhSachGoc = FXCollections.observableArrayList();
    private Consumer<ChiTietPhieuNhap> yeuCauSuaChiTiet;

    @FXML
    public void initialize() {
        cauHinhBangChiTiet();
        cauHinhCotTenThuoc();
        cauHinhCotSoLuong();
        cauHinhCotDonVi();
        cauHinhCotDonGia();
        cauHinhCotHanSuDung();
        cauHinhCotMoTa();
        cauHinhCotThaoTac();
    }

    /**
     * Cho màn hình cha xử lý nút Sửa bằng hộp thoại đang dùng sẵn.
     */
    public void datXuLySuaChiTiet(Consumer<ChiTietPhieuNhap> callback) {
        this.yeuCauSuaChiTiet = callback;
    }

    public List<ChiTietPhieuNhap> layDanhSachNhapDeIn() {
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
        ChiTietPhieuNhap dongMoi = taoDongNhapTrong();
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
            protected void updateItem(ChiTietPhieuNhap item, boolean empty) {
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

        // Custom CellFactory để bắt phím TAB
        colTenThuoc.setCellFactory(column -> new TableCell<ChiTietPhieuNhap, String>() {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                textField.setText(getItem() == null ? "" : getItem());
                setGraphic(textField);
                setText(null);

                // Focus và bôi đen text để người dùng dễ gõ đè nếu muốn
                Platform.runLater(() -> {
                    textField.requestFocus();
                    textField.selectAll();
                });
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getItem() == null ? "" : getItem());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getItem());
                    setGraphic(null);
                }
            }

            private void createTextField() {
                textField = new TextField();

                // Lắng nghe sự kiện phím
                textField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        // 1. Lưu giá trị Tên thuốc vừa nhập
                        commitEdit(textField.getText());

                        int currentRow = getIndex();

                        // 2. Chuyển ngay tiêu điểm sang chỉnh sửa ô Số lượng (colSoLuong)
                        Platform.runLater(() -> {
                            tableChiTiet.getSelectionModel().select(currentRow, colSoLuong);
                            tableChiTiet.edit(currentRow, colSoLuong);
                        });

                        event.consume(); // Chặn phím Tab mặc định của JavaFX
                    } else if (event.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                        event.consume();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                        event.consume();
                    }
                });
            }
        });

        colTenThuoc.setOnEditCommit(event -> {
            ChiTietPhieuNhap chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            String tenThuocMoi = event.getNewValue() == null ? "" : event.getNewValue().trim();
            damBaoCoThuoc(chiTiet).setTenThuoc(tenThuocMoi);
            lamMoiBang();
        });
    }

    private void cauHinhCotSoLuong() {
        colSoLuong.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getSoLuong()).asObject());

        colSoLuong.setCellFactory(column -> new TableCell<ChiTietPhieuNhap, Integer>() {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                textField.setText(getItem() == null ? "0" : getItem().toString());
                setGraphic(textField);
                setText(null);

                // Focus và bôi đen số lượng để người dùng gõ đè nhanh
                Platform.runLater(() -> {
                    textField.requestFocus();
                    textField.selectAll();
                });
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem() == null ? "0" : getItem().toString());
                setGraphic(null);
            }

            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getItem() == null ? "0" : getItem().toString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getItem() == null ? "0" : getItem().toString());
                    setGraphic(null);
                }
            }

            private void createTextField() {
                textField = new TextField();

                // CHẶN NHẬP CHỮ: Chỉ cho phép nhập các chữ số (0-9)
                textField.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*")) {
                        textField.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                });

                // Lắng nghe phím
                textField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        // 1. Chuyển String -> int rồi mới commit
                        commitEdit(chuyenSangSo(textField.getText()));

                        int currentRow = getIndex();

                        // 2. Chuyển sang edit ô Đơn vị (colDonVi)
                        Platform.runLater(() -> {
                            tableChiTiet.getSelectionModel().select(currentRow, colDonVi);
                            tableChiTiet.edit(currentRow, colDonVi);
                        });

                        event.consume();
                    } else if (event.getCode() == KeyCode.ENTER) {
                        commitEdit(chuyenSangSo(textField.getText()));
                        event.consume();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                        event.consume();
                    }
                });
            }

            // Hàm hỗ trợ ép kiểu an toàn, tránh crash ứng dụng khi để trống
            private int chuyenSangSo(String text) {
                if (text == null || text.trim().isEmpty()) {
                    return 0;
                }
                try {
                    return Integer.parseInt(text.trim());
                } catch (NumberFormatException e) {
                    return getItem() != null ? getItem() : 0;
                }
            }
        });

        // CẬP NHẬT MODEL: Lưu Số lượng mới vào đối tượng ChiTietPhieuNhap
        colSoLuong.setOnEditCommit(event -> {
            ChiTietPhieuNhap chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet != null) {
                chiTiet.setSoLuong(event.getNewValue());
                lamMoiBang();
            }
        });
    }

    private void cauHinhCotDonVi() {
        colDonVi.setCellValueFactory(
                cellData -> new SimpleStringProperty(layChuoiAnToan(cellData.getValue().getDonVi())));

        colDonVi.setCellFactory(column -> new TableCell<ChiTietPhieuNhap, String>() {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                textField.setText(getItem() == null ? "" : getItem());
                setGraphic(textField);
                setText(null);

                // Focus và bôi đen text để người dùng dễ gõ đè nếu muốn
                Platform.runLater(() -> {
                    textField.requestFocus();
                    textField.selectAll();
                });
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getItem() == null ? "" : getItem());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getItem());
                    setGraphic(null);
                }
            }

            private void createTextField() {
                textField = new TextField();

                // Lắng nghe sự kiện phím
                textField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        // 1. Lưu giá trị Đơn vị vừa nhập
                        commitEdit(textField.getText());

                        int currentRow = getIndex();

                        // 2. Chuyển sang edit Đơn giá (colDonGia)
                        Platform.runLater(() -> {
                            tableChiTiet.getSelectionModel().select(currentRow, colDonGia);
                            tableChiTiet.edit(currentRow, colDonGia);
                        });

                        event.consume();
                    } else if (event.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                        event.consume();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                        event.consume();
                    }
                });
            }
        });

        colDonVi.setOnEditCommit(event -> {
            ChiTietPhieuNhap chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            String donViMoi = event.getNewValue() == null ? "" : event.getNewValue().trim();
            chiTiet.setDonVi(donViMoi);
            damBaoCoThuoc(chiTiet).setDonVi(donViMoi);
            // lamMoiBang();
        });
    }

    private void cauHinhCotDonGia() {
        colDonGia.setCellValueFactory(cellData -> {
            BigDecimal donGia = cellData.getValue().getDonGia();
            return new SimpleStringProperty(donGia == null ? "" : MoneyFormatter.format(donGia));
        });
        colDonGia.setEditable(true);
        colDonGia.setCellFactory(TextFieldTableCell.forTableColumn());
        colDonGia.setOnEditCommit(event -> {
            ChiTietPhieuNhap chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
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
                AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu", null,
                        "Giá nhập không hợp lệ: " + giaNhapStr);
                lamMoiBang();
                return;
            }

            chiTiet.setDonGia(giaNhapMoi);
            damBaoCoThuoc(chiTiet).setGiaNhap(giaNhapMoi);
            lamMoiBang();
        });
    }

    private void cauHinhCotHanSuDung() {
        colHanSuDung.setCellValueFactory(cellData -> {
            ChiTietPhieuNhap chiTiet = cellData.getValue();
            return new SimpleObjectProperty<>(chiTiet != null ? chiTiet.getHanSuDung() : null);
        });

        colHanSuDung.setCellFactory(cellData -> new TableCell<ChiTietPhieuNhap, LocalDate>() {
            private final DatePicker datePicker = new DatePicker();

            @Override
            public void startEdit() {
                super.startEdit();
                datePicker.setValue(getItem());
                setGraphic(datePicker);
                setText(null);

                // Focus và mở popup lịch khi bắt đầu edit
                Platform.runLater(() -> {
                    datePicker.requestFocus();
                    datePicker.show();
                });
            }

            {
                datePicker.setEditable(true);

                DatePickerFormatter.formatDatePickerToVn(datePicker);

                datePicker.setMaxWidth(Double.MAX_VALUE);

                // Bắt sự kiện khi chọn ngày từ lịch popup (Action event)
                datePicker.setOnAction(event -> {
                    ChiTietPhieuNhap chiTiet = getTableRow() != null ? getTableRow().getItem() : null;
                    if (chiTiet != null) {
                        chiTiet.setHanSuDung(datePicker.getValue());
                        damBaoCoThuoc(chiTiet).setHanSuDung(datePicker.getValue());
                    }
                });

                datePicker.getEditor().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        // 1. Tự động parse chuỗi ngày gõ tay (nếu có) thành LocalDate
                        try {
                            String text = datePicker.getEditor().getText();
                            if (text != null && !text.trim().isEmpty()) {
                                LocalDate parsedDate = datePicker.getConverter().fromString(text);
                                datePicker.setValue(parsedDate);
                            }
                        } catch (Exception ignored) {
                        }

                        // 2. Lưu giá trị vào Model
                        ChiTietPhieuNhap chiTiet = getTableRow() != null ? getTableRow().getItem() : null;
                        if (chiTiet != null) {
                            chiTiet.setHanSuDung(datePicker.getValue());
                            damBaoCoThuoc(chiTiet).setHanSuDung(datePicker.getValue());
                        }

                        datePicker.hide();

                        int currentRow = getIndex();

                        Platform.runLater(() -> {
                            tableChiTiet.getSelectionModel().select(currentRow, colMoTa);
                            tableChiTiet.edit(currentRow, colMoTa);
                        });

                        event.consume();
                    }
                });
            }

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    datePicker.setValue(item);
                    setGraphic(datePicker);
                }
            }
        });
    }

    private void cauHinhCotMoTa() {
        colMoTa.setCellValueFactory(cellData -> {
            Thuoc thuoc = cellData.getValue().getThuoc();
            return new SimpleStringProperty(thuoc == null ? "" : layChuoiAnToan(thuoc.getMoTa()));
        });

        colMoTa.setCellFactory(column -> new TableCell<ChiTietPhieuNhap, String>() {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                textField.setText(getItem() == null ? "" : getItem());
                setGraphic(textField);
                setText(null);

                // Focus và bôi đen text để người dùng dễ gõ đè nếu muốn
                Platform.runLater(() -> {
                    textField.requestFocus();
                    textField.selectAll();
                });
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getItem() == null ? "" : getItem());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getItem());
                    setGraphic(null);
                }
            }

            private void createTextField() {
                textField = new TextField();

                // Lắng nghe sự kiện phím
                textField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.TAB) {
                        // 1. Lưu giá trị Mô tả vừa nhập
                        commitEdit(textField.getText());

                        int currentRow = getIndex();

                        // 2. Chuyển sang edit ô Tên thuốc (colTenThuoc) của dòng tiếp theo
                        Platform.runLater(() -> {
                            int nextRow = currentRow + 1;
                            if (nextRow < tableChiTiet.getItems().size()) {
                                tableChiTiet.getSelectionModel().select(nextRow, colTenThuoc);
                                tableChiTiet.edit(nextRow, colTenThuoc);
                            } else {
                                // Nếu là dòng cuối cùng, thêm một dòng mới và edit ô Tên thuốc
                                xuLyThemDong();
                            }
                        });

                        event.consume(); // Chặn phím Tab mặc định của JavaFX
                    } else if (event.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                        event.consume();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                        event.consume();
                    }
                });
            }
        });
        colMoTa.setOnEditCommit(event -> {
            ChiTietPhieuNhap chiTiet = layDongTheoIndex(event.getTablePosition().getRow());
            if (chiTiet == null) {
                return;
            }

            damBaoCoThuoc(chiTiet).setMoTa(event.getNewValue());
            lamMoiBang();
        });
    }

    private void cauHinhCotThaoTac() {
        colThaoTac.setCellFactory(column -> new TableCell<ChiTietPhieuNhap, Void>() {
            private final Button btnThem = taoNutThaoTac("＋", "#16a34a", "Thêm dòng thuốc");
            private final Button btnSua = taoNutThaoTac("✎", "#f97316", "Sửa thuốc");
            private final Button btnXoa = taoNutThaoTac("×", "#dc2626", "Xóa thuốc");
            private final HBox hopThaoTac = new HBox(4, btnThem, btnSua, btnXoa);

            {
                hopThaoTac.setAlignment(Pos.CENTER);

                btnThem.setOnAction(event -> {
                    ChiTietPhieuNhap dong = layDongCuaO();
                    themDongSauDong(dong);
                });

                btnSua.setOnAction(event -> {
                    ChiTietPhieuNhap dong = layDongCuaO();
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

            private ChiTietPhieuNhap layDongCuaO() {
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

    private void themDongSauDong(ChiTietPhieuNhap dongHienTai) {
        if (dongHienTai == null) {
            xuLyThemDong();
            return;
        }

        int index = danhSachGoc.indexOf(dongHienTai);
        if (index < 0) {
            xuLyThemDong();
            return;
        }

        ChiTietPhieuNhap dongMoi = taoDongNhapTrong();
        danhSachGoc.add(index + 1, dongMoi);
        chonVaMoNhapDong(danhSachGoc.indexOf(dongMoi));
    }

    private void batDauSuaDongInline(ChiTietPhieuNhap dong) {
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

    private void xoaDong(ChiTietPhieuNhap dong) {
        if (dong == null) {
            return;
        }

        danhSachGoc.remove(dong);
        lamMoiBang();
    }

    public void themChiTietNhap(ChiTietPhieuNhap chiTietMoi) {
        if (chiTietMoi == null || chiTietMoi.getThuoc() == null) {
            return;
        }

        ChiTietPhieuNhap chiTietTonTai = timChiTietTrung(chiTietMoi);
        if (chiTietTonTai != null) {
            chiTietTonTai.setSoLuong(chiTietTonTai.getSoLuong() + chiTietMoi.getSoLuong());
            chiTietTonTai.setDonGia(layDonGiaAnToan(chiTietMoi));
            capNhatThongTinThuoc(chiTietTonTai, chiTietMoi.getThuoc());
            lamMoiBang();
            return;
        }

        danhSachGoc.add(chiTietMoi);
        lamMoiBang();
    }

    public void capNhatChiTietNhap(ChiTietPhieuNhap chiTietCu, ChiTietPhieuNhap chiTietMoi) {
        if (chiTietCu == null || chiTietMoi == null) {
            return;
        }

        chiTietCu.setThuoc(chiTietMoi.getThuoc());
        chiTietCu.setSoLuong(chiTietMoi.getSoLuong());
        chiTietCu.setDonVi(chiTietMoi.getDonVi());
        chiTietCu.setDonGia(layDonGiaAnToan(chiTietMoi));
        lamMoiBang();
    }

    public ChiTietPhieuNhap layDongDuocChon() {
        return tableChiTiet == null ? null : tableChiTiet.getSelectionModel().getSelectedItem();
    }

    public void xoaDongDuocChon() {
        ChiTietPhieuNhap hangDuocChon = layDongDuocChon();
        if (hangDuocChon == null) {
            AlertUtils.hienThiThongBao(Alert.AlertType.WARNING, "Chưa chọn thuốc", null,
                    "Vui lòng chọn dòng thuốc cần xóa.");
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
            AlertUtils.hienThiThongBao(Alert.AlertType.WARNING, "Cảnh báo", null,
                    "Không có thuốc nào trong phiếu nhập.");
            return null;
        }

        if (!kiemTraDuLieuHopLe()) {
            return null;
        }

        try {
            PhieuNhapRequestDTO request = taoPhieuNhapRequest(nhaCungCap, ghiChu);

            PhieuNhap phieuNhapDaLuu = phieuNhapService.nhapHangVaoKho(request);
            danhSachGoc.clear();
            lamMoiBang();

            if (hienThiThongBaoThanhCong) {
                AlertUtils.hienThiThongBao(
                        Alert.AlertType.INFORMATION,
                        "Thành công",
                        null,
                        "Đã lưu phiếu nhập kho thành công !!");
            }

            return phieuNhapDaLuu;

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.hienThiThongBao(
                    Alert.AlertType.ERROR,
                    "Lỗi hệ thống",
                    null,
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

    private ChiTietPhieuNhapRequestDTO taoChiTietPhieuNhapRequest(ChiTietPhieuNhap chiTiet) {
        Thuoc thuocDaCoId = damBaoThuocDaCoTrongDatabase(chiTiet);
        chiTiet.setThuoc(thuocDaCoId);

        ChiTietPhieuNhapRequestDTO item = new ChiTietPhieuNhapRequestDTO();
        item.setThuocId(thuocDaCoId.getId());
        item.setSoLuong(chiTiet.getSoLuong());
        item.setDonGia(layDonGiaAnToan(chiTiet));
        return item;
    }

    private Thuoc damBaoThuocDaCoTrongDatabase(ChiTietPhieuNhap chiTiet) {
        Thuoc thuoc = chiTiet.getThuoc();
        if (thuoc == null) {
            throw new IllegalArgumentException("Dòng nhập thiếu thông tin thuốc.");
        }

        if (thuoc.getId() != null) {
            return thuoc;
        }

        Optional<Thuoc> thuocDaTonTai = thuocRepository
                .findFirstByTenThuocAndDonViAndGiaNhapAndMoTaIgnoreCase(thuoc.getTenThuoc(), thuoc.getDonVi(),
                        thuoc.getGiaNhap(), thuoc.getMoTa());

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
        for (ChiTietPhieuNhap chiTiet : danhSachGoc) {
            // Dòng được tạo bằng nút "+" nhưng chưa nhập gì được bỏ qua.
            if (laDongNhapTrong(chiTiet)) {
                continue;
            }

            Thuoc thuoc = chiTiet.getThuoc();
            String tenThuoc = thuoc == null ? "" : layChuoiAnToan(thuoc.getTenThuoc());
            String donVi = thuoc == null ? "" : layChuoiAnToan(thuoc.getDonVi());
            String moTa = thuoc == null ? "" : layChuoiAnToan(thuoc.getMoTa());

            if (thuoc == null || tenThuoc.isBlank()) {
                AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu", null, "Có dòng chưa nhập tên thuốc.");
                return false;
            }

            if (donVi.isBlank()) {
                AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu", null,
                        "Vui lòng nhập đơn vị cho thuốc '" + tenThuoc.trim() + "'.");
                return false;
            }

            if (thuocRepository.existsByTenThuocAndDonViIgnoreCaseAndGiaNhapAndMoTaIgnoreCase(tenThuoc.trim(),
                    donVi.trim(), thuoc.getGiaNhap(), moTa.trim())) {
                AlertUtils.hienThiThongBao(Alert.AlertType.INFORMATION, "Thuốc đã tồn tại", null,
                        "Thuốc '" + tenThuoc.trim()
                                + "' đã tồn tại. Hệ thống sẽ cộng thêm vào số lượng tồn hiện có khi lưu.");
            }

            if (chiTiet.getSoLuong() <= 0) {
                AlertUtils.hienThiThongBao(Alert.AlertType.ERROR, "Lỗi dữ liệu", null,
                        "Số lượng nhập của thuốc '" + tenThuoc.trim() + "' phải lớn hơn 0.");
                return false;
            }

        }

        return true;
    }

    private boolean laChiTietCoTheLuu(ChiTietPhieuNhap chiTiet) {
        return chiTiet != null
                && chiTiet.getThuoc() != null
                && chiTiet.getSoLuong() > 0
                && chiTiet.getThuoc().getTenThuoc() != null
                && !chiTiet.getThuoc().getTenThuoc().isBlank()
                && chiTiet.getThuoc().getDonVi() != null
                && !chiTiet.getThuoc().getDonVi().isBlank();
    }

    private boolean laDongNhapTrong(ChiTietPhieuNhap chiTiet) {
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

    private ChiTietPhieuNhap taoDongNhapTrong() {
        Thuoc thuoc = new Thuoc();
        thuoc.setGiaNhap(ZERO);
        thuoc.setGiaBanSi(ZERO);
        thuoc.setSoLuongTon(0);

        ChiTietPhieuNhap chiTiet = new ChiTietPhieuNhap();
        chiTiet.setThuoc(thuoc);
        chiTiet.setSoLuong(0);
        chiTiet.setDonVi("");
        chiTiet.setDonGia(ZERO);
        return chiTiet;
    }

    private ChiTietPhieuNhap timChiTietTrung(ChiTietPhieuNhap chiTietMoi) {
        return danhSachGoc.stream()
                .filter(chiTietHienTai -> laCungThuoc(chiTietHienTai, chiTietMoi))
                .findFirst()
                .orElse(null);
    }

    private boolean laCungThuoc(ChiTietPhieuNhap chiTietA, ChiTietPhieuNhap chiTietB) {
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

    private void capNhatThongTinThuoc(ChiTietPhieuNhap chiTiet, Thuoc thuocMoi) {
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

    private Thuoc damBaoCoThuoc(ChiTietPhieuNhap chiTiet) {
        if (chiTiet.getThuoc() == null) {
            chiTiet.setThuoc(new Thuoc());
        }
        return chiTiet.getThuoc();
    }

    private ChiTietPhieuNhap layDongTheoIndex(int rowIndex) {
        if (tableChiTiet == null || rowIndex < 0 || rowIndex >= tableChiTiet.getItems().size()) {
            return null;
        }
        return tableChiTiet.getItems().get(rowIndex);
    }

    private BigDecimal layDonGiaAnToan(ChiTietPhieuNhap chiTiet) {
        return chiTiet != null && chiTiet.getDonGia() != null ? chiTiet.getDonGia() : ZERO;
    }

    private void lamMoiBang() {
        if (tableChiTiet != null) {
            tableChiTiet.refresh();
        }
    }

    private String chuanHoaChuoi(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String layChuoiAnToan(String value) {
        return value == null ? "" : value;
    }

}
