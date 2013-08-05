package com.android.rss.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;

public class NewsView extends WebView {

    private int mDownX, mDownY;
    private int mTouchSlop = 0;
    enum DIR{LEFT, RIGHT, NO};
    private DIR mDir = DIR.NO;
    public NewsView(Context context) {
        super(context);
        init(context);
    }
    public NewsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public NewsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    private void init(Context context){
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }
    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
            boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean  canScrollLeft = this.canScrollHorizontally(-1);
        boolean  canScrollRight = this.canScrollHorizontally(1);
        switch(action){
        case MotionEvent.ACTION_DOWN:
            mDownX = (int) event.getX();
            mDownY = (int) event.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            int curX = (int) event.getX();
            int curY = (int) event.getY();
            if(curX < mDownX){
                mDir = DIR.LEFT;
            }else if(curX == mDownX){
                mDir = DIR.NO;
            }else{
                mDir = DIR.RIGHT;
            }
            int delta = Math.abs(curX - mDownX);
            if(delta > mTouchSlop && !canScrollRight && mDir == DIR.LEFT){
//                LogUtils.debug(TAG, "LEFT");
            }
            if(delta > mTouchSlop && !canScrollLeft && mDir == DIR.RIGHT){
//                LogUtils.debug(TAG, "RIGHT");
            }
            
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            break;
        default:
            break;
        }
        return super.onTouchEvent(event);
    }

    
}
