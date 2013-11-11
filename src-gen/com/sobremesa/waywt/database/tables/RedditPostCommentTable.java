package com.sobremesa.waywt.database.tables;

/**
 * This interface represents the columns and SQLite statements for the RedditPostCommentTable.
 * This table is represented in the sqlite database as RedditPostComment column.
 * 				  
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.11
 */
public interface RedditPostCommentTable {
	String TABLE_NAME = "redditpostcomment";

	String ID = "_id";
	String AUTHOR = "author";
	String UPS = "ups";
	String DOWNS = "downs";
	String CREATED = "created";
	String BODY_HTML = "body_html";
	String IDENTIFIER = "identifier";
	String REDDITPOST_ID = "redditpostid";

	String[] ALL_COLUMNS = new String[]{ID, AUTHOR, UPS, DOWNS, CREATED,
			BODY_HTML, IDENTIFIER, REDDITPOST_ID};

	String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + AUTHOR + " TEXT"
			+ "," + UPS + " INTEGER" + "," + DOWNS + " INTEGER" + "," + CREATED
			+ " INTEGER" + "," + BODY_HTML + " TEXT" + "," + IDENTIFIER
			+ " TEXT" + "," + REDDITPOST_ID + " INTEGER" + " )";

	String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + AUTHOR + "," + UPS
			+ "," + DOWNS + "," + CREATED + "," + BODY_HTML + "," + IDENTIFIER
			+ REDDITPOST_ID + ") VALUES ( ?, ?, ?, ?, ?, ?, ? )";

	String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	String WHERE_ID_EQUALS = ID + "=?";
}
