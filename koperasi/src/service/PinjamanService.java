package service;

import dao.AnggotaDao;
import dao.CicilanDao;
import dao.PinjamanDao;
import dao.SimpananDao;
import model.Cicilan;
import model.Pinjaman;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * PinjamanService — Business Logic untuk pengajuan pinjaman & cicilan.
 *
 * Aturan bisnis koperasi:
 * - Maksimal pinjaman = 3× total simpanan anggota
 * - Bunga default 1.5% per bulan (flat)
 * - Anggota tidak boleh punya pinjaman aktif lebih dari 1
 */
public class PinjamanService {

    private static final BigDecimal BUNGA_DEFAULT   = new BigDecimal("1.5");
    private static final BigDecimal FAKTOR_PINJAMAN = new BigDecimal("3");  // 3× simpanan

    private final PinjamanDao pinjamanDao;
    private final CicilanDao  cicilanDao;
    private final SimpananDao simpananDao;
    private final AnggotaDao  anggotaDao;

    public PinjamanService() {
        this.pinjamanDao = new PinjamanDao();
        this.cicilanDao  = new CicilanDao();
        this.simpananDao = new SimpananDao();
        this.anggotaDao  = new AnggotaDao();
    }

    // ── Pengajuan Pinjaman ────────────────────────────────────────────────────

    /**
     * Ajukan pinjaman baru.
     *
     * Validasi:
     * 1. Anggota harus terdaftar
     * 2. Tidak boleh ada pinjaman aktif lain
     * 3. Jumlah ≤ 3× total simpanan
     * 4. Minimal pinjaman Rp 500.000
     *
     * @param anggotaId ID anggota pemohon
     * @param jumlah    jumlah pinjaman yang diajukan
     * @return Pinjaman yang disetujui, atau null jika ditolak
     */
    public Pinjaman ajukanPinjaman(int anggotaId, BigDecimal jumlah) {
        // Validasi: anggota ada?
        if (anggotaDao.findById(anggotaId).isEmpty()) {
            System.out.println("[PinjamanService] Anggota ID " + anggotaId + " tidak ditemukan!");
            return null;
        }

        // Validasi: jumlah minimum
        if (jumlah == null || jumlah.compareTo(new BigDecimal("500000")) < 0) {
            System.out.println("[PinjamanService] Minimal pinjaman Rp 500.000!");
            return null;
        }

        // Validasi: tidak ada pinjaman aktif
        List<Pinjaman> aktif = pinjamanDao.findAktifByAnggotaId(anggotaId);
        if (!aktif.isEmpty()) {
            System.out.println("[PinjamanService] Anggota masih memiliki pinjaman aktif! Lunasi dulu.");
            return null;
        }

        // Validasi: plafon = 3× simpanan
        BigDecimal totalSimpanan = simpananDao.getTotalSaldo(anggotaId);
        BigDecimal plafonMaks    = totalSimpanan.multiply(FAKTOR_PINJAMAN);

        if (jumlah.compareTo(plafonMaks) > 0) {
            System.out.printf("[PinjamanService] Plafon maksimal Rp %.0f (3× simpanan Rp %.0f)%n",
                    plafonMaks, totalSimpanan);
            return null;
        }

        // ── Buat & Simpan Pinjaman ────────────────────────────────────────────
        Pinjaman pinjaman = new Pinjaman(anggotaId, jumlah, BUNGA_DEFAULT);
        Pinjaman saved    = pinjamanDao.save(pinjaman);

        if (saved != null) {
            System.out.printf("[PinjamanService] Pinjaman disetujui! ID: %d | Jumlah: Rp %.0f | Bunga: %.1f%%/bln%n",
                    saved.getId(), saved.getJumlah(), saved.getBunga());
        }
        return saved;
    }

    // ── Pembayaran Cicilan ────────────────────────────────────────────────────

    /**
     * Bayar cicilan pinjaman secara ATOMIC.
     *
     * @param pinjamanId  ID pinjaman yang akan dicicil
     * @param jumlahBayar jumlah yang dibayarkan
     * @return true jika pembayaran berhasil (commit)
     */
    public boolean bayarCicilan(int pinjamanId, BigDecimal jumlahBayar) {
        // Cek pinjaman ada & masih aktif
        Optional<Pinjaman> opt = pinjamanDao.findById(pinjamanId);
        if (opt.isEmpty()) {
            System.out.println("[PinjamanService] Pinjaman ID " + pinjamanId + " tidak ditemukan!");
            return false;
        }

        Pinjaman pinjaman = opt.get();
        if ("LUNAS".equals(pinjaman.getStatus())) {
            System.out.println("[PinjamanService] Pinjaman ini sudah LUNAS!");
            return false;
        }

        // Validasi jumlah bayar
        if (jumlahBayar == null || jumlahBayar.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[PinjamanService] Jumlah bayar harus lebih dari 0!");
            return false;
        }
        if (jumlahBayar.compareTo(pinjaman.getSisa()) > 0) {
            System.out.printf("[PinjamanService] Jumlah bayar (Rp %.0f) melebihi sisa (Rp %.0f)!%n",
                    jumlahBayar, pinjaman.getSisa());
            return false;
        }

        // Delegasi ke CicilanDao dengan ATOMIC TRANSACTION
        boolean ok = cicilanDao.bayarCicilan(
                pinjamanId, jumlahBayar, pinjaman.getSisa(), pinjamanDao);

        if (ok) {
            BigDecimal sisaBaru = pinjaman.getSisa().subtract(jumlahBayar);
            System.out.printf("[PinjamanService] Cicilan Rp %.0f diterima. Sisa: Rp %.0f%n",
                    jumlahBayar, sisaBaru);
            if (sisaBaru.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("[PinjamanService] 🎉 Pinjaman LUNAS!");
            }
        }
        return ok;
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    public List<Pinjaman> semuaPinjaman() {
        return pinjamanDao.findAll();
    }

    public List<Pinjaman> pinjamanAnggota(int anggotaId) {
        return pinjamanDao.findByAnggotaId(anggotaId);
    }

    public List<Pinjaman> pinjamanAktifAnggota(int anggotaId) {
        return pinjamanDao.findAktifByAnggotaId(anggotaId);
    }

    public List<Cicilan> riwayatCicilan(int pinjamanId) {
        return cicilanDao.findByPinjamanId(pinjamanId);
    }

    public Optional<Pinjaman> cariPinjaman(int id) {
        return pinjamanDao.findById(id);
    }

    /**
     * Hitung total cicilan yang sudah dibayar untuk sebuah pinjaman.
     */
    public BigDecimal totalTerbayar(int pinjamanId) {
        return cicilanDao.getTotalBayar(pinjamanId);
    }
}
