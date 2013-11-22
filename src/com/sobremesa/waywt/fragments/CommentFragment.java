package com.sobremesa.waywt.fragments;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.activities.ImageActivity;
import com.sobremesa.waywt.activities.MainActivity;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.common.CacheInfo;
import com.sobremesa.waywt.common.Common;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.common.RedditIsFunHttpClientFactory;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.listeners.LoginListener;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.service.clients.ImgurServiceClient;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.tasks.DrsdTask;
import com.sobremesa.waywt.tasks.ImgurAlbumTask;
import com.sobremesa.waywt.tasks.VoteTask;
import com.sobremesa.waywt.util.CollectionUtils;
import com.sobremesa.waywt.util.Util;
import com.sobremesa.waywt.views.AspectRatioImageView;
import com.sobremesa.waywt.views.WaywtSecondaryTextView;
import com.sobremesa.waywt.views.WaywtTextView;
import com.xtremelabs.imageutils.ImageLoader;
import com.xtremelabs.imageutils.ImageLoaderListener;
import com.xtremelabs.imageutils.ImageReturnedFrom;
import com.xtremelabs.imageutils.ImageLoader.Options;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CommentFragment extends Fragment implements View.OnCreateContextMenuListener
{
	private static final String TAG = CommentFragment.class.getSimpleName();
	
	public static class Extras
	{
		public static String ARG_COMMENT = "comment";
	}
	private ThingInfo mComment;
	private ArrayList<String> mImageUrls;
	
	private List<String> mDressedUrls;
	private List<String> mImgurAlbumUrls;
	
	private ImageLoader mImageLoader;
	private AspectRatioImageView mMainIv;
	
	private WaywtSecondaryTextView mTitleTv;
	private TextView mPointsTv;
	
    private final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
    private final RedditSettings mSettings = new RedditSettings();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mSettings.loadRedditPreferences(getActivity(), null);
		
		mComment = (ThingInfo)getArguments().get(Extras.ARG_COMMENT);
		mImageUrls = new ArrayList<String>();
		mDressedUrls = new ArrayList<String>();
		mImgurAlbumUrls = new ArrayList<String>();
		
		String bodyHtml = Html.fromHtml(mComment.getBody_html()).toString();
		
	    if( bodyHtml != null )
		{
			Pattern pattern = Pattern.compile("href=\"(.*?)\"");
			Matcher matcher = pattern.matcher(bodyHtml);
			
			String url = "";
			
				
			while (matcher.find()) {
				url = matcher.group(1);
				
				Log.d(mComment.getAuthor(), bodyHtml);
				
				if( url.contains("imgur.com"))
				{
					url = url.replace("gallery/", "");
					
					if( !url.contains("i.imgur.com"))
					{
						if( url.contains("imgur.com/a/"))
						{
							mImgurAlbumUrls.add(url);
						
						}
						else
						{
							url = url.replace("imgur", "i.imgur");
							url += ".jpg";
							
							if( !mImageUrls.contains( url ))
								mImageUrls.add(url);
						}
					}
					else
					{
						if( !url.contains(".jpg"))
							url += ".jpg";
						
						if( !mImageUrls.contains( url ))
							mImageUrls.add(url);
						
					}
				}
				
				else if( url.contains("drsd.so") || url.contains("dressed.so"))
				{
					if( url.contains("drsd.so") )
					{
						mDressedUrls.add(url);
						
					}
					else
					{
						if( !url.contains("cdn.dressed.so") )
						{
							url = url.replace("dressed.so/post/view", "cdn.dressed.so/i");
							
	
							
							url += "m.jpg";  
						}
						
						if( !mImageUrls.contains( url ))
							mImageUrls.add(url);
					}
				}
				
			
			}
				
		}
	    
		
		
		Options options = new Options();
		options.scalingPreference = Options.ScalingPreference.ROUND_TO_CLOSEST_MATCH;
		mImageLoader = ImageLoader.buildImageLoaderForSupportFragment(this);
		mImageLoader.setDefaultOptions(options);

	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mSettings.loadRedditPreferences(getActivity(), mClient);
	}
	
	
	private OnClickListener mArrowUpListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
	    	getActivity().removeDialog(Constants.DIALOG_COMMENT_CLICK);
	    	String thingFullname = mComment.getName();
			if (mComment.getLikes() != null && mComment.getLikes())
				new VoteTask(mComment.getName(), 0).execute();
			else
				new VoteTask(thingFullname, 1).execute();
		}
	};
	
	private OnClickListener mArrowDownListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
	    	getActivity().removeDialog(Constants.DIALOG_COMMENT_CLICK);
	    	String thingFullname = mComment.getName();
			if (mComment.getLikes() != null && !mComment.getLikes())
				new VoteTask(mComment.getName(), 0).execute();
			else
				new VoteTask(thingFullname, -1).execute();
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.fragment_comment, null, false);
		
		
		mMainIv = (AspectRatioImageView)view.findViewById(R.id.comment_image_iv);
		mPointsTv = (TextView)view.findViewById(R.id.comment_points_tv);
		mTitleTv = (WaywtSecondaryTextView)view.findViewById(R.id.comment_title_tv);
		
	    
		ImageView arrowUpIv = (ImageView)view.findViewById(R.id.comment_arrow_up_iv);
		arrowUpIv.setOnClickListener(mArrowUpListener);
		
		ImageView arrowDownIv = (ImageView)view.findViewById(R.id.comment_arrow_down_iv);	
		arrowDownIv.setOnClickListener(mArrowDownListener);
		
		//Points
		updatePoints(view);
		
		// Text
