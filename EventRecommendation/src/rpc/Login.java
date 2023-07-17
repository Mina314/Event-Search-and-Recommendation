package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import db.DBConnection;
import org.json.JSONObject;
import db.DBConnectionFactory;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final DBConnection connection = DBConnectionFactory.getConnection();
		try {
			final HttpSession session = request.getSession(false);
			final JSONObject obj = new JSONObject();
			if (session != null) {
				final String userId = session.getAttribute("user_id").toString();
				obj.put("status", (Object) "OK").put("user_id", (Object) userId).put("name",
						(Object) connection.getFullname(userId));
			} else {
				obj.put("status", (Object) "Invalid Session");
				response.setStatus(403);
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			connection.close();
		}
		connection.close();
	}

	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		final DBConnection connection = DBConnectionFactory.getConnection();
		try {
			final JSONObject input = RpcHelper.readJsonObject(request);
			final String userId = input.getString("user_id");
			final String password = input.getString("password");
			final JSONObject obj = new JSONObject();
			if (connection.verifyLogin(userId, password)) {
				final HttpSession session = request.getSession();
				session.setAttribute("user_id", (Object) userId);
				session.setMaxInactiveInterval(600);
				obj.put("status", (Object) "OK").put("user_id", (Object) userId).put("name",
						(Object) connection.getFullname(userId));
			} else {
				obj.put("status", (Object) "User Doesn't Exist");
				response.setStatus(401);
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			connection.close();
		}
		connection.close();
	}

}