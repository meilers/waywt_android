package com.sobremesa.waywt.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import com.sobremesa.waywt.util.AnalyticsUtil;
import com.sobremesa.waywt.util.StringUtils;
import com.sobremesa.waywt.util.UserUtil;
import com.sobremesa.waywt.util.Util;
import com.sobremesa.waywt.R;
import com.sobremesa.waywt.activities.CameraActivity;
import com.sobremesa.waywt.activities.MainActivity;
import com.sobremesa.waywt.activities.MainActivity.DrawerTabIndex;
import com.sobremesa.waywt.activities.MainActivity.NavItem;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.common.Constants;  
import com.sobremesa.waywt.common.RedditIsFunHttpClientFactory;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.database.tables.PostTable;
import com.sobremesa.waywt.enums.SortByType;
import com.sobremesa.waywt.listeners.CommentsListener;
import com.sobremesa.waywt.listeners.MyPostsListener;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.model.Post;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.service.PostService;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.tasks.DownloadMyPostsTask;
import com.viewpagerindicator.TitlePageIndicator;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.TextView;
import android.widget.Toast;

public class MyPostsFragment extends BaseFragment implements MyPostsListener, LoaderCallbacks<Cursor> {

	public static final String TAG = WaywtFragment.class.getSimpleName();

	public static final int POST_LOADER_ID = 0;
	public static final int COMMENT_LOADER_ID = 1;
	
	
	public static class Extras {
		public static String SUBREDDIT = "subreddit";
	}
	
	

	private final Pattern COMMENT_PATH_PATTERN = Pattern.compile(Constants.COMMENT_PATH_PATTERN_STRING);
	private final Pattern COMMENT_CONTEXT_PATTERN = Pattern.compile("context=(\\d+)");

	private ViewPager mPager;
	public MyPostPagerAdapter mPagerAdapter;
	private TitlePageIndicator mIndicator;

	private String mSubreddit = UserUtil.getSubreddit();
	private List<String> mThreadIds;
	private List<String> mPermalinks;
	private List<String> mTitles;
	private ThingInfo mOPComment = null;
	
	private final HttpClient mRedditClient = WaywtApplication.getRedditClient();
	private final RedditSettings mRedditSettings = WaywtApplication.getRedditSettings();

	private MenuItem mRefreshMenuItem;
	private MenuItem mLoadingMenuItem;
	
	
	private DownloadMyPostsTask getNewDownloadMyPostsTask() {
		return new DownloadMyPostsTask(this, mSubreddit, mThreadIds,mPermalinks, mTitles, mRedditSettings, mRedditClient);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mThreadIds = new ArrayList<String>();
		mPermalinks = new ArrayList<String>();
		mTitles = new ArrayList<String>();
		
		mSubreddit = getArguments().getString(Extras.SUBREDDIT);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_my_posts, null, false);

		mPager = (ViewPager) view.findViewById(R.id.pager);
		mPagerAdapter = new MyPostPagerAdapter(getChildFragmentManager(), mSubreddit);
		mPager.setAdapter(mPagerAdapter);

		mIndicator = (TitlePageIndicator) view.findViewById(R.id.page_indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setTypeface(FontManager.INSTANCE.getAppFont());

		setHasOptionsMenu(true);
		
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		if( mRedditSettings.isLoggedIn() )
		{
			getLoaderManager().initLoader(COMMENT_LOADER_ID, null, this);			
			getLoaderManager().initLoader(POST_LOADER_ID, null, this);
		}
		else
		{
			getView().findViewById(R.id.loading).setVisibility(View.GONE);
			getView().findViewById(R.id.not_logged_in_tv).setVisibility(View.VISIBLE);
			
			getActivity().showDialog(Constants.DIALOG_LOGIN);
		}

	}
	

