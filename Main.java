// Compile with: javac -cp .;lib\mysql-connector-j-8.0.33.jar Main.java
// Run with: java -cp .;lib\mysql-connector-j-8.0.33.jar Main
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        // 1. MySQL connection details
        String url = "jdbc:mysql://localhost:3306/event_planner";
        String username = "root";
        String password = "ritika17x22"; // add your MySQL password here

        try {
            // 2. Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 3. Connect to the database
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Connected to database!");

            // 4. Query the events table
            String query = "SELECT * FROM events";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // 5. Display the results
            System.out.println("📅 Events:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("event_id") +
                                   ", Title: " + rs.getString("title") +
                                   ", Date: " + rs.getDate("event_date") +
                                   ", Location: " + rs.getString("location"));
            }

            // 6. Close everything
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
