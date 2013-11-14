package com.sobremesa.waywt.activities;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.RedditPostTable;
import com.sobremesa.waywt.fragments.WaywtFragment;
import com.sobremesa.waywt.service.RedditPostService;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener, LoaderCallbacks<Cursor> {

	public static class PostPermalink
	{
		public String mId;
		public String mPermalink;
	}
	private CursorLoader mLoader; 
	
	ArrayAdapter<String> mNavAdapter;
	
	private ArrayList<PostPermalink> mPermalinks;

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPermalinks = new ArrayList<PostPermalink>();

		
		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mNavAdapter = new ArrayAdapter<String>(actionBar.getThemedContext(), R.layout.list_item_navigation);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(mNavAdapter, this);
		
		

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		fetchRedditPostData();
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		Fragment fragment = new WaywtFragment();
		Bundle args = new Bundle();
		PostPermalink p = mPermalinks.get(position);
		args.putString(WaywtFragment.Extras.ARG_POST_ID,p.mId);
		args.putString(WaywtFragment.Extras.ARG_PERMALINK,p.mPermalink);
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
		return true;
	}


	private void fetchRedditPostData() {

		Intent i = new Intent(this, RedditPostService.class);
		i.setAction(Intent.ACTION_SYNC);
		startService(i);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		mLoader = new CursorLoader(this, Provider.REDDITPOST_CONTENT_URI, RedditPostTable.ALL_COLUMNS, null, null, RedditPostTable.CREATED + " DESC");

		return mLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		
		mNavAdapter.clear();
		
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) { 
//			SimpleDateFormat formatter=new SimpleDateFormat("DD-MMM-yyyy");  
//			
//			Date date = new Date(cursor.getLong(cursor.getColumnIndex(RedditPostTable.CREATED))*1000);
//			Calendar c = Calendar.getInstance();
//			c.setTime(date);
//			
//			String str = formatter.format(date);
//			
//			mNavAdapter.add(new SimpleDateFormat("MMM").format(c.getTime()) + " " + c.get(Calendar.DATE) + " " +  c.get(Calendar.YEAR));
			
			mNavAdapter.add( cursor.getString(cursor.getColumnIndex(RedditPostTable.TITLE)));
			
			PostPermalink p = new PostPermalink();
			String id = cursor.getString(cursor.getColumnIndex(RedditPostTable.ID));
			p.mId = cursor.getString(cursor.getColumnIndex(RedditPostTable.ID));
			p.mPermalink = cursor.getString(cursor.getColumnIndex(RedditPostTable.PERMALINK));
			mPermalinks.add(p); 
		}
		
		mNavAdapter.notifyDataSetChanged();

		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}





}
