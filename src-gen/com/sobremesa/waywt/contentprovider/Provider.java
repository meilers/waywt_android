package com.sobremesa.waywt.contentprovider;

import com.sobremesa.waywt.database.Database;

import com.sobremesa.waywt.database.tables.*;

import android.provider.BaseColumns;
import android.text.TextUtils;
import android.content.ContentUris;
import android.database.sqlite.SQLiteQueryBuilder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Content provider implementation
 * The authority of the content provider is: content://com.sobremesa.waywt.provider.Model
 * 
 * More information about content providers:
 * @see <a href="http://developer.android.com/reference/android/content/ContentProvider.html">Reference</a>
 * @see <a href="http://developer.android.com/guide/topics/providers/content-providers.html">Tutorial</a>
 * @see <a href="http://developer.android.com/guide/topics/testing/contentprovider_testing.html">Content Provider Testing</a>
 *
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.09
 */
public class Provider extends ContentProvider {
	private static final String TAG = "com.sobremesa.waywt.contentprovider.Provider";

	public static final String AUTHORITY = "com.sobremesa.waywt.provider.Model";
	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	public static final Uri REDDITPOST_CONTENT_URI = Uri.withAppendedPath(
			Provider.AUTHORITY_URI, RedditPostContent.CONTENT_PATH);

	public static final Uri IMAGE_CONTENT_URI = Uri.withAppendedPath(
			Provider.AUTHORITY_URI, ImageContent.CONTENT_PATH);

	public static final Uri REDDITPOSTCOMMENT_CONTENT_URI = Uri
			.withAppendedPath(Provider.AUTHORITY_URI,
					RedditPostCommentContent.CONTENT_PATH);

	private static final UriMatcher URI_MATCHER;

	private Database db;

