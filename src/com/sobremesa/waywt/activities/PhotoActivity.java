package com.sobremesa.waywt.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.common.CacheInfo;
import com.sobremesa.waywt.common.Common;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.listeners.MyPostsListener;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.tasks.DownloadMyPostsTask;
import com.sobremesa.waywt.tasks.ImgurUploadTask;
import com.sobremesa.waywt.util.UserUtil;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;


/**
 * Activity displaying the taken photo and offering to share it with other apps.
 *
 * @author Sebastian Kaspari <sebastian@androidzeitgeist.com>
 */
public class PhotoActivity extends BaseFragmentActivity implements MyPostsListener {
	private static final String TAG = PhotoActivity.class.getSimpleName();
	
    private static final String MIME_TYPE = "image/jpeg";

	public static class Extras
	{
		public static String OP_COMMENT = "op_comment";
	}
	
    private ThingInfo mComment;
	
	private Uri mImageUri;
	private String mImgurUrl;
	
    private final HttpClient mRedditClient = WaywtApplication.getRedditClient();
    private final RedditSettings mRedditSettings = WaywtApplication.getRedditSettings();
    
	private MyImgurUploadTask mImgurUploadTask;
	
	
	// UI
	private EditText mTitleEt;
	private EditText mDescriptionEt;
	

	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Add a caption");
        
        mComment = getIntent().getParcelableExtra(Extras.OP_COMMENT);
        mImageUri = getIntent().getData();
        
        setContentView(R.layout.activity_photo);

        ImageView photoView = (ImageView) findViewById(R.id.photo);
        photoView.setImageURI(mImageUri);
        
        mTitleEt = (EditText)findViewById(R.id.photo_title_et);
        mDescriptionEt = (EditText)findViewById(R.id.photo_description_et);
        
