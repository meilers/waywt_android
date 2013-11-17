/*
 * Copyright 2009 Andrew Shu
 *
 * This file is part of "reddit is fun".
 *
 * "reddit is fun" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * "reddit is fun" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "reddit is fun".  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sobremesa.waywt.settings;

import java.util.Date;

import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.common.RedditIsFunHttpClientFactory;

/**
 * Common settings
 * @author Andrew
 *
 */
public class RedditSettings {
	
	private static final String TAG = "RedditSettings";
	
	private String username = null;
	private Cookie redditSessionCookie = null;
	private String modhash = null;
	
	
	
	//
	// --- Methods ---
	//
	
	// --- Preferences ---
	public static class Rotation {
		/* From http://developer.android.com/reference/android/R.attr.html#screenOrientation
		 * unspecified -1
		 * landscape 0
		 * portrait 1
		 * user 2
		 * behind 3
		 * sensor 4
		 * nosensor 5
		 */
		public static int valueOf(String valueString) {
			if (Constants.PREF_ROTATION_UNSPECIFIED.equals(valueString))
				return -1;
			if (Constants.PREF_ROTATION_PORTRAIT.equals(valueString))
				return 1;
			if (Constants.PREF_ROTATION_LANDSCAPE.equals(valueString))
				return 0;
			return -1;
		}
		public static String toString(int value) {
			switch (value) {
			case -1:
				return Constants.PREF_ROTATION_UNSPECIFIED;
			case 1:
				return Constants.PREF_ROTATION_PORTRAIT;
			case 0:
				return Constants.PREF_ROTATION_LANDSCAPE;
			default:
				return Constants.PREF_ROTATION_UNSPECIFIED;
			}
		}
	}
	
    public void saveRedditPreferences(Context context) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	// Session
    	if (this.username != null)
    		editor.putString("username", this.username);
    	else
    		editor.remove("username");
    	if (this.redditSessionCookie != null) {
    		editor.putString("reddit_sessionValue",  this.redditSessionCookie.getValue());
    		editor.putString("reddit_sessionDomain", this.redditSessionCookie.getDomain());
    		editor.putString("reddit_sessionPath",   this.redditSessionCookie.getPath());
    		if (this.redditSessionCookie.getExpiryDate() != null)
    			editor.putLong("reddit_sessionExpiryDate", this.redditSessionCookie.getExpiryDate().getTime());
    	}
    	if (this.modhash != null)
    		editor.putString("modhash", this.modhash.toString());
    	
    	editor.commit();
    }
    
    public void loadRedditPreferences(Context context, HttpClient client) {
        // Session
    	SharedPreferences sessionPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    	this.setUsername(sessionPrefs.getString("username", null));
    	this.setModhash(sessionPrefs.getString("modhash", null));
        String cookieValue = sessionPrefs.getString("reddit_sessionValue", null);
        String cookieDomain = sessionPrefs.getString("reddit_sessionDomain", null);
        String cookiePath = sessionPrefs.getString("reddit_sessionPath", null);
        long cookieExpiryDate = sessionPrefs.getLong("reddit_sessionExpiryDate", -1);
        if (cookieValue != null) {
        	BasicClientCookie redditSessionCookie = new BasicClientCookie("reddit_session", cookieValue);
        	redditSessionCookie.setDomain(cookieDomain);
        	redditSessionCookie.setPath(cookiePath);
        	if (cookieExpiryDate != -1)
        		redditSessionCookie.setExpiryDate(new Date(cookieExpiryDate));
        	else
        		redditSessionCookie.setExpiryDate(null);
        	this.setRedditSessionCookie(redditSessionCookie);
    		RedditIsFunHttpClientFactory.getCookieStore().addCookie(redditSessionCookie);
    		try {
    			CookieSyncManager.getInstance().sync();
    		} catch (IllegalStateException ex) {
    			if (Constants.LOGGING) Log.e(TAG, "CookieSyncManager.getInstance().sync()", ex);
    		}
        }
        

    }
    

	public boolean isLoggedIn() {
		return username != null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Cookie getRedditSessionCookie() {
		return redditSessionCookie;
	}

	public void setRedditSessionCookie(Cookie redditSessionCookie) {
		this.redditSessionCookie = redditSessionCookie;
	}

	public String getModhash() {
		return modhash;
	}

	public void setModhash(String modhash) {
		this.modhash = modhash;
	}


}