    @Override
    public void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	AnalyticsUtil.sendView(getActivity(), TAG);
    }

	@Override
	public void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
		mRedditSettings.saveRedditPreferences(getActivity());
	}

	@Override
	public void onDestroy() {
		mPagerAdapter = null;

		super.onDestroy();
	} 

	
	
	private void fetchComments()
	{
		if (mRefreshMenuItem != null && mLoadingMenuItem != null) {
			mLoadingMenuItem.setVisible(true);
			mRefreshMenuItem.setVisible(false);
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getNewDownloadMyPostsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
		else
			getNewDownloadMyPostsTask().execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
	}
	
	
	private int getReplyCount( ThingInfo ci )
	{
		int total = 0;
		
		if( ci.getReplies() != null && ci.getReplies().getData() != null && ci.getReplies().getData().getChildren() != null )
		{
			for( int i=0; i< ci.getReplies().getData().getChildren().length; ++i)
				total += getReplyCount( ci.getReplies().getData().getChildren()[i].getData() );
				
			return ci.getReplies().getData().getChildren().length + total;
			
		}
		else
			return total;
	}
	

	public static class MyPostPagerAdapter extends FragmentStatePagerAdapter {
		public boolean mIsLoading = false;

		private ArrayList<ThingInfo> mMyPosts = new ArrayList<ThingInfo>();

		private String mSubreddit = "";

		public MyPostPagerAdapter(FragmentManager fragmentManager, String subreddit) {
			super(fragmentManager);

			mSubreddit = subreddit;
		}

		public void addMyPosts(List<ThingInfo> comments) {
			mMyPosts.clear();
			mMyPosts.addAll(comments);

			this.notifyDataSetChanged();
		}

		@Override
		public Fragment getItem(int position) {

			CommentFragment fragment = new CommentFragment();
			Bundle args = new Bundle();
			
			args.putString(CommentFragment.Extras.SUBREDDIT, mSubreddit);
			args.putString(CommentFragment.Extras.THREAD_ID,  mMyPosts.get(position).getThreadId());
			args.putParcelable(CommentFragment.Extras.COMMENT, mMyPosts.get(position));  
			fragment.setArguments(args);

			return fragment;

		}

		@Override
		public int getCount() {
			return mMyPosts.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			
			if( mMyPosts.get(position).getPostTitle() != null )
				return mMyPosts.get(position).getPostTitle().toUpperCase();
			
			return "";
		}

	}
	
	
	@Override
	protected int getOptionsMenuId() {
		return R.menu.my_posts;
	}
	
	@Override
	protected void onFinisingCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onFinisingCreateOptionsMenu(menu, inflater);
		
		mRefreshMenuItem = menu.findItem(R.id.refresh_menu_id);
		mLoadingMenuItem = menu.findItem(R.id.loading_menu_id);
		mLoadingMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {

		
//		// Login/Logout
//		if (mRedditSettings.isLoggedIn()) {
//			menu.findItem(R.id.login_menu_id).setVisible(false);
//			menu.findItem(R.id.logout_menu_id).setVisible(true);
//			menu.findItem(R.id.logout_menu_id).setTitle(String.format(getResources().getString(R.string.logout), mRedditSettings.getUsername()));
//		} else {
//			menu.findItem(R.id.login_menu_id).setVisible(true);
//			menu.findItem(R.id.logout_menu_id).setVisible(false);
//		}
//		
//		String sortByTxt = "RANDOM";
//
//		switch (UserUtil.getSortBy()) {
//		case 0:
//			sortByTxt = "RANDOM";
//			break;
//
//		case 1:
//			sortByTxt = "VOTES";
//			break;
//
//		case 2:
//			sortByTxt = "COMMENTS";
//			break;
//		}
//
//		menu.findItem(R.id.sort_by_menu_id).setTitle(String.format(getResources().getString(R.string.sort_by), sortByTxt));
//		
//		
//		String subredditTxt = "MFA";
//
//		if( UserUtil.getIsMale() )
//			subredditTxt = "MFA";
//		else
//			subredditTxt = "FFA";
//		
//		menu.findItem(R.id.subreddit_menu_id).setTitle(String.format(getResources().getString(R.string.subreddit), subredditTxt));

		
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
			
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		
			case R.id.camera_menu_id:
				if (!mRedditSettings.isLoggedIn())
				{
					getActivity().showDialog(Constants.DIALOG_LOGIN);  
					break;
				}
				
				if( mOPComment != null )
				{
					Intent intent = new Intent(getActivity(), CameraActivity.class);
					intent.putExtra(CameraActivity.Extras.OP_COMMENT, (Parcelable)mOPComment);
					startActivity(intent);					
				}

				break;
				
			case R.id.refresh_menu_id:
				fetchComments();
				break;
			
//		case R.id.login_menu_id:
//			showDialog(Constants.DIALOG_LOGIN);
//			break;
//		case R.id.logout_menu_id:
//			Common.doLogout(mRedditSettings, mRedditClient, getApplicationContext());
//			Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
//
//			mRedditSettings.saveRedditPreferences(this);
//			break;
//
//		case R.id.sort_by_menu_id:
//			showSortByDialog();
//			break;
//			
//		case R.id.subreddit_menu_id:
//			showSubredditDialog();
//			break;

		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		switch(loaderId)
		{
		case POST_LOADER_ID:
			return new CursorLoader(getActivity(), Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, PostTable.IS_MALE + "=? AND " + PostTable.IS_TEEN + "=?", new String[] { UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0" }, PostTable.CREATED + " DESC");
		case COMMENT_LOADER_ID:
			return new CursorLoader(getActivity(), Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.AUTHOR + "=? AND " + CommentTable.IS_MALE + "=? AND " + CommentTable.IS_TEEN + "=?", new String[] { mRedditSettings.getUsername(), UserUtil.getIsMale() ? "1" : "0", UserUtil.getIsTeen() ? "1" : "0" }, CommentTable.CREATED + " DESC");
		}
		
		return null;
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		switch( loader.getId() )
		{
		case POST_LOADER_ID:
			
			mThreadIds.clear();
			mPermalinks.clear();
			mTitles.clear();
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				
				mTitles.add(cursor.getString(cursor.getColumnIndex(PostTable.TITLE)));
				mPermalinks.add(cursor.getString(cursor.getColumnIndex(PostTable.PERMALINK)));
				
				String commentPath = null;
				String commentQuery;
				String jumpToCommentId = null;
				Uri data = Uri.parse(cursor.getString(cursor.getColumnIndex(PostTable.PERMALINK)));
				if (data != null) {
					// Comment path: a URL pointing to a thread or a comment in a
					// thread.
					commentPath = data.getPath();
					commentQuery = data.getQuery();
				} else {
					if (Constants.LOGGING)
						Log.e(TAG, "Quitting because no subreddit and thread id data was passed into the Intent.");
					getActivity().finish();
				}
				
				Post post;
				
				if (commentPath != null) {
					if (Constants.LOGGING)
						Log.d(TAG, "comment path: " + commentPath);
					
					post = new Post();
					
					if (Util.isRedditShortenedUri(data)) {
						// http://redd.it/abc12
						post.setPermalink(commentPath.substring(1));
						post.setTitle(cursor.getString(cursor.getColumnIndex(PostTable.TITLE)));
						mThreadIds.add(commentPath.substring(1));
					} else {
						// http://www.reddit.com/...
						Matcher m = COMMENT_PATH_PATTERN.matcher(commentPath);
						if (m.matches()) {
							mSubreddit = m.group(1);
							mThreadIds.add(m.group(2));
							jumpToCommentId = m.group(3);
						}
					}
				} else {
					if (Constants.LOGGING)
						Log.e(TAG, "Quitting because of bad comment path.");
					getActivity().finish();
				}
			}
			
			fetchComments(); 
			
			
			break;
			
		case COMMENT_LOADER_ID: 
			
			List<ThingInfo> myPosts = new ArrayList<ThingInfo>();
			
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				
				ThingInfo comment = new ThingInfo();
				comment.setPostTitle(cursor.getString(cursor.getColumnIndex(CommentTable.POST_TITLE)));
				comment.setPostPermalink(cursor.getString(cursor.getColumnIndex(CommentTable.POST_PERMALINK)));
				comment.setThreadId(cursor.getString(cursor.getColumnIndex(CommentTable.THREAD_ID)));
				comment.setAuthor(cursor.getString(cursor.getColumnIndex(CommentTable.AUTHOR)));
				comment.setBody_html(cursor.getString(cursor.getColumnIndex(CommentTable.BODY_HTML)));
				comment.setId(cursor.getString(cursor.getColumnIndex(CommentTable.COMMENT_ID)));
				comment.setDowns(cursor.getInt(cursor.getColumnIndex(CommentTable.DOWNS)));
				comment.setUps(cursor.getInt(cursor.getColumnIndex(CommentTable.UPS)));
				comment.setLikes(cursor.getInt(cursor.getColumnIndex(CommentTable.LIKES)) == 1);
				comment.setName(cursor.getString(cursor.getColumnIndex(CommentTable.NAME)));
				comment.setCreated_utc(cursor.getLong(cursor.getColumnIndex(CommentTable.CREATED)));
				
				myPosts.add(comment);
			}

			if( cursor.getCount() > 0 )
			{
				if (getView() != null) {

					getView().findViewById(R.id.loading).setVisibility(View.GONE);
					
					mPagerAdapter = new MyPostPagerAdapter(getChildFragmentManager(), mSubreddit);
					mPager.setAdapter(mPagerAdapter);

					mIndicator = (TitlePageIndicator) getView().findViewById(R.id.page_indicator);
					mIndicator.setViewPager(mPager);
					mIndicator.setTypeface(FontManager.INSTANCE.getAppFont());
					
					mPagerAdapter.addMyPosts(myPosts);
					
					// UPDATE MENU ITEMS
					if (mRefreshMenuItem != null && mLoadingMenuItem != null) {
						mRefreshMenuItem.setVisible(true);
						mLoadingMenuItem.setVisible(false);
					}
				}
			}
			break;
		}
		

	}
	
	

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(final List<ThingInfo> posts) {
		if( getActivity() != null && getView() != null )
		{
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					getView().findViewById(R.id.loading).setVisibility(View.GONE);
					
					if( posts.size() == 0 )
					{
						TextView tv = (TextView)getView().findViewById(R.id.not_logged_in_tv);
						tv.setVisibility(View.VISIBLE);
						tv.setText("No Posts.");
					}
				}
			});					
		}
	}


}
