package dao;

import model.Cicilan;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CicilanDao — Data Access Object untuk tabel [cicilan].
 *
 * ⭐ FITUR UTAMA: bayarCicilan() menggunakan TRANSACTION ATOMIC.
 *    - Jika insert cicilan BERHASIL dan update sisa pinjaman BERHASIL → COMMIT
 *    - Jika salah satu GAGAL → ROLLBACK (tidak ada data setengah-tersimpan)
 *
 * Ini adalah contoh nyata penggunaan JDBC Transaction Management.
 */
public class CicilanDao {

    // ── SQL Queries ──────────────────────────────────────────────────────────
    private static final String SQL_INSERT =
            "INSERT INTO cicilan (pinjaman_id, jumlah_bayar, tanggal_bayar) " +
            "VALUES (?, ?, GETDATE())";

    private static final String SQL_FIND_BY_PINJAMAN =
            "SELECT id, pinjaman_id, jumlah_bayar, tanggal_bayar " +
            "FROM cicilan WHERE pinjaman_id = ? ORDER BY tanggal_bayar DESC";

    private static final String SQL_TOTAL_BAYAR =
            "SELECT ISNULL(SUM(jumlah_bayar), 0) AS total FROM cicilan WHERE pinjaman_id = ?";

    // ── ORM Mapper ───────────────────────────────────────────────────────────

    private Cicilan mapRow(ResultSet rs) throws SQLException {
        return new Cicilan(
                rs.getInt("id"),
                rs.getInt("pinjaman_id"),
                rs.getBigDecimal("jumlah_bayar"),
                rs.getTimestamp("tanggal_bayar").toLocalDateTime()
        );
    }

    // ── ATOMIC TRANSACTION: Bayar Cicilan ────────────────────────────────────

    /**
     * Proses pembayaran cicilan secara ATOMIC.
     *
     * Alur transaksi:
     *  1. Nonaktifkan auto-commit (mulai transaksi manual)
     *  2. Insert record cicilan baru
     *  3. Update sisa pinjaman (dan status jika lunas)
     *  4. COMMIT jika kedua operasi sukses
     *  5. ROLLBACK jika ada yang gagal
     *  6. Kembalikan status auto-commit ke true
     *
     * @param pinjamanId  ID pinjaman yang dibayar
     * @param jumlahBayar jumlah yang dibayarkan
     * @param sisaSekarang sisa pinjaman saat ini (sebelum bayar)
     * @param pinjamanDao digunakan untuk update sisa (shared connection)
     * @return true jika transaksi berhasil
     */
    public boolean bayarCicilan(int pinjamanId, BigDecimal jumlahBayar,
                                 BigDecimal sisaSekarang, PinjamanDao pinjamanDao) {

        // Validasi: jumlah bayar tidak boleh melebihi sisa
        if (jumlahBayar.compareTo(sisaSekarang) > 0) {
            System.err.println("[CicilanDao] Jumlah bayar melebihi sisa pinjaman!");
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // ── Tahap 1: Mulai transaksi manual ─────────────────────────────
            conn.setAutoCommit(false);

            // ── Tahap 2: Insert cicilan baru ─────────────────────────────────
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                ps.setInt(1, pinjamanId);
                ps.setBigDecimal(2, jumlahBayar);
                ps.executeUpdate();
            }

            // ── Tahap 3: Hitung sisa baru & update pinjaman ──────────────────
            BigDecimal sisaBaru   = sisaSekarang.subtract(jumlahBayar);
            String    statusBaru  = sisaBaru.compareTo(BigDecimal.ZERO) <= 0
                                    ? "LUNAS" : "AKTIF";

            boolean updateOk = pinjamanDao.updateSisaStatus(
                    pinjamanId, sisaBaru, statusBaru, conn);

            if (!updateOk) {
                throw new SQLException("Gagal update sisa pinjaman.");
            }

            // ── Tahap 4: COMMIT ───────────────────────────────────────────────
            conn.commit();
            System.out.println("[CicilanDao] Transaksi COMMIT berhasil.");
            return true;

        } catch (SQLException e) {
            // ── Tahap 5: ROLLBACK jika ada error ─────────────────────────────
            System.err.println("[CicilanDao ERROR] " + e.getMessage() + " → ROLLBACK");
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[CicilanDao] Transaksi di-ROLLBACK.");
                } catch (SQLException rollbackEx) {
                    System.err.println("[CicilanDao ERROR] Rollback gagal: " + rollbackEx.getMessage());
                }
            }
            return false;

        } finally {
            // ── Tahap 6: Kembalikan auto-commit ke true ───────────────────────
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("[CicilanDao ERROR] Reset auto-commit gagal.");
                }
            }
        }
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * Ambil semua cicilan untuk satu pinjaman.
     */
    public List<Cicilan> findByPinjamanId(int pinjamanId) {
        List<Cicilan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PINJAMAN)) {

            ps.setInt(1, pinjamanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[CicilanDao ERROR] findByPinjamanId: " + e.getMessage());
        }
        return list;
    }

    /**
     * Total jumlah yang sudah dibayar untuk satu pinjaman.
     */
    public BigDecimal getTotalBayar(int pinjamanId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_TOTAL_BAYAR)) {

            ps.setInt(1, pinjamanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }

        } catch (SQLException e) {
            System.err.println("[CicilanDao ERROR] getTotalBayar: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
