package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.PostService;
import com.sobremesa.waywt.service.PostService.RemoteRedditPost;
import com.sobremesa.waywt.service.PostService.RemoteRedditPostData;


public class PostPreprocessor extends RemotePreProcessor<PostService.RemoteRedditPost> {

    public PostPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteRedditPost> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
