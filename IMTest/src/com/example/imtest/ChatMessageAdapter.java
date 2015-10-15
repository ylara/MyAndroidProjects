package com.example.imtest;

import java.util.List;

import com.example.imtest.ChatMessage.Type;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatMessageAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private List<ChatMessage> mDatas;
	private Context mContext;

	public ChatMessageAdapter(Context context, List<ChatMessage> datas,Context mContext)
	{
		mInflater = LayoutInflater.from(context);
		mDatas = datas;
		this.mContext = mContext;
	}

	@Override
	public int getCount()
	{
		return mDatas.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	/**
	 * 接受到消息为1，发送消息为0
	 */
	@Override
	public int getItemViewType(int position)
	{
		switch(mDatas.get(position).getType()){
		case INPUT:
			return 0;
		case OUTPUT:
			return 1;
		case INPUT_IMAGE:
			return 2;
		case OUTPUT_IMAGE:
			return 3;
		default:
				return 0;
		}
	}

	@Override
	public int getViewTypeCount()
	{
		return 4;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ChatMessage chatMessage = mDatas.get(position);

		ViewHolder viewHolder = null;
		
		ImageViewHolder imageViewHolder = null;

		if (convertView == null)
		{
			viewHolder = new ViewHolder();
			imageViewHolder = new ImageViewHolder();
			if (chatMessage.getType() == Type.INPUT)
			{
				convertView = mInflater.inflate(R.layout.main_chat_from_msg,
						parent, false);
				viewHolder.createDate = (TextView) convertView
						.findViewById(R.id.chat_from_createDate);
				viewHolder.content = (TextView) convertView
						.findViewById(R.id.chat_from_content);
				viewHolder.name = (TextView) convertView
						.findViewById(R.id.chat_from_name);
				viewHolder.content.setText(chatMessage.getMsg());
				viewHolder.createDate.setText(chatMessage.getDateStr());
				viewHolder.name.setText(chatMessage.getName());
				convertView.setTag(viewHolder);
			} else if(chatMessage.getType() == Type.OUTPUT){
				convertView = mInflater.inflate(R.layout.main_chat_send_msg,
						null);

				viewHolder.createDate = (TextView) convertView
						.findViewById(R.id.chat_send_createDate);
				viewHolder.content = (TextView) convertView
						.findViewById(R.id.chat_send_content);
				viewHolder.name = (TextView)convertView
						.findViewById(R.id.chat_send_name);
				viewHolder.content.setText(chatMessage.getMsg());
				viewHolder.createDate.setText(chatMessage.getDateStr());
				viewHolder.name.setText(chatMessage.getName());
				convertView.setTag(viewHolder);
			} else if(chatMessage.getType() == Type.INPUT_IMAGE){
				convertView = mInflater.inflate(R.layout.main_chat_from_image,
						parent, false);
				imageViewHolder.createDate_image = (TextView) convertView
						.findViewById(R.id.chat_from_createDate_image);
				imageViewHolder.content_image = (ImageView) convertView
						.findViewById(R.id.chat_from_content_image);
				imageViewHolder.name_image = (TextView) convertView
						.findViewById(R.id.chat_from_name_image);
				//Log.d("path", mDatas.get(position).getMsg());
				Bitmap image = decodeSampledBitmapFromFile(mDatas.get(position).getMsg(), 150,150);
				Drawable drawable = new BitmapDrawable(mContext.getResources(),image); 
				imageViewHolder.content_image.setBackground(drawable);
				imageViewHolder.createDate_image.setText(chatMessage.getDateStr());
				imageViewHolder.name_image.setText(chatMessage.getName());
				convertView.setTag(imageViewHolder);
			} else if(chatMessage.getType() == Type.OUTPUT_IMAGE){
				convertView = mInflater.inflate(R.layout.main_chat_send_image,
						parent, false);
				imageViewHolder.createDate_image = (TextView) convertView
						.findViewById(R.id.chat_send_createDate_image);
				imageViewHolder.content_image = (ImageView) convertView
						.findViewById(R.id.chat_send_content_image);
				imageViewHolder.name_image = (TextView) convertView
						.findViewById(R.id.chat_send_name_image);
				//Log.d("path", mDatas.get(position).getMsg());
				Bitmap image = decodeSampledBitmapFromFile(mDatas.get(position).getMsg(), 150,150);
				Drawable drawable = new BitmapDrawable(mContext.getResources(),image); 
				imageViewHolder.content_image.setBackground(drawable);
				imageViewHolder.createDate_image.setText(chatMessage.getDateStr());
				imageViewHolder.name_image.setText(chatMessage.getName());
				convertView.setTag(imageViewHolder);
			}

		} else
		{
			if(chatMessage.getType() == Type.INPUT || chatMessage.getType() == Type.OUTPUT){
				viewHolder = (ViewHolder) convertView.getTag();
				viewHolder.content.setText(chatMessage.getMsg());
				viewHolder.createDate.setText(chatMessage.getDateStr());
				viewHolder.name.setText(chatMessage.getName());
			}else if(chatMessage.getType() == Type.INPUT_IMAGE || chatMessage.getType() == Type.OUTPUT_IMAGE){
				imageViewHolder = (ImageViewHolder)convertView.getTag();
				Bitmap image = decodeSampledBitmapFromFile(mDatas.get(position).getMsg(), 150,150);
				Drawable drawable = new BitmapDrawable(mContext.getResources(),image); 
				imageViewHolder.content_image.setBackground(drawable);
				imageViewHolder.createDate_image.setText(chatMessage.getDateStr());
				imageViewHolder.name_image.setText(chatMessage.getName());
			}
		}

		return convertView;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,   
	        int reqWidth, int reqHeight) {   
	    // ԴͼƬ�ĸ߶ȺͿ��   
	    final int height = options.outHeight;   
	    final int width = options.outWidth;   
	    int inSampleSize = 1;   
	    if (height > reqHeight || width > reqWidth) {   
	        // �����ʵ�ʿ�ߺ�Ŀ���ߵı���   
	        final int heightRatio = Math.round((float) height / (float) reqHeight);   
	        final int widthRatio = Math.round((float) width / (float) reqWidth);   
	        // ѡ���͸�����С�ı�����ΪinSampleSize��ֵ���������Ա�֤����ͼƬ�Ŀ�͸�   
	        // һ��������ڵ���Ŀ��Ŀ�͸ߡ�   
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;   
	    }   
	    return inSampleSize;   
	} 
	
	public static Bitmap decodeSampledBitmapFromFile(String path,   
	        int reqWidth, int reqHeight) {   
	    // ��һ�ν�����inJustDecodeBounds����Ϊtrue������ȡͼƬ��С   
	    final BitmapFactory.Options options = new BitmapFactory.Options();   
	    options.inJustDecodeBounds = true;   
	    BitmapFactory.decodeFile(path, options);
	    // �������涨��ķ�������inSampleSizeֵ   
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);   
	    // ʹ�û�ȡ����inSampleSizeֵ�ٴν���ͼƬ   
	    options.inJustDecodeBounds = false;   
	    return BitmapFactory.decodeFile(path, options); 
	} 
	
	
	private class ViewHolder
	{
		public TextView createDate;
		public TextView name;
		public TextView content;
	}

	public class ImageViewHolder{
		public TextView createDate_image;
		public TextView name_image;
		public ImageView content_image;
	}
}
