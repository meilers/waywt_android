package com.sobremesa.waywt.model;

public class MyPost {
	private ThingInfo mComment;
	private String mThreadId;
	
	public MyPost(ThingInfo comment, String threadId )
	{
		this.mComment = comment;
		this.mThreadId = threadId;
	}
	
	public ThingInfo getComment()
	{
		return mComment;
	}
	
	public String getThreadId()
	{
		return mThreadId;
	}
}
