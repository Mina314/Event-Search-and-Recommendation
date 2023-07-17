package rpc;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet({ "/history" })
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();

		DBConnection conn = DBConnectionFactory.getConnection();
		Set<Item> items = conn.getFavoriteItems(userId);
		for (Item item : items) {
			JSONObject obj = item.toJSONObject();
			try {
				obj.append("favorite", Boolean.valueOf(true));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            final JSONObject input = RpcHelper.readJsonObject(request);
            final String userId = input.getString("user_id");
            final JSONArray array = input.getJSONArray("favorite");
            final List<String> itemId = new ArrayList<String>();
            for (int i = 0; i < array.length(); ++i) {
                itemId.add(array.get(i).toString());
            }
            final DBConnection conn = DBConnectionFactory.getConnection();
            conn.setFavoriteItems(userId, itemId);
            conn.close();
            final JSONObject obj = new JSONObject();
            obj.put("result", (Object)"SUCCESS");
            RpcHelper.writeJsonObject(response, obj);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
            final JSONObject input = RpcHelper.readJsonObject(request);
            final String userId = input.getString("user_id");
            final JSONArray array = input.getJSONArray("favorite");
            final List<String> itemIds = new ArrayList<String>();
            for (int i = 0; i < array.length(); ++i) {
                itemIds.add(array.get(i).toString());
            }
            final DBConnection conn = DBConnectionFactory.getConnection();
            conn.unsetFavoriteItems(userId, itemIds);
            conn.close();
            RpcHelper.writeJsonObject(response, new JSONObject().put("result", (Object)"SUCCESS"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}