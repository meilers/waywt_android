package com.sobremesa.waywt.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.sobremesa.waywt.database.tables.PostTable;

/**
 * Generated model class for usage in your application, defined by classifiers in ecore diagram
 * 		
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.11.15	 
 */
public class Post {

	private Long id;
	private int ups;
	private int downs;
	private java.lang.String author;
	private long created;
	private java.lang.String permalink;
	private java.lang.String title;

	private final ContentValues values = new ContentValues();

	public Post() {
	}

	public Post(final Cursor cursor) {
		setId(cursor.getLong(cursor.getColumnIndex(PostTable.ID)));
		setUps(cursor.getInt(cursor.getColumnIndex(PostTable.UPS)));
		setDowns(cursor.getInt(cursor.getColumnIndex(PostTable.DOWNS)));
		setAuthor(cursor.getString(cursor.getColumnIndex(PostTable.AUTHOR)));
		setCreated(cursor.getLong(cursor.getColumnIndex(PostTable.CREATED)));
		setPermalink(cursor.getString(cursor
				.getColumnIndex(PostTable.PERMALINK)));
		setTitle(cursor.getString(cursor.getColumnIndex(PostTable.TITLE)));

	}

	/**
	 * Set id
	 *
	 * @param id from type java.lang.Long
	 */
	public void setId(final Long id) {
		this.id = id;
		this.values.put(PostTable.ID, id);
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
	 * Set ups and set content value
	 *
	 * @param ups from type int
	 */
	public void setUps(final int ups) {
		this.ups = ups;
		this.values.put(PostTable.UPS, ups);
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
		this.values.put(PostTable.DOWNS, downs);
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
	 * Set author and set content value
	 *
	 * @param author from type java.lang.String
	 */
	public void setAuthor(final java.lang.String author) {
		this.author = author;
		this.values.put(PostTable.AUTHOR, author);
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
	 * Set created and set content value
	 *
	 * @param created from type long
	 */
	public void setCreated(final long created) {
		this.created = created;
		this.values.put(PostTable.CREATED, created);
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
	 * Set permalink and set content value
	 *
	 * @param permalink from type java.lang.String
	 */
	public void setPermalink(final java.lang.String permalink) {
		this.permalink = permalink;
		this.values.put(PostTable.PERMALINK, permalink);
	}

	/**
	 * Get permalink
	 *
	 * @return permalink from type java.lang.String				
	 */
	public java.lang.String getPermalink() {
		return this.permalink;
	}

	/**
	 * Set title and set content value
	 *
	 * @param title from type java.lang.String
	 */
	public void setTitle(final java.lang.String title) {
		this.title = title;
		this.values.put(PostTable.TITLE, title);
	}

	/**
	 * Get title
	 *
	 * @return title from type java.lang.String				
	 */
	public java.lang.String getTitle() {
		return this.title;
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
