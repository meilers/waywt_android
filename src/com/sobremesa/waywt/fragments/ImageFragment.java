package com.sobremesa.waywt.fragments;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.model.ThingInfo;
import com.xtremelabs.imageutils.ImageLoader;
import com.xtremelabs.imageutils.ImageLoaderListener;
import com.xtremelabs.imageutils.ImageReturnedFrom;
import com.xtremelabs.imageutils.ImageLoader.Options;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;

public class ImageFragment extends Fragment {

	private static final String TAG = ImageFragment.class.getSimpleName();
	
	
	public static class Extras
	{
		public static String ARG_IMAGE_URL = "comment";
	}
	
	private ImageLoader mImageLoader;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub 
		super.onCreate(savedInstanceState);
		
		Options options = new Options();
		options.scalingPreference = Options.ScalingPreference.ROUND_TO_CLOSEST_MATCH;
		mImageLoader = ImageLoader.buildImageLoaderForSupportFragment(this);
		mImageLoader.setDefaultOptions(options);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_image, null);
		
		ImageView iv = (ImageView)view.findViewById(R.id.image_iv);
		
		
		mImageLoader.loadImage(iv, getArguments().getString(Extras.ARG_IMAGE_URL), new ImageLoaderListener() {
			@Override
			public void onImageLoadError(String arg0) { 
			}
			
			@Override
			public void onImageAvailable(ImageView imageView, Bitmap bitmap, ImageReturnedFrom imageReturnedFrom) {
				  
				
				
				if( getActivity() != null )
				{
					imageView.setImageBitmap(bitmap);
					
					Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
					imageView.startAnimation(myFadeInAnimation);
				}
			}
		});
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		mImageLoader.destroy();
	}
}
