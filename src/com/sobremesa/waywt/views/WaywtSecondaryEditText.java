package com.sobremesa.waywt.views;

import com.sobremesa.waywt.managers.FontManager;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;


public class WaywtSecondaryEditText extends EditText {
	public WaywtSecondaryEditText(Context context) {
		super(context);
	}

	public WaywtSecondaryEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WaywtSecondaryEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setTypeface(Typeface tf, int style) {
		switch (style) {
		case Typeface.ITALIC:
		case Typeface.BOLD_ITALIC:
			tf = FontManager.INSTANCE.getGeorgiaItalicFont();
			break;
		default:
			tf = FontManager.INSTANCE.getGeorgiaFont();
			break;
		}

		super.setTypeface(tf);
	}
}
