package com.sobremesa.waywt.fragments;

import com.sobremesa.waywt.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CommentFragment extends Fragment
{
	public static final String TAG = CommentFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_COMMENT_ID = "comment_id";
	}
	
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
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
}
