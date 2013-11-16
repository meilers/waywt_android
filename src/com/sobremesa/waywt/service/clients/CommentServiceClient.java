package com.sobremesa.waywt.service.clients;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sobremesa.waywt.service.CommentService;
import com.sobremesa.waywt.service.CommentService.RemoteData;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostComment;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentData;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentDataReplies;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentDataRepliesData;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentDataRepliesDataChild;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentDataRepliesDataChildData;
import com.sobremesa.waywt.service.CommentService.RemoteResponse;

import retrofit.RestAdapter;
import retrofit.Server;
import retrofit.converter.GsonConverter;
import android.content.Context;

public class CommentServiceClient {
	private static CommentServiceClient instance;
	public static final String BASE_URL_PROD = "http://www.reddit.com";
	public static final String BASE_URL_TEST = BASE_URL_PROD;
	public static final String BASE_URL_DEV = BASE_URL_PROD;

	private RestAdapter mRestAdapter;
	private Map<String, Object> mClients = new HashMap<String, Object>();

	private String mBaseUrl = BASE_URL_PROD;

	private CommentServiceClient() {
	}

	public static CommentServiceClient getInstance() {
		if (null == instance) {
			instance = new CommentServiceClient();
		}

		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T> T getClient(Context context, Class<T> clazz) {
		if (mRestAdapter == null) {
			Gson gson = new GsonBuilder()  
	           .registerTypeAdapter(RemoteResponse.class, new RedditPostCommentDeserializer())    
	           .create();  
			
			mRestAdapter = new RestAdapter.Builder().setConverter(new GsonConverter(gson)).setServer(new Server(getBaseUrl(context))).build();
		}
		T client = null;
		if ((client = (T) mClients.get(clazz.getCanonicalName())) != null) {
			return client;
		}
		client = mRestAdapter.create(clazz);
		mClients.put(clazz.getCanonicalName(), client);
		return client;
	}

	public void setRestAdapter(RestAdapter restAdapter) {
		mRestAdapter = restAdapter;
	}

	public String getBaseUrl(Context context) {
		return mBaseUrl;
	}

	private static class RedditPostCommentDeserializer implements JsonDeserializer<RemoteResponse> {
		@Override
		public RemoteResponse deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			JsonObject jobject = (JsonObject) json;

			String str = json.toString();
			
			// remoteResopnse
			RemoteResponse remoteResponse = new RemoteResponse();
			remoteResponse.data = new RemoteData();
			JsonObject remoteResponseData = jobject.get("data").getAsJsonObject();
			
			
			// remoteData
			remoteResponse.data.children = new ArrayList<RemoteRedditPostComment>();
			JsonArray remoteDataChildren = remoteResponseData.get("children").getAsJsonArray();
			
			
			// RemoteRedditPostComment
			for( int i=0; i < remoteDataChildren.size(); ++i)
			{
				RemoteRedditPostComment remoteRedditPostComment = new RemoteRedditPostComment();
				remoteRedditPostComment.data = new RemoteRedditPostCommentData();
				
				JsonObject remoteRedditPostCommentChild = remoteDataChildren.get(i).getAsJsonObject();
				JsonObject remoteRedditPostCommentChildData = remoteRedditPostCommentChild.get("data").getAsJsonObject();
				
				String subredditId = null;
				
				if( remoteRedditPostCommentChildData.has("subreddit_id"))
					subredditId = remoteRedditPostCommentChildData.get("subreddit_id").getAsString();
				
				String id = null;
				
				if( remoteRedditPostCommentChildData.has("id"))
					id = remoteRedditPostCommentChildData.get("id").getAsString();
				
				String author = null;
				
				if( remoteRedditPostCommentChildData.has("author"))
					author = remoteRedditPostCommentChildData.get("author").getAsString();
				
				String created = null;
				
				if( remoteRedditPostCommentChildData.has("created"))
					created = remoteRedditPostCommentChildData.get("created").getAsString();
				
				String bodyHtml = null;
				
				if( remoteRedditPostCommentChildData.has("body_html"))
					bodyHtml = remoteRedditPostCommentChildData.get("body_html").getAsString();
				
				int ups = 0;
				
				if( remoteRedditPostCommentChildData.has("ups"))
					ups = remoteRedditPostCommentChildData.get("ups").getAsInt();
				
				int downs = 0;
				
				if( remoteRedditPostCommentChildData.has("downs"))
					downs = remoteRedditPostCommentChildData.get("downs").getAsInt();
				
				remoteRedditPostComment.data.replies = getReplies( remoteRedditPostCommentChildData );
				
				
				if( subredditId != null && id != null && author != null && created != null && bodyHtml != null )
				{
					remoteRedditPostComment.data.subreddit_id = subredditId;
					remoteRedditPostComment.data.id = id;
					remoteRedditPostComment.data.author = author;
					remoteRedditPostComment.data.created = created;
					remoteRedditPostComment.data.body_html = bodyHtml;
					remoteRedditPostComment.data.ups = ups;
					remoteRedditPostComment.data.downs = downs;
					
					remoteResponse.data.children.add(remoteRedditPostComment);
				}
				
			}
			
			
			
			return remoteResponse;
		}
	}
	
	
	private static RemoteRedditPostCommentDataReplies getReplies( JsonObject jsonCommentData )
	{
		RemoteRedditPostCommentDataReplies commentDataReplies = null;
		
		if( jsonCommentData.has("replies") && jsonCommentData.get("replies").isJsonObject())
		{
			commentDataReplies = new RemoteRedditPostCommentDataReplies();
			JsonObject remoteRedditPostCommentChildDataReplies = jsonCommentData.get("replies").getAsJsonObject();
			JsonObject remoteRedditPostCommentChildDataRepliesData = remoteRedditPostCommentChildDataReplies.get("data").getAsJsonObject();
			
			commentDataReplies.data = new RemoteRedditPostCommentDataRepliesData();
			JsonArray remoteRedditPostCommentChildDataRepliesDataChildren = remoteRedditPostCommentChildDataRepliesData.get("children").getAsJsonArray();
			
			commentDataReplies.data.children = new ArrayList<RemoteRedditPostCommentDataRepliesDataChild>();
			
			for( int j=0; j < remoteRedditPostCommentChildDataRepliesDataChildren.size(); ++j)
			{
				RemoteRedditPostCommentDataRepliesDataChild remoteRedditPostCommentDataRepliesDataChild = new RemoteRedditPostCommentDataRepliesDataChild();
				remoteRedditPostCommentDataRepliesDataChild.data = new RemoteRedditPostCommentDataRepliesDataChildData();
				
				JsonObject remoteRedditPostCommentChildDataRepliesDataChild = remoteRedditPostCommentChildDataRepliesDataChildren.get(j).getAsJsonObject();
				JsonObject remoteRedditPostCommentChildDataRepliesDataChildData = remoteRedditPostCommentChildDataRepliesDataChild.get("data").getAsJsonObject();
				
				String replySubredditId = null;
				String replyAuthor = null;
				String replyId = null;
				String replyCreated = null;
				String replyBodyHtml = null;
				int replyUps = 0;
				int replyDowns = 0;
				
				if( remoteRedditPostCommentChildDataRepliesDataChildData.has("subreddit_id"))
					replySubredditId = remoteRedditPostCommentChildDataRepliesDataChildData.get("subreddit_id").getAsString();
				
				if( remoteRedditPostCommentChildDataRepliesDataChildData.has("id"))
					replyId = remoteRedditPostCommentChildDataRepliesDataChildData.get("id").getAsString();
				
				if( remoteRedditPostCommentChildDataRepliesDataChildData.has("subreddit_id"))
					replyAuthor = remoteRedditPostCommentChildDataRepliesDataChildData.get("author").getAsString();
				
				if( remoteRedditPostCommentChildDataRepliesDataChildData.has("created"))
					replyCreated = remoteRedditPostCommentChildDataRepliesDataChildData.get("created").getAsString();
				
				if( remoteRedditPostCommentChildDataRepliesDataChildData.has("body_html"))
					replyBodyHtml = remoteRedditPostCommentChildDataRepliesDataChildData.get("body_html").getAsString();
				
				if( remoteRedditPostCommentChildDataRepliesDataChildData.has("ups"))
					replyUps = remoteRedditPostCommentChildDataRepliesDataChildData.get("ups").getAsInt();
				
				if( remoteRedditPostCommentChildDataRepliesDataChildData.has("downs"))
					replyDowns = remoteRedditPostCommentChildDataRepliesDataChildData.get("downs").getAsInt();
				
				// recursion
				remoteRedditPostCommentDataRepliesDataChild.data.replies = getReplies( remoteRedditPostCommentChildDataRepliesDataChildData );
				
				
				if( replyId != null && replyAuthor != null && replyCreated != null && replyBodyHtml != null )
				{
					remoteRedditPostCommentDataRepliesDataChild.data.subreddit_id = replySubredditId;
					remoteRedditPostCommentDataRepliesDataChild.data.id = replyId;
					remoteRedditPostCommentDataRepliesDataChild.data.author = replyAuthor;
					remoteRedditPostCommentDataRepliesDataChild.data.created = replyCreated;
					remoteRedditPostCommentDataRepliesDataChild.data.body_html = replyBodyHtml;
					remoteRedditPostCommentDataRepliesDataChild.data.ups = replyUps;
					remoteRedditPostCommentDataRepliesDataChild.data.downs = replyDowns;
					
					commentDataReplies.data.children.add(remoteRedditPostCommentDataRepliesDataChild);
					
				}
			}
			
		}
		
		return commentDataReplies;
	}
	
	
}
