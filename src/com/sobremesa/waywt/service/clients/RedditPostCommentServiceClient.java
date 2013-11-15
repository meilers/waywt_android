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
import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteData;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostComment;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostCommentData;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostCommentDataReply;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostCommentDataReplyData;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChild;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChildData;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteResponse;

import retrofit.RestAdapter;
import retrofit.Server;
import retrofit.converter.GsonConverter;
import android.content.Context;

public class RedditPostCommentServiceClient {
	private static RedditPostCommentServiceClient instance;
	public static final String BASE_URL_PROD = "http://www.reddit.com";
	public static final String BASE_URL_TEST = BASE_URL_PROD;
	public static final String BASE_URL_DEV = BASE_URL_PROD;

	private RestAdapter mRestAdapter;
	private Map<String, Object> mClients = new HashMap<String, Object>();

	private String mBaseUrl = BASE_URL_PROD;

	private RedditPostCommentServiceClient() {
	}

	public static RedditPostCommentServiceClient getInstance() {
		if (null == instance) {
			instance = new RedditPostCommentServiceClient();
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
				
				if( remoteRedditPostCommentChildData.has("replies") && remoteRedditPostCommentChildData.get("replies").isJsonObject())
				{
					remoteRedditPostComment.data.replies = new RemoteRedditPostCommentDataReply();
					JsonObject remoteRedditPostCommentChildDataReply = remoteRedditPostCommentChildData.get("replies").getAsJsonObject();
					JsonObject remoteRedditPostCommentChildDataReplyData = remoteRedditPostCommentChildDataReply.get("data").getAsJsonObject();
					
					remoteRedditPostComment.data.replies.data = new RemoteRedditPostCommentDataReplyData();
					JsonArray remoteRedditPostCommentChildDataReplyDataChildren = remoteRedditPostCommentChildDataReplyData.get("children").getAsJsonArray();
					
					remoteRedditPostComment.data.replies.data.children = new ArrayList<RemoteRedditPostCommentDataReplyDataChild>();
					
					for( int j=0; j < remoteRedditPostCommentChildDataReplyDataChildren.size(); ++j)
					{
						RemoteRedditPostCommentDataReplyDataChild remoteRedditPostCommentDataReplyDataChild = new RemoteRedditPostCommentDataReplyDataChild();
						remoteRedditPostCommentDataReplyDataChild.data = new RemoteRedditPostCommentDataReplyDataChildData();
						
						JsonObject remoteRedditPostCommentChildDataReplyDataChild = remoteRedditPostCommentChildDataReplyDataChildren.get(j).getAsJsonObject();
						JsonObject remoteRedditPostCommentChildDataReplyDataChildData = remoteRedditPostCommentChildDataReplyDataChild.get("data").getAsJsonObject();
						
						String replySubredditId = null;
						String replyAuthor = null;
						String replyId = null;
						String replyCreated = null;
						String replyBodyHtml = null;
						int replyUps = 0;
						int replyDowns = 0;
						
						if( remoteRedditPostCommentChildDataReplyDataChildData.has("subreddit_id"))
							replySubredditId = remoteRedditPostCommentChildDataReplyDataChildData.get("subreddit_id").getAsString();
						
						if( remoteRedditPostCommentChildDataReplyDataChildData.has("id"))
							replyId = remoteRedditPostCommentChildDataReplyDataChildData.get("id").getAsString();
						
						if( remoteRedditPostCommentChildDataReplyDataChildData.has("subreddit_id"))
							replyAuthor = remoteRedditPostCommentChildDataReplyDataChildData.get("author").getAsString();
						
						if( remoteRedditPostCommentChildDataReplyDataChildData.has("created"))
							replyCreated = remoteRedditPostCommentChildDataReplyDataChildData.get("created").getAsString();
						
						if( remoteRedditPostCommentChildDataReplyDataChildData.has("body_html"))
							replyBodyHtml = remoteRedditPostCommentChildDataReplyDataChildData.get("body_html").getAsString();
						
						if( remoteRedditPostCommentChildDataReplyDataChildData.has("ups"))
							replyUps = remoteRedditPostCommentChildDataReplyDataChildData.get("ups").getAsInt();
						
						if( remoteRedditPostCommentChildDataReplyDataChildData.has("downs"))
							replyDowns = remoteRedditPostCommentChildDataReplyDataChildData.get("downs").getAsInt();
						
						if( replySubredditId != null && replyId != null && replyAuthor != null && replyCreated != null && replyBodyHtml != null )
						{
							remoteRedditPostCommentDataReplyDataChild.data.subreddit_id = replySubredditId;
							remoteRedditPostCommentDataReplyDataChild.data.id = replyId;
							remoteRedditPostCommentDataReplyDataChild.data.author = replyAuthor;
							remoteRedditPostCommentDataReplyDataChild.data.created = replyCreated;
							remoteRedditPostCommentDataReplyDataChild.data.body_html = replyBodyHtml;
							remoteRedditPostCommentDataReplyDataChild.data.ups = replyUps;
							remoteRedditPostCommentDataReplyDataChild.data.downs = replyDowns;
							
							remoteRedditPostComment.data.replies.data.children.add(remoteRedditPostCommentDataReplyDataChild);
							
						}
					}
					
				}
				else
					remoteRedditPostComment.data.replies = null;
				
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
}