//		String bodyHtml =  mComment.getBody_html();
//		mTitleTv.setText(Html.fromHtml(Html.fromHtml(bodyHtml).toString()));
//		mTitleTv.setTypeface(FontManager.INSTANCE.getGeorgiaFont(), Typeface.ITALIC);
//		
//		mTitleTv.setMovementMethod (LinkMovementMethod.getInstance());
//		mTitleTv.setClickable(true);
		
		
    	if (mComment.getSpannedBody() != null)
    		mTitleTv.setText(mComment.getSpannedBody());
		else
			mTitleTv.setText(mComment.getBody());
    
		
		
		
		// Replies fragment
		RepliesFragment fragment = new RepliesFragment();
		Bundle args = new Bundle();
		args.putParcelable(RepliesFragment.Extras.ARG_COMMENT, mComment);
		fragment.setArguments(args);
		
		getChildFragmentManager().beginTransaction().replace(R.id.comment_replies_container, fragment, RepliesFragment.class.getCanonicalName()).commit();
		
		if(mImgurAlbumUrls.isEmpty() && mDressedUrls.isEmpty())
			updateImages(view);
		else
		{
			Iterator<String> i = mImgurAlbumUrls.iterator();
			while (i.hasNext()) {
			   new ImgurAlbumTask(this).execute(i.next());
			   i.remove();
			}
			
			i = mDressedUrls.iterator();
			while (i.hasNext()) {
			   new DrsdTask(this).execute(i.next());
			   i.remove();
			}
		}
		
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mImageLoader.destroy();
		
		super.onDestroy();
	}

	
	private void updatePoints( final View view )
	{
		// Points
		int ups = Integer.valueOf(mComment.getUps()); 
		int downs = Integer.valueOf(mComment.getDowns());
		
		mPointsTv.setText((ups-downs)  + "");
		
		

	}
	
	private void updateImages( final View view )
	{
		if( mImageUrls.size() > 0 )
		{
			final String mainImageUrl = mImageUrls.get(0);
			
			mImageLoader.loadImage(mMainIv, mainImageUrl, new ImageLoaderListener() {
				@Override
				public void onImageLoadError(String arg0) { 
					
					Log.d("yes", mainImageUrl);
					
					ScrollView sv = (ScrollView)view.findViewById(R.id.container);
					sv.setVisibility(View.VISIBLE);
					
					Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
					sv.startAnimation(myFadeInAnimation);
				}
				
				@Override
				public void onImageAvailable(ImageView imageView, Bitmap bitmap, ImageReturnedFrom imageReturnedFrom) {
					
					// bitmap = getResizedBitmap(bitmap, 200);
					
					imageView.setImageBitmap(bitmap);
					
					ScrollView sv = (ScrollView)view.findViewById(R.id.container);
					sv.setVisibility(View.VISIBLE);
					
					Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
					sv.startAnimation(myFadeInAnimation);
				}
			});
			
			LinearLayout imagesLayout = (LinearLayout)view.findViewById(R.id.images_grid_layout);
			imagesLayout.removeAllViews();
			
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int width = size.x;
			LinearLayout innerLayout = new LinearLayout(getActivity());
			
			for( int i = 1; i < mImageUrls.size(); ++i )
			{
				final String imageUrl = mImageUrls.get(i);
				
				
				if( (i & 1) == 1 )
				{
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width/2);
					params.gravity = Gravity.LEFT;
					
					innerLayout = new LinearLayout(getActivity());
					innerLayout.setLayoutParams(params);
					innerLayout.setOrientation(LinearLayout.HORIZONTAL);
				}
				
				ImageView iv = new ImageView(getActivity());
				iv.setLayoutParams(new LinearLayout.LayoutParams(width/2, width/2));
				iv.setScaleType(ScaleType.CENTER_CROP);
				
				mImageLoader.loadImage(iv, imageUrl, new ImageLoaderListener() {
					@Override
					public void onImageLoadError(String arg0) {
		
						Log.d("no", imageUrl);
					}
					
					@Override
					public void onImageAvailable(ImageView imageView, Bitmap bitmap, ImageReturnedFrom imageReturnedFrom) {
						
						// bitmap = getResizedBitmap(bitmap, 200);
						
						imageView.setImageBitmap(bitmap); 
//							if (imageReturnedFrom != ImageReturnedFrom.MEMORY) {
//								
//								if (getActivity() != null) {
//									
//									Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
//									imageView.startAnimation(myFadeInAnimation);
//								}
//							}
					}
				});
				
				final int position = i;
				
				iv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startImagesActivity(position);
					}
				});
				
				innerLayout.addView(iv);
	
				
				if( (i & 1) == 0  || i == mImageUrls.size()-1 )
					imagesLayout.addView(innerLayout);
			}
			
		}
	}
	
	
	public synchronized void addImageUrls( List<String> urls)
	{
		for( String url: urls )
		{
			if(!mImageUrls.contains(url))
				mImageUrls.add(url);
		}
		
		if( getView() != null )
			updateImages(getView());
	}
	
	private void startImagesActivity(int position)
	{
		Intent intent = new Intent(getActivity(), ImageActivity.class);
		Bundle extras = new Bundle();
		extras.putString(ImageActivity.Extras.ARG_AUTHOR, mComment.getAuthor());
		extras.putStringArrayList(ImageActivity.Extras.ARG_IMAGE_URLS, mImageUrls);
		extras.putInt(ImageActivity.Extras.ARG_IMAGE_SELECTED_POSITION, position);
		intent.putExtras(extras);
		startActivity(intent);
	}

	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getActivity();
		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			// some code
		}
	}


    private class VoteTask extends AsyncTask<Void, Void, Boolean> {
    	
    	private static final String TAG = "VoteWorker";
    	
    	private String _mThingFullname;
    	private int _mDirection;
    	private String _mUserError = "Error voting.";
    	private ThingInfo _mTargetThingInfo;
    	
    	// Save the previous arrow and score in case we need to revert
    	private int _mPreviousUps, _mPreviousDowns;
    	private Boolean _mPreviousLikes;
    	
    	VoteTask(String thingFullname, int direction) {
    		_mThingFullname = thingFullname;
    		_mDirection = direction;
    		// Copy these because they can change while voting thread is running
    		_mTargetThingInfo = mComment;
    	}
    	
    	@Override
    	public Boolean doInBackground(Void... v) {
        	HttpEntity entity = null;
        	
        	if (!mSettings.isLoggedIn()) {
        		_mUserError = "You must be logged in to vote.";
        		return false;
        	}
        	
        	// Update the modhash if necessary
        	if (mSettings.getModhash() == null) {
        		String modhash = Common.doUpdateModhash(mClient); 
        		if (modhash == null) {
        			// doUpdateModhash should have given an error about credentials
        			Common.doLogout(mSettings, mClient, WaywtApplication.getContext());
        			if (Constants.LOGGING) Log.e(TAG, "Vote failed because doUpdateModhash() failed");
        			return false;
        		}
        		mSettings.setModhash(modhash);
        	}
        	
        	try {
        		// Construct data
    			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    			nvps.add(new BasicNameValuePair("id", _mThingFullname.toString()));
    			nvps.add(new BasicNameValuePair("dir", String.valueOf(_mDirection)));
    			nvps.add(new BasicNameValuePair("r", "malefashionadvice"));
    			nvps.add(new BasicNameValuePair("uh", mSettings.getModhash().toString()));
    			// Votehash is currently unused by reddit 
//    				nvps.add(new BasicNameValuePair("vh", "0d4ab0ffd56ad0f66841c15609e9a45aeec6b015"));
    			
    			HttpPost httppost = new HttpPost(Constants.REDDIT_BASE_URL + "/api/vote");
    	        httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    	        
    	        if (Constants.LOGGING) Log.d(TAG, nvps.toString());
    	        
                // Perform the HTTP POST request
    	    	HttpResponse response = mClient.execute(httppost);
            	entity = response.getEntity();

            	String error = Common.checkResponseErrors(response, entity);
            	if (error != null)
            		throw new Exception(error);

            	return true;
        	} catch (Exception e) {
        		if (Constants.LOGGING) Log.e(TAG, "VoteTask", e);
        		_mUserError = e.getMessage();
        	} finally {
        		if (entity != null) {
        			try {
        				entity.consumeContent();
        			} catch (Exception e2) {
        				if (Constants.LOGGING) Log.e(TAG, "entity.consumeContent()", e2);
        			}
        		}
        	}
        	return false;
        }
    	
    	public void onPreExecute() {
        	if (!mSettings.isLoggedIn()) {
        		Common.showErrorToast("You must be logged in to vote.", Toast.LENGTH_LONG, CommentFragment.this.getActivity());
        		cancel(true);
        		return;
        	}
        	if (_mDirection < -1 || _mDirection > 1) {
        		if (Constants.LOGGING) Log.e(TAG, "WTF: _mDirection = " + _mDirection);
        		throw new RuntimeException("How the hell did you vote something besides -1, 0, or 1?");
        	}

    		int newUps, newDowns;
        	Boolean newLikes;
        	_mPreviousUps = Integer.valueOf(_mTargetThingInfo.getUps());
        	_mPreviousDowns = Integer.valueOf(_mTargetThingInfo.getDowns());
    	    newUps = _mPreviousUps;
    	    newDowns = _mPreviousDowns;
    	    _mPreviousLikes = _mTargetThingInfo.getLikes();
        	
    	    if (_mPreviousLikes == null) {
	    		if (_mDirection == 1) {
	    			newUps = _mPreviousUps + 1;
	    			newLikes = true;
	    		} else if (_mDirection == -1) {
	    			newDowns = _mPreviousDowns + 1;
	    			newLikes = false;
	    		} else {
	    			cancel(true);
	    			return;
	    		}
    	    } else if (_mPreviousLikes == true) {
    	    	if (_mDirection == 0) {
	    			newUps = _mPreviousUps - 1;
	    			newLikes = null;
	    		} else if (_mDirection == -1) {
	    			newUps = _mPreviousUps - 1;
	    			newDowns = _mPreviousDowns + 1;
	    			newLikes = false;
	    		} else {
	    			cancel(true);
	    			return;
	    		}
	    	} else {
	    		if (_mDirection == 1) {
	    			newUps = _mPreviousUps + 1;
	    			newDowns = _mPreviousDowns - 1;
	    			newLikes = true;
	    		} else if (_mDirection == 0) {
	    			newDowns = _mPreviousDowns - 1;
	    			newLikes = null;
	    		} else {
	    			cancel(true);
	    			return;
	    		}
	    	}

    		_mTargetThingInfo.setLikes(newLikes);
    		_mTargetThingInfo.setUps(newUps);
    		_mTargetThingInfo.setDowns(newDowns);
    		_mTargetThingInfo.setScore(newUps - newDowns);
    		
    		if( getView() != null )
    			updatePoints(getView());
    	}
    	
    	public void onPostExecute(Boolean success) {
    		if (success) {
    			CacheInfo.invalidateCachedThread(WaywtApplication.getContext());
    		} else {
    			// Vote failed. Undo the arrow and score.
            	_mTargetThingInfo.setLikes(_mPreviousLikes);
       			_mTargetThingInfo.setUps(_mPreviousUps);
       			_mTargetThingInfo.setDowns(_mPreviousDowns);
       			_mTargetThingInfo.setScore(_mPreviousUps - _mPreviousDowns);

        		if( getView() != null )
        			updatePoints(getView());
        		
    			Common.showErrorToast(_mUserError, Toast.LENGTH_LONG, CommentFragment.this.getActivity());
    		}
    	}
    }
    
    

}
