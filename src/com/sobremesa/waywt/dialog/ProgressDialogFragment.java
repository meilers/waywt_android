package com.sobremesa.waywt.dialog;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.managers.FontManager;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgressDialogFragment extends DialogFragment {

	public static final String TAG = ProgressDialogFragment.class.getCanonicalName();
	
	public static final class Extras {
		public static final String PROGRESS_TEXT = "progressText";
	}

	AnimationDrawable mFrameAnimation;
	private ProgressDialogObserver observer;

	class Starter implements Runnable {
		public void run() {
			mFrameAnimation.start();
		}
	}

	@Override
	public void onInflate(Activity activity, AttributeSet attrs,Bundle savedInstanceState) {
		super.onInflate(activity, attrs, savedInstanceState);
		
		TypedArray a = activity.obtainStyledAttributes(attrs,R.styleable.FoProgressFragment);
		
	    CharSequence myString = a.getText(R.styleable.FoProgressFragment_progress_text);
	    
	    Bundle args = new Bundle();
	    args.putString(Extras.PROGRESS_TEXT, myString.toString());;
	    this.setArguments(args);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_fo_progress, container);

		if(getDialog() != null && getDialog().getWindow() != null){
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
			getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
		}

		ImageView img = (ImageView) view.findViewById(R.id.fo_progress_iv);
		mFrameAnimation = (AnimationDrawable) img.getDrawable();
		mFrameAnimation.setCallback(img);
		mFrameAnimation.setVisible(true, true);
		img.post(new Starter());

		String txt = getArguments().getString(Extras.PROGRESS_TEXT);
		TextView tv = (TextView) view.findViewById(R.id.fo_progress_tv);
		tv.setTypeface(FontManager.INSTANCE.getAppFont());
		tv.setText(txt);

		return view;
	}
	
	
	public static interface ProgressDialogObserver {
		public void onFragmentSetupComplete();
	}
	
	public synchronized void setProgressDialogObserver(ProgressDialogObserver observer) {
		this.observer = observer;
	}
	
	@Override
	public synchronized void onStart() {
		if (observer != null) {
			observer.onFragmentSetupComplete();
		}
		super.onStart();
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		observer = null;
	}
	
	
}
