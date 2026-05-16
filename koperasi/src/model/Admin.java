package model;

/**
 * Entity: Admin
 * Memetakan tabel [admin] di SQL Server.
 *
 * ORM Manual Mapping → setiap atribut = kolom tabel.
 */
public class Admin {

    // ── Kolom tabel ─────────────────────────────────────────────────────────
    private int    id;
    private String username;
    private String password;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Admin() {}

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Admin(int id, String username, String password) {
        this.id       = id;
        this.username = username;
        this.password = password;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int    getId()       { return id; }
    public void   setId(int id) { this.id = id; }

    public String getUsername()              { return username; }
    public void   setUsername(String u)      { this.username = u; }

    public String getPassword()              { return password; }
    public void   setPassword(String p)      { this.password = p; }

    // ── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Admin{id=" + id + ", username='" + username + "'}";
    }
}
