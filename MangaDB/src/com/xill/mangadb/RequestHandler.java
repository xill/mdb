package com.xill.mangadb;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xill.mangadb.control.DatabaseControl;
import com.xill.mangadb.db.Author;
import com.xill.mangadb.db.Chapter;
import com.xill.mangadb.db.ContentTag;
import com.xill.mangadb.db.Series;
import com.xill.mangadb.db.SeriesName;
import com.xill.mangadb.db.Tag;
import com.xill.mangadb.util.StringUtil;

/**
 * Server api handler class.
 */
public class RequestHandler extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2921521477377665126L;

	private final static String API_KEY = "/api/";
	private final static String API_GET_ALL = API_KEY + "all";
	private final static String API_GET_SERIES = API_KEY + "series";
	private final static String API_GET_TAGS = API_KEY + "tags";
	private final static String API_GET_CONTENT_TAGS = API_KEY + "ctags";
	private final static String API_GET_SEARCH = API_KEY + "search";

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
			else if (requestString.startsWith(API_GET_TAGS))
				handleGetTags(request, response, requestString);
			else if (requestString.startsWith(API_GET_CONTENT_TAGS))
				handleGetContentTags(request, response, requestString);
			else if (requestString.startsWith(API_GET_SEARCH))
				handleSearch(request, response);
		}

	}

	private void handleSearch(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String tags = request.getParameter("tags");
		String name = request.getParameter("name");
		String author = request.getParameter("author");
		String artist = request.getParameter("artist");

		System.out.println("search");
		System.out.println(tags);
		System.out.println(name);
		System.out.println(author);
		System.out.println(artist);

		String[] tagList = removeRedundant(tags.split(","));
		List<Series> s = null;
		// no tags. get all.
		if (tagList.length == 0) {
			try {
				s = DatabaseControl.get().getSeriesList();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// has tags. get with given tags.
		else {
			try {
				s = DatabaseControl.get().getSeriesByTags(tagList);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// trim results by series name.
		if (name != null && name.length() > 0 && !name.equals("null")) {
			Iterator<Series> it = s.iterator();
			while (it.hasNext()) {
				Series ns = it.next();
				if (!StringUtil.containsIgnoreCase(ns.getName(), name)) {
					it.remove();
				}
			}
		}

		// trim results by author name.
		if (author != null && author.length() > 0 && !author.equals("null")) {
			Iterator<Series> it = s.iterator();
			while (it.hasNext()) {
				Series ns = it.next();
				Author au = ns.getAuthor();
				if (au == null
						|| !StringUtil.containsIgnoreCase(au.getName(), author)) {
					it.remove();
				}
			}
		}

		// trim results by artist name.
		if (artist != null && artist.length() > 0 && !artist.equals("null")) {
			Iterator<Series> it = s.iterator();
			while (it.hasNext()) {
				Series ns = it.next();
				Author au = ns.getArtist();
				if (au == null
						|| !StringUtil.containsIgnoreCase(au.getName(), artist)) {
					it.remove();
				}
			}
		}

		System.out.println(s);
		// build search response.
		if (s != null && s.size() > 0) {
			StringBuilder builder = new StringBuilder();
			builder.append("{");
			builder.append("\"series\":[");
			for (int i = 0; i < s.size(); ++i) {
				if (i > 0)
					builder.append(",");
				if (s.get(i) == null)
					System.out.println(i + " is null");
				builder.append("\"" + s.get(i).getName() + "\"");
			}
			builder.append("]");
			builder.append("}");
			response.getWriter().println(builder.toString());

		} else {
			response.getWriter().println("{}");
		}

	}

	private void handleGetAll(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		List<Series> seriesSet = null;
		// get data set of all series.
		try {
			seriesSet = DatabaseControl.get().getSeriesList();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// build response
		if (seriesSet != null) {
			StringBuilder builder = new StringBuilder();
			builder.append("{");
			builder.append("\"series\":[");
			for (int i = 0; i < seriesSet.size(); ++i) {
				if (i > 0)
					builder.append(",");

				Series serie = seriesSet.get(i);
				builder.append("{");
				builder.append("\"name\":\"" + serie.getName() + "\",");
				builder.append("\"names\": [");
				List<SeriesName> sNames = new ArrayList<SeriesName>(
						serie.getSeriesNames());
				for (int f = 0; f < sNames.size(); ++f) {
					if (f > 0)
						builder.append(",");
					builder.append("\"" + sNames.get(f).getName() + "\"");
				}
				builder.append("]");
				builder.append("}");
			}
			builder.append("]");
			builder.append("}");

			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(builder.toString());
		}
	}

	private void handleGetContentTags(HttpServletRequest request,
			HttpServletResponse response, String query)
			throws ServletException, IOException {

		// for all
		// /api/ctags

		// for specific
		// /api/ctags/Example1

		// for combination
		// /api/ctags/Example1+Example2
		
		String shortQuery = query.replace(API_GET_CONTENT_TAGS, "").replace("/", "")
				.trim();
		
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		
		// get content tag listing
		if (shortQuery.length() == 0) {
			List<ContentTag> tagDaos = null;
			try {
				tagDaos = DatabaseControl.get().getAllContentTags();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (tagDaos != null) {
				List<String> tagNames = new ArrayList<String>();
				Map<String, Integer> tagCounts = new HashMap<String, Integer>();
				for (ContentTag t : tagDaos) {
					String name = t.getName();
					Integer i = tagCounts.get(name);
					if (i == null) {
						tagCounts.put(name, 1);
						tagNames.add(name);
					} else {
						tagCounts.put(name, i + 1);
					}
				}

				StringBuilder builder = new StringBuilder();
				builder.append("{");
				builder.append("\"tags\":");
				builder.append("[");
				for (int i = 0; i < tagNames.size(); ++i) {
					if (i > 0)
						builder.append(",");
					builder.append("\"" + tagNames.get(i) + "\"");
				}
				builder.append("],");
				builder.append("\"count\":");
				builder.append("{");
				for (int i = 0; i < tagNames.size(); ++i) {
					if (i > 0)
						builder.append(",");
					builder.append("\"" + tagNames.get(i) + "\":");
					builder.append("\"" + tagCounts.get(tagNames.get(i)) + "\"");
				}
				builder.append("}");
				builder.append("}");
				response.getWriter().println(builder.toString());
			} else {
				response.getWriter().println("{}");
			}

		}
		// get results from a tag search.
		else {
			String[] tags = shortQuery.split(",");
			List<Series> s = null;
			try {
				s = DatabaseControl.get().getSeriesByTags(tags);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println(s);
			if (s != null) {
				StringBuilder builder = new StringBuilder();
				builder.append("{");
				builder.append("\"series\":[");
				for (int i = 0; i < s.size(); ++i) {
					if (i > 0)
						builder.append(",");
					if (s.get(i) == null)
						System.out.println(i + " is null");
					builder.append("\"" + s.get(i).getName() + "\"");
				}
				builder.append("]");
				builder.append("}");
				response.getWriter().println(builder.toString());

			} else {
				response.getWriter().println("{}");
			}
		}
	}

	private void handleGetTags(HttpServletRequest request,
			HttpServletResponse response, String query)
			throws ServletException, IOException {

		// for all
		// /api/tags

		// for specific
		// /api/tags/Example1

		// for combination
		// /api/tags/Example1+Example2

		String shortQuery = query.replace(API_GET_TAGS, "").replace("/", "")
				.trim();

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);

		// get tag listing
		if (shortQuery.length() == 0) {
			List<Tag> tagDaos = null;
			try {
				tagDaos = DatabaseControl.get().getAllTags();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			if (tagDaos != null) {
				List<String> tagNames = new ArrayList<String>();
				Map<String, Integer> tagCounts = new HashMap<String, Integer>();
				for (Tag t : tagDaos) {
					String name = t.getName();
					Integer i = tagCounts.get(name);
					if (i == null) {
						tagCounts.put(name, 1);
						tagNames.add(name);
					} else {
						tagCounts.put(name, i + 1);
					}
				}

				StringBuilder builder = new StringBuilder();
				builder.append("{");
				builder.append("\"tags\":");
				builder.append("[");
				for (int i = 0; i < tagNames.size(); ++i) {
					if (i > 0)
						builder.append(",");
					builder.append("\"" + tagNames.get(i) + "\"");
				}
				builder.append("],");
				builder.append("\"count\":");
				builder.append("{");
				for (int i = 0; i < tagNames.size(); ++i) {
					if (i > 0)
						builder.append(",");
					builder.append("\"" + tagNames.get(i) + "\":");
					builder.append("\"" + tagCounts.get(tagNames.get(i)) + "\"");
				}
				builder.append("}");
				builder.append("}");
				response.getWriter().println(builder.toString());
			} else {
				response.getWriter().println("{}");
			}

		}
		// get results from a tag search.
		else {
			String[] tags = shortQuery.split(",");
			List<Series> s = null;
			try {
				s = DatabaseControl.get().getSeriesByTags(tags);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println(s);
			if (s != null) {
				StringBuilder builder = new StringBuilder();
				builder.append("{");
				builder.append("\"series\":[");
				for (int i = 0; i < s.size(); ++i) {
					if (i > 0)
						builder.append(",");
					if (s.get(i) == null)
						System.out.println(i + " is null");
					builder.append("\"" + s.get(i).getName() + "\"");
				}
				builder.append("]");
				builder.append("}");
				response.getWriter().println(builder.toString());

			} else {
				response.getWriter().println("{}");
			}
		}
	}

	private void handleGetSeries(HttpServletRequest request,
			HttpServletResponse response, String query)
			throws ServletException, IOException {

		// detemine series name and chapter number
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

		// search series with name.
		Series s = null;
		try {
			s = DatabaseControl.get().getSeriesByName(seriesName);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// build response
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		if (s != null) {
			if (chapterNumber > -1) {
				List<Chapter> chapters = new ArrayList<Chapter>(s.getChapters());
				if (chapterNumber < chapters.size()) {
					Chapter c = chapters.get(chapterNumber);
					if (c != null)
						response.getWriter().println(c.outputJson());
					else
						response.getWriter().println("{}");
				}
				// nothing to return
				else {
					response.getWriter().println("{}");
				}
			} else {
				response.getWriter().println(s.outputJson());
			}
		}
		// nothing to return
		else {
			response.getWriter().println("{}");
		}
	}

	/**
	 * remove empty string from array
	 * 
	 * @param strings
	 *            - array of string to trim
	 * @return trimmed array
	 */
	private String[] removeRedundant(String... strings) {
		List<String> out = new LinkedList<String>(Arrays.asList(strings));
		Iterator<String> it = out.iterator();
		while (it.hasNext()) {
			String str = it.next();
			if (str == null || str.isEmpty())
				it.remove();
		}
		return out.toArray(new String[] {});
	}
}
