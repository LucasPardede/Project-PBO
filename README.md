# 🏦 Sistem Koperasi Simpan Pinjam
### Java Console App | JDBC + ORM Manual | SQL Server (SSMS)

---

## 📁 Struktur Project

```
koperasi/
├── database/
│   └── koperasi_sqlserver.sql     ← Script DDL untuk SSMS
├── lib/
│   └── mssql-jdbc-12.x.x.jar     ← Driver JDBC SQL Server (download manual)
└── src/
    ├── util/
    │   └── DBConnection.java      ← Koneksi Singleton ke SQL Server
    ├── model/
    │   ├── Admin.java             ← Entity tabel admin
    │   ├── Anggota.java           ← Entity tabel anggota
    │   ├── Simpanan.java          ← Entity tabel simpanan
    │   ├── Pinjaman.java          ← Entity tabel pinjaman
    │   └── Cicilan.java           ← Entity tabel cicilan
    ├── dao/
    │   ├── AdminDao.java          ← JDBC DAO admin
    │   ├── AnggotaDao.java        ← JDBC DAO anggota (CRUD)
    │   ├── SimpananDao.java       ← JDBC DAO simpanan
    │   ├── PinjamanDao.java       ← JDBC DAO pinjaman
    │   └── CicilanDao.java        ← JDBC DAO cicilan + ATOMIC TRANSACTION
    ├── service/
    │   ├── AdminService.java      ← Business logic login
    │   ├── AnggotaService.java    ← Business logic manajemen anggota
    │   ├── SimpananService.java   ← Business logic simpanan
    │   └── PinjamanService.java   ← Business logic pinjaman & cicilan
    └── main/
        └── Main.java              ← Entry point aplikasi
```

---

## 🚀 Setup di Eclipse

### Langkah 1: Persiapan Database (SSMS)

1. Buka **SQL Server Management Studio (SSMS)**
2. Connect ke server lokal (biasanya `localhost` atau `.\SQLEXPRESS`)
3. Klik **New Query**
4. Buka file `database/koperasi_sqlserver.sql`
5. **Execute (F5)** → Database `KoperasiDB` akan otomatis terbuat
6. Verifikasi: di **Object Explorer** → Databases → KoperasiDB

### Langkah 2: Download Driver JDBC SQL Server

1. Pergi ke: https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
2. Download **mssql-jdbc-12.x.x.jre11.jar** (untuk JDK 11+)
3. Letakkan file JAR di folder `lib/`

### Langkah 3: Import ke Eclipse

1. **File → New → Java Project**
2. Project name: `KoperasiApp`
3. Klik kanan project → **Build Path → Configure Build Path**
4. Tab **Libraries → Classpath → Add External JARs**
5. Pilih `mssql-jdbc-12.x.x.jre11.jar` dari folder `lib/`
6. **OK**

### Langkah 4: Buat Package Structure

Di src, buat package:
- `util`
- `model`
- `dao`
- `service`
- `main`

Lalu masukkan masing-masing file `.java` sesuai packagenya.

### Langkah 5: Konfigurasi Koneksi

Buka `src/util/DBConnection.java`, ubah:

```java
private static final String HOST     = "localhost";    // atau .\SQLEXPRESS
private static final String PORT     = "1433";
private static final String DB_NAME  = "KoperasiDB";
private static final String USER     = "sa";           // username SQL Server kamu
private static final String PASSWORD = "YourPassword"; // password kamu
```

> **Alternatif Windows Authentication** (jika tidak pakai username/password):
> Ubah URL menjadi:
> ```java
> private static final String URL =
>     "jdbc:sqlserver://localhost\\SQLEXPRESS;"
>     + "databaseName=KoperasiDB;"
>     + "integratedSecurity=true;"
>     + "trustServerCertificate=true;";
> ```
> Dan tambahkan `sqljdbc_auth.dll` ke System32.

### Langkah 6: Run Program

1. Klik kanan `Main.java` → **Run As → Java Application**
2. Login dengan: **username: `admin`** | **password: `admin123`**

---

## 💻 Contoh Interaksi Program

