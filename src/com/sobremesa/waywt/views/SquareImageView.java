package com.sobremesa.waywt.views;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

public class SquareImageView extends ImageView {

	int mWidth;
	
    public SquareImageView(Context context) {
        this(context, null, 0);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
		Display display = ((FragmentActivity) context).getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mWidth = size.x;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
    	setMeasuredDimension(mWidth, mWidth);
    }
}
