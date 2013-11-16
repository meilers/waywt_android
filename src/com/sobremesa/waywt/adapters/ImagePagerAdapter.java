package com.sobremesa.waywt.adapters;

import java.util.List;

import com.sobremesa.waywt.fragments.CommentFragment;
import com.sobremesa.waywt.fragments.ImageFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

public class ImagePagerAdapter extends FragmentStatePagerAdapter {
	List<String> mImageUrls;

	public ImagePagerAdapter(FragmentManager fragmentManager,  List<String> imageUrls ) {
		super(fragmentManager);
		mImageUrls = imageUrls;
	}
	

	@Override
	public Fragment getItem(int position) {
		
		ImageFragment fragment = new ImageFragment();
		Bundle args = new Bundle();
		args.putString(ImageFragment.Extras.ARG_IMAGE_URL, mImageUrls.get(position));;
		
		
		fragment.setArguments(args);
		
		return fragment;
	}

	@Override
	public int getCount() {
		return mImageUrls.size();
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
      return "";
    }
	
	
}
