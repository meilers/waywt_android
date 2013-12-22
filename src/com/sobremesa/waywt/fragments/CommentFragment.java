package com.sobremesa.waywt.fragments;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
import com.sobremesa.waywt.listeners.CommentsListener;
import com.sobremesa.waywt.listeners.RepliesListener;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.service.clients.ImgurServiceClient;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.tasks.DownloadRepliesTask;
import com.sobremesa.waywt.tasks.DrsdTask;
import com.sobremesa.waywt.tasks.ImgurAlbumTask;
import com.sobremesa.waywt.tasks.VoteTask;
import com.sobremesa.waywt.util.CollectionUtils;
import com.sobremesa.waywt.util.StringUtils;
import com.sobremesa.waywt.util.UserUtil;
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
import android.content.res.Resources;
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
import android.support.v4.app.FragmentStatePagerAdapter;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CommentFragment extends Fragment implements View.OnCreateContextMenuListener, RepliesListener, OnItemClickListener
{
	private static final String TAG = CommentFragment.class.getSimpleName();
	
	public static class Extras
	{
		public static String SUBREDDIT = "subreddit";
		public static String THREAD_ID = "thread_id";
		
		public static String COMMENT = "comment";
	}
	
    private String mSubreddit = UserUtil.getSubreddit();
    private String mThreadId = null;
    
	private ThingInfo mComment;
	private ArrayList<String> mImageUrls;
	
	private List<String> mDressedUrls;
	private List<String> mImgurAlbumUrls;
	
	private ImageLoader mImageLoader;
	private AspectRatioImageView mMainIv;
	
	private TextView mPointsTv;
	private EditText mReplyEt;
	private ImageView mSendBtn;
	
	// ListView
	private ListView mListView;
	private RepliesListAdapter mRepliesAdapter = null;
	private ArrayList<ThingInfo> mRepliesList = new ArrayList<ThingInfo>();
    
	private View mHeaderListView;
	private int last_found_position = -1;
	
	
    private final HttpClient mRedditClient = WaywtApplication.getRedditClient();
    private final RedditSettings mRedditSettings = WaywtApplication.getRedditSettings();
    
    
    private DownloadRepliesTask getNewDownloadRepliesTask() {
    	return new DownloadRepliesTask(
				this,
				mSubreddit,
				mThreadId,
				mRedditSettings,
				mRedditClient
		);
    }
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mSubreddit = getArguments().getString(Extras.SUBREDDIT);
		mThreadId = getArguments().getString(Extras.THREAD_ID);
		
		mRedditSettings.loadRedditPreferences(getActivity(), null);
		
		mComment = (ThingInfo)getArguments().get(Extras.COMMENT);
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
				
				
				try
				{
					if( url.contains("imgur.com"))
					{
						url = url.replace("gallery/", "");
						
						if( !url.contains("i.imgur.com"))
						{
							if( url.contains(",") )
							{
								List<String> urls = new ArrayList<String>();
								
								String[] split = url.split("imgur.com/");
								String scheme = split[0];
								String path = split[1];
								String[] ids = path.split(",");
								
								for( String id : ids )
								{
									urls.add(scheme + "imgur.com/" + id + ".jpg");
								}
								
								for( String u : urls )
								{
									if( mComment.getAuthor().toLowerCase().equals("mydogisnoodles"))
										Log.d("noodles", u);
									
									if( !mImageUrls.contains( u ))
										mImageUrls.add(u);	
								}
							}
							else if( url.contains("imgur.com/a/"))
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
					else if( url.contains(".jpg") || url.contains(".jpeg"))
					{
						mImageUrls.add(url);
					}
				}
				catch(Exception e) {}
			
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
		
		mRedditSettings.loadRedditPreferences(getActivity(), mRedditClient);
		
		
		getNewDownloadRepliesTask().prepareLoadMoreComments(mComment.getId(), 0, mComment.getIndent()).execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
	}
	
    @Override
    public void onPause() {
    	super.onPause();
		CookieSyncManager.getInstance().stopSync();
		mRedditSettings.saveRedditPreferences(getActivity());
    }
    
    @Override
    public void onDestroyView() {
    	// TODO Auto-generated method stub
    	super.onDestroyView();
    	
    	unbindDrawables(getView().findViewById(R.id.vf));
    }
	
	private void unbindDrawables(View view)
	{
        if (view.getBackground() != null)
        {
                view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView))
        {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
                {
                        unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
        }
	}
	
	private OnClickListener mArrowUpListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mRedditSettings.saveRedditPreferences(WaywtApplication.getContext());
			
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
			
			mRedditSettings.saveRedditPreferences(WaywtApplication.getContext());
			
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
		
		
		// ListView
		mListView = (ListView)view.findViewById(R.id.replies_lv);
		mRepliesList = new ArrayList<ThingInfo>();
        mRepliesAdapter = new RepliesListAdapter(getActivity(), mRepliesList);
        
        View footerListView = inflater.inflate(R.layout.footer_comment, null);
		mHeaderListView = inflater.inflate(R.layout.header_comment, null);
		
		
		mMainIv = (AspectRatioImageView)mHeaderListView.findViewById(R.id.comment_image_iv);
		mPointsTv = (TextView)mHeaderListView.findViewById(R.id.comment_points_tv);
		
		ImageView arrowUpIv = (ImageView)mHeaderListView.findViewById(R.id.comment_arrow_up_iv);
		arrowUpIv.setOnClickListener(mArrowUpListener);
		
		ImageView arrowDownIv = (ImageView)mHeaderListView.findViewById(R.id.comment_arrow_down_iv);	
		arrowDownIv.setOnClickListener(mArrowDownListener);
		
		LinearLayout pointsLayout = (LinearLayout)mHeaderListView.findViewById(R.id.comment_points_layout);
		pointsLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		updatePoints(mHeaderListView);
		
		updateImages(mHeaderListView);
		
		if(mImgurAlbumUrls.isEmpty() && mDressedUrls.isEmpty())
			;//updateImages(mHeaderListView);
		else
		{
			Log.d("ici", mComment.getAuthor());
			
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
		mListView.addFooterView(footerListView);
		mListView.addHeaderView(mHeaderListView);
        mListView.setAdapter(mRepliesAdapter);
        mListView.setOnItemClickListener(this);
        
		
		// Reply
		mReplyEt = (EditText)view.findViewById(R.id.comment_reply_et);
		mSendBtn = (ImageView)view.findViewById(R.id.comment_reply_send_btn);
		mSendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// TODO Auto-generated method stub
				String kak = mReplyEt.getText().toString();
				Log.d("zut", kak);
				
				
				new CommentReplyTask(mComment.getName()).execute(mReplyEt.getText().toString());
			}
		});
		
		
		
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
		
		super.onDestroy();
		
		mImageLoader.destroy();
	}

	

	
	
	private void updatePoints( final View view )
	{
		// Points
		int ups = Integer.valueOf(mComment.getUps()); 
		int downs = Integer.valueOf(mComment.getDowns());
		
		ImageView arrowUpIv = (ImageView)view.findViewById(R.id.comment_arrow_up_iv);
		ImageView arrowDownIv = (ImageView)view.findViewById(R.id.comment_arrow_down_iv);
		Resources r = getActivity().getResources();
		
		if( mComment.getLikes() != null && mComment.getLikes() )
			arrowUpIv.setImageDrawable(r.getDrawable(R.drawable.arrow_up_orange));
		else
			arrowUpIv.setImageDrawable(r.getDrawable(R.drawable.arrow_up));
		
		if( mComment.getLikes() != null && !mComment.getLikes() )
			arrowDownIv.setImageDrawable(r.getDrawable(R.drawable.arrow_down_blue));
		else
			arrowDownIv.setImageDrawable(r.getDrawable(R.drawable.arrow_down));
		
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
					
					Log.d("fail", mainImageUrl);
					View parentView = CommentFragment.this.getView();
					
					if( parentView != null )
					{
						ViewFlipper vf = (ViewFlipper)parentView.findViewById(R.id.vf);
						vf.setDisplayedChild(1);
						
						ListView lv = (ListView)parentView.findViewById(R.id.replies_lv);
						lv.setVisibility(View.VISIBLE);
						
						Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
						lv.startAnimation(myFadeInAnimation);
					}
				}
				
				@Override
				public void onImageAvailable(ImageView imageView, Bitmap bitmap, ImageReturnedFrom imageReturnedFrom) {
					
					Log.d("succeed", mainImageUrl);
					View parentView = CommentFragment.this.getView();
					
					if( parentView != null )
					{
						imageView.setImageBitmap(bitmap);
					
						ViewFlipper vf = (ViewFlipper)parentView.findViewById(R.id.vf);
						vf.setDisplayedChild(1);
						
						ListView lv = (ListView)parentView.findViewById(R.id.replies_lv);
						lv.setVisibility(View.VISIBLE);
						
						Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
						lv.startAnimation(myFadeInAnimation);
					}
				}
			});
			
			mMainIv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startImagesActivity(0);
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
		else
		{
			View parentView = getView();
			
			if( parentView != null )
			{
				ViewFlipper vf = (ViewFlipper)parentView.findViewById(R.id.vf);
				vf.setDisplayedChild(1);
				
				ListView lv = (ListView)parentView.findViewById(R.id.replies_lv);
				lv.setVisibility(View.VISIBLE);
				
				Animation myFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
				lv.startAnimation(myFadeInAnimation);
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
        	
        	if (!mRedditSettings.isLoggedIn()) {
        		_mUserError = "You must be logged in to vote.";
        		return false;
        	}
        	
        	// Update the modhash if necessary
        	if (mRedditSettings.getModhash() == null) {
        		String modhash = Common.doUpdateModhash(mRedditClient); 
        		if (modhash == null) {
        			// doUpdateModhash should have given an error about credentials
        			Common.doLogout(mRedditSettings, mRedditClient, WaywtApplication.getContext());
        			if (Constants.LOGGING) Log.e(TAG, "Vote failed because doUpdateModhash() failed");
        			return false;
        		}
        		mRedditSettings.setModhash(modhash);
        	}
        	
        	try {
        		// Construct data
    			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    			nvps.add(new BasicNameValuePair("id", _mThingFullname.toString()));
    			nvps.add(new BasicNameValuePair("dir", String.valueOf(_mDirection)));
    			nvps.add(new BasicNameValuePair("r", UserUtil.getSubreddit()));
    			nvps.add(new BasicNameValuePair("uh", mRedditSettings.getModhash().toString()));
    			// Votehash is currently unused by reddit 
//    				nvps.add(new BasicNameValuePair("vh", "0d4ab0ffd56ad0f66841c15609e9a45aeec6b015"));
    			
    			HttpPost httppost = new HttpPost(Constants.REDDIT_BASE_URL + "/api/vote");
    	        httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    	        
    	        if (Constants.LOGGING) Log.d(TAG, nvps.toString());
    	        
                // Perform the HTTP POST request
    	    	HttpResponse response = mRedditClient.execute(httppost);
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
        	if (!mRedditSettings.isLoggedIn()) {
        		if( CommentFragment.this.getActivity() != null )
        		{
        			getActivity().showDialog(Constants.DIALOG_LOGIN);
        		}
//        			Common.showErrorToast("You must be logged in to vote.", Toast.LENGTH_LONG, CommentFragment.this.getActivity());
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
    		{
    			updatePoints(getView());
    		}
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
        		{
        			updatePoints(getView());
        		}
        			
//        		if( CommentFragment.this.getActivity() != null )
//        			Common.showErrorToast(_mUserError, Toast.LENGTH_LONG, CommentFragment.this.getActivity());
        		
        		getActivity().showDialog(Constants.DIALOG_LOGIN);
    		}
    	}
    }
    
    final class RepliesListAdapter extends ArrayAdapter<ThingInfo> {
    	public static final int OP_ITEM_VIEW_TYPE = 0;
    	public static final int COMMENT_ITEM_VIEW_TYPE = 1;
    	public static final int MORE_ITEM_VIEW_TYPE = 2;
    	public static final int HIDDEN_ITEM_HEAD_VIEW_TYPE = 3;
    	// The number of view types
    	public static final int VIEW_TYPE_COUNT = 4;
    	
    	public boolean mIsLoading = true;
    	
    	private LayoutInflater mInflater;
        private int mFrequentSeparatorPos = ListView.INVALID_POSITION;
        
        public RepliesListAdapter(Context context, List<ThingInfo> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getItemViewType(int position) {
        	if (position == mFrequentSeparatorPos) {
                // We don't want the separator view to be recycled.
                return IGNORE_ITEM_VIEW_TYPE;
            }
        	
            ThingInfo item = getItem(position);
            if (item.isHiddenCommentDescendant())
            	return IGNORE_ITEM_VIEW_TYPE;
            if (item.isHiddenCommentHead())
            	return HIDDEN_ITEM_HEAD_VIEW_TYPE;
            if (item.isLoadMoreCommentsPlaceholder())
            	return MORE_ITEM_VIEW_TYPE;
            
            return COMMENT_ITEM_VIEW_TYPE;
        }
        
        @Override
        public int getViewTypeCount() {
        	return VIEW_TYPE_COUNT;
        }
        
        @Override
        public boolean isEmpty() {
        	if (mIsLoading)
        		return false;
        	return super.isEmpty();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            
            ThingInfo item = this.getItem(position);
            
            try {
	            if (isHiddenCommentDescendantPosition(position)) { 
	            	if (view == null) {
	            		// Doesn't matter which view we inflate since it's gonna be invisible
	            		view = mInflater.inflate(R.layout.zero_size_layout, null);
	            	}
	            } else if (isHiddenCommentHeadPosition(position)) {
	            	if (view == null) {
	            		view = mInflater.inflate(R.layout.comments_list_item_hidden, null);
	            	}
	            	TextView votesView = (TextView) view.findViewById(R.id.votes);
		            TextView submitterView = (TextView) view.findViewById(R.id.submitter);
	                TextView submissionTimeView = (TextView) view.findViewById(R.id.submissionTime);
		            
		            try {
		            	votesView.setText(Util.showNumPoints(item.getUps() - item.getDowns()));
		            } catch (NumberFormatException e) {
		            	// This happens because "ups" comes after the potentially long "replies" object,
		            	// so the ListView might try to display the View before "ups" in JSON has been parsed.
		            	if (Constants.LOGGING) Log.e(TAG, "getView, hidden comment heads", e);
		            }
		            if (getOpThingInfo() != null && item.getAuthor().equalsIgnoreCase(getOpThingInfo().getAuthor()))
		            {
		            	submitterView.setText(item.getAuthor() + " [S]");
		            	submitterView.setTextColor(getActivity().getResources().getColor(R.color.orange));
		            }
		            else
		            {
		            	submitterView.setText(item.getAuthor());
		            	submitterView.setTextColor(getActivity().getResources().getColor(R.color.gray));
		            }
		            
		            submissionTimeView.setText(Util.getTimeAgo(item.getCreated_utc()));
		            
		            setCommentIndent(view, item.getIndent(), mRedditSettings);
		            
            	} else if (isLoadMoreCommentsPosition(position)) {
	            	// "load more comments"
	            	if (view == null) {
	            		view = mInflater.inflate(R.layout.more_comments_view, null);
	            	}

	            	setCommentIndent(view, item.getIndent(), mRedditSettings);
	            	
	            } else {  // Regular comment
	            	// Here view may be passed in for re-use, or we make a new one.
		            if (view == null) {
		                view = mInflater.inflate(R.layout.comments_list_item, null);
		            } else {
		                view = convertView;
		            }

					// Sometimes (when in touch mode) the "selection" highlight disappears.
					// So we make our own persistent highlight. This background color must
					// be set explicitly on every element, however, or the "cached" list
					// item views will show up with the color.
					if (position == last_found_position)
						view.setBackgroundResource(R.color.translucent_yellow);
					else
						view.setBackgroundColor(Color.TRANSPARENT);

		            fillCommentsListItemView(view, item, mRedditSettings);
	            }
            } catch (NullPointerException e) {
            	if (Constants.LOGGING) Log.w(TAG, "NPE in getView()", e);
            	// Probably means that the List is still being built, and OP probably got put in wrong position
            	if (view == null) {
            		if (position == 0)
            			view = mInflater.inflate(R.layout.threads_list_item, null);
            		else
            			view = mInflater.inflate(R.layout.comments_list_item, null);
	            }
            }
            return view;
        }
    } // End of RepliesListAdapter
    
    public static void setCommentIndent(View commentListItemView, int indentLevel, RedditSettings settings) {
        View[] indentViews = new View[] {
        	commentListItemView.findViewById(R.id.left_indent1),
        	commentListItemView.findViewById(R.id.left_indent2),
        	commentListItemView.findViewById(R.id.left_indent3),
        	commentListItemView.findViewById(R.id.left_indent4),
        	commentListItemView.findViewById(R.id.left_indent5),
        	commentListItemView.findViewById(R.id.left_indent6),
        	commentListItemView.findViewById(R.id.left_indent7),
        	commentListItemView.findViewById(R.id.left_indent8)
        };
        for (int i = 0; i < indentLevel && i < indentViews.length; i++) {
        	if (settings.isShowCommentGuideLines()) {
            	indentViews[i].setVisibility(View.VISIBLE);
            	indentViews[i].setBackgroundResource(R.color.light_light_gray);
        	} else {
        		indentViews[i].setVisibility(View.INVISIBLE);
        	}
        }
        for (int i = indentLevel; i < indentViews.length; i++) {
        	indentViews[i].setVisibility(View.GONE);
        }
    }
    
    public void fillCommentsListItemView(View view, ThingInfo item, RedditSettings settings) {
        // Set the values of the Views for the CommentsListItem
        
        TextView votesView = (TextView) view.findViewById(R.id.votes);
        TextView submitterView = (TextView) view.findViewById(R.id.submitter);
        TextView bodyView = (TextView) view.findViewById(R.id.body);
        
        TextView submissionTimeView = (TextView) view.findViewById(R.id.submissionTime);
        ImageView voteUpView = (ImageView) view.findViewById(R.id.vote_up_image);
        ImageView voteDownView = (ImageView) view.findViewById(R.id.vote_down_image);
        
        try {
        	votesView.setText(Util.showNumPoints(item.getUps() - item.getDowns()));
        } catch (NumberFormatException e) {
        	// This happens because "ups" comes after the potentially long "replies" object,
        	// so the ListView might try to display the View before "ups" in JSON has been parsed.
        	if (Constants.LOGGING) Log.e(TAG, "getView, normal comment", e);
        }
        
        
        if (getOpThingInfo() != null && item.getAuthor().equalsIgnoreCase(getOpThingInfo().getAuthor()))
        {
        	submitterView.setText(item.getAuthor() + " [S]");
        	submitterView.setTextColor(getActivity().getResources().getColor(R.color.orange));
        }
        else
        {
        	submitterView.setText(item.getAuthor());
        	submitterView.setTextColor(getActivity().getResources().getColor(R.color.gray));
        }
        
        
        submissionTimeView.setText(Util.getTimeAgo(item.getCreated_utc()));
        
    	if (item.getSpannedBody() != null)
    		bodyView.setText(item.getSpannedBody());
    	else
    		bodyView.setText(item.getBody());
        
    	
        
    	bodyView.setMovementMethod(LinkMovementMethod.getInstance());
    	
        setCommentIndent(view, item.getIndent(), settings);
        
        if (voteUpView != null && voteDownView != null) {
	        if (item.getLikes() == null || "[deleted]".equals(item.getAuthor())) {
	        	voteUpView.setVisibility(View.GONE);
	        	voteDownView.setVisibility(View.GONE);
	    	}
	        else if (Boolean.TRUE.equals(item.getLikes())) {
	    		voteUpView.setVisibility(View.VISIBLE);
	    		voteDownView.setVisibility(View.GONE);
	    	}
	        else if (Boolean.FALSE.equals(item.getLikes())) {
	    		voteUpView.setVisibility(View.GONE);
	    		voteDownView.setVisibility(View.VISIBLE);
	    	}
        }
    }
    
    public ThingInfo getOpThingInfo() {
    	return mComment;
    }
    
    private boolean isHiddenCommentHeadPosition(int position) {
    	return mRepliesAdapter != null && mRepliesAdapter.getItemViewType(position) == RepliesListAdapter.HIDDEN_ITEM_HEAD_VIEW_TYPE;
    }
    
    private boolean isHiddenCommentDescendantPosition(int position) {
    	return mRepliesAdapter != null && mRepliesAdapter.getItem(position).isHiddenCommentDescendant();
    }
    
    private boolean isLoadMoreCommentsPosition(int position) {
    	return mRepliesAdapter != null && mRepliesAdapter.getItemViewType(position) == RepliesListAdapter.MORE_ITEM_VIEW_TYPE;
    }
    
    
    
    
    private class CommentReplyTask extends AsyncTask<String, Void, String> {
    	private String _mParentThingId;
    	String _mUserError = "Error submitting reply. Please try again.";
    	
    	CommentReplyTask(String parentThingId) {
    		_mParentThingId = parentThingId;
    	}
    	
    	@Override
        public String doInBackground(String... text) {
        	HttpEntity entity = null;
        	
        	if (!mRedditSettings.isLoggedIn()) {
//        		if( CommentFragment.this.getActivity() != null )
//        			Common.showErrorToast("You must be logged in to reply.", Toast.LENGTH_LONG, CommentFragment.this.getActivity());
        		_mUserError = "Not logged in";
        		return null;
        	}
        	// Update the modhash if necessary
        	if (mRedditSettings.getModhash() == null) {
        		String modhash = Common.doUpdateModhash(mRedditClient);
        		if (modhash == null) {
        			// doUpdateModhash should have given an error about credentials
        			Common.doLogout(mRedditSettings, mRedditClient, WaywtApplication.getContext());
        			if (Constants.LOGGING) Log.e(TAG, "Reply failed because doUpdateModhash() failed");
        			return null;
        		}
        		mRedditSettings.setModhash(modhash);
        	}
        	
        	try {
        		// Construct data
    			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    			nvps.add(new BasicNameValuePair("thing_id", _mParentThingId));
    			nvps.add(new BasicNameValuePair("text", text[0]));
    			nvps.add(new BasicNameValuePair("r", UserUtil.getSubreddit()));
    			nvps.add(new BasicNameValuePair("uh", mRedditSettings.getModhash()));
    			// Votehash is currently unused by reddit 
//    				nvps.add(new BasicNameValuePair("vh", "0d4ab0ffd56ad0f66841c15609e9a45aeec6b015"));
    			
    			HttpPost httppost = new HttpPost(Constants.REDDIT_BASE_URL + "/api/comment");
    	        httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    	        
    	        HttpParams params = httppost.getParams();
    	        HttpConnectionParams.setConnectionTimeout(params, 40000);
    	        HttpConnectionParams.setSoTimeout(params, 40000);
    	        
    	        if (Constants.LOGGING) Log.d(TAG, nvps.toString());
    	        
                // Perform the HTTP POST request
    	    	HttpResponse response = mRedditClient.execute(httppost);
    	    	entity = response.getEntity();
    	    	
            	// Getting here means success. Create a new CommentInfo.
            	return Common.checkIDResponse(response, entity);
            	
        	} catch (Exception e) {
        		if (Constants.LOGGING) Log.e(TAG, "CommentReplyTask", e);
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
        	return null;
        }
    	
    	@Override
    	public void onPreExecute() {
    		getActivity().showDialog(Constants.DIALOG_REPLYING);
    	}
    	
    	@Override
    	public void onPostExecute(String newId) {
    		getActivity().removeDialog(Constants.DIALOG_REPLYING);
    		if (newId == null) {
//    			if( CommentFragment.this.getActivity() != null )
//    				Common.showErrorToast(_mUserError, Toast.LENGTH_LONG, CommentFragment.this.getActivity());
    			
    			getActivity().showDialog(Constants.DIALOG_LOGIN);
    		} else {
    			// Refresh
    			CacheInfo.invalidateCachedThread(WaywtApplication.getContext());
    			
    			try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    			getNewDownloadRepliesTask().prepareLoadMoreComments(mComment.getId(), 0, mComment.getIndent()).execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
    			
    			mReplyEt.getText().clear();
    			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    			imm.hideSoftInputFromWindow(mReplyEt.getWindowToken(), 0);
    		}
    	}
    }

	@Override
	public void enableLoadingScreen() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resetUI() {
		// TODO Auto-generated method stub
		
	}

	private int mMorePosition = 0;
	
	@Override
	public void updateComments(List<ThingInfo> comments) {  
		
		Log.d("author", mComment.getAuthor());
		

		
		if( getView() != null && mHeaderListView != null && comments != null && comments.size() > 0 && comments.get(0) != null )
		{
			if( comments.get(0).getAuthor().equals(mComment.getAuthor()) )
			{
			
				mComment = comments.get(0);
				mRepliesList.clear();
				mRepliesList.addAll(comments);
				mRepliesAdapter.notifyDataSetChanged();
				
				if( mHeaderListView != null )
				{
					updatePoints(mHeaderListView);
				}
			}
			else
			{
				ArrayList<ThingInfo> newCommentsList = new ArrayList<ThingInfo>();
				
				for( int i = 0; i < mMorePosition && i < mRepliesList.size(); ++i)
				{
					newCommentsList.add(mRepliesList.get(i));
				}
				
				newCommentsList.addAll(comments);
				 
				for( int i = mMorePosition + 1; i < mRepliesList.size(); ++i)
				{
					newCommentsList.add(mRepliesList.get(i));
				}
				
				mRepliesList.clear();
				mRepliesList.addAll(newCommentsList);
				mRepliesAdapter.notifyDataSetChanged();
				
				if( mHeaderListView != null )
				{
					updatePoints(mHeaderListView);
				}
			}
			
//			ViewFlipper vf = (ViewFlipper)getView().findViewById(R.id.vf);
//			vf.setDisplayedChild(1);
		}
	}
	
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// TODO Auto-generated method stub
    	switch(item.getItemId() )
    	{
//    	case R.id.login_menu_id:
//    		mRedditSettings.loadRedditPreferences(getActivity(), null);
//    		break;
//    	
//    	case R.id.logout_menu_id:
//    		mRedditSettings.loadRedditPreferences(getActivity(), null);
//    		getNewDownloadRepliesTask().prepareLoadMoreComments(mComment.getId(), 0, mComment.getIndent()).execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
//            break;
    	}
    	
    	
    	return super.onOptionsItemSelected(item);
    }


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
        ThingInfo item = mRepliesAdapter.getItem(position);
        
//        if (isHiddenCommentHeadPosition(position)) {
//        	showComment(position);
//        	return;
//        }
        
//        // Mark the OP post/regular comment as selected
//        mVoteTargetThing = item;
//        mReplyTargetName = mVoteTargetThing.getName();
		
        mMorePosition = position;
        
        if (isLoadMoreCommentsPosition(position)) {
        	// Use this constructor to tell it to load more comments inline
        	getNewDownloadRepliesTask().prepareLoadMoreComments(item.getId(), position, item.getIndent())
        			.execute(Constants.DEFAULT_COMMENT_DOWNLOAD_LIMIT);
        } else {
//        	if (!"[deleted]".equals(item.getAuthor()))
//        		showDialog(Constants.DIALOG_COMMENT_CLICK);
        }
	    
	}
	
	
	

}
