package com.example.imtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.VCard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserActivity extends Activity {

	private Button addFriends;
	private Button changeState;
	private TextView userState;
	private ExpandableListView userList;
	private Roster roster;
	private List<RosterGroup> mFriendsTypeList;
	private List<List<RosterEntry>> mFriendsMap;
	private MyAdapter ma;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_activity);
		roster = MyApplication.getConnection().getRoster();
		initView();
		initEvent();
		getFriends();
		
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == 0){
					ma = new MyAdapter(UserActivity.this,UserInfo.getmFriendsTypeList(),UserInfo.getmFriendsMap());
					userList.setAdapter(ma);
				}
			}
			
		};
	}

	private void initView() {

		
		addFriends = (Button)findViewById(R.id.button1);
		changeState = (Button)findViewById(R.id.button2);
		userState = (TextView)findViewById(R.id.textView1);
		userList = (ExpandableListView)findViewById(R.id.expandableListView1);
	}
	
	private void initEvent() {
		addFriends.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RelativeLayout rl = (RelativeLayout)LayoutInflater.from(UserActivity.this).inflate(R.layout.add_user_layout, null);
				final EditText et = (EditText)rl.findViewById(R.id.editText1);
				final EditText et2 = (EditText)rl.findViewById(R.id.editText2);
				AlertDialog alert1 = new AlertDialog.Builder(UserActivity.this).create();
				alert1.setTitle("添加好友");
				alert1.setView(rl);
				alert1.setButton(DialogInterface.BUTTON_POSITIVE,"确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								addUser(roster,et.getText().toString(),et2.getText().toString(),"group2");
														
							}
						}).start();
					}
				});
				alert1.setButton(DialogInterface.BUTTON_NEGATIVE,"取消",  new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				alert1.show();
			}
		});
		
		changeState.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ListView stateListView = new ListView(UserActivity.this);
				final String[] state = new String[6];
				state[0]="在线";
				state[1]="Q我吧";
				state[2]="忙碌";
				state[3]="离开";
				state[4]="隐身";
				state[5]="离线";
				ArrayAdapter<String> aa = new ArrayAdapter<String>(UserActivity.this, android.R.layout.simple_list_item_1, state);
				stateListView.setAdapter(aa);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
				builder.setTitle("请选择状态")
				.setView(stateListView)
				.create();
				
				final Dialog dialog = builder.show();
				
				stateListView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						setPresence(position);
						userState.setText(state[position]);
						dialog.dismiss();
					}
				});
			}
		});
		
		userList.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				String chat_to = mFriendsMap.get(groupPosition).get(childPosition).getUser().toString();
				//Log.d("tag", chat_to);
				Intent intent = new Intent(UserActivity.this,ChatActivity.class);
				intent.putExtra("user", chat_to);
				startActivity(intent);
				return true;
			}
		});
	}

	public boolean addUser(Roster roster, String userName, String name,  
            String groupName) {  
        try {  
            roster.createEntry(userName, name, new String[] { groupName });
            return true;  
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;  
        }  
    }  
    
	public void getFriends() {
		//Log.d("tag", "application--getFriends");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mFriendsTypeList = new ArrayList<RosterGroup>();
				mFriendsTypeList = getGroups(MyApplication.getConnection().getRoster());
				UserInfo.setmFriendsTypeList(mFriendsTypeList);
				
				mFriendsMap = new ArrayList<List<RosterEntry>>();
				List<RosterEntry> item = new ArrayList<RosterEntry>();
				for(int i = 0;i<mFriendsTypeList.size();i++){
					item = getEntriesByGroup(roster,mFriendsTypeList.get(i).getName());
					mFriendsMap.add(item);
				}
				UserInfo.setmFriendsMap(mFriendsMap);
				
				mHandler.sendEmptyMessage(0);
			}
		}).start();
		
		//Log.d("tag", mFriendsList.get(0).getName());
		
	}
	

	
	
    public List<RosterEntry> getAllEntries(Roster roster) {  
        List<RosterEntry> Entrieslist = new ArrayList<RosterEntry>();  
        Collection<RosterEntry> rosterEntry = roster.getEntries();  
        Iterator<RosterEntry> i = rosterEntry.iterator();  
        while (i.hasNext()) {  
            Entrieslist.add(i.next());  
        }  
        return Entrieslist;  
    }  
    
    public List<RosterGroup> getGroups(Roster roster) {  
        List<RosterGroup> grouplist = new ArrayList<RosterGroup>();  
        Collection<RosterGroup> rosterGroup = roster.getGroups();  
        Iterator<RosterGroup> i = rosterGroup.iterator();  
        while (i.hasNext()) {  
            grouplist.add(i.next());  
        }  
        return grouplist;  
    }  

	public List<RosterEntry> getEntriesByGroup(Roster roster,  
            String groupName) {  
        List<RosterEntry> Entrieslist = new ArrayList<RosterEntry>();  
        RosterGroup rosterGroup = roster.getGroup(groupName);  
        Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();  
        Iterator<RosterEntry> i = rosterEntry.iterator();  
        while (i.hasNext()) {  
            Entrieslist.add(i.next());  
        }  
        return Entrieslist;  
    }  
	
    public void setPresence(int code) {  
        if (MyApplication.getConnection() == null)  
            return;  
        Presence presence;  
        switch (code) {  
            case 0:  
                presence = new Presence(Presence.Type.available);  
                MyApplication.getConnection().sendPacket(presence);  
                Log.v("state", "设置在线");  
                break;  
            case 1:  
                presence = new Presence(Presence.Type.available);  
                presence.setMode(Presence.Mode.chat);  
                MyApplication.getConnection().sendPacket(presence);  
                Log.v("state", "设置Q我吧");  
                System.out.println(presence.toXML());  
                break;  
            case 2:  
                presence = new Presence(Presence.Type.available);  
                presence.setMode(Presence.Mode.dnd);  
                MyApplication.getConnection().sendPacket(presence);  
                Log.v("state", "设置忙碌");  
                System.out.println(presence.toXML());  
                break;  
            case 3:  
                presence = new Presence(Presence.Type.available);  
                presence.setMode(Presence.Mode.away);  
                MyApplication.getConnection().sendPacket(presence);  
                Log.v("state", "设置离开");  
                System.out.println(presence.toXML());  
                break;  
            case 4:  
                Roster roster = MyApplication.getConnection().getRoster();  
                Collection<RosterEntry> entries = roster.getEntries();  
                for (RosterEntry entry : entries) {  
                    presence = new Presence(Presence.Type.unavailable);  
                    presence.setPacketID(Packet.ID_NOT_AVAILABLE);  
                    presence.setFrom(MyApplication.getConnection().getUser());  
                    presence.setTo(entry.getUser());  
                    MyApplication.getConnection().sendPacket(presence);  
                    //System.out.println(presence.toXML());  
                }  
                // 向同一用户的其他客户端发送隐身状态  
                presence = new Presence(Presence.Type.unavailable);  
                presence.setPacketID(Packet.ID_NOT_AVAILABLE);  
                presence.setFrom(MyApplication.getConnection().getUser());  
                presence.setTo(StringUtils.parseBareAddress(MyApplication.getConnection().getUser()));  
                MyApplication.getConnection().sendPacket(presence);  
                Log.v("state", "设置隐身");  
                break;  
            case 5:  
                presence = new Presence(Presence.Type.unavailable);  
                MyApplication.getConnection().sendPacket(presence);  
                Log.v("state", "设置离线");  
                break;  
            default:  
                break;  
            }  
        }  
}
