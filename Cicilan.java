package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity: Cicilan
 * Memetakan tabel [cicilan] di SQL Server.
 *
 * Relasi: Many-to-One ke Pinjaman.
 */
public class Cicilan {

    // ── Kolom tabel ─────────────────────────────────────────────────────────
    private int           id;
    private int           pinjamanId;
    private BigDecimal    jumlahBayar;
    private LocalDateTime tanggalBayar;

    // ── ORM: object relasi ───────────────────────────────────────────────────
    private Pinjaman pinjaman;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Cicilan() {}

    public Cicilan(int pinjamanId, BigDecimal jumlahBayar) {
        this.pinjamanId   = pinjamanId;
        this.jumlahBayar  = jumlahBayar;
        this.tanggalBayar = LocalDateTime.now();
    }

    public Cicilan(int id, int pinjamanId, BigDecimal jumlahBayar, LocalDateTime tanggalBayar) {
        this.id           = id;
        this.pinjamanId   = pinjamanId;
        this.jumlahBayar  = jumlahBayar;
        this.tanggalBayar = tanggalBayar;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int           getId()                        { return id; }
    public void          setId(int id)                  { this.id = id; }

    public int           getPinjamanId()                { return pinjamanId; }
    public void          setPinjamanId(int p)           { this.pinjamanId = p; }

    public BigDecimal    getJumlahBayar()               { return jumlahBayar; }
    public void          setJumlahBayar(BigDecimal j)   { this.jumlahBayar = j; }

    public LocalDateTime getTanggalBayar()              { return tanggalBayar; }
    public void          setTanggalBayar(LocalDateTime t){ this.tanggalBayar = t; }

    public Pinjaman      getPinjaman()                  { return pinjaman; }
    public void          setPinjaman(Pinjaman p)        { this.pinjaman = p; }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Cicilan{id=%d, pinjamanId=%d, bayar=%.2f, tanggal=%s}",
                id, pinjamanId, jumlahBayar, tanggalBayar);
    }
}
