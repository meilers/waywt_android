package com.sobremesa.waywt.fragments;

import java.util.ArrayList;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;
import com.sobremesa.waywt.fragments.CommentImagesFragment.Extras;
import com.sobremesa.waywt.views.AspectRatioImageView;
import com.xtremelabs.imageutils.ImageLoader;
import com.xtremelabs.imageutils.ImageLoaderListener;
import com.xtremelabs.imageutils.ImageReturnedFrom;
import com.xtremelabs.imageutils.ImageLoader.Options;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class CommentFragment extends Fragment implements LoaderCallbacks<Cursor>
{
	public static final String TAG = CommentFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_COMMENT_ID = "comment_id";
	}
	
	public static final int LOADER_COMMENTS = 0;
	public static final int LOADER_IMAGES = 1;

	private CursorLoader mLoader;
	
	private ImageLoader mImageLoader;
	private AspectRatioImageView mMainIv;
	
	private TextView mTitleTv;
	private TextView mPointsTv;
	
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
		View view = inflater.inflate(R.layout.fragment_comment, null, false);
		
		
		mMainIv = (AspectRatioImageView)view.findViewById(R.id.comment_image_iv);
		mPointsTv = (TextView)view.findViewById(R.id.comment_points_tv);
		mTitleTv = (TextView)view.findViewById(R.id.comment_title_tv);
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		getLoaderManager().initLoader(LOADER_COMMENTS, null, this);
		getLoaderManager().initLoader(LOADER_IMAGES, null, this);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mImageLoader.destroy();
		
		super.onDestroy();
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		switch( loaderId )
		{
		case LOADER_COMMENTS:
			return new CursorLoader(getActivity(), Provider.REDDITPOSTCOMMENT_CONTENT_URI, RedditPostCommentTable.ALL_COLUMNS, RedditPostCommentTable.ID + "=?", new String[] { getArguments().getString(Extras.ARG_COMMENT_ID) }, null);
		case LOADER_IMAGES:
			return new CursorLoader(getActivity(), Provider.IMAGE_CONTENT_URI, ImageTable.ALL_COLUMNS, ImageTable.REDDITPOSTCOMMENT_ID + "=?", new String[] { getArguments().getString(Extras.ARG_COMMENT_ID) }, null);
		}
		
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
		cursor.moveToFirst();
	
		switch( loader.getId() )
		{
		case LOADER_COMMENTS:
			
			if( cursor.getCount() > 0)
			{
				int ups = Integer.valueOf(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.UPS))); 
				int downs = Integer.valueOf(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.DOWNS)));
				
				if( getView() != null )
				{
					ImageView arrowUpIv = (ImageView)getView().findViewById(R.id.comment_arrow_up_iv);
					ImageView arrowDownIv = (ImageView)getView().findViewById(R.id.comment_arrow_down_iv);		
					
					arrowUpIv.setVisibility(View.VISIBLE);
					arrowDownIv.setVisibility(View.VISIBLE);
					
					mPointsTv.setText((ups-downs)  + "");
					mTitleTv.setText(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.AUTHOR)));				
				}

			}
			break;
		case LOADER_IMAGES:
			if( cursor.getCount() > 0)
			{
				Log.d("image", cursor.getString(cursor.getColumnIndex(ImageTable.URL)));
				
				final String str = cursor.getString(cursor.getColumnIndex(ImageTable.URL));
				mImageLoader.loadImage(mMainIv, cursor.getString(cursor.getColumnIndex(ImageTable.URL)), new ImageLoaderListener() {
					@Override
					public void onImageLoadError(String arg0) {
		
						Log.d("fail", str);
						
						ArrayList<String> imageUrls = new ArrayList<String>();
						
						for (cursor.moveToNext(); !cursor.isAfterLast(); cursor.moveToNext()) {
							imageUrls.add(cursor.getString(cursor.getColumnIndex(ImageTable.URL)));
						}
						
						ImageAdapter adapter = new ImageAdapter(getActivity(), R.layout.list_item_image, imageUrls);
						
						if( getView() != null )
						{
							GridView gv = (GridView)getView().findViewById(R.id.comment_images_gv);
							gv.setAdapter(adapter);
							
							ScrollView sv = (ScrollView)getView().findViewById(R.id.container);
							sv.fullScroll(ScrollView.FOCUS_UP);
							sv.smoothScrollTo(0,0);
						}
						
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
						
						ArrayList<String> imageUrls = new ArrayList<String>();
						
						for (cursor.moveToNext(); !cursor.isAfterLast(); cursor.moveToNext()) {
							imageUrls.add(cursor.getString(cursor.getColumnIndex(ImageTable.URL)));
						}
						
						ImageAdapter adapter = new ImageAdapter(getActivity(), R.layout.list_item_image, imageUrls);
						
						if( getView() != null )
						{
							GridView gv = (GridView)getView().findViewById(R.id.comment_images_gv);
							gv.setAdapter(adapter);
							
							ScrollView sv = (ScrollView)getView().findViewById(R.id.container);
							sv.fullScroll(ScrollView.FOCUS_UP);
							sv.smoothScrollTo(0,0);
						}
						
					}
				});
			}
			break;
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	private class ImageAdapter extends ArrayAdapter<String> {
		Context context;
		int layoutResourceId;
		ArrayList<String> data = new ArrayList<String>();

		public ImageAdapter(Context context, int layoutResourceId, ArrayList<String> data) {
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ImageHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new ImageHolder();
				holder.imageItem = (ImageView) row.findViewById(R.id.list_item_iv);
				row.setTag(holder);
			} else {
				holder = (ImageHolder) row.getTag();
			}

			String imageUrl = data.get(position);
			
			mImageLoader.loadImage(holder.imageItem,imageUrl, new ImageLoaderListener() {
				@Override
				public void onImageLoadError(String arg0) {
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
			
			
			if( CommentFragment.this.getView() != null )
			{
				ScrollView sv = (ScrollView)CommentFragment.this.getView().findViewById(R.id.container);
				sv.fullScroll(ScrollView.FOCUS_UP);
				sv.smoothScrollTo(0,0);
			}
			
			
			return row;

		}

		private class ImageHolder {
			ImageView imageItem;

		}
	}
}
