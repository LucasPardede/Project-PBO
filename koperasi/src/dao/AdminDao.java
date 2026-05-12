package dao;

import model.Admin;
import util.DBConnection;

import java.sql.*;
import java.util.Optional;

/**
 * AdminDao — Data Access Object untuk tabel [admin].
 *
 * Implementasi JDBC manual (Data Mapper pattern):
 *   - Semua query menggunakan PreparedStatement (SQL injection safe)
 *   - Semua resource ditutup dengan try-with-resources
 */
public class AdminDao {

    // ── SQL Queries ──────────────────────────────────────────────────────────
    private static final String SQL_FIND_BY_USERNAME =
            "SELECT id, username, password FROM admin WHERE username = ?";

    private static final String SQL_FIND_BY_CREDENTIALS =
            "SELECT id, username, password FROM admin WHERE username = ? AND password = ?";

    // ── Mapper: ResultSet → Admin object ────────────────────────────────────

    /**
     * ORM Manual Mapping: konversi baris ResultSet menjadi object Admin.
     */
    private Admin mapRow(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password")
        );
    }

    // ── Public Methods ───────────────────────────────────────────────────────

    /**
     * Cari admin berdasarkan username saja (cek keberadaan).
     *
     * @param username username yang dicari
     * @return Optional<Admin> — kosong jika tidak ditemukan
     */
    public Optional<Admin> findByUsername(String username) {
        // try-with-resources → Connection, PreparedStatement, ResultSet otomatis ditutup
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USERNAME)) {

            ps.setString(1, username);  // Tahap 3: set parameter PreparedStatement

            try (ResultSet rs = ps.executeQuery()) {  // Tahap 4: eksekusi & ambil ResultSet
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminDao ERROR] findByUsername: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Autentikasi admin: cocokkan username + password.
     *
     * @param username username
     * @param password password (plain text — di production gunakan hashing)
     * @return Optional<Admin> — kosong jika credentials salah
     */
    public Optional<Admin> authenticate(String username, String password) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_CREDENTIALS)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AdminDao ERROR] authenticate: " + e.getMessage());
        }
        return Optional.empty();
    }
}
