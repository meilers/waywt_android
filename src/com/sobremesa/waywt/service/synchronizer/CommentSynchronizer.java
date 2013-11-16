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
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.service.CommentService;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostComment;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class CommentSynchronizer extends Synchronizer<CommentService.RemoteRedditPostComment> {

	public CommentSynchronizer(Context context) {
		super(context);
	}

	@Override
	protected void performSynchronizationOperations(Context context, List<RemoteRedditPostComment> inserts, List<RemoteRedditPostComment> updates, List<Long> deletions) {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		for (CommentService.RemoteRedditPostComment w : inserts) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newInsert(Provider.COMMENT_CONTENT_URI).withValues(values).build();
			operations.add(op);
			
			
		}

		for (CommentService.RemoteRedditPostComment w : updates) {
			ContentValues values = this.getContentValuesForRemoteEntity(w);
			ContentProviderOperation op = ContentProviderOperation.newUpdate(Provider.COMMENT_CONTENT_URI).withSelection(CommentTable.IDENTIFIER + " = ?", new String[] { w.data.getIdentifier() })
					.withValues(values).build();
			operations.add(op);
			
		}

		for (Long id : deletions) {
			ContentProviderOperation op = ContentProviderOperation.newDelete(Provider.COMMENT_CONTENT_URI).withSelection(CommentTable.ID + " = ? ", new String[] { String.valueOf(id) }).build();
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
		
		values.put(CommentTable.POST_ID, t.data.postId);
		values.put(CommentTable.IDENTIFIER, t.data.getIdentifier());
		
		values.put(CommentTable.AUTHOR, t.data.author);
		values.put(CommentTable.CREATED, t.data.created);
		values.put(CommentTable.DOWNS, t.data.downs);
		values.put(CommentTable.UPS, t.data.ups);
		values.put(CommentTable.DOWNS, t.data.downs);
		values.put(CommentTable.BODY_HTML, t.data.body_html);

		return values;
	}
	
	@Override
	public void synchronize(Context context, List<RemoteRedditPostComment> items, Cursor localItems, int remoteIdentifierColumn) {
		// TODO Auto-generated method stub
		super.synchronize(context, items, localItems, remoteIdentifierColumn);
	}

}
