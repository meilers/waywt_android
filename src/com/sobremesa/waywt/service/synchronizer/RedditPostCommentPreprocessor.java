package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteRedditPostComment;


public class RedditPostCommentPreprocessor extends RemotePreProcessor<RedditPostCommentService.RemoteRedditPostComment> {

    public RedditPostCommentPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteRedditPostComment> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
