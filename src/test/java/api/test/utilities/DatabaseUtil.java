package api.test.utilities;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.StringUtil;
import org.h2.tools.Server;

import api.test.entities.SuiteDataDBEntity;
import api.test.entities.SuiteInfoDBEntity;
import groovy.json.StringEscapeUtils;

public class DatabaseUtil {
	private static DatabaseUtil instance;
	private PropertyUtil pu = new PropertyUtil();
	private Server server;
	private Connection connection;
	private String databaseName = pu.getPropAsString("DATABASE_NAME");
	// private String embeddedPath = pu.getPropAsString("EMBEDDED_PATH");
	private String serverPath = pu.getPropAsString("SERVER_PATH");
	private String currentBranch = "UAT";
	// private String jdbc1 = embeddedPath + databaseName;
	private String jdbc2 = serverPath + databaseName;
	private String username = "sa";
	private String password = "1234";

	// Private constructor to prevent instantiation
	private DatabaseUtil() {
		// Initialize the connection
		getServer();
		getConnection();
	}

	// Public method to provide access to the instance
	public static DatabaseUtil getInstance() {
		if (instance == null) {
			synchronized (DatabaseUtil.class) { // Ensure thread safety
				if (instance == null) {
					instance = new DatabaseUtil();
				}
			}
		}
		return instance;
	}

	/** Method to establish a connection **/
	private void getConnection() {
		try {
			this.connection = DriverManager.getConnection(jdbc2, username, password);
			if (checkConnection()) {
				System.out.println("[DatabaseUtil] Connected to h2 database");
			}
		}
		catch (SQLException e) {
			System.err.println("[DatabaseUtil] Error while trying to get connection: " + e.getMessage());
		}
	}

	private void getServer() {
		try {
			this.server = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers").start();
		}
		catch (SQLException e) {
			System.err.println("[DatabaseUtil] Error while trying to get server: " + e.getMessage());
		}
	}

	public void setCurrentBranch(String branch) {
		if (branch.equals("PROD"))
			this.currentBranch = "";
	}

	private void createTable() {
		try {
			Statement statement = this.connection.createStatement();
			// Create table SuiteInfo
			statement.execute("CREATE TABLE SUITEINFO (\n" + //
					"    ID CHARACTER VARYING(255) NOT NULL PRIMARY KEY,\n" + //
					"    MODE CHARACTER VARYING(255),\n" + //
					"    PASSEDCOUNT INTEGER DEFAULT 0,\n" + //
					"    FAILEDCOUNT INTEGER DEFAULT 0,\n" + //
					"    SKIPPEDCOUNT INTEGER DEFAULT 0,\n" + //
					"    STARTDATE TIMESTAMP,\n" + //
					"    ENDDATE TIMESTAMP,\n" + //
					"    TOTALTIME CHARACTER VARYING(255),\n" + //
					"    SUITENAME CHARACTER VARYING(255)\n" + //
					");\n" + //
					"");
			// Create table SuiteData
			statement.execute("CREATE TABLE SUITEDATA (\n" + //
					"    ID INTEGER AUTO_INCREMENT PRIMARY KEY,\n" + //
					"    TYPE CHARACTER VARYING(255),\n" + //
					"    SUITEID INTEGER,\n" + //
					"    TESTNAME CHARACTER VARYING(255),\n" + //
					"    METHOD CHARACTER VARYING(255),\n" + //
					"    RESULTLIST CHARACTER VARYING(255),\n" + //
					"    STATUS CHARACTER VARYING(255),\n" + //
					"    TIMEESLAPSED CHARACTER VARYING(255),\n" + //
					"    EXCEPTION CLOB,\n" + //
					"    DESCRIPTION CHARACTER VARYING(255)\n" + //
					");");
			statement.closeOnCompletion();
		}
		catch (SQLException e) {
			System.err.println("[DatabaseUtil] Error while trying to create tables: " + e.getMessage());
		}
	}

