package com.kz.activity;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kz.Adapter.HowToPlayListItemAdapter;
import com.kz.Adapter.MyPagerAdapter;
import com.kz.Adapter.HowToPlayListItemAdapter.ViewHolder;
import com.kz.Service.PlayMusicService;
import com.kz.Util.MediaUtil;
import com.kz.View.CharacterParser;
import com.kz.View.PinyinComparator;
import com.kz.domin.MusicSituation;
import com.kz.domin.SortModel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SingleSongActivity extends FragmentActivity {


	private ViewPager viewPager;
	private TextView song_name;
	private Button play;
	private Button pre;
	private Button next;
	private SeekBar sb;
	private TextView duration;
	private int int_duration;
	private int current;
	private int currentTime;
	NotificationManager nm;
	static final int NOTIFICATION_ID = 0x123;
	private List<SortModel> SourceDateList;
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;
	private boolean isplaying;
	private boolean ispause;
	private HomeReceiver homeReceiver;
	private HowToPlayListItemAdapter mHowToPlayListItemAdapter;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//System.out.println("onCreate");
		setContentView(R.layout.single_song_activity_layout);
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		SourceDateList = MediaUtil.getMp3Infos(SingleSongActivity.this);
		SourceDateList = filledData(SourceDateList);
		Collections.sort(SourceDateList, pinyinComparator);
		initViewPager();
		initView();
		initBroadcast();
		initPreferences();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		//System.out.println("onNewIntent");
		//System.out.println(intent.getExtras().getString("name"));
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(homeReceiver);
		super.onDestroy();
	}
	private void initPreferences(){
		preferences = getSharedPreferences("MyMusicPlayer" , MODE_WORLD_READABLE);
		editor = preferences.edit();
	}
	private void initBroadcast(){
		homeReceiver = new HomeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("UPDATE_CURRENT");
		filter.addAction("MUSIC_CURRENT");
		filter.addAction("LYRIC_CURRENT");
		filter.addAction("PLAY_OR_PAUSE");
		registerReceiver(homeReceiver, filter);
	}
	private void initView(){
		song_name = (TextView)findViewById(R.id.single_song_activity_textView1);
		duration = (TextView)findViewById(R.id.current_duration);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		//System.out.println(bundle.getString("key"));
		//System.out.println("intent.getExtras():" + " " + (intent == null));
		/*if(intent.getExtras() != null){
			System.out.println(intent.getExtras().getString("key","def"));
		}else{
			System.out.println("null");
		}*/
		String name = bundle.getString("name");
		song_name.setText(name);
		
		current = bundle.getInt("position");
		/*System.out.println("name:" + " " + name);
		System.out.println("current:" + " " + current);*/
		isplaying = bundle.getBoolean("isplaying");
		ispause = !bundle.getBoolean("isplaying");
		//System.out.println(SourceDateList.get(current).getName());
		play = (Button)findViewById(R.id.single_song_activity_button2);
		if(isplaying){
			play.setBackground(getResources().getDrawable(R.drawable.play));
			ispause = false;
			isplaying = true;
		}
		else{
			play.setBackground(getResources().getDrawable(R.drawable.pause));
			isplaying = false;
			ispause = true;
		}
		play.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isplaying == true){
					play.setBackground(getResources().getDrawable(R.drawable.pause));
					Intent intent = new Intent(SingleSongActivity.this,PlayMusicService.class);
					Bundle bundle = new Bundle();
					bundle.putInt("situation", MusicSituation.REPLAY);
					intent.putExtras(bundle);
					startService(intent);
					isplaying = false;
					ispause = true;
					Intent intent2 = new Intent("PLAY OR STOP");
					Bundle bundle2 = new Bundle();
					bundle2.putBoolean("isplaying", isplaying);
					intent2.putExtras(bundle2);
					sendBroadcast(intent2);
					
					Intent intent3 = new Intent("PLAY_OR_PAUSE");
					Bundle bundle3 = new Bundle();
					bundle3.putBoolean("playing", true);
					intent3.putExtras(bundle3);
					sendBroadcast(intent3);
				}
				else if(ispause == true){
					play.setBackground(getResources().getDrawable(R.drawable.play));
					Intent intent = new Intent(SingleSongActivity.this,PlayMusicService.class);
					Bundle bundle = new Bundle();
					bundle.putInt("situation", MusicSituation.PAUSE);
					intent.putExtras(bundle);
					startService(intent);
					ispause = false;
					isplaying = true;
					Intent intent2 = new Intent("PLAY OR STOP");
					Bundle bundle2 = new Bundle();
					bundle2.putBoolean("isplaying", isplaying);
					System.out.println("single:" + isplaying);
					intent2.putExtras(bundle2);
					sendBroadcast(intent2);
					
					Intent intent3 = new Intent("PLAY_OR_PAUSE");
					Bundle bundle3 = new Bundle();
					bundle3.putBoolean("playing", false);
					intent3.putExtras(bundle3);
					sendBroadcast(intent3);
				}
			}
		});

		pre = (Button)findViewById(R.id.single_song_activity_button1);
		pre.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				isplaying = false;
				ispause = true;
				play.setBackground(getResources().getDrawable(R.drawable.pause));
				Intent intent = new Intent(SingleSongActivity.this,PlayMusicService.class);
				Bundle bundle = new Bundle();
				bundle.putInt("situation", MusicSituation.PRE);
				bundle.putInt("position", current);
				intent.putExtras(bundle);
				startService(intent);
				Intent intent3 = new Intent("PLAY OR STOP");
				Bundle bundle3 = new Bundle();
				bundle3.putBoolean("isplaying", isplaying);
				intent3.putExtras(bundle3);
				sendBroadcast(intent3);
				if(current == 0){
					current = SourceDateList.size() - 1;
				}
				else{
					current--;
				}

				Intent intent2 = new Intent("UPDATE_CURRENT");
				Bundle bundle2 = new Bundle();
				bundle2.putInt("position", current);
				intent2.putExtras(bundle2);
				sendBroadcast(intent2);
				String name = SourceDateList.get(current).getName();
				song_name.setText(name);
			}
			
		});
		next = (Button)findViewById(R.id.single_song_activity_button3);
		next.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				isplaying = false;
				ispause = true;
				play.setBackground(getResources().getDrawable(R.drawable.pause));
				Intent intent = new Intent(SingleSongActivity.this,PlayMusicService.class);
				Bundle bundle = new Bundle();
				bundle.putInt("situation", MusicSituation.NEXT);
				bundle.putInt("position", current);
				intent.putExtras(bundle);
				startService(intent);
				Intent intent3 = new Intent("PLAY OR STOP");
				Bundle bundle3 = new Bundle();
				bundle3.putBoolean("isplaying", isplaying);
				intent3.putExtras(bundle3);
				sendBroadcast(intent3);
				if(current == SourceDateList.size() - 1){
					current = 0;
					
				}
				else{
					current++;
				}
				Intent intent2 = new Intent("UPDATE_CURRENT");
				Bundle bundle2 = new Bundle();
				bundle2.putInt("position", current);
				intent2.putExtras(bundle2);
				sendBroadcast(intent2);
				String name = SourceDateList.get(current).getName();
				song_name.setText(name);
			}
		});
		sb = (SeekBar)findViewById(R.id.single_song_activity_seekBar1);
		Intent intent2 = getIntent();
		sb.setMax((int) intent2.getLongExtra("duration", -1));
		sb.setProgress(currentTime);
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					audioTrackChange(progress); // 用户控制进度的改变
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO 自动生成的方法存根
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO 自动生成的方法存根
				
			}
			
		});
	}
	public void audioTrackChange(int progress) {
		Intent intent = new Intent(SingleSongActivity.this,PlayMusicService.class);
		Bundle bundle = new Bundle();
		bundle.putInt("situation",  MusicSituation.PROGRESS_CHANGE);
		bundle.putInt("position", current);
		bundle.putInt("progress", progress);

		intent.putExtras(bundle);
		startService(intent);
		play.setBackground(getResources().getDrawable(R.drawable.pause));
		isplaying = false;
		ispause = true;
		Intent intent2 = new Intent("PLAY OR STOP");
		Bundle bundle2 = new Bundle();
		bundle2.putBoolean("isplaying", isplaying);
		intent2.putExtras(bundle2);
		sendBroadcast(intent2);
		
		Intent intent3 = new Intent("PLAY_OR_PAUSE");
		Bundle bundle3 = new Bundle();
		bundle3.putBoolean("playing", true);
		intent3.putExtras(bundle3);
		sendBroadcast(intent3);
		
	}
	private void initViewPager(){
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
	}
	private List<SortModel> filledData(List<SortModel> date){
		List<SortModel> mSortList = new ArrayList<SortModel>();
		
		for(int i=0; i<date.size(); i++){
			SortModel sortModel = new SortModel();
			sortModel.setName(date.get(i).getTitle());
			sortModel.setDuration(date.get(i).getDuration());
			sortModel.setPath(date.get(i).getUrl());
			//汉字转换成拼音
			String pinyin = characterParser.getSelling(date.get(i).getTitle());
			String sortString = pinyin.substring(0, 1).toUpperCase();
			
			// 正则表达式，判断首字母是否是英文字母
			if(sortString.matches("[A-Z]")){
				sortModel.setSortLetters(sortString.toUpperCase());
			}else{
				sortModel.setSortLetters("#");
			}
			
			mSortList.add(sortModel);
		}
		return mSortList;
		
	}
	
	public class HomeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if(action.equals("UPDATE_CURRENT")){
				isplaying = false;
				ispause = true;
				play.setBackground(getResources().getDrawable(R.drawable.pause));
				current = intent.getExtras().getInt("position");
				String name = SourceDateList.get(current).getName();
				song_name.setText(name);
			}
			else if(action.equals("MUSIC_CURRENT")){
				currentTime = intent.getIntExtra("currentTime", -1);
				int_duration = intent.getIntExtra("duration", -1);
				sb.setProgress(currentTime);
				sb.setMax(int_duration);
				duration.setText(MediaUtil.formatTime(currentTime) + "/" + MediaUtil.formatTime(int_duration));
			}
			else if(action.equals("PLAY_OR_PAUSE")){
				if(intent.getExtras().getBoolean("playing")){
					play.setBackground(getResources().getDrawable(R.drawable.pause));
					isplaying = false;
					ispause = true;
				}
				else{
					play.setBackground(getResources().getDrawable(R.drawable.play));
					isplaying = true;
					ispause = false;
				}
			}
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
			final RelativeLayout rl = (RelativeLayout)LayoutInflater.from(SingleSongActivity.this)
			.inflate(R.layout.how_to_play_list,null);
			final ListView lv = (ListView)rl.findViewById(R.id.listView1);				
			mHowToPlayListItemAdapter = new HowToPlayListItemAdapter(SingleSongActivity.this);
			lv.setAdapter(mHowToPlayListItemAdapter);
			AlertDialog.Builder builder = new AlertDialog.Builder(SingleSongActivity.this);
			builder.setIcon(R.drawable.ic_launcher)
			.setTitle("您要选择什么播放方式")
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
						Toast.makeText(SingleSongActivity.this, "现在模式为单曲循环", Toast.LENGTH_SHORT).show();
				    	Intent intent = new Intent("HOW_TO_PLAY");
				    	Bundle bundle = new Bundle();
				    	bundle.putInt("how", MusicSituation.PLAY_ONE_SONG);
				    	intent.putExtras(bundle);
				    	sendBroadcast(intent);
						break;
					case 1:
						Toast.makeText(SingleSongActivity.this, "现在模式为顺序播放", Toast.LENGTH_SHORT).show();
				    	Intent intent2 = new Intent("HOW_TO_PLAY");
				    	Bundle bundle2 = new Bundle();
				    	bundle2.putInt("how", MusicSituation.PLAY_BY_ORDER);
				    	intent2.putExtras(bundle2);
				    	sendBroadcast(intent2);
						break;
					case 2:
						Toast.makeText(SingleSongActivity.this, "现在模式为随机播放", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(MainActivity.this, "现在模式为单曲循环", Toast.LENGTH_SHORT).show();
			break;*/
	    /*case R.id.item2:
	    	editor.putInt("type", 2);
	    	editor.commit();
			Intent intent2 = new Intent("HOW_TO_PLAY");
			Bundle bundle2 = new Bundle();
			bundle2.putInt("how", MusicSituation.PLAY_BY_ORDER);
			intent2.putExtras(bundle2);
			sendBroadcast(intent2);
			Toast.makeText(MainActivity.this, "现在模式为顺序播放", Toast.LENGTH_SHORT).show();
			break;
	    case R.id.item3:
	    	editor.putInt("type", 3);
	    	editor.commit();
			Intent intent3 = new Intent("HOW_TO_PLAY");
			Bundle bundle3 = new Bundle();
			bundle3.putInt("how", MusicSituation.PLAY_RANDOM);
			intent3.putExtras(bundle3);
			sendBroadcast(intent3);
			Toast.makeText(MainActivity.this, "现在模式为随机播放", Toast.LENGTH_SHORT).show();
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
