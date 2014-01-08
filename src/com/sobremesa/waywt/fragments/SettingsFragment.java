package com.sobremesa.waywt.fragments;

import java.util.List;

import org.apache.http.client.HttpClient;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.activities.MainActivity;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.common.Common;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.enums.SortByType;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.util.UserUtil;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingsFragment extends BaseFragment {

	private RelativeLayout mMfaRl;
	private RelativeLayout mFfaRl;
	private RelativeLayout mTeenMfaRl;
	private RelativeLayout mTeenFfaRl;
	private RelativeLayout mSortByRandomRl;
	private RelativeLayout mSortByUpvotesRl;
	private RelativeLayout mSortByMostRecentRl;
	private RelativeLayout mSortByNoneRl;
	
	private RadioButton mMfaRb;
	private RadioButton mFfaRb;
	private RadioButton mTeenMfaRb;
	private RadioButton mTeenFfaRb;
	private RadioButton mSortByRandomRb;
	private RadioButton mSortByUpvotesRb;
	private RadioButton mSortByMostRecentRb;
	private RadioButton mSortByNoneRb;
	
	private TextView mLoginTv;
	private TextView mNbPostsTv;
	private TextView mNbPointsTv;
	
	private final HttpClient mRedditClient = WaywtApplication.getRedditClient();
	private final RedditSettings mRedditSettings = WaywtApplication.getRedditSettings();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_settings, null);
		
		mMfaRl = (RelativeLayout)view.findViewById(R.id.settings_mfa_rl);
		mFfaRl = (RelativeLayout)view.findViewById(R.id.settings_ffa_rl);
		mTeenMfaRl = (RelativeLayout)view.findViewById(R.id.settings_teen_mfa_rl);
		mTeenFfaRl = (RelativeLayout)view.findViewById(R.id.settings_teen_ffa_rl);
		
		mSortByRandomRl = (RelativeLayout)view.findViewById(R.id.settings_sort_by_random_rl);
		mSortByUpvotesRl = (RelativeLayout)view.findViewById(R.id.settings_sort_by_upvotes_rl);
		mSortByMostRecentRl = (RelativeLayout)view.findViewById(R.id.settings_sort_by_most_recent_rl);
		mSortByNoneRl = (RelativeLayout)view.findViewById(R.id.settings_sort_by_none_rl);
		mMfaRb = (RadioButton)view.findViewById(R.id.settings_mfa_rb);
		mFfaRb = (RadioButton)view.findViewById(R.id.settings_ffa_rb);
		mTeenMfaRb = (RadioButton)view.findViewById(R.id.settings_teen_mfa_rb);
		mTeenFfaRb = (RadioButton)view.findViewById(R.id.settings_teen_ffa_rb);
		mSortByRandomRb = (RadioButton)view.findViewById(R.id.settings_sort_by_random_rb);
		mSortByUpvotesRb = (RadioButton)view.findViewById(R.id.settings_sort_by_upvotes_rb);
		mSortByMostRecentRb = (RadioButton)view.findViewById(R.id.settings_sort_by_most_recent_rb);
		mSortByNoneRb = (RadioButton)view.findViewById(R.id.settings_sort_by_none_rb);
		mLoginTv = (TextView)view.findViewById(R.id.settings_login_tv);
		mNbPostsTv = (TextView)view.findViewById(R.id.settings_nb_posts_tv);
		mNbPointsTv = (TextView)view.findViewById(R.id.settings_nb_points_tv);
		
		mMfaRb.setChecked(UserUtil.getIsMale() && !UserUtil.getIsTeen());
		mFfaRb.setChecked(!UserUtil.getIsMale() && !UserUtil.getIsTeen());
		mTeenMfaRb.setChecked(UserUtil.getIsMale() && UserUtil.getIsTeen());
		mTeenFfaRb.setChecked(!UserUtil.getIsMale() && UserUtil.getIsTeen());
		mSortByRandomRb.setChecked(UserUtil.getSortBy() == SortByType.RANDOM.ordinal());
		mSortByUpvotesRb.setChecked(UserUtil.getSortBy() == SortByType.UPVOTES.ordinal());
		mSortByMostRecentRb.setChecked(UserUtil.getSortBy() == SortByType.MOST_RECENT.ordinal());
		mSortByNoneRb.setChecked(UserUtil.getSortBy() == SortByType.NONE.ordinal());
		
		if( mRedditSettings.isLoggedIn() )
		{
			mLoginTv.setText("Logged in as " + mRedditSettings.getUsername());
			mNbPostsTv.setVisibility(View.VISIBLE);
			mNbPointsTv.setVisibility(View.VISIBLE);
			
			Cursor cursor = getActivity().getContentResolver().query(Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.AUTHOR + "=?", new String[]{mRedditSettings.getUsername()}, null);
			
			int points = 0;
			
			for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() )
			{
				points += cursor.getInt(cursor.getColumnIndex(CommentTable.UPS))-cursor.getInt(cursor.getColumnIndex(CommentTable.DOWNS));
			}
			
			mNbPostsTv.setText(cursor.getCount() + " posts");
			mNbPointsTv.setText(points + " upvotes");
		}
		else
		{
			mLoginTv.setText("Not logged in");
		
			mNbPostsTv.setVisibility(View.GONE);
			mNbPointsTv.setVisibility(View.GONE);	
		}
		
		mMfaRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mMfaRb.setChecked(true);
			}
		});
		
		mFfaRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mFfaRb.setChecked(true);
			}
		});
		
		mTeenMfaRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTeenMfaRb.setChecked(true);
			}
		});
		
		mTeenFfaRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTeenFfaRb.setChecked(true);
			}
		});
		
		mSortByRandomRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSortByRandomRb.setChecked(true);
			}
		});

		mSortByUpvotesRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSortByUpvotesRb.setChecked(true);
			}
		});
		
		mSortByMostRecentRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSortByMostRecentRb.setChecked(true);
			}
		});
		
		mSortByNoneRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSortByNoneRb.setChecked(true);
			}
		});
		
		mMfaRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked )
				{
					mMfaRb.setChecked(isChecked);
					mFfaRb.setChecked(!isChecked);
					mTeenMfaRb.setChecked(!isChecked);
					mTeenFfaRb.setChecked(!isChecked);
						
					UserUtil.setIsMale(true);
					UserUtil.setIsTeen(false);
					
					updateMainActivity(isChecked);
				}
			}
		});
		
		mFfaRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked )
				{
					mMfaRb.setChecked(!isChecked);
					mFfaRb.setChecked(isChecked);
					mTeenMfaRb.setChecked(!isChecked);
					mTeenFfaRb.setChecked(!isChecked);
					
					UserUtil.setIsMale(false);
					UserUtil.setIsTeen(false);
					
					updateMainActivity(!isChecked);
				}
			}
		});
		
		mTeenMfaRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if( isChecked )
				{
					mMfaRb.setChecked(!isChecked);
					mFfaRb.setChecked(!isChecked);
					mTeenMfaRb.setChecked(isChecked);
					mTeenFfaRb.setChecked(!isChecked);
						
					UserUtil.setIsMale(true);
					UserUtil.setIsTeen(true);
					
					updateMainActivity(true);
				}
			}
		});
		
		mTeenFfaRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if( isChecked )
				{
					mMfaRb.setChecked(!isChecked);
					mFfaRb.setChecked(!isChecked);
					mTeenMfaRb.setChecked(!isChecked);
					mTeenFfaRb.setChecked(isChecked);
					
					UserUtil.setIsMale(false);
					UserUtil.setIsTeen(true);
					
					updateMainActivity(false);
				}
			}
		});
		
		mSortByRandomRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				
				if( isChecked )
				{
					mSortByRandomRb.setChecked(isChecked);
					mSortByUpvotesRb.setChecked(!isChecked);
					mSortByMostRecentRb.setChecked(!isChecked);
					mSortByNoneRb.setChecked(!isChecked);
					
					UserUtil.setSortBy(SortByType.RANDOM);
				}
			}
		});
		
		mSortByUpvotesRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked )
				{
					mSortByRandomRb.setChecked(!isChecked);
					mSortByUpvotesRb.setChecked(isChecked);
					mSortByMostRecentRb.setChecked(!isChecked);
					mSortByNoneRb.setChecked(!isChecked);
					
					UserUtil.setSortBy(SortByType.UPVOTES);
				}
			}
		});
		
		mSortByMostRecentRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked )
				{
					mSortByRandomRb.setChecked(!isChecked);
					mSortByUpvotesRb.setChecked(!isChecked);
					mSortByMostRecentRb.setChecked(isChecked);
					mSortByNoneRb.setChecked(!isChecked);
					
					UserUtil.setSortBy(SortByType.MOST_RECENT);
				}
			}
		});
		
		mSortByNoneRb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if( isChecked )
				{
					mSortByRandomRb.setChecked(!isChecked);
					mSortByUpvotesRb.setChecked(!isChecked);
					mSortByMostRecentRb.setChecked(!isChecked);
					mSortByNoneRb.setChecked(isChecked);
					
					UserUtil.setSortBy(SortByType.NONE);
				}
			}
		});

		setHasOptionsMenu(true);

		
		return view;
	}
	
	private void updateView()
	{
		mMfaRb.setChecked(UserUtil.getIsMale() && !UserUtil.getIsTeen());
		mFfaRb.setChecked(!UserUtil.getIsMale() && !UserUtil.getIsTeen());
		mTeenMfaRb.setChecked(UserUtil.getIsMale() && UserUtil.getIsTeen());
		mTeenFfaRb.setChecked(!UserUtil.getIsMale() && UserUtil.getIsTeen());
		mSortByRandomRb.setChecked(UserUtil.getSortBy() == SortByType.RANDOM.ordinal());
		mSortByUpvotesRb.setChecked(UserUtil.getSortBy() == SortByType.UPVOTES.ordinal());
		mSortByMostRecentRb.setChecked(UserUtil.getSortBy() == SortByType.MOST_RECENT.ordinal());
		mSortByNoneRb.setChecked(UserUtil.getSortBy() == SortByType.NONE.ordinal());
		
		if( mRedditSettings.isLoggedIn() )
		{
			mLoginTv.setText("Logged in as " + mRedditSettings.getUsername());
			mNbPostsTv.setVisibility(View.VISIBLE);
			mNbPointsTv.setVisibility(View.VISIBLE);
			
			Cursor cursor = getActivity().getContentResolver().query(Provider.COMMENT_CONTENT_URI, CommentTable.ALL_COLUMNS, CommentTable.AUTHOR + "=?", new String[]{mRedditSettings.getUsername()}, null);
			
			int points = 0;
			
			for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() )
			{
				points += cursor.getInt(cursor.getColumnIndex(CommentTable.UPS))-cursor.getInt(cursor.getColumnIndex(CommentTable.DOWNS));
			}
			
			mNbPostsTv.setText(cursor.getCount() + " posts");
			mNbPointsTv.setText(points + " upvotes");
		}
		else
		{
			mLoginTv.setText("Not logged in");
		
			mNbPostsTv.setVisibility(View.GONE);
			mNbPointsTv.setVisibility(View.GONE);	
		}

	}
	
	private void updateMainActivity( boolean isMale )
	{
		MainActivity act = (MainActivity)getActivity();
		
		if( act != null )
		{
			if( isMale )
			{
				act.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo)); 
			}
			else
			{
				act.getActionBar().setIcon(getResources().getDrawable(R.drawable.ic_logo_ffa));
			}
			
