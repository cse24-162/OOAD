package dao;
import java.sql.*;

public class LoginDAO {
    public String login(String username, String password) {
        String role = "INVALID";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{call login_user(?,?,?)}")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.registerOutParameter(3, Types.VARCHAR);

            stmt.execute();
            role = stmt.getString(3);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return role;
    }
}
