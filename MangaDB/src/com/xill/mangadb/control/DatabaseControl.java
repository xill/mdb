package com.xill.mangadb.control;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import com.xill.mangadb.db.Chapter;
import com.xill.mangadb.db.Page;
import com.xill.mangadb.db.Series;
import com.xill.mangadb.db.Tag;

public abstract class DatabaseControl {

	/**
	 * TODO : add caching. if series set for example hasnt changed. there is no
	 * point refetching it.
	 * 
	 */

	protected String databaseUrl = "jdbc:sqlite:manga.db";
	protected ConnectionSource source;

	protected Dao<Series, String> seriesDao;
	protected Dao<Chapter, String> chaptersDao;
	protected Dao<Page, String> pageDao;
	protected Dao<Tag, String> tagDao;

	private static DatabaseControl m_instance = null;

	public DatabaseControl() {
		m_instance = this;
	}

	public static DatabaseControl get() {
		return m_instance;
	}

	public void init() throws SQLException {
		if (source == null) {
			System.out
					.println("ERROR : init called when connection source is not defined.");
		}

		// create table if required.
		TableUtils.createTableIfNotExists(source, Tag.class);
		TableUtils.createTableIfNotExists(source, Page.class);
		TableUtils.createTableIfNotExists(source, Chapter.class);
		TableUtils.createTableIfNotExists(source, Series.class);

		// instantiate the dao
		tagDao = DaoManager.createDao(source, Tag.class);
		pageDao = DaoManager.createDao(source, Page.class);
		chaptersDao = DaoManager.createDao(source, Chapter.class);
		seriesDao = DaoManager.createDao(source, Series.class);

		// setup auto connections.
		DatabaseConnection seriesConn = seriesDao.startThreadConnection();
		seriesDao.setAutoCommit(seriesConn, true);
		DatabaseConnection chapterConn = chaptersDao.startThreadConnection();
		chaptersDao.setAutoCommit(chapterConn, true);
		DatabaseConnection pageConn = pageDao.startThreadConnection();
		pageDao.setAutoCommit(pageConn, true);
		DatabaseConnection tagConn = tagDao.startThreadConnection();
		tagDao.setAutoCommit(tagConn, true);
	}

	public List<Series> getSeriesList() throws SQLException {
		return seriesDao.queryForAll();
	}

	public Series getSeries(String id) throws SQLException {
		return seriesDao.queryForId(id);
	}

	public Series getSeriesByName(String name) throws SQLException {
		return seriesDao.queryBuilder().where().eq("name", name)
				.queryForFirst();
	}

	public Series getSeriesById(int id) throws SQLException {
		return seriesDao.queryBuilder().where().eq("id", id + "")
				.queryForFirst();
	}

	public List<Series> getSeriesByTags(String... tags) throws SQLException {

		List<Series> seriesList = new ArrayList<Series>();
		if (tags.length > 0) {
			List<Tag> tagList = getTagsByName(tags[0]);
			List<Integer> seriesIds = new ArrayList<Integer>();
			for (Tag t : tagList) {
				int i = t.getRefId();
				if (!seriesIds.contains(i))
					seriesIds.add(i);
			}

			for (int i = 0; i < seriesIds.size(); ++i) {
				int id = seriesIds.get(i);
				Series s = getSeriesById(id);
				System.out.println(id + " is " + ((s != null)?"not null":"null"));
				
				if(tags.length > 1) {
					boolean valid = true;
					List<Tag> sTags = new ArrayList<Tag>(s.getTags());
					for( int f = 0; f < tags.length ; ++f ) {
						boolean curValid = false;
						String tagName = tags[f];
						for(Tag g : sTags) {
							if(g.getName().equals(tagName)) {
								curValid = true;
								break;
							}
						}
						if(!curValid) {
							valid = false;
							break;
						}
					}
					
					if(valid) seriesList.add(s);
				}
				else {
					seriesList.add(s);
				}
			}
		}

		return seriesList;
	}

	public void setSeries(Series dao) throws SQLException {
		seriesDao.createOrUpdate(dao);

		Collection<Tag> tags = dao.getTags();
		for (Tag t : tags) {
			setTag(t);
		}

		Collection<Chapter> chapters = dao.getChapters();
		for (Chapter c : chapters) {
			setChapter(c);
		}
	}

	public Chapter getChapter(String id) throws SQLException {
		return chaptersDao.queryForId(id);
	}

	public void setChapter(Chapter dao) throws SQLException {
		chaptersDao.createOrUpdate(dao);

		Collection<Page> pages = dao.getPages();
		for (Page p : pages) {
			setPage(p);
		}
	}

	public Page getPage(String id) throws SQLException {
		return pageDao.queryForId(id);
	}

	public void setPage(Page dao) throws SQLException {
		pageDao.createOrUpdate(dao);
	}

	public List<Tag> getAllTags() throws SQLException {
		return tagDao.queryForAll();
	}

	public List<Tag> getTagsByName(String name) throws SQLException {
		return tagDao.queryBuilder().where().eq("name", name).query();
	}

	public Tag getTag(String id) throws SQLException {
		return tagDao.queryForId(id);
	}

	public void setTag(Tag dao) throws SQLException {
		tagDao.createOrUpdate(dao);
	}

	/**
	 * Does what it says. Obviously not reversible.
	 * 
	 * @throws SQLException
	 */
	public void clearDatabase() throws SQLException {
		TableUtils.dropTable(source, Series.class, true);
		TableUtils.dropTable(source, Chapter.class, true);
		TableUtils.dropTable(source, Page.class, true);
	}
}
