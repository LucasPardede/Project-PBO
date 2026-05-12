package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection — Singleton JDBC connection ke SQL Server (SSMS).
 *
 * Tahapan JDBC:
 *  1. Load driver  → Class.forName(...)
 *  2. Buat koneksi → DriverManager.getConnection(...)
 *  3. Eksekusi query dilakukan di DAO
 *  4. Ambil ResultSet dilakukan di DAO
 *  5. Close resource dilakukan di setiap DAO (try-with-resources)
 *
 * Ubah HOST, PORT, DB_NAME, USER, PASSWORD sesuai konfigurasi SSMS kamu.
 */
public class DBConnection {

    // ── Konfigurasi koneksi SQL Server ──────────────────────────────────────
    private static final String HOST     = "localhost\\SQLEXPRESS";
    private static final String PORT     = "1433";
    private static final String DB_NAME  = "KoperasiDB";
    private static final String USER     = "sa";          // ganti sesuai login SSMS
    private static final String PASSWORD = "siantar222"; // ganti password kamu

    /**
     * JDBC URL untuk SQL Server.
     * trustServerCertificate=true → agar tidak perlu setup SSL certificate di lokal.
     * encrypt=true                → enkripsi koneksi.
     */
    private static final String URL =
            "jdbc:sqlserver://" + HOST + ":" + PORT + ";"
            + "databaseName=" + DB_NAME + ";"
            + "user=" + USER + ";"
            + "password=" + PASSWORD + ";"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    // ── Driver class SQL Server ─────────────────────────────────────────────
  private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    // ── Singleton instance ──────────────────────────────────────────────────
    private static Connection connection = null;

    /** Private constructor → cegah instantiasi langsung. */
    private DBConnection() {}

    /**
     * Mengembalikan satu instance Connection (Singleton).
     * Jika koneksi tutup / null → buat baru.
     *
     * @return Connection ke SQL Server
     */
    public static Connection getConnection() {
        try {
            // Tahap 1 — Load driver
            Class.forName(DRIVER_CLASS);

            // Tahap 2 — Buat atau re-open koneksi
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL);
                System.out.println("[DB] Koneksi ke SQL Server berhasil.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB ERROR] Driver tidak ditemukan: " + e.getMessage());
            System.err.println("           Pastikan mssql-jdbc-*.jar ada di classpath.");
            throw new RuntimeException("Driver JDBC tidak ditemukan.", e);
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Gagal konek ke SQL Server: " + e.getMessage());
            System.err.println("           Cek HOST/PORT/USER/PASSWORD di DBConnection.java");
            throw new RuntimeException("Koneksi database gagal.", e);
        }
        return connection;
    }

    /**
     * Tutup koneksi global saat aplikasi selesai.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[DB] Koneksi ditutup.");
                }
            } catch (SQLException e) {
                System.err.println("[DB ERROR] Gagal menutup koneksi: " + e.getMessage());
            }
        }
    }
}