	/**
	 * Insert to <b>table.[?][SuiteData]</b> with given values
	 * <p>
	 * <b>?</b> can be empty -> <b>main</b> or "UAT" -> <b>sub</b>
	 * </p>
	 * <p>
	 * Columns in order: {@code TYPE}, {@code SUITEID}, {@code TESTNAME},{@code METHOD}, {@code RESULTLIST}, {@code STATUS},
	 * {@code TIMEESLAPSED},{@code EXCEPTION},{@code DESCRIPTION}
	 * </p>
	 **/
	public void insertSuiteDataList(List<SuiteDataDBEntity> entityList) {
		if (checkConnection())
			try {
				if (entityList.size() < 1) {
					System.out.println("[DatabaseUtil] SuiteDataDBEntity list is empty");

				} else {
					String query = " INSERT INTO " + this.currentBranch
							+ "SUITEDATA VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					PreparedStatement pstmt = connection.prepareStatement(query);
					for (SuiteDataDBEntity entity : entityList) {
						pstmt.setString(1, entity.getType());
						pstmt.setInt(2, entity.getSuiteId());
						pstmt.setString(3, entity.getTestName());
						pstmt.setString(4, entity.getMethod());
						pstmt.setString(5, entity.getResultList());
						pstmt.setString(6, entity.getStatus());
						pstmt.setString(7, entity.getTimeElapsed());
						pstmt.setCharacterStream(8,
								new StringReader(StringEscapeUtils.escapeJava(entity.getException())));
						pstmt.setString(9, entity.getDescription());
						pstmt.executeUpdate();
					}
					pstmt.closeOnCompletion();

				}
			}
			catch (SQLException e) {
				System.err.println(
						"[DatabaseUtil] table.[SuiteData] Error while trying to insert: " + e.getMessage());
			}
	}

	/**
	 * Insert to <b>table.[?][SuiteInfo]</b> with given values
	 * <p>
	 * <b>?</b> can be empty -> <b>main</b> or "UAT" -> <b>sub</b>
	 * </p>
	 * <p>
	 * Columns in order: {@code ID}, {@code MODE}, {@code PASSEDCOUNT},{@code FAILEDCOUNT}, {@code SKIPPEDCOUNT},
	 * {@code STARTDATE}, {@code ENDDATE},{@code TOTALTIME}
	 * </p>
	 **/
	public void insertSuiteInfo(SuiteInfoDBEntity entity) {
		if (checkConnection())
			try {
				// values = addApostrophe(values);
				if (entity == null) {
					System.out.println("[DatabaseUtil] SuiteInfoDBEntity is empty");
				} else {
					String query = " INSERT INTO " + this.currentBranch
							+ "SUITEINFO VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
					PreparedStatement pstmt = connection.prepareStatement(query);
					pstmt.setInt(1, entity.getId());
					pstmt.setString(2, entity.getMode());
					pstmt.setInt(3, entity.getPassedCount());
					pstmt.setInt(4, entity.getFailedCount());
					pstmt.setInt(5, entity.getSkippedCount());
					pstmt.setString(6, entity.getStartDate());
					pstmt.setString(7, entity.getEndDate());
					pstmt.setString(8, entity.getTotalTime());
					pstmt.setString(9, entity.getSuiteName());
					pstmt.executeUpdate();
					pstmt.closeOnCompletion();

				}
			}
			catch (SQLException e) {
				System.err.println(
						"[DatabaseUtil] table.[SuiteInfo] Error while trying to insert: " + e.getMessage());
			}
	}

	public List<SuiteDataDBEntity> getRecords(String inputSuiteName, String inputDateTime) {
		try {
			String suiteInfoQuery = " SELECT * FROM " + this.currentBranch + " where STARTDATE = '"
					+ inputDateTime + "' and SUITENAME = '" + inputSuiteName + "'";

			// String suiteDataQuery = " SELECT 8 FROM " + this.currentBranch + " where SUITEID = " +

			List<SuiteDataDBEntity> entityList = new ArrayList<SuiteDataDBEntity>();

			PreparedStatement pstmt = connection.prepareStatement(suiteInfoQuery);
			ResultSet resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

			}
			pstmt.close();
			return entityList;
		}
		catch (SQLException e) {
			System.err.println("[DatabaseUtil] Error while trying to insert: " + e.getMessage());
			return null;
		}

	}

	/**
	 * Add apostrophes between strings seperated by {@code commas}
	 * <p>
	 * Example: string1,string2
	 * </p>
	 * 
	 * @return 'string1','string2'
	 **/
	public String addApostrophe(String input) {
		StringBuilder result = new StringBuilder();
		boolean isComma = false;
		result.append("'");
		for (int i = 0; i < input.length(); i++) {
			char currentChar = input.charAt(i);
			if (input.charAt(i) == ',') {
				isComma = true;
				result.append("'");
			} else if (isComma) {
				isComma = false;
				result.append("'");
			}
			result.append(currentChar);
		}
		result.append("'");
		return result.toString();
	}

	private boolean checkConnection() {
		if (this.connection != null && this.server != null)
			return true;
		else
			return false;
	}

	public void closeConnection() {
		try {
			if (checkConnection()) {
				System.out.println("[DatabaseUtil] Closing connection...");
				this.connection.close();
				this.server.stop();
			}
			// Server.shutdownTcpServer("tcp://localhost:9092", password, true, true);
		}
		catch (SQLException e) {
			System.err.println("[DatabaseUtil] Error while trying to close connection: " + e.getMessage());
		}
	}
}
