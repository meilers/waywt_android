package com.sobremesa.waywt.listeners;

import java.util.ArrayList;
import java.util.List;

import com.sobremesa.waywt.model.MyPost;
import com.sobremesa.waywt.model.ThingInfo;



public interface MyPostsListener {
	public void enableLoadingScreen();
	public void resetUI();
	public void updateMyPosts(List<MyPost> myPosts);
}

