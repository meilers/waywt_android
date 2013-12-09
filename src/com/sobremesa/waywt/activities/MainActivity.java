package com.sobremesa.waywt.activities;

import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.adapters.DrawerListAdapter;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.common.Common;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.common.RedditIsFunHttpClientFactory;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.PostTable;
import com.sobremesa.waywt.dialog.LoginDialog;
import com.sobremesa.waywt.enums.SortByType;
import com.sobremesa.waywt.fragments.CommentFragment;
import com.sobremesa.waywt.fragments.WaywtFragment;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.service.PostService;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.tasks.LoginTask;
import com.sobremesa.waywt.util.UserUtil;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends BaseFragmentActivity implements ActionBar.OnNavigationListener, LoaderCallbacks<Cursor> {

	public static class PostPermalink {
		public String mId;
		public String mPermalink;
	}

	public static class DrawerTabIndex {
		public static final int WAYWT = 0;
//		public static final int TOP_POSTERS = 1;
//		public static final int MY_POSTS = 2;
//		public static final int PROFILE = 3;
		public static final int SETTINGS = 1;
	}

	// DRAWER
	private ListView mDrawerList;
	private DrawerListAdapter mDrawerListAdapter;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private CharSequence mDrawerTitle;
	
	// META
	private int mSelectedTabFromDrawer = -1;
	private CharSequence mTitle = "";
	private int mCurrentTab = -1;
	
	private CursorLoader mLoader;

	private ArrayAdapter<String> mNavAdapter;
	private int mCurrentWaywtIndex = 0;
	

	private ArrayList<PostPermalink> mPermalinks;

	private static final HttpClient mRedditClient = WaywtApplication.getRedditClient();
	private static final RedditSettings mRedditSettings = WaywtApplication.getRedditSettings();

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getOverflowMenu();
		
		mPermalinks = new ArrayList<PostPermalink>();

		// ACTION BAR
		final ActionBar actionBar = getActionBar();
		
		if( UserUtil.getIsMale() )
		{
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar));
			actionBar.setIcon(getResources().getDrawable(R.drawable.ic_logo));
		}
		else
		{
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_ffa));
			actionBar.setIcon(getResources().getDrawable(R.drawable.ic_logo_ffa));
		}
		
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		mTitle = mDrawerTitle = getString(R.string.app_name);
		setTitle(mTitle);
		
		mNavAdapter = new ArrayAdapter<String>(actionBar.getThemedContext(), R.layout.list_item_navigation);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(mNavAdapter, this);
		
		CookieSyncManager.createInstance(getApplicationContext());
		
		
		
		// DRAWER
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
				int selectedItem = position;
				position = position - 1;
				if (mDrawerListAdapter != null) {
					if (selectedItem > 0) {
						mDrawerListAdapter.setSelectedItem(position);
					}
				}

				onDrawerItemSelected(selectedItem);
			}
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

			public void onDrawerClosed(View view) {
				setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()

				updateMainView();

			}

			public void onDrawerOpened(View drawerView) {
				setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()

			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerListAdapter = new DrawerListAdapter(this.getApplicationContext());

		mDrawerList.setScrollContainer(false);
		mDrawerList.setAdapter(mDrawerListAdapter);  
		
		
		

		if( UserUtil.getHasChosenSubreddit() )
		{
			getSupportLoaderManager().initLoader(0, null, this);
			fetchPostData();
		}
		else
			showSubredditDialog();
	}

	public void onDrawerItemSelected(int position) {
		setTitle(getString(R.string.app_name));

		mSelectedTabFromDrawer = position - 1;

		closeDrawer();

	}
	
	private void closeDrawer() {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		}
	}
	
	private void getOverflowMenu() {

	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void updateMainView() {
		switch (mSelectedTabFromDrawer) {
		case DrawerTabIndex.WAYWT:
			showWaywt();
			break;
		}
	}
	
	public void showWaywt() {

		if (mCurrentTab != DrawerTabIndex.WAYWT) {
			setSelectedDrawerAdapterPosition(DrawerTabIndex.WAYWT);
//			mTabBackStack.add(mCurrentTab);
			mCurrentTab = DrawerTabIndex.WAYWT;
			refreshNavigationBar(mCurrentWaywtIndex);
		}

	}
	
	private void setSelectedDrawerAdapterPosition(int position) {
		mDrawerListAdapter.setSelectedItem(position);
		mSelectedTabFromDrawer = position;
	}
	
	private void goToFragment(Fragment fragment, String tag, String title, boolean animate) {
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = manager.beginTransaction();

		if (animate)
			fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);

		fragmentTransaction.replace(R.id.content_frame, new Fragment());
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

		FragmentTransaction ft = manager.beginTransaction();
		ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
		ft.replace(R.id.content_frame, fragment, tag);
		ft.addToBackStack(null);
		ft.commit();
		closeDrawer();

		if (title != null) {
			mTitle = title;
			setTitle(title);
		}
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		mRedditSettings.loadRedditPreferences(this, mRedditClient);
		CookieSyncManager.getInstance().startSync();


	}

	@Override
	protected void onPause() {
		super.onPause();

		CookieSyncManager.getInstance().stopSync();
		mRedditSettings.saveRedditPreferences(this);
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
	public boolean onNavigationItemSelected(int position, long id) {
		mCurrentWaywtIndex = position;
		refreshNavigationBar(position);
		return true;
	}

	private void refreshNavigationBar(int position) {
		Fragment fragment = new WaywtFragment();
		Bundle args = new Bundle();
		PostPermalink p = mPermalinks.get(position);
		args.putString(WaywtFragment.Extras.SUBREDDIT, UserUtil.getSubreddit());
		args.putString(WaywtFragment.Extras.PERMALINK, p.mPermalink);
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	private void fetchPostData() {

		Intent i = new Intent(this, PostService.class);
		i.setAction(Intent.ACTION_SYNC);
		i.putExtra(PostService.Extras.IS_MALE, UserUtil.getIsMale());
		startService(i);  
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		mLoader = new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, PostTable.IS_MALE + "=?", new String[] { UserUtil.getIsMale() ? "1" : "0" }, PostTable.CREATED + " DESC");

		return mLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mNavAdapter.clear();
		mPermalinks.clear();

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

			mNavAdapter.add(cursor.getString(cursor.getColumnIndex(PostTable.TITLE)));

			PostPermalink p = new PostPermalink();
			p.mId = cursor.getString(cursor.getColumnIndex(PostTable.ID));
			p.mPermalink = cursor.getString(cursor.getColumnIndex(PostTable.PERMALINK));
			mPermalinks.add(p);
		}

		mNavAdapter.notifyDataSetChanged();

		if (cursor.getCount() > 0) {

			refreshNavigationBar(0);
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		StringBuilder sb;

		switch (id) {
		case Constants.DIALOG_LOGIN:
			if (mRedditSettings.getUsername() != null) {
				final TextView loginUsernameInput = (TextView) dialog.findViewById(R.id.login_username_input);
				loginUsernameInput.setText(mRedditSettings.getUsername());
			}
			final TextView loginPasswordInput = (TextView) dialog.findViewById(R.id.login_password_input);
			loginPasswordInput.setText("");
			break;

		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Dialog dialog;
		ProgressDialog pdialog;
		AlertDialog.Builder builder;
		LayoutInflater inflater;

		switch (id) {
		case Constants.DIALOG_LOGIN:
			dialog = new LoginDialog(this, mRedditSettings, false) {
				@Override
				public void onLoginChosen(String user, String password) {
					removeDialog(Constants.DIALOG_LOGIN);
					new MyLoginTask(user, password).execute();
				}
			};
			return dialog;

		}
		return null;
	}

	private class MyLoginTask extends LoginTask {
		public MyLoginTask(String username, String password) {
			super(username, password, mRedditSettings, mRedditClient, getApplicationContext());
		}

		@Override
		protected void onPreExecute() {
			showDialog(Constants.DIALOG_LOGGING_IN);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			removeDialog(Constants.DIALOG_LOGGING_IN);
			if (success) {
				Toast.makeText(MainActivity.this, "Logged in as " + mUsername, Toast.LENGTH_SHORT).show();
				mRedditSettings.saveRedditPreferences(MainActivity.this);
			} else {
				Common.showErrorToast(mUserError, Toast.LENGTH_LONG, MainActivity.this);
			}
		}
	}


	private void toggleDrawer() {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			mDrawerListAdapter.notifyDataSetChanged();
			mDrawerLayout.openDrawer(mDrawerList);

		}

	}


	private void showSortByDialog() {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		// builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setTitle("Sort By");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
		arrayAdapter.add("Random");
		arrayAdapter.add("Votes");
//		arrayAdapter.add("Comments");

		
		builderSingle.setSingleChoiceItems(arrayAdapter, UserUtil.getSortBy(), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case 0:
					UserUtil.setSortBy(SortByType.RANDOM);
					break;
					
				case 1:
					UserUtil.setSortBy(SortByType.VOTES);
					break;
					
//				case 2:
//					UserUtil.setSortBy(SortByType.COMMENTS);
//					break;
				}
				
				if( mPermalinks.size() > 0)
					refreshNavigationBar(mCurrentWaywtIndex);
				
				dialog.dismiss();
			}
		});
		
		builderSingle.show(); 
	}
	
	private void showSubredditDialog() {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		// builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setTitle("Choose Subreddit");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice); 
		arrayAdapter.add("Male Fashion Advice");
		arrayAdapter.add("Female Fashion Advice");

		int selected = 0;
		
		if( !UserUtil.getIsMale() )
			selected = 1;
		
		builderSingle.setSingleChoiceItems(arrayAdapter, selected, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case 0:
					UserUtil.setIsMale(true);
					MainActivity.this.getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar));
					MainActivity.this.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo));
					break;
					
				case 1:
					UserUtil.setIsMale(false);
					MainActivity.this.getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_ffa));
					MainActivity.this.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo_ffa));
					break;
				}
				
				getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new Fragment()).commitAllowingStateLoss();
				
				UserUtil.setHasChosenSubreddit(true);
				
				getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
				fetchPostData();		

				
				
				dialog.dismiss();
			}
		});
		
		builderSingle.show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		
		case android.R.id.home:
			toggleDrawer();
			return true;
			

		}

		return super.onOptionsItemSelected(item);
	}


}
