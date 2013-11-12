package com.sobremesa.waywt.database.tables;

/**
 * This interface represents the columns and SQLite statements for the RedditPostTable.
 * This table is represented in the sqlite database as RedditPost column.
 * 				  
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.11
 */
public interface RedditPostTable {
	String TABLE_NAME = "redditpost";

	String ID = "_id";
	String UPS = "ups";
	String DOWNS = "downs";
	String AUTHOR = "author";
	String CREATED = "created";
	String PERMALINK = "permalink";
	String TITLE = "title";

	String[] ALL_COLUMNS = new String[]{ID, UPS, DOWNS, AUTHOR, CREATED,
			PERMALINK, TITLE};

	String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + UPS + " INTEGER"
			+ "," + DOWNS + " INTEGER" + "," + AUTHOR + " TEXT" + "," + CREATED
			+ " INTEGER" + "," + PERMALINK + " TEXT" + "," + TITLE + " TEXT"
			+ " )";

	String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + UPS + "," + DOWNS
			+ "," + AUTHOR + "," + CREATED + "," + PERMALINK + "," + TITLE
			+ ") VALUES ( ?, ?, ?, ?, ?, ? )";

	String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	String WHERE_ID_EQUALS = ID + "=?";
}
