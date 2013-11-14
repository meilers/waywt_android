package com.sobremesa.waywt.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.adapters.CommentPagerAdapter;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;
import com.sobremesa.waywt.database.tables.RedditPostTable;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostService;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View; 
import android.view.ViewGroup;

public class WaywtFragment extends Fragment implements LoaderCallbacks<Cursor> {

	public static final String TAG = WaywtFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_POST_ID = "post_id";
		public static String ARG_PERMALINK = "permalink";
	}
	
	private CursorLoader mLoader;
	
	private ViewPager mPager;
	private CommentPagerAdapter mPagerAdapter;
	TitlePageIndicator mindicator;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_waywt, null, false);
		
		mPager = (ViewPager)view.findViewById(R.id.pager);
		mPagerAdapter = new CommentPagerAdapter(getChildFragmentManager(), new ArrayList<String>(),new ArrayList<String>());
		mPager.setAdapter(mPagerAdapter);
		
		mindicator = (TitlePageIndicator)view.findViewById(R.id.page_indicator);
		mindicator.setViewPager(mPager);
		mindicator.setTypeface(FontManager.INSTANCE.getAppFont());
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		 
		getLoaderManager().initLoader(0, null, this);
		fetchRedditPostCommentData();
	}
	
	@Override
	public void onDestroy() {
		mPagerAdapter = null;
		
		super.onDestroy();
	}
	
	private void fetchRedditPostCommentData() {  
 
		Intent i = new Intent(getActivity(), RedditPostCommentService.class);
		i.setAction(Intent.ACTION_SYNC);
		i.putExtra(RedditPostCommentService.Extras.ARG_POST_ID, getArguments().getString(Extras.ARG_POST_ID));
		i.putExtra(RedditPostCommentService.Extras.ARG_PERMALINK, getArguments().getString(Extras.ARG_PERMALINK));
		getActivity().startService(i);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		mLoader = new CursorLoader(getActivity(), Provider.REDDITPOSTCOMMENT_CONTENT_URI, new String[] {RedditPostCommentTable.ID, RedditPostCommentTable.REDDITPOST_ID, RedditPostCommentTable.AUTHOR, RedditPostCommentTable.UPS + " - " + RedditPostCommentTable.DOWNS + " AS `difference`" }, RedditPostCommentTable.REDDITPOST_ID + "=?", new String[] { getArguments().getString(Extras.ARG_POST_ID) }, "`difference` DESC");

		return mLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) { 
		// TODO Auto-generated method stub
		List<String> commentIds = new ArrayList<String>();
		List<String> usernames = new ArrayList<String>();
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			commentIds.add(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.ID)));
			usernames.add(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.AUTHOR)));
		}
		
		mPagerAdapter.setInfo(commentIds, usernames);
		mindicator.notifyDataSetChanged();

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
