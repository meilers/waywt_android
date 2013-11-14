package com.sobremesa.waywt.managers;

import com.sobremesa.waywt.application.WaywtApplication;

import android.content.res.AssetManager;
import android.graphics.Typeface;


public enum FontManager {

	INSTANCE;

	private Typeface mAppFont;
	private Typeface mGeorgiaTypeFace;
	private Typeface mGeorgiaItalicFont;

	private FontManager() {
		AssetManager assetManager = WaywtApplication.getContext().getResources().getAssets();
		mAppFont = Typeface.createFromAsset(assetManager, "fonts/DINCondensedC.otf");
		mGeorgiaTypeFace = Typeface.createFromAsset(assetManager, "fonts/Georgia.ttf");
		mGeorgiaItalicFont = Typeface.createFromAsset(assetManager, "fonts/GeorgiaItalic.ttf");
	}

	public Typeface getAppFont() {
		return mAppFont;
	}

	public Typeface getGeorgiaFont() {
		return mGeorgiaTypeFace;
	}

	public Typeface getGeorgiaItalicFont() {
		return mGeorgiaItalicFont;
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
