package com.sobremesa.waywt.fragments;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.RedditPostCommentSubcommentTable;
import com.sobremesa.waywt.fragments.CommentFragment.Extras;
import com.sobremesa.waywt.views.AspectRatioImageView;
import com.sobremesa.waywt.views.WaywtSecondaryTextView;
import com.xtremelabs.imageutils.ImageLoader;
import com.xtremelabs.imageutils.ImageLoader.Options;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RepliesFragment extends Fragment implements LoaderCallbacks<Cursor> {
	public static final String TAG = RepliesFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_COMMENT_ID = "comment_id";
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_replies, null, false);
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		getLoaderManager().initLoader(0, null, this);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), Provider.REDDITPOSTCOMMENTSUBCOMMENT_CONTENT_URI, RedditPostCommentSubcommentTable.ALL_COLUMNS, RedditPostCommentSubcommentTable.REDDITPOSTCOMMENT_ID + "=?", new String[] { getArguments().getString(Extras.ARG_COMMENT_ID) }, null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		
		
		
		if( getView() != null && cursor.getCount() > 0 )
		{
			LinearLayout container = (LinearLayout)getView().findViewById(R.id.replies_container);
			
			container.removeAllViews();
			
			for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() )
			{
				String author = cursor.getString(cursor.getColumnIndex(RedditPostCommentSubcommentTable.AUTHOR));
				String bodyHtml = cursor.getString(cursor.getColumnIndex(RedditPostCommentSubcommentTable.BODY_HTML));
				
				LinearLayout newLayout = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.list_item_reply, null);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.gravity = Gravity.LEFT;
				newLayout.setLayoutParams(params);
				
				TextView authorTv = (TextView)newLayout.findViewById(R.id.list_item_reply_author_tv);
				authorTv.setText(author);
				
				TextView bodyTv = (TextView)newLayout.findViewById(R.id.list_item_reply_body_tv);
				bodyTv.setText(Html.fromHtml(bodyHtml));
				
				container.addView(newLayout);
				
			}
			
			
		}
	}


	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
