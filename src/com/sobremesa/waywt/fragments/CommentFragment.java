package com.sobremesa.waywt.fragments;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.activities.ImageActivity;
import com.sobremesa.waywt.activities.MainActivity;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.listeners.LoginListener;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.service.CommentService.ImgurClient;
import com.sobremesa.waywt.service.CommentService.RemoteImgurAlbum;
import com.sobremesa.waywt.service.CommentService.RemoteImgurAlbumImage;
import com.sobremesa.waywt.service.CommentService.RemoteImgurResponse;
import com.sobremesa.waywt.service.clients.ImgurServiceClient;
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
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
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
	private List<String> mImageUrls;
	
	
	private ImageLoader mImageLoader;
	private AspectRatioImageView mMainIv;
	
	private WaywtSecondaryTextView mTitleTv;
	private TextView mPointsTv;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mComment = (ThingInfo)getArguments().get(Extras.ARG_COMMENT);
		mImageUrls = new ArrayList<String>();
		
		String bodyHtml = mComment.getBody_html();
		
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
//							ImgurClient imgurClient = ImgurServiceClient.getInstance().getClient(getContext(), ImgurClient.class);  
//							
//							String albumId = url.split("/a/")[1];
//							RemoteImgurResponse imgurResponse = imgurClient.getAlbum( "Client-ID " + "e52e554e5972395", albumId);  
//							RemoteImgurAlbum imgurAlbum = imgurResponse.data;
//							List<RemoteImgurAlbumImage> imgs = imgurAlbum.images;
//							
//							for( RemoteImgurAlbumImage img : imgs)
//							{
//								url = img.link;
//								
//								url = url.replace("imgur", "i.imgur");
//								url += "m.jpg";
//								
//								RemoteImage image = new RemoteImage();
//								image.url = url;
//								image.postId = mPostId;
//								image.commentId = commentId;
//								images.add(image);
//							}
						
						}
						else
						{
							url = url.replace("imgur", "i.imgur");
							url += "m.jpg";
							mImageUrls.add(url);
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
						
						mImageUrls.add(url);
						
					}
				}
				
				else if( url.contains("drsd.so") || url.contains("dressed.so"))
				{
					if( url.contains("drsd.so") )
					{
						URL u;
//						try {
//							u = new URL(url);
//							HttpURLConnection ucon = (HttpURLConnection) u.openConnection();
//							ucon.setInstanceFollowRedirects(false);
//							URL secondURL = new URL(ucon.getHeaderField("Location"));
//							
//							url = secondURL.toString(); 
//							
//							
//							
//						} catch (MalformedURLException e) {
//							// TODO Auto-generated catch block
//							Log.d("exc", url);  
//							e.printStackTrace();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							Log.d("exc", url);  
//							e.printStackTrace();
//						}
							continue;
					}
					
					if( !url.contains("cdn.dressed.so") )
					{
						url = url.replace("dressed.so/post/view", "cdn.dressed.so/i");
						

						
						url += "m.jpg";  
					}
					
					mImageUrls.add(url);
				}
				
			
			}
				
		}
	    
		
		
		
		Options options = new Options();
		options.scalingPreference = Options.ScalingPreference.ROUND_TO_CLOSEST_MATCH;
		mImageLoader = ImageLoader.buildImageLoaderForSupportFragment(this);
		mImageLoader.setDefaultOptions(options);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final View view = inflater.inflate(R.layout.fragment_comment, null, false);
		
		
		mMainIv = (AspectRatioImageView)view.findViewById(R.id.comment_image_iv);
		mPointsTv = (TextView)view.findViewById(R.id.comment_points_tv);
		mTitleTv = (WaywtSecondaryTextView)view.findViewById(R.id.comment_title_tv);
		
//		ImageView arrowUpIv = (ImageView)view.findViewById(R.id.comment_arrow_up_iv);
//		arrowUpIv.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
////				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ssl.reddit.com/api/v1/authorize?state=egrwnoierignoernreuiw&duration=permanent&response_type=code&scope=identity&client_id=v2-xpPJEV2GZhg&redirect_uri=https://com.sobremesa.waywt"));
//				
////				Intent intent = new Intent(getActivity(), WebViewActivity.class);
////				startActivityForResult(intent, 3445);
//				
//				
////				LiveAuthenticator.authenticate("opheliawnik", "allanpoe", CommentFragment.this);
//				MainActivity act = (MainActivity)getActivity();
//				
//				act.showDialog(Constants.DIALOG_LOGIN);
//			}
//		});
		
		
		
		// Images
		if( mImageUrls.size() > 0 )
		{
			final String mainImageUrl = mImageUrls.get(0);
			
			mImageLoader.loadImage(mMainIv, mainImageUrl, new ImageLoaderListener() {
				@Override
				public void onImageLoadError(String arg0) { 
					
					Log.d("fail", mainImageUrl);
					
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
				final String imageUrl = mImageUrls.get(0);
				
				
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
		
						Log.d("fail", imageUrl);
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

		
		
		// Points
		int ups = Integer.valueOf(mComment.getUps()); 
		int downs = Integer.valueOf(mComment.getDowns());
		
		ImageView arrowUpIv = (ImageView)view.findViewById(R.id.comment_arrow_up_iv);
		ImageView arrowDownIv = (ImageView)view.findViewById(R.id.comment_arrow_down_iv);		
		
		arrowUpIv.setVisibility(View.VISIBLE);
		arrowDownIv.setVisibility(View.VISIBLE);
		
		mPointsTv.setText((ups-downs)  + "");
		
		
		// Text
		String bodyHtml =  mComment.getBody_html();
		mTitleTv.setText(Html.fromHtml(Html.fromHtml(bodyHtml).toString()));
		mTitleTv.setTypeface(FontManager.INSTANCE.getGeorgiaFont(), Typeface.ITALIC);
		
		mTitleTv.setMovementMethod (LinkMovementMethod.getInstance());
		mTitleTv.setClickable(true);
		
		
		
		
		// Replies fragment
		RepliesFragment fragment = new RepliesFragment();
		Bundle args = new Bundle();
		args.putParcelable(RepliesFragment.Extras.ARG_COMMENT, mComment);
		fragment.setArguments(args);
		
		getChildFragmentManager().beginTransaction().replace(R.id.comment_replies_container, fragment, RepliesFragment.class.getCanonicalName()).commit();
		
	
		
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

	
	private void startImagesActivity(int position)
	{
		Intent intent = new Intent(getActivity(), ImageActivity.class);
		Bundle extras = new Bundle();
		extras.putString(ImageActivity.Extras.ARG_COMMENT, getArguments().getString(Extras.ARG_COMMENT));
		extras.putInt(ImageActivity.Extras.ARG_IMAGE_SELECTED_POSITION, position);
		intent.putExtras(extras);
		startActivity(intent);
	}

	
	
	@Override
	  public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    getActivity();
	    if(requestCode == 1 && resultCode == Activity.RESULT_OK) {
	       //some code
	    }
	  }


	
}
