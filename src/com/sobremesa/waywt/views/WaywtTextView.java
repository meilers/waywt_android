package com.sobremesa.waywt.views;

import com.sobremesa.waywt.managers.FontManager;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class WaywtTextView extends TextView {

	public WaywtTextView(Context context) {
		super(context);
	}

	public WaywtTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WaywtTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void setTypeface(Typeface tf, int style) {
		tf = FontManager.INSTANCE.getAppFont();
		super.setTypeface(tf);
	}
}
