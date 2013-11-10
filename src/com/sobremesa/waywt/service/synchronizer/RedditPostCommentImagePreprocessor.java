package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.RedditPostCommentService;
import com.sobremesa.waywt.service.RedditPostCommentService.RemoteImage;


public class RedditPostCommentImagePreprocessor extends RemotePreProcessor<RedditPostCommentService.RemoteImage> {

    public RedditPostCommentImagePreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteImage> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
