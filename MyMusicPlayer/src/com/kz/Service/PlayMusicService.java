package com.kz.Service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kz.Util.LrcContent;
import com.kz.Util.MediaUtil;
import com.kz.View.CharacterParser;
import com.kz.View.LyricView;
import com.kz.View.PinyinComparator;
import com.kz.View.SingleSongLyric;
import com.kz.activity.R;
import com.kz.activity.SingleSongActivity.HomeReceiver;
import com.kz.domin.LrcProcess;
import com.kz.domin.MusicSituation;
import com.kz.domin.SortModel;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

public class PlayMusicService extends Service {
	private String path;
	private MediaPlayer mp;
	private int current;
	private int currentTime;
	private int duration;
	private int situation;
	private boolean ispause;
	private int HowToPlay;
	private HomeReceiver homeReceiver;
	private List<SortModel> SourceDateList;
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;
	private LrcProcess mLrcProcess; 
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); 
	private int index = 0;   
	private SharedPreferences preferences;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				if(mp != null) {
					currentTime = mp.getCurrentPosition();
					Intent intent = new Intent();
					intent.setAction("MUSIC_CURRENT");
					intent.putExtra("currentTime", currentTime);
					intent.putExtra("duration", duration);
					sendBroadcast(intent); 
					handler.sendEmptyMessageDelayed(1, 1000);
				}
			}
			else if(msg.what == 2){
				Intent intent = new Intent("LYRIC_CURRENT");
				Bundle bundle = new Bundle();
				bundle.putInt("lyric",lrcIndex());
				bundle.putInt("current", current);
				intent.putExtras(bundle);
				sendBroadcast(intent);
				//System.out.println("Service current:" + current);
				handler.sendEmptyMessageDelayed(2, 100);
				//System.out.println("lyric:" + lrcIndex());
				//System.out.println("current:" + current);
			}
		};
	};
	@Override
	public void onCreate() {
		super.onCreate();
		initPreferences();
		initBroadcast();
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		SourceDateList = MediaUtil.getMp3Infos(PlayMusicService.this);
		SourceDateList = filledData(SourceDateList);
		Collections.sort(SourceDateList, pinyinComparator);
		mp = new MediaPlayer();
		mp.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(HowToPlay == 1){
					mp.start();
				}
				else if(HowToPlay == 2){
					if(current == SourceDateList.size() - 1){
						current = 0;
					}
					else{
						current++;
					}
					path = SourceDateList.get(current).getPath();
					Intent intent = new Intent("UPDATE_CURRENT");
					Bundle bundle = new Bundle();
					bundle.putInt("position", current);
					
					intent.putExtras(bundle);
					sendBroadcast(intent);
					play();
				}
				else if(HowToPlay == 3){
					current = getRandomIndex(SourceDateList.size() - 1);
					path = SourceDateList.get(current).getPath();
					Intent intent = new Intent("UPDATE_CURRENT");
					Bundle bundle = new Bundle();
					bundle.putInt("position", current);
					
					intent.putExtras(bundle);
					sendBroadcast(intent);

					play();
				}
				else if(HowToPlay == 0){
					if(current == SourceDateList.size() - 1){
						current = 0;
					}
					else{
						current++;
					}
					path = SourceDateList.get(current).getPath();
					Intent intent = new Intent("UPDATE_CURRENT");
					Bundle bundle = new Bundle();
					bundle.putInt("position", current);
					
					intent.putExtras(bundle);
					sendBroadcast(intent);
					play();
				}
			}
		});

	}
	
	public void initPreferences(){
		preferences = getSharedPreferences("MyMusicPlayer",1);
		int type = preferences.getInt("type", 2);
		System.out.println(type);
		switch(type){
		case 1:
			HowToPlay = 1;
			break;
		case 2:
			HowToPlay = 2;
			break;
		case 3:
			HowToPlay = 3;
			break;
		}
	}
	public void initLrc(){  
        mLrcProcess = new LrcProcess();  
        //读取歌词文件  
        mLrcProcess.readLRC(SourceDateList.get(current).getPath());  
        //传回处理后的歌词文件  
        lrcList = mLrcProcess.getLrcList();  
        //System.out.println("size" + lrcList.size());
		Intent intent = new Intent("LYRIC_CURRENT");
		Bundle bundle = new Bundle();
		bundle.putInt("lyric",lrcIndex());
		bundle.putInt("current", current);
		intent.putExtras(bundle);
		sendBroadcast(intent);
		handler.sendEmptyMessage(2);
    }  
	
    public int lrcIndex() {  
        if(mp.isPlaying()) {  
            currentTime = mp.getCurrentPosition();  
            duration = mp.getDuration();  
        }  
        if(currentTime < duration) {  
            for (int i = 0; i < lrcList.size(); i++) {  
                if (i < lrcList.size() - 1) {  
                    if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {  
                        index = i;  
                    }  
                    if (currentTime > lrcList.get(i).getLrcTime()  
                            && currentTime < lrcList.get(i + 1).getLrcTime()) {  
                        index = i;  
                    }  
                }  
                if (i == lrcList.size() - 1  
                        && currentTime > lrcList.get(i).getLrcTime()) {  
                    index = i;  
                }  
            }  
        }  
        return index;  
    } 
	private int getRandomIndex(int end) {
		int index = (int) (Math.random() * end);
		return index;
	}
	private void initBroadcast(){
		homeReceiver = new HomeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("HOW_TO_PLAY");
		filter.addAction("FINISH");
		registerReceiver(homeReceiver, filter);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//initPreferences();
		situation = intent.getExtras().getInt("situation");
		current = intent.getExtras().getInt("position");
		if(situation == MusicSituation.PLAY){
			path = intent.getExtras().getString("url");
			play();
		}
		else if(situation == MusicSituation.PAUSE){
			pause();
		}
		else if(situation == MusicSituation.REPLAY){
			replay();
		}
		else if(situation == MusicSituation.PRE){
			playPre();
		}
		else if(situation == MusicSituation.NEXT){
			playNext();
		}
		else if(situation == MusicSituation.CONTINV){
			continvPlay();
		}
		else if(situation == MusicSituation.PROGRESS_CHANGE){
			int progress = intent.getExtras().getInt("progress");
			changeProgressPlay(progress);
		}
		else if(situation == MusicSituation.PULL_TO_REFRESH){
			pullToRefresh();
		}
		else if(situation == MusicSituation.PLAY_OR_PAUSE){
			playOrPause();
		}
		initLrc();
		return super.onStartCommand(intent, flags, startId);
	}

	private void play() {
        try {
			mp.reset();
			path = SourceDateList.get(current).getPath();
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
			handler.sendEmptyMessage(1);
			Intent intent = new Intent("PLAY_OR_PAUSE");
			Bundle bundle = new Bundle();
			bundle.putBoolean("playing", true);
			intent.putExtras(bundle);
			sendBroadcast(intent);
         } catch (IllegalArgumentException e) {
            e.printStackTrace();
         } catch (IllegalStateException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
	}
	private void continvToPlay(){
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
			handler.sendEmptyMessage(1);
         } catch (IllegalArgumentException e) {
            e.printStackTrace();
         } catch (IllegalStateException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }
	}
	private void changeProgressPlay(int progress){
        try {
    		mp.reset();
			mp.setDataSource(path);
	        mp.prepare();
	        mp.start();
	        mp.seekTo(progress);
			handler.sendEmptyMessage(1);
		} catch (IllegalArgumentException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}

	}
	private void pause(){
		if(mp != null && mp.isPlaying()){
			mp.pause();
			ispause = true;
		}
	}
	private void replay(){
		if(ispause == true){
			mp.start();
		}
		ispause = false;
	}
	private void playPre(){
		if(current == 0){
			current = SourceDateList.size() - 1;
		}
		else{
			current--;
		}
		path = SourceDateList.get(current).getPath();
		Intent intent = new Intent("UPDATE_CURRENT");
		Bundle bundle = new Bundle();
		bundle.putInt("position", current);
		intent.putExtras(bundle);
		sendBroadcast(intent);
		play();
	}
	private void playNext(){
		if(current == SourceDateList.size() - 1){
			current = 0;
		}
		else{
			current++;
		}
		path = SourceDateList.get(current).getPath();
		Intent intent = new Intent("UPDATE_CURRENT");
		Bundle bundle = new Bundle();
		bundle.putInt("position", current);
		intent.putExtras(bundle);
		sendBroadcast(intent);
		play();
	}
	private void continvPlay(){
		currentTime = mp.getCurrentPosition();
		mp.seekTo(currentTime);
		continvToPlay();
	}
	private void pullToRefresh(){
		current = getRandomIndex(SourceDateList.size() - 1);
		path = SourceDateList.get(current).getPath();
		Intent intent = new Intent("UPDATE_CURRENT");
		Bundle bundle = new Bundle();
		bundle.putInt("position", current);
		intent.putExtras(bundle);
		sendBroadcast(intent);

		play();
	}
	private void playOrPause(){

		if(mp != null){
			if(mp.isPlaying()){
				pause();
				Intent intent = new Intent("PLAY_OR_PAUSE");
				Bundle bundle = new Bundle();
				bundle.putBoolean("playing", false);
				intent.putExtras(bundle);
				sendBroadcast(intent);
			}
			else{
				replay();
				Intent intent = new Intent("PLAY_OR_PAUSE");
				Bundle bundle = new Bundle();
				bundle.putBoolean("playing", true);
				intent.putExtras(bundle);
				sendBroadcast(intent);
			}
		}	
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
			if(action.equals("HOW_TO_PLAY")){
				int flag = intent.getExtras().getInt("how");
				if(flag == MusicSituation.PLAY_ONE_SONG){
					HowToPlay = 1;
				}
				else if(flag == MusicSituation.PLAY_BY_ORDER){
					HowToPlay = 2;
				}
				else if(flag == MusicSituation.PLAY_RANDOM){
					HowToPlay = 3;
				}
			}
			else if(action.equals("FINISH")){
				stopSelf();
			}
		}
	}
	@Override
	public void onDestroy() {
		this.unregisterReceiver(homeReceiver);
		mp.release();
		super.onDestroy();
	}
	
}
