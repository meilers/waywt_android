package com.sobremesa.waywt.activities;


import java.util.ArrayList;
import java.util.List;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.activities.ImageActivity.Extras;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.managers.FontManager;
import com.sobremesa.waywt.managers.TypefaceSpan;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class BaseFragmentActivity extends FragmentActivity {
	

	public static final int NO_OPTIONS_MENU_ID = -1;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true); 
	}
	
	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		super.setTitle(title);

		SpannableString s = new SpannableString(String.valueOf(title).toUpperCase());
		s.setSpan(new TypefaceSpan(WaywtApplication.getContext()), 0, s.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		

		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);
	}

	

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		onStartingCreateOptionsMenu(menu);

		int optionsMenuId = getOptionsMenuId();
		if (optionsMenuId != NO_OPTIONS_MENU_ID) {
			getMenuInflater().inflate(optionsMenuId, menu);

			List<Integer> optionIds = getMenuOptionIdsToStyle();
			for (Integer id : optionIds) {
				final MenuItem item = menu.findItem(id);

				if (item != null) {
					View actionView = item.getActionView();

					if (actionView == null) {
						Log.d("ACTIONBAR", "creating action view");
						actionView = this.getLayoutInflater().inflate(R.layout.action_menu_button_layout, null, false);
						((TextView) actionView.findViewById(R.id.action_menu_button_text)).setText(item.getTitle());
						actionView.setBackgroundResource(R.drawable.item_selector);
						actionView.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								menu.performIdentifierAction(item.getItemId(), 0);

							}
						});
						item.setActionView(actionView);
					} else if (actionView instanceof TextView) {
						((TextView) actionView).setTypeface(FontManager.INSTANCE.getAppFont());
					}
				}
			}
		}

		onFinishingCreateOptionsMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

	protected void onStartingCreateOptionsMenu(Menu menu) {
		/* DO NOTHING */
	}

	protected void onFinishingCreateOptionsMenu(Menu menu) {
		/* DO NOTHING */
	}

	protected List<Integer> getMenuOptionIdsToStyle() {
		return new ArrayList<Integer>();
	}

	protected int getOptionsMenuId() {
		return NO_OPTIONS_MENU_ID;
	}
	
	
	
}
