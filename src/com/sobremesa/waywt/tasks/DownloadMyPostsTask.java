package com.sobremesa.waywt.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;

import android.Manifest.permission;
import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.sobremesa.waywt.activities.MainActivity;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.common.CacheInfo;
import com.sobremesa.waywt.common.Common;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.common.ProgressInputStream;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.database.tables.PostTable;
import com.sobremesa.waywt.fragments.CommentFragment;
import com.sobremesa.waywt.fragments.WaywtFragment;
import com.sobremesa.waywt.util.Assert;
import com.sobremesa.waywt.util.StringUtils;
import com.sobremesa.waywt.util.UserUtil;
import com.sobremesa.waywt.util.Util;
import com.sobremesa.waywt.util.Markdown;
import com.sobremesa.waywt.service.PostService.RemoteRedditPost;
import com.sobremesa.waywt.service.synchronizer.CommentPreprocessor;
import com.sobremesa.waywt.service.synchronizer.CommentSynchronizer;
import com.sobremesa.waywt.service.synchronizer.PostPreprocessor;
import com.sobremesa.waywt.service.synchronizer.PostSynchronizer;
import com.sobremesa.waywt.service.synchronizer.RemotePreProcessor;
import com.sobremesa.waywt.service.synchronizer.Synchronizer;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.listeners.MyPostsListener;
import com.sobremesa.waywt.model.Listing;
import com.sobremesa.waywt.model.ListingData;
import com.sobremesa.waywt.model.Post;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.model.ThingListing;

