package com.sobremesa.waywt.fragments;

import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostService;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WaywtFragment extends Fragment {

	public static final String TAG = WaywtFragment.class.getCanonicalName();
	
	public static class Extras
	{
		public static String ARG_PERMALINK = "permalink";
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		fetchRedditPostData();
	}
	
	private void fetchRedditPostData() {

		Intent i = new Intent(getActivity(), RedditPostCommentService.class);
		i.setAction(Intent.ACTION_SYNC);
		i.putExtra(RedditPostCommentService.Extras.ARG_PERMALINK, getArguments().getString(Extras.ARG_PERMALINK));
		getActivity().startService(i);
	}
}
