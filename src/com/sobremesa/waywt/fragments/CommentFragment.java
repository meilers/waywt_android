package com.sobremesa.waywt.fragments;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;
import com.sobremesa.waywt.fragments.CommentImagesFragment.Extras;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CommentFragment extends Fragment implements LoaderCallbacks<Cursor>
{
	public static final String TAG = CommentFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_COMMENT_ID = "comment_id";
	}
	

	private CursorLoader mLoader;
	
	private TextView mTitleTv;
	private TextView mPointsTv;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_comment, null, false);
		
		FragmentManager fm = getChildFragmentManager();
		
		CommentImagesFragment fragment = new CommentImagesFragment();
		Bundle args = new Bundle();
		args.putString(CommentImagesFragment.Extras.ARG_COMMENT_ID, getArguments().getString(Extras.ARG_COMMENT_ID));
		fragment.setArguments(args);
		
		fm.beginTransaction().replace(R.id.images_content_frame, fragment).commit();
		
		mPointsTv = (TextView)view.findViewById(R.id.comment_points_tv);
		mTitleTv = (TextView)view.findViewById(R.id.comment_title_tv);
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
		mLoader = new CursorLoader(getActivity(), Provider.REDDITPOSTCOMMENT_CONTENT_URI, RedditPostCommentTable.ALL_COLUMNS, RedditPostCommentTable.ID + "=?", new String[] { getArguments().getString(Extras.ARG_COMMENT_ID) }, null);

		return mLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		cursor.moveToFirst();
		
		if( cursor.getCount() > 0)
		{
			int ups = Integer.valueOf(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.UPS))); 
			int downs = Integer.valueOf(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.DOWNS)));
			
			mPointsTv.setText((ups-downs)  + "");
			mTitleTv.setText(cursor.getString(cursor.getColumnIndex(RedditPostCommentTable.AUTHOR)));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
