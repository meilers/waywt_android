package com.sobremesa.waywt.adapters;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.activities.MainActivity;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.util.UserUtil;

public class DrawerListAdapter extends BaseAdapter {

	private List<DrawerListItem> nameItems;
	private LayoutInflater mInflater;
	private int mSelectedItem = 0;
	


	public DrawerListAdapter(Context c) {
		mInflater = LayoutInflater.from(c);
		nameItems = new ArrayList<DrawerListItem>();
		nameItems.add(new DrawerListItem("WAYWT", R.drawable.ic_logo, R.drawable.ic_logo));
		nameItems.add(new DrawerListItem("MY POSTS", R.drawable.ic_camera, R.drawable.ic_camera));
		nameItems.add(new DrawerListItem("SETTINGS", R.drawable.ic_action_settings, R.drawable.ic_action_settings));
	}

	@Override
	public int getCount() {
		return nameItems.size();
	}

	@Override
	public DrawerListItem getItem(int position) {
		return nameItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_drawer, null);
		}

		final Resources r = WaywtApplication.getContext().getResources();

		final View itemLayout = convertView.findViewById(R.id.item_layout);
		final DrawerListItem item = getItem(position);
		final TextView textView = (TextView) convertView.findViewById(R.id.item_title);
		textView.setText(item.mTitle.toUpperCase());

		final ImageView imageView = (ImageView) convertView.findViewById(R.id.item_image);
		TextView cartNumberTv = (TextView)convertView.findViewById(R.id.item_drawer_cart_tv);
		
//		int nbItemsInCart = UserUtil.getCurrentShoppingCartSize();
//		int nbItemsInCrate = UserUtil.getCurrentCrateSize() >= 0 ? UserUtil.getCurrentCrateSize() : 0;
//		int totalItems = nbItemsInCart + nbItemsInCrate;
		
		if (mSelectedItem == position) {
			imageView.setImageResource(item.mImageWhiteResourceId);
			textView.setTextColor(r.getColor(R.color.white));
			itemLayout.setSelected(true);
			
//			if( position == MainActivity.TabsIndex.CART_POSITION && totalItems > 0 )
//			{
//				cartNumberTv.setVisibility(View.VISIBLE);
//				
//				cartNumberTv.setText("[" + totalItems + "]");
//				cartNumberTv.setTextColor(convertView.getResources().getColor(R.color.fo_content_background));
//			}
//			else
//				cartNumberTv.setVisibility(View.INVISIBLE);
				
		} else {
			imageView.setImageResource(item.mImageResourceId);
			textView.setTextColor(r.getColor(R.color.white));
			itemLayout.setSelected(false);
			
//			if( position == MainActivity.DrawerTabIndex.CART_POSITION && totalItems > 0 )
//			{
//				cartNumberTv.setVisibility(View.VISIBLE);
//			
//				cartNumberTv.setText("[" + totalItems + "]");
//				cartNumberTv.setTextColor(convertView.getResources().getColor(R.color.fo_red));
//			}
//			else
//				cartNumberTv.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}

	public void setSelectedItem(int position) {
		mSelectedItem = position;
		notifyDataSetChanged();
	}

	private class DrawerListItem {
		private String mTitle;
		private int mImageResourceId;
		private int mImageWhiteResourceId;

		public DrawerListItem(String title, int imageId, int imageWhiteId) {
			mTitle = title;
			mImageResourceId = imageId;
			mImageWhiteResourceId = imageWhiteId;
		}
	}
}
