package db;

import db.mysql.MySQLConnection;
import db.mongoDB.MongoDBConnection;

public class DBConnectionFactory {

	private static final String DEFAULT_DB = "mysql"; // Change the default to "mongodb" here

	public static DBConnection getConnection(final String db) {
		switch (db) {
		case "mysql": {
			return (DBConnection) new MySQLConnection();
		}
		case "mongodb": {
			return (DBConnection) new MongoDBConnection();
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
