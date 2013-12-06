package com.sobremesa.waywt.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.fragments.CameraFragment;
import com.sobremesa.waywt.listeners.CameraFragmentListener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Activity displaying the camera and mustache preview.
 * 
 */
public class CameraActivity extends FragmentActivity implements CameraFragmentListener {

	private static final String TAG = CameraActivity.class.getSimpleName();

	private LayoutInflater controlInflater = null;
	private View viewControl;

	private CameraFragment mCameraFragment;
	private boolean mCameraFacing = true;

	private static final int PICTURE_QUALITY = 90;

	/**
	 * On activity getting created.
	 * 
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		setTitle("Take Photo");

		// controlInflater = LayoutInflater.from(getBaseContext());
		// // Add the capture button and the center image
		// viewControl = controlInflater.inflate(R.layout.control, null);
		// LayoutParams layoutParamsControl = new LayoutParams(
		// LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		// this.addContentView(viewControl, layoutParamsControl);
		// // Add the info and close buttons
		// viewControl = controlInflater.inflate(R.layout.info_close, null);
		// this.addContentView(viewControl, layoutParamsControl);

		showCameraFragment(mCameraFacing);

		if (checkCameraHardware(getApplicationContext())) {

		} else {
			Toast.makeText(getApplicationContext(), "No camera found on this device", Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	private void showCameraFragment(boolean facing) {
		mCameraFragment = new CameraFragment();
		Bundle args = new Bundle();
		args.putBoolean(CameraFragment.Extras.CAMERA_FACING, facing);
		mCameraFragment.setArguments(args);

		getSupportFragmentManager().beginTransaction().replace(R.id.camera_fragment, mCameraFragment).commit();
	}

	/**
	 * On fragment notifying about a non-recoverable problem with the camera.
	 */
	@Override
	public void onCameraError() {
		Toast.makeText(this, "camera error", Toast.LENGTH_SHORT).show();

		finish();
	}

	ImageButton mCameraBtn;
	
	/**
	 * The user wants to take a picture.
	 * 
	 * @param view
	 */
	public void takePicture(View view) {
		mCameraBtn = (ImageButton)view;
		mCameraBtn.setEnabled(false);

		if (mCameraFragment != null)
			mCameraFragment.takePicture();
	}

	/**
	 * A picture has been taken.
	 */
	public void onPictureTaken(Bitmap bitmap) {
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				showSavingPictureErrorToast();
				return;
			}
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "MUSTACHE_" + timeStamp + ".jpg");

		try {
			FileOutputStream stream = new FileOutputStream(mediaFile);
			bitmap.compress(CompressFormat.JPEG, PICTURE_QUALITY, stream);
		} catch (IOException exception) {
			showSavingPictureErrorToast();

			Log.w(TAG, "IOException during saving bitmap", exception);
			return;
		}

		MediaScannerConnection.scanFile(this, new String[] { mediaFile.toString() }, new String[] { "image/jpeg" }, null);

		Intent intent = new Intent(this, PhotoActivity.class);
		intent.setData(Uri.fromFile(mediaFile));
		startActivity(intent);

//		finish();
	}

	private void showSavingPictureErrorToast() {
		// Toast.makeText(this, getText(R.string.toast_error_save_picture),
		// Toast.LENGTH_SHORT).show();
	}

	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			// This device has a camera
			return true;
		} else {
			// No camera on this device
			return false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if( mCameraBtn != null )
			mCameraBtn.setEnabled(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	Handler mHideHandler = new Handler();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.switch_camera_menu_id:

			mCameraFacing = !mCameraFacing;
			showCameraFragment(mCameraFacing);

			// takePicture(mCameraFragment.getCameraPreview());
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
