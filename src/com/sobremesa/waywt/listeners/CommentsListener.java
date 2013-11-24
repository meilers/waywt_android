package com.sobremesa.waywt.listeners;

import java.util.ArrayList;
import java.util.List;

import com.sobremesa.waywt.model.ThingInfo;



public interface CommentsListener {
	public void enableLoadingScreen();
	public void resetUI();
	public void updateComments(List<ThingInfo> comments);
}

