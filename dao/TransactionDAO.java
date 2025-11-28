package dao;
import dao.AccountDAO.AccountDTO;
import mainmodels.Account;
import mainmodels.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleTypes;
public class TransactionDAO {
    private final Connection conn;

    public TransactionDAO(Connection conn) {
        this.conn = conn;
    }

    // Using stored procedure perform_transaction
    public void performTransaction(int customerId, int accountNumber, int userId, String type, double amount) throws SQLException {
        String call = "{call perform_transaction(?, ?, ?, ?, ?)}";
        try (CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, customerId);
            cs.setInt(2, accountNumber);
            cs.setInt(3, userId);
            cs.setString(4, type);
            cs.setDouble(5, amount);
            cs.execute();
        }
    }

    // Get transactions for an account via stored proc that returns SYS_REFCURSOR
    public List<Transaction> getTransactionsForAccount(int accountNumber) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, a.account_type FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.account_number = ? ORDER BY t.transaction_date DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }

    // transactions for a customer (by customer id)
    public List<Transaction> getTransactionsForCustomer(int customerId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, a.account_type FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "WHERE t.customer_id = ? ORDER BY t.transaction_date DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return list;
    }
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        int accNo = rs.getInt("account_number");

        String accType = rs.getString("account_type");

        // We use AccountDTO (defined in AccountDAO) to hold the type string
        Account accountInfo = new AccountDTO(accNo, accType, null, 0.0);

        return new Transaction(
                String.valueOf(rs.getInt("transaction_id")),
                rs.getTimestamp("transaction_date").toLocalDateTime(),
                rs.getString("transaction_type"),
                rs.getDouble("t_amount"),
                accountInfo, // Pass the account object containing the type
                rs.getDouble("pre_balance"),
                rs.getDouble("post_balance")
        );
    }
    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, a.account_type FROM transactions t " +
                "JOIN accounts a ON t.account_number = a.account_number " +
                "ORDER BY t.transaction_date DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTransaction(rs));
            }
        }
        return list;
    }
}
