import java.sql.*;

public class CheckDb {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tourly_db", "tourly_user", "Tourly@123");
            Statement stmt = conn.createStatement();
            
            System.out.println("--- USERS ---");
            ResultSet rs = stmt.executeQuery("SELECT u.id, u.full_name, u.email, u.phone, u.account_status, r.role_name FROM users u JOIN roles r ON u.role_id = r.id");
            while (rs.next()) {
                System.out.println(String.format("User ID: %d, Name: %s, Email: %s, Phone: %s, Status: %s, Role: %s",
                    rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
                    rs.getString("phone"), rs.getString("account_status"), rs.getString("role_name")));
            }
            
            System.out.println("\n--- TRIPS ---");
            rs = stmt.executeQuery("SELECT id, title, destination, base_price, planner_id FROM trips");
            while (rs.next()) {
                System.out.println(String.format("Trip ID: %d, Title: %s, Destination: %s, Price: %f, Planner ID: %d",
                    rs.getLong("id"), rs.getString("title"), rs.getString("destination"),
                    rs.getDouble("base_price"), rs.getLong("planner_id")));
            }
            
            System.out.println("\n--- BOOKINGS ---");
            rs = stmt.executeQuery("SELECT id, user_id, trip_id, total_amount, status FROM bookings");
            while (rs.next()) {
                System.out.println(String.format("Booking ID: %d, User ID: %d, Trip ID: %d, Amount: %f, Status: %s",
                    rs.getLong("id"), rs.getLong("user_id"), rs.getLong("trip_id"),
                    rs.getDouble("total_amount"), rs.getString("status")));
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
