package com.example.imtest;

import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import com.example.imtest.R.drawable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends BaseExpandableListAdapter {

	private Activity mContext;
	private List<RosterGroup> mFriendsTypeList;
	private List<List<RosterEntry>> mFriendsMap;
	
	public MyAdapter(Activity mContext,List<RosterGroup> mFriendsTypeList,List<List<RosterEntry>> mFriendsMap){
		this.mContext = mContext;
		this.mFriendsTypeList = mFriendsTypeList;
		this.mFriendsMap = mFriendsMap;
	}

	@Override
	public int getGroupCount() {
		return mFriendsTypeList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mFriendsMap.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mFriendsTypeList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mFriendsMap.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		FriendTypeViewHolder ftvh;
		if(convertView == null){
			ftvh = new FriendTypeViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_type_list_item, null);
			ftvh.friend_type_tv = (TextView)convertView.findViewById(R.id.textView1);
			convertView.setTag(ftvh);
		}else{
			ftvh = (FriendTypeViewHolder)convertView.getTag();
		}
		ftvh.friend_type_tv.setText(mFriendsTypeList.get(groupPosition).getName());
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		FriendViewHolder fvh;
		if(convertView == null){
			fvh = new FriendViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.friend_list_item, null);
			fvh.friend_tv = (TextView)convertView.findViewById(R.id.textView1);
			fvh.friend_iv = (ImageView)convertView.findViewById(R.id.imageView1);
			convertView.setTag(fvh);
		}else{
			fvh = (FriendViewHolder)convertView.getTag();
		}
		//System.out.println(mFriendsMap.get(groupPosition).size());
		fvh.friend_tv.setText(mFriendsMap.get(groupPosition).get(childPosition).getName());
		try {
			fvh.friend_iv.setBackground(getUserVCard(MyApplication.getConnection(),mFriendsMap.get(groupPosition).get(childPosition).getUser()));
		} catch (XMPPException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	public static class FriendViewHolder{
		public TextView friend_tv;
		public ImageView friend_iv;
	}
	
	public static class FriendTypeViewHolder{
		public TextView friend_type_tv;
	}
	
	public Drawable getUserVCard(XMPPConnection connection, String user) throws XMPPException  
    {  
        VCard vcard = new VCard();  
        Log.d("tag", user + "@" + MyApplication.getConnection().getServiceName());
        vcard.load(connection, user + "@" + MyApplication.getConnection().getServiceName());
        
        if(vcard == null || vcard.getAvatar() == null){
        	//Log.d("tag", "null");
        	Drawable db1 = mContext.getResources().getDrawable(R.drawable.ic_launcher);
        	return db1;
        }else{
        	//Log.d("tag", "not null");
            Bitmap touxiang = Bytes2Bimap(vcard.getAvatar());
            BitmapDrawable db = new BitmapDrawable(mContext.getResources(), touxiang);
            return db;	
        }

    } 
	
	public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
