DROP DATABASE IF EXISTS db_pharmacy;
CREATE DATABASE IF NOT EXISTS db_pharmacy
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE db_pharmacy;

DROP TABLE IF EXISTS thuoc;
CREATE TABLE IF NOT EXISTS thuoc (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    ten_thuoc     VARCHAR(200)    NOT NULL        COMMENT 'Tên thuốc',
    don_vi        VARCHAR(50)     NOT NULL        COMMENT 'Đơn vị: viên, hộp, chai...',
    gia_nhap      DECIMAL(15, 2)  DEFAULT 0       COMMENT 'Giá nhập',
    gia_ban_si    DECIMAL(15, 2)  DEFAULT 0       COMMENT 'Giá bán sỉ',
    so_luong_ton  INT              DEFAULT 0       COMMENT 'Tồn kho',
    han_su_dung   DATE                            COMMENT 'Hạn sử dụng',
    mo_ta         TEXT                            COMMENT 'Mô tả thêm',
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

DROP TABLE IF EXISTS phieu_nhap;
CREATE TABLE IF NOT EXISTS phieu_nhap (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nha_cung_cap  VARCHAR(200),
    
    ghi_chu       TEXT							NULL,
    
	anh_hoa_don_nhap_path	VARCHAR(500) 		NULL,

    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

DROP TABLE IF EXISTS chi_tiet_phieu_nhap;
CREATE TABLE IF NOT EXISTS chi_tiet_phieu_nhap (
    id            			INT AUTO_INCREMENT PRIMARY KEY, -- Khóa chính độc lập
    phieu_nhap_id 			INT             NOT NULL,       -- Khóa ngoại (cho phép trùng lặp để tạo quan hệ 1-n)
    
    thuoc_id      			INT             	NULL,
    ten_thuoc 				VARCHAR(50)		NOT NULL,
    so_luong      			INT             NOT NULL,
	don_vi					VARCHAR(10)		NOT NULL,
    don_gia					DECIMAL(15, 2)		NULL,
    han_su_dung				date				NULL,
	mo_ta					VARCHAR(50)				,
    
    FOREIGN KEY (phieu_nhap_id) REFERENCES phieu_nhap (id) ON DELETE CASCADE,
    FOREIGN KEY (thuoc_id)      REFERENCES thuoc (id) ON DELETE SET NULL
) ENGINE = InnoDB;

DROP TABLE IF EXISTS khach_hang;
CREATE TABLE IF NOT EXISTS khach_hang (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten      VARCHAR(30)     UNIQUE NOT NULL,
    sdt         VARCHAR(10)     UNIQUE NOT NULL
) ENGINE = InnoDB;

DROP TABLE IF EXISTS hoa_don;
CREATE TABLE IF NOT EXISTS hoa_don (
    id            	INT AUTO_INCREMENT PRIMARY KEY,
    
    khach_hang_id 	INT             NULL,
    ten_khach_hang  VARCHAR (50)    NOT NULL,
    tong_tien     	DECIMAL(15, 2)  DEFAULT 0,
    	
    ngay_ban      	DATE            NOT NULL,
	
    anh_hoa_don_path VARCHAR(500) NULL,

    created_at    	TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (khach_hang_id) REFERENCES khach_hang (id) ON DELETE SET NULL
) ENGINE = InnoDB;

DROP TABLE IF EXISTS chi_tiet_hoa_don;
CREATE TABLE IF NOT EXISTS chi_tiet_hoa_don (
    id          INT AUTO_INCREMENT PRIMARY KEY, -- Khóa chính độc lập
    hoa_don_id  INT             NOT NULL,       -- Khóa ngoại (cho phép trùng lặp để tạo quan hệ 1-n)
    
    thuoc_id    INT             NULL,
    ten_thuoc	VARCHAR(50)		NOT NULL,
    so_luong    INT             NOT NULL,
    don_vi		VARCHAR(10)     NOT NULL,
    
    don_gia     DECIMAL(15, 2)  NOT NULL,
    thanh_tien  DECIMAL(15, 2)  NOT NULL,
    
    FOREIGN KEY (hoa_don_id) REFERENCES hoa_don (id) ON DELETE CASCADE,
    FOREIGN KEY (thuoc_id)   REFERENCES thuoc (id) ON DELETE SET NULL
) ENGINE = InnoDB;


-- ============================================
-- DỮ LIỆU MẪU (Giữ nguyên)
-- ============================================
INSERT INTO thuoc
    (ten_thuoc, don_vi, gia_nhap, gia_ban_si, so_luong_ton, han_su_dung)
VALUES
    ('Paracetamol Syrup 120mg/5ml', 'Chai', 22000, 30000, 75, '2026-08-31'),
    ('Azithromycin 250mg', 'Hộp', 48000, 65000, 45, '2026-07-31'),
    ('Cefuroxime 500mg', 'Hộp', 70000, 95000, 35, '2026-09-30'),
    ('Ciprofloxacin 500mg', 'Hộp', 40000, 58000, 65, '2026-06-30'),
    ('Doxycycline 100mg', 'Hộp', 28000, 40000, 55, '2025-12-31'),

    ('Clindamycin 300mg', 'Hộp', 65000, 85000, 30, '2026-10-31'),
    ('Levofloxacin 500mg', 'Hộp', 75000, 98000, 40, '2026-11-30'),
    ('Erythromycin 500mg', 'Hộp', 42000, 58000, 50, '2026-05-31'),
    ('Fluconazole 150mg', 'Viên', 12000, 18000, 90, '2026-12-31'),
    ('Nystatin 100000 IU', 'Hộp', 30000, 45000, 60, '2025-10-31'),

    ('Diclofenac 50mg', 'Hộp', 18000, 28000, 110, '2026-07-20'),
    ('Meloxicam 7.5mg', 'Hộp', 25000, 37000, 80, '2026-09-30'),
    ('Naproxen 500mg', 'Hộp', 35000, 50000, 70, '2026-11-30'),
    ('Celecoxib 200mg', 'Hộp', 60000, 82000, 45, '2026-12-31'),
    ('Tramadol 50mg', 'Hộp', 42000, 60000, 25, '2025-08-31'),

    ('Esomeprazole 20mg', 'Hộp', 38000, 55000, 100, '2026-12-31'),
    ('Pantoprazole 40mg', 'Hộp', 45000, 62000, 90, '2026-10-31'),
    ('Famotidine 20mg', 'Hộp', 27000, 39000, 75, '2026-06-30'),
    ('Domperidone 10mg', 'Hộp', 22000, 32000, 85, '2025-11-30'),
    ('Ondansetron 4mg', 'Hộp', 55000, 78000, 40, '2026-08-31'),

    ('Loperamide 2mg', 'Hộp', 15000, 23000, 120, '2026-12-31'),
    ('Smecta 3g', 'Gói', 3500, 5000, 300, '2026-11-30'),
    ('Bisacodyl 5mg', 'Hộp', 18000, 27000, 65, '2026-05-31'),
    ('Lactulose 10g/15ml', 'Chai', 45000, 65000, 55, '2025-09-30'),
    ('Salbutamol 2mg', 'Hộp', 25000, 38000, 70, '2026-07-25'),

    ('Montelukast 10mg', 'Hộp', 60000, 85000, 50, '2026-07-31'),
    ('Bromhexine 8mg', 'Hộp', 17000, 26000, 100, '2026-10-31'),
    ('Acetylcysteine 200mg', 'Gói', 4000, 6000, 250, '2026-12-31'),
    ('Dextromethorphan 15mg', 'Chai', 23000, 34000, 80, '2025-06-30'),
    ('Ambroxol 30mg', 'Hộp', 24000, 36000, 95, '2026-12-31'),

    ('Amlodipine 5mg', 'Hộp', 20000, 30000, 150, '2026-08-15'),
    ('Losartan 50mg', 'Hộp', 35000, 50000, 130, '2026-11-30'),
    ('Valsartan 80mg', 'Hộp', 50000, 70000, 85, '2026-08-31'),
    ('Bisoprolol 5mg', 'Hộp', 42000, 60000, 75, '2025-09-30'),
    ('Hydrochlorothiazide 25mg', 'Hộp', 16000, 25000, 110, '2026-12-31'),

    ('Furosemide 40mg', 'Hộp', 18000, 28000, 90, '2026-07-18'),
    ('Clopidogrel 75mg', 'Hộp', 65000, 90000, 60, '2026-10-31'),
    ('Rosuvastatin 10mg', 'Hộp', 58000, 80000, 70, '2026-12-31'),
    ('Gliclazide MR 30mg', 'Hộp', 45000, 65000, 80, '2025-12-31'),
    ('Sitagliptin 100mg', 'Hộp', 180000, 230000, 35, '2026-09-30'),

    ('Glimepiride 2mg', 'Hộp', 30000, 45000, 90, '2026-05-31'),
    ('Insulin Glargine 100IU/ml', 'Bút', 210000, 260000, 20, '2026-07-30'),
    ('Levothyroxine 50mcg', 'Hộp', 40000, 58000, 65, '2026-12-31'),
    ('Prednisolone 5mg', 'Hộp', 22000, 33000, 100, '2025-06-30'),
    ('Dexamethasone 0.5mg', 'Hộp', 19000, 29000, 75, '2026-04-30'),

    ('Ferrous Sulfate 200mg', 'Hộp', 28000, 40000, 120, '2026-08-31'),
    ('Calcium Carbonate 500mg', 'Hộp', 35000, 50000, 140, '2026-11-30'),
    ('Vitamin D3 1000IU', 'Hộp', 40000, 58000, 160, '2026-12-31'),
    ('Zinc 10mg', 'Hộp', 25000, 37000, 130, '2025-08-31'),
    ('ORESOL', 'Gói', 2500, 4000, 400, '2026-09-30');

INSERT INTO khach_hang (ho_ten, sdt)
VALUES 	('Nguyễn Văn A','0456789012'),
		('Nguyễn Văn B','0345678901'),
        ('Nguyễn Văn C','0234567891'),
        ('Nguyễn Văn D','0123456789');
