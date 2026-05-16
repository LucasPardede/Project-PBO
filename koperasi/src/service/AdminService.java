package service;

import dao.AdminDao;
import model.Admin;

import java.util.Optional;

/**
 * AdminService — Business Logic untuk autentikasi admin.
 *
 * Layer Service bertugas:
 * - Validasi input sebelum ke DAO
 * - Orchestrasi beberapa DAO jika perlu
 * - Tidak tahu detail JDBC (itu urusan DAO)
 */
public class AdminService {

    private final AdminDao adminDao;

    public AdminService() {
        this.adminDao = new AdminDao();
    }

    /**
     * Login admin: validasi input lalu panggil DAO.
     *
     * @param username username yang diinput
     * @param password password yang diinput
     * @return Optional<Admin> jika berhasil login
     */
    public Optional<Admin> login(String username, String password) {
        // Validasi input
        if (username == null || username.trim().isEmpty()) {
            System.out.println("[AdminService] Username tidak boleh kosong.");
            return Optional.empty();
        }
        if (password == null || password.trim().isEmpty()) {
            System.out.println("[AdminService] Password tidak boleh kosong.");
            return Optional.empty();
        }

        // Delegasi ke DAO
        Optional<Admin> admin = adminDao.authenticate(username.trim(), password.trim());
        if (admin.isEmpty()) {
            System.out.println("[AdminService] Username atau password salah.");
        }
        return admin;
    }
}
