import java.sql.Connection;
import java.sql.DriverManager;

public class JDBCDemo {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
        String user = "SYSTEM";
        String password = "NewPass123";

        try (Connection con =
                 DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to Oracle successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}