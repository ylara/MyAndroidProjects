package com.example.imtest;


import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.packet.DelayInformation;

import com.example.imtest.ChatActivity.TaxiChatManagerListener;
import com.example.imtest.ChatActivity.RecFileTransferListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {

	private EditText user;
	private EditText password;
	private Button btn;
	private Button btn2;
	private String username;
	private String password_string;
	private CheckBox cb;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		
		initEvent();
		
		initSharedPreferences();
		//Log.d("tag", sp.getString("password", ""));
		user.setText(sp.getString("username", ""));
		password.setText(sp.getString("password", ""));
		if(sp.getBoolean("isChecked", false)){
			cb.setChecked(true);
		}else{
			cb.setChecked(false);
			password.setText("");
		}
	}

	private void initSharedPreferences() {
		sp = getSharedPreferences("login" , 1);
		editor = sp.edit();
	}

	private void initView() {
		user = (EditText)findViewById(R.id.editText1);
		password = (EditText)findViewById(R.id.editText2);
		btn = (Button)findViewById(R.id.button1);
		btn2 = (Button)findViewById(R.id.button2);
		cb = (CheckBox)findViewById(R.id.checkBox1);
	}

	private void initEvent() {
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				username = user.getText().toString();
				password_string = password.getText().toString();
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(MyApplication.conServer()){
							regist(username,password_string);
						}
					}
				}).start();
			}
		});
		
		btn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				username = user.getText().toString();
				password_string = password.getText().toString();
				editor.putString("username", username);
				if(cb.isChecked()){
					editor.putString("password", password_string);
				}
				editor.commit();
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(MyApplication.conServer()){
							login(username, password_string);
							if(MyApplication.getConnection().isAuthenticated()){
								Intent intent = new Intent(MainActivity.this,UserActivity.class);
								startActivity(intent);
							}else{
								Looper.prepare();
								Toast.makeText(MainActivity.this, "login failed,wrong username or password.", Toast.LENGTH_SHORT).show();
								Looper.loop();
							}
						}
					}
				}).start();
			}
		});	
		
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					editor.putBoolean("isChecked", true);
				}else{
					editor.putBoolean("isChecked", false);
				}
				editor.commit();
			}
		});
	}
	
	

	
	public String regist(String account, String password){  
	    if (MyApplication.getConnection() == null)  
	        return "0";  
	    Registration reg = new Registration();  
	    reg.setType(IQ.Type.SET);  
	    reg.setTo(MyApplication.getConnection().getServiceName());  
	    reg.setUsername(account);// 注意这里createAccount注册时，参数是username，不是jid，是“@”前面的部分。  
	    reg.setPassword(password);  
	    reg.addAttribute("android", "geolo_createUser_android");// 这边addAttribute不能为空，否则出错。所以做个标志是android手机创建的吧！！！！！  
	    PacketFilter filter = new AndFilter(new PacketIDFilter(  
	            reg.getPacketID()), new PacketTypeFilter(IQ.class));  
	    PacketCollector collector = MyApplication.getConnection()  
	            .createPacketCollector(filter);  
	    MyApplication.getConnection().sendPacket(reg);  
	    IQ result = (IQ) collector.nextResult(SmackConfiguration  
	            .getPacketReplyTimeout());  
	    // Stop queuing results  
	    collector.cancel();// 停止请求results（是否成功的结果）  
	    if (result == null) {  
	        Log.e("RegistActivity", "No response from server.");  
	        return "0";  
	    } else if (result.getType() == IQ.Type.RESULT) {  
	    	Log.d("RegistActivity", "register OK");
	        return "1";  
	    } else { // if (result.getType() == IQ.Type.ERROR)  
	        if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {  
	            Log.e("RegistActivity", "IQ.Type.ERROR: "  
	                    + result.getError().toString());  
	            return "2";  
	        } else {  
	            Log.e("RegistActivity", "IQ.Type.ERROR: "  
	                    + result.getError().toString());  
	            return "3";  
	        }  
	    }  
	}  
	
	public boolean login(String a, String p) {  
	    try {  
	        if (MyApplication.getConnection() == null)  
	            return false;  
	        /** 登录 */             
	        MyApplication.getConnection().login(a, p);  
	        getHisMessage();
	        TaxiChatManagerListener chatManagerListener = new TaxiChatManagerListener();
	        MyApplication.getConnection().getChatManager().addChatListener(chatManagerListener);
	        FileTransferManager transfer = new FileTransferManager(MyApplication.getConnection());
	        transfer.addFileTransferListener(new RecFileTransferListener());
	        PacketFilter filter = new AndFilter(new PacketTypeFilter(  
                    Presence.class));  
	        MyApplication.getConnection().addPacketListener(new PacketListener() {
				
				@Override
				public void processPacket(Packet packet) {
					Presence p = (Presence)packet;
					//Log.d("tag", p.getFrom());
					//Log.d("tag", p.getMode().toString());
				}
			}, filter);
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	    return false;  
	} 
	
	public Map<String, List<HashMap<String, String>>> getHisMessage() {  
        if (MyApplication.getConnection() == null)  
            return null;  
        Map<String, List<HashMap<String, String>>> offlineMsgs = null;  
  
        try {  
            OfflineMessageManager offlineManager = new OfflineMessageManager(  
            		MyApplication.getConnection());  
            Iterator<Message> it = offlineManager.getMessages();  
  
            int count = offlineManager.getMessageCount();  
            Log.d("count", String.valueOf(count));
            if (count <= 0)  {
            	 Presence presence = new Presence(Presence.Type.available);
                 MyApplication.getConnection().sendPacket(presence);
                 return null;  
            }
            offlineMsgs = new HashMap<String, List<HashMap<String, String>>>();  
  
            while (it.hasNext()) {  
                Message message = it.next();  
                Date date = null;
                DelayInformation  info = (DelayInformation)message.getExtension("x","jabber:x:delay");
				if (info != null) {
					 date = info.getStamp();
					 
					//Log.e("离线消息", "收到离线消息, 时间："+date.getTime());
				}
                String fromUser = StringUtils.parseName(message.getFrom());   
                HashMap<String, String> histrory = new HashMap<String, String>(); 
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
                histrory.put("date",df.format(date));
                Log.d("date", df.format(date));
                histrory.put("useraccount",  
                        StringUtils.parseName(MyApplication.getConnection().getUser()));  
                histrory.put("friendaccount", fromUser);  
                histrory.put("info", message.getBody());  
                histrory.put("type", "left");  
                if (offlineMsgs.containsKey(fromUser)) {  
                    offlineMsgs.get(fromUser).add(histrory);  
                } else {  
                    List<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();  
                    temp.add(histrory);  
                    offlineMsgs.put(fromUser, temp);  
                }  
            }  
            offlineManager.deleteMessages();  
            Presence presence = new Presence(Presence.Type.available);
            MyApplication.getConnection().sendPacket(presence);
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        MyApplication.setOfflineMsg(offlineMsgs);
        return offlineMsgs;  
    }  
}