//			getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new Fragment()).commitAllowingStateLoss();
			
			UserUtil.setHasChosenSubreddit(true);
		
		
			getActivity().getSupportLoaderManager().restartLoader(0, null, (MainActivity)getActivity());
			act.fetchPostData();		
			act.resetWaywt();

		}
	

	}
	
    @Override
	protected int getOptionsMenuId() {
		return R.menu.settings;
	}

	@Override
	protected List<Integer> getMenuOptionIdsToStyle() {
		List<Integer> ids = super.getMenuOptionIdsToStyle();
		ids.add(R.id.login_menu_id);
		ids.add(R.id.logout_menu_id);
		return ids;
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
		
		MenuItem loginItem = menu.findItem(R.id.login_menu_id);
		MenuItem logoutItem = menu.findItem(R.id.logout_menu_id);
		
		if( loginItem != null && logoutItem != null )
		{
			if( mRedditSettings.isLoggedIn() )
			{
				loginItem.setVisible(false);
				logoutItem.setVisible(true);
			}
			else
			{
				loginItem.setVisible(true);
				logoutItem.setVisible(false);
			}
		}
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{
		case R.id.login_menu_id:
			getActivity().showDialog(Constants.DIALOG_LOGIN);
			break;
			
		case R.id.logout_menu_id:
			Common.doLogout(mRedditSettings, mRedditClient, WaywtApplication.getContext());
			updateView();
			break;
		}
		
		getActivity().invalidateOptionsMenu();
		
		return super.onOptionsItemSelected(item);
	}
	
	
}
