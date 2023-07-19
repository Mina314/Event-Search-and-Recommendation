package db.mongoDB;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MongoDBUtil {
    public static final String DB_NAME = "firstproject";
    private static final String MONGODB_URI = "mongodb://localhost:27017/";

    private static MongoClient mongoClient;
    private static MongoDatabase db;

    static {
        try {
            MongoClientURI connectionString = new MongoClientURI(MONGODB_URI);
            mongoClient = new MongoClient(connectionString);
            db = mongoClient.getDatabase(DB_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MongoDatabase getDB() {
        return db;
    }
}
