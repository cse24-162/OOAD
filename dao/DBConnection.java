package dao;
import java.sql.*;

public class DBConnection {
    private static final String host = "jdbc:oracle:thin:@db.freesql.com:1521/23ai_mb9q7";
    private static final String username = "SQL_QZWLW2SSSGU37TZG3HVCMKTQFN";
    private static final String password = "XN0B57LGJ6!RDYWP41S8W1tRUJIWYD";

    private static Connection connection;

    private DBConnection(){}

    public static Connection getConnection() {
        try{
            if (connection == null || connection.isClosed()) {
                try {
                    connection = DriverManager.getConnection(host, username, password);
                    System.out.println("Connected to Oracle database");
                } catch (SQLException e) {
                    System.out.println("Failed to connect to Oracle database");
                    e.printStackTrace();
                    return null;
                }
            }
        }catch(SQLException e) {
            System.out.println("Failed to connect to Oracle database, attempting to reconnect");
            try {
                connection = DriverManager.getConnection(host, username, password);
                System.out.println("Connected to Oracle database after status check error.");
            } catch (SQLException connectE) {
                System.out.println("Failed to connect to Oracle database after status check error.");
                connectE.printStackTrace();
                return null;
            }
        }
        return connection;
    }
}
