package com.sobremesa.waywt.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sobremesa.waywt.database.tables.RedditPostTable;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;

/**
 * This database class extends the SQLiteOpenHelper
 * A database file is created: mdsdacpdatabase.db
 * 
 * It is possible to implement an own mechanism to store data on database updates:
 * Write your code inside the defined block inside the "onUpgrade" method!
 *
 * More details about sqlite databases in android:
 * @see <a href="http://developer.android.com/guide/topics/data/data-storage.html#db">Tutorial</a>
 * @see <a href="http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html">Reference</a>
 *
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.05
 */
public class Database extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "mdsdacpdatabase.db";
	private static final int DATABASE_VERSION = 3;

	public Database(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public final void onCreate(final SQLiteDatabase db) {
		db.execSQL(RedditPostTable.SQL_CREATE);
		db.execSQL(ImageTable.SQL_CREATE);
		db.execSQL(RedditPostCommentTable.SQL_CREATE);
	}

	@Override
	public final void onUpgrade(final SQLiteDatabase db, final int oldVersion,
			final int newVersion) {
		/*PROTECTED REGION ID(DatabaseUpdate) ENABLED START*/

		// TODO Implement your database update functionality here and remove the following method call!
		onUpgradeDropTablesAndCreate(db);

		/*PROTECTED REGION END*/
	}

	/**
	 * This basic upgrade functionality will destroy all old data on upgrade
	 */
	private final void onUpgradeDropTablesAndCreate(final SQLiteDatabase db) {
		db.execSQL(RedditPostTable.SQL_DROP);
		db.execSQL(ImageTable.SQL_DROP);
		db.execSQL(RedditPostCommentTable.SQL_DROP);
		onCreate(db);
	}
}
