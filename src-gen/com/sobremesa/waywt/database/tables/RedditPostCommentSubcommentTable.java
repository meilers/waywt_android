package com.sobremesa.waywt.database.tables;

/**
 * This interface represents the columns and SQLite statements for the RedditPostCommentSubcommentTable.
 * This table is represented in the sqlite database as RedditPostCommentSubcomment column.
 * 				  
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.13
 */
public interface RedditPostCommentSubcommentTable {
	String TABLE_NAME = "redditpostcommentsubcomment";

	String ID = "_id";
	String AUTHOR = "author";
	String UPS = "ups";
	String DOWNS = "downs";
	String CREATED = "created";
	String BODY_HTML = "body_html";
	String IDENTIFIER = "identifier";
	String REDDITPOST_ID = "redditpostid";
	String REDDITPOSTCOMMENT_ID = "redditpostcommentid";

	String[] ALL_COLUMNS = new String[]{ID, AUTHOR, UPS, DOWNS, CREATED,
			BODY_HTML, IDENTIFIER, REDDITPOST_ID, REDDITPOSTCOMMENT_ID};

	String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + AUTHOR + " TEXT"
			+ "," + UPS + " TEXT" + "," + DOWNS + " TEXT" + "," + CREATED
			+ " TEXT" + "," + BODY_HTML + " TEXT" + "," + IDENTIFIER + " TEXT"
			+ "," + REDDITPOST_ID + " INTEGER" + "," + REDDITPOSTCOMMENT_ID
			+ " INTEGER" + " )";

	String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + AUTHOR + "," + UPS
			+ "," + DOWNS + "," + CREATED + "," + BODY_HTML + "," + IDENTIFIER
			+ REDDITPOST_ID + "," + REDDITPOSTCOMMENT_ID
			+ ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";

	String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	String WHERE_ID_EQUALS = ID + "=?";
}
