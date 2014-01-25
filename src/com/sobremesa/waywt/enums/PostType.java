package com.sobremesa.waywt.enums;

import java.util.HashMap;

public enum PostType {
	INVALID(0), WAYWT(1), OUTFIT_FEEDBACK(2), RECENT_PURCHASES(3);

	private static final HashMap<Integer, PostType> idToTypeMap = new HashMap<Integer, PostType>();
	static {
		for (PostType type : values()) {
			idToTypeMap.put(type.getId(), type);
		}
	}
	
	private int mId;

	private PostType(int id) {
		mId = id;
	}
	
	public int getId() {
		return mId;
	}
	
	public String getIdString() {
		return Integer.toString(mId);
	}
	
}
