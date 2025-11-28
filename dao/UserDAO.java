package dao;

import java.sql.*;

public class UserDAO {
    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    // Returns role (customer/clerk) and userid
    public static class LoginResult {
        public final int userId;
        public final String role;
        public LoginResult(int userId, String role) { this.userId = userId; this.role = role; }
    }

    // Query users table directly (returns null if not found)
    public LoginResult login(String username, String password) {
        String sql = "SELECT user_id, role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LoginResult(rs.getInt("user_id"), rs.getString("role"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create a user and return generated user_id
    public int createUser(String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[] {"user_id"})) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to create user");
    }
}
