package db.mysql;

import external.TicketMasterAPI;
import entity.Item;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.DriverManager;
import java.sql.Connection;
import db.DBConnection;

public class MySQLConnection implements DBConnection {

	private Connection conn;

	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (this.conn != null) {
			try {
				this.conn.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	
	@Override
	public void setFavoriteItems(final String userId, final List<String> itemIds) {
        if (this.conn == null) {
            return;
        }
        try {
            final String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES(?, ?)";
            final PreparedStatement stmt = this.conn.prepareStatement(sql);
            for (final String itemId : itemIds) {
                stmt.setString(1, userId);
                stmt.setString(2, itemId);
                stmt.execute();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void unsetFavoriteItems(final String userId, final List<String> itemIds) {
        if (this.conn == null) {
            return;
        }
        try {
            final String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
            final PreparedStatement stmt = this.conn.prepareStatement(sql);
            for (final String itemId : itemIds) {
                stmt.setString(1, userId);
                stmt.setString(2, itemId);
                stmt.execute();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

	@Override
	public Set<String> getFavoriteItemIds(final String userId) {
		if (this.conn == null) {
			return new HashSet<String>();
		}
		final Set<String> favoriteItemIds = new HashSet<String>();
		try {
			final String sql = "SELECT item_id FROM history WHERE user_id = ?";
			final PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, userId);
			final ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				final String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(final String userId) {
		if (this.conn == null) {
			return new HashSet<Item>();
		}
		final Set<Item> favoriteItems = new HashSet<Item>();
		final Set<String> itemIds = this.getFavoriteItemIds(userId);
		try {
			final String sql = "SELECT * FROM items WHERE item_id = ?";
			final PreparedStatement stmt = this.conn.prepareStatement(sql);
			for (final String itemId : itemIds) {
				stmt.setString(1, itemId);
				final ResultSet rs = stmt.executeQuery();
				final Item.ItemBuilder builder = new Item.ItemBuilder();
				
				while (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
                    builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));
					
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(final String itemId) {
		if (this.conn == null) {
			return new HashSet<String>();
		}
		final Set<String> categories = new HashSet<String>();
		try {
			final String sql = "SELECT category FROM categories WHERE item_id = ?";
			final PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, itemId);
			final ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(final double lat, final double lon, final String term) {
		final TicketMasterAPI tmAPI = new TicketMasterAPI();
		final List<Item> items = tmAPI.search(lat, lon, term);
		for (final Item item : items) {
			this.saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(final Item item) {
		if (this.conn == null) {
			return;
		}
		try {
			final String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.execute();
			final String insertSQL = "INSERT IGNORE INTO categories VALUES(?,?)";
			stmt = this.conn.prepareStatement(insertSQL);
			for (final String category : item.getCategories()) {
				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFullname(final String userId) {
		if (this.conn == null) {
			return "";
		}
		String name = "";
		try {
			final String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
			final PreparedStatement statement = this.conn.prepareStatement(sql);
			statement.setString(1, userId);
			final ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean verifyLogin(final String userId, final String password) {
		if (this.conn == null) {
			return false;
		}
		try {
			final String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
			final PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, userId);
			stmt.setString(2, password);
			final ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean registerUser(final String userId, final String password, final String firstname,
			final String lastname) {
		if (this.conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			final String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
			final PreparedStatement ps = this.conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ps.setString(3, firstname);
			ps.setString(4, lastname);
			return ps.executeUpdate() == 1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}





}
