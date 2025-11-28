package dao;
import mainmodels.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private final Connection conn;
    public AccountDAO(Connection connection) {this.conn = connection;}

    public int createAccount(int customerId, String accountType, double initialBalance) throws SQLException {
        String sql = "INSERT INTO accounts (account_type, customer_id, balance) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[] {"account_number"})) {
            ps.setString(1, accountType);
            ps.setInt(2, customerId);
            ps.setDouble(3, initialBalance);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to create account");
    }

    // Return list of Account models for a customer
    public List<Account> getAccountsForCustomer(int customerId) throws SQLException {
        String sql = "SELECT account_number, account_type, opening_date, balance FROM accounts WHERE customer_id = ? ORDER BY account_number";
        List<Account> list = new ArrayList<>();
        CustomerDAO custDAO = new CustomerDAO(conn);
        Customer owner = custDAO.getCustomerById(customerId);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int accNo = rs.getInt("account_number");
                    String type = rs.getString("account_type");
                    java.sql.Date od = rs.getDate("opening_date");
                    double bal = rs.getDouble("balance");

                    Account acc = null;
                    if (type.equalsIgnoreCase("savings")) {
                        acc = new SavingsAccount(accNo, "Main Branch", bal, owner);
                    } else if (type.equalsIgnoreCase("cheque")) {
                        acc = new ChequeAccount(accNo, "Main Branch", owner, "N/A");
                        acc.setBalance(bal);
                    } else if (type.equalsIgnoreCase("investment")) {
                        acc = new InvestmentAccount(accNo, "Main Branch", owner, bal);
                    } else {
                        acc = new AccountDTO(accNo, type, od == null ? null : od.toLocalDate(), bal);
                    }

                    if (acc != null) {
                        list.add(acc);
                    }
                }
            }
        }
        return list;
    }

    public void updateBalance(int accountNumber, double balance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, balance);
            ps.setInt(2, accountNumber);
            ps.executeUpdate();
        }

    }
    public int payInterest(int clerkId) throws SQLException {
        int totalUpdated = 0;

        // First, get all interest-bearing accounts that need interest payment
        String selectSql = "SELECT account_number, account_type, balance FROM accounts " +
                "WHERE account_type IN ('savings', 'investment') " +
                "AND balance > 0"; // Only pay interest on positive balances

        String updateSql = "UPDATE accounts SET balance = ? WHERE account_number = ?";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             ResultSet rs = selectStmt.executeQuery()) {

            while (rs.next()) {
                int accountNumber = rs.getInt("account_number");
                String accountType = rs.getString("account_type");
                double currentBalance = rs.getDouble("balance");
                double interestRate = 0.0;
                double interestAmount = 0.0;
                double newBalance = currentBalance;

                // Apply rate based on account type
                if ("savings".equalsIgnoreCase(accountType)) {
                    interestRate = 0.0005; // 0.05% for savings
                    interestAmount = currentBalance * interestRate;
                    newBalance = currentBalance + interestAmount;
                } else if ("investment".equalsIgnoreCase(accountType)) {
                    interestRate = 0.005; // 0.5% for investment
                    interestAmount = currentBalance * interestRate;
                    newBalance = currentBalance + interestAmount;
                }

                // Updating the account balance
                updateStmt.setDouble(1, newBalance);
                updateStmt.setInt(2, accountNumber);
                updateStmt.executeUpdate();
                totalUpdated++;

                // Log the interest payment as a transaction
                logInterestTransaction(accountNumber, clerkId, interestAmount, currentBalance, newBalance, accountType);
            }
        }

        // Log summary of interest payments
        if (totalUpdated > 0) {
            logInterestPaymentSummary(clerkId, totalUpdated);
        }

        return totalUpdated;
    }

    private void logInterestTransaction(int accountNumber, int clerkId, double interestAmount,
                                        double oldBalance, double newBalance, String accountType) throws SQLException {
        // First, get the customer_id for this account
        String customerSql = "SELECT customer_id FROM accounts WHERE account_number = ?";
        int customerId = 0;

        try (PreparedStatement custStmt = conn.prepareStatement(customerSql)) {
            custStmt.setInt(1, accountNumber);
            try (ResultSet rs = custStmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                }
            }
        }

        // Insert the interest payment as a transaction
        String transactionSql = "INSERT INTO transactions (" +
                "transaction_type, t_amount, pre_balance, post_balance, " +
                "account_number, user_id, customer_id, transaction_date" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement transStmt = conn.prepareStatement(transactionSql)) {
            transStmt.setString(1, "INTEREST");
            transStmt.setDouble(2, interestAmount);
            transStmt.setDouble(3, oldBalance);
            transStmt.setDouble(4, newBalance);
            transStmt.setInt(5, accountNumber);
            transStmt.setInt(6, clerkId);
            transStmt.setInt(7, customerId);
            transStmt.executeUpdate();
        }
    }

    public Account getAccountByNumber(int accountNumber) throws SQLException {
        String sql = "SELECT a.*, c.name as customer_name FROM accounts a " +
                "JOIN customers c ON a.customer_id = c.customer_id " +
                "WHERE a.account_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Create and return Account object based on account type
                    String type = rs.getString("account_type");
                    double balance = rs.getDouble("balance");
                    return new AccountDTO(accountNumber, type, null, balance);
                }
            }
        }
        return null;
    }
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_number, account_type, opening_date, customer_id, balance " +
                "FROM accounts ORDER BY account_number";

        // 1. Create the DAO instance OUTSIDE the try block so we can use it
        CustomerDAO customerDAO = new CustomerDAO(conn);

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 2. define variables once inside the loop
                int accNumber = rs.getInt("account_number");
                String type = rs.getString("account_type");
                int customerId = rs.getInt("customer_id");
                double balance = rs.getDouble("balance");

                // 3. Use the INSTANCE 'customerDAO', not the class name 'CustomerDAO'
                Customer owner = customerDAO.getCustomerById(customerId);

                String branch = "Main Branch";
                String employer = null;
                Account account = null;

                if (type.equalsIgnoreCase("cheque")) {
                    account = new ChequeAccount(accNumber, branch, owner, employer);
                }
                else if (type.equalsIgnoreCase("savings")) {
                    account = new SavingsAccount(accNumber, branch, balance, owner);
                }
                else if (type.equalsIgnoreCase("investment")) {
                    account = new InvestmentAccount(accNumber, branch, owner, balance);
                }
                else {
                    System.out.println("Unknown account type in DB: " + type);
                }

                if (account != null) {
                    account.setBalance(balance);
                    accounts.add(account);
                }
            }
        }
        return accounts;
    }
    private void logInterestPaymentSummary(int clerkId, int accountCount) throws SQLException {
        // The SQL has 7 placeholders (?) before CURRENT_TIMESTAMP
        String summarySql = "INSERT INTO transactions (" +
                "transaction_type, t_amount, pre_balance, post_balance, " +
                "account_number, user_id, customer_id, transaction_date" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = conn.prepareStatement(summarySql)) {
            // 1. transaction_type
            stmt.setString(1, "INTEREST_SUMMARY");

            // 2. t_amount (storing the count of accounts updated)
            stmt.setDouble(2, accountCount);

            // 3. pre_balance (N/A for summary, set to 0)
            stmt.setDouble(3, 0);

            // 4. post_balance (N/A for summary, set to 0)
            stmt.setDouble(4, 0);

            // 5. account_number (N/A, set to NULL)
            stmt.setNull(5, java.sql.Types.NUMERIC);

            // 6. user_id (The clerk who clicked the button)
            stmt.setInt(6, clerkId);

            // 7. customer_id (N/A, set to NULL)
            stmt.setNull(7, java.sql.Types.NUMERIC);

            stmt.executeUpdate();
        }
    }


    public static class AccountDTO extends Account {
            private String accountType;
            private LocalDate openingDate;

            public AccountDTO(int accountNumber, String accountType, LocalDate openingDate, double balance) {
                super(accountNumber, null, null, balance);
                this.accountType = accountType;
                this.openingDate = openingDate;
            }

            public String getAccountType() {
                return accountType;
            }

            public LocalDate getOpeningDate() {
                return openingDate;
            }
    }

}

