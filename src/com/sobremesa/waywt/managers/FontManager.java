package com.sobremesa.waywt.managers;

import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.util.UserUtil;

import android.content.res.AssetManager;
import android.graphics.Typeface;


public enum FontManager {

	INSTANCE;

	private Typeface mAppFont;
	
	private FontManager() {
		AssetManager assetManager = WaywtApplication.getContext().getResources().getAssets();
		mAppFont = Typeface.createFromAsset(assetManager, "fonts/DINCondensedC.otf");
	}

	public Typeface getAppFont() {
		return mAppFont; 
	}

	
	public String wrapTextWithHTMLColorTag(String text, int colorResId) {
		int colorInt = WaywtApplication.getContext().getResources().getColor(colorResId);
		String hexString = Integer.toHexString(colorInt);
		
		if (hexString.length() > 6) {
			hexString = hexString.substring(hexString.length() - 6);
		}
		
		return "<font color=\"#" + hexString + "\">" + text + "</font>";
	}
}
