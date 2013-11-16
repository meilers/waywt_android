package com.sobremesa.waywt.service.synchronizer;

import java.util.List;

import com.sobremesa.waywt.service.CommentService;
import com.sobremesa.waywt.service.CommentService.RemoteImage;


public class ImagePreprocessor extends RemotePreProcessor<CommentService.RemoteImage> {

    public ImagePreprocessor() {
	
    }

    @Override
    public void preProcessRemoteRecords(List<RemoteImage> records) {
//	for (RemoteRedditPostData rep : records) {
//	    rep.zip = mZip;
//	    rep.uniqueId = rep.name + "_" + mZip;
//	}
    }

}
