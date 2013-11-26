package com.sobremesa.waywt.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
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

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.sobremesa.waywt.activities.MainActivity;
import com.sobremesa.waywt.common.CacheInfo;
import com.sobremesa.waywt.common.Common;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.common.ProgressInputStream;
import com.sobremesa.waywt.fragments.WaywtFragment;
import com.sobremesa.waywt.util.Assert;
import com.sobremesa.waywt.util.StringUtils;
import com.sobremesa.waywt.util.Util;
import com.sobremesa.waywt.util.Markdown;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.listeners.CommentsListener;
import com.sobremesa.waywt.model.Listing;
import com.sobremesa.waywt.model.ListingData;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.model.ThingListing;

/**
 * Task takes in a subreddit name string and thread id, downloads its data, parses
 * out the comments, and communicates them back to the UI as they are read.
 * 
 * Requires the following navigation variables to be set:
 * mSettings.subreddit
 * mSettings.threadId
 * mMoreChildrenId (can be "")
 * mSortByUrl
 */
public class DownloadCommentsTask extends AsyncTask<Integer, Long, Boolean>
		implements PropertyChangeListener {
	
	
	private static final String TAG = "CommentsListActivity.DownloadCommentsTask";
    private final ObjectMapper mObjectMapper = Common.getObjectMapper();
    private final Markdown markdown = new Markdown();

    private static AsyncTask<?, ?, ?> mCurrentDownloadCommentsTask = null;
    private static final Object mCurrentDownloadCommentsTaskLock = new Object();  
    
    private final Object mCurrentShowThumbnailsTaskLock = new Object();
    
    private CommentsListener mListener;
    private String mSubreddit;
    private String mThreadId;
    private String mThreadTitle;
    private RedditSettings mSettings;
    private HttpClient mClient;
	
	// offset of the first comment being loaded; 0 if it includes OP
	private int mPositionOffset = 0;
	private int mIndentation = 0;
	private String mMoreChildrenId = "";
    private ThingInfo mOpThingInfo = null;

    // Progress bar
	private long mContentLength = 0;
	
	private String mJumpToCommentId = "";
	private int mJumpToCommentFoundIndex = -1;
	
	private int mJumpToCommentContext = 0;
	
    /**
     * List holding the comments to be appended at the end.
     * Used when loading an entire thread.
     */
    private final LinkedList<ThingInfo> mDeferredAppendList = new LinkedList<ThingInfo>();
    /**
     * List holding the comments to be inserted at mPositionOffset; the existing comment there will be removed.
     * Used for "load more comments" links.
     */
    private final LinkedList<ThingInfo> mDeferredReplacementList = new LinkedList<ThingInfo>();
	
	/**
	 * Default constructor to do normal comments page
	 */
	public DownloadCommentsTask(
			CommentsListener activity,
			String subreddit,
			String threadId,
			RedditSettings settings,
			HttpClient client
	) {
		this.mListener = activity;
		this.mSubreddit = subreddit;
		this.mThreadId = threadId;
		this.mSettings = settings;
		this.mClient = client;  
	}
	
	/**
	 * "load more comments" starting at this position
	 * @param moreChildrenId The reddit thing-id of the "more" children comment
	 * @param morePosition Position in local list to insert
	 * @param indentation The indentation level of the child.
	 */
	public DownloadCommentsTask prepareLoadMoreComments(String moreChildrenId, int morePosition, int indentation) {
		mMoreChildrenId = moreChildrenId;
		mPositionOffset = morePosition;
		mIndentation = indentation;
		return this;
	}
	
	public DownloadCommentsTask prepareLoadAndJumpToComment(String commentId, int context) {
		mJumpToCommentId = commentId;
		mJumpToCommentContext = context;
		return this;
	}
	
	// XXX: maxComments is unused for now
	public Boolean doInBackground(Integer... maxComments) {
		HttpEntity entity = null;
        try {
        	StringBuilder sb = new StringBuilder(Constants.REDDIT_BASE_URL);
    		if (mSubreddit != null) {
    			sb.append("/r/").append(mSubreddit.trim());
    		}
    		sb.append("/comments/")
        		.append(mThreadId)
        		.append("/z/").append(mMoreChildrenId).append("/.json?")
        		.append(mSettings.getCommentsSortByUrl()).append("&");
        	if (mJumpToCommentContext != 0)
        		sb.append("context=").append(mJumpToCommentContext).append("&");
        	
        	String url = sb.toString();
        	
        	InputStream in = null;
    		boolean currentlyUsingCache = false;
    		
        	if (Constants.USE_COMMENTS_CACHE) {
    			try {
	    			if (CacheInfo.checkFreshThreadCache(((Fragment) mListener).getActivity().getApplicationContext())
	    					&& url.equals(CacheInfo.getCachedThreadUrl(((Fragment) mListener).getActivity().getApplicationContext()))) {
	    				in = ((Fragment) mListener).getActivity().openFileInput(Constants.FILENAME_THREAD_CACHE);
	    				mContentLength = ((Fragment) mListener).getActivity().getFileStreamPath(Constants.FILENAME_THREAD_CACHE).length();
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
                	in = CacheInfo.writeThenRead(((Fragment) mListener).getActivity().getApplicationContext(), in, Constants.FILENAME_THREAD_CACHE);
                	try {
                		CacheInfo.setCachedThreadUrl(((Fragment) mListener).getActivity().getApplicationContext(), url);
                	} catch (IOException e) {
                		if (Constants.LOGGING) Log.e(TAG, "error on setCachedThreadId", e);
                	}
            	}
        	}
            
        	// setup a special InputStream to report progress
        	ProgressInputStream pin = new ProgressInputStream(in, mContentLength);
        	pin.addPropertyChangeListener(this);
        	
        	parseCommentsJSON(pin);
        	if (Constants.LOGGING) Log.d(TAG, "parseCommentsJSON completed");
        	
        	pin.close();
            in.close();
            
            return true;
            
        } catch (Exception e) {
        	if (Constants.LOGGING) Log.e(TAG, "DownloadCommentsTask", e);
        } finally {
    		if (entity != null) {
    			try {
    				entity.consumeContent();
    			} catch (Exception e2) {
    				if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e2);
    			}
    		}
        }
        return false;
    }
	
	
	/**
	 * defer insertion of comment for adding at end of entire comments list
	 */
	private void deferCommentAppend(ThingInfo comment) {
		mDeferredAppendList.add(comment);
	}
	
	/**
	 * defer insertion of comment for "more" case
	 */
	private void deferCommentReplacement(ThingInfo comment) {
		mDeferredReplacementList.add(comment);
	}
	
	/**
	 * tell if inserting entire thread, versus loading "more comments"
	 */
	private boolean isInsertingEntireThread() {
		return mPositionOffset == 0;
	}
	
	private void disableLoadingScreenKeepProgress() {
		
		Activity act = ((Fragment) mListener).getActivity();
		
		if( act != null )
		{
			((Fragment) mListener).getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
		    		mListener.resetUI();
				}
			});			
		}

	}
	
	private void parseCommentsJSON(
			InputStream in
	) throws IOException, JsonParseException {
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

			if (isInsertingEntireThread()) {
				parseOP(threadThingListing.getData());
				insertedCommentIndex = 0;  // we just inserted the OP into position 0
				
				// at this point we've started displaying comments, so disable the loading screen
				disableLoadingScreenKeepProgress();
			}
			else {
				insertedCommentIndex = mPositionOffset - 1;  // -1 because we +1 for the first comment
			}
			
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
						
						if (ci.getBody_html() != null) {
				        	CharSequence spanned = createSpanned(ci.getBody_html());
				        	ci.setSpannedBody(spanned);
						}
						
						if (isInsertingEntireThread())
							deferCommentAppend(ci);
						else
							deferCommentReplacement(ci);
						
//						insertedCommentIndex = insertNestedComment(commentThingListing, 0, insertedCommentIndex + 1);
					}
				}
			}
			
			
		} catch (Exception ex) {
			if (Constants.LOGGING) Log.e(TAG, "parseCommentsJSON", ex);
		}
	}
	
	private void parseOP(final ThingInfo data) {
		data.setIndent(0);
		
//		mListener.getActivity().runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				mListener.mCommentsList.add(0, data);
//			}
//		});

		if (data.isIs_self() && data.getSelftext_html() != null) {
			// HTML to Spanned
			String unescapedHtmlSelftext = Html.fromHtml(data.getSelftext_html()).toString();
			Spanned selftext = Html.fromHtml(Util.convertHtmlTags(unescapedHtmlSelftext));
			
    		// remove last 2 newline characters
			if (selftext.length() > 2)
				data.setSpannedSelftext(selftext.subSequence(0, selftext.length()-2));
			else
				data.setSpannedSelftext("");

			// Get URLs from markdown
			markdown.getURLs(data.getSelftext(), data.getUrls());
		}
		
		// We might not have a title if we've intercepted a plain link to a thread.
		mThreadTitle = data.getTitle();
		mSubreddit = data.getSubreddit();
		mThreadId = data.getId();
		
		mOpThingInfo = data;
	}
	
	/**
	 * Recursive method to insert comment tree into the mCommentsList,
	 * with proper list order and indentation
	 */
	int insertNestedComment(ThingListing commentThingListing, int indentLevel, int insertedCommentIndex) {
		ThingInfo ci = commentThingListing.getData();
		
		// Add comment to deferred append/replace list
//		if (isInsertingEntireThread())
//			deferCommentAppend(ci);
//		else
//			deferCommentReplacement(ci);

		
		// Formatting that applies to all items, both real comments and "more" entries
		ci.setIndent(mIndentation + indentLevel);
		
		// Handle "more" entry
		if (Constants.MORE_KIND.equals(commentThingListing.getKind())) {
			ci.setLoadMoreCommentsPlaceholder(true);
			if (Constants.LOGGING) Log.v(TAG, "new more position at " + (insertedCommentIndex));
	    	return insertedCommentIndex;
		}
		
		// Regular comment
		
		// Skip things that are not comments, which shouldn't happen
		if (!Constants.COMMENT_KIND.equals(commentThingListing.getKind())) {
			if (Constants.LOGGING) Log.e(TAG, "comment whose kind is \""+commentThingListing.getKind()+"\" (expected "+Constants.COMMENT_KIND+")");
			return insertedCommentIndex;
		}
		
		// handle the replies
		Listing repliesListing = ci.getReplies();
		if (repliesListing == null)
			return insertedCommentIndex;
		ListingData repliesListingData = repliesListing.getData();
		if (repliesListingData == null)
			return insertedCommentIndex;
		ThingListing[] replyThingListings = repliesListingData.getChildren();
		if (replyThingListings == null)
			return insertedCommentIndex;
		
		for (ThingListing replyThingListing : replyThingListings) {
			insertedCommentIndex = insertNestedComment(replyThingListing, indentLevel + 1, insertedCommentIndex + 1);
		}
		return insertedCommentIndex;
	}
	
	private boolean isHasJumpTarget() {
		return ! StringUtils.isEmpty(mJumpToCommentId);
	}
	
	private boolean isFoundJumpTargetComment() {
		return mJumpToCommentFoundIndex != -1;
	}
	

	
    /**
     * Call from UI Thread
     */
    private void insertCommentsUI() {
    	mListener.updateComments(mDeferredAppendList);
    }
	
    
	
    void cleanupDeferred() {
    	mDeferredAppendList.clear();
    	mDeferredReplacementList.clear();
    }
    
    @Override
	public void onPreExecute() {
		if (mThreadId == null) {
			if (Constants.LOGGING) Log.e(TAG, "mSettings.threadId == null");
    		this.cancel(true);
    		return;
		}
		synchronized (mCurrentDownloadCommentsTaskLock) {
    		if (mCurrentDownloadCommentsTask != null) {
    			mCurrentDownloadCommentsTask.cancel(true);
    		}
    		mCurrentDownloadCommentsTask = this;
		}
		
		if (isInsertingEntireThread()) {

			
    		// Do loading screen when loading new thread; otherwise when "loading more comments" don't show it
			mListener.enableLoadingScreen();
		}
		
		if (mContentLength == -1)
			((Fragment) mListener).getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_ON);
	}
    
	@Override
	public void onPostExecute(Boolean success) {
		
		if( (Fragment) mListener != null && ((Fragment) mListener).getActivity() != null )
		{
			if (isInsertingEntireThread()) {
				insertCommentsUI();
				if (isFoundJumpTargetComment());
	//				mListener.getListView().setSelection(;);
			}
			
			
			if (mContentLength == -1)
				((Fragment) mListener).getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_OFF);
			else
				((Fragment) mListener).getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_END);
			
			if (success) {
				// We should clear any replies the user was composing.
	//			mListener.setShouldClearReply(true);
	//
	//			// Set title in android titlebar
	//			if (mThreadTitle != null)
	//				mListener.setTitle(mThreadTitle + " : " + mSubreddit);
			} else {
				if (!isCancelled()) {
					Common.showErrorToast("No Internet Connection", Toast.LENGTH_LONG, ((Fragment) mListener).getActivity());
					mListener.resetUI();
				}
			}
		}
		
		synchronized (mCurrentDownloadCommentsTaskLock) {
			mCurrentDownloadCommentsTask = null;
		}
		
		mListener = null;
	}
	
	@Override
	public void onProgressUpdate(Long... progress) {
		
		if( mListener != null && ((Fragment) mListener).getActivity() != null )
		{
			if (mContentLength == -1)
				((Fragment) mListener).getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_INDETERMINATE_ON);
			else
				((Fragment) mListener).getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS, progress[0].intValue() * (Window.PROGRESS_END-1) / (int) mContentLength);
		}
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

