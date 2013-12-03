package com.sobremesa.waywt.activities;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;

import com.sobremesa.waywt.R;
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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener, LoaderCallbacks<Cursor> {

	public static class PostPermalink {
		public String mId;
		public String mPermalink;
	}

	private CursorLoader mLoader;

	private ArrayAdapter<String> mNavAdapter;
	private int mCurrentWaywtIndex = 0;
	

	private ArrayList<PostPermalink> mPermalinks;

	private static final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
	private static final RedditSettings mSettings = new RedditSettings();

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			mSettings.loadRedditPreferences(this, null);

			mPermalinks = new ArrayList<PostPermalink>();

			// Set up the action bar to show a dropdown list.
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

			mNavAdapter = new ArrayAdapter<String>(actionBar.getThemedContext(), R.layout.list_item_navigation);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			// Set up the dropdown list navigation in the action bar.
			actionBar.setListNavigationCallbacks(mNavAdapter, this);

			
			
			// load settings
			CookieSyncManager.createInstance(getApplicationContext());
			mSettings.loadRedditPreferences(this, mClient);
		} catch (Exception e) {
			Common.showErrorToast("No Internet Connection", Toast.LENGTH_LONG, this);

		}

		if( UserUtil.getHasChosenSubreddit() )
		{
			getSupportLoaderManager().initLoader(0, null, this);
			fetchPostData();
		}
		else
			showSubredditDialog();
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

		mSettings.loadRedditPreferences(this, mClient);
		CookieSyncManager.getInstance().startSync();


	}

	@Override
	protected void onPause() {
		super.onPause();

		CookieSyncManager.getInstance().stopSync();
		mSettings.saveRedditPreferences(this);
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
		getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
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
			ViewFlipper vf = (ViewFlipper) findViewById(R.id.vf);
			vf.setDisplayedChild(1);

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
			if (mSettings.getUsername() != null) {
				final TextView loginUsernameInput = (TextView) dialog.findViewById(R.id.login_username_input);
				loginUsernameInput.setText(mSettings.getUsername());
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
			dialog = new LoginDialog(this, mSettings, false) {
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
			super(username, password, mSettings, mClient, getApplicationContext());
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
				mSettings.saveRedditPreferences(MainActivity.this);
			} else {
				Common.showErrorToast(mUserError, Toast.LENGTH_LONG, MainActivity.this);
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {

		
		// Login/Logout
		if (mSettings.isLoggedIn()) {
			menu.findItem(R.id.login_menu_id).setVisible(false);
			menu.findItem(R.id.logout_menu_id).setVisible(true);
			menu.findItem(R.id.logout_menu_id).setTitle(String.format(getResources().getString(R.string.logout), mSettings.getUsername()));
		} else {
			menu.findItem(R.id.login_menu_id).setVisible(true);
			menu.findItem(R.id.logout_menu_id).setVisible(false);
		}
		
		String sortByTxt = "RANDOM";

		switch (UserUtil.getSortBy()) {
		case 0:
			sortByTxt = "RANDOM";
			break;

		case 1:
			sortByTxt = "VOTES";
			break;

		case 2:
			sortByTxt = "COMMENTS";
			break;
		}

		menu.findItem(R.id.sort_by_menu_id).setTitle(String.format(getResources().getString(R.string.sort_by), sortByTxt));
		
		
		String subredditTxt = "MFA";

		if( UserUtil.getIsMale() )
			subredditTxt = "MFA";
		else
			subredditTxt = "FFA";
		
		menu.findItem(R.id.subreddit_menu_id).setTitle(String.format(getResources().getString(R.string.subreddit), subredditTxt));

		
//		List<Integer> optionIds = new ArrayList<Integer>();
//		optionIds.add(R.id.login_menu_id);
//		optionIds.add(R.id.logout_menu_id);
//		optionIds.add(R.id.sort_by_menu_id);
//		
//		for (Integer id : optionIds) {
//			final MenuItem item = menu.findItem(id);
//
//			if (item != null) {
//				View actionView = item.getActionView();
//
//				if (actionView == null) {
//					Log.d("ACTIONBAR", "creating action view");
//					actionView = this.getLayoutInflater().inflate(R.layout.action_menu_button_layout, null, false);
//					((TextView) actionView.findViewById(R.id.action_menu_button_text)).setText(item.getTitle());
//					((TextView) actionView.findViewById(R.id.action_menu_button_text)).setTypeface(FontManager.INSTANCE.getAppFont());
////					actionView.setBackgroundResource(Common.);
//					actionView.setOnClickListener(new OnClickListener() {
//
//						@Override
//						public void onClick(View v) {
//							menu.performIdentifierAction(item.getItemId(), 0);
//
//						}
//					});
//					item.setActionView(actionView);
//				} else if (actionView instanceof TextView) {
//					((TextView) actionView).setTypeface(FontManager.INSTANCE.getAppFont());
//				}
//			}
//		}
			
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.login_menu_id:
			showDialog(Constants.DIALOG_LOGIN);
			break;
		case R.id.logout_menu_id:
			Common.doLogout(mSettings, mClient, getApplicationContext());
			Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();

			mSettings.saveRedditPreferences(this);
			break;

		case R.id.sort_by_menu_id:
			showSortByDialog();
			break;
			
		case R.id.subreddit_menu_id:
			showSubredditDialog();
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	public static RedditSettings getSettings() {
		return mSettings;
	}

	public static HttpClient getClient() {
		return mClient;
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
				
				getSupportFragmentManager().beginTransaction().replace(R.id.container, new Fragment()).commitAllowingStateLoss();
				
				UserUtil.setHasChosenSubreddit(true);
				
				getSupportLoaderManager().restartLoader(0, null, MainActivity.this);
				fetchPostData();		

				
				
				dialog.dismiss();
			}
		});
		
		builderSingle.show();
	}

}
