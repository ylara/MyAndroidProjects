package com.kz.activity;



import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.kz.Adapter.HowToPlayListItemAdapter;
import com.kz.Adapter.HowToPlayListItemAdapter.ViewHolder;
import com.kz.Adapter.SortAdapter;
import com.kz.Service.PlayMusicService;
import com.kz.Util.MediaUtil;
import com.kz.View.CharacterParser;
import com.kz.View.ClearEditText;
import com.kz.View.PinyinComparator;
import com.kz.View.RefreshableView;
import com.kz.View.RefreshableView.PullToRefreshListener;
import com.kz.View.SideBar;
import com.kz.View.SideBar.OnTouchingLetterChangedListener;
import com.kz.domin.MusicSituation;
import com.kz.domin.SortModel;

public class MainActivity extends Activity {
	public static final int NOTIFICATION_ID = 0x123;
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private RefreshableView refreshableView;
	private SortAdapter adapter;
	private ClearEditText mClearEditText;
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;
	private PinyinComparator pinyinComparator;
	private TextView song_name;
	private Button btn;
	private boolean isplaying;
	private boolean ispause;
	private boolean isFirstClickItem = true;
	private int ItemPosition;
	private NotificationManager nm;
	private HomeReceiver homeReceiver;
	private RemoteViews contentView;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private HowToPlayListItemAdapter mHowToPlayListItemAdapter;
	private boolean isExit = false;
	
