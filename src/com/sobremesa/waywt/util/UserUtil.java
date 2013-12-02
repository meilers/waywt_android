package com.sobremesa.waywt.util;

import com.sobremesa.waywt.application.WaywtApplication;

import android.content.SharedPreferences;


public class UserUtil {
	
	public static final String IS_MALE = "is_male";
	public static final String SORT_BY = "sort_by";

	
	public static boolean getIsMale() {
		SharedPreferences prefs = WaywtApplication.getSharedPreferences();
		return prefs.getBoolean(IS_MALE, false);
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

}
