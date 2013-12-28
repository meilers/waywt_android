package com.sobremesa.waywt.enums;

import java.util.HashMap;

public enum SortByType {
	RANDOM(0), UPVOTES(1), MOST_RECENT(2), NONE(3);

	private static final HashMap<Integer, SortByType> idToTypeMap = new HashMap<Integer, SortByType>();
	static {
		for (SortByType type : values()) {
			idToTypeMap.put(type.getId(), type);
		}
	}
	
	private int mId;

	private SortByType(int id) {
		mId = id;
	}
	
	public int getId() {
		return mId;
	}
	
	public String getIdString() {
		return Integer.toString(mId);
	}
	
}
