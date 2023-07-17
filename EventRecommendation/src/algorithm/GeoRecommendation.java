package algorithm;

import java.util.Set;
import db.DBConnection;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import db.DBConnectionFactory;
import java.util.ArrayList;
import entity.Item;
import java.util.List;

public class GeoRecommendation {

	public List<Item> recommendItems(String userId, double lat, double lon) {
		// Step 1, Get all favorited items
		List<Item> recommendedItems = new ArrayList<>();
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		
		// Step 2, Get all categories of favorite items, sort by count
		Map<String, Integer> allCategories = new HashMap<>();
		for (String itemId : favoritedItemIds) {
			Set<String> categories = connection.getCategories(itemId);
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		
		List<Map.Entry<String, Integer>> categoryList = new ArrayList<Map.Entry<String, Integer>>(
				allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(final Map.Entry<String, Integer> o1, final Map.Entry<String, Integer> o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});
		
		// Step 3, do search based on category, filter out favorite events, sort by distance
		Set<Item> visitedItems = new HashSet<>();
		for (Map.Entry<String, Integer> category : categoryList) {
			List<Item> items = connection.searchItems(lat, lon, category.getKey());
			List<Item> filteredItems = new ArrayList<Item>();
			for (Item item : items) {
				if (!favoritedItemIds.contains(item.getItemId()) && !visitedItems.contains(item)) {
                    filteredItems.add(item);
				}
			}
			
			Collections.sort(filteredItems, new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});
			
			visitedItems.addAll(items);
			recommendedItems.addAll(filteredItems);
		}
		connection.close();
		return recommendedItems;
	}
}
