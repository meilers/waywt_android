package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.CommentService;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostComment;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostCommentDataRepliesDataChild;


public class ReplyPreprocessor extends RemotePreProcessor<CommentService.RemoteRedditPostCommentDataRepliesDataChild> {

    public ReplyPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteRedditPostCommentDataRepliesDataChild> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
