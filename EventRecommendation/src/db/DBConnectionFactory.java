package db;

import db.mysql.MySQLConnection;

public class DBConnectionFactory {

	private static final String DEFAULT_DB = "mysql";

	public static DBConnection getConnection(final String db) {
		switch (db) {
		case "mysql": {
			return (DBConnection) new MySQLConnection();
		}
		case "mongodb": {
			return null;
		}
		default:
			break;
		}
		throw new IllegalArgumentException("Invalid db: " + db);
	}

	public static DBConnection getConnection() throws IllegalArgumentException {
		return getConnection(DEFAULT_DB);
	}

}
