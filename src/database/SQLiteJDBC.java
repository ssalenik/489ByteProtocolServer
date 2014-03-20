package database;

import java.sql.*;
import java.util.ArrayList;

import logging.LogLevel;
import logging.Logfile;

import java.util.Date;
import java.text.SimpleDateFormat;

public class SQLiteJDBC implements IResource {

	public static final String USERS_TABLENAME = "users";

	private Connection c;
	private static SimpleDateFormat sdf;

	static {
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	public SQLiteJDBC() {
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:./db/test.db");
			Logfile.writeToFile("Database opened successfully", LogLevel.DEBUG);

			checkSchema();
			Logfile.writeToFile("Database schema verified", LogLevel.DEBUG);

		} catch (Exception e) {
			Logfile.writeToFile("Could not open the database", LogLevel.CRITICAL);
			e.printStackTrace();
			shutdownDueToDatabaseError();
		}
	}

	private void insertBaseUser() {
		try {
			Statement stmt = c.createStatement();

			String sql = "INSERT INTO users (USERNAME, PASSWORD, LASTACCESS) VALUES ('admin', 'admin', '2013-12-09');";
			stmt.executeUpdate(sql);

			stmt.close();
		} catch (Exception e) {
			Logfile.writeToFile("Failed to insert the base user",
					LogLevel.ERROR);
			shutdownDueToDatabaseError();
		}
	}

	private void shutdownDueToDatabaseError() {
		Logfile.writeToFile("Shutdown due to DB error", LogLevel.CRITICAL);
		System.exit(-1);
	}

	public static String getDate() {
		Date d = new Date();
		return sdf.format(d);
	}

	private void createTables() {
		try {
			Statement stmt = c.createStatement();
			String sql = "CREATE TABLE users "
					+ "(ID 			INTEGER 	PRIMARY KEY	AUTOINCREMENT,"
					+ " USERNAME		TEXT 				NOT NULL,"
					+ " PASSWORD		TEXT				NOT NULL,"
					+ " LASTACCESS	TEXT				NOT NULL)";

			stmt.executeUpdate(sql);

			stmt.close();
		} catch (Exception e) {
			Logfile.writeToFile("Failed to create the tables", LogLevel.CRITICAL);
			shutdownDueToDatabaseError();
		}
	}

	private void checkSchema() {
		try {
			Statement stmt = c.createStatement();
			String sql = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='"
					+ USERS_TABLENAME + "'; ";
			ResultSet rs = stmt.executeQuery(sql);
			int count = rs.getInt(1);
			rs.close();

			if (count > 0) {
				// table exists
				stmt.close();
				return;
			} else {
				stmt.close();
				createTables();
				insertBaseUser();
			}

		} catch (Exception e) {
			Logfile.writeToFile(
					"Failed to check the schema of the current database",
					LogLevel.CRITICAL);
			shutdownDueToDatabaseError();
		}
	}

	public void CloseDBConnection() {
		try {
			c.close();
		} catch (Exception e) {
			Logfile.writeToFile("Failed to close db connection", LogLevel.CRITICAL);
		}
	}

	/*
	 * --------------------- Interface implementations
	 * ------------------------------
	 */
	public synchronized boolean login(String user, String pass) {
		try {
			String sql = "SELECT COUNT(*) FROM users WHERE USERNAME = '" + user
					+ "' AND PASSWORD='" + pass + "'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			int count = rs.getInt(1);
			stmt.close();

			if (count > 0) {
				Logfile.writeToFile("User " + user + " logged in",
						LogLevel.INFO);

				String sql2 = " UPDATE users SET LASTACCESS = '" + getDate()
						+ "' WHERE USERNAME = '" + user + "'; ";
				// Make a new prepared statement
				stmt = c.prepareStatement(sql2);
				stmt.executeUpdate();
				stmt.close();

				return true;
			} else {
				Logfile.writeToFile("User " + user
						+ " either doesn't exist or input a bad password", LogLevel.ERROR);
				return false;
			}

		} catch (Exception e) {
			Logfile.writeToFile("User " + user
					+ " failed to login due to sql exception", LogLevel.ERROR);
			return false;
		}
	}

