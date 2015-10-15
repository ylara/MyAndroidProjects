package com.example.imtest;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import com.example.imtest.ChatMessage.Type;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChatActivity extends Activity {

	private static final int RESULT_LOAD_IMAGE = 1;
	private ListView ChatListView;
	private EditText message;
	private Button send;
	private Button send_file;
	private ImageView send_image;
	private Map<String, Chat> chatManage = new HashMap<String, Chat>();
	private String chat_to_string;
	private static Handler mHandler;
	private static String receive_msg;
	private static String receive_user;
	private List<ChatMessage> mDatas;
	private ChatMessageAdapter cma;
	private Map<String, List<HashMap<String, String>>> offlineMsg;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_chatting);
		chat_to_string = getIntent().getStringExtra("user");
		mDatas = new ArrayList<ChatMessage>();
		initView();
		initEvent();
		getOffLineMsg();
		
		mHandler = new Handler(){

			@Override
			public void handleMessage(android.os.Message msg) {
				super.handleMessage(msg);
				if(msg.what == 0){
					ChatMessage from = (ChatMessage) msg.obj;
					//Log.d("from.getName()",from.getName());
					//Log.d("chat_to_string", chat_to_string);
					if(from.getName().equals(chat_to_string)){
						//Log.d("tag", "same");
						mDatas.add(from);
						cma.notifyDataSetChanged();
						ChatListView.setSelection(mDatas.size() - 1);
					}else{
						
					}
				}
			}
			
		};
	}

	private void getOffLineMsg() {
		offlineMsg = MyApplication.getOfflineMsg();
		if(offlineMsg.containsKey(chat_to_string)){
			int count = offlineMsg.get(chat_to_string).size();
			for(int i = 0;i<count;i++){
				ChatMessage from = new ChatMessage();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				try {
					from.setDate(df.parse(offlineMsg.get(chat_to_string).get(i).get("date")));
				} catch (ParseException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				from.setName(chat_to_string);
				from.setType(Type.INPUT);
				from.setMsg(offlineMsg.get(chat_to_string).get(i).get("info"));
				mDatas.add(from);
				cma.notifyDataSetChanged();
				ChatListView.setSelection(mDatas.size() - 1);
			}
		}
	}

	private void initView() {
		ChatListView = (ListView)findViewById(R.id.id_chat_listView);
		cma = new ChatMessageAdapter(ChatActivity.this, mDatas,ChatActivity.this);
		ChatListView.setAdapter(cma);
		
		message = (EditText)findViewById(R.id.id_chat_msg);
		send = (Button)findViewById(R.id.id_chat_send);
		send_image = (ImageView)findViewById(R.id.chat_send_content_image);
		send_file = (Button)findViewById(R.id.id_chat_send_file);
	}
	
	private void initEvent() {
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String msg = message.getText().toString();
				ChatMessage to = new ChatMessage(Type.OUTPUT, msg);
				to.setDate(new Date());
				to.setName(StringUtils.parseName(MyApplication.getConnection().getUser().toString()));
				mDatas.add(to);
				cma.notifyDataSetChanged();
				ChatListView.setSelection(mDatas.size() - 1);
				message.setText("");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						Chat chat = getFriendChat(chat_to_string,null);  
						try {   
						    chat.sendMessage(msg);  
						} catch (XMPPException e) {  
						    e.printStackTrace();  
						}  						
					}
				}).start();
			}
		});
		
		send_file.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
