package main;

import model.*;
import service.*;
import util.DBConnection;

import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║     SISTEM INFORMASI KOPERASI SIMPAN PINJAM                  ║
 * ║     Java Console Application — JDBC + ORM Manual             ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * Entry point aplikasi.
 * Mengelola alur menu utama dan sub-menu setiap fitur.
 */
public class Main {

    // ── Services ─────────────────────────────────────────────────────────────
    private static final AdminService    adminService    = new AdminService();
    private static final AnggotaService  anggotaService  = new AnggotaService();
    private static final SimpananService simpananService = new SimpananService();
    private static final PinjamanService pinjamanService = new PinjamanService();

    private static final Scanner scanner = new Scanner(System.in);

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String LINE = "=".repeat(60);
    private static final String DASH = "-".repeat(60);

    // ─────────────────────────────────────────────────────────────────────────
    //  MAIN ENTRY
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        tampilkanBanner();

        // ── Login Admin ───────────────────────────────────────────────────────
        Admin adminLogin = prosesLogin();
        if (adminLogin == null) {
            System.out.println("Login gagal. Keluar dari sistem.");
            DBConnection.closeConnection();
            return;
        }

        System.out.println("\nSelamat datang, " + adminLogin.getUsername().toUpperCase() + "!");

        // ── Menu Utama ────────────────────────────────────────────────────────
        boolean jalan = true;
        while (jalan) {
            tampilkanMenuUtama();
            int pilihan = inputInt("Pilih menu: ");

            switch (pilihan) {
                case 1:
                    menuAnggota();
                    break;
                case 2:
                    menuSimpanan();
                    break;
                case 3:
                    menuPinjaman();
                    break;
                case 4:
                    menuLaporan();
                    break;
                case 0:
                    jalan = false;
                    System.out.println("\nTerima kasih. Sampai jumpa!");
                    break;
                default:
                    System.out.println("⚠ Pilihan tidak valid.");
                    break;
            }
        }

        // ── Tutup koneksi saat keluar ─────────────────────────────────────────
        DBConnection.closeConnection();
        scanner.close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MENU ANGGOTA
    // ─────────────────────────────────────────────────────────────────────────

