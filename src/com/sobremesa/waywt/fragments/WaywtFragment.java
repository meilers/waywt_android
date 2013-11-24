package com.sobremesa.waywt.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import com.sobremesa.waywt.util.StringUtils;
import com.sobremesa.waywt.util.Util;
import com.sobremesa.waywt.R;
import com.sobremesa.waywt.adapters.CommentPagerAdapter;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.common.RedditIsFunHttpClientFactory;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.service.PostService;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.tasks.DownloadCommentsTask;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View; 
import android.view.ViewGroup;
import android.view.Window;

public class WaywtFragment extends Fragment {

	public static final String TAG = WaywtFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_POST_ID = "post_id";
		public static String ARG_PERMALINK = "permalink";
		public static String ARG_DO_SORT = "do_sort";
	}
	
    private final Pattern COMMENT_PATH_PATTERN = Pattern.compile(Constants.COMMENT_PATH_PATTERN_STRING);
    private final Pattern COMMENT_CONTEXT_PATTERN = Pattern.compile("context=(\\d+)");
    
	
	private ViewPager mPager;
	public CommentPagerAdapter mPagerAdapter;
	private TitlePageIndicator mindicator;
	
    private String mSubreddit = "malefashionadvice";
    private String mThreadId = null;
    private final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
    private final RedditSettings mSettings = new RedditSettings();
    
    private DownloadCommentsTask getNewDownloadCommentsTask() {
    	return new DownloadCommentsTask(
				this,
				mSubreddit,
				mThreadId,
				mSettings,
				mClient
		);
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    }
    
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_waywt, null, false);
		
		mPager = (ViewPager)view.findViewById(R.id.pager);
		mPagerAdapter = new CommentPagerAdapter(getChildFragmentManager(), new ArrayList<ThingInfo>(), getArguments().getBoolean(Extras.ARG_DO_SORT));
		mPager.setAdapter(mPagerAdapter);
		
		mindicator = (TitlePageIndicator)view.findViewById(R.id.page_indicator);
		mindicator.setViewPager(mPager);
		mindicator.setTypeface(FontManager.INSTANCE.getAppFont());
		
		
    	String commentPath;
    	String commentQuery;
    	String jumpToCommentId = null;
    	int jumpToCommentContext = 0;
		// We get the URL through getIntent().getData()
        Uri data = Uri.parse(getArguments().getString(Extras.ARG_PERMALINK));
        if (data != null) {
        	// Comment path: a URL pointing to a thread or a comment in a thread.
        	commentPath = data.getPath();
        	commentQuery = data.getQuery();
        } else {
    		if (Constants.LOGGING) Log.e(TAG, "Quitting because no subreddit and thread id data was passed into the Intent.");
    		getActivity().finish();
    		return null;
        }
        
    	if (commentPath != null) {
    		if (Constants.LOGGING) Log.d(TAG, "comment path: "+commentPath);
    		
    		if (Util.isRedditShortenedUri(data)) {
    			// http://redd.it/abc12
    			mThreadId = commentPath.substring(1);
    		} else {
    			// http://www.reddit.com/...
        		Matcher m = COMMENT_PATH_PATTERN.matcher(commentPath);
        		if (m.matches()) {
            		mSubreddit = m.group(1);
        			mThreadId = m.group(2);
        			jumpToCommentId = m.group(3);
        		}
    		}
    	} else {
			if (Constants.LOGGING) Log.e(TAG, "Quitting because of bad comment path.");
			getActivity().finish();
			return null;
		}
    	
    	if (commentQuery != null) {
    		Matcher m = COMMENT_CONTEXT_PATTERN.matcher(commentQuery);
    		if (m.find()) {
    			jumpToCommentContext = m.group(1) != null ? Integer.valueOf(m.group(1)) : 0;
    		}
    	}
    	
    	if (!StringUtils.isEmpty(jumpToCommentId)) {
    		getNewDownloadCommentsTask().prepareLoadAndJumpToComment(jumpToCommentId, jumpToCommentContext)
    				.execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
    	}
    	else {
    		getNewDownloadCommentsTask().execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
    	}
		
		return view;
	}
	
	@Override
	public void onStart() {  
		// TODO Auto-generated method stub
		super.onStart();
		 

	}
	
	@Override
	public void onDestroy() {
		mPagerAdapter = null;
		
		super.onDestroy();
	}
	
	
    public void resetUI() {
//    	findViewById(R.id.loading_light).setVisibility(View.GONE);
//    	findViewById(R.id.loading_dark).setVisibility(View.GONE);
    	
    	mPagerAdapter.mIsLoading = false;
    }
    
    public void enableLoadingScreen() {
//    	findViewById(R.id.loading_light).setVisibility(View.VISIBLE);
//		findViewById(R.id.loading_dark).setVisibility(View.GONE);
		
		
    	if (mPagerAdapter != null)
    		mPagerAdapter.mIsLoading = true;
    	getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_START);
    }
}
