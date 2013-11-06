package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.RedditPostService;
import com.sobremesa.waywt.service.RedditPostService.RemoteRedditPostData;


public class RedditPostPreprocessor extends RemotePreProcessor<RedditPostService.RemoteRedditPostData> {

    public RedditPostPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteRedditPostData> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
