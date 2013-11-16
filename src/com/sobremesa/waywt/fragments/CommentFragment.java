package com.sobremesa.waywt.fragments;

import java.util.ArrayList;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.activities.ImageActivity;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.views.AspectRatioImageView;
import com.sobremesa.waywt.views.WaywtSecondaryTextView;
import com.sobremesa.waywt.views.WaywtTextView;
import com.xtremelabs.imageutils.ImageLoader;
import com.xtremelabs.imageutils.ImageLoaderListener;
import com.xtremelabs.imageutils.ImageReturnedFrom;
import com.xtremelabs.imageutils.ImageLoader.Options;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CommentFragment extends Fragment implements LoaderCallbacks<Cursor>
{
	public static final String TAG = CommentFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_COMMENT_ID = "comment_id";
	}
	
	public static final int LOADER_COMMENT = 0;
	public static final int LOADER_IMAGES = 1;
	
	private ImageLoader mImageLoader;
	private AspectRatioImageView mMainIv;
	
	private WaywtSecondaryTextView mTitleTv;
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
		mTitleTv = (WaywtSecondaryTextView)view.findViewById(R.id.comment_title_tv);
		
		RepliesFragment fragment = new RepliesFragment();
		Bundle args = new Bundle();
		args.putString(RepliesFragment.Extras.ARG_COMMENT_ID, getArguments().getString(Extras.ARG_COMMENT_ID));
		fragment.setArguments(args);
		
		getChildFragmentManager().beginTransaction().replace(R.id.comment_replies_container, fragment, RepliesFragment.TAG).commit();
		
		getLoaderManager().initLoader(LOADER_COMMENT, null, this);
		getLoaderManager().initLoader(LOADER_IMAGES, null, this);
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		

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
		case LOADER_COMMENT:
			return new CursorLoader(getActivity(), Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.ID + "=?", new String[] { getArguments().getString(Extras.ARG_COMMENT_ID) }, null);
		case LOADER_IMAGES:
			return new CursorLoader(getActivity(), Provider.IMAGE_CONTENT_URI, ImageTable.ALL_COLUMNS, ImageTable.COMMENT_ID + "=?", new String[] { getArguments().getString(Extras.ARG_COMMENT_ID) }, null);
		}
		
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
		cursor.moveToFirst();
	
		switch( loader.getId() )
		{
		case LOADER_COMMENT:
			
			if( cursor.getCount() > 0)
			{
				int ups = Integer.valueOf(cursor.getString(cursor.getColumnIndex(CommentTable.UPS))); 
				int downs = Integer.valueOf(cursor.getString(cursor.getColumnIndex(CommentTable.DOWNS)));
				
				if( getView() != null )
				{
					ImageView arrowUpIv = (ImageView)getView().findViewById(R.id.comment_arrow_up_iv);
					ImageView arrowDownIv = (ImageView)getView().findViewById(R.id.comment_arrow_down_iv);		
					
					arrowUpIv.setVisibility(View.VISIBLE);
					arrowDownIv.setVisibility(View.VISIBLE);
					
					mPointsTv.setText((ups-downs)  + "");
					
					
					String bodyHtml =  cursor.getString(cursor.getColumnIndex(CommentTable.BODY_HTML));
					mTitleTv.setText(Html.fromHtml(bodyHtml));
					mTitleTv.setTypeface(FontManager.INSTANCE.getGeorgiaFont(), Typeface.ITALIC);
					
					mTitleTv.setMovementMethod (LinkMovementMethod.getInstance());
					mTitleTv.setClickable(true);
				}

			}
			break;
		case LOADER_IMAGES:
			if( getView() != null && cursor.getCount() > 0)
			{
				Log.d("image", cursor.getString(cursor.getColumnIndex(ImageTable.URL)));
				
				final String str = cursor.getString(cursor.getColumnIndex(ImageTable.URL));
				mImageLoader.loadImage(mMainIv, cursor.getString(cursor.getColumnIndex(ImageTable.URL)), new ImageLoaderListener() {
					@Override
					public void onImageLoadError(String arg0) { 
		
						Log.d("fail", str);
						
						ScrollView sv = (ScrollView)getView().findViewById(R.id.container);
						sv.setVisibility(View.VISIBLE);
						
						Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
						sv.startAnimation(myFadeInAnimation);
					}
					
					@Override
					public void onImageAvailable(ImageView imageView, Bitmap bitmap, ImageReturnedFrom imageReturnedFrom) {
						
						// bitmap = getResizedBitmap(bitmap, 200);
						
						imageView.setImageBitmap(bitmap);
						
						ScrollView sv = (ScrollView)getView().findViewById(R.id.container);
						sv.setVisibility(View.VISIBLE);
						
						Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
						sv.startAnimation(myFadeInAnimation);
						
						imageView.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								startImagesActivity(0);
							}
						});
					}
				});
			
				LinearLayout imagesLayout = (LinearLayout)getView().findViewById(R.id.images_grid_layout);
				imagesLayout.removeAllViews();
				
				Display display = getActivity().getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				int height = size.y;
				
				int i = 1;
				
				LinearLayout innerLayout = new LinearLayout(getActivity());
				
				for (cursor.moveToNext(); !cursor.isAfterLast(); cursor.moveToNext()) {
					String imageUrl = cursor.getString(cursor.getColumnIndex(ImageTable.URL));
					
					
					if( (i & 1) == 1 )
					{
						
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width/2);
						params.gravity = Gravity.LEFT;
						
						innerLayout = new LinearLayout(getActivity());
						innerLayout.setLayoutParams(params);
						innerLayout.setOrientation(LinearLayout.HORIZONTAL);
					}
					
					ImageView iv = new ImageView(getActivity());
					iv.setLayoutParams(new LinearLayout.LayoutParams(width/2, width/2));
					iv.setScaleType(ScaleType.CENTER_CROP);
					
					mImageLoader.loadImage(iv, imageUrl, new ImageLoaderListener() {
						@Override
						public void onImageLoadError(String arg0) {
			
							Log.d("fail", str);
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
					
					final int position = i;
					
					iv.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							startImagesActivity(position);
						}
					});
					
					innerLayout.addView(iv);

					
					if( (i & 1) == 0  || i == cursor.getCount()-1 )
					{
						imagesLayout.addView(innerLayout);
					}
					
					++i;
					

				}
			}
			break;
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void startImagesActivity(int position)
	{
		Intent intent = new Intent(getActivity(), ImageActivity.class);
		Bundle extras = new Bundle();
		extras.putString(ImageActivity.Extras.ARG_COMMENT_ID, getArguments().getString(Extras.ARG_COMMENT_ID));
		extras.putInt(ImageActivity.Extras.ARG_IMAGE_SELECTED_POSITION, position);
		intent.putExtras(extras);
		startActivity(intent);
	}
	
	
	
	
}
