package com.sobremesa.waywt.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectRatioImageView extends ImageView {

    public AspectRatioImageView(Context context) {
        this(context, null, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	if( getDrawable() != null && getDrawable().getIntrinsicWidth() != 0 ) 
    	{
	        int width = MeasureSpec.getSize(widthMeasureSpec);
	        int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
	        setMeasuredDimension(width+1, height+1);  // +1 because of weird bug that would leave white column of pixels at borders
    	}
    	else
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
