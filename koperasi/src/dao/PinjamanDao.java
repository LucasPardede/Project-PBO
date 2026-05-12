package dao;

import model.Pinjaman;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PinjamanDao — Data Access Object untuk tabel [pinjaman].
 *
 * Fitur utama:
 * - Ajukan pinjaman baru
 * - Cari pinjaman per anggota
 * - Update status pinjaman
 */
public class PinjamanDao {

    // ── SQL Queries ──────────────────────────────────────────────────────────
    private static final String SQL_INSERT =
            "INSERT INTO pinjaman (anggota_id, jumlah, sisa, bunga, status, tanggal_pengajuan) " +
            "VALUES (?, ?, ?, ?, 'AKTIF', GETDATE())";

    private static final String SQL_FIND_ALL =
            "SELECT id, anggota_id, jumlah, sisa, bunga, status, tanggal_pengajuan " +
            "FROM pinjaman ORDER BY tanggal_pengajuan DESC";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, anggota_id, jumlah, sisa, bunga, status, tanggal_pengajuan " +
            "FROM pinjaman WHERE id = ?";

    private static final String SQL_FIND_BY_ANGGOTA =
            "SELECT id, anggota_id, jumlah, sisa, bunga, status, tanggal_pengajuan " +
            "FROM pinjaman WHERE anggota_id = ? ORDER BY tanggal_pengajuan DESC";

    private static final String SQL_FIND_AKTIF_BY_ANGGOTA =
            "SELECT id, anggota_id, jumlah, sisa, bunga, status, tanggal_pengajuan " +
            "FROM pinjaman WHERE anggota_id = ? AND status = 'AKTIF'";

    private static final String SQL_UPDATE_SISA_STATUS =
            "UPDATE pinjaman SET sisa = ?, status = ? WHERE id = ?";

    // ── ORM Mapper ───────────────────────────────────────────────────────────

    private Pinjaman mapRow(ResultSet rs) throws SQLException {
        return new Pinjaman(
                rs.getInt("id"),
                rs.getInt("anggota_id"),
                rs.getBigDecimal("jumlah"),
                rs.getBigDecimal("sisa"),
                rs.getBigDecimal("bunga"),
                rs.getString("status"),
                rs.getTimestamp("tanggal_pengajuan").toLocalDateTime()
        );
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    public Pinjaman save(Pinjaman pinjaman) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pinjaman.getAnggotaId());
            ps.setBigDecimal(2, pinjaman.getJumlah());
            ps.setBigDecimal(3, pinjaman.getSisa());
            ps.setBigDecimal(4, pinjaman.getBunga());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) pinjaman.setId(keys.getInt(1));
                }
                return pinjaman;
            }

        } catch (SQLException e) {
            System.err.println("[PinjamanDao ERROR] save: " + e.getMessage());
        }
        return null;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Pinjaman> findAll() {
        List<Pinjaman> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[PinjamanDao ERROR] findAll: " + e.getMessage());
        }
        return list;
    }

    public Optional<Pinjaman> findById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[PinjamanDao ERROR] findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Pinjaman> findByAnggotaId(int anggotaId) {
        List<Pinjaman> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ANGGOTA)) {

            ps.setInt(1, anggotaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[PinjamanDao ERROR] findByAnggotaId: " + e.getMessage());
        }
        return list;
    }

    public List<Pinjaman> findAktifByAnggotaId(int anggotaId) {
        List<Pinjaman> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_AKTIF_BY_ANGGOTA)) {

            ps.setInt(1, anggotaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[PinjamanDao ERROR] findAktifByAnggotaId: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Update sisa pinjaman dan status (dipanggil dari CicilanDao dalam transaksi).
     */
    public boolean updateSisaStatus(int id, BigDecimal sisaBaru, String status, Connection conn)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_SISA_STATUS)) {
            ps.setBigDecimal(1, sisaBaru);
            ps.setString(2, status);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }
}
