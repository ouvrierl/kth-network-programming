package server.integration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import common.constants.Constants;
import common.exception.DatabaseException;
import common.exception.IOException;
import server.net.ClientHandler;

public class Catalog {

	private static final String TABLE_NAME_USER = "user";
	private static final String TABLE_NAME_FILE = "file";
	private PreparedStatement createAccount;
	private PreparedStatement checkAccount;
	private PreparedStatement deleteAccount;
	private PreparedStatement checkUsername;
	private PreparedStatement addFile;
	private PreparedStatement getFile;
	private PreparedStatement deleteFile;
	private PreparedStatement getOwnerFiles;
	private PreparedStatement updateFile;
	private PreparedStatement getAllFiles;

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
		return DriverManager.getConnection("jdbc:mysql://localhost:3307/catalog?characterEncoding=utf8", "root", "");
	}

	private void prepareStatements(Connection connection) throws SQLException {
		this.createAccount = connection.prepareStatement("INSERT INTO " + TABLE_NAME_USER + " VALUES (?, ?)");
		this.checkAccount = connection
				.prepareStatement("SELECT * from " + TABLE_NAME_USER + " WHERE username = ? AND password = ?");
		this.deleteAccount = connection.prepareStatement("DELETE FROM " + TABLE_NAME_USER + " WHERE username = ?");
		this.checkUsername = connection.prepareStatement("SELECT * from " + TABLE_NAME_USER + " WHERE username = ?");
		this.addFile = connection.prepareStatement("INSERT INTO " + TABLE_NAME_FILE + " VALUES (?, ?, ?, ?, ?)");
		this.getFile = connection.prepareStatement("SELECT * from " + TABLE_NAME_FILE + " WHERE name = ?");
		this.getOwnerFiles = connection.prepareStatement("SELECT * from " + TABLE_NAME_FILE + " WHERE owner = ?");
		this.deleteFile = connection.prepareStatement("DELETE FROM " + TABLE_NAME_FILE + " WHERE name = ?");
		this.updateFile = connection
				.prepareStatement("UPDATE " + TABLE_NAME_FILE + " SET size = ?, access = ?, action = ? WHERE name = ?");
		this.getAllFiles = connection.prepareStatement("SELECT * from " + TABLE_NAME_FILE);
	}

	public boolean isAccount(String username, String password) {
		ResultSet result = null;
		try {
			this.checkAccount.setString(1, username);
			this.checkAccount.setString(2, password);
			result = this.checkAccount.executeQuery();
			if (result.next()) {
				// Ok, match
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
				// Username already used
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

	public boolean addFile(String name, long size, String owner, String access, String action) {
		try {
			this.getFile.setString(1, name);
			ResultSet result = this.getFile.executeQuery();
			if (result.next()) {
				// Name of file already used
				return false;
			}
			this.addFile.setString(1, name);
			this.addFile.setLong(2, size);
			this.addFile.setString(3, owner);
			this.addFile.setString(4, access);
			this.addFile.setString(5, action);
			int rows = this.addFile.executeUpdate();
			return rows == 1;
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while adding file infos in the database.");
		}
	}

	public boolean deleteAccount(String user) {
		try {
			this.getOwnerFiles.setString(1, user);
			ResultSet result = this.getOwnerFiles.executeQuery();
			while (result.next()) {
				// All files are removed on the server system
				String fileName = result.getObject(1).toString();
				File fileToDelete = new File(ClientHandler.FILES_DIRECTORY + fileName);
				if (!fileToDelete.delete()) {
					throw new IOException("Error while deleting the user's files.");
				}
			}
			this.deleteAccount.setString(1, user);
			int rows = this.deleteAccount.executeUpdate();
			// Cascade so it will automatically remove the files of the user on the table file
			return rows == 1;
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while removing account.");
		}
	}

	public boolean deleteFile(String fileName, String user) {
		try {
			this.getFile.setString(1, fileName);
			ResultSet result = this.getFile.executeQuery();
			if (result.next()) {
				String owner = result.getObject(3).toString();
				String access = result.getObject(4).toString();
				Object action = result.getObject(5);
				if (access.equals(Constants.ACCESS_PUBLIC) && action.toString().equals(Constants.ACTION_READ)
						&& !owner.equals(user)) {
					// The user must have the rights to delete the file. Not possible if file public read, and user not the owner
					return false;
				}
			} else {
				// Name of file not in the database
				return false;
			}
			this.deleteFile.setString(1, fileName);
			int rows = this.deleteFile.executeUpdate();
			return rows == 1;
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while removing file.");
		}
	}

	public List<Object[]> getFiles(String user) {
		List<Object[]> files = new ArrayList<>();
		ResultSet result = null;
		try {
			result = this.getAllFiles.executeQuery();
			while (result.next()) {
				Object[] file = new Object[5];
				file[0] = result.getObject(1);
				file[1] = result.getObject(2);
				file[2] = result.getObject(3);
				file[3] = result.getObject(4);
				file[4] = result.getObject(5);
				if (!(file[3].toString().equals(Constants.ACCESS_PRIVATE) && !file[2].toString().equals(user))) {
					// We add the file if public or private with user == owner
					files.add(file);
				}
			}
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while getting files list.");
		} finally {
			try {
				result.close();
			} catch (Exception e) {
				throw new DatabaseException("Error while getting files list.");
			}
		}
		return files;
	}

	public boolean updateFile(String fileName, long length, String user, String access, String action) {
		try {
			this.getFile.setString(1, fileName);
			ResultSet result = this.getFile.executeQuery();
			if (result.next()) {
				String owner = result.getObject(3).toString();
				String accessValue = result.getObject(4).toString();
				Object actionValue = result.getObject(5);
				if (accessValue.equals(Constants.ACCESS_PUBLIC) && actionValue.toString().equals(Constants.ACTION_READ)
						&& !owner.equals(user)) {
					// The user must have the rights to update the file. Not
					// possible if file public read, and user not the owner
					return false;
				} else {
					// Informations about file updated (size, access, action)
					this.updateFile.setLong(1, length);
					this.updateFile.setString(2, access);
					this.updateFile.setString(3, action);
					this.updateFile.setString(4, fileName);
					int rows = this.updateFile.executeUpdate();
					return rows == 1;
				}
			} else {
				// Name of file not found in database
				return false;
			}
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while updating file.");
		}
	}

	public boolean controlNotify(String user, String fileName) {
		try {
			this.getFile.setString(1, fileName);
			ResultSet result = this.getFile.executeQuery();
			if (result.next()) {
				String owner = result.getObject(3).toString();
				String accessValue = result.getObject(4).toString();
				return accessValue.equals(Constants.ACCESS_PUBLIC) && owner.equals(user);
				// User must be the owner to ask notification, and pointless on
				// private file
			} else {
				// Name of file not found in database
				return false;
			}
		} catch (SQLException sqle) {
			throw new DatabaseException("Error while checking for notify in the database.");
		}
	}

}
