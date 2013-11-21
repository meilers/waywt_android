package com.sobremesa.waywt.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import retrofit.http.EncodedPath;
import retrofit.http.GET;
import retrofit.http.Header;

import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.fragments.CommentFragment;
import com.sobremesa.waywt.service.clients.ImgurServiceClient;

import android.os.AsyncTask;

public class ImgurAlbumTask  extends AsyncTask<String, Long, List<String>>
implements PropertyChangeListener {

    private static AsyncTask<?, ?, ?> mCurrentDownloadCommentsTask = null;
    private static final Object mCurrentDownloadCommentsTaskLock = new Object();
    
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
		
		synchronized (mCurrentDownloadCommentsTaskLock) {
    		if (mCurrentDownloadCommentsTask != null) {
    			this.cancel(true);
    			return;
    		}
    		mCurrentDownloadCommentsTask = this;
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
		
		
		synchronized (mCurrentDownloadCommentsTaskLock) {
			mCurrentDownloadCommentsTask = null;
		}
		
		if( mFragment != null )
			mFragment.addImageUrls(result);
	}
	
	
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		publishProgress((Long) event.getNewValue());
	}
}
