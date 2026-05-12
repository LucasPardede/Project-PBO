package service;

import dao.AnggotaDao;
import model.Anggota;

import java.util.List;
import java.util.Optional;

/**
 * AnggotaService — Business Logic untuk manajemen anggota koperasi.
 *
 * Tanggung jawab:
 * - Validasi data anggota sebelum disimpan
 * - Format nama (capitalize)
 * - Koordinasi CRUD anggota
 */
public class AnggotaService {

    private final AnggotaDao anggotaDao;

    public AnggotaService() {
        this.anggotaDao = new AnggotaDao();
    }

    /**
     * Daftarkan anggota baru.
     *
     * @param nama       nama lengkap anggota
     * @param alamat     alamat tinggal
     * @param noTelepon  nomor telepon
     * @return Anggota yang berhasil disimpan, atau null jika gagal
     */
    public Anggota daftarAnggota(String nama, String alamat, String noTelepon) {
        // ── Validasi Input ────────────────────────────────────────────────────
        if (nama == null || nama.trim().isEmpty()) {
            System.out.println("[AnggotaService] Nama tidak boleh kosong!");
            return null;
        }
        if (nama.trim().length() < 3) {
            System.out.println("[AnggotaService] Nama minimal 3 karakter!");
            return null;
        }
        if (noTelepon != null && !noTelepon.trim().isEmpty()
                && !noTelepon.trim().matches("[0-9+\\-\\s]{8,15}")) {
            System.out.println("[AnggotaService] Format nomor telepon tidak valid!");
            return null;
        }

        // ── Business Logic ────────────────────────────────────────────────────
        Anggota anggota = new Anggota(
                kapitalisasi(nama.trim()),
                alamat != null ? alamat.trim() : "",
                noTelepon != null ? noTelepon.trim() : ""
        );

        Anggota saved = anggotaDao.save(anggota);
        if (saved != null) {
            System.out.println("[AnggotaService] Anggota berhasil didaftarkan. ID: " + saved.getId());
        }
        return saved;
    }

    /**
     * Ambil semua daftar anggota.
     */
    public List<Anggota> semuaAnggota() {
        return anggotaDao.findAll();
    }

    /**
     * Cari anggota berdasarkan ID.
     */
    public Optional<Anggota> cariById(int id) {
        if (id <= 0) {
            System.out.println("[AnggotaService] ID anggota tidak valid!");
            return Optional.empty();
        }
        return anggotaDao.findById(id);
    }

    /**
     * Cari anggota berdasarkan nama (partial match).
     */
    public List<Anggota> cariByNama(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return anggotaDao.findAll();
        }
        return anggotaDao.findByNama(keyword.trim());
    }

    /**
     * Update data anggota.
     *
     * @param id        ID anggota yang diupdate
     * @param nama      nama baru
     * @param alamat    alamat baru
     * @param noTelepon telepon baru
     * @return true jika berhasil
     */
    public boolean updateAnggota(int id, String nama, String alamat, String noTelepon) {
        Optional<Anggota> existing = anggotaDao.findById(id);
        if (existing.isEmpty()) {
            System.out.println("[AnggotaService] Anggota ID " + id + " tidak ditemukan!");
            return false;
        }

        if (nama == null || nama.trim().isEmpty()) {
            System.out.println("[AnggotaService] Nama tidak boleh kosong!");
            return false;
        }

        Anggota anggota = existing.get();
        anggota.setNama(kapitalisasi(nama.trim()));
        anggota.setAlamat(alamat != null ? alamat.trim() : anggota.getAlamat());
        anggota.setNoTelepon(noTelepon != null ? noTelepon.trim() : anggota.getNoTelepon());

        boolean ok = anggotaDao.update(anggota);
        if (ok) System.out.println("[AnggotaService] Data anggota berhasil diupdate.");
        return ok;
    }

    /**
     * Hapus anggota beserta seluruh data terkait (CASCADE).
     */
    public boolean hapusAnggota(int id) {
        Optional<Anggota> existing = anggotaDao.findById(id);
        if (existing.isEmpty()) {
            System.out.println("[AnggotaService] Anggota ID " + id + " tidak ditemukan!");
            return false;
        }
        boolean ok = anggotaDao.delete(id);
        if (ok) System.out.println("[AnggotaService] Anggota berhasil dihapus.");
        return ok;
    }

    /**
     * Hitung total anggota terdaftar.
     */
    public int totalAnggota() {
        return anggotaDao.count();
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /** Kapitalisasi setiap kata dalam nama. */
    private String kapitalisasi(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] kata = s.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String k : kata) {
            if (!k.isEmpty()) {
                sb.append(Character.toUpperCase(k.charAt(0)))
                  .append(k.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
