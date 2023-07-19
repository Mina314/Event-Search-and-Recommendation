package db.mongoDB;


import static com.mongodb.client.model.Filters.eq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import db.DBConnection;
import entity.Item;
import entity.Item.*;
import external.TicketMasterAPI;

public class MongoDBConnection implements DBConnection {
	
	private static MongoDBConnection instance;

	public static DBConnection getInstance() {
		if (instance == null) {
			instance = new MongoDBConnection();
		}
		return instance;
	}
	
	private MongoClient mongoClient;
	private MongoDatabase db;
	
	public MongoDBConnection() {
		// Connects to MongoDB server using the MongoDBUtil class.
        db = MongoDBUtil.getDB();
	}

	@Override
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$push", new Document("favorite", new Document("$each", itemIds))));
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pull", new Document("favorite", new Document("$each", itemIds))));
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> favoriteItems = new HashSet<String>();
		// db.users.find({user_id:1111})
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null && iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteItems.addAll(list);
			}
		return favoriteItems;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		Set<Item> favoriteItems = new HashSet<>();
				Set<String> itemIds = getFavoriteItemIds(userId);
				for (String itemId : itemIds) {
				FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id",itemId));
				if (iterable.first() != null) {
				Document doc = iterable.first();
				ItemBuilder builder = new ItemBuilder();
				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setAddress(doc.getString("address"));
				builder.setUrl(doc.getString("url"));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setRating(doc.getDouble("rating"));
				builder.setDistance(doc.getDouble("distance"));
				builder.setCategories(getCategories(itemId));
				favoriteItems.add(builder.build());
				}
				}
				return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));

		if (iterable.first() != null && iterable.first().containsKey("categories")){
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		 UpdateOptions options = new UpdateOptions().upsert(true);
		    db.getCollection("items").updateOne(
		        new Document("item_id", item.getItemId()),
		        new Document("$set", new Document()
		            .append("item_id", item.getItemId())
		            .append("name", item.getName())
		            .append("rating", item.getRating())
		            .append("address", item.getAddress())
		            .append("image_url", item.getImageUrl())
		            .append("url", item.getUrl())
		            .append("categories", item.getCategories())
		        ),
		        options
		    );

	}

	@Override
	public String getFullname(String userId) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null) {
			Document doc = iterable.first();
			return doc.getString("first_name") + " " + doc.getString("last_name");
			}
			return "";
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null) {
			Document doc = iterable.first();
			return doc.getString("password").equals(password);
		}
			return false;
	}

	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if (this.db == null) {
	        System.err.println("DB connection failed");
	        return false;
	    }

	    try {
	        Document newUser = new Document("user_id", userId)
	                .append("password", password)
	                .append("firstname", firstname)
	                .append("lastname", lastname);

	        db.getCollection("users").insertOne(newUser);

	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}

}
