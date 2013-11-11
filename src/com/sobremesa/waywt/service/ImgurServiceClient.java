package com.sobremesa.waywt.service;

import java.util.HashMap;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.Server;
import android.content.Context;

public class ImgurServiceClient {
	private static ImgurServiceClient instance;
	public static final String BASE_URL_PROD = "https://api.imgur.com";
	public static final String BASE_URL_TEST = BASE_URL_PROD;
	public static final String BASE_URL_DEV = BASE_URL_PROD;
	
	private RestAdapter mRestAdapter;
	private Map<String, Object> mClients = new HashMap<String, Object>();
	
	private String mBaseUrl = BASE_URL_PROD;

	private ImgurServiceClient() {
	}
	
	public static ImgurServiceClient getInstance() {
		if (null == instance) {
			instance = new ImgurServiceClient();
		}
		
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T> T getClient(Context context, Class<T> clazz) {
		if (mRestAdapter == null) {
			mRestAdapter = new RestAdapter.Builder().setServer(
					new Server(getBaseUrl(context))).build();
		}
		T client = null;
		if ((client = (T) mClients.get(clazz.getCanonicalName())) != null) {
			return client;
		}
		client = mRestAdapter.create(clazz);
		mClients.put(clazz.getCanonicalName(), client);
		return client;
	}

	public void setRestAdapter(RestAdapter restAdapter) {
		mRestAdapter = restAdapter;
	}
	
	public String getBaseUrl(Context context) {
//		if (mBaseUrl == null) {
//			try {
//				ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
//				if (ai.metaData != null && ai.metaData.containsKey("deployment")) {
//					String deployment = (String)ai.metaData.getString("deployment");
//					if (deployment.equals("dev")) {
//						mBaseUrl = BASE_URL_DEV;
//					} else if (deployment.equals("test")) {
//						mBaseUrl = BASE_URL_TEST;
//					} else {
//						mBaseUrl = BASE_URL_PROD;
//					}
//				}
//			} catch (NameNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
//		return mBaseUrl;
		return mBaseUrl;
	}
}
