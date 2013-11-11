package com.sobremesa.waywt.adapters;

import java.util.List;

import com.sobremesa.waywt.fragments.CommentFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class CommentPagerAdapter extends FragmentStatePagerAdapter {
	List<String> mCommentIds;

	public CommentPagerAdapter(FragmentManager fragmentManager,  List<String> commentIds) {
		super(fragmentManager);
		mCommentIds = commentIds;
	}
	
	public void setCommentIds(List<String> commentIds)
	{
		mCommentIds = commentIds;
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
}
