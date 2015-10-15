package com.kz.View;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.kz.Service.PlayMusicService;
import com.kz.Util.LrcContent;
import com.kz.Util.MediaUtil;
import com.kz.activity.R;
import com.kz.activity.SingleSongActivity.HomeReceiver;
import com.kz.domin.LrcProcess;
import com.kz.domin.SortModel;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SingleSongLyric extends BaseFragment{
	private LyricView tv;
	private LrcProcess mLrcProcess; 
	private List<LrcContent> lrcList = new ArrayList<LrcContent>(); 
	private List<SortModel> SourceDateList;
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;
	private HomeReceiver homeReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		SourceDateList = MediaUtil.getMp3Infos(getActivity());
		SourceDateList = filledData(SourceDateList);
		Collections.sort(SourceDateList, pinyinComparator);
        mLrcProcess = new LrcProcess();  
		//System.out.println(SourceDateList.get(5).getName());

        //initBroadcast();
	}
	/*private void initBroadcast(){
		homeReceiver = new HomeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("UPDATE_CURRENT");
		filter.addAction("MUSIC_CURRENT");
		filter.addAction("LYRIC_CURRENT");
		registerReceiver(homeReceiver, filter);
	}*/
	
	@Override
	public void onAttach(Activity activity) {
		homeReceiver = new HomeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("UPDATE_CURRENT");
		filter.addAction("MUSIC_CURRENT");
		filter.addAction("LYRIC_CURRENT");
		activity.registerReceiver(homeReceiver, filter);
		super.onAttach(activity);
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(homeReceiver);
		super.onDestroy();
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.single_song_lyric, container,false);
		tv = (LyricView)rootView.findViewById(R.id.textView1);

		//tv.invalidate();
		return rootView;
	}


	@Override
	public void setCurrent(int current) {
		super.setCurrent(current);
		//System.out.println("setCurrent" + current);
        //mLrcProcess.readLRC(SourceDateList.get(this.current).getPath());  
        //lrcList = mLrcProcess.getLrcList();  
		//System.out.println("fragment:" + this.current);
		//System.out.println(SourceDateList.size());
		//System.out.println(SourceDateList.get(current).getPath());
	}


	@Override
	public int getCurrent() {
		return super.getCurrent();
	}


	@Override
	public void setLyric(int lyric) {
		super.setLyric(lyric);
		//System.out.println("setLyric" + lyric);
		//tv.setmLrcList(lrcList);
		//tv.setIndex(lrcIndex);
		//System.out.println(lrcIndex);
		//tv.invalidate();
	}


	@Override
	public int getLyric() {
		return super.getLyric();
	}


	public class HomeReceiver extends BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals("LYRIC_CURRENT")){
		        mLrcProcess.readLRC(SourceDateList.get(intent.getExtras().getInt("current")).getPath()); 
		        lrcList = mLrcProcess.getLrcList();  
				tv.setmLrcList(lrcList);
				tv.setIndex(intent.getExtras().getInt("lyric"));
				tv.invalidate();
				
				//System.out.println("onReceive:" + " " + intent.getExtras().getInt("current"));
			}
		}
		
	}

	
}
