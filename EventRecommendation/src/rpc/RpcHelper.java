package rpc;

import java.io.BufferedReader;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import java.io.PrintWriter;
import org.json.JSONObject;
import javax.servlet.http.HttpServletResponse;

public class RpcHelper {
	public static void writeJsonObject(final HttpServletResponse response, final JSONObject obj) {
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			final PrintWriter out = response.getWriter();
			out.print(obj);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeJsonArray(final HttpServletResponse response, final JSONArray array) {
		try {
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			final PrintWriter out = response.getWriter();
			out.print(array);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static JSONObject readJsonObject(final HttpServletRequest request) {
		final StringBuilder sb = new StringBuilder();
		try {
			final BufferedReader reader = request.getReader();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
