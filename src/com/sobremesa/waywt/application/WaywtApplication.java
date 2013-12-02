package com.sobremesa.waywt.application;

import com.xtremelabs.imageutils.ImageLoader;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class WaywtApplication extends Application {

	private static final int MAX_IMAGES_MEM_CACHE_SIZE = 2 * 1024 * 1024;
	private static Context sContext;
	private static WaywtApplication sApplication;
	
	public WaywtApplication()
	{
		sApplication = this;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		ImageLoader.setMaximumMemCacheSize(this, MAX_IMAGES_MEM_CACHE_SIZE);

		sContext = getApplicationContext();
	}
	
	
	public static final Context getContext() { 
		return sContext;
	}
	
	public static WaywtApplication getApplication(){
		return sApplication;
	}
	
	public static SharedPreferences getSharedPreferences() {
		SharedPreferences prefs = sContext.getSharedPreferences(sContext.getPackageName(), Context.MODE_PRIVATE);
		return prefs;
	}
}
