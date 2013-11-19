package com.sobremesa.waywt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import retrofit.http.Query;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.PostTable;
import com.sobremesa.waywt.service.BaseService;
import com.sobremesa.waywt.service.RemoteObject;
import com.sobremesa.waywt.service.clients.PostServiceClient;
import com.sobremesa.waywt.service.synchronizer.PostPreprocessor;
import com.sobremesa.waywt.service.synchronizer.PostSynchronizer;
import com.sobremesa.waywt.service.synchronizer.RemotePreProcessor;
import com.sobremesa.waywt.service.synchronizer.Synchronizer;

import de.greenrobot.event.EventBus;

public class PostService extends BaseService {


	public class RemoteResponse {
		public String kind;
		public RemoteData data;
	}

	public class RemoteData extends RemoteObject {
		public String modhash;
		public String after;
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
		
		
		@Override
	    public boolean equals(Object object)
	    {
	        boolean sameSame = false;

	        if (object != null && object instanceof RemoteRedditPost)
	        {
	            sameSame = this.data.permalink == ((RemoteRedditPost) object).data.permalink;
	        }

	        return sameSame;
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
	
	public interface PostClient {
		@GET("/r/malefashionadvice/{path}.json")
		RemoteResponse getPosts(@EncodedPath("path") String path, @Query("t")String time, @Query("after")String after);  
	}
	
	
	
	

	public PostService() {
		super("RedditPostService");
	} 

	public PostService(Context c) {
		super("RedditPostService", c);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SYNC)) 
		{
			List<RemoteRedditPost> totalPosts = new ArrayList<RemoteRedditPost>();
			
			PostClient client = PostServiceClient.getInstance().getClient(getContext(), PostClient.class); 
			
			
			RemoteResponse response; 
			RemoteData remoteData;
			
			List<RemoteRedditPost> posts;
			
			Iterator<RemoteRedditPost> iter;
			
			
			// new 
			String after = "";
			int i = 0;
			
			while( after != null && i < 1)
			{
				response = client.getPosts("hot", "", after);  
				remoteData = response.data;
				
				posts = remoteData.children;   
				
				iter = posts.iterator();
				while (iter.hasNext()) {
					RemoteRedditPost post = iter.next();
					
				    if (!post.data.domain.equals("self.malefashionadvice") || post.data.author_flair_text == null || !post.data.author_flair_text.equals("Automated Robo-Mod") || !post.data.title.contains("WAYWT")) {
				        iter.remove(); 
				    }
				}
				
				totalPosts.addAll(posts);
				after = response.data.after;
				
				++i;
			}
			
			
			// today
			after = "";
			i = 0;
			
			while( after != null && i < 2)
			{
				response = client.getPosts("top", "today", after);  
				remoteData = response.data;
				
				posts = remoteData.children;  
				
				iter = posts.iterator();
				while (iter.hasNext()) {
					RemoteRedditPost post = iter.next();
					
				    if (!post.data.domain.equals("self.malefashionadvice") || post.data.author_flair_text == null || !post.data.author_flair_text.equals("Automated Robo-Mod") || !post.data.title.contains("WAYWT")) {
				        iter.remove(); 
				    }
				}
				
				totalPosts.addAll(posts);
				after = response.data.after;
				
				++i;
			}
			
			// week 
			after = "";
			i = 0;
			
			while( after != null && i < 2)
			{
				response = client.getPosts("top", "week", after);  
				remoteData = response.data;
				
				posts = remoteData.children;  
				
				iter = posts.iterator();
				while (iter.hasNext()) {
					RemoteRedditPost post = iter.next();
					
				    if (!post.data.domain.equals("self.malefashionadvice") || post.data.author_flair_text == null || !post.data.author_flair_text.equals("Automated Robo-Mod") || !post.data.title.contains("WAYWT") || totalPosts.contains(post)) {
				        iter.remove(); 
				    }
				}
				
				totalPosts.addAll(posts);
				after = response.data.after;
				
				++i;
			}
			
			
			// month
			after = "";
			i = 0;
			
			while( after != null && i < 5)
			{
				response = client.getPosts("top", "month", after);  
				remoteData = response.data;
				
				posts = remoteData.children;  
				
				iter = posts.iterator();
				while (iter.hasNext()) {
					RemoteRedditPost post = iter.next();
					
				    if (!post.data.domain.equals("self.malefashionadvice") || post.data.author_flair_text == null || !post.data.author_flair_text.equals("Automated Robo-Mod") || !post.data.title.contains("WAYWT") || totalPosts.contains(post)) {
				        iter.remove(); 
				    }
				}
				
				totalPosts.addAll(posts);
				after = response.data.after;
				
				++i;
			}

			
			if (totalPosts != null && totalPosts.size() > 0) { 
				// synchronize!
				Cursor localRecCursor = getContext().getContentResolver().query(Provider.POST_CONTENT_URI, PostTable.ALL_COLUMNS, null, null, null);
				localRecCursor.moveToFirst();
				synchronizeRemoteRecords(totalPosts, localRecCursor, localRecCursor.getColumnIndex(PostTable.PERMALINK), new PostSynchronizer(getContext()), new PostPreprocessor());
				
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
