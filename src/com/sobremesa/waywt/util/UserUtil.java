package com.sobremesa.waywt.util;

import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.enums.SortByType;

import android.content.SharedPreferences;


public class UserUtil {
	
	public static final String HAS_CHOSEN_SUBREDDIT = "has_chosen_subreddit";
	public static final String IS_MALE = "is_male";
	public static final String SORT_BY = "sort_by";

	public static boolean getHasChosenSubreddit() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getBoolean(HAS_CHOSEN_SUBREDDIT, false);
	}
	
	public static void setHasChosenSubreddit( boolean hasChosenSubreddit ) {
		SharedPreferences sharedPrefs = WaywtApplication.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(HAS_CHOSEN_SUBREDDIT, hasChosenSubreddit);
		editor.commit(); 
	}
	
	public static boolean getIsMale() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getBoolean(IS_MALE, true);
	}
	
	public static void setIsMale( boolean isMale ) {
		SharedPreferences sharedPrefs = WaywtApplication.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(IS_MALE, isMale);
		editor.commit(); 
	}
	
	public static String getSubreddit() {
		if(  getIsMale() )
			return "malefashionadvice";
		else
			return "femalefashionadvice";
	}
	
	public static String getSubredditAcronym() {
		if(  getIsMale() )
			return "MFA";
		else
			return "FFA"; 
	}
	
	public static int getSortBy() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getInt(SORT_BY, 0);
	}
	
	public static void setSortBy(SortByType type) {
		SharedPreferences sharedPrefs = WaywtApplication.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(SORT_BY, type.ordinal());
		editor.commit(); 
	}

}
