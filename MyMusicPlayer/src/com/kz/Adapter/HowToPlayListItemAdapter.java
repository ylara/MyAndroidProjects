package com.kz.Adapter;

import com.kz.activity.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class HowToPlayListItemAdapter extends BaseAdapter {
	private String[] items;
	private Context mContext;
	SharedPreferences preferences;
	
	public HowToPlayListItemAdapter(Context context){
		items = new String[]{"单曲循环","顺序播放","随机播放"};
		mContext = context;
		preferences = mContext.getSharedPreferences("MyMusicPlayer", mContext.MODE_WORLD_READABLE);
	}
	@Override
	public int getCount() {
		// TODO 自动生成的方法存根
		return items.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO 自动生成的方法存根
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO 自动生成的方法存根
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int type = preferences.getInt("type", 2);
		ViewHolder vh = null;
		if(convertView == null){
			vh = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.how_to_play_list_item, null);
			vh.tv = (TextView)convertView.findViewById(R.id.textView1);
			vh.cb = (CheckBox)convertView.findViewById(R.id.checkBox1);
			convertView.setTag(vh);
		}
		else{
			vh = (ViewHolder)convertView.getTag();
		}
		if(position == type - 1){
			vh.tv.setText(items[position]);
			vh.cb.setChecked(true);
			return convertView;
		}
		else{
			vh.tv.setText(items[position]);
			vh.cb.setChecked(false);
			return convertView;
		}
	}
	
	public static class ViewHolder{
		public TextView tv;
		public CheckBox cb;
	}
}
