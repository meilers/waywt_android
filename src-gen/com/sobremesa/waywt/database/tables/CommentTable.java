package com.sobremesa.waywt.database.tables;

/**
 * This interface represents the columns and SQLite statements for the CommentTable.
 * This table is represented in the sqlite database as Comment column.
 * 				  
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.15
 */
public interface CommentTable {
	String TABLE_NAME = "comment";

	String ID = "_id";
	String AUTHOR = "author";
	String UPS = "ups";
	String DOWNS = "downs";
	String CREATED = "created";
	String BODY_HTML = "body_html";
	String IDENTIFIER = "identifier";
	String POST_ID = "postid";

	String[] ALL_COLUMNS = new String[]{ID, AUTHOR, UPS, DOWNS, CREATED,
			BODY_HTML, IDENTIFIER, POST_ID};

	String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + AUTHOR + " TEXT"
			+ "," + UPS + " INTEGER" + "," + DOWNS + " INTEGER" + "," + CREATED
			+ " INTEGER" + "," + BODY_HTML + " TEXT" + "," + IDENTIFIER
			+ " TEXT" + "," + POST_ID + " INTEGER" + " )";

	String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + AUTHOR + "," + UPS
			+ "," + DOWNS + "," + CREATED + "," + BODY_HTML + "," + IDENTIFIER
			+ POST_ID + ") VALUES ( ?, ?, ?, ?, ?, ?, ? )";

	String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	String WHERE_ID_EQUALS = ID + "=?";
}
