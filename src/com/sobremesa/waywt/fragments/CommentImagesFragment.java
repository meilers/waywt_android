package com.sobremesa.waywt.fragments;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;
import com.sobremesa.waywt.fragments.WaywtFragment.Extras;
import com.sobremesa.waywt.views.AspectRatioImageView;
import com.xtremelabs.imageutils.ImageLoader;
import com.xtremelabs.imageutils.ImageLoaderListener;
import com.xtremelabs.imageutils.ImageReturnedFrom;
import com.xtremelabs.imageutils.ImageLoader.Options;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

public class CommentImagesFragment extends Fragment implements LoaderCallbacks<Cursor>
{
	public static final String TAG = CommentImagesFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_COMMENT_ID = "comment_id";
	}
	
	private CursorLoader mLoader;
	private ImageLoader mImageLoader;
	
	
	AspectRatioImageView mMainIv;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_comment_images, null, false);
		
		mMainIv = (AspectRatioImageView)view.findViewById(R.id.comment_image_iv);
		
		Options options = new Options();
		options.scalingPreference = Options.ScalingPreference.ROUND_TO_CLOSEST_MATCH;
		mImageLoader = ImageLoader.buildImageLoaderForSupportFragment(this);
		mImageLoader.setDefaultOptions(options);
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		mLoader = new CursorLoader(getActivity(), Provider.IMAGE_CONTENT_URI, ImageTable.ALL_COLUMNS, ImageTable.REDDITPOSTCOMMENT_ID + "=?", new String[] { getArguments().getString(Extras.ARG_COMMENT_ID) }, null);

		return mLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, final Cursor cursor) {
		// TODO Auto-generated method stub
		cursor.moveToFirst();
		
		Log.d("image", cursor.getString(cursor.getColumnIndex(ImageTable.URL)));
		
		if( cursor.getCount() > 0)
		{
			mImageLoader.loadImage(mMainIv, cursor.getString(cursor.getColumnIndex(ImageTable.URL)), new ImageLoaderListener() {
				@Override
				public void onImageLoadError(String arg0) {
					if( getActivity() != null)
					Toast.makeText(getActivity(), "IMAGE ERROR", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onImageAvailable(ImageView imageView, Bitmap bitmap, ImageReturnedFrom imageReturnedFrom) {
					
					// bitmap = getResizedBitmap(bitmap, 200);
					
					imageView.setImageBitmap(bitmap);
					if (imageReturnedFrom != ImageReturnedFrom.MEMORY) {
						
						if (getActivity() != null) {
							
							Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
							imageView.startAnimation(myFadeInAnimation);
						}
					}
				}
			});
			
		}
		else
			Toast.makeText(getActivity(), "NO IMAGE", Toast.LENGTH_LONG).show();
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
