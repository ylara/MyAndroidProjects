package com.example.imtest;

import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;

import android.util.SparseArray;

public class UserInfo {

	
	public static List<RosterGroup> mFriendsTypeList;

	public static List<List<RosterEntry>> mFriendsMap;
	
	
	public static List<List<RosterEntry>> getmFriendsMap() {
		return mFriendsMap;
	}

	public static void setmFriendsMap(List<List<RosterEntry>> mFriendsMap) {
		UserInfo.mFriendsMap = mFriendsMap;
	}

	public static List<RosterGroup> getmFriendsTypeList() {
		return mFriendsTypeList;
	}

	public static void setmFriendsTypeList(List<RosterGroup> mFriendsTypeList) {
		UserInfo.mFriendsTypeList = mFriendsTypeList;
	}

	
	
}
