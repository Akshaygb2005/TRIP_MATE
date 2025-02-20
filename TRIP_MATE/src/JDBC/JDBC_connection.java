package JDBC;
import java.sql.*;
/* Class to connect java to mysql*/
public class JDBC_connection {
    Connection con = null;

    public Connection get_connection(String database) {
        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish Connection
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/"+database+"?serverTimezone=UTC",
                    "root",
                    "sgqbl"
            );

            // Print success message (optional)
            //System.out.println("✅ Database connected successfully!");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
        return con;
    }
}