	private static final int REDDITPOST_DIR = 0;
	private static final int REDDITPOST_ID = 1;
	private static final int IMAGE_DIR = 2;
	private static final int IMAGE_ID = 3;
	private static final int REDDITPOSTCOMMENT_DIR = 4;
	private static final int REDDITPOSTCOMMENT_ID = 5;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, RedditPostContent.CONTENT_PATH,
				REDDITPOST_DIR);
		URI_MATCHER.addURI(AUTHORITY, RedditPostContent.CONTENT_PATH + "/#",
				REDDITPOST_ID);
		URI_MATCHER.addURI(AUTHORITY, ImageContent.CONTENT_PATH, IMAGE_DIR);
		URI_MATCHER.addURI(AUTHORITY, ImageContent.CONTENT_PATH + "/#",
				IMAGE_ID);
		URI_MATCHER.addURI(AUTHORITY, RedditPostCommentContent.CONTENT_PATH,
				REDDITPOSTCOMMENT_DIR);
		URI_MATCHER.addURI(AUTHORITY, RedditPostCommentContent.CONTENT_PATH
				+ "/#", REDDITPOSTCOMMENT_ID);
	}

	/**
	 * Provides the content information of the RedditPostTable.
	 * 
	 * CONTENT_PATH: redditpost (String)
	 * CONTENT_TYPE: vnd.android.cursor.dir/vnd.mdsdacp.redditpost (String)
	 * CONTENT_ITEM_TYPE: vnd.android.cursor.item/vnd.mdsdacp.redditpost (String)
	 * ALL_COLUMNS: Provides the same information as RedditPostTable.ALL_COLUMNS (String[])
	 */
	public static final class RedditPostContent implements BaseColumns {
		/**
		 * Specifies the content path of the RedditPostTable for the required uri
		 * Exact URI: content://com.sobremesa.waywt.provider.Model/redditpost
		 */
		public static final String CONTENT_PATH = "redditpost";

		/**
		 * Specifies the type for the folder and the single item of the RedditPostTable  
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mdsdacp.redditpost";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mdsdacp.redditpost";

		/**
		 * Contains all columns of the RedditPostTable
		 */
		public static final String[] ALL_COLUMNS = RedditPostTable.ALL_COLUMNS;
	}

	/**
	 * Provides the content information of the ImageTable.
	 * 
	 * CONTENT_PATH: image (String)
	 * CONTENT_TYPE: vnd.android.cursor.dir/vnd.mdsdacp.image (String)
	 * CONTENT_ITEM_TYPE: vnd.android.cursor.item/vnd.mdsdacp.image (String)
	 * ALL_COLUMNS: Provides the same information as ImageTable.ALL_COLUMNS (String[])
	 */
	public static final class ImageContent implements BaseColumns {
		/**
		 * Specifies the content path of the ImageTable for the required uri
		 * Exact URI: content://com.sobremesa.waywt.provider.Model/image
		 */
		public static final String CONTENT_PATH = "image";

		/**
		 * Specifies the type for the folder and the single item of the ImageTable  
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mdsdacp.image";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mdsdacp.image";

		/**
		 * Contains all columns of the ImageTable
		 */
		public static final String[] ALL_COLUMNS = ImageTable.ALL_COLUMNS;
	}

	/**
	 * Provides the content information of the RedditPostCommentTable.
	 * 
	 * CONTENT_PATH: redditpostcomment (String)
	 * CONTENT_TYPE: vnd.android.cursor.dir/vnd.mdsdacp.redditpostcomment (String)
	 * CONTENT_ITEM_TYPE: vnd.android.cursor.item/vnd.mdsdacp.redditpostcomment (String)
	 * ALL_COLUMNS: Provides the same information as RedditPostCommentTable.ALL_COLUMNS (String[])
	 */
	public static final class RedditPostCommentContent implements BaseColumns {
		/**
		 * Specifies the content path of the RedditPostCommentTable for the required uri
		 * Exact URI: content://com.sobremesa.waywt.provider.Model/redditpostcomment
		 */
		public static final String CONTENT_PATH = "redditpostcomment";

		/**
		 * Specifies the type for the folder and the single item of the RedditPostCommentTable  
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mdsdacp.redditpostcomment";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.mdsdacp.redditpostcomment";

		/**
		 * Contains all columns of the RedditPostCommentTable
		 */
		public static final String[] ALL_COLUMNS = RedditPostCommentTable.ALL_COLUMNS;
	}

	/**
	 * Instantiate the database, when the content provider is created
	 */
	@Override
	public final boolean onCreate() {
		db = new Database(getContext());
		return true;
	}

	/**
	 * Providing information whether uri returns an item or an directory.
	 * 
	 * @param uri from type Uri
	 * 
	 * @return content_type from type Content.CONTENT_TYPE or Content.CONTENT_ITEM_TYPE
	 *
	 */
	@Override
	public final String getType(final Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case REDDITPOST_DIR :
				return RedditPostContent.CONTENT_TYPE;
			case REDDITPOST_ID :
				return RedditPostContent.CONTENT_ITEM_TYPE;
			case IMAGE_DIR :
				return ImageContent.CONTENT_TYPE;
			case IMAGE_ID :
				return ImageContent.CONTENT_ITEM_TYPE;
			case REDDITPOSTCOMMENT_DIR :
				return RedditPostCommentContent.CONTENT_TYPE;
			case REDDITPOSTCOMMENT_ID :
				return RedditPostCommentContent.CONTENT_ITEM_TYPE;
			default :
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	/**
	 * Insert given values to given uri. Uri has to be from type directory (see switch-cases).
	 * Returns uri of inserted element.
	 *
	 * @param uri from type Uri
	 * @param values from type ContentValues
	 *
	 * @return uri of inserted element from type Uri
	 */
	@Override
	public final Uri insert(final Uri uri, final ContentValues values) {
		final SQLiteDatabase dbConnection = db.getWritableDatabase();

		try {
			dbConnection.beginTransaction();

			switch (URI_MATCHER.match(uri)) {
				case REDDITPOST_DIR :
				case REDDITPOST_ID :
					final long redditpostid = dbConnection.insertOrThrow(
							RedditPostTable.TABLE_NAME, null, values);
					final Uri newRedditPost = ContentUris.withAppendedId(
							REDDITPOST_CONTENT_URI, redditpostid);
					getContext().getContentResolver().notifyChange(
							newRedditPost, null);
					dbConnection.setTransactionSuccessful();
					return newRedditPost;
				case IMAGE_DIR :
				case IMAGE_ID :
					final long imageid = dbConnection.insertOrThrow(
							ImageTable.TABLE_NAME, null, values);
					final Uri newImage = ContentUris.withAppendedId(
							IMAGE_CONTENT_URI, imageid);
					getContext().getContentResolver().notifyChange(newImage,
							null);
					dbConnection.setTransactionSuccessful();
					return newImage;
				case REDDITPOSTCOMMENT_DIR :
				case REDDITPOSTCOMMENT_ID :
					final long redditpostcommentid = dbConnection
							.insertOrThrow(RedditPostCommentTable.TABLE_NAME,
									null, values);
					final Uri newRedditPostComment = ContentUris
							.withAppendedId(REDDITPOSTCOMMENT_CONTENT_URI,
									redditpostcommentid);
					getContext().getContentResolver().notifyChange(
							newRedditPostComment, null);
					dbConnection.setTransactionSuccessful();
					return newRedditPostComment;
				default :
					throw new IllegalArgumentException("Unsupported URI:" + uri);
			}
		} catch (Exception e) {
			Log.e(TAG, "Insert Exception", e);
		} finally {
			dbConnection.endTransaction();
		}

		return null;
	}

	/**
	 * Updates given values of given uri, returning number of affected rows.
	 *
	 * @param uri from type Uri
	 * @param values from type ContentValues
	 * @param selection from type String
	 * @param selectionArgs from type String[]
	 *
	 * @return number of affected rows from type int
	 */
	@Override
	public final int update(final Uri uri, final ContentValues values,
			final String selection, final String[] selectionArgs) {

		final SQLiteDatabase dbConnection = db.getWritableDatabase();
		int updateCount = 0;

		try {
			dbConnection.beginTransaction();

			switch (URI_MATCHER.match(uri)) {

				case REDDITPOST_DIR :
					updateCount = dbConnection.update(
							RedditPostTable.TABLE_NAME, values, selection,
							selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;
				case REDDITPOST_ID :
					final Long redditpostId = ContentUris.parseId(uri);
					updateCount = dbConnection.update(
							RedditPostTable.TABLE_NAME, values,
							RedditPostTable.ID
									+ "="
									+ redditpostId
									+ (TextUtils.isEmpty(selection)
											? ""
											: " AND (" + selection + ")"),
							selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;

				case IMAGE_DIR :
					updateCount = dbConnection.update(ImageTable.TABLE_NAME,
							values, selection, selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;
				case IMAGE_ID :
					final Long imageId = ContentUris.parseId(uri);
					updateCount = dbConnection.update(
							ImageTable.TABLE_NAME,
							values,
							ImageTable.ID
									+ "="
									+ imageId
									+ (TextUtils.isEmpty(selection)
											? ""
											: " AND (" + selection + ")"),
							selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;

				case REDDITPOSTCOMMENT_DIR :
					updateCount = dbConnection.update(
							RedditPostCommentTable.TABLE_NAME, values,
							selection, selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;
				case REDDITPOSTCOMMENT_ID :
					final Long redditpostcommentId = ContentUris.parseId(uri);
					updateCount = dbConnection.update(
							RedditPostCommentTable.TABLE_NAME,
							values,
							RedditPostCommentTable.ID
									+ "="
									+ redditpostcommentId
									+ (TextUtils.isEmpty(selection)
											? ""
											: " AND (" + selection + ")"),
							selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;
				default :
					throw new IllegalArgumentException("Unsupported URI:" + uri);
			}
		} finally {
			dbConnection.endTransaction();
		}

		if (updateCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return updateCount;

	}

	/**
	 * Deletes given elements by their uri (items or directories) and returns number of deleted rows.
	 *
	 * @param uri from type Uri
	 * @param selection from type String
	 * @param selectionArgs from type String[]
	 *
	 * @return number of deleted rows from type int
	 */
	@Override
	public final int delete(final Uri uri, final String selection,
			final String[] selectionArgs) {

		final SQLiteDatabase dbConnection = db.getWritableDatabase();
		int deleteCount = 0;

		try {
			dbConnection.beginTransaction();

			switch (URI_MATCHER.match(uri)) {
				case REDDITPOST_DIR :
					deleteCount = dbConnection.delete(
							RedditPostTable.TABLE_NAME, selection,
							selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;
				case REDDITPOST_ID :
					deleteCount = dbConnection.delete(
							RedditPostTable.TABLE_NAME,
							RedditPostTable.WHERE_ID_EQUALS, new String[]{uri
									.getPathSegments().get(1)});
					dbConnection.setTransactionSuccessful();
					break;
				case IMAGE_DIR :
					deleteCount = dbConnection.delete(ImageTable.TABLE_NAME,
							selection, selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;
				case IMAGE_ID :
					deleteCount = dbConnection.delete(ImageTable.TABLE_NAME,
							ImageTable.WHERE_ID_EQUALS, new String[]{uri
									.getPathSegments().get(1)});
					dbConnection.setTransactionSuccessful();
					break;
				case REDDITPOSTCOMMENT_DIR :
					deleteCount = dbConnection.delete(
							RedditPostCommentTable.TABLE_NAME, selection,
							selectionArgs);
					dbConnection.setTransactionSuccessful();
					break;
				case REDDITPOSTCOMMENT_ID :
					deleteCount = dbConnection.delete(
							RedditPostCommentTable.TABLE_NAME,
							RedditPostCommentTable.WHERE_ID_EQUALS,
							new String[]{uri.getPathSegments().get(1)});
					dbConnection.setTransactionSuccessful();
					break;

				default :
					throw new IllegalArgumentException("Unsupported URI:" + uri);
			}
		} finally {
			dbConnection.endTransaction();
		}

		if (deleteCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return deleteCount;

	}

	/**
	 * Executes a query on a given uri and returns a Cursor with results.
	 *
	 * @param uri from type Uri
	 * @param projection from type String[]
	 * @param selection from type String 
	 * @param selectionArgs from type String[]
	 * @param sortOrder from type String
	 *
	 * @return cursor with results from type Cursor
	 */
	@Override
	public final Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder) {

		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		final SQLiteDatabase dbConnection = db.getReadableDatabase();

		switch (URI_MATCHER.match(uri)) {
			case REDDITPOST_ID :
				queryBuilder.appendWhere(RedditPostTable.ID + "="
						+ uri.getPathSegments().get(1));
			case REDDITPOST_DIR :
				queryBuilder.setTables(RedditPostTable.TABLE_NAME);
				break;
			case IMAGE_ID :
				queryBuilder.appendWhere(ImageTable.ID + "="
						+ uri.getPathSegments().get(1));
			case IMAGE_DIR :
				queryBuilder.setTables(ImageTable.TABLE_NAME);
				break;
			case REDDITPOSTCOMMENT_ID :
				queryBuilder.appendWhere(RedditPostCommentTable.ID + "="
						+ uri.getPathSegments().get(1));
			case REDDITPOSTCOMMENT_DIR :
				queryBuilder.setTables(RedditPostCommentTable.TABLE_NAME);
				break;
			default :
				throw new IllegalArgumentException("Unsupported URI:" + uri);
		}

		Cursor cursor = queryBuilder.query(dbConnection, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;

	}

}