public class DownloadMyPostsTask extends AsyncTask<Integer, Long, Boolean>
		implements PropertyChangeListener {
	
	
	private static final String TAG = "CommentsListActivity.DownloadCommentsTask";
    private final ObjectMapper mObjectMapper = Common.getObjectMapper();

    private static AsyncTask<?, ?, ?> mCurrentDownloadCommentsTask = null;
    private static final Object mCurrentDownloadCommentsTaskLock = new Object();  
    
	private static final RedditSettings mRedditSettings = WaywtApplication.getRedditSettings();
	
    private WeakReference<MyPostsListener> mListenerRef;
    private String mSubreddit;
    private List<String> mThreadIds;
    private List<String> mPermalinks;
    private List<String> mTitles;
    private RedditSettings mSettings;
    private HttpClient mClient;
	
	private String mMoreChildrenId = "";

    // Progress bar
	private long mContentLength = 0;
	
	
	private int mJumpToCommentContext = 0;
	
    /**
     * List holding the comments to be appended at the end.
     * Used when loading an entire thread.
     */
    private final LinkedList<ThingInfo> mDeferredAppendList = new LinkedList<ThingInfo>();
	
	/**
	 * Default constructor to do normal comments page
	 */
	public DownloadMyPostsTask(
			MyPostsListener activity,
			String subreddit,
			List<String> threadIds,
			List<String> permalinks,
			List<String> titles,
			RedditSettings settings,
			HttpClient client
	) {
		this.mListenerRef = new WeakReference<MyPostsListener>(activity);
		this.mSubreddit = subreddit;
		this.mThreadIds = threadIds;
		this.mPermalinks = permalinks;
		this.mTitles = titles;
		this.mSettings = settings;
		this.mClient = client;  
	}
	

	
	// XXX: maxComments is unused for now
	public Boolean doInBackground(Integer... maxComments) {
		
		List<ThingInfo> myPosts = new ArrayList<ThingInfo>();
    	InputStream in = null;
		HttpEntity entity = null;
		ProgressInputStream pin = null;
		
		for( int i=0; i <mThreadIds.size(); ++i )
		{
		
			String threadId = mThreadIds.get(i);
			String title = mTitles.get(i);
			String permalink = mPermalinks.get(i);
			
	        try {
	        	StringBuilder sb = new StringBuilder(Constants.REDDIT_BASE_URL);
	    		if (mSubreddit != null) {
	    			sb.append("/r/").append(mSubreddit.trim());
	    		}
	    		sb.append("/comments/")
	        		.append(threadId)
	        		.append("/z/").append(mMoreChildrenId).append("/.json?")
	        		.append(mSettings.getCommentsSortByUrl()).append("&");
	        	if (mJumpToCommentContext != 0)
	        		sb.append("context=").append(mJumpToCommentContext).append("&");  
	        	
	        	sb.append("limit=500&depth=1");
	        	String url = sb.toString();
	        	

	    		boolean currentlyUsingCache = false;
	    		
	        	if (Constants.USE_COMMENTS_CACHE) {
	    			try {
		    			if (CacheInfo.checkFreshThreadCache(((Fragment) mListenerRef.get()).getActivity().getApplicationContext())
		    					&& url.equals(CacheInfo.getCachedThreadUrl(((Fragment) mListenerRef.get()).getActivity().getApplicationContext()))) {
		    				in = ((Fragment) mListenerRef.get()).getActivity().openFileInput(Constants.FILENAME_THREAD_CACHE);
		    				mContentLength = ((Fragment) mListenerRef.get()).getActivity().getFileStreamPath(Constants.FILENAME_THREAD_CACHE).length();
		    				currentlyUsingCache = true;
		    				if (Constants.LOGGING) Log.d(TAG, "Using cached thread JSON, length=" + mContentLength);
		    			}
	    			} catch (Exception cacheEx) {
	    				if (Constants.LOGGING) Log.w(TAG, "skip cache", cacheEx);
	    			}
	    		}
	    		
	    		// If we couldn't use the cache, then do HTTP request
	        	if (!currentlyUsingCache) {
			    	HttpGet request = new HttpGet(url);
	                HttpResponse response = mClient.execute(request);
	            	
	                // Read the header to get Content-Length since entity.getContentLength() returns -1
	            	Header contentLengthHeader = response.getFirstHeader("Content-Length");
	            	if (contentLengthHeader != null) {
	            		mContentLength = Long.valueOf(contentLengthHeader.getValue());
		            	if (Constants.LOGGING) Log.d(TAG, "Content length: "+mContentLength);
	            	}
	            	else {
	            		mContentLength = -1; 
		            	if (Constants.LOGGING) Log.d(TAG, "Content length: UNAVAILABLE");
	            	}
	
	            	entity = response.getEntity();
	            	in = entity.getContent();
	            	
	            	if (Constants.USE_COMMENTS_CACHE) {
	                	in = CacheInfo.writeThenRead(((Fragment) mListenerRef.get()).getActivity().getApplicationContext(), in, Constants.FILENAME_THREAD_CACHE);
	                	try {
	                		CacheInfo.setCachedThreadUrl(((Fragment) mListenerRef.get()).getActivity().getApplicationContext(), url);
	                	} catch (IOException e) {
	                		if (Constants.LOGGING) Log.e(TAG, "error on setCachedThreadId", e);
	                	}
	            	}
	        	}
	            
	        	// setup a special InputStream to report progress
	        	pin = new ProgressInputStream(in, mContentLength);
	        	pin.addPropertyChangeListener(this);
	        	
	        	List<ThingInfo> results = parseCommentsJSON(pin, threadId);
	        	
	        	for( ThingInfo ci : results )
	        	{
	        		ci.setPostPermalink(permalink);
	        		ci.setPostTitle(title);
	        		ci.setThreadId(threadId);
	        		myPosts.add(ci);
	        	}
	        	
	        	if (Constants.LOGGING) Log.d(TAG, "parseCommentsJSON completed");
	        	
	            
	            
	        } catch (Exception e) {
	        	if (Constants.LOGGING) Log.e(TAG, "DownloadCommentsTask", e);
	        	return false;
	        } finally {
	    		if (entity != null) {
	    			try {
	    				entity.consumeContent();
	    			} catch (Exception e2) {
	    				if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e2);
	    			}
	    		}
	        }
		}
		
        try {
        	pin.close();
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
    		if (entity != null) {
    			try {
    				entity.consumeContent();
    			} catch (Exception e2) {
    				if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e2);  
    			}
    		}
        }
		
		if (myPosts != null && myPosts.size() > 0) { 
			// synchronize!
			Cursor localRecCursor = WaywtApplication.getContext().getContentResolver().query(Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.AUTHOR + "=?", new String[] { mRedditSettings.getUsername() }, null);
			localRecCursor.moveToFirst();
			
			CommentSynchronizer sync = new CommentSynchronizer( WaywtApplication.getContext());
			synchronizeRemoteRecords(myPosts, localRecCursor, localRecCursor.getColumnIndex(CommentTable.NAME), sync, new CommentPreprocessor());
			
			//
		} else {
//			EventBus.getDefault().post(new RecordingServiceEvent(false, "There where no representatives for this zip code"));
		}
		
		if( mListenerRef.get() != null )
			mListenerRef.get().onSuccess();
		
        return true;
    }
	
	


	public void synchronizeRemoteRecords(List<ThingInfo> remoteReps, Cursor localReps, int remoteIdentifierColumn, Synchronizer<ThingInfo> synchronizer, RemotePreProcessor<ThingInfo> preProcessor) {
		preProcessor.preProcessRemoteRecords(remoteReps);
		synchronizer.synchronize(WaywtApplication.getContext(), remoteReps, localReps, remoteIdentifierColumn);
	}
	

	
	private List<ThingInfo> parseCommentsJSON(
			InputStream in, String threadId
	) throws IOException, JsonParseException {
		
		List<ThingInfo> results = new ArrayList<ThingInfo>();
		int insertedCommentIndex;
		String genericListingError = "Not a comments listing";
		try {

			
			Listing[] listings = mObjectMapper.readValue(in, Listing[].class);  

			// listings[0] is a thread Listing for the OP.
			// process same as a thread listing more or less
			
			Assert.assertEquals(Constants.JSON_LISTING, listings[0].getKind(), genericListingError);
			
			// Save modhash, ignore "after" and "before" which are meaningless in this context (and probably null)
			ListingData threadListingData = listings[0].getData();
			if (StringUtils.isEmpty(threadListingData.getModhash()))  
				mSettings.setModhash(null); 
			else
				mSettings.setModhash(threadListingData.getModhash());
			
			if (Constants.LOGGING) Log.d(TAG, "Successfully got OP listing[0]: modhash "+mSettings.getModhash());
			
			ThingListing threadThingListing = threadListingData.getChildren()[0];
			Assert.assertEquals(Constants.THREAD_KIND, threadThingListing.getKind(), genericListingError);

			insertedCommentIndex = 0;  // we just inserted the OP into position 0

			
			
			// listings[1] is a comment Listing for the comments 
			// Go through the children and get the ThingInfos
			Pattern pattern1 = Pattern.compile("href=\"[^\"]+?imgur.com[^\"]+?\"");
			Pattern pattern2 = Pattern.compile("href=\"[^\"]+?dressed.so[^\"]+?\"");
			Pattern pattern3 = Pattern.compile("href=\"[^\"]+?drsd.so[^\"]+?\""); 
			
			ListingData commentListingData = listings[1].getData();
			for (ThingListing commentThingListing : commentListingData.getChildren()) {
				// insert the comment and its replies, prefix traversal order
				
				ThingInfo thingInfo = commentThingListing.getData();
				
				if( thingInfo != null && thingInfo.getBody_html() != null )
				{
					Matcher matcher1 = pattern1.matcher(Html.fromHtml(thingInfo.getBody_html()));  
					Matcher matcher2 = pattern2.matcher(Html.fromHtml(thingInfo.getBody_html()));
					Matcher matcher3 = pattern3.matcher(Html.fromHtml(thingInfo.getBody_html()));
					 
					boolean one = matcher1.find();
					boolean two = matcher2.find();
					boolean three = matcher3.find();
							
					if (one || two || three)  
					{
						ThingInfo ci = commentThingListing.getData();
						
						if( ci.getAuthor().equals(mRedditSettings.getUsername()))
						{
							if (ci.getBody_html() != null) {
					        	CharSequence spanned = createSpanned(ci.getBody_html());  
					        	ci.setSpannedBody(spanned);
							}
							
							results.add(ci);
						}
					}
				}
			}
			
			
		} catch (Exception ex) {
			if (Constants.LOGGING) Log.e(TAG, "parseCommentsJSON", ex);
		}
		
		return results;
	}
    
    
    /**
     * Call from UI Thread
     */
    private void insertCommentsUI() {
//    	mListenerRef.get().updateMyPosts(mDeferredAppendList);
    }
	
    
	
    void cleanupDeferred() {
    	mDeferredAppendList.clear();
    }
    
