package com.sobremesa.waywt.fragments;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.dialog.ProgressDialogFragment;
import com.sobremesa.waywt.dialog.ProgressDialogFragment.ProgressDialogObserver;
import com.sobremesa.waywt.managers.FontManager;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class BaseFragment extends Fragment {
	private static final int NO_OPTIONS_MENU_ID = -1;
	
	protected boolean mIsShowing = true;
	protected boolean mDialogDismissOnResume = false;

	protected ProgressDialogFragment mProgressFoDialog;

	public BaseFragment() {
	}

	@Override
	public final void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
		onStartingCreateOptionsMenu(menu, inflater);

		int optionsMenuId = getOptionsMenuId();
		if (optionsMenuId != NO_OPTIONS_MENU_ID) {
			inflater.inflate(optionsMenuId, menu);

			List<Integer> optionIds = getMenuOptionIdsToStyle();
			for (Integer id : optionIds) {
				final MenuItem item = menu.findItem(id);

				if (item != null) {
					View actionView = item.getActionView();

					if (actionView == null) {
						Log.d("ACTIONBAR", "creating action view");
						actionView = getActivity().getLayoutInflater().inflate(R.layout.action_menu_button_layout, null, false);
						((TextView) actionView.findViewById(R.id.action_menu_button_text)).setText(item.getTitle());
						actionView.setBackgroundResource(R.drawable.item_selector);
						actionView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								menu.performIdentifierAction(item.getItemId(), 0);

							}
						});
						item.setActionView(actionView);
					} else if (actionView instanceof Button) {
						((Button) actionView).setTypeface(FontManager.INSTANCE.getAppFont());
					} else if (actionView instanceof TextView) {
						((TextView) actionView).setTypeface(FontManager.INSTANCE.getAppFont());
					}
				}
			}
		}

		onFinisingCreateOptionsMenu(menu, inflater);
	}

	protected void onStartingCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		/* DO NOTHING */
	}

	protected void onFinisingCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		/* DO NOTHING */
	}

	protected List<Integer> getMenuOptionIdsToStyle() {
		return new ArrayList<Integer>();
	}

	protected int getOptionsMenuId() {
		return NO_OPTIONS_MENU_ID;
	}
	
	public void showProgressDialog(int stringResource) {
		showProgressDialog(getString(stringResource));
	}

	public void showProgressDialog(String text) {
		showProgressDialog(text, null);
	}

	public void showProgressDialog(int stringResource, ProgressDialogObserver observer) {
		showProgressDialog(getString(stringResource), observer);
	}
	public void showProgressDialog(String text, ProgressDialogObserver observer) {
		Bundle args = new Bundle();
		args.putString(ProgressDialogFragment.Extras.PROGRESS_TEXT, text);
		mProgressFoDialog = (ProgressDialogFragment) Fragment.instantiate(getActivity(), ProgressDialogFragment.class.getName(), args);
		mProgressFoDialog.setProgressDialogObserver(observer);
		mProgressFoDialog.show(getActivity().getSupportFragmentManager(), ProgressDialogFragment.class.getCanonicalName());
		mProgressFoDialog.setCancelable(false);
	}


	

	protected void hideProgressDialog() {
		if (mProgressFoDialog != null) {
			if (mIsShowing || isResumed()) {
				mProgressFoDialog.dismiss();
				mDialogDismissOnResume = false;
			} else {
				mDialogDismissOnResume = true;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mIsShowing = true;
		if (mDialogDismissOnResume)
			hideProgressDialog();
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsShowing = false;
	}
	
	
	@Override
	public void onDetach() {
	    super.onDetach();

	    try {
	        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
	        childFragmentManager.setAccessible(true);
	        childFragmentManager.set(this, null);

	    } catch (NoSuchFieldException e) {
	        throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	        throw new RuntimeException(e);
	    }
	}
}
