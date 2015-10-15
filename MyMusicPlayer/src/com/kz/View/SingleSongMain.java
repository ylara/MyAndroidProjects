package com.kz.View;

import com.kz.activity.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SingleSongMain extends BaseFragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.single_song_main, null);
	}

	@Override
	public void setCurrent(int current) {
		super.setCurrent(current);
	}



	@Override
	public int getCurrent() {
		return super.getCurrent();
	}

	@Override
	public void setLyric(int lyric) {
		super.setLyric(lyric);
	}

	@Override
	public int getLyric() {
		return super.getLyric();
	}


}
