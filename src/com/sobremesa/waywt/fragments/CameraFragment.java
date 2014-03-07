package com.sobremesa.waywt.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.listeners.CameraFragmentListener;
import com.sobremesa.waywt.listeners.CameraOrientationListener;
import com.sobremesa.waywt.util.AnalyticsUtil;
import com.sobremesa.waywt.views.CameraPreview;

/**
 * Fragment for displaying the mCamera preview.
 *
 */
public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {
    public static final String TAG = CameraFragment.class.getSimpleName();

    public static final class Extras
    {
    	public static final String CAMERA_FACING = "camera_facing";
    }
    
    
    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 640;

    private int mCameraId;
    private Camera mCamera;
    private SurfaceHolder surfaceHolder;
    private CameraFragmentListener listener;
    private int displayOrientation;
    private int layoutOrientation;

    private CameraOrientationListener orientationListener;

    private CameraPreview mCameraPreview;
    /**
     * On activity getting attached.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof CameraFragmentListener)) {
            throw new IllegalArgumentException(
                "Activity has to implement CameraFragmentListener interface"
            );
        }

        listener = (CameraFragmentListener) activity;

        orientationListener = new CameraOrientationListener(activity);
    }

    /**
     * On creating view for fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mCameraPreview = new CameraPreview(getActivity());

    	mCameraPreview.getHolder().addCallback(this);

        return mCameraPreview;
    }

    @Override
    public void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	AnalyticsUtil.sendView(getActivity(), TAG);
    }
    
    /**
     * On fragment getting resumed.
     */
    @Override
    public void onResume() {
        super.onResume();

        orientationListener.enable();

        try {
            
        	if( mCamera != null )
        	{
                mCamera.release();
        	}
        	
        	if( !getArguments().getBoolean(Extras.CAMERA_FACING))
        	{
        		mCameraId = 0;
        		mCamera = Camera.open( mCameraId );
        	}
        	else
        	{
        		Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
        		int cameraCount = Camera.getNumberOfCameras();
        		for ( int id = 0; id < cameraCount; id++ ) {
        			Camera.getCameraInfo( id, mCameraInfo );
        			if ( mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
        				try {
        					mCameraId = id;
        					mCamera = Camera.open( mCameraId );
        				} catch (RuntimeException e) {
        					Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
        				}
        				return;
        			}
        		}
        		
        	}
            
        } catch (Exception exception) {
            Log.e(TAG, "Can't open mCamera with id " + mCameraId, exception);

            listener.onCameraError();
            return;
        }
    }

    /**
     * On fragment getting paused.
     */
    @Override
    public void onPause() {
        super.onPause();

        orientationListener.disable();

        stopCameraPreview();
        mCamera.release();
    }

    /**
     * Start the mCamera preview.
     */
    private synchronized void startCameraPreview() {
    	
    	if( mCamera != null )
    	{
            determineDisplayOrientation();
            setupCamera();

            try {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            } catch (Exception exception) {
                Log.e(TAG, "Can't start mCamera preview due to Exception", exception);

                listener.onCameraError();
            }    		
    	}

    }

    /**
     * Stop the mCamera preview.
     */
    private synchronized void stopCameraPreview() {
        try {
            mCamera.stopPreview();
        } catch (Exception exception) {
            Log.i(TAG, "Exception during stopping mCamera preview");
        }
    }
    
    public CameraPreview getCameraPreview()
    {
    	return mCameraPreview;
    }

    /**
     * Determine the current display orientation and rotate the mCamera preview
     * accordingly.
     */
    public void determineDisplayOrientation() {
        CameraInfo mCameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraId, mCameraInfo);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees  = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90:
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int displayOrientation;

        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (mCameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (mCameraInfo.orientation - degrees + 360) % 360;
        }

        this.displayOrientation = displayOrientation;
        this.layoutOrientation  = degrees;

        mCamera.setDisplayOrientation(displayOrientation);
    }

    /**
     * Setup the mCamera parameters.
     */
    public void setupCamera() {
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
        
        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPreviewSizes();

        return determineBestSize(sizes, PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        List<Size> sizes = parameters.getSupportedPictureSizes();

        return determineBestSize(sizes, PICTURE_SIZE_MAX_WIDTH);
    }

    protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;

        for (Size currentSize : sizes) {
            boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isInBounds = currentSize.width <= PICTURE_SIZE_MAX_WIDTH;

            if (isDesiredRatio && isInBounds && isBetterSize) {
                bestSize = currentSize;
            }
        }

        if (bestSize == null) {
            listener.onCameraError();

            return sizes.get(0);
        }

        return bestSize;
    }

    /**
     * Take a picture and notify the listener once the picture is taken.
     */
    public void takePicture() {
        orientationListener.rememberOrientation();

        mCamera.takePicture(null, null, this);
    }

    /**
     * A picture has been taken.
     */
    @Override
    public void onPictureTaken(byte[] data, Camera mCamera) {

		
		
		Camera.Parameters parameters = mCamera.getParameters();
		
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        int rotation = (
            displayOrientation
            + orientationListener.getRememberedOrientation()
            + layoutOrientation
        ) % 360;

        CameraInfo mCameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraId, mCameraInfo);
        
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Bitmap oldBitmap = bitmap;

            float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrix = new Matrix();
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);
            matrix.postConcat(matrixMirrorY);
            matrix.postRotate(90);
            
            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                false
            );

            oldBitmap.recycle();
        }
        else if (rotation != 0) {
            Bitmap oldBitmap = bitmap;

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);

            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                false
            );

            oldBitmap.recycle();
        }

		Bitmap newImage = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), 
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(newImage);

		canvas.drawBitmap(bitmap, 0f, 0f, null);

		
        listener.onPictureTaken(newImage);
    }

    /**
     * On mCamera preview surface created.
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;

        startCameraPreview();
    }

    /**
     * On mCamera preview surface changed.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // The interface forces us to have this method but we don't need it
        // up to now.
    }

    /**
     * On mCamera preview surface getting destroyed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // We don't need to handle this case as the fragment takes care of
        // releasing the mCamera when needed.
    }
}
