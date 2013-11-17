package com.sobremesa.waywt.service.clients;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sobremesa.waywt.service.CommentService.RemoteData;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostComment;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentData;
import com.sobremesa.waywt.service.CommentService.RemoteResponse;

import retrofit.RestAdapter;
import retrofit.Server;
import retrofit.converter.GsonConverter;
import android.content.Context;
import android.util.Log;

public class LoginServiceClient {
	private static LoginServiceClient instance;
	public static final String BASE_URL_PROD = "https://ssl.reddit.com";
	public static final String BASE_URL_TEST = BASE_URL_PROD;
	public static final String BASE_URL_DEV = BASE_URL_PROD;
	
	private RestAdapter mRestAdapter;
	private Map<String, Object> mClients = new HashMap<String, Object>();
	
	private String mBaseUrl = BASE_URL_PROD;

	private LoginServiceClient() {
	}
	
	public static LoginServiceClient getInstance() {
		if (null == instance) {
			instance = new LoginServiceClient();
		}
		
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T> T getClient(Context context, Class<T> clazz) {
		
		Gson gson = new GsonBuilder()  
        .registerTypeAdapter(RemoteResponse.class, new LoginDeserializer())    
        .create();  
		
		if (mRestAdapter == null) {
			mRestAdapter = new RestAdapter.Builder().setConverter(new GsonConverter(gson)).setServer(
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
		return mBaseUrl;
	}
	
	private static class LoginDeserializer implements JsonDeserializer<RemoteResponse> {
		@Override
		public RemoteResponse deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

			JsonObject jobject = (JsonObject) json;
			Log.d("kece", "kece");
			int lala = 0;
			
			lala++;
			
			return new RemoteResponse();
		}
	}
	
}
