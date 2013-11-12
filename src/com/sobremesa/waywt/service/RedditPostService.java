package com.sobremesa.waywt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import retrofit.http.GET;
import retrofit.http.Query;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;
import com.sobremesa.waywt.database.tables.RedditPostTable;
import com.sobremesa.waywt.service.BaseService;
import com.sobremesa.waywt.service.RemoteObject;
import com.sobremesa.waywt.service.RedditServiceClient;
import com.sobremesa.waywt.service.synchronizer.RedditPostPreprocessor;
import com.sobremesa.waywt.service.synchronizer.RedditPostSynchronizer;
import com.sobremesa.waywt.service.synchronizer.RemotePreProcessor;
import com.sobremesa.waywt.service.synchronizer.Synchronizer;

import de.greenrobot.event.EventBus;

public class RedditPostService extends BaseService {


	public class RemoteResponse {
		public String kind;
		public RemoteData data;
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
			return data.permalink;
		}
	}

	public class RemoteRedditPostData extends RemoteObject {

		public String domain;
		public String subreddit;
		public String permalink; // unique
		
		public String author;
		public String created;
		public String title;
		public int ups;
		public int downs;
		
		public String author_flair_text;
		
		@Override
		public String getIdentifier() {
			return permalink; 
		}
	}
	
	// Interfaces
	
	public interface RedditMaleFashionAdvicePostClient {
		@GET("/r/malefashionadvice/top.json")
		RemoteResponse getPosts(@Query("t")String time, @Query("q")String query);  
	}
	
	
	
	

	public RedditPostService() {
		super("RedditPostService");
	} 

	public RedditPostService(Context c) {
		super("RedditPostService", c);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SYNC)) 
		{
			
			
			RedditMaleFashionAdvicePostClient client = RedditServiceClient.getInstance().getClient(getContext(), RedditMaleFashionAdvicePostClient.class); 
			
			RemoteResponse response = client.getPosts("week", "waywt");  
			RemoteData remoteData = response.data;
			
			List<RemoteRedditPost> posts = remoteData.children;  
			
			Iterator<RemoteRedditPost> iter = posts.iterator();
			while (iter.hasNext()) {
				RemoteRedditPost post = iter.next();
				
			    if (!post.data.domain.equals("self.malefashionadvice") || post.data.author_flair_text == null || !post.data.author_flair_text.equals("Automated Robo-Mod") || !post.data.title.startsWith("WAYWT")) {
//				if (!post.data.domain.equals("self.malefashionadvice") || !post.data.title.contains("WAYWT")) {
			        iter.remove();
			    }
			}

			
			if (posts != null && posts.size() > 0) { 
				// synchronize!
				Cursor localRecCursor = getContext().getContentResolver().query(Provider.REDDITPOST_CONTENT_URI, RedditPostTable.ALL_COLUMNS, null, null, null);
				localRecCursor.moveToFirst();
				synchronizeRemoteRecords(posts, localRecCursor, localRecCursor.getColumnIndex(RedditPostTable.PERMALINK), new RedditPostSynchronizer(getContext()), new RedditPostPreprocessor());
				
				//
			} else {
//				EventBus.getDefault().post(new RecordingServiceEvent(false, "There where no representatives for this zip code"));
			}
			
		}
	}


	public void synchronizeRemoteRecords(List<RemoteRedditPost> remoteReps, Cursor localReps, int remoteIdentifierColumn, Synchronizer<RemoteRedditPost> synchronizer, RemotePreProcessor<RemoteRedditPost> preProcessor) {
		preProcessor.preProcessRemoteRecords(remoteReps);
		synchronizer.synchronize(getContext(), remoteReps, localReps, remoteIdentifierColumn);
	}

}
