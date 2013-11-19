package com.sobremesa.waywt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import retrofit.http.Query;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;

import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.ReplyTable;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.service.BaseService;
import com.sobremesa.waywt.service.RemoteObject;
import com.sobremesa.waywt.service.PostService.RemoteRedditPost;
import com.sobremesa.waywt.service.clients.ImgurServiceClient;
import com.sobremesa.waywt.service.clients.CommentServiceClient;
import com.sobremesa.waywt.service.clients.PostServiceClient;
import com.sobremesa.waywt.service.synchronizer.ImagePreprocessor;
import com.sobremesa.waywt.service.synchronizer.ImageSynchronizer;
import com.sobremesa.waywt.service.synchronizer.CommentPreprocessor;
import com.sobremesa.waywt.service.synchronizer.ReplyPreprocessor;
import com.sobremesa.waywt.service.synchronizer.ReplySynchronizer;
import com.sobremesa.waywt.service.synchronizer.CommentSynchronizer;
import com.sobremesa.waywt.service.synchronizer.PostPreprocessor;
import com.sobremesa.waywt.service.synchronizer.PostSynchronizer;
import com.sobremesa.waywt.service.synchronizer.RemotePreProcessor;
import com.sobremesa.waywt.service.synchronizer.Synchronizer;

import de.greenrobot.event.EventBus;

public class CommentService extends BaseService {

	public final static class Extras
	{
		public static final String ARG_POST_ID = "arg_post_id";
		public static final String ARG_PERMALINK = "arg_permalink";
	}

	static public String mPostId = "";
	static public String mPermalink = "";
	
	public static class RemoteResponse {
		
		public RemoteResponse()
		{
		}
		
		public RemoteData data;
	}

	public static class RemoteData extends RemoteObject {
		public RemoteData()
		{
		}
		
		public List<RemoteRedditPostComment> children;
		 
		@Override
		public String getIdentifier() {
			return "";
		}
	}
	
	public static class RemoteRedditPostComment extends RemoteObject {
		public RemoteRedditPostComment()
		{
		}
		
		public RemoteRedditPostCommentData data;
		
		@Override
		public String getIdentifier() {
			return data.subreddit_id + data.id;
		}
	}

	public static class RemoteRedditPostCommentData extends RemoteObject {

		public RemoteRedditPostCommentData()
		{
		}
		
		public String postId; //parent
		
		public String subreddit_id = ""; // unique
		public String id; // unique
		
		public String author;
		public String created;
		public int ups;
		public int downs;
		
		public String body_html;
		
		public RemoteRedditPostCommentDataReplies replies = null;
		
		@Override 
		public String getIdentifier() {  
			return subreddit_id + id;
		}
	}
	
	public static class RemoteRedditPostCommentDataReplies extends RemoteObject {
		public RemoteRedditPostCommentDataReplies()
		{
		}
		
		public RemoteRedditPostCommentDataRepliesData data;
		
		@Override 
		public String getIdentifier() {  
			return "";
		}
	}
	
	public static class RemoteRedditPostCommentDataRepliesData extends RemoteObject {

		public RemoteRedditPostCommentDataRepliesData()
		{
		}
		
		public List<RemoteRedditPostCommentDataRepliesDataChild> children;
		 
		@Override
		public String getIdentifier() {
			return "";
		}
	}
	
	public static class RemoteRedditPostCommentDataRepliesDataChild extends RemoteObject {

		public RemoteRedditPostCommentDataRepliesDataChild()
		{
		}
		
		public RemoteRedditPostCommentDataRepliesDataChildData data;
		
		@Override 
		public String getIdentifier() {  
			return data.subreddit_id + data.id;
		}
	}
	
	public static class RemoteRedditPostCommentDataRepliesDataChildData extends RemoteObject {

		public RemoteRedditPostCommentDataRepliesDataChildData()
		{
		}
		
		public String postId; //parent
		public String commentId; //parent
		
		public String parentReplyIdentifier; //parent
		
		public String subreddit_id = ""; // unique
		public String id; // unique
		
		public String author;
		public String created;
		public int ups;
		public int downs;
		
		public String body_html;
		
		public RemoteRedditPostCommentDataReplies replies = null;
		
