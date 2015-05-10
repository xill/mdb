package com.xill.mangadb;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.Configuration;

import com.xill.mangadb.control.DatabaseControl;
import com.xill.mangadb.control.DisableFileMappedBufferConfiguration;
import com.xill.mangadb.control.X86DatabaseControl;
import com.xill.mangadb.db.Series;
import com.xill.mangadb.registry.RegistryParser;
import com.xill.mangadb.util.Options;

public class MangaDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// load options.
		// TODO add option for jetty memory cache
		DatabaseControl database = new X86DatabaseControl();
		Options.load();
		Options.setIfNotSet(Options.REGISTRY_URL,"");
		Options.save();
		List<Series> seriesCache = null;
		
		try {
			seriesCache = database.getSeriesList();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		// no database. read it.
		if(seriesCache == null || seriesCache.size() == 0) {
			RegistryParser registryParser = new RegistryParser();
			registryParser.parseInternalRegistry();
			List<Series> seriesRegister = registryParser.getSeriesRegistry();

			for(Series s : seriesRegister) {
				try {
					database.setSeries(s);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("done registering.");
		}
		else {
			System.out.println("database setup done.");
		}
		
		// standard jetty server.
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);
        
        // disables jetty file cache. awesome for development.
        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.add(DisableFileMappedBufferConfiguration.class.getName());
 
        // overlay html resource servlet.
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase("./resources/");
        
        // manga registry resource servlet.
        ResourceHandler registry_resource_handler = new ResourceHandler();
        registry_resource_handler.setDirectoriesListed(true);
        registry_resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        registry_resource_handler.setResourceBase(Options.get(Options.REGISTRY_URL));
 
        // servlet handler for api calls.
        ServletHandler apiHandler = new ServletHandler();
        apiHandler.addServletWithMapping(RequestHandler.class, "/api/*");
      
        // bundle all jetty handlers.
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, registry_resource_handler, apiHandler , new DefaultHandler() });
        server.setHandler(handlers);
 
        try {
        	server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
