package com.sobremesa.waywt.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import android.os.AsyncTask;

public class ImgurAlbumTask  extends AsyncTask<String, Long, List<String>>
implements PropertyChangeListener {

    private static HashMap<CommentFragment, AsyncTask<?, ?, ?>> mCurrentDownloadCommentsTasks = new HashMap<CommentFragment, AsyncTask<?, ?, ?>>();
    
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
    }
    
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		Iterator<Map.Entry<CommentFragment, AsyncTask<?, ?, ?>>> it = mCurrentDownloadCommentsTasks.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<CommentFragment, AsyncTask<?, ?, ?>> pairs = (Map.Entry<CommentFragment, AsyncTask<?, ?, ?>>)it.next();
	        if( pairs.getKey() == null || pairs.getValue() == null)
	        {
	        	if( pairs.getValue() != null )
	        		pairs.getValue().cancel(true);

	        	CommentsListener listener = pairs.getKey();
	        	AsyncTask<?, ?, ?> task = pairs.getValue();
	        	
	        	listener = null;
	        	task = null;
	        	
	        	it.remove(); // avoids a ConcurrentModificationException	
	        	

	        }
	    }
	    
	    if( mCurrentDownloadCommentsTasks.containsKey(mFragment) )
	    {
	    	mCurrentDownloadCommentsTasks.get(mFragment).cancel(true); 
	    	mCurrentDownloadCommentsTasks.put(mFragment, this);
	    }
	    else
	    {
	    	mCurrentDownloadCommentsTasks.put(mFragment, this);
	    }
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
		
		if( mCurrentDownloadCommentsTasks.containsKey(mFragment) ) {
			mCurrentDownloadCommentsTasks.remove(mFragment);
		}
		mFragment = null;
	}
	
	
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		publishProgress((Long) event.getNewValue());
	}
}
