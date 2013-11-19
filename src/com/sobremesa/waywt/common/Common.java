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

package com.sobremesa.waywt.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import android.app.Activity;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.exceptions.CaptchaException;
import com.sobremesa.waywt.util.StringUtils;
import com.sobremesa.waywt.util.Util;
import com.sobremesa.waywt.settings.RedditSettings;

public class Common {
	
	private static final String TAG = "Common";
	
	// 1:subreddit 2:threadId 3:commentId
	private static final Pattern COMMENT_LINK = Pattern.compile(Constants.COMMENT_PATH_PATTERN_STRING);
	private static final Pattern REDDIT_LINK = Pattern.compile(Constants.REDDIT_PATH_PATTERN_STRING);
	private static final Pattern USER_LINK = Pattern.compile(Constants.USER_PATH_PATTERN_STRING);
	private static final ObjectMapper mObjectMapper = new ObjectMapper();
	
	public static void showErrorToast(String error, int duration, Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Toast t = new Toast(context);
		t.setDuration(duration);
		View v = inflater.inflate(R.layout.error_toast, null);
		TextView errorMessage = (TextView) v.findViewById(R.id.errorMessage);
		errorMessage.setText(error);
		t.setView(v);
		t.show();
	}
	
    public static boolean shouldLoadThumbnails(Activity activity, RedditSettings settings) {
    	//check for wifi connection and wifi thumbnail setting
    	boolean thumbOkay = true;
    	if (settings.isLoadThumbnailsOnlyWifi())
    	{
    		thumbOkay = false;
    		ConnectivityManager connMan = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    		NetworkInfo netInfo = connMan.getActiveNetworkInfo();
    		if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && netInfo.isConnected()) {
    			thumbOkay = true;
    		}
    	}
    	return settings.isLoadThumbnails() && thumbOkay;
    }
    
	
    
	
    static void clearCookies(RedditSettings settings, HttpClient client, Context context) {
        settings.setRedditSessionCookie(null);

        RedditIsFunHttpClientFactory.getCookieStore().clear();
        CookieSyncManager.getInstance().sync();
        
        SharedPreferences sessionPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    	SharedPreferences.Editor editor = sessionPrefs.edit();
    	editor.remove("reddit_sessionValue");
    	editor.remove("reddit_sessionDomain");
    	editor.remove("reddit_sessionPath");
    	editor.remove("reddit_sessionExpiryDate");
        editor.commit();
    }
    
        
    public static void doLogout(RedditSettings settings, HttpClient client, Context context) {
    	clearCookies(settings, client, context);
    	CacheInfo.invalidateAllCaches(context);
    	settings.setUsername(null);
    }
    
    
    /**
     * Get a new modhash by scraping and return it
     * 
     * @param client
     * @return
     */
    public static String doUpdateModhash(HttpClient client) {
        final Pattern MODHASH_PATTERN = Pattern.compile("modhash: '(.*?)'");
    	String modhash;
    	HttpEntity entity = null;
        // The pattern to find modhash from HTML javascript area
    	try {
    		HttpGet httpget = new HttpGet(Constants.MODHASH_URL);
    		HttpResponse response = client.execute(httpget);
    		
    		// For modhash, we don't care about the status, since the 404 page has the info we want.
//    		status = response.getStatusLine().toString();
//        	if (!status.contains("OK"))
//        		throw new HttpException(status);
        	
        	entity = response.getEntity();

        	BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
        	// modhash should appear within first 1200 chars
        	char[] buffer = new char[1200];
        	in.read(buffer, 0, 1200);
        	in.close();
        	String line = String.valueOf(buffer);
        	entity.consumeContent();
        	
        	if (StringUtils.isEmpty(line)) {
        		throw new HttpException("No content returned from doUpdateModhash GET to "+Constants.MODHASH_URL);
        	}
        	if (line.contains("USER_REQUIRED")) {
        		throw new Exception("User session error: USER_REQUIRED");
        	}
        	
        	Matcher modhashMatcher = MODHASH_PATTERN.matcher(line);
        	if (modhashMatcher.find()) {
        		modhash = modhashMatcher.group(1);
        		if (StringUtils.isEmpty(modhash)) {
        			// Means user is not actually logged in.
        			return null;
        		}
        	} else {
        		throw new Exception("No modhash found at URL "+Constants.MODHASH_URL);
        	}

        	if (Constants.LOGGING) Common.logDLong(TAG, line);
        	
        	if (Constants.LOGGING) Log.d(TAG, "modhash: "+modhash);
        	return modhash;
        	
    	} catch (Exception e) {
    		if (entity != null) {
    			try {
    				entity.consumeContent();
    			} catch (Exception e2) {
    				if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e);
    			}
    		}
    		if (Constants.LOGGING) Log.e(TAG, "doUpdateModhash()", e);
    		return null;
    	}
    }
    
    public static String checkResponseErrors(HttpResponse response, HttpEntity entity) {
    	String status = response.getStatusLine().toString();
    	String line;
    	
    	if (!status.contains("OK")) {
    		return "HTTP error. Status = "+status;
    	}
    	
    	try {
    		BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
    		line = in.readLine();
    		if (Constants.LOGGING) Common.logDLong(TAG, line);
        	in.close();
    	} catch (IOException e) {
    		if (Constants.LOGGING) Log.e(TAG, "IOException", e);
    		return "Error reading retrieved data.";
    	}
    	
    	if (StringUtils.isEmpty(line)) {
    		return "API returned empty data.";
    	}
    	if (line.contains("WRONG_PASSWORD")) {
    		return "Wrong password.";
    	}
    	if (line.contains("USER_REQUIRED")) {
    		// The modhash probably expired
    		return "Login expired.";
    	}
    	if (line.contains("SUBREDDIT_NOEXIST")) {
    		return "That subreddit does not exist.";
    	}
    	if (line.contains("SUBREDDIT_NOTALLOWED")) {
    		return "You are not allowed to post to that subreddit.";
    	}
    	
    	return null;
    }
    

	public static String checkIDResponse(HttpResponse response, HttpEntity entity) throws CaptchaException, Exception {
	    // Group 1: fullname. Group 2: kind. Group 3: id36.
	    final Pattern NEW_ID_PATTERN = Pattern.compile("\"id\": \"((.+?)_(.+?))\"");
	    // Group 1: whole error. Group 2: the time part
	    final Pattern RATELIMIT_RETRY_PATTERN = Pattern.compile("(you are trying to submit too fast. try again in (.+?)\\.)");

	    String status = response.getStatusLine().toString();
    	String line;
    	
    	if (!status.contains("OK")) {
    		throw new Exception("HTTP error. Status = "+status);
    	}
    	
    	try {
    		BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
    		line = in.readLine();
    		if (Constants.LOGGING) Common.logDLong(TAG, line);
        	in.close();
    	} catch (IOException e) {
    		if (Constants.LOGGING) Log.e(TAG, "IOException", e);
    		throw new Exception("Error reading retrieved data.");
    	}
    	
    	if (StringUtils.isEmpty(line)) {
    		throw new Exception("API returned empty data.");
    	}
    	if (line.contains("WRONG_PASSWORD")) {
    		throw new Exception("Wrong password.");
    	}
    	if (line.contains("USER_REQUIRED")) {
    		// The modhash probably expired
    		throw new Exception("Login expired.");
    	}
    	if (line.contains("SUBREDDIT_NOEXIST")) {
    		throw new Exception("That subreddit does not exist.");
    	}
    	if (line.contains("SUBREDDIT_NOTALLOWED")) {
    		throw new Exception("You are not allowed to post to that subreddit.");
    	}
    	
    	String newId;
    	Matcher idMatcher = NEW_ID_PATTERN.matcher(line);
    	if (idMatcher.find()) {
    		newId = idMatcher.group(3);
    	} else {
    		if (line.contains("RATELIMIT")) {
        		// Try to find the # of minutes using regex
            	Matcher rateMatcher = RATELIMIT_RETRY_PATTERN.matcher(line);
            	if (rateMatcher.find())
            		throw new Exception(rateMatcher.group(1));
            	else
            		throw new Exception("you are trying to submit too fast. try again in a few minutes.");
        	}
    		if (line.contains("DELETED_LINK")) {
    			throw new Exception("the link you are commenting on has been deleted");
    		}
    		if (line.contains("BAD_CAPTCHA")) {
    			throw new CaptchaException("Bad CAPTCHA. Try again.");
    		}
        	// No id returned by reply POST.
    		return null;
    	}
    	
    	// Getting here means success.
    	return newId;
	}
    
	
    
    public static boolean isClicked(Context context, String url) {
    	Cursor cursor;
    	try {
			cursor = context.getContentResolver().query(
					Browser.BOOKMARKS_URI,
					Browser.HISTORY_PROJECTION,
					Browser.HISTORY_PROJECTION[Browser.HISTORY_PROJECTION_URL_INDEX] + "=?",
					new String[]{ url },
					null
			);
    	} catch (Exception ex) {
    		if (Constants.LOGGING) Log.w(TAG, "Error querying Android Browser for history; manually revoked permission?", ex);
    		return false;
    	}
    	
		if (cursor != null) {
	        boolean isClicked = cursor.moveToFirst();  // returns true if cursor is not empty
	        cursor.close();
	        return isClicked;
		} else {
			return false;
		}
    }
    
    public static ObjectMapper getObjectMapper() {
    	return mObjectMapper;
    }
    
	public static void logDLong(String tag, String msg) {
		int c;
		boolean done = false;
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < msg.length(); k += 80) {
			for (int i = 0; i < 80; i++) {
				if (k + i >= msg.length()) {
					done = true;
					break;
				}
				c = msg.charAt(k + i);
				sb.append((char) c);
			}
			if (Constants.LOGGING) Log.d(tag, "multipart log: " + sb.toString());
			sb = new StringBuilder();
			if (done)
				break;
		}
	} 
    
    public static String getSubredditId(String mSubreddit){
    	String subreddit_id = null;
    	JsonNode subredditInfo = 
    	RestJsonClient.connect(Constants.REDDIT_BASE_URL + "/r/" + mSubreddit + "/.json?count=1");
    	    	
    	if(subredditInfo != null){
    		ArrayNode children = (ArrayNode) subredditInfo.path("data").path("children");
    		subreddit_id = children.get(0).get("data").get("subreddit_id").getTextValue();
    	}
    	return subreddit_id;
    }

}
