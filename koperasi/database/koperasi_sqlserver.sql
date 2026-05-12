-- ============================================================
--  KOPERASI SIMPAN PINJAM - SQL Server (SSMS) Script
--  Database: KoperasiDB
--  Author  : Senior Java Engineer
-- ============================================================

-- 1. Buat & gunakan database
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'KoperasiDB')
    CREATE DATABASE KoperasiDB;
GO

USE KoperasiDB;
GO

-- ============================================================
-- 2. DROP TABLE (jika sudah ada, urutan FK-aware)
-- ============================================================
IF OBJECT_ID('cicilan',   'U') IS NOT NULL DROP TABLE cicilan;
IF OBJECT_ID('pinjaman',  'U') IS NOT NULL DROP TABLE pinjaman;
IF OBJECT_ID('simpanan',  'U') IS NOT NULL DROP TABLE simpanan;
IF OBJECT_ID('anggota',   'U') IS NOT NULL DROP TABLE anggota;
IF OBJECT_ID('admin',     'U') IS NOT NULL DROP TABLE admin;
GO

-- ============================================================
-- 3. CREATE TABLE admin
-- ============================================================
CREATE TABLE admin (
    id       INT           IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50)  NOT NULL UNIQUE,
    password NVARCHAR(100) NOT NULL
);
GO

-- ============================================================
-- 4. CREATE TABLE anggota
-- ============================================================
CREATE TABLE anggota (
    id                 INT           IDENTITY(1,1) PRIMARY KEY,
    nama               NVARCHAR(100) NOT NULL,
    alamat             NVARCHAR(200) NULL,
    no_telepon         NVARCHAR(20)  NULL,
    tanggal_bergabung  DATE          NOT NULL DEFAULT CAST(GETDATE() AS DATE)
);
GO

-- ============================================================
-- 5. CREATE TABLE simpanan
-- ============================================================
CREATE TABLE simpanan (
    id          INT              IDENTITY(1,1) PRIMARY KEY,
    anggota_id  INT              NOT NULL,
    jumlah      DECIMAL(15, 2)   NOT NULL CHECK (jumlah > 0),
    tanggal     DATETIME         NOT NULL DEFAULT GETDATE(),
    keterangan  NVARCHAR(200)    NULL,

    CONSTRAINT FK_simpanan_anggota
        FOREIGN KEY (anggota_id) REFERENCES anggota(id)
        ON DELETE CASCADE
);
GO

-- ============================================================
-- 6. CREATE TABLE pinjaman
-- ============================================================
CREATE TABLE pinjaman (
    id               INT            IDENTITY(1,1) PRIMARY KEY,
    anggota_id       INT            NOT NULL,
    jumlah           DECIMAL(15, 2) NOT NULL CHECK (jumlah > 0),
    sisa             DECIMAL(15, 2) NOT NULL,
    bunga            DECIMAL(5, 2)  NOT NULL DEFAULT 1.5,   -- % per bulan
    status           NVARCHAR(20)   NOT NULL DEFAULT 'AKTIF'
                                    CHECK (status IN ('AKTIF','LUNAS')),
    tanggal_pengajuan DATETIME      NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_pinjaman_anggota
        FOREIGN KEY (anggota_id) REFERENCES anggota(id)
        ON DELETE CASCADE
);
GO

-- ============================================================
-- 7. CREATE TABLE cicilan (pembayaran angsuran)
-- ============================================================
CREATE TABLE cicilan (
    id            INT            IDENTITY(1,1) PRIMARY KEY,
    pinjaman_id   INT            NOT NULL,
    jumlah_bayar  DECIMAL(15, 2) NOT NULL CHECK (jumlah_bayar > 0),
    tanggal_bayar DATETIME       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_cicilan_pinjaman
        FOREIGN KEY (pinjaman_id) REFERENCES pinjaman(id)
        ON DELETE CASCADE
);
GO

-- ============================================================
-- 8. INDEXES (performa query)
-- ============================================================
CREATE INDEX IX_simpanan_anggota   ON simpanan  (anggota_id);
CREATE INDEX IX_pinjaman_anggota   ON pinjaman  (anggota_id);
CREATE INDEX IX_cicilan_pinjaman   ON cicilan   (pinjaman_id);
GO

-- ============================================================
-- 9. SEED DATA — akun admin default
-- ============================================================
INSERT INTO admin (username, password)
VALUES ('admin', 'admin123');
GO

-- ============================================================
-- 10. VIEWS (laporan ringkas)
-- ============================================================

-- View: total simpanan per anggota
CREATE OR ALTER VIEW vw_saldo_anggota AS
SELECT
    a.id          AS anggota_id,
    a.nama,
    ISNULL(SUM(s.jumlah), 0) AS total_simpanan
FROM anggota a
LEFT JOIN simpanan s ON s.anggota_id = a.id
GROUP BY a.id, a.nama;
GO

-- View: ringkasan pinjaman aktif
CREATE OR ALTER VIEW vw_pinjaman_aktif AS
SELECT
    p.id           AS pinjaman_id,
    a.nama         AS nama_anggota,
    p.jumlah,
    p.sisa,
    p.bunga,
    p.status,
    p.tanggal_pengajuan
FROM pinjaman p
JOIN anggota  a ON a.id = p.anggota_id
WHERE p.status = 'AKTIF';
GO

-- ============================================================
-- 11. STORED PROCEDURE — bayar cicilan (atomic)
-- ============================================================
CREATE OR ALTER PROCEDURE sp_bayar_cicilan
    @pinjaman_id  INT,
    @jumlah_bayar DECIMAL(15,2)
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        DECLARE @sisa_sekarang DECIMAL(15,2);

        SELECT @sisa_sekarang = sisa
        FROM pinjaman
        WHERE id = @pinjaman_id AND status = 'AKTIF';

        IF @sisa_sekarang IS NULL
            THROW 50001, 'Pinjaman tidak ditemukan atau sudah lunas.', 1;

        IF @jumlah_bayar > @sisa_sekarang
            THROW 50002, 'Jumlah bayar melebihi sisa pinjaman.', 1;

        -- Kurangi sisa pinjaman
        UPDATE pinjaman
        SET sisa   = sisa - @jumlah_bayar,
            status = CASE WHEN (sisa - @jumlah_bayar) <= 0 THEN 'LUNAS' ELSE 'AKTIF' END
        WHERE id = @pinjaman_id;

        -- Catat cicilan
        INSERT INTO cicilan (pinjaman_id, jumlah_bayar)
        VALUES (@pinjaman_id, @jumlah_bayar);

        COMMIT TRANSACTION;
        SELECT 'SUKSES' AS hasil, @sisa_sekarang - @jumlah_bayar AS sisa_baru;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- ============================================================
-- 12. VERIFIKASI STRUKTUR
-- ============================================================
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM   INFORMATION_SCHEMA.COLUMNS
WHERE  TABLE_CATALOG = 'KoperasiDB'
ORDER  BY TABLE_NAME, ORDINAL_POSITION;
GO

PRINT 'Database KoperasiDB berhasil dibuat!';
GO
