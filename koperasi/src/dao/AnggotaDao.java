package dao;

import model.Anggota;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AnggotaDao — Data Access Object untuk tabel [anggota].
 *
 * CRUD lengkap dengan:
 * - PreparedStatement (keamanan SQL injection)
 * - try-with-resources (close otomatis)
 * - ORM Manual Mapping (mapRow)
 */
public class AnggotaDao {

    // ── SQL Queries ──────────────────────────────────────────────────────────
    private static final String SQL_INSERT =
            "INSERT INTO anggota (nama, alamat, no_telepon, tanggal_bergabung) " +
            "VALUES (?, ?, ?, ?)";

    private static final String SQL_FIND_ALL =
            "SELECT id, nama, alamat, no_telepon, tanggal_bergabung FROM anggota ORDER BY id";

    private static final String SQL_FIND_BY_ID =
            "SELECT id, nama, alamat, no_telepon, tanggal_bergabung FROM anggota WHERE id = ?";

    private static final String SQL_FIND_BY_NAMA =
            "SELECT id, nama, alamat, no_telepon, tanggal_bergabung FROM anggota " +
            "WHERE nama LIKE ?";

    private static final String SQL_UPDATE =
            "UPDATE anggota SET nama = ?, alamat = ?, no_telepon = ? WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM anggota WHERE id = ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM anggota";

    // ── ORM Mapper: ResultSet → Anggota ─────────────────────────────────────

    /**
     * Konversi satu baris ResultSet menjadi object Anggota.
     * Inilah inti ORM Manual (Data Mapper Pattern).
     */
    private Anggota mapRow(ResultSet rs) throws SQLException {
        return new Anggota(
                rs.getInt("id"),
                rs.getString("nama"),
                rs.getString("alamat"),
                rs.getString("no_telepon"),
                rs.getDate("tanggal_bergabung").toLocalDate()
        );
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    /**
     * Simpan Anggota baru ke database.
     * Menggunakan RETURN_GENERATED_KEYS untuk mendapat ID auto-increment.
     *
     * @param anggota object Anggota yang akan disimpan
     * @return Anggota dengan ID yang sudah di-set, atau null jika gagal
     */
    public Anggota save(Anggota anggota) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            // Tahap 3: Set parameter PreparedStatement
            ps.setString(1, anggota.getNama());
            ps.setString(2, anggota.getAlamat());
            ps.setString(3, anggota.getNoTelepon());
            ps.setDate(4, Date.valueOf(
                    anggota.getTanggalBergabung() != null
                    ? anggota.getTanggalBergabung()
                    : LocalDate.now()
            ));

            int rowsAffected = ps.executeUpdate();

            // Tahap 4: Ambil generated key (ID baru)
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        anggota.setId(generatedKeys.getInt(1));
                    }
                }
                return anggota;
            }

        } catch (SQLException e) {
            System.err.println("[AnggotaDao ERROR] save: " + e.getMessage());
        }
        return null;
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    /**
     * Ambil semua anggota.
     *
     * @return List berisi semua Anggota
     */
    public List<Anggota> findAll() {
        List<Anggota> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[AnggotaDao ERROR] findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Cari Anggota berdasarkan ID.
     *
     * @param id primary key anggota
     * @return Optional<Anggota>
     */
    public Optional<Anggota> findById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[AnggotaDao ERROR] findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Cari Anggota berdasarkan nama (LIKE search, case-insensitive).
     *
     * @param keyword kata kunci pencarian nama
     * @return List Anggota yang cocok
     */
    public List<Anggota> findByNama(String keyword) {
        List<Anggota> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_NAMA)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[AnggotaDao ERROR] findByNama: " + e.getMessage());
        }
        return list;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Update data Anggota (nama, alamat, telepon).
     *
     * @param anggota object Anggota dengan data terbaru (id harus ada)
     * @return true jika berhasil update
     */
    public boolean update(Anggota anggota) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, anggota.getNama());
            ps.setString(2, anggota.getAlamat());
            ps.setString(3, anggota.getNoTelepon());
            ps.setInt(4, anggota.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AnggotaDao ERROR] update: " + e.getMessage());
        }
        return false;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    /**
     * Hapus Anggota berdasarkan ID.
     * Catatan: CASCADE DELETE akan menghapus simpanan & pinjaman terkait.
     *
     * @param id ID anggota yang akan dihapus
     * @return true jika berhasil
     */
    public boolean delete(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AnggotaDao ERROR] delete: " + e.getMessage());
        }
        return false;
    }

    // ── UTILITY ──────────────────────────────────────────────────────────────

    /**
     * Hitung total jumlah anggota.
     *
     * @return jumlah anggota
     */
    public int count() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[AnggotaDao ERROR] count: " + e.getMessage());
        }
        return 0;
    }
}
