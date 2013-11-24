package com.sobremesa.waywt.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.sobremesa.waywt.fragments.CommentFragment;
import com.sobremesa.waywt.model.ThingInfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

public class CommentPagerAdapter extends FragmentStatePagerAdapter {
	public boolean mIsLoading = false;
	
	private ArrayList<ThingInfo> mComments;
	private boolean mDoSort = false;
	

	public CommentPagerAdapter(FragmentManager fragmentManager,  ArrayList<ThingInfo> comments, boolean doSort) {
		super(fragmentManager);
		mComments = comments;
		mDoSort = doSort;
	}
	
	public void setComments( List<ThingInfo> comments )
	{
		mComments.clear();
		mComments.addAll(comments);
		
		if( mDoSort )
			Collections.sort(mComments);
		else
		{
			long seed = System.nanoTime();
			Collections.shuffle(mComments, new Random(seed));
		}
		
		this.notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int position) {
		
		CommentFragment fragment = new CommentFragment();
		Bundle args = new Bundle();
		args.putParcelable(CommentFragment.Extras.ARG_COMMENT, mComments.get(position));
		
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public int getCount() {
		return mComments.size();
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
		if( mComments.get(position).getAuthor() != null )
			return mComments.get(position).getAuthor().toUpperCase();
		
		return null;
    }
	
	
}