/*	            Log.d("JID", chat_to_string + "@"+  
					    		MyApplication.getConnection().getServiceName());*/
				 Intent i = new Intent(
	                        Intent.ACTION_PICK,
	                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	  
	             startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});
	}
	
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	  
	        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	            Uri selectedImage = data.getData();
	            //Log.d("uri", selectedImage.toString());
	            String[] filePathColumn = { MediaStore.Images.Media.DATA };
	  
	            Cursor cursor = getContentResolver().query(selectedImage,
	                    filePathColumn, null, null, null);
	            cursor.moveToFirst();
	  
	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	            final String picturePath = cursor.getString(columnIndex);
	            cursor.close();
	            
				String JId = chat_to_string + "@"+  
					    		MyApplication.getConnection().getServiceName() + "/Spark 2.6.3";
				sendFile(JId,picturePath);
				
				ChatMessage cm_image = new ChatMessage();
				cm_image.setMsg(picturePath);
				cm_image.setDate(new Date());
				cm_image.setType(Type.OUTPUT_IMAGE);
				cm_image.setName(MyApplication.getConnection().getUser().toString());	
				mDatas.add(cm_image);
				cma.notifyDataSetChanged();
				ChatListView.setSelection(mDatas.size() - 1);
	            //Log.d("path", picturePath);
	        }
	  
	    }
	 
	public Chat getFriendChat(String friend, MessageListener listenter) {  
	    if(MyApplication.getConnection()==null)  
	        return null;  
	    /** 判断是否创建聊天窗口 */  
	    for (String fristr : chatManage.keySet()) {  
	        if (fristr.equals(friend)) {  
	            // 存在聊天窗口，则返回对应聊天窗口  
	            return chatManage.get(fristr);  
	        }  
	    }  
	    /** 创建聊天窗口 */  
	    Chat chat = MyApplication.getConnection().getChatManager().createChat(friend + "@"+  
	    		MyApplication.getConnection().getServiceName(), listenter);  
	    /** 添加聊天窗口到chatManage */  
	    chatManage.put(friend, chat);  
	    return chat;  
	}  
	
	public static class TaxiChatManagerListener implements ChatManagerListener {

		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			chat.addMessageListener(new MessageListener() {  
				@Override
				public void processMessage(Chat chat, Message msg) {
	                //登录用户  
	                StringUtils.parseName(MyApplication.getConnection().getUser());  
	                //发送消息用户  
	                receive_user = msg.getFrom();  
	                //消息内容  
	                receive_msg = msg.getBody();  	
	                if(receive_msg.equals("")){
	                	
	                }else{
	                	ChatMessage cm = new ChatMessage();
		                cm.setMsg(receive_msg);
		                cm.setName(StringUtils.parseName(receive_user));
		                cm.setType(Type.INPUT);
		                cm.setDate(new Date());

		                android.os.Message message = new android.os.Message();
		                message.obj = cm;
		                message.what = 0;
		                mHandler.sendMessage(message);
	                }

				}  
	        });  
	    }  
	}  
	
	public static class RecFileTransferListener implements FileTransferListener {

		@Override
		public void fileTransferRequest(final FileTransferRequest request) {
			Log.d("tag", String.valueOf(request.getFileSize()));
			final IncomingFileTransfer transfer = request.accept();
			final File file = new File( Environment.getExternalStorageDirectory().getPath() + "/" + request.getFileName());
			try {  
                if (!file.exists()){  
                    //Log.i("have no file",file.getPath());  
                    file.createNewFile();  
                }  
                //Log.d("file", file.getAbsolutePath().toString());
                transfer.recieveFile(file);  
                
                new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(true){
		                	if(transfer.isDone()){
		                		ChatMessage cm_image = new ChatMessage();
		        				cm_image.setMsg(file.getAbsolutePath());
		        				cm_image.setDate(new Date());
		        				cm_image.setType(Type.INPUT_IMAGE);
		        				cm_image.setName(StringUtils.parseName(request.getRequestor()));
		        				
		                        android.os.Message message = new android.os.Message();
		                        message.obj = cm_image;
		                        message.what = 0;
		                        mHandler.sendMessage(message);
		                        break;
		                	}
		                }						
					}
				}).start();
            }catch (Exception e) {  
                e.printStackTrace();  
            }  
		}
	}
		

	
	public void sendFile(String user, String filePath) {  
        if (MyApplication.getConnection() == null)  
            return;  
        // 创建文件传输管理器  
        FileTransferManager manager = new FileTransferManager(MyApplication.getConnection());  
  
        // 创建输出的文件传输  
        OutgoingFileTransfer transfer = manager  
                .createOutgoingFileTransfer(user);  
  
        // 发送文件  
        try {  
        	//Log.d("tag", "before transfer");
            transfer.sendFile(new File(filePath), "You won't believe this!"); 
            //Log.d("tag", "after transfer");
        } catch (XMPPException e) {  
            e.printStackTrace();  
        }  
    } 
}
