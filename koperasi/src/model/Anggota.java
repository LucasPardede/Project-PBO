package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Anggota
 * Memetakan tabel [anggota] di SQL Server.
 *
 * Relasi One-to-Many:
 *   - Satu Anggota → banyak Simpanan
 *   - Satu Anggota → banyak Pinjaman
 */
public class Anggota {

    // ── Kolom tabel ─────────────────────────────────────────────────────────
    private int       id;
    private String    nama;
    private String    alamat;
    private String    noTelepon;
    private LocalDate tanggalBergabung;

    // ── Relasi ORM (lazy load via service) ──────────────────────────────────
    private List<Simpanan> daftarSimpanan = new ArrayList<>();
    private List<Pinjaman> daftarPinjaman = new ArrayList<>();

    // ── Constructors ─────────────────────────────────────────────────────────

    public Anggota() {}

    public Anggota(String nama, String alamat, String noTelepon) {
        this.nama       = nama;
        this.alamat     = alamat;
        this.noTelepon  = noTelepon;
        this.tanggalBergabung = LocalDate.now();
    }

    public Anggota(int id, String nama, String alamat, String noTelepon, LocalDate tanggalBergabung) {
        this.id               = id;
        this.nama             = nama;
        this.alamat           = alamat;
        this.noTelepon        = noTelepon;
        this.tanggalBergabung = tanggalBergabung;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int       getId()                      { return id; }
    public void      setId(int id)                { this.id = id; }

    public String    getNama()                    { return nama; }
    public void      setNama(String nama)         { this.nama = nama; }

    public String    getAlamat()                  { return alamat; }
    public void      setAlamat(String alamat)     { this.alamat = alamat; }

    public String    getNoTelepon()               { return noTelepon; }
    public void      setNoTelepon(String noTelepon){ this.noTelepon = noTelepon; }

    public LocalDate getTanggalBergabung()        { return tanggalBergabung; }
    public void      setTanggalBergabung(LocalDate d){ this.tanggalBergabung = d; }

    public List<Simpanan> getDaftarSimpanan()          { return daftarSimpanan; }
    public void           setDaftarSimpanan(List<Simpanan> s){ this.daftarSimpanan = s; }

    public List<Pinjaman> getDaftarPinjaman()          { return daftarPinjaman; }
    public void           setDaftarPinjaman(List<Pinjaman> p){ this.daftarPinjaman = p; }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Anggota{id=%d, nama='%s', alamat='%s', telepon='%s', bergabung=%s}",
                id, nama, alamat, noTelepon, tanggalBergabung);
    }
}
