package com.sobremesa.waywt.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;


public class UiUtil {

	
	public static int convertDpToPixel(int dp, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		int px = (int)(dp* (metrics.densityDpi / 160f));
		return px;
	}
}
