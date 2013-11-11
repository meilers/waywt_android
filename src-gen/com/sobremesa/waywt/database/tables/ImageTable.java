package com.sobremesa.waywt.database.tables;

/**
 * This interface represents the columns and SQLite statements for the ImageTable.
 * This table is represented in the sqlite database as Image column.
 * 				  
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.11
 */
public interface ImageTable {
	String TABLE_NAME = "image";

	String ID = "_id";
	String URL = "url";
	String REDDITPOST_ID = "redditpostid";
	String REDDITPOSTCOMMENT_ID = "redditpostcommentid";

	String[] ALL_COLUMNS = new String[]{ID, URL, REDDITPOST_ID,
			REDDITPOSTCOMMENT_ID};

	String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + URL + " TEXT" + ","
			+ REDDITPOST_ID + " INTEGER" + "," + REDDITPOSTCOMMENT_ID
			+ " INTEGER" + " )";

	String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + URL
			+ REDDITPOST_ID + "," + REDDITPOSTCOMMENT_ID
			+ ") VALUES ( ?, ?, ? )";

	String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	String WHERE_ID_EQUALS = ID + "=?";
}
