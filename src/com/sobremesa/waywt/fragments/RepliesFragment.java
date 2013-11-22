package com.sobremesa.waywt.fragments;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;

import com.sobremesa.waywt.R;
import com.sobremesa.waywt.application.WaywtApplication;
import com.sobremesa.waywt.common.Constants;
import com.sobremesa.waywt.common.RedditIsFunHttpClientFactory;
import com.sobremesa.waywt.contentprovider.Provider;
import com.sobremesa.waywt.database.tables.CommentTable;
import com.sobremesa.waywt.database.tables.ImageTable;
import com.sobremesa.waywt.database.tables.ReplyTable;
import com.sobremesa.waywt.fragments.CommentFragment.Extras;
import com.sobremesa.waywt.model.Listing;
import com.sobremesa.waywt.model.ListingData;
import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.model.ThingListing;
import com.sobremesa.waywt.settings.RedditSettings;
import com.sobremesa.waywt.util.CollectionUtils;
import com.sobremesa.waywt.util.Util;
import com.sobremesa.waywt.views.AspectRatioImageView;
import com.sobremesa.waywt.views.WaywtSecondaryTextView;
import com.xtremelabs.imageutils.ImageLoader;
import com.xtremelabs.imageutils.ImageLoader.Options;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RepliesFragment extends Fragment {
	private static final String TAG = RepliesFragment.class.getSimpleName();
	
	public static class Extras
	{
		public static String ARG_COMMENT = "comment";
	}
	
	ThingInfo mComment = null;
    ArrayList<ThingInfo> mCommentsList = null;
    
    private final RedditSettings mSettings = new RedditSettings();
    private final HttpClient mClient = RedditIsFunHttpClientFactory.getGzipHttpClient();
	
    private int last_found_position = -1;
    private int mIndentation = 1;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mSettings.loadRedditPreferences(getActivity(), mClient);
		mCommentsList = new ArrayList<ThingInfo>();
		
		mComment = (ThingInfo)getArguments().get(Extras.ARG_COMMENT);
		
		int insertedCommentIndex = 0;
		
		mCommentsList.add(mComment);
		
		if( mComment.getReplies() != null && mComment.getReplies().getData() != null && mComment.getReplies().getData().getChildren() != null)
		{
			for (ThingListing commentThingListing : mComment.getReplies().getData().getChildren()) {
				// insert the comment and its replies, prefix traversal order
				insertedCommentIndex = insertNestedComment(commentThingListing, 0, insertedCommentIndex + 1);
			}
		}
		
//		mCommentsList.add(comment);
	}
	
	
	int insertNestedComment(ThingListing commentThingListing, int indentLevel, int insertedCommentIndex) {
		ThingInfo ci = commentThingListing.getData();
		
		// Add comment to deferred append/replace list
		deferCommentAppend(ci);
		
		if (ci.getBody_html() != null) {
        	CharSequence spanned = createSpanned(ci.getBody_html());
        	ci.setSpannedBody(spanned);
		}
		
		
		// Formatting that applies to all items, both real comments and "more" entries
		ci.setIndent(mIndentation + indentLevel);
		
		
		// Regular comment
		
		// Skip things that are not comments, which shouldn't happen
		if (!Constants.COMMENT_KIND.equals(commentThingListing.getKind())) {
			if (Constants.LOGGING) Log.e(TAG, "comment whose kind is \""+commentThingListing.getKind()+"\" (expected "+Constants.COMMENT_KIND+")");
			return insertedCommentIndex;
		}
		
		// handle the replies
		Listing repliesListing = ci.getReplies();
		if (repliesListing == null)
			return insertedCommentIndex;
		ListingData repliesListingData = repliesListing.getData();
		if (repliesListingData == null)
			return insertedCommentIndex;
		ThingListing[] replyThingListings = repliesListingData.getChildren();
		if (replyThingListings == null)
			return insertedCommentIndex;
		
		for (ThingListing replyThingListing : replyThingListings) {
			insertedCommentIndex = insertNestedComment(replyThingListing, indentLevel + 1, insertedCommentIndex + 1);
		}
		return insertedCommentIndex;
	}
	
	private void deferCommentAppend(ThingInfo comment) {
		
		if( comment != null )
		{
			String author = comment.getAuthor();
			String bodyHtml = comment.getBody_html();
			
			if( author != null && !author.isEmpty() && bodyHtml != null && !bodyHtml.isEmpty() )
				mCommentsList.add(comment);
		}
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.fragment_replies, null, false);
		
		
		
		for(int i=0; i< mCommentsList.size(); ++i)
		{
			View listItemView = getListItemView(i);
			view.addView(listItemView);
		}
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mSettings.loadRedditPreferences(getActivity(), mClient);
	}
	
	
	public View getListItemView(int position) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.comments_list_item, null);
        
        ThingInfo item = mCommentsList.get(position);
        
        
        try {

			// Sometimes (when in touch mode) the "selection" highlight disappears.
			// So we make our own persistent highlight. This background color must
			// be set explicitly on every element, however, or the "cached" list
			// item views will show up with the color.
			if (position == last_found_position)
				view.setBackgroundResource(R.color.translucent_yellow);
			else
				view.setBackgroundColor(Color.TRANSPARENT);

            fillCommentsListItemView(view, item, mSettings);
        } catch (NullPointerException e) {
        	if (Constants.LOGGING) Log.w(TAG, "NPE in getView()", e);
        	// Probably means that the List is still being built, and OP probably got put in wrong position
        	if (view == null) {
        		if (position == 0)
        			view = getActivity().getLayoutInflater().inflate(R.layout.threads_list_item, null);
        		else
        			view = getActivity().getLayoutInflater().inflate(R.layout.comments_list_item, null);
            }
        }
        return view;
	}
        

    
    public void fillCommentsListItemView(View view, ThingInfo item, RedditSettings settings) {
        // Set the values of the Views for the CommentsListItem
        
        TextView votesView = (TextView) view.findViewById(R.id.votes);
        TextView submitterView = (TextView) view.findViewById(R.id.submitter);
        TextView bodyView = (TextView) view.findViewById(R.id.body);
        
        TextView submissionTimeView = (TextView) view.findViewById(R.id.submissionTime);
        ImageView voteUpView = (ImageView) view.findViewById(R.id.vote_up_image);
        ImageView voteDownView = (ImageView) view.findViewById(R.id.vote_down_image);
        
        try {
        	votesView.setText(Util.showNumPoints(item.getUps() - item.getDowns()));
        } catch (NumberFormatException e) {
        	// This happens because "ups" comes after the potentially long "replies" object,
        	// so the ListView might try to display the View before "ups" in JSON has been parsed.
        	if (Constants.LOGGING) Log.e(TAG, "getView, normal comment", e);
        }
        if (item.getSSAuthor() != null)
        	submitterView.setText(item.getSSAuthor());
        else
        	submitterView.setText(item.getAuthor());
        submissionTimeView.setText(Util.getTimeAgo(item.getCreated_utc()));
        
    	if (item.getSpannedBody() != null)
    		bodyView.setText(item.getSpannedBody());
    	else
    		bodyView.setText(item.getBody());
        
    	bodyView.setMovementMethod(LinkMovementMethod.getInstance());
    	
        setCommentIndent(view, item.getIndent(), settings);
        
        if (voteUpView != null && voteDownView != null) {
	        if (item.getLikes() == null || "[deleted]".equals(item.getAuthor())) {
	        	voteUpView.setVisibility(View.GONE);
	        	voteDownView.setVisibility(View.GONE);
	    	}
	        else if (Boolean.TRUE.equals(item.getLikes())) {
	    		voteUpView.setVisibility(View.VISIBLE);
	    		voteDownView.setVisibility(View.GONE);
	    	}
	        else if (Boolean.FALSE.equals(item.getLikes())) {
	    		voteUpView.setVisibility(View.GONE);
	    		voteDownView.setVisibility(View.VISIBLE);
	    	}
        }
        
        if (item.getAuthor().equalsIgnoreCase(mComment.getAuthor()))
        	submitterView.setText(item.getAuthor() + " [S]");
    }
    
    public static void setCommentIndent(View commentListItemView, int indentLevel, RedditSettings settings) {
        View[] indentViews = new View[] {
        	commentListItemView.findViewById(R.id.left_indent1),
        	commentListItemView.findViewById(R.id.left_indent2),
        	commentListItemView.findViewById(R.id.left_indent3),
        	commentListItemView.findViewById(R.id.left_indent4),
        	commentListItemView.findViewById(R.id.left_indent5),
        	commentListItemView.findViewById(R.id.left_indent6),
        	commentListItemView.findViewById(R.id.left_indent7),
        	commentListItemView.findViewById(R.id.left_indent8)
        };
        for (int i = 0; i < indentLevel && i < indentViews.length; i++) {
        	if (settings.isShowCommentGuideLines()) {
            	indentViews[i].setVisibility(View.VISIBLE);
            	indentViews[i].setBackgroundResource(R.color.light_light_gray);
        	} else {
        		indentViews[i].setVisibility(View.INVISIBLE);
        	}
        }
        for (int i = indentLevel; i < indentViews.length; i++) {
        	indentViews[i].setVisibility(View.GONE);
        }
    }
    
    public ThingInfo getOpThingInfo() {
    	if (!CollectionUtils.isEmpty(mCommentsList))
    		return mCommentsList.get(0);
    	return null;
    }
    
    private CharSequence createSpanned(String bodyHtml) {
    	try {
    		// get unescaped HTML
    		bodyHtml = Html.fromHtml(bodyHtml).toString();
    		// fromHtml doesn't support all HTML tags. convert <code> and <pre>
    		bodyHtml = Util.convertHtmlTags(bodyHtml);
    		
    		Spanned body = Html.fromHtml(bodyHtml);
    		// remove last 2 newline characters
    		if (body.length() > 2)
    			return body.subSequence(0, body.length()-2);
    		else
    			return "";
    	} catch (Exception e) {
    		if (Constants.LOGGING) Log.e(TAG, "createSpanned failed", e);
    		return null;
    	}
    }
    
}
