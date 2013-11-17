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

package com.sobremesa.waywt.util;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

import android.app.Activity;
import android.net.Uri;
import android.text.style.URLSpan;
import android.util.Log;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.common.Constants;

public class Util {
	
	private static final String TAG = "Util";
	
	public static ArrayList<String> extractUris(URLSpan[] spans) {
        int size = spans.length;
        ArrayList<String> accumulator = new ArrayList<String>();

        for (int i = 0; i < size; i++) {
            accumulator.add(spans[i].getURL());
        }
        return accumulator;
    }
	
	/**
	 * Convert HTML tags so they will be properly recognized by
	 * android.text.Html.fromHtml()
	 * @param html unescaped HTML
	 * @return converted HTML
	 */
	public static String convertHtmlTags(String html) {
		// Handle <code>
		html = html.replaceAll("<code>", "<tt>").replaceAll("</code>", "</tt>");
		
		// Handle <pre>
		int preIndex = html.indexOf("<pre>");
		int preEndIndex = -6;  // -"</pre>".length()
		StringBuilder bodyConverted = new StringBuilder();
		while (preIndex != -1) {
			// get the text between previous </pre> and next <pre>.
			bodyConverted = bodyConverted.append(html.substring(preEndIndex + 6, preIndex));
			preEndIndex = html.indexOf("</pre>", preIndex);
			// Replace newlines with <br> inside the <pre></pre>
			// Retain <pre> tags since android.text.Html.fromHtml() will ignore them anyway.
			bodyConverted = bodyConverted.append(html.substring(preIndex, preEndIndex).replaceAll("\n", "<br>"))
				.append("</pre>");
			preIndex = html.indexOf("<pre>", preEndIndex);
		}
		html = bodyConverted.append(html.substring(preEndIndex + 6)).toString();
		
		// Handle <li>
		html = html.replaceAll("<li>(<p>)?", "&#8226; ")
		           .replaceAll("(</p>)?</li>", "<br>");
		
		// Handle <strong> and <em>, which are normally <b> and <i> respectively, but reversed in Android.
		// ANDROID BUG: http://code.google.com/p/android/issues/detail?id=3473
		html = html.replaceAll("<strong>", "<b>").replaceAll("</strong>", "</b>")
		           .replaceAll("<em>", "<i>").replaceAll("</em>", "</i>");
		
		return html;
	}
	
	/**
	 * To the second, not millisecond like reddit
	 * @param timeSeconds
	 * @return
	 */
	public static String getTimeAgo(long utcTimeSeconds) {
		long systime = System.currentTimeMillis() / 1000;
		long diff = systime - utcTimeSeconds;
		if (diff <= 0)
			return "very recently";
		else if (diff < 60) {
			if (diff == 1)
				return "1 second ago";
			else
				return diff + " seconds ago";
		}
		else if (diff < 3600) {
			if ((diff / 60) == 1)
				return "1 minute ago";
			else
				return (diff / 60) + " minutes ago";
		}
		else if (diff < 86400) { // 86400 seconds in a day
			if ((diff / 3600) == 1)
				return "1 hour ago";
			else
				return (diff / 3600) + " hours ago";
		}
		else if (diff < 604800) { // 86400 * 7
			if ((diff / 86400) == 1)
				return "1 day ago";
			else
				return (diff / 86400) + " days ago";
		}
		else if (diff < 2592000) { // 86400 * 30
			if ((diff / 604800) == 1)
				return "1 week ago";
			else
				return (diff / 604800) + " weeks ago";
		}
		else if (diff < 31536000) { // 86400 * 365
			if ((diff / 2592000) == 1)
				return "1 month ago";
			else
				return (diff / 2592000) + " months ago";
		}
		else {
			if ((diff / 31536000) == 1)
				return "1 year ago";
			else
				return (diff / 31536000) + " years ago";
		}
	}
	
	public static String getTimeAgo(double utcTimeSeconds) {
		return getTimeAgo((long)utcTimeSeconds);
	}
	
	public static String showNumComments(int comments) {
		if (comments == 1) {
			return "1 comment";
		} else {
			return comments + " comments";
		}
	}
	
	public static String showNumPoints(int score) {
		if (score == 1) {
			return "1 point";
		} else {
			return score + " points";
		}
	}
	
	public static String absolutePathToURL(String path) {
		if (path.startsWith("/"))
			return Constants.REDDIT_BASE_URL + path;
		return path;
	}
	
	public static String nameToId(String name) {
		// indexOf('_') == -1 if not found; -1 + 1 == 0
		return name.substring(name.indexOf('_') + 1);
	}
	
	public static boolean isHttpStatusOK(HttpResponse response) {
		if (response == null || response.getStatusLine() == null) {
			return false;
		}
		return response.getStatusLine().getStatusCode() == 200;
	}
	
	public static String getResponseErrorMessage(String line) throws Exception{
    	String error = null;
		
		if (StringUtils.isEmpty(line)) {
			error = "Connection error when subscribing. Try again.";
    		throw new HttpException("No content returned from subscribe POST");
    	}
    	if (line.contains("WRONG_PASSWORD")) {
    		error = "Wrong password.";
    		throw new Exception("Wrong password.");
    	}
    	if (line.contains("USER_REQUIRED")) {
    		// The modhash probably expired
    		throw new Exception("User required. Huh?");
    	}
    	
		return error;
	}

	
	
	// ===============
	//   Transitions
	// ===============
	
	public static void overridePendingTransition(Method activity_overridePendingTransition, Activity act, int enterAnim, int exitAnim) {
		// only available in Android 2.0 (SDK Level 5) and later
		if (activity_overridePendingTransition != null) {
			try {
				activity_overridePendingTransition.invoke(act, enterAnim, exitAnim);
			} catch (Exception ex) {
				if (Constants.LOGGING) Log.e(TAG, "overridePendingTransition", ex);
			}
		}
	}



    
}