    private  Handler mHandler = new Handler() { 
    	   
        @Override 
        public void handleMessage(Message msg) { 
            if(msg.what == 0){
                isExit = false; 	
            }
        } 
    }; 
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		initData();
		initViews();
		initBroadcast();
		initPreferences();
		this.getDrawable(R.drawable.arrow);
	}
	@Override
	protected void onDestroy() {
		unregisterReceiver(homeReceiver);
    	nm.cancel(NOTIFICATION_ID);
		super.onDestroy();
	}
	
	private void initPreferences(){
		preferences = getSharedPreferences("MyMusicPlayer" , 1);
		editor = preferences.edit();
	}
	
	private void initBroadcast(){
		homeReceiver = new HomeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("UPDATE_CURRENT");
		filter.addAction("PLAY OR STOP");
		filter.addAction("FINISH_ACTIVITY");
		filter.addAction("PLAY_OR_PAUSE");
		registerReceiver(homeReceiver, filter);
	}
	private void initData(){
		SourceDateList = MediaUtil.getMp3Infos(MainActivity.this);
		isplaying = false;
		ispause = true;
	}
	private void initViews() {
		contentView = new RemoteViews(MainActivity.this.getPackageName(),
				R.layout.notification_contentview);
		song_name = (TextView)findViewById(R.id.main_activity_layout_textView1);
		refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
		refreshableView.setOnRefreshListener(new PullToRefreshListener() {
			@Override
			public void onRefresh() {
				Intent intent = new Intent(MainActivity.this,PlayMusicService.class);
				Bundle bundle = new Bundle();
				bundle.putInt("situation", MusicSituation.PULL_TO_REFRESH);
				intent.putExtras(bundle);
				startService(intent);
				refreshableView.finishRefreshing();
			}

			@Override
			public void ReFreshed() {
				btn.setBackground(getResources().getDrawable(R.drawable.pause));
				isplaying = false;
				ispause = true;
				Toast.makeText(MainActivity.this, "���Ÿ���" , Toast.LENGTH_SHORT).show();
				Intent intent3 = new Intent("HOW_TO_PLAY");
				Bundle bundle3 = new Bundle();
				bundle3.putInt("how", MusicSituation.PLAY_RANDOM);
				intent3.putExtras(bundle3);
				sendBroadcast(intent3);
				isFirstClickItem = false;
				
				Intent intent4 = new Intent("PLAY_OR_PAUSE");
				Bundle bundle4 = new Bundle();
				bundle4.putBoolean("playing", true);
				intent4.putExtras(bundle4);
				sendBroadcast(intent4);
			}

			@Override
			public void PreFresh() {

			}
			
		}, 0);
		btn = (Button)findViewById(R.id.main_activity_layout_button1);
		btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(!isFirstClickItem){
					if(isplaying == true){
						btn.setBackground(getResources().getDrawable(R.drawable.pause));
						Intent intent = new Intent(MainActivity.this,PlayMusicService.class);
						Bundle bundle = new Bundle();
						bundle.putInt("situation", MusicSituation.REPLAY);
						intent.putExtras(bundle);
						startService(intent);
						isplaying = false;
						ispause = true;
						
						Intent intent3 = new Intent("PLAY_OR_PAUSE");
						Bundle bundle3 = new Bundle();
						bundle3.putBoolean("playing", true);
						intent3.putExtras(bundle3);
						sendBroadcast(intent3);
					}
					else if(ispause == true){
						btn.setBackground(getResources().getDrawable(R.drawable.play));
						Intent intent = new Intent(MainActivity.this,PlayMusicService.class);
						Bundle bundle = new Bundle();
						bundle.putInt("situation", MusicSituation.PAUSE);
						intent.putExtras(bundle);
						startService(intent);
						ispause = false;
						isplaying = true;

						Intent intent3 = new Intent("PLAY_OR_PAUSE");
						Bundle bundle3 = new Bundle();
						bundle3.putBoolean("playing", false);
						intent3.putExtras(bundle3);
						sendBroadcast(intent3);
					}
				}
			}
		});
		characterParser = CharacterParser.getInstance();
		
		pinyinComparator = new PinyinComparator();
		
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);
		
		//�����Ҳഥ������
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			
			@Override
			public void onTouchingLetterChanged(String s) {
				//����ĸ�״γ��ֵ�λ��
				int position = adapter.getPositionForSection(s.charAt(0));
				if(position != -1){
					sortListView.setSelection(position);
				}
				
			}
		});

		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@SuppressLint("NewApi")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(isFirstClickItem){
					
					song_name.setText(((SortModel)adapter.getItem(position)).getName());
					Intent intent1 = new Intent(MainActivity.this,SingleSongActivity.class);
					Bundle bundle1 = new Bundle();
					
					bundle1.putString("name", ((SortModel)adapter.getItem(position)).getName());
					bundle1.putInt("position", position);
					bundle1.putLong("duration", ((SortModel)adapter.getItem(position)).getDuration());
					//bundle1.putString("key", "value");
					intent1.putExtras(bundle1);
					startActivity(intent1);

					Intent intent2 = new Intent(MainActivity.this,PlayMusicService.class);
					Bundle bundle2 = new Bundle();
					String url = SourceDateList.get(position).getPath();
					bundle2.putInt("situation", MusicSituation.PLAY);
					bundle2.putString("url", url);
					bundle2.putInt("position", position);
					intent2.putExtras(bundle2);
					startService(intent2);
					isplaying = false;
					ispause = true;
					btn.setBackground(getResources().getDrawable(R.drawable.pause));
					sortListView.setSelection(position);
					ItemPosition = position;
					isFirstClickItem = false;
					
					contentView.setTextViewText(R.id.textView1, ((SortModel)adapter.getItem(ItemPosition)).getName());
					Intent intent3 = new Intent(MainActivity.this,PlayMusicService.class);
					Bundle bundle = new Bundle();
					bundle.putInt("situation", MusicSituation.NEXT);
					bundle.putInt("position", ItemPosition);
					intent3.putExtras(bundle);
					Intent intent4 = new Intent(MainActivity.this,PlayMusicService.class);
				    intent4 = new Intent(MainActivity.this,PlayMusicService.class);
				    Bundle bundle4 = new Bundle();
					bundle4.putInt("situation", MusicSituation.PLAY_OR_PAUSE);
					intent4.putExtras(bundle4);
					
					PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button1, pendingIntent);
					
					PendingIntent pendingIntent2 = PendingIntent.getService(MainActivity.this, 1, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button2, pendingIntent2);
					
					
					Intent intent = new Intent(MainActivity.this,SingleSongActivity.class);
					/*intent.putExtra("name", ((SortModel)adapter.getItem(position)).getName());
					intent.putExtra("position", position);
					intent.putExtra("duration", ((SortModel)adapter.getItem(position)).getDuration());*/
		
					intent.putExtras(bundle1);
					System.out.println(intent.getExtras().getString("name"));
					/*Bundle b = new Bundle();
					b.putString("key", "value");
					intent.putExtras(b);*/
					//intent.putExtras(bundle1);
					//System.out.println("intent.getExtras():" + " " + intent.getExtras().getString("name"));
					PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					Notification notify = new Notification.Builder(MainActivity.this)
					.setContent(contentView)
					.setTicker(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentTitle(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentText(((SortModel)adapter.getItem(ItemPosition)).getPath())
					.setSmallIcon(R.drawable.notification_icon)
					.setWhen(System.currentTimeMillis())
					.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
					.setContentIntent(pi).build();
					
					nm.notify(NOTIFICATION_ID, notify);

				}
				else if(!isFirstClickItem){
					if(position == ItemPosition){
						song_name = (TextView)findViewById(R.id.main_activity_layout_textView1);
						song_name.setText(((SortModel)adapter.getItem(position)).getName());
						Intent intent1 = new Intent(MainActivity.this,SingleSongActivity.class);
						Bundle bundle1 = new Bundle();
						bundle1.putBoolean("isplaying", isplaying);
						bundle1.putString("name", ((SortModel)adapter.getItem(position)).getName());
						bundle1.putInt("position", position);
						bundle1.putLong("duration", ((SortModel)adapter.getItem(position)).getDuration());
						intent1.putExtras(bundle1);
						startActivity(intent1);
						
						Intent intent2 = new Intent(MainActivity.this,PlayMusicService.class);
						Bundle bundle2 = new Bundle();
						String url = SourceDateList.get(position).getPath();
						bundle2.putInt("situation", MusicSituation.CONTINV);
						bundle2.putString("url", url);
						bundle2.putInt("position", position);
						intent2.putExtras(bundle2);
						startService(intent2);

						isplaying = false;
						ispause = true;
						sortListView.setSelection(position);

					}
					else if(position != ItemPosition){
						isplaying = false;
						ispause = true;
						btn.setBackground(getResources().getDrawable(R.drawable.pause));
						song_name = (TextView)findViewById(R.id.main_activity_layout_textView1);
						song_name.setText(((SortModel)adapter.getItem(position)).getName());
						Intent intent1 = new Intent(MainActivity.this,SingleSongActivity.class);
						Bundle bundle1 = new Bundle();
						bundle1.putBoolean("isplaying", isplaying);
						bundle1.putString("name", ((SortModel)adapter.getItem(position)).getName());
						bundle1.putInt("position", position);
						bundle1.putLong("duration", ((SortModel)adapter.getItem(position)).getDuration());
						intent1.putExtras(bundle1);
						startActivity(intent1);
						
						Intent intent2 = new Intent(MainActivity.this,PlayMusicService.class);
						Bundle bundle2 = new Bundle();
						String url = SourceDateList.get(position).getPath();
						
						bundle2.putInt("situation", MusicSituation.PLAY);
						bundle2.putString("url", url);
						bundle2.putInt("position", position);
						intent2.putExtras(bundle2);
						startService(intent2);

						ItemPosition = position;
						contentView.setTextViewText(R.id.textView1, ((SortModel)adapter.getItem(ItemPosition)).getName());
						Intent intent3 = new Intent(MainActivity.this,PlayMusicService.class);
						Bundle bundle = new Bundle();
						bundle.putInt("situation", MusicSituation.NEXT);
						bundle.putInt("position", ItemPosition);
						intent3.putExtras(bundle);
						Intent intent4 = new Intent(MainActivity.this,PlayMusicService.class);
					    intent4 = new Intent(MainActivity.this,PlayMusicService.class);
					    Bundle bundle4 = new Bundle();
						bundle4.putInt("situation", MusicSituation.PLAY_OR_PAUSE);
						intent4.putExtras(bundle4);
						PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
						contentView.setOnClickPendingIntent(R.id.button1, pendingIntent);
						PendingIntent pendingIntent2 = PendingIntent.getService(MainActivity.this, 1, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
						contentView.setOnClickPendingIntent(R.id.button2, pendingIntent2);
						sortListView.setSelection(position);
						
						Intent intent = new Intent(MainActivity.this,SingleSongActivity.class);
						intent.putExtras(bundle1);
						PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
						Notification notify = new Notification.Builder(MainActivity.this)
						.setContent(contentView)
						.setTicker(((SortModel)adapter.getItem(ItemPosition)).getName())
						//.setContentTitle(((SortModel)adapter.getItem(ItemPosition)).getName())
						//.setContentText(((SortModel)adapter.getItem(ItemPosition)).getPath())
						.setSmallIcon(R.drawable.notification_icon)
						.setWhen(System.currentTimeMillis())
						.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
						.setContentIntent(pi).build();
						nm.notify(NOTIFICATION_ID, notify);
					}
				}

			}
		});
		
        sortListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
				vibrator.vibrate(50); 
				musicListItemDialog(); 
				ItemPosition = position;
				return true;
			}
        });



		SourceDateList = filledData(SourceDateList);
		// ���a-z��������Դ���
		Collections.sort(SourceDateList, pinyinComparator);
		for(SortModel sm : SourceDateList){
			//System.out.println(sm.getSortLetters());
		}
		adapter = new SortAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);
		
		
		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
		
		//������������ֵ�ĸı�����������
		mClearEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//������������ֵΪ�գ�����Ϊԭ�����б?����Ϊ��������б�
				filterData(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}
	public void musicListItemDialog() {
		final LinearLayout ll = (LinearLayout)getLayoutInflater().inflate(R.layout.music_menu_list, null);
		ListView lv = (ListView)ll.findViewById(R.id.listView1);
		String[] items = new String[]{"��������","��Ϊ����","��Ϊ����","��������"};
		final ArrayAdapter<String> aa = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, items);
		lv.setAdapter(aa);
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setIcon(R.drawable.ic_launcher)
		.setTitle("��Ҫѡ��ʲô")
		.setView(ll)
		.create();
		final Dialog dialog = builder.show();
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch(position){
				case 0:
					isFirstClickItem = false;
					isplaying = false;
					ispause = true;
					song_name.setText(((SortModel)adapter.getItem(ItemPosition)).getName());
					Intent intent1 = new Intent(MainActivity.this,SingleSongActivity.class);
					Bundle bundle1 = new Bundle();
					bundle1.putBoolean("isplaying", isplaying);
					bundle1.putString("name", ((SortModel)adapter.getItem(ItemPosition)).getName());
					bundle1.putInt("position", ItemPosition);
					bundle1.putLong("duration", ((SortModel)adapter.getItem(ItemPosition)).getDuration());
					intent1.putExtras(bundle1);
					startActivity(intent1);

					Intent intent2 = new Intent(MainActivity.this,PlayMusicService.class);
					Bundle bundle2 = new Bundle();
					String url = SourceDateList.get(ItemPosition).getPath();
					bundle2.putInt("situation", MusicSituation.PLAY);
					bundle2.putString("url", url);
					bundle2.putInt("position", ItemPosition);
					intent2.putExtras(bundle2);
					startService(intent2);
					isplaying = false;
					ispause = true;
					btn.setBackground(getResources().getDrawable(R.drawable.pause));
					sortListView.setSelection(ItemPosition);
					isFirstClickItem = false;
					
					BitmapDrawable bd1 = (BitmapDrawable)getResources().getDrawable(R.drawable.pause);
					BitmapDrawable bd2 = (BitmapDrawable)getResources().getDrawable(R.drawable.next);
					contentView.setImageViewBitmap(R.id.button2,zoomBitmap(bd1.getBitmap()));
					contentView.setImageViewBitmap(R.id.button1, zoomBitmap(bd2.getBitmap()));
					contentView.setTextViewText(R.id.textView1, ((SortModel)adapter.getItem(ItemPosition)).getName());
					Intent intent3 = new Intent(MainActivity.this,PlayMusicService.class);
					Bundle bundle = new Bundle();
					bundle.putInt("situation", MusicSituation.NEXT);
					bundle.putInt("position", ItemPosition);
					intent3.putExtras(bundle);
					Intent intent4 = new Intent(MainActivity.this,PlayMusicService.class);
				    intent4 = new Intent(MainActivity.this,PlayMusicService.class);
				    Bundle bundle4 = new Bundle();
					bundle4.putInt("situation", MusicSituation.PLAY_OR_PAUSE);
					intent4.putExtras(bundle4);
					PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button1, pendingIntent);
					PendingIntent pendingIntent2 = PendingIntent.getService(MainActivity.this, 1, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button2, pendingIntent2);
					Intent intent = new Intent(MainActivity.this,SingleSongActivity.class);
					intent.putExtras(bundle1);
					PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					Notification notify = new Notification.Builder(MainActivity.this)
					.setContent(contentView)
					.setTicker(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentTitle(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentText(((SortModel)adapter.getItem(ItemPosition)).getPath())
					.setSmallIcon(R.drawable.notification_icon)
					.setWhen(System.currentTimeMillis())
					.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
					.setContentIntent(pi).build();
					nm.notify(NOTIFICATION_ID, notify);
					dialog.dismiss();
					break;
				case 1:
					setRing();
					dialog.dismiss();
					break;
				case 2:
					setAlerm();
					dialog.dismiss();
					break;
				case 3:
					shared();
					dialog.dismiss();
					break;
				}
			}				
		});

	}
