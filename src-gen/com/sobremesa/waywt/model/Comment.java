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
 * @date 2014.01.25	 
 */
public class Comment {

	private Long id;
	private java.lang.String post_title;
	private java.lang.String post_permalink;
	private java.lang.String comment_id;
	private java.lang.String author;
	private java.lang.String body_html;
	private java.lang.String name;
	private int likes;
	private int ups;
	private int downs;
	private long created;
	private java.lang.String thread_id;
	private int is_male;
	private int is_teen;

	private final ContentValues values = new ContentValues();

	public Comment() {
	}

	public Comment(final Cursor cursor) {
		setId(cursor.getLong(cursor.getColumnIndex(CommentTable.ID)));
		setPost_title(cursor.getString(cursor
				.getColumnIndex(CommentTable.POST_TITLE)));
		setPost_permalink(cursor.getString(cursor
				.getColumnIndex(CommentTable.POST_PERMALINK)));
		setComment_id(cursor.getString(cursor
				.getColumnIndex(CommentTable.COMMENT_ID)));
		setAuthor(cursor.getString(cursor.getColumnIndex(CommentTable.AUTHOR)));
		setBody_html(cursor.getString(cursor
				.getColumnIndex(CommentTable.BODY_HTML)));
		setName(cursor.getString(cursor.getColumnIndex(CommentTable.NAME)));
		setLikes(cursor.getInt(cursor.getColumnIndex(CommentTable.LIKES)));
		setUps(cursor.getInt(cursor.getColumnIndex(CommentTable.UPS)));
		setDowns(cursor.getInt(cursor.getColumnIndex(CommentTable.DOWNS)));
		setCreated(cursor.getLong(cursor.getColumnIndex(CommentTable.CREATED)));
		setThread_id(cursor.getString(cursor
				.getColumnIndex(CommentTable.THREAD_ID)));
		setIs_male(cursor.getInt(cursor.getColumnIndex(CommentTable.IS_MALE)));
		setIs_teen(cursor.getInt(cursor.getColumnIndex(CommentTable.IS_TEEN)));

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
	 * Set post_title and set content value
	 *
	 * @param post_title from type java.lang.String
	 */
	public void setPost_title(final java.lang.String post_title) {
		this.post_title = post_title;
		this.values.put(CommentTable.POST_TITLE, post_title);
	}

	/**
	 * Get post_title
	 *
	 * @return post_title from type java.lang.String				
	 */
	public java.lang.String getPost_title() {
		return this.post_title;
	}

	/**
	 * Set post_permalink and set content value
	 *
	 * @param post_permalink from type java.lang.String
	 */
	public void setPost_permalink(final java.lang.String post_permalink) {
		this.post_permalink = post_permalink;
		this.values.put(CommentTable.POST_PERMALINK, post_permalink);
	}

	/**
	 * Get post_permalink
	 *
	 * @return post_permalink from type java.lang.String				
	 */
	public java.lang.String getPost_permalink() {
		return this.post_permalink;
	}

	/**
	 * Set comment_id and set content value
	 *
	 * @param comment_id from type java.lang.String
	 */
	public void setComment_id(final java.lang.String comment_id) {
		this.comment_id = comment_id;
		this.values.put(CommentTable.COMMENT_ID, comment_id);
	}

	/**
	 * Get comment_id
	 *
	 * @return comment_id from type java.lang.String				
	 */
	public java.lang.String getComment_id() {
		return this.comment_id;
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
	 * Set name and set content value
	 *
	 * @param name from type java.lang.String
	 */
	public void setName(final java.lang.String name) {
		this.name = name;
		this.values.put(CommentTable.NAME, name);
	}

	/**
	 * Get name
	 *
	 * @return name from type java.lang.String				
	 */
	public java.lang.String getName() {
		return this.name;
	}

	/**
	 * Set likes and set content value
	 *
	 * @param likes from type int
	 */
	public void setLikes(final int likes) {
		this.likes = likes;
		this.values.put(CommentTable.LIKES, likes);
	}

	/**
	 * Get likes
	 *
	 * @return likes from type int				
	 */
	public int getLikes() {
		return this.likes;
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
	 * Set thread_id and set content value
	 *
	 * @param thread_id from type java.lang.String
	 */
	public void setThread_id(final java.lang.String thread_id) {
		this.thread_id = thread_id;
		this.values.put(CommentTable.THREAD_ID, thread_id);
	}

	/**
	 * Get thread_id
	 *
	 * @return thread_id from type java.lang.String				
	 */
	public java.lang.String getThread_id() {
		return this.thread_id;
	}

	/**
	 * Set is_male and set content value
	 *
	 * @param is_male from type int
	 */
	public void setIs_male(final int is_male) {
		this.is_male = is_male;
		this.values.put(CommentTable.IS_MALE, is_male);
	}

	/**
	 * Get is_male
	 *
	 * @return is_male from type int				
	 */
	public int getIs_male() {
		return this.is_male;
	}

	/**
	 * Set is_teen and set content value
	 *
	 * @param is_teen from type int
	 */
	public void setIs_teen(final int is_teen) {
		this.is_teen = is_teen;
		this.values.put(CommentTable.IS_TEEN, is_teen);
	}

	/**
	 * Get is_teen
	 *
	 * @return is_teen from type int				
	 */
	public int getIs_teen() {
		return this.is_teen;
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
