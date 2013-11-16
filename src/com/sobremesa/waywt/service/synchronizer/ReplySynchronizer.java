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
import com.sobremesa.waywt.database.tables.ReplyTable;
import com.sobremesa.waywt.service.CommentService;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostComment;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentDataRepliesDataChild;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ReplySynchronizer extends Synchronizer<CommentService.RemoteRedditPostCommentDataRepliesDataChild> {

	
	
	public ReplySynchronizer(Context context) {
		super(context);
	}

	@Override
	protected void performSynchronizationOperations(Context context, List<RemoteRedditPostCommentDataRepliesDataChild> inserts, List<RemoteRedditPostCommentDataRepliesDataChild> updates, List<Long> deletions) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (CommentService.RemoteRedditPostCommentDataRepliesDataChild w : inserts) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newInsert(Provider.REPLY_CONTENT_URI).withValues(values).build();
			operations.add(op);
			
			
		}

		for (CommentService.RemoteRedditPostCommentDataRepliesDataChild w : updates) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(Provider.REPLY_CONTENT_URI).withSelection(ReplyTable.IDENTIFIER + " = ?", new String[] { w.data.getIdentifier() })
					.withValues(values).build();
			operations.add(op);
			
		}

		for (Long id : deletions) {
			ContentProviderOperation op = ContentProviderOperation.newDelete(Provider.REPLY_CONTENT_URI).withSelection(ReplyTable.ID + " = ? " , new String[] { String.valueOf(id) }).build();
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
	protected boolean isRemoteEntityNewerThanLocal(RemoteRedditPostCommentDataRepliesDataChild remote, Cursor c) {
		// there isn't a versioning mechanism on the service resources so always
		// consider the remote copy new
		return true;
	}

	@Override
	protected ContentValues getContentValuesForRemoteEntity(RemoteRedditPostCommentDataRepliesDataChild t) {
		ContentValues values = new ContentValues();
		
		values.put(ReplyTable.POST_ID, t.data.postId);
		values.put(ReplyTable.COMMENT_ID, t.data.commentId);
		values.put(ReplyTable.PARENTIDENTIFIER, t.data.parentReplyIdentifier);
		
		values.put(ReplyTable.IDENTIFIER, t.data.getIdentifier());
		values.put(ReplyTable.AUTHOR, t.data.author);
		values.put(ReplyTable.CREATED, t.data.created);
		values.put(ReplyTable.DOWNS, t.data.downs);
		values.put(ReplyTable.UPS, t.data.ups);
		values.put(ReplyTable.DOWNS, t.data.downs);
		values.put(ReplyTable.BODY_HTML, t.data.body_html);

		return values;
	}
	
	@Override
	public void synchronize(Context context, List<RemoteRedditPostCommentDataRepliesDataChild> items, Cursor localItems, int remoteIdentifierColumn) {
		// TODO Auto-generated method stub
		super.synchronize(context, items, localItems, remoteIdentifierColumn);
	}

}
