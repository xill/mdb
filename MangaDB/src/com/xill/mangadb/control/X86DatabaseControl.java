package com.xill.mangadb.control;

import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;

public class X86DatabaseControl extends DatabaseControl {

	public X86DatabaseControl()
	{
		super();
	
		try {
			source = new JdbcConnectionSource(databaseUrl);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			init();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
