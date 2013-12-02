package com.sobremesa.waywt.model;

import android.content.ContentValues;
import android.database.Cursor;
import com.sobremesa.waywt.database.tables.ImageTable;

/**
 * Generated model class for usage in your application, defined by classifiers in ecore diagram
 * 		
 * Generated Class. Do not modify!
 * 
 * @author MDSDACP Team - goetzfred@fh-bingen.de 
 * @date 2013.12.01	 
 */
public class Image {

	private Long id;
	private java.lang.String url;
	private java.lang.String identifier;

	private final ContentValues values = new ContentValues();

	public Image() {
	}

	public Image(final Cursor cursor) {
		setId(cursor.getLong(cursor.getColumnIndex(ImageTable.ID)));
		setUrl(cursor.getString(cursor.getColumnIndex(ImageTable.URL)));
		setIdentifier(cursor.getString(cursor
				.getColumnIndex(ImageTable.IDENTIFIER)));

	}

	/**
	 * Set id
	 *
	 * @param id from type java.lang.Long
	 */
	public void setId(final Long id) {
		this.id = id;
		this.values.put(ImageTable.ID, id);
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
	 * Set url and set content value
	 *
	 * @param url from type java.lang.String
	 */
	public void setUrl(final java.lang.String url) {
		this.url = url;
		this.values.put(ImageTable.URL, url);
	}

	/**
	 * Get url
	 *
	 * @return url from type java.lang.String				
	 */
	public java.lang.String getUrl() {
		return this.url;
	}

	/**
	 * Set identifier and set content value
	 *
	 * @param identifier from type java.lang.String
	 */
	public void setIdentifier(final java.lang.String identifier) {
		this.identifier = identifier;
		this.values.put(ImageTable.IDENTIFIER, identifier);
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
