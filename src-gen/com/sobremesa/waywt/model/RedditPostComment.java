package com.sobremesa.waywt.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;

/**
 * Generated model class for usage in your application, defined by classifiers in ecore diagram
 * 		
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.11	 
 */
public class RedditPostComment {

	private Long id;
	private java.lang.String author;
	private int ups;
	private int downs;
	private long created;
	private java.lang.String body_html;
	private java.lang.String identifier;

	private final ContentValues values = new ContentValues();

	public RedditPostComment() {
	}

	public RedditPostComment(final Cursor cursor) {
		setId(cursor.getLong(cursor.getColumnIndex(RedditPostCommentTable.ID)));
		setAuthor(cursor.getString(cursor
				.getColumnIndex(RedditPostCommentTable.AUTHOR)));
		setUps(cursor.getInt(cursor.getColumnIndex(RedditPostCommentTable.UPS)));
		setDowns(cursor.getInt(cursor
				.getColumnIndex(RedditPostCommentTable.DOWNS)));
		setCreated(cursor.getLong(cursor
				.getColumnIndex(RedditPostCommentTable.CREATED)));
		setBody_html(cursor.getString(cursor
				.getColumnIndex(RedditPostCommentTable.BODY_HTML)));
		setIdentifier(cursor.getString(cursor
				.getColumnIndex(RedditPostCommentTable.IDENTIFIER)));

	}

	/**
	 * Set id
	 *
	 * @param id from type java.lang.Long
	 */
	public void setId(final Long id) {
		this.id = id;
		this.values.put(RedditPostCommentTable.ID, id);
	}

	/**
	 * Get id
	 *
	 * @return id from type java.lang.Long				
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * Set author and set content value
	 *
	 * @param author from type java.lang.String
	 */
	public void setAuthor(final java.lang.String author) {
		this.author = author;
		this.values.put(RedditPostCommentTable.AUTHOR, author);
	}

	/**
	 * Get author
	 *
	 * @return author from type java.lang.String				
	 */
	public java.lang.String getAuthor() {
		return this.author;
	}

	/**
	 * Set ups and set content value
	 *
	 * @param ups from type int
	 */
	public void setUps(final int ups) {
		this.ups = ups;
		this.values.put(RedditPostCommentTable.UPS, ups);
	}

	/**
	 * Get ups
	 *
	 * @return ups from type int				
	 */
	public int getUps() {
		return this.ups;
	}

	/**
	 * Set downs and set content value
	 *
	 * @param downs from type int
	 */
	public void setDowns(final int downs) {
		this.downs = downs;
		this.values.put(RedditPostCommentTable.DOWNS, downs);
	}

	/**
	 * Get downs
	 *
	 * @return downs from type int				
	 */
	public int getDowns() {
		return this.downs;
	}

	/**
	 * Set created and set content value
	 *
	 * @param created from type long
	 */
	public void setCreated(final long created) {
		this.created = created;
		this.values.put(RedditPostCommentTable.CREATED, created);
	}

	/**
	 * Get created
	 *
	 * @return created from type long				
	 */
	public long getCreated() {
		return this.created;
	}

	/**
	 * Set body_html and set content value
	 *
	 * @param body_html from type java.lang.String
	 */
	public void setBody_html(final java.lang.String body_html) {
		this.body_html = body_html;
		this.values.put(RedditPostCommentTable.BODY_HTML, body_html);
	}

	/**
	 * Get body_html
	 *
	 * @return body_html from type java.lang.String				
	 */
	public java.lang.String getBody_html() {
		return this.body_html;
	}

	/**
	 * Set identifier and set content value
	 *
	 * @param identifier from type java.lang.String
	 */
	public void setIdentifier(final java.lang.String identifier) {
		this.identifier = identifier;
		this.values.put(RedditPostCommentTable.IDENTIFIER, identifier);
	}

	/**
	 * Get identifier
	 *
	 * @return identifier from type java.lang.String				
	 */
	public java.lang.String getIdentifier() {
		return this.identifier;
	}

	/**
	 * Get ContentValues
	 *
	 * @return id from type android.content.ContentValues with the values of this object				
	 */
	public ContentValues getContentValues() {
		return this.values;
	}
}
