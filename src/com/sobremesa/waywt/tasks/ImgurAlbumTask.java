package com.sobremesa.waywt.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;

import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.fragments.CommentFragment;
import com.sobremesa.waywt.listeners.CommentsListener;
import com.sobremesa.waywt.service.clients.ImgurServiceClient;
import com.sobremesa.waywt.tasks.DrsdTask.ListenerTask;

import android.os.AsyncTask;

public class ImgurAlbumTask  extends AsyncTask<String, Long, List<String>>
implements PropertyChangeListener {
	
	public static class ListenerTask
	{
		public AsyncTask<?, ?, ?> mCurrentDownloadCommentsTask = null;
		public WeakReference<CommentFragment> mListenerReference;
	}
	
	private static ListenerTask[] mTasks = new ListenerTask[3];
	private static int mInc = 0;

	public int mIndex = 0;
	
	
    private CommentFragment mFragment;
    
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
	
	public interface ImgurClient {
		@GET("/3/album/{path}")
		RemoteImgurResponse getAlbum(@Header("Authorization") String auth, @EncodedPath("path") String path);
	}
	
	
    public ImgurAlbumTask( CommentFragment fragment )
    {
    	this.mFragment = fragment;
    	
		ListenerTask task = mTasks[mInc];
		
		if( task != null )
		{
			if( task.mCurrentDownloadCommentsTask != null )
			{
				task.mCurrentDownloadCommentsTask.cancel(true);
				task.mCurrentDownloadCommentsTask = null;
				
				task.mListenerReference.clear();
				task.mListenerReference = null;
			}
		}
		
		mTasks[mInc] = new ListenerTask();
		
		mTasks[mInc].mCurrentDownloadCommentsTask = this;
		mTasks[mInc].mListenerReference = new WeakReference<CommentFragment>(fragment);
		
		if( mInc < 2)
			++mInc;
		else
			mInc = 0;
    }
    
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	
	@Override
	protected List<String> doInBackground(String... params) {
		ImgurClient imgurClient = ImgurServiceClient.getInstance().getClient(WaywtApplication.getContext(), ImgurClient.class);  
		
		String url = params[0];
		
		String albumId = url.split("/a/")[1];
		RemoteImgurResponse imgurResponse = imgurClient.getAlbum( "Client-ID " + "e52e554e5972395", albumId);  
		RemoteImgurAlbum imgurAlbum = imgurResponse.data;
		List<RemoteImgurAlbumImage> imgs = imgurAlbum.images;
		
		List<String> imageUrls = new ArrayList<String>();
		
		for( RemoteImgurAlbumImage img : imgs)
		{
			url = img.link;
			
			url = url.replace("imgur", "i.imgur");
			url += "m.jpg";
			
			imageUrls.add(url);
		}
		
		return imageUrls;
	}
    
	
	@Override
	protected void onPostExecute(List<String> result) {
		super.onPostExecute(result);

		
		if( mFragment != null )
			mFragment.addImageUrls(result);
	}
	
	
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		publishProgress((Long) event.getNewValue());
	}
}
