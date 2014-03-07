package com.sobremesa.waywt.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.settings.RedditSettings;

public class AnalyticsUtil {

	private static final class DimensionValues{
		private static final int LANGUAGE = 1;
		private static final int LOGGED_IN = 2;
		private static final int WAVE_ID = 3;
		private static final String EN = "en";
		private static final String PARTNER = "partner";
		private static final String MEMBER = "member";
		private static final String GUEST = "guest";
	}
	
	private static final class ShareValues{
		private static final String FACEBOOK = "Facebook";
		private static final String SHARE = "Share";
		private static final String TWITTER = "Twitter";
		private static final String TWEET = "Tweet";
		private static final String EMAIL = "Email";
		private static final String SEND = "Send";
	}
	
	private static final class EventCategories{
		private static final String STORE = "Store";
		
		private static final String PRODUCT_DETAIL = "Product Detail";
		private static final String CART = "Cart";
		private static final String CRATE = "Crate";
		private static final String ACCOUNT = "Account";
		private static final String REGISTRATION = "Registration";
		
		private static final String GIFT_WRAPS = "Gift Wraps";
		

	}
	
	private static final class EventActions{
		private static final String ADD_TO_CART = "Add To Cart";
		private static final String SELECT_SIZE = "Select Size";
		private static final String VIEW_ALTERNATE_IMAGE = "View Alternate Image";
		private static final String VIEW_LARGE_IMAGE = "View Large Image";
		private static final String VIEW_PRODUCT = "View Product";
		private static final String SIGN_UP_ATTEMPT = "New Member Sign-up Attempt";
		private static final String SIGN_UP = "New Member Sign-up";
		private static final String REMOVE_FROM_CART = "Remove from Cart";
		private static final String LOG_OUT = "Log Out";
		private static final String LOG_IN = "Log In";
		
		private static final String FILTER = "Filter";
		private static final String FAVORITE = "Favorite";
		
		
		private static final String CHECKOUT_ERROR = "Checkout Error";
		
		private static final String PURCHASE_ERROR = "Purchase Error";
		
	}
	
	private static final class TotalTypes{
		private static final String SHIPPING = "Shipping";
		private static final String TAX = "Tax";
		private static final String GRAND = "Grand Total";
	}
	
 
	public static void sendView( Context c, String screenName )
	{
		Tracker easyTracker = EasyTracker.getInstance(c);

		// This screen name value will remain set on the tracker and sent with
		// hits until it is set to a new value or to null.
		
		String tag = screenName + ", " + UserUtil.getSubredditAcronym() + ", ";
		
		if( UserUtil.getSeeWaywtPosts() )
			tag += "WAYWT ";
		
		if( UserUtil.getSeeRecentPurchasesPosts() )
			tag += "RP ";
		
		if( UserUtil.getSeeOutfitFeedbackPosts() )
			tag += "OF ";
		
		tag += ", ";
		
		RedditSettings redditSettings = WaywtApplication.getRedditSettings();
		
		if( redditSettings.isLoggedIn() )
			tag += "logged in";
		else
			tag += "not logged in";
		
		Log.d("thissss", tag);
		
		
		easyTracker.set(Fields.SCREEN_NAME, tag);

		easyTracker.send(MapBuilder
		    .createAppView()
		    .build()
		);
	}
	
	
}
