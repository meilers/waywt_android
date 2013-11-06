package com.sobremesa.waywt.service.synchronizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.RedditPostTable;
import com.sobremesa.waywt.service.RedditPostService;
import com.sobremesa.waywt.service.RedditPostService.RemoteRedditPost;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class RedditPostSynchronizer extends Synchronizer<RedditPostService.RemoteRedditPost> {

	HashMap<String, String> mImageUrlMap;
	
	public RedditPostSynchronizer(Context context) {
		super(context);
		
		mImageUrlMap = new HashMap<String, String>();
	}

	@Override
	protected void performSynchronizationOperations(Context context, List<RemoteRedditPost> inserts, List<RemoteRedditPost> updates, List<Long> deletions) {
//		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
//
//		for (RedditPostDataService.RemoteRedditPost w : inserts) {
//			ContentValues values = this.getContentValuesForRemoteEntity(w);
//			ContentProviderOperation op = ContentProviderOperation.newInsert(Provider.RECORDING_CONTENT_URI).withValues(values).build();
//			operations.add(op);
//		}
//
//		for (RedditPostDataService.RemoteRedditPost w : updates) {
//			ContentValues values = this.getContentValuesForRemoteEntity(w);
//			ContentProviderOperation op = ContentProviderOperation.newUpdate(Provider.RECORDING_CONTENT_URI).withSelection(RedditPostDataTable.RECORDINGID + " = ?", new String[] { w.id })
//					.withValues(values).build();
//			operations.add(op);
//		}
//
//		for (Long id : deletions) {
//			ContentProviderOperation op = ContentProviderOperation.newDelete(Provider.RECORDING_CONTENT_URI).withSelection(RedditPostDataTable.ID + " = ?", new String[] { String.valueOf(id) }).build();
//			operations.add(op);
//		}
//
//		try {
//			context.getContentResolver().applyBatch(Provider.AUTHORITY, operations);
//
//			context.getContentResolver().notifyChange(Uri.withAppendedPath(Provider.RECORDING_CONTENT_URI, "/EN_NAME"), null);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (OperationApplicationException e) {
//			e.printStackTrace();
//		}

	}

	@Override
	protected boolean isRemoteEntityNewerThanLocal(RemoteRedditPost remote, Cursor c) {
		// there isn't a versioning mechanism on the service resources so always
		// consider the remote copy new
		return true;
	}

	@Override
	protected ContentValues getContentValuesForRemoteEntity(RemoteRedditPost t) {
		ContentValues values = new ContentValues();
//		values.put(RedditPostDataTable.RECORDINGID, t.id);
//		values.put(RedditPostDataTable.GEN, t.gen);
//		values.put(RedditPostDataTable.SP, t.sp);
//		values.put(RedditPostDataTable.EN, t.en);
//		values.put(RedditPostDataTable.REC, t.rec);
//		values.put(RedditPostDataTable.LOC, t.loc);
//		values.put(RedditPostDataTable.CNT, t.cnt);
//		values.put(RedditPostDataTable.LAT, t.lat);
//		values.put(RedditPostDataTable.LNG, t.lng);
//		values.put(RedditPostDataTable.TYPE, t.type);
//		values.put(RedditPostDataTable.FILE, t.file);
//		values.put(RedditPostDataTable.LIC, t.lic);


		return values;
	}

}
