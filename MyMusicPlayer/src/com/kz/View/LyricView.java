package com.kz.View;

import java.util.ArrayList;
import java.util.List;
import com.kz.Util.LrcContent;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class LyricView extends TextView {
    private float width;       
    private float height;       
    private Paint currentPaint; 
    private Paint notCurrentPaint; 
    private float textHeight = 25; 
    private float textSize = 30;       
    private int index = 0;     
      
      
    private List<LrcContent> mLrcList = new ArrayList<LrcContent>();  
      
    public  void setmLrcList(List<LrcContent> mLrcList) {  
        this.mLrcList = mLrcList;  
    }  
    public LyricView(){
    	this(null,null,0);
    }
    public LyricView(Context context) {  
        super(context);  
        init();  
    }  
    
    public LyricView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
        init();  
    }  
  
    public LyricView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        init();  
    }  
  
    private void init() {  
        setFocusable(true);    
          
        currentPaint = new Paint();  
        currentPaint.setAntiAlias(true);    
        currentPaint.setTextAlign(Paint.Align.CENTER);
          
        notCurrentPaint = new Paint();  
        notCurrentPaint.setAntiAlias(true);  
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);
        
    }  
      
    @Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        if(canvas == null) {  
            return;  
        }    
        currentPaint.setColor(Color.RED);  
        notCurrentPaint.setColor(Color.BLACK);  
          
        currentPaint.setTextSize(35);  
        currentPaint.setTypeface(Typeface.SERIF);  
          
        notCurrentPaint.setTextSize(textSize);  
        notCurrentPaint.setTypeface(Typeface.DEFAULT);  
          
        try {  
            setText("");  
            canvas.drawText(mLrcList.get(index).getLrcStr(), width / 2, height / 2, currentPaint);  

            float tempY = height / 2;  
            for(int i = index - 1; i >= 0; i--) {  
                tempY = tempY - textHeight;  
                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);  
            }  
            tempY = height / 2;  
            for(int i = index + 1; i < mLrcList.size(); i++) {  
                tempY = tempY + textHeight;  
                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);  
            }   
        } catch (Exception e) {  
            setText("...还没有歌词...");  
        } 
    }  
  
    @Override  
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {  
        super.onSizeChanged(w, h, oldw, oldh);  
        this.width = w;  
        this.height = h;  
    }  
  
    public void setIndex(int index) {  
        this.index = index;  
    }  
      
}  
