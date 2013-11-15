package com.sobremesa.waywt.views;

import com.sobremesa.waywt.application.WaywtApplication;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class OutlineWaywtTextView extends WaywtTextView {

	public OutlineWaywtTextView(Context context) {
		this(context, null, 0);
		// TODO Auto-generated constructor stub
	}
	
	public OutlineWaywtTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public OutlineWaywtTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void onDraw(Canvas canvas)
    {
		 final ColorStateList textColor = getTextColors();
		
		 TextPaint paint = this.getPaint();
		
		 paint.setStyle(Style.STROKE);
		 paint.setStrokeJoin(Join.ROUND);
		 paint.setStrokeMiter(10);
		 this.setTextColor(WaywtApplication.getContext().getResources().getColor(android.R.color.black));
		 paint.setStrokeWidth(12.0f);
		
		 super.onDraw(canvas);
		 paint.setStyle(Style.FILL);
		
		 setTextColor(textColor);
		 super.onDraw(canvas);
    }
}
