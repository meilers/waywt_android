package com.sobremesa.waywt.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.sobremesa.waywt.database.tables.CommentTable;

/**
 * Generated model class for usage in your application, defined by classifiers in ecore diagram
 * 		
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.20	 
 */
public class Comment {

	private Long id;
	private java.lang.String author;
	private int ups;
	private int downs;
	private long created;
	private java.lang.String body_html;
	private java.lang.String identifier;

	private final ContentValues values = new ContentValues();

	public Comment() {
	}

	public Comment(final Cursor cursor) {
		setId(cursor.getLong(cursor.getColumnIndex(CommentTable.ID)));
		setAuthor(cursor.getString(cursor.getColumnIndex(CommentTable.AUTHOR)));
		setUps(cursor.getInt(cursor.getColumnIndex(CommentTable.UPS)));
		setDowns(cursor.getInt(cursor.getColumnIndex(CommentTable.DOWNS)));
		setCreated(cursor.getLong(cursor.getColumnIndex(CommentTable.CREATED)));
		setBody_html(cursor.getString(cursor
				.getColumnIndex(CommentTable.BODY_HTML)));
		setIdentifier(cursor.getString(cursor
				.getColumnIndex(CommentTable.IDENTIFIER)));

	}

	/**
	 * Set id
	 *
	 * @param id from type java.lang.Long
	 */
	public void setId(final Long id) {
		this.id = id;
		this.values.put(CommentTable.ID, id);
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
		this.values.put(CommentTable.AUTHOR, author);
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
		this.values.put(CommentTable.UPS, ups);
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
		this.values.put(CommentTable.DOWNS, downs);
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
		this.values.put(CommentTable.CREATED, created);
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
		this.values.put(CommentTable.BODY_HTML, body_html);
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
		this.values.put(CommentTable.IDENTIFIER, identifier);
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
