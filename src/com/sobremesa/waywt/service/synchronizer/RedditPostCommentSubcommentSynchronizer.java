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
import com.sobremesa.waywt.database.tables.RedditPostCommentSubcommentTable;
import com.sobremesa.waywt.database.tables.RedditPostCommentTable;
import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostComment;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChild;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class RedditPostCommentSubcommentSynchronizer extends Synchronizer<RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChild> {

	
	
	public RedditPostCommentSubcommentSynchronizer(Context context) {
		super(context);
	}

	@Override
	protected void performSynchronizationOperations(Context context, List<RemoteRedditPostCommentDataReplyDataChild> inserts, List<RemoteRedditPostCommentDataReplyDataChild> updates, List<Long> deletions) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChild w : inserts) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newInsert(Provider.REDDITPOSTCOMMENTSUBCOMMENT_CONTENT_URI).withValues(values).build();
			operations.add(op);
			
			
		}

		for (RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChild w : updates) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(Provider.REDDITPOSTCOMMENTSUBCOMMENT_CONTENT_URI).withSelection(RedditPostCommentSubcommentTable.IDENTIFIER + " = ?", new String[] { w.data.getIdentifier() })
					.withValues(values).build();
			operations.add(op);
			
		}

		for (Long id : deletions) {
			ContentProviderOperation op = ContentProviderOperation.newDelete(Provider.REDDITPOSTCOMMENTSUBCOMMENT_CONTENT_URI).withSelection(RedditPostCommentSubcommentTable.ID + " = ? " , new String[] { String.valueOf(id) }).build();
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
	protected boolean isRemoteEntityNewerThanLocal(RemoteRedditPostCommentDataReplyDataChild remote, Cursor c) {
		// there isn't a versioning mechanism on the service resources so always
		// consider the remote copy new
		return true;
	}

	@Override
	protected ContentValues getContentValuesForRemoteEntity(RemoteRedditPostCommentDataReplyDataChild t) {
		ContentValues values = new ContentValues();
		
		values.put(RedditPostCommentSubcommentTable.REDDITPOST_ID, t.data.postId);
		values.put(RedditPostCommentSubcommentTable.REDDITPOSTCOMMENT_ID, t.data.commentId);
		values.put(RedditPostCommentSubcommentTable.IDENTIFIER, t.data.getIdentifier());
		
		values.put(RedditPostCommentSubcommentTable.AUTHOR, t.data.author);
		values.put(RedditPostCommentSubcommentTable.CREATED, t.data.created);
		values.put(RedditPostCommentSubcommentTable.DOWNS, t.data.downs);
		values.put(RedditPostCommentSubcommentTable.UPS, t.data.ups);
		values.put(RedditPostCommentSubcommentTable.DOWNS, t.data.downs);
		values.put(RedditPostCommentSubcommentTable.BODY_HTML, t.data.body_html);

		return values;
	}
	
	@Override
	public void synchronize(Context context, List<RemoteRedditPostCommentDataReplyDataChild> items, Cursor localItems, int remoteIdentifierColumn) {
		// TODO Auto-generated method stub
		super.synchronize(context, items, localItems, remoteIdentifierColumn);
	}

}
