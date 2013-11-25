package com.sobremesa.waywt.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
import com.sobremesa.waywt.listeners.CommentsListener;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
import android.widget.Toast;
import android.widget.ViewFlipper;

public class WaywtFragment extends Fragment implements CommentsListener {

	public static final String TAG = WaywtFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String SUBREDDIT = "subreddit";
		
		public static String PERMALINK = "permalink";
		public static String DO_SORT = "do_sort";
	}
	
    private final Pattern COMMENT_PATH_PATTERN = Pattern.compile(Constants.COMMENT_PATH_PATTERN_STRING);
    private final Pattern COMMENT_CONTEXT_PATTERN = Pattern.compile("context=(\\d+)");
    
	private boolean mDoSort = false;
    
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
    	
    	mSubreddit = getArguments().getString(Extras.SUBREDDIT);
    	mDoSort = getArguments().getBoolean(Extras.DO_SORT);
    	
    	String commentPath = null;
    	String commentQuery;
    	String jumpToCommentId = null;
        Uri data = Uri.parse(getArguments().getString(Extras.PERMALINK));
        if (data != null) {
        	// Comment path: a URL pointing to a thread or a comment in a thread.
        	commentPath = data.getPath();
        	commentQuery = data.getQuery();
        } else {
    		if (Constants.LOGGING) Log.e(TAG, "Quitting because no subreddit and thread id data was passed into the Intent.");
    		getActivity().finish();
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
		}
    }
    
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_waywt, null, false);
		
    	
    	
		mPager = (ViewPager)view.findViewById(R.id.pager);
		mPagerAdapter = new CommentPagerAdapter(getChildFragmentManager(), mSubreddit, mThreadId);
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
		 
		getNewDownloadCommentsTask().execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
	}
	
	@Override
	public void onDestroy() {
		mPagerAdapter = null;
		
		super.onDestroy();
	}
	
	@Override
    public void resetUI() {
//    	findViewById(R.id.loading_light).setVisibility(View.GONE);
//    	findViewById(R.id.loading_dark).setVisibility(View.GONE);
    	
    	mPagerAdapter.mIsLoading = false;
    }
    
	@Override
    public void enableLoadingScreen() {
//    	findViewById(R.id.loading_light).setVisibility(View.VISIBLE);
//		findViewById(R.id.loading_dark).setVisibility(View.GONE);
		
		
    	if (mPagerAdapter != null)
    		mPagerAdapter.mIsLoading = true;
    	getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_START);
    }


	@Override
	public void updateComments(List<ThingInfo> comments) {
		
		
		if( mDoSort )
			Collections.sort(comments);
		else
		{
			long seed = System.nanoTime();
			Collections.shuffle(comments, new Random(seed));
		}
		
		mPagerAdapter.addComments(comments);
		
		if( getView() != null )
		{
			ViewFlipper vf = (ViewFlipper)getView().findViewById(R.id.vf);
			vf.setDisplayedChild(1);
		}

	}
	
	
}
