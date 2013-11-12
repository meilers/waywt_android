package com.sobremesa.waywt.adapters;

import java.util.List;

import com.sobremesa.waywt.fragments.CommentFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

public class CommentPagerAdapter extends FragmentStatePagerAdapter {
	List<String> mCommentIds;
	List<String> mAuthors;

	public CommentPagerAdapter(FragmentManager fragmentManager,  List<String> commentIds, List<String> authors) {
		super(fragmentManager);
		mCommentIds = commentIds;
		mAuthors = authors;
	}
	
	public void setInfo(List<String> commentIds,  List<String> authors)
	{
		mCommentIds = commentIds;
		mAuthors = authors;
		notifyDataSetChanged();
	}
	

	@Override
	public Fragment getItem(int position) {
		
		CommentFragment fragment = new CommentFragment();
		Bundle args = new Bundle();
		args.putString(CommentFragment.Extras.ARG_COMMENT_ID, mCommentIds.get(position));;
		
		
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public int getCount() {
		return mCommentIds.size();
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
      return mAuthors.get(position);
    }
	
	
}
