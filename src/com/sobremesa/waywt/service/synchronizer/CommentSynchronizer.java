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
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.service.PostService;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class CommentSynchronizer extends Synchronizer<ThingInfo> {

	private boolean mIsMale = true;
	
	public CommentSynchronizer(Context context) {
		super(context);
	}
	

	public void setIsMale(boolean isMale)
	{
		mIsMale = isMale;
	}

	@Override
	protected void performSynchronizationOperations(Context context, List<ThingInfo> inserts, List<ThingInfo> updates, List<Long> deletions) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (ThingInfo w : inserts) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newInsert(Provider.COMMENT_CONTENT_URI).withValues(values).build();
			operations.add(op);
		}

		
		for (ThingInfo w : updates) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(Provider.COMMENT_CONTENT_URI).withSelection(CommentTable.NAME + " = ?", new String[] { w.getName() })
					.withValues(values).build();
			operations.add(op);
		}

		for (Long id : deletions) {
			ContentProviderOperation op = ContentProviderOperation.newDelete(Provider.COMMENT_CONTENT_URI).withSelection(CommentTable.ID + " = ?", new String[] { String.valueOf(id) }).build();
			operations.add(op);
		}

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
	protected boolean isRemoteEntityNewerThanLocal(ThingInfo remote, Cursor c) {
		// there isn't a versioning mechanism on the service resources so always
		// consider the remote copy new
		return true;
	}

	@Override
	protected ContentValues getContentValuesForRemoteEntity(ThingInfo t) {
		ContentValues values = new ContentValues();
		values.put(CommentTable.POST_TITLE, t.getPostTitle());
		values.put(CommentTable.THREAD_ID, t.getThreadId());
		values.put(CommentTable.POST_PERMALINK, t.getPostPermalink());
		values.put(CommentTable.CREATED, t.getCreated_utc());
		values.put(CommentTable.COMMENT_ID, t.getId());
		values.put(CommentTable.AUTHOR, t.getAuthor());
		values.put(CommentTable.BODY_HTML, t.getBody_html());
		values.put(CommentTable.NAME, t.getName());
		values.put(CommentTable.LIKES, t.getLikes() ? 1:0);
		values.put(CommentTable.UPS, t.getUps());
		values.put(CommentTable.DOWNS, t.getDowns());
		values.put(CommentTable.IS_MALE, mIsMale ? 1 : 0);
		
		return values;
	}

}
