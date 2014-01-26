package com.sobremesa.waywt.util;

import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.enums.SortByType;

import android.content.SharedPreferences;


public class UserUtil {
	
	public static final String HAS_CHOSEN_SUBREDDIT = "has_chosen_subreddit";
	public static final String IS_MALE = "is_male";
	public static final String IS_TEEN = "is_teen";
	public static final String SORT_BY = "sort_by";
	
	public static final String SEE_WAYWT_POSTS = "see_waywt_posts";
	public static final String SEE_OUTFIT_FEEDBACK_POSTS = "see_outfit_feedback_posts";
	public static final String SEE_RECENT_PURCHASES_POSTS = "see_recent_purchases_posts";
	

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
	
	public static boolean getIsTeen() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getBoolean(IS_TEEN, false);
	}
	
	public static void setIsTeen( boolean isTeen ) {
		SharedPreferences sharedPrefs = WaywtApplication.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(IS_TEEN, isTeen);
		editor.commit(); 
	}
	
	
	public static String getSubreddit() {
		if(  getIsMale() )
		{
			if( !getIsTeen() )
				return "malefashionadvice";
			else
				return "TeenMFA";
		}
		else
		{
			if( !getIsTeen() )
				return "femalefashionadvice";
			else
				return "TeenFFA";
		}
	}
	
	public static String getSubredditAcronym() {
		if(  getIsMale() )
		{
			if( !getIsTeen() )
				return "MFA";
			else
				return "TeenMFA";
		}
		else
		{
			if( !getIsTeen() )
				return "FFA";
			else
				return "TeenFFA";
		}
	}
	
	public static boolean getSeeWaywtPosts() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getBoolean(SEE_WAYWT_POSTS, true);
	}
	
	public static void setSeeWaywtPosts(boolean flag) {
		SharedPreferences sharedPrefs = WaywtApplication.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(SEE_WAYWT_POSTS, flag);
		editor.commit(); 
	}
	
	public static boolean getSeeOutfitFeedbackPosts() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getBoolean(SEE_OUTFIT_FEEDBACK_POSTS, false);
	}
	
	public static void setSeeOutfitFeedbackPosts(boolean flag) {
		SharedPreferences sharedPrefs = WaywtApplication.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(SEE_OUTFIT_FEEDBACK_POSTS, flag);
		editor.commit(); 
	}
	
	public static boolean getSeeRecentPurchasesPosts() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getBoolean(SEE_RECENT_PURCHASES_POSTS, false);
	}
	
	public static void setSeeRecentPurchasesPosts(boolean flag) {
		SharedPreferences sharedPrefs = WaywtApplication.getSharedPreferences();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean(SEE_RECENT_PURCHASES_POSTS, flag);
		editor.commit(); 
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
