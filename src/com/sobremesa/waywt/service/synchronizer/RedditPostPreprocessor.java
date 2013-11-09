package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.RedditPostService;
import com.sobremesa.waywt.service.RedditPostService.RemoteRedditPost;
import com.sobremesa.waywt.service.RedditPostService.RemoteRedditPostData;


public class RedditPostPreprocessor extends RemotePreProcessor<RedditPostService.RemoteRedditPost> {

    public RedditPostPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteRedditPost> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
