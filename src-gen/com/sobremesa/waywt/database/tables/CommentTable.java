package com.sobremesa.waywt.database.tables;

/**
 * This interface represents the columns and SQLite statements for the CommentTable.
 * This table is represented in the sqlite database as Comment column.
 * 				  
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2014.01.06
 */
public interface CommentTable {
	String TABLE_NAME = "comment";

	String ID = "_id";
	String POST_TITLE = "post_title";
	String POST_PERMALINK = "post_permalink";
	String COMMENT_ID = "comment_id";
	String AUTHOR = "author";
	String BODY_HTML = "body_html";
	String NAME = "name";
	String LIKES = "likes";
	String UPS = "ups";
	String DOWNS = "downs";
	String CREATED = "created";
	String THREAD_ID = "thread_id";
	String IS_MALE = "is_male";
	String IS_TEEN = "is_teen";

	String[] ALL_COLUMNS = new String[]{ID, POST_TITLE, POST_PERMALINK,
			COMMENT_ID, AUTHOR, BODY_HTML, NAME, LIKES, UPS, DOWNS, CREATED,
			THREAD_ID, IS_MALE, IS_TEEN};

	String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + POST_TITLE + " TEXT"
			+ "," + POST_PERMALINK + " TEXT" + "," + COMMENT_ID + " TEXT" + ","
			+ AUTHOR + " TEXT" + "," + BODY_HTML + " TEXT" + "," + NAME
			+ " TEXT" + "," + LIKES + " INTEGER" + "," + UPS + " INTEGER" + ","
			+ DOWNS + " INTEGER" + "," + CREATED + " INTEGER" + "," + THREAD_ID
			+ " TEXT" + "," + IS_MALE + " INTEGER" + "," + IS_TEEN + " INTEGER"
			+ " )";

	String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + POST_TITLE + ","
			+ POST_PERMALINK + "," + COMMENT_ID + "," + AUTHOR + ","
			+ BODY_HTML + "," + NAME + "," + LIKES + "," + UPS + "," + DOWNS
			+ "," + CREATED + "," + THREAD_ID + "," + IS_MALE + "," + IS_TEEN
			+ ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	String WHERE_ID_EQUALS = ID + "=?";
}