//    @Override
//	public void onPreExecute() {
//		if (mThreadIds == null) {
//			if (Constants.LOGGING) Log.e(TAG, "mSettings.threadId == null");
//    		this.cancel(true);
//    		return;
//		}
//		synchronized (mCurrentDownloadCommentsTaskLock) {
//    		if (mCurrentDownloadCommentsTask != null) {
//    			mCurrentDownloadCommentsTask.cancel(true);
//    		}
//    		mCurrentDownloadCommentsTask = this;
//		}
//		
//		
//		if (mContentLength == -1)
//			((Fragment) mListenerRef.get()).getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_ON);
//	}
    
	@Override
	public void onPostExecute(Boolean success) {
		
		if( mListenerRef.get() != null )
		{
			insertCommentsUI();
			
			
			if (success) {
			} else {
				if (!isCancelled()) {
					if( mListenerRef.get() instanceof Fragment ) 
						Common.showErrorToast("No Internet Connection", Toast.LENGTH_LONG, ((Fragment) mListenerRef.get()).getActivity());
					else
						Common.showErrorToast("No Internet Connection", Toast.LENGTH_LONG, ((Activity) mListenerRef.get()));
				}
			}
		}
		
		synchronized (mCurrentDownloadCommentsTaskLock) {
			mCurrentDownloadCommentsTask = null;
		}
		
		mListenerRef = null;
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		publishProgress((Long) event.getNewValue());
	}
	
    private CharSequence createSpanned(String bodyHtml) {
    	try {
    		// get unescaped HTML
    		bodyHtml = Html.fromHtml(bodyHtml).toString();
    		// fromHtml doesn't support all HTML tags. convert <code> and <pre>
    		bodyHtml = Util.convertHtmlTags(bodyHtml);
    		
    		Spanned body = Html.fromHtml(bodyHtml);
    		// remove last 2 newline characters
    		if (body.length() > 2)
    			return body.subSequence(0, body.length()-2);
    		else
    			return "";
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "createSpanned failed", e);
    		return null;
    	}
    }
}

