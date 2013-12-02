package com.sobremesa.waywt.database.tables;

/**
 * This interface represents the columns and SQLite statements for the ReplyTable.
 * This table is represented in the sqlite database as Reply column.
 * 				  
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.12.01
 */
public interface ReplyTable {
	String TABLE_NAME = "reply";

	String ID = "_id";
	String AUTHOR = "author";
	String UPS = "ups";
	String DOWNS = "downs";
	String CREATED = "created";
	String BODY_HTML = "body_html";
	String IDENTIFIER = "identifier";
	String PARENTIDENTIFIER = "parentidentifier";
	String POST_ID = "postid";
	String COMMENT_ID = "commentid";
	String REPLY_ID = "replyid";

	String[] ALL_COLUMNS = new String[]{ID, AUTHOR, UPS, DOWNS, CREATED,
			BODY_HTML, IDENTIFIER, PARENTIDENTIFIER, POST_ID, COMMENT_ID,
			REPLY_ID};

	String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ( " + ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + "," + AUTHOR + " TEXT"
			+ "," + UPS + " TEXT" + "," + DOWNS + " TEXT" + "," + CREATED
			+ " TEXT" + "," + BODY_HTML + " TEXT" + "," + IDENTIFIER + " TEXT"
			+ "," + PARENTIDENTIFIER + " TEXT" + "," + POST_ID + " INTEGER"
			+ "," + COMMENT_ID + " INTEGER" + "," + REPLY_ID + " INTEGER"
			+ " )";

	String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (" + AUTHOR + "," + UPS
			+ "," + DOWNS + "," + CREATED + "," + BODY_HTML + "," + IDENTIFIER
			+ "," + PARENTIDENTIFIER + POST_ID + "," + COMMENT_ID + ","
			+ REPLY_ID + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

	String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	String WHERE_ID_EQUALS = ID + "=?";
}