		@Override 
		public String getIdentifier() {  
			return subreddit_id + id;
		}
	}
	
	
	
	
	
	
	public static class RemoteImage extends RemoteObject {
		public String postId; //parent
		public String commentId; //parent
		public String url;
		
		@Override
		public String getIdentifier() {
			return postId+commentId+url;
		}
	}
	
	
	
	// imgur
	
	public class RemoteImgurResponse {
		
		public RemoteImgurAlbum data;
	}
	

	public class RemoteImgurAlbum {
		
		
		public List<RemoteImgurAlbumImage> images;
	}
	
	public class RemoteImgurAlbumImage {
		
		
		public String link;
	}


	
	// Interfaces
	
	public interface RedditPostCommentClient {
		@GET("/{path}")
		List<RemoteResponse> getComments(@EncodedPath("path") String path);
	}
	
	public interface ImgurClient {
		@GET("/3/album/{path}")
		RemoteImgurResponse getAlbum(@Header("Authorization") String auth, @EncodedPath("path") String path);
	}
	
	
	
	

	public CommentService() {
		super("RedditPostCommentService");
	}

	public CommentService(Context c) {
		super("RedditPostCommentService", c); 
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SYNC)) 
		{
			mPostId = intent.getStringExtra(Extras.ARG_POST_ID); 
			mPermalink = intent.getStringExtra(Extras.ARG_PERMALINK); 
			
			RedditPostCommentClient client = CommentServiceClient.getInstance().getClient(getContext(), RedditPostCommentClient.class);
			
			HashMap<String, RemoteRedditPostComment> mCommentMap = new HashMap<String, RemoteRedditPostComment>();
			
			try {
				String encodedValue = URLEncoder.encode(String.valueOf(mPermalink.toLowerCase() + "search.json"), "UTF-8");
				encodedValue = encodedValue.replace("+", "%20");
				String uploadUrlEncoded = encodedValue.replace("%2F", "/");
				
				List<RemoteResponse> response = client.getComments( uploadUrlEncoded.substring(1));
				
				if( response.size() > 0 )
				{
					Pattern pattern1 = Pattern.compile("href=\"[^\"]+?imgur.com[^\"]+?\"");
					Pattern pattern2 = Pattern.compile("href=\"[^\"]+?dressed.so[^\"]+?\"");
					Pattern pattern3 = Pattern.compile("href=\"[^\"]+?drsd.so[^\"]+?\""); 
					
					List<RemoteRedditPostComment> comments = response.get(1).data.children;
							
					
					Iterator<RemoteRedditPostComment> iter = comments.iterator();
					while (iter.hasNext()) {
						RemoteRedditPostComment comment = iter.next();
						
						// set parent
						comment.data.postId = mPostId;
						
						if( comment.data.body_html != null )
							comment.data.body_html = Html.fromHtml(comment.data.body_html).toString();
						
						String bodyHtml = comment.data.body_html;
						
						if( bodyHtml != null )
						{
							Matcher matcher1 = pattern1.matcher(bodyHtml);
							Matcher matcher2 = pattern2.matcher(bodyHtml);
							Matcher matcher3 = pattern3.matcher(bodyHtml);
							
							boolean one = matcher1.find();
							boolean two = matcher2.find();
							boolean three = matcher3.find();
									
							
							if (!one && !two && !three)  {
								iter.remove();
								continue;
							}
						}
						else
						{
							iter.remove(); 
							continue;	
						}
						
						mCommentMap.put(comment.getIdentifier(), comment);
					}
					
					    
					if (comments != null && comments.size() > 0) {
						// synchronize!
						Cursor localCommentCursor = getContext().getContentResolver().query(Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.POST_ID + "=?", new String[]{mPostId}, null);
						localCommentCursor.moveToFirst();
						synchronizeCommentRecords(comments, localCommentCursor, localCommentCursor.getColumnIndex(CommentTable.IDENTIFIER), new CommentSynchronizer(getContext()), new CommentPreprocessor());
						localCommentCursor.close();
						
						// Get images
						List<RemoteImage> images = new ArrayList<RemoteImage>();
						localCommentCursor = getContext().getContentResolver().query(Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.POST_ID + "=?", new String[]{mPostId}, null);
						
						for (localCommentCursor.moveToFirst(); !localCommentCursor.isAfterLast(); localCommentCursor.moveToNext()) {
						    String commentId = localCommentCursor.getString(localCommentCursor.getColumnIndex(CommentTable.ID));
						    
						    
						    
						    // GET IMAGES
						    
						    String bodyHtml = localCommentCursor.getString(localCommentCursor.getColumnIndex(CommentTable.BODY_HTML));
						    
						    if( bodyHtml != null )
							{
								Pattern pattern = Pattern.compile("href=\"(.*?)\"");
								Matcher matcher = pattern.matcher(bodyHtml);
								
								String url = "";
								
									
								while (matcher.find()) {
									url = matcher.group(1);
									
									if( url.contains("imgur.com"))
									{
										url = url.replace("gallery/", "");
										
										if( !url.contains("i.imgur.com"))
										{
											if( url.contains("imgur.com/a/"))
											{
												ImgurClient imgurClient = ImgurServiceClient.getInstance().getClient(getContext(), ImgurClient.class);  
												
												String albumId = url.split("/a/")[1];
												RemoteImgurResponse imgurResponse = imgurClient.getAlbum( "Client-ID " + "e52e554e5972395", albumId);  
												RemoteImgurAlbum imgurAlbum = imgurResponse.data;
												List<RemoteImgurAlbumImage> imgs = imgurAlbum.images;
												
												for( RemoteImgurAlbumImage img : imgs)
												{
													url = img.link;
													
													url = url.replace("imgur", "i.imgur");
													url += "m.jpg";
													
													RemoteImage image = new RemoteImage();
													image.url = url;
													image.postId = mPostId;
													image.commentId = commentId;
													images.add(image);
												}
											
											}
											else
											{
												url = url.replace("imgur", "i.imgur");
												url += "m.jpg";
												RemoteImage image = new RemoteImage();
												image.url = url;
												image.postId = mPostId;
												image.commentId = commentId;
												images.add(image);
											}
										}
										else
										{
											if( !url.contains(".jpg"))
												url += "m.jpg";
											else
											{
												url = url.replace("s.jpg", ".jpg");
												url = url.replace("l.jpg", ".jpg");
												
												url = url.replace(".jpg", "m.jpg");
											}
											
											RemoteImage image = new RemoteImage();
											image.url = url;
											image.postId = mPostId;
											image.commentId = commentId;
											images.add(image); 
											
										}
									}
									
									else if( url.contains("drsd.so") || url.contains("dressed.so"))
									{
										if( url.contains("drsd.so") )
										{
											URL u;
											try {
												u = new URL(url);
												HttpURLConnection ucon = (HttpURLConnection) u.openConnection();
												ucon.setInstanceFollowRedirects(false);
												URL secondURL = new URL(ucon.getHeaderField("Location"));
												
												url = secondURL.toString(); 
												
												
												
											} catch (MalformedURLException e) {
												// TODO Auto-generated catch block
												Log.d("exc", url);  
												e.printStackTrace();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												Log.d("exc", url);  
												e.printStackTrace();
											}
										}
										
										if( !url.contains("cdn.dressed.so") )
										{
											url = url.replace("dressed.so/post/view", "cdn.dressed.so/i");
											

											
											url += "m.jpg";  
										}
										
										RemoteImage image = new RemoteImage(); 
										image.url = url;
										image.postId = mPostId;
										image.commentId = commentId;
										images.add(image);
									}
									
								
								}
									
							}
						    
						    
						    // GET SUBCOMMENTS
						    RemoteRedditPostComment comment = mCommentMap.get(localCommentCursor.getString(localCommentCursor.getColumnIndex(CommentTable.IDENTIFIER)));
						    
						    if( comment.data.replies != null )
						    {
						    	List<RemoteRedditPostCommentDataRepliesDataChild> replies = comment.data.replies.data.children;
						    	List<RemoteRedditPostCommentDataRepliesDataChild> repliesChildren = new ArrayList<RemoteRedditPostCommentDataRepliesDataChild>();
						    	
						    	if (replies != null && replies.size() > 0) {
						    		
						    		Iterator<RemoteRedditPostCommentDataRepliesDataChild> replyIter = replies.iterator();
									while (replyIter.hasNext()) {
										RemoteRedditPostCommentDataRepliesDataChild reply = replyIter.next();
										
										// set parent
										reply.data.parentReplyIdentifier = null;
										reply.data.postId = mPostId;
										reply.data.commentId = commentId;
										
										if( reply.data.body_html != null )
											reply.data.body_html = Html.fromHtml(reply.data.body_html).toString();
										
										// recursive
										repliesChildren = getReplies(commentId, reply, repliesChildren);
										
									}
									
									
									// synchronize!
									Cursor localCommentSubcommentCursor = getContext().getContentResolver().query(Provider.REPLY_CONTENT_URI, ReplyTable.ALL_COLUMNS, ReplyTable.POST_ID + "=? AND " + ReplyTable.COMMENT_ID + "=?", new String[]{mPostId, commentId}, null);
									localCommentSubcommentCursor.moveToFirst();
									synchronizeCommentSubcommentRecords(replies, localCommentSubcommentCursor, localCommentSubcommentCursor.getColumnIndex(ReplyTable.IDENTIFIER), new ReplySynchronizer(getContext()), new ReplyPreprocessor());
									localCommentSubcommentCursor.close();
						    		
						    		

						    	}
						    	
						    }
							
						} 
						localCommentCursor.close(); 
						
						if (images != null && images.size() > 0) {
							Cursor localImageCursor = getContext().getContentResolver().query(Provider.IMAGE_CONTENT_URI, ImageTable.ALL_COLUMNS, ImageTable.POST_ID + "=?", new String[] {mPostId}, null);
							localImageCursor.moveToFirst();
							synchronizeImageRecords(images, localImageCursor, localImageCursor.getColumnIndex(ImageTable.IDENTIFIER), new ImageSynchronizer(getContext()), new ImagePreprocessor());
							localImageCursor.close(); 
						}
					}
					else {
//					EventBus.getDefault().post(new RecordingServiceEvent(false, "There where no representatives for this zip code"));
					}
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	private List<RemoteRedditPostCommentDataRepliesDataChild> getReplies( String commentId, RemoteRedditPostCommentDataRepliesDataChild parent, List<RemoteRedditPostCommentDataRepliesDataChild> container )
	{
		if( parent.data.replies != null )    
		{
			List<RemoteRedditPostCommentDataRepliesDataChild> replies = parent.data.replies.data.children;
			
			container.addAll(replies);
			
			for( int i=0; i<replies.size(); ++i )
			{
				// set parent
				replies.get(i).data.parentReplyIdentifier = null;
				replies.get(i).data.postId = mPostId;
				replies.get(i).data.commentId = commentId;
				replies.get(i).data.parentReplyIdentifier = parent.getIdentifier();
				
				if( replies.get(i).data.body_html != null )
					replies.get(i).data.body_html = Html.fromHtml(replies.get(i).data.body_html).toString();
				
				getReplies( commentId, replies.get(i), container );
			}
		}
		
		return container;
		
	}


	public void synchronizeCommentRecords(List<RemoteRedditPostComment> remoteReps, Cursor localReps, int remoteIdentifierColumn, Synchronizer<RemoteRedditPostComment> synchronizer, RemotePreProcessor<RemoteRedditPostComment> preProcessor) {
		preProcessor.preProcessRemoteRecords(remoteReps);
		synchronizer.synchronize(getContext(), remoteReps, localReps, remoteIdentifierColumn);
	}
	
	public void synchronizeImageRecords(List<RemoteImage> remoteReps, Cursor localReps, int remoteIdentifierColumn, Synchronizer<RemoteImage> synchronizer, RemotePreProcessor<RemoteImage> preProcessor) {
		preProcessor.preProcessRemoteRecords(remoteReps);
		synchronizer.synchronize(getContext(), remoteReps, localReps, remoteIdentifierColumn);
	}
	
	public void synchronizeCommentSubcommentRecords(List<RemoteRedditPostCommentDataRepliesDataChild> remoteReps, Cursor localReps, int remoteIdentifierColumn, Synchronizer<RemoteRedditPostCommentDataRepliesDataChild> synchronizer, RemotePreProcessor<RemoteRedditPostCommentDataRepliesDataChild> preProcessor) {
		preProcessor.preProcessRemoteRecords(remoteReps);
		synchronizer.synchronize(getContext(), remoteReps, localReps, remoteIdentifierColumn);
	}

}