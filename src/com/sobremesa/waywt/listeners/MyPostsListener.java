package com.sobremesa.waywt.listeners;

import java.util.ArrayList;
import java.util.List;

import com.sobremesa.waywt.model.ThingInfo;



public interface MyPostsListener {
	public void onSuccess(List<ThingInfo> posts);
}

