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
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.database.tables.PostTable;
import com.sobremesa.waywt.service.PostService;
import com.sobremesa.waywt.service.PostService.RemoteRedditPost;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class PostSynchronizer extends Synchronizer<PostService.RemoteRedditPost> {

	private boolean mIsMale = true;
	private boolean mIsTeen = false;
	
	public PostSynchronizer(Context context) {
		super(context);
	}

	public void setIsMale(boolean isMale)
	{
		mIsMale = isMale;
	}
	
	public void setIsTeen(boolean isTeen)
	{
		mIsTeen = isTeen;
	}
	
	@Override
	protected void performSynchronizationOperations(Context context, List<RemoteRedditPost> inserts, List<RemoteRedditPost> updates, List<Long> deletions) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (PostService.RemoteRedditPost w : inserts) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newInsert(Provider.POST_CONTENT_URI).withValues(values).build();
			operations.add(op);
		}

		for (PostService.RemoteRedditPost w : updates) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(Provider.POST_CONTENT_URI).withSelection(PostTable.PERMALINK + " = ?", new String[] { w.data.permalink })
					.withValues(values).build();
			operations.add(op);
		}

//		for (Long id : deletions) {
//			ContentProviderOperation op = ContentProviderOperation.newDelete(Provider.POST_CONTENT_URI).withSelection(PostTable.ID + " = ?", new String[] { String.valueOf(id) }).build();
//			operations.add(op);
//		}

		try {
			if( operations.size() > 0 )
			{
				context.getContentResolver().applyBatch(Provider.AUTHORITY, operations);
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}

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
		values.put(PostTable.AUTHOR, t.data.author);
		values.put(PostTable.CREATED, t.data.created);
		values.put(PostTable.DOWNS, t.data.downs);
		values.put(PostTable.UPS, t.data.ups);
		values.put(PostTable.DOWNS, t.data.downs);
		values.put(PostTable.PERMALINK, t.data.permalink);
		values.put(PostTable.TITLE, t.data.title);
		values.put(PostTable.IS_MALE, mIsMale ? 1 : 0);
		values.put(PostTable.IS_TEEN, mIsTeen ? 1 : 0);
		
		return values;
	}

}
