package com.xill.mangadb;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xill.mangadb.control.DatabaseControl;
import com.xill.mangadb.db.Chapter;
import com.xill.mangadb.db.Series;
import com.xill.mangadb.util.StringUtil;

public class RequestHandler extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2921521477377665126L;

	private final static String API_KEY = "/api/";
	private final static String API_GET_ALL = API_KEY + "all";
	private final static String API_GET_SERIES = API_KEY + "series";

	// reader/

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	private void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String requestString = URLDecoder.decode(request.getRequestURI(),
				"UTF-8");
		System.out.println(requestString);

		// Explicit requests.
		if (API_GET_ALL.equals(requestString))
			handleGetAll(request, response);
		// requests with options.
		else if (requestString != null && requestString.length() > 0) {
			if (requestString.startsWith(API_GET_SERIES))
				handleGetSeries(request, response, requestString);
		}

	}

	private void handleGetAll(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		List<Series> seriesSet = null;
		try {
			seriesSet = DatabaseControl.get().getSeriesList();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (seriesSet != null) {
			StringBuilder builder = new StringBuilder();
			builder.append("{");
			builder.append("\"series\":[");
			for (int i = 0; i < seriesSet.size(); ++i) {
				if (i > 0)
					builder.append(",");
				builder.append("\"" + seriesSet.get(i).getName() + "\"");
			}
			builder.append("]");
			builder.append("}");

			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(builder.toString());
		}
	}

	private void handleGetSeries(HttpServletRequest request,
			HttpServletResponse response, String query)
			throws ServletException, IOException {

		String shortQuery = query.replace(API_GET_SERIES + "/", "");
		String seriesName = null;
		int chapterNumber = -1;
		if (shortQuery.contains("/")) {
			String[] splits = shortQuery.split("/");
			seriesName = splits[0];
			try {
				chapterNumber = Integer.parseInt(splits[1]);
			} catch (NumberFormatException e) {
				chapterNumber = -1;
			}
		} else {
			seriesName = shortQuery;
		}

		System.out.println("series name \"" + seriesName + "\"");
		System.out.println("chapter number \"" + chapterNumber + "\"");
		/*
		 * System.out.println(shortQuery);
		 * 
		 * String[] splits = query.split("/"); = splits[splits.length-1];
		 */
		Series s = null;
		try {
			s = DatabaseControl.get().getSeriesByName(seriesName);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		if (s != null) {
			if (chapterNumber > -1) {
				List<Chapter> chapters = new ArrayList(s.getChapters());
				if(chapterNumber < chapters.size()) {
					Chapter c = chapters.get(chapterNumber);
					if(c != null) response.getWriter().println(StringUtil.toValidJson(c.outputJson()));
					else response.getWriter().println("{}");
				}
				// nothing to return
				else {
					response.getWriter().println("{}");
				}
			}
			else {
				response.getWriter().println(StringUtil.toValidJson(s.outputJson()));
			}
		}
		// nothing to return
		else {
			response.getWriter().println("{}");
		}
	}
}
