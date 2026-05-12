package service;

import dao.AnggotaDao;
import dao.SimpananDao;
import model.Simpanan;

import java.math.BigDecimal;
import java.util.List;

/**
 * SimpananService — Business Logic untuk transaksi simpanan (setor uang).
 */
public class SimpananService {

    private final SimpananDao simpananDao;
    private final AnggotaDao  anggotaDao;

    public SimpananService() {
        this.simpananDao = new SimpananDao();
        this.anggotaDao  = new AnggotaDao();
    }

    /**
     * Setor simpanan untuk anggota.
     *
     * @param anggotaId ID anggota
     * @param jumlah    jumlah yang disetor
     * @param keterangan keterangan setoran (boleh kosong)
     * @return Simpanan yang berhasil dicatat, atau null jika gagal
     */
    public Simpanan setor(int anggotaId, BigDecimal jumlah, String keterangan) {
        // ── Validasi ──────────────────────────────────────────────────────────
        if (anggotaDao.findById(anggotaId).isEmpty()) {
            System.out.println("[SimpananService] Anggota ID " + anggotaId + " tidak ditemukan!");
            return null;
        }
        if (jumlah == null || jumlah.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("[SimpananService] Jumlah simpanan harus lebih dari 0!");
            return null;
        }
        if (jumlah.compareTo(new BigDecimal("10000")) < 0) {
            System.out.println("[SimpananService] Minimal setoran Rp 10.000!");
            return null;
        }

        // ── Simpan ───────────────────────────────────────────────────────────
        Simpanan simpanan = new Simpanan(anggotaId, jumlah,
                keterangan != null ? keterangan.trim() : "Setoran tunai");
        Simpanan saved = simpananDao.save(simpanan);

        if (saved != null) {
            BigDecimal totalSaldo = simpananDao.getTotalSaldo(anggotaId);
            System.out.printf("[SimpananService] Setoran Rp %.0f berhasil. Total saldo: Rp %.0f%n",
                    jumlah, totalSaldo);
        }
        return saved;
    }

    /**
     * Lihat saldo total anggota.
     */
    public BigDecimal getSaldo(int anggotaId) {
        return simpananDao.getTotalSaldo(anggotaId);
    }

    /**
     * Riwayat simpanan milik anggota.
     */
    public List<Simpanan> riwayatSimpanan(int anggotaId) {
        return simpananDao.findByAnggotaId(anggotaId);
    }

    /**
     * Semua data simpanan (untuk laporan admin).
     */
    public List<Simpanan> semuaSimpanan() {
        return simpananDao.findAll();
    }
}
