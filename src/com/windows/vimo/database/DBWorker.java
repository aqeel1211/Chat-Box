package com.windows.vimo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class DBWorker {

	/** The name of the MySQL account to use (or empty for anonymous) */
	private final String userName = "root";

	/** The password for the MySQL account (or empty for anAnymous) */
	private final String password = "daunting786";

	/** The name of the computer running MySQL */
	private final String serverName = "localhost";

	/** The port of the MySQL server (default is 3306) */
	private final int portNumber = 3306;

	/**
	 * The name of the database we are testing with (this default is installed with
	 * MySQL)
	 */
	private final String dbName = "CREDENTIALS";

	/** The name of the table we are testing with */
	private final String table1 = "loginInfo";

	private Statement stmt;

	/**
	 * Get a new database connection
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.userName);
		connectionProps.put("password", this.password);
		System.out.println("trying to get connection!! ");
		conn = DriverManager.getConnection(
				"jdbc:mysql://" + this.serverName + ":" + this.portNumber + "/" + this.dbName, connectionProps);
		System.out.println(" Connection achieved!! ");
		return conn;
	}

	public boolean executeUpdate(Connection conn, String command) throws SQLException {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(command); // This will throw a SQLException if it fails
			return true;
		} finally {

			// This will run whether we throw an exception or not
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void MakeDB(Connection conn) {
		stmt = null;
		String sql = "CREATE DATABASE CREDENTIALS";
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {

		// Connect to MySQL
		Connection conn = null;
		try {
			conn = this.getConnection();
			System.out.println("connection name is :: " + conn.getClass().getName());
			System.out.println("Connected to database");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
			return;
		}

		// Create tables
		try {

			String createString1 = "CREATE TABLE " + "if not exists " + this.table1 + " ( "
					+ "CredentialID INTEGER NOT NULL PRIMARY KEY KEY AUTO_INCREMENT, "
					+ "Username varchar(40) NOT NULL, " + "Password varchar(40) NOT NULL)";

			this.executeUpdate(conn, createString1);
			System.out.println(createString1 + "created");

		} catch (Exception e) {
			System.out.println("ERROR: Could not create the table");
			e.printStackTrace();
			return;
		}

	}

	public boolean addUserInTable(Connection conn, String username, String password) throws SQLException {
		boolean exist = false;
		String query = "select * from loginInfo";
		Statement stmt = conn.createStatement();
		ResultSet rs1 = stmt.executeQuery(query);
		while (rs1.next()) {
			if (rs1.getString("Username").equalsIgnoreCase(username)) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			createUserTable(username, conn); // if a new user is registered a table will be created for the specified
												// user
			query = " insert into loginInfo (CredentialID, Username, Password)" + " values (?,?,?)";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setInt(1, 0);
			preparedStmt.setString(2, username);
			preparedStmt.setString(3, password);
			preparedStmt.execute();
			return true;

		} else {
			System.out.println("same user already exists");
			return false;
		}
	}

	public void createUserTable(String username2, Connection conn) {
		// TODO Auto-generated method stub
		try {

			String createString1 = "CREATE TABLE " + username2 + " ( "
					+ "ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, " + "friend_Username varchar(40) NOT NULL)";

			this.executeUpdate(conn, createString1);
			System.out.println(createString1 + "created");

		} catch (Exception e) {
			System.out.println("ERROR: Could not create the user table");
			e.printStackTrace();
			return;
		}
	}

	public boolean checkInTable(Connection conn, String username, String password) throws SQLException {
		boolean exist = false;
		String query = "select * from loginInfo";
		Statement stmt = conn.createStatement();
		ResultSet rs1 = stmt.executeQuery(query);
		while (rs1.next()) {
			if (rs1.getString("Username").equalsIgnoreCase(username) && rs1.getString("Password").equals(password)) {
				exist = true;
				break;
			}
		}
		return exist;
	}

	

}
