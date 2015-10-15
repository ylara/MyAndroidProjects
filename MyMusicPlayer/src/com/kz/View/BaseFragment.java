package com.kz.View;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
	private int current;
	private int lyric;
	public void setCurrent(int current){
		this.current = current;
	}
	public void setLyric(int lyric){
		this.lyric = lyric;
	}
	public int getCurrent(){
		return this.current;
	}
	public int getLyric(){
		return this.lyric;
	}
}
