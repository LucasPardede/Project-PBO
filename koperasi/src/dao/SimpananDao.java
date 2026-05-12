package dao;

import model.Simpanan;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SimpananDao — Data Access Object untuk tabel [simpanan].
 *
 * Fitur:
 * - Tambah simpanan (setor)
 * - Hitung total saldo per anggota
 * - Lihat riwayat simpanan
 */
public class SimpananDao {

    // ── SQL Queries ──────────────────────────────────────────────────────────
    private static final String SQL_INSERT =
            "INSERT INTO simpanan (anggota_id, jumlah, tanggal, keterangan) " +
            "VALUES (?, ?, GETDATE(), ?)";

    private static final String SQL_FIND_ALL =
            "SELECT id, anggota_id, jumlah, tanggal, keterangan FROM simpanan ORDER BY tanggal DESC";

    private static final String SQL_FIND_BY_ANGGOTA =
            "SELECT id, anggota_id, jumlah, tanggal, keterangan FROM simpanan " +
            "WHERE anggota_id = ? ORDER BY tanggal DESC";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, anggota_id, jumlah, tanggal, keterangan FROM simpanan WHERE id = ?";

    private static final String SQL_TOTAL_SALDO =
            "SELECT ISNULL(SUM(jumlah), 0) AS total FROM simpanan WHERE anggota_id = ?";

    // ── ORM Mapper ───────────────────────────────────────────────────────────

    private Simpanan mapRow(ResultSet rs) throws SQLException {
        return new Simpanan(
                rs.getInt("id"),
                rs.getInt("anggota_id"),
                rs.getBigDecimal("jumlah"),
                rs.getTimestamp("tanggal").toLocalDateTime(),
                rs.getString("keterangan")
        );
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    /**
     * Catat setoran simpanan baru.
     *
     * @param simpanan object Simpanan yang akan disimpan
     * @return Simpanan dengan ID, atau null jika gagal
     */
    public Simpanan save(Simpanan simpanan) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, simpanan.getAnggotaId());
            ps.setBigDecimal(2, simpanan.getJumlah());
            ps.setString(3, simpanan.getKeterangan());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) simpanan.setId(keys.getInt(1));
                }
                return simpanan;
            }

        } catch (SQLException e) {
            System.err.println("[SimpananDao ERROR] save: " + e.getMessage());
        }
        return null;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * Ambil semua riwayat simpanan.
     */
    public List<Simpanan> findAll() {
        List<Simpanan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[SimpananDao ERROR] findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Ambil semua simpanan milik anggota tertentu.
     *
     * @param anggotaId ID anggota
     * @return List Simpanan
     */
    public List<Simpanan> findByAnggotaId(int anggotaId) {
        List<Simpanan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ANGGOTA)) {

            ps.setInt(1, anggotaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[SimpananDao ERROR] findByAnggotaId: " + e.getMessage());
        }
        return list;
    }

    /**
     * Cari simpanan berdasarkan ID.
     */
    public Optional<Simpanan> findById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[SimpananDao ERROR] findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Hitung total saldo simpanan seorang anggota.
     *
     * @param anggotaId ID anggota
     * @return total saldo dalam BigDecimal
     */
    public BigDecimal getTotalSaldo(int anggotaId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_TOTAL_SALDO)) {

            ps.setInt(1, anggotaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }

        } catch (SQLException e) {
            System.err.println("[SimpananDao ERROR] getTotalSaldo: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
