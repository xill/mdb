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
import com.xill.mangadb.db.Author;
import com.xill.mangadb.db.Chapter;
import com.xill.mangadb.db.Page;
import com.xill.mangadb.db.Series;
import com.xill.mangadb.db.SeriesName;
import com.xill.mangadb.db.Tag;
import com.xill.mangadb.db.Thumbnail;

/**
 * Generic database controlled
 */
public abstract class DatabaseControl {

	/**
	 * TODO : add caching. if series set for example hasnt changed. there is no
	 * point refetching it.
	 * 
	 */

	// database connections
	protected String databaseUrl = "jdbc:sqlite:manga.db";
	protected ConnectionSource source;

	// database dao wrappers
	protected Dao<Series, String> seriesDao;
	protected Dao<Chapter, String> chaptersDao;
	protected Dao<Page, String> pageDao;
	protected Dao<Tag, String> tagDao;
	protected Dao<Author, String> authorDao;
	protected Dao<SeriesName, String> seriesNameDao;
	protected Dao<Thumbnail, String> thumbnailDao;

	// singleton instance for this database controller.
	private static DatabaseControl m_instance = null;

	public DatabaseControl() {
		m_instance = this;
	}

	/**
	 * @return - current database controller instance.
	 */
	public static DatabaseControl get() {
		return m_instance;
	}

	/**
	 * Initialize database controller.
	 * 
	 * @throws SQLException
	 */
	public void init() throws SQLException {
		if (source == null) {
			System.out
					.println("ERROR : init called when connection source is not defined.");
		}

		// create table if required.
		TableUtils.createTableIfNotExists(source, Author.class);
		TableUtils.createTableIfNotExists(source, Tag.class);
		TableUtils.createTableIfNotExists(source, Page.class);
		TableUtils.createTableIfNotExists(source, Chapter.class);
		TableUtils.createTableIfNotExists(source, Thumbnail.class);
		TableUtils.createTableIfNotExists(source, SeriesName.class);
		TableUtils.createTableIfNotExists(source, Series.class);

		// instantiate the dao
		authorDao = DaoManager.createDao(source, Author.class);
		tagDao = DaoManager.createDao(source, Tag.class);
		pageDao = DaoManager.createDao(source, Page.class);
		chaptersDao = DaoManager.createDao(source, Chapter.class);
		thumbnailDao = DaoManager.createDao(source, Thumbnail.class);
		seriesNameDao = DaoManager.createDao(source, SeriesName.class);
		seriesDao = DaoManager.createDao(source, Series.class);

		// setup auto connections.
		DatabaseConnection seriesConn = seriesDao.startThreadConnection();
		seriesDao.setAutoCommit(seriesConn, true);
		DatabaseConnection seriesNameConn = seriesNameDao.startThreadConnection();
		seriesNameDao.setAutoCommit(seriesNameConn, true);
		DatabaseConnection thumbnailConn = thumbnailDao.startThreadConnection();
		thumbnailDao.setAutoCommit(thumbnailConn, true);
		DatabaseConnection chapterConn = chaptersDao.startThreadConnection();
		chaptersDao.setAutoCommit(chapterConn, true);
		DatabaseConnection pageConn = pageDao.startThreadConnection();
		pageDao.setAutoCommit(pageConn, true);
		DatabaseConnection tagConn = tagDao.startThreadConnection();
		tagDao.setAutoCommit(tagConn, true);
		DatabaseConnection authorConn = authorDao.startThreadConnection();
		authorDao.setAutoCommit(authorConn, true);
	}
	
	/**
	 * @return - full list of authors
	 * @throws SQLException
	 */
	public List<Author> getAuthorList() throws SQLException {
		return authorDao.queryForAll();
	}
	
	/**
	 * @param id - author id key
	 * @return - author with id key or null if none.
	 * @throws SQLException
	 */
	public Author getAuthor(String id) throws SQLException {
		return authorDao.queryForId(id);
	}
	
	/**
	 * Create or update an author dao object.
	 * 
	 * @param dao - object to create or update.
	 * @throws SQLException
	 */
	public void setAuthor(Author dao) throws SQLException {
		authorDao.createOrUpdate(dao);
	}

	/**
	 * @return - full list of series.
	 * @throws SQLException
	 */
	public List<Series> getSeriesList() throws SQLException {
		return seriesDao.queryForAll();
	}

	/**
	 * @param id - get series with this id.
	 * @return - series found or null if none.
	 * @throws SQLException
	 */
	public Series getSeries(String id) throws SQLException {
		return seriesDao.queryForId(id);
	}

	/**
	 * Get series with name
	 * 
	 * @param name - series with name to find.
	 * @return - series with name or null if none.
	 * @throws SQLException
	 */
	public Series getSeriesByName(String name) throws SQLException {
		return seriesDao.queryBuilder().where().eq("name", name)
				.queryForFirst();
	}

