package com.sobremesa.waywt.views;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.YuvImage;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView  {

	
	
	
    public CameraPreview(Context context) {
        super(context);
        
        
        init(context);
    }
    private static final double ASPECT_RATIO = 3.0 / 4.0;

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        init(context);
    }
    

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (width > height * ASPECT_RATIO) {
             width = (int) (height * ASPECT_RATIO + .5);
        } else {
            height = (int) (width / ASPECT_RATIO + .5);
        }

        setMeasuredDimension(width, height);
    }
    
	
   

 

	private void init(Context context) {
        setWillNotDraw(false);
	}
	
}
