package com.sobremesa.waywt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.RedditPostTable;
import com.sobremesa.waywt.service.BaseService;
import com.sobremesa.waywt.service.RemoteObject;
import com.sobremesa.waywt.service.ServiceClient;
import com.sobremesa.waywt.service.synchronizer.RedditPostPreprocessor;
import com.sobremesa.waywt.service.synchronizer.RedditPostSynchronizer;
import com.sobremesa.waywt.service.synchronizer.RemotePreProcessor;
import com.sobremesa.waywt.service.synchronizer.Synchronizer;

import de.greenrobot.event.EventBus;

public class RedditPostCommentService extends BaseService {

	public final static class Extras
	{
		public static final String ARG_PERMALINK = "arg_permalink";
	}


	private String mPermalink = "";
	
	public class RemoteResponse {
		
		
		public String kind;
//		public RemoteData data;
	}

	public class RemoteData extends RemoteObject {
		public String modhash;
		public List<RemoteRedditPost> children;
		
		@Override
		public String getIdentifier() {
			return "";
		}
	}
	
	public class RemoteRedditPost extends RemoteObject {
		public String kind;
		public RemoteRedditPostData data;
		
		@Override
		public String getIdentifier() {
			return mPermalink + data.id;
		}
	}

	public class RemoteRedditPostData extends RemoteObject {

		public String id; // unique
		
		public String author;
		public String created;
		public int ups;
		public int downs;
		
		public String body_html;
		
		@Override 
		public String getIdentifier() {
			return mPermalink + id;
		}
	}
	  
	// Interfaces
	
	public interface RedditPostCommentClient {
		@GET("/{path}")
		List<RemoteResponse> getComments(@EncodedPath("path") String path);
	}
	
	
	
	

	public RedditPostCommentService() {
		super("RedditPostCommentService");
	}

	public RedditPostCommentService(Context c) {
		super("RedditPostCommentService", c);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SYNC)) 
		{
			mPermalink = intent.getStringExtra(Extras.ARG_PERMALINK); 
			
			RedditPostCommentClient client = ServiceClient.getInstance().getClient(getContext(), RedditPostCommentClient.class);
			
			try {
				String encodedValue = URLEncoder.encode(String.valueOf(mPermalink.toLowerCase() + "search.json"), "UTF-8");
				encodedValue = encodedValue.replace("+", "%20");
				String uploadUrlEncoded = encodedValue.replace("%2F", "/");
				
				List<RemoteResponse> response = client.getComments( uploadUrlEncoded.substring(1));
				
				
				if (response != null && response.size() > 0) {
					// synchronize!
					Cursor localRecCursor = getContext().getContentResolver().query(Provider.REDDITPOSTCOMMENT_CONTENT_URI, null, null, null, null);
					localRecCursor.moveToFirst();
//					synchronizeRemoteRecords(posts, localRecCursor, localRecCursor.getColumnIndex(RedditPostTable.PERMALINK), new RedditPostSynchronizer(getContext()), new RedditPostPreprocessor());
					
					//
				} else {
//					EventBus.getDefault().post(new RecordingServiceEvent(false, "There where no representatives for this zip code"));
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			RemoteData remoteData = response.data;
//			
//			List<RemoteRedditPost> posts = remoteData.children;
			
//			Iterator<RemoteRedditPost> iter = posts.iterator();
//			while (iter.hasNext()) {
//				RemoteRedditPost post = iter.next();
//				
//			    if (!post.data.domain.equals("self.malefashionadvice") || post.data.author_flair_text == null || !post.data.author_flair_text.equals("Automated Robo-Mod") ) {
//			        iter.remove();
//			    }
//			}

			
			
		}
	}


	public void synchronizeRemoteRecords(List<RemoteRedditPost> remoteReps, Cursor localReps, int remoteIdentifierColumn, Synchronizer<RemoteRedditPost> synchronizer, RemotePreProcessor<RemoteRedditPost> preProcessor) {
		preProcessor.preProcessRemoteRecords(remoteReps);
		synchronizer.synchronize(getContext(), remoteReps, localReps, remoteIdentifierColumn);
	}

}
