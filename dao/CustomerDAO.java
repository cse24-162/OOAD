package dao;
import mainmodels.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private static Connection conn;

    public CustomerDAO(Connection connection) {
        this.conn = connection;
    }

    public Connection getConnection() {
        return conn;
    }
    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    // Fetch customer by customer_id and return appropriate subclass
    /*public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, name, contact_number, address, customer_type FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String type = rs.getString("customer_type");
                String name = rs.getString("name");
                long contact = rs.getString("contact_number") == null ? 0L : Long.parseLong(rs.getString("contact_number"));
                String address = rs.getString("address");

                if ("individual".equalsIgnoreCase(type)) {
                    // load individual-specific fields
                    String indSql = "SELECT date_of_birth, employername, employeraddress FROM individualcustomer WHERE customer_id = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(indSql)) {
                        ps2.setInt(1, customerId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            java.util.Date dob = null;
                            String employer = null;
                            String empAddr = null;
                            if (rs2.next()) {
                                Date d = rs2.getDate("date_of_birth");
                                if (d != null) dob = new java.util.Date(d.getTime());
                                employer = rs2.getString("employername");
                                empAddr = rs2.getString("employeraddress");
                            }
                            IndividualCustomer c = new IndividualCustomer(name, contact, address, dob, employer, empAddr);
                            c.setCustomerID(customerId);
                            return c;
                        }
                    }
                } else { // company
                    String compSql = "SELECT companyname, registrationnumebr, contactperson FROM companycustomer WHERE customer_id = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(compSql)) {
                        ps2.setInt(1, customerId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            String companyName = null;
                            long regNo = 0L;
                            String contactPerson = null;
                            if (rs2.next()) {
                                companyName = rs2.getString("companyname");
                                regNo = rs2.getLong("registrationnumebr");
                                contactPerson = rs2.getString("contactperson");
                            }
                            CompanyCustomer cc = new CompanyCustomer(companyName, regNo, contactPerson, name, contact, address);
                            cc.setCustomerID(customerId);
                            return cc;
                        }
                    }
                }
            }
        }
    }*/

    // Create customer (caller should have created Users row and pass user_id)
    // Returns id
    public static int createCustomer(Customer customer, String customerType, int userId) throws SQLException {
        String sql = "INSERT INTO customers (name, contact_number, address, user_id, customer_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[] {"customer_id"})) {
            ps.setString(1, customer.getName());
            ps.setString(2, String.valueOf(customer.getContactNumber()));
            ps.setString(3, customer.getAddress());
            ps.setInt(4, userId);
            ps.setString(5, customerType);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int custId = keys.getInt(1);

                    // insert subtype table row
                    if (customer instanceof IndividualCustomer) {
                        IndividualCustomer ic = (IndividualCustomer) customer;
                        String indSql = "INSERT INTO individualcustomer (customer_id, name, date_of_birth, employername, employeraddress) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement ps2 = conn.prepareStatement(indSql)) {
                            ps2.setInt(1, custId);
                            ps2.setString(2, ic.getName());
                            if (ic.getDateOfBirth() != null) ps2.setDate(3, new java.sql.Date(ic.getDateOfBirth().getTime()));
                            else ps2.setNull(3, Types.DATE);
                            ps2.setString(4, ic.getEmployerName());
                            ps2.setString(5, ic.getEmployerAddress());
                            ps2.executeUpdate();
                        }
                    } else if (customer instanceof CompanyCustomer) {
                        CompanyCustomer cc = (CompanyCustomer) customer;
                        String compSql = "INSERT INTO companycustomer (customer_id, companyname, registrationnumebr, contactperson) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ps2 = conn.prepareStatement(compSql)) {
                            ps2.setInt(1, custId);
                            ps2.setString(2, cc.getCompanyName());
                            ps2.setLong(3, cc.getRegistrationNumber());
                            ps2.setString(4, cc.getContactPerson());
                            ps2.executeUpdate();
                        }
                    }

                    return custId;
                } else throw new SQLException("No customer_id returned");
            }
        }
    }
    public Customer getCustomerByUserId(int userId) {
        String sql = "SELECT * FROM Customers WHERE user_id = ?";
        try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            int customerId = rs.getInt("customer_id");
            String type = rs.getString("customer_type");
            String name = rs.getString("name");
            long contact = Long.parseLong(rs.getString("contact_number"));
            String address = rs.getString("address");

            if (type.equalsIgnoreCase("INDIVIDUAL")) {
                String indSql = "SELECT * FROM IndividualCustomer WHERE customer_id = ?";
                try (PreparedStatement indStmt = this.conn.prepareStatement(indSql)) {
                    indStmt.setInt(1, customerId);
                    ResultSet irs = indStmt.executeQuery();
                    if (irs.next()) {
                        IndividualCustomer ic = new IndividualCustomer(
                                customerId,
                                name,
                                contact,
                                address,
                                userId,
                                irs.getDate("date_of_birth"),
                                irs.getString("employerName"),
                                irs.getString("employerAddress")
                        );
                        // Failsafe: Set ID explicitly
                        ic.setCustomerID(customerId);
                        return ic;
                    }
                }
            } else if (type.equalsIgnoreCase("COMPANY")) {
                String compSql = "SELECT * FROM CompanyCustomer WHERE customer_id = ?";
                try (PreparedStatement compStmt = this.conn.prepareStatement(compSql)) {
                    compStmt.setInt(1, customerId);
                    ResultSet crs = compStmt.executeQuery();
                    if (crs.next()) {
                        CompanyCustomer cc = new CompanyCustomer(
                                customerId,
                                name,
                                contact,
                                address,
                                userId,
                                crs.getString("companyName"),
                                crs.getLong("RegistrationNumebr"),
                                crs.getString("ContactPerson")
                        );
                        // Failsafe: Set ID explicitly
                        cc.setCustomerID(customerId);
                        return cc;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*public Customer getCustomerByUserId(int userId) {
        String sql = "SELECT * FROM Customers WHERE user_id = ?";
        try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            int customerId = rs.getInt("customer_id");
            String type = rs.getString("customer_type");
            String name = rs.getString("name");
            long contact = Long.parseLong(rs.getString("contact_number"));
            String address = rs.getString("address");

            if (type.equalsIgnoreCase("INDIVIDUAL")) {
                String indSql = "SELECT * FROM IndividualCustomer WHERE customer_id = ?";
                try (PreparedStatement indStmt = this.conn.prepareStatement(indSql)) {
                    indStmt.setInt(1, customerId);
                    ResultSet irs = indStmt.executeQuery();
                    if (irs.next()) {
                        return new IndividualCustomer(
                                customerId,
                                name,
                                contact,
                                address,
                                userId,
                                irs.getDate("date_of_birth"),
                                irs.getString("employerName"),
                                irs.getString("employerAddress")
                        );
                    }
                }
            } else if (type.equalsIgnoreCase("COMPANY")) {
                String compSql = "SELECT * FROM CompanyCustomer WHERE customer_id = ?";
                try (PreparedStatement compStmt = this.conn.prepareStatement(compSql)) {
                    compStmt.setInt(1, customerId);
                    ResultSet crs = compStmt.executeQuery();
                    if (crs.next()) {
                        return new CompanyCustomer(
                                customerId,
                                name,
                                contact,
                                address,
                                userId,
                                crs.getString("companyName"),
                                crs.getLong("RegistrationNumebr"),
                                crs.getString("ContactPerson")
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/


    public Customer getCustomerByAccountNumber(int accountNumber) throws SQLException {
        String sql = "SELECT c.customer_id FROM customers c " +
                "JOIN accounts a ON c.customer_id = a.customer_id " +
                "WHERE a.account_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int customerId = rs.getInt("customer_id");
                    return getCustomerById(customerId);
                }
            }
        }
        return null;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id FROM customers ORDER BY customer_id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int customerId = rs.getInt("customer_id");
                Customer c = getCustomerById(customerId);
                if (c != null) customers.add(c);
            }
        }

        return customers;
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, name, contact_number, address, customer_type FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String type = rs.getString("customer_type");
                String name = rs.getString("name");
                long contact = rs.getString("contact_number") == null ? 0L : Long.parseLong(rs.getString("contact_number"));
                String address = rs.getString("address");

                if ("individual".equalsIgnoreCase(type)) {
                    // load individual-specific fields
                    String indSql = "SELECT date_of_birth, employername, employeraddress FROM individualcustomer WHERE customer_id = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(indSql)) {
                        ps2.setInt(1, customerId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            java.util.Date dob = null;
                            String employer = null;
                            String empAddr = null;
                            if (rs2.next()) {
                                Date d = rs2.getDate("date_of_birth");
                                if (d != null) dob = new java.util.Date(d.getTime());
                                employer = rs2.getString("employername");
                                empAddr = rs2.getString("employeraddress");
                            }
                            IndividualCustomer c = new IndividualCustomer(name, contact, address, dob, employer, empAddr);
                            c.setCustomerID(customerId);
                            return c;
                        }
                    }
                } else { // company
                    String compSql = "SELECT companyname, registrationnumebr, contactperson FROM companycustomer WHERE customer_id = ?";
                    try (PreparedStatement ps2 = conn.prepareStatement(compSql)) {
                        ps2.setInt(1, customerId);
                        try (ResultSet rs2 = ps2.executeQuery()) {
                            String companyName = null;
                            long regNo = 0L;
                            String contactPerson = null;
                            if (rs2.next()) {
                                companyName = rs2.getString("companyname");
                                regNo = rs2.getLong("registrationnumebr");
                                contactPerson = rs2.getString("contactperson");
                            }
                            CompanyCustomer cc = new CompanyCustomer(companyName, regNo, contactPerson, name, contact, address);
                            cc.setCustomerID(customerId);
                            return cc;
                        }
                    }
                }
            }
        }
    }

}
