package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity: Pinjaman
 * Memetakan tabel [pinjaman] di SQL Server.
 *
 * Relasi:
 *   - Many-to-One ke Anggota
 *   - One-to-Many ke Cicilan
 */
public class Pinjaman {

    // ── Kolom tabel ─────────────────────────────────────────────────────────
    private int           id;
    private int           anggotaId;
    private BigDecimal    jumlah;
    private BigDecimal    sisa;
    private BigDecimal    bunga;          // % per bulan
    private String        status;         // AKTIF | LUNAS
    private LocalDateTime tanggalPengajuan;

    // ── ORM: object relasi ───────────────────────────────────────────────────
    private Anggota       anggota;
    private List<Cicilan> daftarCicilan = new ArrayList<>();

    // ── Constructors ─────────────────────────────────────────────────────────

    public Pinjaman() {}

    public Pinjaman(int anggotaId, BigDecimal jumlah, BigDecimal bunga) {
        this.anggotaId       = anggotaId;
        this.jumlah          = jumlah;
        this.sisa            = jumlah;   // awalnya sisa = jumlah pinjaman
        this.bunga           = bunga;
        this.status          = "AKTIF";
        this.tanggalPengajuan = LocalDateTime.now();
    }

    public Pinjaman(int id, int anggotaId, BigDecimal jumlah, BigDecimal sisa,
                    BigDecimal bunga, String status, LocalDateTime tanggalPengajuan) {
        this.id               = id;
        this.anggotaId        = anggotaId;
        this.jumlah           = jumlah;
        this.sisa             = sisa;
        this.bunga            = bunga;
        this.status           = status;
        this.tanggalPengajuan = tanggalPengajuan;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int           getId()                       { return id; }
    public void          setId(int id)                 { this.id = id; }

    public int           getAnggotaId()                { return anggotaId; }
    public void          setAnggotaId(int a)           { this.anggotaId = a; }

    public BigDecimal    getJumlah()                   { return jumlah; }
    public void          setJumlah(BigDecimal j)       { this.jumlah = j; }

    public BigDecimal    getSisa()                     { return sisa; }
    public void          setSisa(BigDecimal s)         { this.sisa = s; }

    public BigDecimal    getBunga()                    { return bunga; }
    public void          setBunga(BigDecimal b)        { this.bunga = b; }

    public String        getStatus()                   { return status; }
    public void          setStatus(String s)           { this.status = s; }

    public LocalDateTime getTanggalPengajuan()         { return tanggalPengajuan; }
    public void          setTanggalPengajuan(LocalDateTime t){ this.tanggalPengajuan = t; }

    public Anggota       getAnggota()                  { return anggota; }
    public void          setAnggota(Anggota a)         { this.anggota = a; }

    public List<Cicilan> getDaftarCicilan()            { return daftarCicilan; }
    public void          setDaftarCicilan(List<Cicilan> c){ this.daftarCicilan = c; }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Pinjaman{id=%d, anggotaId=%d, jumlah=%.2f, sisa=%.2f, bunga=%.1f%%, status='%s'}",
                id, anggotaId, jumlah, sisa, bunga, status);
    }
}
