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
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;
import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostComment;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class RedditPostCommentSynchronizer extends Synchronizer<RedditPostCommentService.RemoteRedditPostComment> {

	HashMap<String, String> mImageUrlMap;
	
	public RedditPostCommentSynchronizer(Context context) {
		super(context);
		
		mImageUrlMap = new HashMap<String, String>();
	}

	@Override
	protected void performSynchronizationOperations(Context context, List<RemoteRedditPostComment> inserts, List<RemoteRedditPostComment> updates, List<Long> deletions) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (RedditPostCommentService.RemoteRedditPostComment w : inserts) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newInsert(Provider.REDDITPOSTCOMMENT_CONTENT_URI).withValues(values).build();
			operations.add(op);
			
			
		}

		for (RedditPostCommentService.RemoteRedditPostComment w : updates) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(Provider.REDDITPOSTCOMMENT_CONTENT_URI).withSelection(RedditPostCommentTable.IDENTIFIER + " = ?", new String[] { w.data.getIdentifier() })
					.withValues(values).build();
			operations.add(op);
			
		}

		for (Long id : deletions) {
			ContentProviderOperation op = ContentProviderOperation.newDelete(Provider.REDDITPOSTCOMMENT_CONTENT_URI).withSelection(RedditPostCommentTable.ID + " = ?", new String[] { String.valueOf(id) }).build();
			operations.add(op);
			
		}

		try {
			context.getContentResolver().applyBatch(Provider.AUTHORITY, operations);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected boolean isRemoteEntityNewerThanLocal(RemoteRedditPostComment remote, Cursor c) {
		// there isn't a versioning mechanism on the service resources so always
		// consider the remote copy new
		return true;
	}

	@Override
	protected ContentValues getContentValuesForRemoteEntity(RemoteRedditPostComment t) {
		ContentValues values = new ContentValues();
		
		values.put(RedditPostCommentTable.REDDITPOST_ID, t.data.postId);
		values.put(RedditPostCommentTable.IDENTIFIER, t.data.getIdentifier());
		
		values.put(RedditPostCommentTable.AUTHOR, t.data.author);
		values.put(RedditPostCommentTable.CREATED, t.data.created);
		values.put(RedditPostCommentTable.DOWNS, t.data.downs);
		values.put(RedditPostCommentTable.UPS, t.data.ups);
		values.put(RedditPostCommentTable.DOWNS, t.data.downs);
		values.put(RedditPostCommentTable.BODY_HTML, t.data.body_html);

		return values;
	}

}
