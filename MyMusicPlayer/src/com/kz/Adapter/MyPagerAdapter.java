package com.kz.Adapter;


import com.kz.View.SingleSongLyric;
import com.kz.View.SingleSongMain;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyPagerAdapter extends FragmentPagerAdapter {

	private SingleSongMain main; 
	private SingleSongLyric lyric;
    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
        case 0:
            if (main == null) {
            	main = new SingleSongMain();
            	//fragment_map.put(position, main);
            }
            return main;
        case 1:
            if (lyric == null) {
                lyric = new SingleSongLyric();
                //fragment_map.put(position, lyric);
            }
            return lyric;
        default:
            return null;
        }
    }


	@Override
	public int getCount() {
		return 2;
	}

}