    private static void menuAnggota() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + LINE);
            System.out.println("   MANAJEMEN ANGGOTA");
            System.out.println(LINE);
            System.out.println("  1. Daftar Anggota Baru");
            System.out.println("  2. Lihat Semua Anggota");
            System.out.println("  3. Cari Anggota");
            System.out.println("  4. Update Data Anggota");
            System.out.println("  5. Hapus Anggota");
            System.out.println("  0. Kembali");
            System.out.println(DASH);

            int pilihan = inputInt("Pilih: ");
            switch (pilihan) {
                case 1:
                    daftarAnggota();
                    break;
                case 2:
                    lihatSemuaAnggota();
                    break;
                case 3:
                    cariAnggota();
                    break;
                case 4:
                    updateAnggota();
                    break;
                case 5:
                    hapusAnggota();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("⚠ Pilihan tidak valid.");
                    break;
            }
        }
    }

    private static void daftarAnggota() {
        System.out.println("\n--- DAFTAR ANGGOTA BARU ---");
        String nama      = inputString("Nama lengkap : ");
        String alamat    = inputString("Alamat       : ");
        String telepon   = inputString("No. Telepon  : ");

        Anggota result = anggotaService.daftarAnggota(nama, alamat, telepon);
        if (result != null) {
            System.out.println("✅ Anggota berhasil didaftarkan!");
            cetakDetailAnggota(result);
        }
    }

    private static void lihatSemuaAnggota() {
        List<Anggota> list = anggotaService.semuaAnggota();
        System.out.println("\n--- DAFTAR ANGGOTA (" + list.size() + " orang) ---");
        if (list.isEmpty()) {
            System.out.println("Belum ada anggota terdaftar.");
            return;
        }
        System.out.printf("%-5s %-25s %-30s %-15s %-12s%n",
                "ID", "NAMA", "ALAMAT", "TELEPON", "TGL BERGABUNG");
        System.out.println(DASH);
        for (Anggota a : list) {
            System.out.printf("%-5d %-25s %-30s %-15s %-12s%n",
                    a.getId(), a.getNama(), a.getAlamat(), a.getNoTelepon(), a.getTanggalBergabung());
        }
    }

    private static void cariAnggota() {
        String keyword = inputString("Cari nama anggota: ");
        List<Anggota> list = anggotaService.cariByNama(keyword);
        System.out.println("Ditemukan " + list.size() + " anggota:");
        for (Anggota a : list) cetakDetailAnggota(a);
    }

    private static void updateAnggota() {
        int id = inputInt("ID Anggota yang diupdate: ");
        Optional<Anggota> opt = anggotaService.cariById(id);
        if (opt.isEmpty()) { System.out.println("Anggota tidak ditemukan."); return; }

        Anggota existing = opt.get();
        System.out.println("Data saat ini: " + existing.getNama() + " | " + existing.getAlamat());
        System.out.println("(Tekan Enter untuk tidak mengubah)");

        String nama    = inputStringOptional("Nama baru    : ", existing.getNama());
        String alamat  = inputStringOptional("Alamat baru  : ", existing.getAlamat());
        String telepon = inputStringOptional("Telepon baru : ", existing.getNoTelepon());

        boolean ok = anggotaService.updateAnggota(id, nama, alamat, telepon);
        System.out.println(ok ? "✅ Data berhasil diupdate." : "❌ Gagal update.");
    }

    private static void hapusAnggota() {
        int id = inputInt("ID Anggota yang dihapus: ");
        Optional<Anggota> opt = anggotaService.cariById(id);
        if (opt.isEmpty()) { System.out.println("Anggota tidak ditemukan."); return; }

        System.out.println("⚠ Menghapus: " + opt.get().getNama());
        System.out.println("   Seluruh data simpanan & pinjaman akan ikut terhapus (CASCADE).");
        String konfirmasi = inputString("Ketik 'HAPUS' untuk konfirmasi: ");
        if ("HAPUS".equals(konfirmasi)) {
            boolean ok = anggotaService.hapusAnggota(id);
            System.out.println(ok ? "✅ Anggota dihapus." : "❌ Gagal menghapus.");
        } else {
            System.out.println("Penghapusan dibatalkan.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MENU SIMPANAN
    // ─────────────────────────────────────────────────────────────────────────

    private static void menuSimpanan() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + LINE);
            System.out.println("   SIMPANAN");
            System.out.println(LINE);
            System.out.println("  1. Setor Simpanan");
            System.out.println("  2. Lihat Saldo Anggota");
            System.out.println("  3. Riwayat Simpanan Anggota");
            System.out.println("  0. Kembali");
            System.out.println(DASH);

            int pilihan = inputInt("Pilih: ");
            switch (pilihan) {
                case 1:
                    setorSimpanan();
                    break;
                case 2:
                    lihatSaldo();
                    break;
                case 3:
                    riwayatSimpanan();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("⚠ Pilihan tidak valid.");
                    break;
            }
        }
    }

    private static void setorSimpanan() {
        System.out.println("\n--- SETOR SIMPANAN ---");
        int id = inputInt("ID Anggota  : ");
        BigDecimal jumlah = inputDecimal("Jumlah (Rp) : ");
        String ket        = inputString("Keterangan  : ");

        Simpanan s = simpananService.setor(id, jumlah, ket);
        if (s != null) {
            System.out.printf("✅ Setoran Rp %,.0f berhasil dicatat (ID Simpanan: %d)%n",
                    s.getJumlah(), s.getId());
            System.out.printf("   Saldo sekarang: Rp %,.0f%n",
                    simpananService.getSaldo(id));
        }
    }

    private static void lihatSaldo() {
        int id = inputInt("ID Anggota: ");
        Optional<Anggota> opt = anggotaService.cariById(id);
        if (opt.isEmpty()) { System.out.println("Anggota tidak ditemukan."); return; }

        BigDecimal saldo = simpananService.getSaldo(id);
        System.out.println("\n┌─────────────────────────────────┐");
        System.out.printf( "│  Anggota : %-22s│%n", opt.get().getNama());
        System.out.printf( "│  Saldo   : Rp %,18.0f │%n", saldo);
        System.out.println("└─────────────────────────────────┘");
    }

    private static void riwayatSimpanan() {
        int id = inputInt("ID Anggota: ");
        List<Simpanan> list = simpananService.riwayatSimpanan(id);
        System.out.println("\n--- RIWAYAT SIMPANAN ---");
        if (list.isEmpty()) { System.out.println("Belum ada simpanan."); return; }
        System.out.printf("%-5s %-22s %-15s %-20s%n", "ID", "TANGGAL", "JUMLAH (Rp)", "KETERANGAN");
        System.out.println(DASH);
        for (Simpanan s : list) {
            System.out.printf("%-5d %-22s %,15.0f %-20s%n",
                    s.getId(), s.getTanggal(), s.getJumlah(), s.getKeterangan());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MENU PINJAMAN
    // ─────────────────────────────────────────────────────────────────────────

    private static void menuPinjaman() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + LINE);
            System.out.println("   PINJAMAN & CICILAN");
            System.out.println(LINE);
            System.out.println("  1. Ajukan Pinjaman");
            System.out.println("  2. Lihat Pinjaman Anggota");
            System.out.println("  3. Bayar Cicilan");
            System.out.println("  4. Riwayat Cicilan");
            System.out.println("  0. Kembali");
            System.out.println(DASH);

            int pilihan = inputInt("Pilih: ");
            switch (pilihan) {
                case 1:
                    ajukanPinjaman();
                    break;
                case 2:
                    lihatPinjamanAnggota();
                    break;
                case 3:
                    bayarCicilan();
                    break;
                case 4:
                    riwayatCicilan();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("⚠ Pilihan tidak valid.");
                    break;
            }
        }
    }

    private static void ajukanPinjaman() {
        System.out.println("\n--- AJUKAN PINJAMAN ---");
        int id = inputInt("ID Anggota    : ");
        Optional<Anggota> opt = anggotaService.cariById(id);
        if (opt.isEmpty()) { System.out.println("Anggota tidak ditemukan."); return; }

        BigDecimal saldo = simpananService.getSaldo(id);
        System.out.printf("   Simpanan saat ini : Rp %,.0f%n", saldo);
        System.out.printf("   Plafon maksimal   : Rp %,.0f (3× simpanan)%n",
                saldo.multiply(new BigDecimal("3")));

        BigDecimal jumlah = inputDecimal("Jumlah pinjaman (Rp): ");
        Pinjaman p = pinjamanService.ajukanPinjaman(id, jumlah);
        if (p != null) {
            System.out.println("✅ Pinjaman disetujui!");
            cetakDetailPinjaman(p);
        }
    }

    private static void lihatPinjamanAnggota() {
        int id = inputInt("ID Anggota: ");
        List<Pinjaman> list = pinjamanService.pinjamanAnggota(id);
        System.out.println("\n--- DAFTAR PINJAMAN ---");
        if (list.isEmpty()) { System.out.println("Belum ada pinjaman."); return; }
        for (Pinjaman p : list) cetakDetailPinjaman(p);
    }

    private static void bayarCicilan() {
        System.out.println("\n--- BAYAR CICILAN ---");
        int pinjamanId = inputInt("ID Pinjaman   : ");
        Optional<Pinjaman> opt = pinjamanService.cariPinjaman(pinjamanId);
        if (opt.isEmpty()) { System.out.println("Pinjaman tidak ditemukan."); return; }

        Pinjaman p = opt.get();
        System.out.printf("   Sisa pinjaman: Rp %,.0f | Status: %s%n", p.getSisa(), p.getStatus());

        BigDecimal bayar = inputDecimal("Jumlah bayar (Rp): ");
        boolean ok = pinjamanService.bayarCicilan(pinjamanId, bayar);
        System.out.println(ok ? "✅ Cicilan berhasil dibayar." : "❌ Pembayaran gagal.");
    }

    private static void riwayatCicilan() {
        int pinjamanId = inputInt("ID Pinjaman: ");
        List<Cicilan> list = pinjamanService.riwayatCicilan(pinjamanId);
        System.out.println("\n--- RIWAYAT CICILAN ---");
        if (list.isEmpty()) { System.out.println("Belum ada cicilan."); return; }
        System.out.printf("%-5s %-22s %-15s%n", "ID", "TANGGAL BAYAR", "JUMLAH (Rp)");
        System.out.println(DASH);
        for (Cicilan c : list) {
            System.out.printf("%-5d %-22s %,15.0f%n",
                    c.getId(), c.getTanggalBayar(), c.getJumlahBayar());
        }
        System.out.printf("   Total terbayar: Rp %,.0f%n",
                pinjamanService.totalTerbayar(pinjamanId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LAPORAN
    // ─────────────────────────────────────────────────────────────────────────

    private static void menuLaporan() {
        System.out.println("\n" + LINE);
        System.out.println("   LAPORAN KOPERASI");
        System.out.println(LINE);

        // Ringkasan anggota
        int totalAnggota = anggotaService.totalAnggota();
        System.out.println("  Total Anggota    : " + totalAnggota + " orang");

        // Laporan simpanan
        List<Simpanan> simpananList = simpananService.semuaSimpanan();
        BigDecimal totalSimpanan = simpananList.stream()
                .map(Simpanan::getJumlah)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.printf("  Total Simpanan   : Rp %,.0f%n", totalSimpanan);

        // Laporan pinjaman
        List<Pinjaman> pinjamanList = pinjamanService.semuaPinjaman();
        long aktif  = pinjamanList.stream().filter(p -> "AKTIF".equals(p.getStatus())).count();
        long lunas  = pinjamanList.stream().filter(p -> "LUNAS".equals(p.getStatus())).count();
        BigDecimal totalPinjaman = pinjamanList.stream()
                .map(Pinjaman::getJumlah)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSisaPinjaman = pinjamanList.stream()
                .filter(p -> "AKTIF".equals(p.getStatus()))
                .map(Pinjaman::getSisa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.printf("  Total Pinjaman   : Rp %,.0f%n", totalPinjaman);
        System.out.printf("  Sisa Pinjaman    : Rp %,.0f%n", totalSisaPinjaman);
        System.out.println("  Pinjaman Aktif   : " + aktif);
        System.out.println("  Pinjaman Lunas   : " + lunas);

        System.out.println("\n--- DETAIL PINJAMAN AKTIF ---");
        pinjamanList.stream()
                .filter(p -> "AKTIF".equals(p.getStatus()))
                .forEach(Main::cetakDetailPinjaman);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    private static Admin prosesLogin() {
        System.out.println("\n" + LINE);
        System.out.println("   LOGIN ADMIN");
        System.out.println(LINE);
        int percobaan = 0;
        while (percobaan < 3) {
            String user = inputString("Username: ");
            String pass = inputString("Password: ");
            Optional<Admin> admin = adminService.login(user, pass);
            if (admin.isPresent()) return admin.get();
            percobaan++;
            System.out.println("⚠ Login gagal. Sisa percobaan: " + (3 - percobaan));
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER: Cetak Detail
    // ─────────────────────────────────────────────────────────────────────────

    private static void cetakDetailAnggota(Anggota a) {
        System.out.println("  ┌─ Anggota ─────────────────────────────┐");
        System.out.printf ("  │ ID         : %-25d │%n", a.getId());
        System.out.printf ("  │ Nama       : %-25s │%n", a.getNama());
        System.out.printf ("  │ Alamat     : %-25s │%n", a.getAlamat());
        System.out.printf ("  │ Telepon    : %-25s │%n", a.getNoTelepon());
        System.out.printf ("  │ Bergabung  : %-25s │%n", a.getTanggalBergabung());
        System.out.println("  └───────────────────────────────────────┘");
    }

    private static void cetakDetailPinjaman(Pinjaman p) {
        System.out.println("  ┌─ Pinjaman ────────────────────────────┐");
        System.out.printf ("  │ ID Pinjaman : %-24d │%n", p.getId());
        System.out.printf ("  │ Anggota ID  : %-24d │%n", p.getAnggotaId());
        System.out.printf ("  │ Jumlah      : Rp %,20.0f │%n", p.getJumlah());
        System.out.printf ("  │ Sisa        : Rp %,20.0f │%n", p.getSisa());
        System.out.printf ("  │ Bunga       : %-23.1f%% │%n", p.getBunga());
        System.out.printf ("  │ Status      : %-24s │%n", p.getStatus());
        System.out.printf ("  │ Tanggal     : %-24s │%n", p.getTanggalPengajuan());
        System.out.println("  └───────────────────────────────────────┘");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER: Input Validation
    // ─────────────────────────────────────────────────────────────────────────

    private static int inputInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int val = scanner.nextInt();
                scanner.nextLine(); // consume newline
                return val;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.println("⚠ Masukkan angka yang valid!");
            }
        }
    }

    private static BigDecimal inputDecimal(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim().replace(",", "").replace(".", "");
                BigDecimal val = new BigDecimal(input);
                if (val.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("⚠ Jumlah harus lebih dari 0!");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("⚠ Masukkan angka yang valid (contoh: 500000)");
            }
        }
    }

    private static String inputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static String inputStringOptional(String prompt, String defaultVal) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultVal : input;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER: Tampilan
    // ─────────────────────────────────────────────────────────────────────────

    private static void tampilkanBanner() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("   ██╗  ██╗ ██████╗ ██████╗ ███████╗██████╗  █████╗ ███████╗██╗");
        System.out.println("   ██║ ██╔╝██╔═══██╗██╔══██╗██╔════╝██╔══██╗██╔══██╗██╔════╝██║");
        System.out.println("   █████╔╝ ██║   ██║██████╔╝█████╗  ██████╔╝███████║███████╗██║");
        System.out.println("   ██╔═██╗ ██║   ██║██╔═══╝ ██╔══╝  ██╔══██╗██╔══██║╚════██║██║");
        System.out.println("   ██║  ██╗╚██████╔╝██║     ███████╗██║  ██║██║  ██║███████║██║");
        System.out.println("   ╚═╝  ╚═╝ ╚═════╝ ╚═╝     ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚═╝");
        System.out.println("          SISTEM INFORMASI KOPERASI SIMPAN PINJAM");
        System.out.println("          Java + JDBC + ORM Manual | SQL Server");
        System.out.println("═".repeat(60));
    }

    private static void tampilkanMenuUtama() {
        System.out.println("\n" + LINE);
        System.out.println("   MENU UTAMA");
        System.out.println(LINE);
        System.out.println("  1. Manajemen Anggota");
        System.out.println("  2. Simpanan");
        System.out.println("  3. Pinjaman & Cicilan");
        System.out.println("  4. Laporan");
        System.out.println("  0. Keluar");
        System.out.println(DASH);
    }
}
