package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.model.ThingInfo;
import com.sobremesa.waywt.service.PostService;
import com.sobremesa.waywt.service.PostService.RemoteRedditPost;
import com.sobremesa.waywt.service.PostService.RemoteRedditPostData;


public class CommentPreprocessor extends RemotePreProcessor<ThingInfo> {

    public CommentPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<ThingInfo> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
