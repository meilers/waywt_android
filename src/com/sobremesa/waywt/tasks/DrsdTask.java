package com.sobremesa.waywt.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import android.os.AsyncTask;
import android.util.Log;

public class DrsdTask  extends AsyncTask<String, Long, String>
implements PropertyChangeListener {

    private static HashMap<CommentFragment, AsyncTask<?, ?, ?>> mCurrentDownloadCommentsTasks = new HashMap<CommentFragment, AsyncTask<?, ?, ?>>();
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
	
	
    public DrsdTask( CommentFragment fragment )
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
	protected String doInBackground(String... params) {
		
		String url = params[0];
		try {
			URL u;
			u = new URL(url);
			HttpURLConnection ucon = (HttpURLConnection) u.openConnection();
			ucon.setInstanceFollowRedirects(false);
			URL secondURL = new URL(ucon.getHeaderField("Location"));
			
			url = secondURL.toString(); 
			
			url = url.replace("dressed.so/post/view", "cdn.dressed.so/i");
			
			url += "m.jpg"; 
			
			return url;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			Log.d("exc", url);  
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("exc", url);  
			e.printStackTrace();
		}
		
		return null;
	}
    
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		
		List<String> urls = new ArrayList<String>();
		urls.add(result != null ? result : "");
		
		if( mFragment != null )
			mFragment.addImageUrls(urls);
		
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
