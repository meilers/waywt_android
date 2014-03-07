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
import com.sobremesa.waywt.enums.PostType;
import com.sobremesa.waywt.enums.SortByType;
import com.sobremesa.waywt.fragments.CommentFragment;
import com.sobremesa.waywt.fragments.LoadingFragment;
import com.sobremesa.waywt.fragments.MyPostsFragment;
import com.sobremesa.waywt.fragments.SettingsFragment;
import com.sobremesa.waywt.fragments.WaywtFragment;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.service.PostService;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.tasks.LoginTask;
import com.sobremesa.waywt.util.AnalyticsUtil;
import com.sobremesa.waywt.util.UiUtil;
import com.sobremesa.waywt.util.UserUtil;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends BaseFragmentActivity implements ActionBar.OnNavigationListener, LoaderCallbacks<Cursor> {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	public static final class Extras {
		public static final String SELECTED_TAB_FROM_DRAWER = "selected_tab_from_drawer";

	}
	
	public static class NavItem {
		public String mTitle;
		public String mDescription;
		public String mPermalink;
	}

	public static class DrawerTabIndex {
		public static final int WAYWT = 0;
//		public static final int TOP_POSTERS = 1;
		public static final int MY_POSTS = 1;
//		public static final int PROFILE = 3;
		public static final int SETTINGS = 2;
	}

	
	public final int MSG_SHOW_DIALOG = 1;
	public final int MSG_HIDE_DIALOG = 2;
	

	private Handler handler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        if(msg.what == MSG_SHOW_DIALOG) {
	            showProgressDialog("LOADING POSTS");
	        }
	        else if(msg.what == MSG_HIDE_DIALOG) {
	            hideProgressDialog();
	        }
	    }
	};
	
	
	// DRAWER
	private ListView mDrawerList;
	private DrawerListAdapter mDrawerListAdapter;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private RelativeLayout mDrawerRelativeLayout;
	
	private CharSequence mDrawerTitle;
	
	// META
	private int mSelectedTabFromDrawer = -1;
	private CharSequence mTitle = "";
	private int mCurrentTab = -1;
	private boolean mResetActionBar = false;

	private CustomAdapter mNavAdapter;
	private int mCurrentWaywtIndex = 0;
	

	private ArrayList<NavItem> mNavItems;

	private static final HttpClient mRedditClient = WaywtApplication.getRedditClient();
	private static final RedditSettings mRedditSettings = WaywtApplication.getRedditSettings();

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getOverflowMenu();
		
		mNavItems = new ArrayList<NavItem>();

		// ACTION BAR
		final ActionBar actionBar = getActionBar();
		
		if( UserUtil.getIsMale() )
		{
			actionBar.setIcon(getResources().getDrawable(R.drawable.ic_logo));
		}
		else
		{
			actionBar.setIcon(getResources().getDrawable(R.drawable.ic_logo_ffa));
		}
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		mTitle = mDrawerTitle = getString(R.string.app_name);
		setTitle(mTitle);
		
		
		// For WAYWT navigation
		mNavAdapter = new CustomAdapter(this, R.layout.list_item_navigation,  new ArrayList<NavItem>());
		actionBar.setListNavigationCallbacks(mNavAdapter, this);
		
		
		
		
		
		// DRAWER
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerRelativeLayout = (RelativeLayout) findViewById(R.id.drawer_relative_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		
		mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
				int selectedItem = position;
				if (mDrawerListAdapter != null) {
					mDrawerListAdapter.setSelectedItem(position);
				}

				onDrawerItemSelected(selectedItem);
			}
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

			public void onDrawerClosed(View view) {
				setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()

				updateMainView(false);

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
		
		
		// FOOTER
		LinearLayout rateTheAppLl = (LinearLayout)findViewById(R.id.drawer_rate_the_app_ll);
		rateTheAppLl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("market://details?id=" + WaywtApplication.getContext().getPackageName());
			    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			    try {
			    	MainActivity.this.startActivity(goToMarket);
			    } catch (ActivityNotFoundException e) {
			    }
			}
		});
		
		

		
		CookieSyncManager.createInstance(getApplicationContext());
		mRedditSettings.loadRedditPreferences(this, mRedditClient);
		
		if( UserUtil.getHasChosenSubreddit() )
		{
			getSupportLoaderManager().initLoader(0, null, this);
		}
		else
			showSubredditDialog();
		
		
		// RESTORE STATE
		mSelectedTabFromDrawer = getIntent().getIntExtra(Extras.SELECTED_TAB_FROM_DRAWER, DrawerTabIndex.WAYWT);
		
		updateMainView(false);
	}
	
	

	public void onDrawerItemSelected(int position) {
		setTitle(getString(R.string.app_name));

		mSelectedTabFromDrawer = position;

		closeDrawer();

	}
	
	private void closeDrawer() {
		if (mDrawerLayout.isDrawerOpen(mDrawerRelativeLayout)) {
			mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
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
	
	public void updateMainView(boolean refresh) {
		switch (mSelectedTabFromDrawer) {
		case DrawerTabIndex.WAYWT:
			showWaywt(refresh);
			break;
		case DrawerTabIndex.MY_POSTS:
			showMyPosts(refresh);
			break;
		case DrawerTabIndex.SETTINGS:
			showSettings(refresh);
			break;
		}
	}
	
	public void showWaywt(boolean refresh) {

		if (mCurrentTab != DrawerTabIndex.WAYWT || refresh ) {
			setSelectedDrawerAdapterPosition(DrawerTabIndex.WAYWT);
			mCurrentTab = DrawerTabIndex.WAYWT;
			
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			getActionBar().setDisplayShowTitleEnabled(false);
			
			refreshNavigationBar(mCurrentWaywtIndex);
		}

	}

	public void showMyPosts(boolean refresh) {

		if (mCurrentTab != DrawerTabIndex.MY_POSTS || refresh ) {
			setSelectedDrawerAdapterPosition(DrawerTabIndex.MY_POSTS);
			mCurrentTab = DrawerTabIndex.MY_POSTS;
			
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getActionBar().setDisplayShowTitleEnabled(true);
			
			Fragment fragment = Fragment.instantiate(this, MyPostsFragment.class.getName());
			Bundle args = new Bundle();
			args.putString(MyPostsFragment.Extras.SUBREDDIT, UserUtil.getSubreddit());
			fragment.setArguments(args);
			
			String tag = MyPostsFragment.class.getCanonicalName();
			
			goToFragment(fragment, tag, "MY POSTS", true);
		}

	}
	
	public void showSettings(boolean refresh)
	{
		if (mCurrentTab != DrawerTabIndex.SETTINGS || refresh ) {
			setSelectedDrawerAdapterPosition(DrawerTabIndex.SETTINGS);
			mCurrentTab = DrawerTabIndex.SETTINGS;
			
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getActionBar().setDisplayShowTitleEnabled(true);
			
			Fragment fragment = Fragment.instantiate(this, SettingsFragment.class.getName());
			String tag = SettingsFragment.class.getCanonicalName();
			
			goToFragment(fragment, tag, "SETTINGS", true);
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
		fragmentTransaction.commit();

		FragmentTransaction ft = manager.beginTransaction();
		ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
		ft.replace(R.id.content_frame, fragment, tag);
		ft.commit();
		closeDrawer();

		if (title != null) {
			mTitle = title;
			setTitle(title);
		}
		

	}
	
	
    @Override
    public void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	AnalyticsUtil.sendView(this, TAG);
    }
    
    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		mRedditSettings.loadRedditPreferences(this, mRedditClient);
		CookieSyncManager.getInstance().startSync();

		if( UserUtil.getHasChosenSubreddit() )
		{
			fetchPostData();
		}
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
		
		if(mNavItems != null && position < mNavItems.size())
		{
			Fragment fragment = new WaywtFragment();
			Bundle args = new Bundle();
			args.putString(WaywtFragment.Extras.SUBREDDIT, UserUtil.getSubreddit());
			args.putString(WaywtFragment.Extras.PERMALINK, mNavItems.get(position).mPermalink);
			args.putString(WaywtFragment.Extras.POST_TITLE, mNavItems.get(position).mTitle);
			fragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();			 
		}
		else
		{
			Fragment fragment = new LoadingFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();			
		}
		
		mNavAdapter.notifyDataSetChanged(); 

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
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerRelativeLayout);
		if (drawerOpen)
			menu.clear();
		return super.onPrepareOptionsMenu(menu);
	}

	
	public void fetchPostData() {

		Intent i = new Intent(this, PostService.class);
		i.setAction(Intent.ACTION_SYNC);
		i.putExtra(PostService.Extras.IS_MALE, UserUtil.getIsMale());
		i.putExtra(PostService.Extras.IS_TEEN, UserUtil.getIsTeen());
		startService(i);  
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		boolean seeWaywtPosts = UserUtil.getSeeWaywtPosts();
		boolean seeOutfitFeedbackPosts = UserUtil.getSeeOutfitFeedbackPosts();
		boolean seeRecentPurchasesPosts = UserUtil.getSeeRecentPurchasesPosts();
		
		String query = PostTable.IS_MALE + "=? AND " + PostTable.IS_TEEN + "=?";
		
		int state = 0;
		
		if( !seeWaywtPosts && !seeOutfitFeedbackPosts && seeRecentPurchasesPosts )
			state = 1;
		else if( !seeWaywtPosts && seeOutfitFeedbackPosts && !seeRecentPurchasesPosts )
			state = 2;
		else if( !seeWaywtPosts && seeOutfitFeedbackPosts && seeRecentPurchasesPosts )
			state = 3;
		else if( seeWaywtPosts && !seeOutfitFeedbackPosts && !seeRecentPurchasesPosts )
			state = 4;
		else if( seeWaywtPosts && !seeOutfitFeedbackPosts && seeRecentPurchasesPosts )
			state = 5;
		else if( seeWaywtPosts && seeOutfitFeedbackPosts && !seeRecentPurchasesPosts )
			state = 6;
		else
			state = 7;
		
		
		switch( state )
		{
		case 0:
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0"}, PostTable.CREATED + " DESC");
		case 1:
			query += " AND " + PostTable.POST_TYPE + " =? ";
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0", PostType.RECENT_PURCHASES.ordinal()+""}, PostTable.CREATED + " DESC");
		case 2:
			query += " AND " + PostTable.POST_TYPE + " =? ";
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0", PostType.OUTFIT_FEEDBACK.ordinal()+""}, PostTable.CREATED + " DESC");
		case 3:
			query += " AND (" + PostTable.POST_TYPE + " =? OR " + PostTable.POST_TYPE + "=? )";
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0", PostType.RECENT_PURCHASES.ordinal()+"", PostType.OUTFIT_FEEDBACK.ordinal()+""}, PostTable.CREATED + " DESC");
		case 4:
			query += " AND " + PostTable.POST_TYPE + " =? ";
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0", PostType.WAYWT.ordinal()+""}, PostTable.CREATED + " DESC");
		case 5:
			query += " AND (" + PostTable.POST_TYPE + " =? OR " + PostTable.POST_TYPE + "=? )";
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0", PostType.RECENT_PURCHASES.ordinal()+"", PostType.WAYWT.ordinal()+""}, PostTable.CREATED + " DESC");
		case 6:
			query += " AND (" + PostTable.POST_TYPE + " =? OR " + PostTable.POST_TYPE + "=? )";
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0", PostType.WAYWT.ordinal()+"", PostType.OUTFIT_FEEDBACK.ordinal()+""}, PostTable.CREATED + " DESC");
		default:
		case 7:
			return new CursorLoader(this, Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, query, new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0"}, PostTable.CREATED + " DESC");
			
		}
		
		
	}


	
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		
		boolean refresh = true;
		
		if( mNavItems.size() > 0 && cursor.getCount() > 0 )
		{
			cursor.moveToFirst();
			NavItem item = mNavItems.get(0);
			refresh = !item.mPermalink.equals(cursor.getString(cursor.getColumnIndex(PostTable.PERMALINK)));
		}
		
		if( refresh || mResetActionBar )
		{
			
			mNavAdapter.clear();
			mNavItems.clear();
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
	
				NavItem navItem = new NavItem();
				navItem.mTitle = cursor.getString(cursor.getColumnIndex(PostTable.TITLE));
				navItem.mPermalink = cursor.getString(cursor.getColumnIndex(PostTable.PERMALINK));
				
				mNavAdapter.add(navItem);
				mNavItems.add(navItem);
			}
	
			mNavAdapter.notifyDataSetChanged();
	
			if (cursor.getCount() > 0 )
			{
				handler.sendEmptyMessage(MSG_HIDE_DIALOG);
				
				if( mCurrentTab == DrawerTabIndex.WAYWT ) 
					refreshNavigationBar(0);
			}
		}
		
		mResetActionBar = false;
		
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
			
			invalidateOptionsMenu();
			updateMainView(true);
		}
	}


	private void toggleDrawer() {
		if (mDrawerLayout.isDrawerOpen(mDrawerRelativeLayout)) {
			mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
		} else {
			mDrawerListAdapter.notifyDataSetChanged();
			mDrawerLayout.openDrawer(mDrawerRelativeLayout);

		}

	}

	
	private void showSubredditDialog() {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
		// builderSingle.setIcon(R.drawable.ic_launcher);
		builderSingle.setCancelable(false);
		builderSingle.setTitle("Choose Subreddit");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice); 
		arrayAdapter.add("Male Fashion Advice");
		arrayAdapter.add("Female Fashion Advice");
		arrayAdapter.add("Teen MFA");
		arrayAdapter.add("Teen FFA");
		
		int selected = 0;
		
		if( !UserUtil.getIsMale() )
			selected = 1;
		
		builderSingle.setSingleChoiceItems(arrayAdapter, selected, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
				case 0:
					UserUtil.setIsMale(true);
					UserUtil.setIsTeen(false);
					MainActivity.this.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo));
					break;
					
				case 1:
					UserUtil.setIsMale(false);
					UserUtil.setIsTeen(false);
					MainActivity.this.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo_ffa));
					break;
					
				case 2:
					UserUtil.setIsMale(true);
					UserUtil.setIsTeen(true);
					MainActivity.this.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo));
					break;
					
				case 3:
					UserUtil.setIsMale(false);
					UserUtil.setIsTeen(true);
					MainActivity.this.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo_ffa));
					break;
				}
				
				getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new Fragment()).commitAllowingStateLoss();
				
				UserUtil.setHasChosenSubreddit(true);
				
				handler.sendEmptyMessage(MSG_SHOW_DIALOG);
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
	
	public synchronized void updateCurrentNavItemDescription(String description)
	{
		if( mCurrentWaywtIndex < mNavAdapter.mArrayList.size() )
		{
			NavItem item = mNavAdapter.mArrayList.get(mCurrentWaywtIndex);
			item.mDescription = description;
			mNavAdapter.mArrayList.set(mCurrentWaywtIndex, item);
			
			mNavAdapter.notifyDataSetChanged();
		}
		
	}
	
	public static class CustomAdapter extends ArrayAdapter<NavItem> implements SpinnerAdapter {
		Context context;
		int textViewResourceId;
		public ArrayList<NavItem> mArrayList;
		
		public CustomAdapter(Context context, int textViewResourceId, ArrayList<NavItem> arrayList) {
			super(context, textViewResourceId, arrayList);

			this.context = context;
			this.textViewResourceId = textViewResourceId;
			this.mArrayList = arrayList;

		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_item_navigation, null);
				int padding = UiUtil.convertDpToPixel(8, context);
				
				convertView.setPadding(padding, padding, padding, padding);
			}

			NavItem item = mArrayList.get(position);
			
			TextView textView = (TextView) convertView.findViewById(R.id.nav_title_tv);
			textView.setText(item.mTitle);

			TextView descriptionTv = (TextView) convertView.findViewById(R.id.nav_description_tv);
			
			if( item.mDescription != null && !item.mDescription.isEmpty() )
			{
				descriptionTv.setVisibility(View.VISIBLE);
				descriptionTv.setText(item.mDescription);				
			}
			else
			{
				descriptionTv.setVisibility(View.GONE);
			}
			

			


			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = ((MainActivity)context).getLayoutInflater().inflate(R.layout.list_item_navigation, null);
			}
			NavItem item = mArrayList.get(position);
			
			TextView textview = (TextView) convertView.findViewById(R.id.nav_title_tv);
			textview.setText(item.mTitle);
			TextView descriptionTv = (TextView) convertView.findViewById(R.id.nav_description_tv);
			
			if( item.mDescription != null && !item.mDescription.isEmpty() )
			{
				descriptionTv.setVisibility(View.VISIBLE);
				descriptionTv.setText(item.mDescription);				
			}
			else
			{
				descriptionTv.setVisibility(View.GONE);
			}
			
			
			return convertView;
		}
	}
	
	public void resetWaywt()
	{
		mResetActionBar = true;
		mCurrentWaywtIndex = 0;
		
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getActionBar().setSelectedNavigationItem(0);
		getActionBar().setNavigationMode(ActionBar.DISPLAY_SHOW_TITLE);
	}


}
