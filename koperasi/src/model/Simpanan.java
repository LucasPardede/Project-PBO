package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity: Simpanan
 * Memetakan tabel [simpanan] di SQL Server.
 *
 * Relasi: Many-to-One ke Anggota.
 */
public class Simpanan {

    // ── Kolom tabel ─────────────────────────────────────────────────────────
    private int          id;
    private int          anggotaId;
    private BigDecimal   jumlah;
    private LocalDateTime tanggal;
    private String       keterangan;

    // ── ORM: object relasi (optional load) ──────────────────────────────────
    private Anggota anggota;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Simpanan() {}

    public Simpanan(int anggotaId, BigDecimal jumlah, String keterangan) {
        this.anggotaId  = anggotaId;
        this.jumlah     = jumlah;
        this.keterangan = keterangan;
        this.tanggal    = LocalDateTime.now();
    }

    public Simpanan(int id, int anggotaId, BigDecimal jumlah,
                    LocalDateTime tanggal, String keterangan) {
        this.id         = id;
        this.anggotaId  = anggotaId;
        this.jumlah     = jumlah;
        this.tanggal    = tanggal;
        this.keterangan = keterangan;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int          getId()                     { return id; }
    public void         setId(int id)               { this.id = id; }

    public int          getAnggotaId()              { return anggotaId; }
    public void         setAnggotaId(int anggotaId) { this.anggotaId = anggotaId; }

    public BigDecimal   getJumlah()                 { return jumlah; }
    public void         setJumlah(BigDecimal jumlah){ this.jumlah = jumlah; }

    public LocalDateTime getTanggal()               { return tanggal; }
    public void          setTanggal(LocalDateTime t){ this.tanggal = t; }

    public String       getKeterangan()             { return keterangan; }
    public void         setKeterangan(String k)     { this.keterangan = k; }

    public Anggota      getAnggota()                { return anggota; }
    public void         setAnggota(Anggota a)       { this.anggota = a; }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Simpanan{id=%d, anggotaId=%d, jumlah=%.2f, tanggal=%s, ket='%s'}",
                id, anggotaId, jumlah, tanggal, keterangan);
    }
}