        mDescriptionEt.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				postPhoto();
				return false;
			}
		});
        
        Button postBtn = (Button) findViewById(R.id.photo_post_btn);
        postBtn.setTypeface(FontManager.INSTANCE.getAppFont());
        postBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				postPhoto();
			}
		});
    }
    
    
    private void postPhoto()
    {
		if( !mTitleEt.getText().toString().isEmpty() && !mDescriptionEt.getText().toString().isEmpty() )
		{
			new MyImgurUploadTask(mImageUri).execute();
		}
		else
			Toast.makeText(this, "Please give your post a title and a description.", Toast.LENGTH_LONG).show();
    }
    

    
    private class MyImgurUploadTask extends ImgurUploadTask {
		public MyImgurUploadTask(Uri imageUri) {
			super(imageUri, PhotoActivity.this);
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			showProgressDialog("UPLOADING PHOTO TO IMGUR");
			
			if (mImgurUploadTask != null) {
				boolean cancelled = mImgurUploadTask.cancel(false);
				if (!cancelled)
					this.cancel(true);
			}
			mImgurUploadTask = this;
			mImgurUrl = null;
		}
		@Override
		protected void onPostExecute(String imageId) {
			super.onPostExecute(imageId);
			mImgurUploadTask = null;
			hideProgressDialog();
			
			if (imageId != null) {
				mImgurUrl = "http://imgur.com/" + imageId;
				
				
				String text = "[" + mTitleEt.getText().toString() + "](" + mImgurUrl + ")";
				text += System.getProperty ("line.separator") + System.getProperty ("line.separator");
				text += mDescriptionEt.getText().toString();
				
				new CommentReplyTask(mComment.getName()).execute(text);
				
			} else {
				mImgurUrl = null;
			}
		}
	}
    

    private class CommentReplyTask extends AsyncTask<String, Void, String> {
    	private String _mParentThingId;
    	String _mUserError = "Error submitting reply. Please try again.";
    	
    	CommentReplyTask(String parentThingId) {
    		_mParentThingId = parentThingId;
    	}
    	
    	
    	
    	@Override
        public String doInBackground(String... text) {
        	HttpEntity entity = null;
        	
        	if (!mRedditSettings.isLoggedIn()) {
//        		if( CommentFragment.this.getActivity() != null )
//        			Common.showErrorToast("You must be logged in to reply.", Toast.LENGTH_LONG, CommentFragment.this.getActivity());
        		_mUserError = "Not logged in";
        		return null;
        	}
        	// Update the modhash if necessary
        	if (mRedditSettings.getModhash() == null) {
        		String modhash = Common.doUpdateModhash(mRedditClient);
        		if (modhash == null) {
        			// doUpdateModhash should have given an error about credentials
        			Common.doLogout(mRedditSettings, mRedditClient, WaywtApplication.getContext());
        			if (Constants.LOGGING) Log.e(TAG, "Reply failed because doUpdateModhash() failed");
        			return null;
        		}
        		mRedditSettings.setModhash(modhash);
        	}
        	
        	try {
        		// Construct data
    			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    			nvps.add(new BasicNameValuePair("thing_id", _mParentThingId));
    			nvps.add(new BasicNameValuePair("text", text[0]));
    			nvps.add(new BasicNameValuePair("r", UserUtil.getSubreddit()));
    			nvps.add(new BasicNameValuePair("uh", mRedditSettings.getModhash()));
    			// Votehash is currently unused by reddit 
//    				nvps.add(new BasicNameValuePair("vh", "0d4ab0ffd56ad0f66841c15609e9a45aeec6b015"));
    			
    			HttpPost httppost = new HttpPost(Constants.REDDIT_BASE_URL + "/api/comment");
    	        httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    	        
    	        HttpParams params = httppost.getParams();
    	        HttpConnectionParams.setConnectionTimeout(params, 40000);
    	        HttpConnectionParams.setSoTimeout(params, 40000);
    	        
    	        if (Constants.LOGGING) Log.d(TAG, nvps.toString());
    	        
                // Perform the HTTP POST request
    	    	HttpResponse response = mRedditClient.execute(httppost);
    	    	entity = response.getEntity();
    	    	
            	// Getting here means success. Create a new CommentInfo.
            	return Common.checkIDResponse(response, entity);
            	
        	} catch (Exception e) {
        		if (Constants.LOGGING) Log.e(TAG, "CommentReplyTask", e);
        		_mUserError = e.getMessage();
        	} finally {
        		if (entity != null) {
        			try {
        				entity.consumeContent();
        			} catch (Exception e2) {
        				if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e2);
        			}
        		}
        	}
        	return null;
        }
    	
    	@Override
    	public void onPreExecute() {
			showProgressDialog("POSTING TO REDDIT");
    	}
    	
    	@Override
    	public void onPostExecute(String newId) {
    		hideProgressDialog();
    		
    		if (newId == null) {
//    			if( CommentFragment.this.getActivity() != null )
//    				Common.showErrorToast(_mUserError, Toast.LENGTH_LONG, CommentFragment.this.getActivity());
    			
    		} else {
    			// Refresh
    			CacheInfo.invalidateCachedThread(WaywtApplication.getContext());
    			
    			List<String> threadIds = new ArrayList<String>();
    			List<String> permalinks = new ArrayList<String>();
    			List<String> titles = new ArrayList<String>();
    			threadIds.add(mComment.getThreadId());
    			permalinks.add(mComment.getPostPermalink());
    			titles.add(mComment.getPostTitle());
    			
    			Log.d("threadId", mComment.getThreadId());
    			Log.d("permalink", mComment.getPostPermalink());
    			
				showProgressDialog("FINALIZING...");
				
    			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    				new DownloadMyPostsTask(PhotoActivity.this, UserUtil.getSubreddit(), threadIds, permalinks, titles, mRedditSettings, mRedditClient).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
    			else
    				new DownloadMyPostsTask(PhotoActivity.this, UserUtil.getSubreddit(), threadIds, permalinks, titles, mRedditSettings, mRedditClient).execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);

    		}
    	}
    }
    
    
    @Override
	protected int getOptionsMenuId() {
		return R.menu.photo;
	}

	@Override
	protected List<Integer> getMenuOptionIdsToStyle() {
		List<Integer> ids = super.getMenuOptionIdsToStyle();
		ids.add(R.id.post_menu_id);
		return ids;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.post_menu_id:
			postPhoto();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onSuccess() {
		hideProgressDialog();
		
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	

}