```
════════════════════════════════════════════════════════════
          SISTEM INFORMASI KOPERASI SIMPAN PINJAM
          Java + JDBC + ORM Manual | SQL Server
════════════════════════════════════════════════════════════

============================================================
   LOGIN ADMIN
============================================================
Username: admin
Password: admin123
[DB] Koneksi ke SQL Server berhasil.
Selamat datang, ADMIN!

============================================================
   MENU UTAMA
============================================================
  1. Manajemen Anggota
  2. Simpanan
  3. Pinjaman & Cicilan
  4. Laporan
  0. Keluar
------------------------------------------------------------
Pilih menu: 1

============================================================
   MANAJEMEN ANGGOTA
============================================================
  1. Daftar Anggota Baru
  2. Lihat Semua Anggota
  ...
Pilih: 1

--- DAFTAR ANGGOTA BARU ---
Nama lengkap : budi santoso
Alamat       : Jl. Merdeka No. 10 Medan
No. Telepon  : 08123456789
[AnggotaService] Anggota berhasil didaftarkan. ID: 1
✅ Anggota berhasil didaftarkan!
  ┌─ Anggota ─────────────────────────────┐
  │ ID         : 1                         │
  │ Nama       : Budi Santoso              │
  │ Alamat     : Jl. Merdeka No. 10 Medan  │
  │ Telepon    : 08123456789               │
  │ Bergabung  : 2026-05-03                │
  └───────────────────────────────────────┘

--- SETOR SIMPANAN ---
ID Anggota  : 1
Jumlah (Rp) : 2000000
Keterangan  : Simpanan awal
✅ Setoran Rp 2.000.000 berhasil dicatat (ID Simpanan: 1)
   Saldo sekarang: Rp 2.000.000

--- AJUKAN PINJAMAN ---
ID Anggota    : 1
   Simpanan saat ini : Rp 2.000.000
   Plafon maksimal   : Rp 6.000.000 (3× simpanan)
Jumlah pinjaman (Rp): 3000000
✅ Pinjaman disetujui!
  ┌─ Pinjaman ────────────────────────────┐
  │ ID Pinjaman : 1                        │
  │ Jumlah      : Rp           3.000.000   │
  │ Sisa        : Rp           3.000.000   │
  │ Bunga       : 1.5%                     │
  │ Status      : AKTIF                    │
  └───────────────────────────────────────┘

--- BAYAR CICILAN ---
ID Pinjaman   : 1
   Sisa pinjaman: Rp 3.000.000 | Status: AKTIF
Jumlah bayar (Rp): 500000
[CicilanDao] Transaksi COMMIT berhasil.
[PinjamanService] Cicilan Rp 500.000 diterima. Sisa: Rp 2.500.000
✅ Cicilan berhasil dibayar.
```

---

## 🔍 JDBC vs ORM — Perbandingan Singkat

| Aspek | JDBC Murni | ORM (Manual Mapper) |
|-------|-----------|---------------------|
| **Paradigma** | Berbasis **tabel** & baris | Berbasis **object** Java |
| **Query** | Tulis SQL sendiri | SQL tetap ada, tapi mapping otomatis |
| **Mapping** | Manual `rs.getString()` setiap field | `mapRow()` terpusat satu tempat |
| **Boilerplate** | Tinggi (banyak kode berulang) | Lebih terstruktur & rapi |
| **Kontrol** | Penuh, tapi verbose | Cukup fleksibel |
| **Keamanan** | PreparedStatement manual | PreparedStatement via DAO |
| **Relasi** | Join query manual | Object graph (anggota.getSimpanan()) |

> Project ini menggunakan **pendekatan hybrid**: SQL ditulis manual (kontrol penuh) 
> tapi mapping ResultSet → Object dilakukan lewat `mapRow()` yang terpusat 
> (prinsip ORM/Data Mapper Pattern).

---

## ⭐ Fitur Unggulan

- ✅ **CRUD Lengkap** — Anggota (Create, Read, Update, Delete)
- ✅ **PreparedStatement** — Aman dari SQL Injection
- ✅ **try-with-resources** — Semua resource JDBC tertutup otomatis
- ✅ **ATOMIC TRANSACTION** — Bayar cicilan = insert cicilan + update sisa (COMMIT/ROLLBACK)
- ✅ **Login Admin** — Autentikasi dengan 3 kali percobaan
- ✅ **Validasi Input** — Nominal, format telepon, plafon pinjaman
- ✅ **ORM Manual** — mapRow() pattern (Data Mapper)
- ✅ **Singleton Connection** — Satu koneksi efisien
- ✅ **Stored Procedure** — `sp_bayar_cicilan` di SQL Server
- ✅ **Views & Indexes** — Performa query optimal
