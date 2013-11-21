package com.sobremesa.waywt.activities;

import java.util.ArrayList;
import java.util.List;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.adapters.CommentPagerAdapter;
import com.sobremesa.waywt.adapters.ImagePagerAdapter;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.fragments.WaywtFragment.Extras;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.managers.TypefaceSpan;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.ActionBar;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;

public class ImageActivity extends FragmentActivity {

	public static class Extras
	{
		public static String ARG_AUTHOR = "author";
		public static String ARG_IMAGE_URLS = "image_urls";
		public static String ARG_IMAGE_SELECTED_POSITION = "image_selected_position";
	}
	
	
	private ViewPager mPager;
	private ImagePagerAdapter mPagerAdapter;
	private CirclePageIndicator mindicator;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_image);
		
		mPager = (ViewPager)findViewById(R.id.pager);
		
		Bundle extras = getIntent().getExtras();
		
		SpannableString s = new SpannableString(extras.getString(Extras.ARG_AUTHOR).toUpperCase());
		s.setSpan(new TypefaceSpan(WaywtApplication.getContext()), 0, s.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		ActionBar actionbar = getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true); 
		actionbar.setTitle(s);
		
		
		List<String> imageUrls = extras.getStringArrayList(Extras.ARG_IMAGE_URLS);
		
		mPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imageUrls);
		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(extras.getInt(Extras.ARG_IMAGE_SELECTED_POSITION,0));
		
		mindicator = (CirclePageIndicator)findViewById(R.id.indicator);
		mindicator.setViewPager(mPager);
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

//	@Override
//	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
//		
//		switch( loaderId )
//		{
//		case LOADER_COMMENT:
//			return new CursorLoader(this, Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.ID + "=?", new String[] { getIntent().getStringExtra(Extras.ARG_COMMENT_ID) }, null);
//		case LOADER_IMAGES:
//			return new CursorLoader(this, Provider.IMAGE_CONTENT_URI, ImageTable.ALL_COLUMNS, ImageTable.COMMENT_ID + "=?", new String[] { getIntent().getStringExtra(Extras.ARG_COMMENT_ID) }, null);
//		}
//		
//		return null;
//	}
//
//	@Override
//	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//		
//		cursor.moveToFirst();
//		
//		switch( loader.getId() )
//		{
//		case LOADER_COMMENT:
//			
//			SpannableString s = new SpannableString(cursor.getString(cursor.getColumnIndex(CommentTable.AUTHOR)).toUpperCase());
//			s.setSpan(new TypefaceSpan(WaywtApplication.getContext()), 0, s.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//			ActionBar actionBar = getActionBar();
//			actionBar.setTitle(s);
//			
//			break;
//		case LOADER_IMAGES:
//			List<String> imageUrls = new ArrayList<String>();
//			
//			for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() )
//			{
//				imageUrls.add(cursor.getString(cursor.getColumnIndex(ImageTable.URL)));
//			}
//			
//			mPagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imageUrls);
//			mPager.setAdapter(mPagerAdapter);
//			mPager.setCurrentItem(getIntent().getIntExtra(Extras.ARG_IMAGE_SELECTED_POSITION,0));
//			
//			mindicator = (CirclePageIndicator)findViewById(R.id.indicator);
//			mindicator.setViewPager(mPager);
//			
//			break;
//			
//		}
//
//	}
//
//	@Override
//	public void onLoaderReset(Loader<Cursor> arg0) {
//		// TODO Auto-generated method stub
//		
//	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
