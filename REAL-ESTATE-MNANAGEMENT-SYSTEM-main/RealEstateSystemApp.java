import java.time.LocalDate;
import java.util.*;
import java.sql.*;

class Property {
    private String propId;
    private String address;
    private String propType;
    private double price;
    private boolean available;
    private List<Map<String, String>> tenants;

    public Property(String propId, String address, String propType, double price) {
        this.propId = propId;
        this.address = address;
        this.propType = propType;
        this.price = price;
        this.available = true;
        this.tenants = new ArrayList<>();
    }

    // Getters and setters
    public String getPropId() { return propId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPropType() { return propType; }
    public void setPropType(String propType) { this.propType = propType; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public List<Map<String, String>> getTenants() { return tenants; }

    @Override
    public String toString() {
        String status = available ? "Available" : "Occupied";
        return "ID: " + propId + ", Address: " + address + ", Type: " + propType + ", Price: $" + price + ", Status: " + status;
    }
}

class Tenant {
    private String tenantId;
    private String name;
    private String contact;

    public Tenant(String tenantId, String name, String contact) {
        this.tenantId = tenantId;
        this.name = name;
        this.contact = contact;
    }

    // Getters
    public String getTenantId() { return tenantId; }
    public String getName() { return name; }
    public String getContact() { return contact; }

    @Override
    public String toString() {
        return "ID: " + tenantId + ", Name: " + name + ", Contact: " + contact;
    }
}

class RealEstateSystem {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/realestate_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root"; 
    private static final String PASSWORD = "Arijeet@123";

    private Map<String, Property> properties;
    private Map<String, Tenant> tenants;
    private List<Map<String, Object>> rentals; // List of maps: {"propId": id, "tenantId": id, "startDate": date, "endDate": date}

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public void addProperty(String propId, String address, String propType, double price) {
        String sql = "INSERT INTO Property (propId, address, propType, price, available) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, propId);
            pstmt.setString(2, address);
            pstmt.setString(3, propType);
            pstmt.setDouble(4, price);
            pstmt.setBoolean(5, true); 

            pstmt.executeUpdate();
            System.out.println("Property added successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Property ID already exists.");
        } catch (SQLException e) {
            System.out.println("Database error adding property: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void viewProperties() {
        String sql = "SELECT propId, address, propType, price, available FROM Property";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!rs.isBeforeFirst()) { 
                System.out.println("No properties available.");
                return;
            }

            while (rs.next()) {
                String propId = rs.getString("propId");
                String address = rs.getString("address");
                String propType = rs.getString("propType");
                double price = rs.getDouble("price");
                boolean available = rs.getBoolean("available");

                String status = available ? "Available" : "Occupied";
                System.out.println("ID: " + propId + ", Address: " + address + ", Type: " + propType + ", Price: $" + price + ", Status: " + status);
            }
        } catch (SQLException e) {
            System.out.println("Database error viewing properties: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateProperty(String propId, String field, String value) {
        String sql = "UPDATE Property SET %s = ? WHERE propId = ?";
        String updateField;
        int paramType; // 1=String, 2=Double, 3=Boolean

        switch (field.toLowerCase()) {
            case "address":
                updateField = "address";
                paramType = 1;
                break;
            case "proptype":
                updateField = "propType";
                paramType = 1;
                break;
            case "price":
                updateField = "price";
                paramType = 2;
                break;
            case "available":
                updateField = "available";
                paramType = 3;
                break;
            default:
                System.out.println("Invalid field.");
                return;
        }

        sql = String.format(sql, updateField);

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Set value parameter based on type
            if (paramType == 1) pstmt.setString(1, value);
            else if (paramType == 2) pstmt.setDouble(1, Double.parseDouble(value));
            else if (paramType == 3) pstmt.setBoolean(1, Boolean.parseBoolean(value));
            
            pstmt.setString(2, propId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Property updated.");
            } else {
                System.out.println("Property not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid price format.");
        } catch (SQLException e) {
            System.out.println("Database error updating property: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteProperty(String propId) {
        String sqlDeleteProperty = "DELETE FROM Property WHERE propId = ?";
        String sqlDeleteRentals = "DELETE FROM Rental WHERE propId = ?";
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete associated rentals first (due to foreign key constraint)
            try (PreparedStatement pstmtRentals = conn.prepareStatement(sqlDeleteRentals)) {
                pstmtRentals.setString(1, propId);
                pstmtRentals.executeUpdate();
            }

            // 2. Delete the property
            try (PreparedStatement pstmtProperty = conn.prepareStatement(sqlDeleteProperty)) {
                pstmtProperty.setString(1, propId);
                int affectedRows = pstmtProperty.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit(); // Commit transaction if successful
                    System.out.println("Property deleted.");
                } else {
                    conn.rollback(); // Rollback if property not found
                    System.out.println("Property not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error deleting property: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addTenant(String tenantId, String name, String contact) {
        String sql = "INSERT INTO Tenant (tenantId, name, contact) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tenantId);
            pstmt.setString(2, name);
            pstmt.setString(3, contact);

            pstmt.executeUpdate();
            System.out.println("Tenant added successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Tenant ID already exists.");
        } catch (SQLException e) {
            System.out.println("Database error adding tenant: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void assignTenant(String propId, String tenantId) {
        String sqlCheckProperty = "SELECT available FROM Property WHERE propId = ?";
        String sqlCheckTenant = "SELECT tenantId FROM Tenant WHERE tenantId = ?";
        String sqlUpdateProperty = "UPDATE Property SET available = FALSE WHERE propId = ?";
        String sqlInsertRental = "INSERT INTO Rental (propId, tenantId, startDate) VALUES (?, ?, CURDATE())";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Check if Property and Tenant exist/available
            boolean propExists = false;
            boolean tenantExists = false;
            boolean isAvailable = false;

            try (PreparedStatement pstmtCheckProp = conn.prepareStatement(sqlCheckProperty)) {
                pstmtCheckProp.setString(1, propId);
                try (ResultSet rs = pstmtCheckProp.executeQuery()) {
                    if (rs.next()) {
                        propExists = true;
                        isAvailable = rs.getBoolean("available");
                    }
                }
            }

            try (PreparedStatement pstmtCheckTenant = conn.prepareStatement(sqlCheckTenant)) {
                pstmtCheckTenant.setString(1, tenantId);
                try (ResultSet rs = pstmtCheckTenant.executeQuery()) {
                    if (rs.next()) {
                        tenantExists = true;
                    }
                }
            }

            if (!propExists || !tenantExists) {
                System.out.println("Property or tenant not found.");
                conn.rollback();
                return;
            }
            if (!isAvailable) {
                System.out.println("Property is already occupied.");
                conn.rollback();
                return;
            }
            
            // 2. Update Property status
            try (PreparedStatement pstmtUpdateProp = conn.prepareStatement(sqlUpdateProperty)) {
                pstmtUpdateProp.setString(1, propId);
                pstmtUpdateProp.executeUpdate();
            }

            // 3. Insert new Rental record
            try (PreparedStatement pstmtInsertRental = conn.prepareStatement(sqlInsertRental)) {
                pstmtInsertRental.setString(1, propId);
                pstmtInsertRental.setString(2, tenantId);
                pstmtInsertRental.executeUpdate();
            }

            conn.commit(); // Commit transaction
            System.out.println("Tenant assigned.");

        } catch (SQLException e) {
            System.out.println("Database error assigning tenant: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void viewRentals() {
        // Joining Rental and Tenant tables for a friendly view
        String sql = "SELECT r.propId, r.tenantId, t.name, r.startDate, r.endDate FROM Rental r JOIN Tenant t ON r.tenantId = t.tenantId ORDER BY r.startDate DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (!rs.isBeforeFirst()) { 
                System.out.println("No rentals recorded.");
                return;
            }

            while (rs.next()) {
                String propId = rs.getString("propId");
                String tenantId = rs.getString("tenantId");
                String tenantName = rs.getString("name");
                java.sql.Date startDate = rs.getDate("startDate"); 
                java.sql.Date endDate = rs.getDate("endDate");

                String end = endDate != null ? endDate.toString() : "Ongoing";
                System.out.println("Property " + propId + " rented to Tenant " + tenantName + " (ID: " + tenantId + ") from " + startDate + " to " + end);
            }
        } catch (SQLException e) {
            System.out.println("Database error viewing rentals: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void searchProperties(String propType, Boolean available) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT propId, address, propType, price, available FROM Property WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (propType != null && !propType.isEmpty()) {
            sqlBuilder.append(" AND propType = ?");
            params.add(propType);
        }
        if (available != null) {
            sqlBuilder.append(" AND available = ?");
            params.add(available);
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) param);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No matching properties.");
                    return;
                }

                while (rs.next()) {
                    String propId = rs.getString("propId");
                    String address = rs.getString("address");
                    String type = rs.getString("propType");
                    double price = rs.getDouble("price");
                    boolean avail = rs.getBoolean("available");

                    String status = avail ? "Available" : "Occupied";
                    System.out.println("ID: " + propId + ", Address: " + address + ", Type: " + type + ", Price: $" + price + ", Status: " + status);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error searching properties: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

public class RealEstateSystemApp {
    public static void main(String[] args) {
        RealEstateSystem system = new RealEstateSystem();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Real Estate Management System ---");
            System.out.println("1. Add Property");
            System.out.println("2. View Properties");
            System.out.println("3. Update Property");
            System.out.println("4. Delete Property");
            System.out.println("5. Add Tenant");
            System.out.println("6. Assign Tenant to Property");
            System.out.println("7. View Rentals");
            System.out.println("8. Search Properties");
            System.out.println("9. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Property ID: ");
                    String propId = scanner.nextLine();
                    System.out.print("Address: ");
                    String address = scanner.nextLine();
                    System.out.print("Type (e.g., Apartment): ");
                    String propType = scanner.nextLine();
                    System.out.print("Price: ");
                    double price = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    system.addProperty(propId, address, propType, price);
                    break;
                case 2:
                    system.viewProperties();
                    break;
                case 3:
                    System.out.print("Property ID: ");
                    propId = scanner.nextLine();
                    System.out.print("Field to update (address, proptype, price, available): ");
                    String field = scanner.nextLine();
                    System.out.print("New value: ");
                    String value = scanner.nextLine();
                    system.updateProperty(propId, field, value);
                    break;
                case 4:
                    System.out.print("Property ID: ");
                    propId = scanner.nextLine();
                    system.deleteProperty(propId);
                    break;
                case 5:
                    System.out.print("Tenant ID: ");
                    String tenantId = scanner.nextLine();
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Contact: ");
                    String contact = scanner.nextLine();
                    system.addTenant(tenantId, name, contact);
                    break;
                case 6:
                    System.out.print("Property ID: ");
                    propId = scanner.nextLine();
                    System.out.print("Tenant ID: ");
                    tenantId = scanner.nextLine();
                    system.assignTenant(propId, tenantId);
                    break;
                case 7:
                    system.viewRentals();
                    break;
                case 8:
                    System.out.print("Property type (leave blank for any): ");
                    propType = scanner.nextLine();
                    if (propType.isEmpty()) propType = null;
                    System.out.print("Available (true/false, leave blank for any): ");
                    String availInput = scanner.nextLine();
                    Boolean available = availInput.isEmpty() ? null : Boolean.parseBoolean(availInput);
                    system.searchProperties(propType, available);
                    break;
                case 9:
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