	/**
	 * @param id - get series with this id.
	 * @return - series found or null if none.
	 * @throws SQLException
	 */
	public Series getSeriesById(int id) throws SQLException {
		return seriesDao.queryBuilder().where().eq("id", id + "")
				.queryForFirst();
	}

	/**
	 * @param tags - tags to search with.
	 * @return - series with given tag.
	 * @throws SQLException
	 */
	public List<Series> getSeriesByTags(String... tags) throws SQLException {

		List<Series> seriesList = new ArrayList<Series>();
		if (tags.length > 0) {
			// get initial series set with first tag.
			List<Tag> tagList = getTagsByName(tags[0]);
			List<Integer> seriesIds = new ArrayList<Integer>();
			for (Tag t : tagList) {
				int i = t.getRefId();
				if (!seriesIds.contains(i))
					seriesIds.add(i);
			}

			// trim series with remaining tags.
			for (int i = 0; i < seriesIds.size(); ++i) {
				int id = seriesIds.get(i);
				Series s = getSeriesById(id);
				
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

	/**
	 * Create or update series dao.
	 * 
	 * @param dao - series dao to create or update.
	 * @throws SQLException
	 */
	public void setSeries(Series dao) throws SQLException {
		
		Author auth = dao.getAuthor(); 
		if(auth != null) setAuthor(auth);
		Author art = dao.getArtist(); 
		if(art != null) setAuthor(art);
		Thumbnail thumb = dao.getThumbnail();
		if(thumb != null) setThumbnail(thumb);
		
		seriesDao.createOrUpdate(dao);

		Collection<SeriesName> seriesName = dao.getSeriesNames();
		for (SeriesName sn : seriesName) {
			setSeriesName(sn);
		}
		
		Collection<Tag> tags = dao.getTags();
		for (Tag t : tags) {
			setTag(t);
		}

		Collection<Chapter> chapters = dao.getChapters();
		for (Chapter c : chapters) {
			setChapter(c);
		}
	}

	/**
	 * @param id - id of the chapter to get.
	 * @return - chapter found or null if not.
	 * @throws SQLException
	 */
	public Chapter getChapter(String id) throws SQLException {
		return chaptersDao.queryForId(id);
	}

	/**
	 * Create or update chapter dao
	 * 
	 * @param dao - chapter dao to create or update.
	 * @throws SQLException
	 */
	public void setChapter(Chapter dao) throws SQLException {
		chaptersDao.createOrUpdate(dao);

		Collection<Page> pages = dao.getPages();
		for (Page p : pages) {
			setPage(p);
		}
	}

	/**
	 * @param id - id of the page to get.
	 * @return - page found or null if not.
	 * @throws SQLException
	 */
	public Page getPage(String id) throws SQLException {
		return pageDao.queryForId(id);
	}

	/**
	 * Create or update page dao
	 * 
	 * @param dao - page dao to create or update.
	 * @throws SQLException
	 */
	public void setPage(Page dao) throws SQLException {
		pageDao.createOrUpdate(dao);
	}
	
	/**
	 * @param id - id of the seriesname to get.
	 * @return - seriesname found or null if not.
	 * @throws SQLException
	 */
	public SeriesName getSeriesName(String id) throws SQLException {
		return seriesNameDao.queryForId(id);
	}
	
	/**
	 * Create or update seriesname dao
	 * 
	 * @param dao - seriesname dao to create or update.
	 * @throws SQLException
	 */
	public void setSeriesName(SeriesName dao) throws SQLException {
		seriesNameDao.createOrUpdate(dao);
	}
	
	/**
	 * @param id - id of the thumbnail to get.
	 * @return - thumbnail found or null if not.
	 * @throws SQLException
	 */
	public Thumbnail getThumbnail(String id) throws SQLException {
		return thumbnailDao.queryForId(id);
	}
	
	/**
	 * Create or update thumbnail dao
	 * 
	 * @param dao - thumbnail dao to create or update.
	 * @throws SQLException
	 */
	public void setThumbnail(Thumbnail dao) throws SQLException {
		thumbnailDao.createOrUpdate(dao);
	}

	/**
	 * @return - full list of tags.
	 * @throws SQLException
	 */
	public List<Tag> getAllTags() throws SQLException {
		return tagDao.queryForAll();
	}

	/**
	 * @param name - tag name to find.
	 * @return - tags found with the given name.
	 * @throws SQLException
	 */
	public List<Tag> getTagsByName(String name) throws SQLException {
		return tagDao.queryBuilder().where().eq("name", name).query();
	}

	/**
	 * @param id - id of the tag to get.
	 * @return - tag found or null if not.
	 * @throws SQLException
	 */
	public Tag getTag(String id) throws SQLException {
		return tagDao.queryForId(id);
	}

	/**
	 * Create or update tag dao
	 * 
	 * @param dao - tag dao to create or update.
	 * @throws SQLException
	 */
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
		// TODO missing tables
	}
}
