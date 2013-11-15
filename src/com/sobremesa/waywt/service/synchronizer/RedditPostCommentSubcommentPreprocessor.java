package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostComment;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChild;


public class RedditPostCommentSubcommentPreprocessor extends RemotePreProcessor<RedditPostCommentService.RemoteRedditPostCommentDataReplyDataChild> {

    public RedditPostCommentSubcommentPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteRedditPostCommentDataReplyDataChild> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
