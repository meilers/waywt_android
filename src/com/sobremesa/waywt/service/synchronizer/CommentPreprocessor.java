package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.CommentService;
import com.sobremesa.waywt.service.CommentService.RemoteRedditPostComment;


public class CommentPreprocessor extends RemotePreProcessor<CommentService.RemoteRedditPostComment> {

    public CommentPreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteRedditPostComment> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