/*	private void setRing(){
		SortModel mSortModel = SourceDateList.get(ItemPosition);
		File sdfile = new File(mSortModel.getPath().substring(4));
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
		values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
		values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
		values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
		values.put(MediaStore.Audio.Media.IS_ALARM, false);
		values.put(MediaStore.Audio.Media.IS_MUSIC, false);
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
				.getAbsolutePath());
		System.out.println(sdfile.getAbsolutePath());
		Uri newUri = this.getContentResolver().insert(uri, values);
		RingtoneManager.setActualDefaultRingtoneUri(this,
				RingtoneManager.TYPE_RINGTONE, newUri);
		Toast.makeText(MainActivity.this, "������������ɹ���", Toast.LENGTH_SHORT)
				.show();
	}*/
	
	 public void setRing() {
		  ContentValues cv = new ContentValues();
		  SortModel mSortModel = SourceDateList.get(ItemPosition);
		  String path = mSortModel.getPath();
		  Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		  Cursor cursor = this.getContentResolver().query(uri, null,
		    AudioColumns.DATA + "=?", new String[] { path },
		    MediaStore.Audio.Media._ID);
		  if (cursor.moveToFirst()) {
		   String _id = cursor.getString(0);
		   //start if
		   cv.put(AudioColumns.IS_RINGTONE, true);
		   cv.put(AudioColumns.IS_NOTIFICATION, false);
		   cv.put(AudioColumns.IS_ALARM, false);
		   cv.put(AudioColumns.IS_MUSIC, true);
		   //end if
		   
		   // insert ringtone
		   getContentResolver().update(uri, cv,
		     MediaStore.MediaColumns.DATA + "=?", new String[] { path });
		   Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
		   
		   RingtoneManager.setActualDefaultRingtoneUri(this,
		       RingtoneManager.TYPE_RINGTONE, newUri );
		  }
			Toast.makeText(MainActivity.this, "������������ɹ���", Toast.LENGTH_SHORT)
			.show();
		 }
	 
	private void setAlerm(){
		  ContentValues cv = new ContentValues();
		  SortModel mSortModel = SourceDateList.get(ItemPosition);
		  String path = mSortModel.getPath();
		  Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		  Cursor cursor = this.getContentResolver().query(uri, null,
		    AudioColumns.DATA + "=?", new String[] { path },
		    MediaStore.Audio.Media._ID);
		  if (cursor.moveToFirst()) {
		   String _id = cursor.getString(0);
		   //start if
		   cv.put(AudioColumns.IS_RINGTONE, false);
		   cv.put(AudioColumns.IS_NOTIFICATION, false);
		   cv.put(AudioColumns.IS_ALARM, true);
		   cv.put(AudioColumns.IS_MUSIC, true);
		   //end if
		   
		   // insert ringtone
		   getContentResolver().update(uri, cv,
		     MediaStore.MediaColumns.DATA + "=?", new String[] { path });
		   Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
		   
		   RingtoneManager.setActualDefaultRingtoneUri(this,
		       RingtoneManager.TYPE_RINGTONE, newUri );
		  }
			Toast.makeText(MainActivity.this, "��������ɹ���", Toast.LENGTH_SHORT)
			.show();
	}
	private void shared(){
		File MusicFile = new File(SourceDateList.get(ItemPosition).getPath());
		Uri uri = Uri.fromFile(MusicFile);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("audio/x-mpeg");
		intent.putExtra(Intent.EXTRA_STREAM,uri);
		startActivity(Intent.createChooser(intent, "��Ҫ����ĵ����"));
	}
	private List<SortModel> filledData(List<SortModel> date){
		List<SortModel> mSortList = new ArrayList<SortModel>();
		
		for(int i=0; i<date.size(); i++){
			SortModel sortModel = new SortModel();
			sortModel.setName(date.get(i).getTitle());
			sortModel.setDuration(date.get(i).getDuration());
			sortModel.setPath(date.get(i).getUrl());
			//����ת����ƴ��
			String pinyin = characterParser.getSelling(date.get(i).getTitle());
			//System.out.println(date.get(i).getTitle());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			
			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
			if(sortString.matches("[A-Z]")){
				sortModel.setSortLetters(sortString.toUpperCase());
			}else{
				sortModel.setSortLetters("#");
			}
			mSortList.add(sortModel);
		}
		return mSortList;
		
	}
	/**
	 * ���������е�ֵ��������ݲ�����ListView
	 * @param filterStr
	 */
	private void filterData(String filterStr){
		List<SortModel> filterDateList = new ArrayList<SortModel>();
		
		if(TextUtils.isEmpty(filterStr)){
			filterDateList = SourceDateList;
		}else{
			filterDateList.clear();
			for(SortModel sortModel : SourceDateList){
				String name = sortModel.getName();
				// ��ǰ�ĳ���
				if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
					filterDateList.add(sortModel);
				}
				
			}
		}
		
		// ���a-z��������
		Collections.sort(filterDateList, pinyinComparator);
		
		adapter.updateListView(filterDateList);
	}
	
    public Bitmap zoomBitmap(Bitmap target)
    {
            int width = target.getWidth();
            int height = target.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidth = ((float)120)/ width;
            float scaleHeight = ((float)120)/ height;
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap result = Bitmap.createBitmap(target, 0, 0, width,   
                height, matrix, true);   
            return result;
    }
    
	public class HomeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			
			if(action.equals("UPDATE_CURRENT")){
				//System.out.println("UPDATE_CURRENT");
				isplaying = false;
				ispause = true;
				btn.setBackground(getResources().getDrawable(R.drawable.pause));
				ItemPosition = intent.getExtras().getInt("position");
				String name = SourceDateList.get(ItemPosition).getName();
				song_name.setText(name);
			    new Handler().postDelayed(new Runnable() {
			           @Override
			           public void run() {
							sortListView.setSelection(ItemPosition);
			           }
			      }, 200);
			    

			    //contentView.setTextViewText(R.id.button2, "��ͣ");
			    BitmapDrawable bd = (BitmapDrawable)getResources().getDrawable(R.drawable.pause);
			    BitmapDrawable bd2 = (BitmapDrawable)getResources().getDrawable(R.drawable.next);
				contentView.setImageViewBitmap(R.id.button2,zoomBitmap(bd.getBitmap()));
				contentView.setImageViewBitmap(R.id.button1, zoomBitmap(bd2.getBitmap()));
				contentView.setTextViewText(R.id.textView1, ((SortModel)adapter.getItem(ItemPosition)).getName());
				Intent intent3 = new Intent(MainActivity.this,PlayMusicService.class);
				Bundle bundle2 = new Bundle();
				bundle2.putInt("situation", MusicSituation.NEXT);
				bundle2.putInt("position", ItemPosition);
				intent3.putExtras(bundle2);
				Intent intent4 = new Intent(MainActivity.this,PlayMusicService.class);
			    intent4 = new Intent(MainActivity.this,PlayMusicService.class);
			    Bundle bundle4 = new Bundle();
				bundle4.putInt("situation", MusicSituation.PLAY_OR_PAUSE);
				intent4.putExtras(bundle4);
				PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
				contentView.setOnClickPendingIntent(R.id.button1, pendingIntent);
				PendingIntent pendingIntent2 = PendingIntent.getService(MainActivity.this, 1, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
				contentView.setOnClickPendingIntent(R.id.button2, pendingIntent2);
				Intent intent1 = new Intent(MainActivity.this,SingleSongActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("name", ((SortModel)adapter.getItem(ItemPosition)).getName());
				bundle.putInt("position", ItemPosition);
				bundle.putLong("duration", ((SortModel)adapter.getItem(ItemPosition)).getDuration());
				intent1.putExtras(bundle);
				PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
				Notification notify = new Notification.Builder(MainActivity.this)
				.setContent(contentView)
				.setTicker(((SortModel)adapter.getItem(ItemPosition)).getName())
				//.setContentTitle(((SortModel)adapter.getItem(ItemPosition)).getName())
				//.setContentText(((SortModel)adapter.getItem(ItemPosition)).getPath())
				.setSmallIcon(R.drawable.notification_icon)
				.setWhen(System.currentTimeMillis())
				.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
				.setContentIntent(pi).build();
				//System.out.println("receiver");
				nm.notify(NOTIFICATION_ID, notify);
			}
			else if(action.equals("PLAY OR STOP")){
				//System.out.println("PLAY OR STOP");
				//System.out.println("play or stop");
				boolean PlayOrStop = intent.getExtras().getBoolean("isplaying");
				//System.out.println(PlayOrStop);
				if(PlayOrStop){
					//System.out.println("111");
					btn.setBackground(getResources().getDrawable(R.drawable.play));
					isplaying = true;
					ispause = false;
				}
				else{
					//System.out.println("222");
					btn.setBackground(getResources().getDrawable(R.drawable.pause));
					isplaying = false;
					ispause = true;
				}
			}
			else if(action.equals("FINISH_ACTIVITY")){
				finish();
			}
			else if(action.equals("PLAY_OR_PAUSE")){
				//System.out.println("PLAY_OR_PAUSE");
				if(intent.getExtras().getBoolean("playing")){
					//System.out.println("if");
					btn.setBackground(getResources().getDrawable(R.drawable.pause));
					isplaying = false;
					ispause = true;
						
					//contentView.setTextViewText(R.id.button2, "��ͣ");
				    BitmapDrawable bd = (BitmapDrawable)getResources().getDrawable(R.drawable.pause);
				    BitmapDrawable bd2 = (BitmapDrawable)getResources().getDrawable(R.drawable.next);
					contentView.setImageViewBitmap(R.id.button2,zoomBitmap(bd.getBitmap()));
					contentView.setImageViewBitmap(R.id.button1, zoomBitmap(bd2.getBitmap()));
					contentView.setTextViewText(R.id.textView1, ((SortModel)adapter.getItem(ItemPosition)).getName());
					Intent intent3 = new Intent(MainActivity.this,PlayMusicService.class);
					Bundle bundle = new Bundle();
					bundle.putInt("situation", MusicSituation.NEXT);
					bundle.putInt("position", ItemPosition);
					intent3.putExtras(bundle);
					Intent intent4 = new Intent(MainActivity.this,PlayMusicService.class);
					intent4 = new Intent(MainActivity.this,PlayMusicService.class);
					Bundle bundle4 = new Bundle();
				    bundle4.putInt("situation", MusicSituation.PLAY_OR_PAUSE);
					intent4.putExtras(bundle4);
						
					PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button1, pendingIntent);
						
					PendingIntent pendingIntent2 = PendingIntent.getService(MainActivity.this, 1, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button2, pendingIntent2);
						   
					Bundle bundle1 = new Bundle();
					bundle1.putString("name", ((SortModel)adapter.getItem(ItemPosition)).getName());
					bundle1.putInt("position", ItemPosition);
					bundle1.putLong("duration", ((SortModel)adapter.getItem(ItemPosition)).getDuration());
					Intent intent2 = new Intent(MainActivity.this,SingleSongActivity.class);
					intent2.putExtras(bundle1);
					PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
					Notification notify = new Notification.Builder(MainActivity.this)
					.setContent(contentView)
					.setTicker(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentTitle(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentText(((SortModel)adapter.getItem(ItemPosition)).getPath())
					.setSmallIcon(R.drawable.notification_icon)
					.setWhen(System.currentTimeMillis())
					.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
					.setContentIntent(pi).build();
					nm.notify(NOTIFICATION_ID, notify);
					}	
				else{
					//System.out.println("else");
					btn.setBackground(getResources().getDrawable(R.drawable.play));
					isplaying = true;
					ispause = false;

					//contentView.setTextViewText(R.id.button2, "��ʼ");
					
					BitmapDrawable bd1 = (BitmapDrawable)getResources().getDrawable(R.drawable.play);
					BitmapDrawable bd2 = (BitmapDrawable)getResources().getDrawable(R.drawable.next);
					contentView.setImageViewBitmap(R.id.button2, zoomBitmap(bd1.getBitmap()));
					contentView.setImageViewBitmap(R.id.button1, zoomBitmap(bd2.getBitmap()));
					contentView.setTextViewText(R.id.textView1, ((SortModel)adapter.getItem(ItemPosition)).getName());
					Intent intent3 = new Intent(MainActivity.this,PlayMusicService.class);
					Bundle bundle = new Bundle();
					bundle.putInt("situation", MusicSituation.NEXT);
					bundle.putInt("position", ItemPosition);
					intent3.putExtras(bundle);
					Intent intent4 = new Intent(MainActivity.this,PlayMusicService.class);
				    intent4 = new Intent(MainActivity.this,PlayMusicService.class);
				    Bundle bundle4 = new Bundle();
					bundle4.putInt("situation", MusicSituation.PLAY_OR_PAUSE);
					intent4.putExtras(bundle4);
					
					PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button1, pendingIntent);
					
					PendingIntent pendingIntent2 = PendingIntent.getService(MainActivity.this, 1, intent4, PendingIntent.FLAG_UPDATE_CURRENT);
					contentView.setOnClickPendingIntent(R.id.button2, pendingIntent2);
					   
					Bundle bundle1 = new Bundle();
					bundle1.putString("name", ((SortModel)adapter.getItem(ItemPosition)).getName());
					bundle1.putInt("position", ItemPosition);
					bundle1.putLong("duration", ((SortModel)adapter.getItem(ItemPosition)).getDuration());
					Intent intent2 = new Intent(MainActivity.this,SingleSongActivity.class);
					intent.putExtras(bundle1);
					PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
					Notification notify = new Notification.Builder(MainActivity.this)
					.setContent(contentView)
					.setTicker(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentTitle(((SortModel)adapter.getItem(ItemPosition)).getName())
					//.setContentText(((SortModel)adapter.getItem(ItemPosition)).getPath())
					.setSmallIcon(R.drawable.notification_icon)
					.setWhen(System.currentTimeMillis())
					.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS)
					.setContentIntent(pi).build();
					nm.notify(NOTIFICATION_ID, notify);
				}	
			}
		}
	}
	
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if (keyCode == KeyEvent.KEYCODE_BACK) {  
        	 exit(); 
             return true;   
        }  
        return super.onKeyDown(keyCode, event);  
    } 
    private void exit(){
    	if(!isExit){
    		isExit = true;
    		Toast.makeText(MainActivity.this, "�ٰ�һ�η�������", Toast.LENGTH_SHORT).show();
    		mHandler.sendEmptyMessageDelayed(0, 1500);
    	}
    	else{
    		Intent i= new Intent(Intent.ACTION_MAIN);
		    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    i.addCategory(Intent.CATEGORY_HOME);
		    startActivity(i); 
    	}
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
		case R.id.item1:
			final RelativeLayout rl = (RelativeLayout)LayoutInflater.from(MainActivity.this)
			.inflate(R.layout.how_to_play_list,null);
			final ListView lv = (ListView)rl.findViewById(R.id.listView1);				
			mHowToPlayListItemAdapter = new HowToPlayListItemAdapter(MainActivity.this);
			lv.setAdapter(mHowToPlayListItemAdapter);
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setIcon(R.drawable.ic_launcher)
			.setTitle("��Ҫѡ��ʲô���ŷ�ʽ")
			.setView(rl)
			.create();
			final Dialog dialog = builder.show();
			lv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					ViewHolder vh = (ViewHolder)view.getTag();
					vh.cb.toggle();
					editor.putInt("type", position + 1);
			    	editor.commit();
					switch(position){
					case 0:
						Toast.makeText(MainActivity.this, "����ģʽΪ����ѭ��", Toast.LENGTH_SHORT).show();
				    	Intent intent = new Intent("HOW_TO_PLAY");
				    	Bundle bundle = new Bundle();
				    	bundle.putInt("how", MusicSituation.PLAY_ONE_SONG);
				    	intent.putExtras(bundle);
				    	sendBroadcast(intent);
						break;
					case 1:
						Toast.makeText(MainActivity.this, "����ģʽΪ˳�򲥷�", Toast.LENGTH_SHORT).show();
				    	Intent intent2 = new Intent("HOW_TO_PLAY");
				    	Bundle bundle2 = new Bundle();
				    	bundle2.putInt("how", MusicSituation.PLAY_BY_ORDER);
				    	intent2.putExtras(bundle2);
				    	sendBroadcast(intent2);
						break;
					case 2:
						Toast.makeText(MainActivity.this, "����ģʽΪ����", Toast.LENGTH_SHORT).show();
				    	Intent intent3 = new Intent("HOW_TO_PLAY");
				    	Bundle bundle3 = new Bundle();
				    	bundle3.putInt("how", MusicSituation.PLAY_RANDOM);
				    	intent3.putExtras(bundle3);
				    	sendBroadcast(intent3);
						break;
					}
					dialog.dismiss();
				}
				
			});
			break;
			/*editor.putInt("type", 1);
	    	editor.commit();
			Intent intent = new Intent("HOW_TO_PLAY");
			Bundle bundle = new Bundle();
			bundle.putInt("how", MusicSituation.PLAY_ONE_SONG);
			intent.putExtras(bundle);
			sendBroadcast(intent);
			Toast.makeText(MainActivity.this, "����ģʽΪ����ѭ��", Toast.LENGTH_SHORT).show();
			break;*/
	    /*case R.id.item2:
	    	editor.putInt("type", 2);
	    	editor.commit();
			Intent intent2 = new Intent("HOW_TO_PLAY");
			Bundle bundle2 = new Bundle();
			bundle2.putInt("how", MusicSituation.PLAY_BY_ORDER);
			intent2.putExtras(bundle2);
			sendBroadcast(intent2);
			Toast.makeText(MainActivity.this, "����ģʽΪ˳�򲥷�", Toast.LENGTH_SHORT).show();
			break;
	    case R.id.item3:
	    	editor.putInt("type", 3);
	    	editor.commit();
			Intent intent3 = new Intent("HOW_TO_PLAY");
			Bundle bundle3 = new Bundle();
			bundle3.putInt("how", MusicSituation.PLAY_RANDOM);
			intent3.putExtras(bundle3);
			sendBroadcast(intent3);
			Toast.makeText(MainActivity.this, "����ģʽΪ����", Toast.LENGTH_SHORT).show();
			break;*/
	    case R.id.item2:
	    	Intent intent4 = new Intent("FINISH");
	    	sendBroadcast(intent4);
	    	finish();
	    	nm.cancel(NOTIFICATION_ID);
		}
	
		return super.onOptionsItemSelected(item);
	}
}
