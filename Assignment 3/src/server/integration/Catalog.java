package server.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import common.exception.DatabaseException;
import server.model.User;

public class Catalog {

	private static final String TABLE_NAME = "user";
	private PreparedStatement createAccount;
	private PreparedStatement checkAccount;
	private PreparedStatement deleteAccount;
	private PreparedStatement checkUsername;

	public Catalog() {
		try {
			Connection connection = this.connection();
			this.prepareStatements(connection);
		} catch (ClassNotFoundException | SQLException exception) {
			throw new DatabaseException("Could not connect to database.");
		}
	}

	private Connection connection() throws ClassNotFoundException, SQLException, DatabaseException {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection("jdbc:mysql://localhost:3307/catalog", "root", "");
	}

	private void prepareStatements(Connection connection) throws SQLException {
		this.createAccount = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES (?, ?)");
		this.checkAccount = connection
				.prepareStatement("SELECT * from " + TABLE_NAME + " WHERE username = ? AND password = ?");
		this.deleteAccount = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE username = ?");
		this.checkUsername = connection.prepareStatement("SELECT * from " + TABLE_NAME + " WHERE username = ?");
	}

	public boolean isAccount(String username, String password) {
		ResultSet result = null;
		try {
			this.checkAccount.setString(1, username);
			this.checkAccount.setString(2, password);
			result = this.checkAccount.executeQuery();
			if (result.next()) {
				return true;
			}
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while checking login.");
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				throw new DatabaseException("Error while checking login.");
			}
		}
		return false;
	}

	public boolean createAccount(String username, String password) {
		try {
			this.checkUsername.setString(1, username);
			ResultSet result = this.checkUsername.executeQuery();
			if (result.next()) {
				return false;
			}
			this.createAccount.setString(1, username);
			this.createAccount.setString(2, password);
			int rows = this.createAccount.executeUpdate();
			return rows == 1;
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while creating account.");
		}
	}

	public boolean deleteAccount(User user) {
		try {
			this.deleteAccount.setString(1, user.getUsername());
			int rows = this.deleteAccount.executeUpdate();
			return rows == 1;
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while removing account.");
		}
	}

}