	public synchronized boolean createUser(String user, String password) {
		try {
			String sqlCheck = "SELECT COUNT(*) as total FROM users WHERE USERNAME='" + user + "'";
			PreparedStatement stmtCheck = c.prepareStatement(sqlCheck);
			
			ResultSet rs = stmtCheck.executeQuery();
			boolean found = rs.getInt("total") > 0;
			rs.close();
			
			if (!found) {
				String sql = "INSERT INTO users (USERNAME, PASSWORD, LASTACCESS) VALUES ('"
						+ user + "','" + password + "','" + getDate() + "');";
				PreparedStatement stmt = c.prepareStatement(sql);
				if (stmt.executeUpdate() > 0) {
					stmt.close();
					return true;
				} else {
					stmt.close();
					return false;
				}
			} else return false;
		} catch (Exception e) {
			Logfile.writeToFile("Failed to insert new user", LogLevel.ERROR);
			return false;
		}
	}

	public synchronized boolean deleteUser(String user) {
		try {
			try {
				String sql_primary = "DROP TABLE IF EXISTS '" + user + "_data';";
				PreparedStatement stmt_primary = c.prepareStatement(sql_primary);
				stmt_primary.executeUpdate();
				stmt_primary.close();
			} catch (Exception ep) {
				Logfile.writeToFile("Failed to delete the user " + user + "'s store, probably because it doesn't exist", LogLevel.ERROR);
			}
			
			String sql = "DELETE FROM users WHERE USERNAME = '" + user + "';";
			PreparedStatement stmt = c.prepareStatement(sql);
			if (stmt.executeUpdate() > 0) {
				stmt.close();
				return true;
			} else {
				stmt.close();
				return false;
			}
		} catch (Exception e) {
			Logfile.writeToFile("Failed to delete user " + user, LogLevel.ERROR);
			return false;
		}
	}

	public synchronized boolean userDataTableExists(String username) {
		try {
			String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='"
					+ username + "_data';";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			boolean exists = rs.getInt(1) > 0;
			rs.close();
			stmt.close();
			return exists;
		} catch (Exception e) {
			Logfile.writeToFile("Failed to check if user data table exists",
					LogLevel.ERROR);
			return false;
		}
	}

	public synchronized boolean createUserDataTable(String username) {
		try {
			if (userDataTableExists(username)) {
				return false;
			} else {
				String sql = "CREATE TABLE " + username + "_data"
						+ "(ID 			INTEGER 	PRIMARY KEY	AUTOINCREMENT,"
						+ " USERNAME		TEXT				NOT NULL,"
						+ " MESSAGE			TEXT				NOT NULL,"
						+ " TIME			TEXT				NOT NULL)";
				PreparedStatement stmt = c.prepareStatement(sql);
				stmt.executeUpdate();
				stmt.close();
				return true;
			}
		} catch (Exception e) {
			Logfile.writeToFile(
					"Failed to create the user data table due to error",
					LogLevel.ERROR);
			return false;
		}
	}
	
	public synchronized boolean sendMessageToUser(String dUsername, String sUsername, String message){
		
		try {
			String sql = "INSERT INTO " + dUsername + "_data (USERNAME, MESSAGE, TIME) VALUES ('" + sUsername + "','" + message + "','" + getDate() + "');";
			PreparedStatement stmt = c.prepareStatement(sql);
			stmt.executeUpdate();
			stmt.close();
			return true;
		} catch (Exception e) {
			Logfile.writeToFile("Failed to send message from user " + sUsername + " to " + dUsername, LogLevel.ERROR);
			return false;
		}
	}
	
	public UserMessage[] getNewUserMessages(String username){
		try {
			String sql = "SELECT USERNAME,MESSAGE,TIME from " + username + "_data";
			PreparedStatement query = c.prepareStatement(sql);
			ResultSet rs = query.executeQuery();
			ArrayList<UserMessage> ums = new ArrayList<UserMessage>();
			while (rs.next()) {
				ums.add(new UserMessage(rs.getString(1), rs.getString(2), rs.getString(3)));
			}
			rs.close();
			query.close();
			
			try {
				String sql_clear = "DELETE FROM " + username + "_data";
				PreparedStatement clear = c.prepareStatement(sql_clear);
				clear.executeUpdate();
				clear.close();
			} catch (Exception e2) {
				Logfile.writeToFile("Failed to clear messages for user " + username, LogLevel.ERROR);
			}
			
			return ums.toArray(new UserMessage[ums.size()]);
		} catch (Exception e) {
			Logfile.writeToFile("Failed to query messages for user " + username, LogLevel.ERROR);
			return new UserMessage[0];
		}
	}
}
