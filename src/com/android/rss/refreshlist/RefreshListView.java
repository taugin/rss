package com.android.rss.refreshlist;

import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.rss.R;
import com.android.rss.util.Log;

public class RefreshListView extends ListView implements OnScrollListener {

    private static final String TAG = "RefreshListView";

    private final static int STATE_ARROW_UP = 0;
    private final static int STATE_ARROW_DOWN = 1;
    private final static int STATE_REFRESHING = 2;
    private final static int STATE_DONE = 3;
    private final static int STATE_LOADING = 4;
    
    private final static int ACTION_DOWN_UP = 1;
    private final static int ACTION_UP_DOWN = 2;

    private final static int RATIO = 3;

    private LayoutInflater mLayoutInflater;

    private RelativeLayout mHeaderView;

    private TextView mTipsTextView;
    private TextView mLastUpdatedTextView;
    private ImageView mImageArrow;
    private ProgressBar mProgressBar;


    private RotateAnimation mRotateAnimation;
    private RotateAnimation mReverseAnimation;

    private boolean mRecorded;

    private int mHeadContentWidth;
    private int mHeadContentHeight;

    private int mStartY;
    private int mFirstItemIndex;

    private int mState;
    
    private int mAction;

    private boolean mBacked;

    private OnRefreshListener mRefreshListener;

    private boolean mRefreshable;

    public RefreshListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setCacheColorHint(context.getResources().getColor(R.color.transparent));
        mLayoutInflater = LayoutInflater.from(context);

        mHeaderView = (RelativeLayout) mLayoutInflater.inflate(R.layout.head, null);
        mHeaderView.setBackgroundColor(Color.BLACK);
        mImageArrow = (ImageView) mHeaderView.findViewById(R.id.head_arrowImageView);
//        mImageArrow.setMinimumWidth(70);
//        mImageArrow.setMinimumHeight(50);
        mProgressBar = (ProgressBar) mHeaderView.findViewById(R.id.head_progressBar);
        mTipsTextView = (TextView) mHeaderView.findViewById(R.id.head_tipsTextView);
        mLastUpdatedTextView = (TextView) mHeaderView.findViewById(R.id.head_lastUpdatedTextView);

        measureView(mHeaderView);
        mHeadContentHeight = mHeaderView.getMeasuredHeight();
        mHeadContentWidth = mHeaderView.getMeasuredWidth();

        mHeaderView.setPadding(0, -1 * mHeadContentHeight, 0, 0);
        mHeaderView.invalidate();

        Log.v("size", "width:" + mHeadContentWidth + " height:"
                + mHeadContentHeight);

        addHeaderView(mHeaderView, null, false);
        setOnScrollListener(this);

        mRotateAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setDuration(250);
        mRotateAnimation.setFillAfter(true);

        mReverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseAnimation.setInterpolator(new LinearInterpolator());
        mReverseAnimation.setDuration(200);
        mReverseAnimation.setFillAfter(true);

        mState = STATE_DONE;
        mRefreshable = false;
    }

    public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2,
            int arg3) {
        mFirstItemIndex = firstVisiableItem;
    }

    public void onScrollStateChanged(AbsListView arg0, int arg1) {
    }
/*
    public boolean onTouchEvent(MotionEvent event) {

        if (!mRefreshable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (mFirstItemIndex == 0 && !mRecorded) {
                mRecorded = true;
                mStartY = (int) event.getY();
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mState != STATE_REFRESHING && mState != STATE_LOADING) {
                if (mState == STATE_DONE) {
                }
                if (mState == STATE_ARROW_DOWN) {
                    mState = STATE_DONE;
                    changeHeaderViewByState();

                }
                if (mState == STATE_ARROW_UP) {
                    mState = STATE_REFRESHING;
                    changeHeaderViewByState();
                    onRefresh();
                }
            }

            mRecorded = false;
            mBacked = false;

            break;

        case MotionEvent.ACTION_MOVE:
            int tempY = (int) event.getY();

            if (!mRecorded && mFirstItemIndex == 0) {
                mRecorded = true;
                mStartY = tempY;
            }

            if (mState != STATE_REFRESHING && mRecorded
                    && mState != STATE_LOADING) {

                if (mState == STATE_ARROW_UP) {

                    setSelection(0);

                    if (((tempY - mStartY) / RATIO < mHeadContentHeight)
                            && (tempY - mStartY) > 0) {
                        mState = STATE_ARROW_DOWN;
                        changeHeaderViewByState();

                    }else if (tempY - mStartY <= 0) {
                        mState = STATE_DONE;
                        changeHeaderViewByState();

                    }else {
                    }
                }
                if (mState == STATE_ARROW_DOWN) {

                    setSelection(0);

                    if ((tempY - mStartY) / RATIO >= mHeadContentHeight) {
                        mState = STATE_ARROW_UP;
                        mBacked = true;
                        changeHeaderViewByState();

                    }else if (tempY - mStartY <= 0) {
                        mState = STATE_DONE;
                        changeHeaderViewByState();
                    }
                }

                if (mState == STATE_DONE) {
                    if (tempY - mStartY > 0) {
                        mState = STATE_ARROW_DOWN;
                        changeHeaderViewByState();
                    }
                }

                if (mState == STATE_ARROW_DOWN) {
                    mHeaderView.setPadding(0, -1 * mHeadContentHeight
                            + (tempY - mStartY) / RATIO, 0, 0);

                }

                if (mState == STATE_ARROW_UP) {
                    mHeaderView.setPadding(0, (tempY - mStartY) / RATIO
                            - mHeadContentHeight, 0, 0);
                }

            }

            break;
        }

        return super.onTouchEvent(event);
    }
*/
    private int mLastY = 0;
    public boolean onTouchEvent(MotionEvent event) {
        if(mState == STATE_LOADING){
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        switch(action){
        case MotionEvent.ACTION_DOWN:
            if(mFirstItemIndex == 0){
                mStartY = (int) event.getY();
                mRecorded = true;
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if(!mRecorded){
                mRecorded = false;
                return super.onTouchEvent(event);
            }
            int curY = (int) event.getY();
            int paddingTop =  (curY - mStartY) / 3;
            Log.d(TAG, "paddingTop = " + paddingTop +  " , mHeadContentHeight = " + mHeadContentHeight);
            if(paddingTop < mHeadContentHeight){
                mState = STATE_ARROW_DOWN;
            }
            if(paddingTop > mHeadContentHeight){
                mState = STATE_ARROW_UP;
            }
            if(paddingTop == mHeadContentHeight){
                if(curY - mLastY > 0){
                    mAction = ACTION_DOWN_UP;
                }else{
                    mAction = ACTION_UP_DOWN;
                }
            }
            mLastY = curY;
            updateListView(paddingTop);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return super.onTouchEvent(event);
    }
    
    private void updateListView(int paddingTop){
        Log.d(TAG, "padding = " + paddingTop);
        switch(mState){
        case STATE_ARROW_UP:
            Log.d(TAG, "STATE_ARROW_UP");
            mImageArrow.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);
            mTipsTextView.setText(R.string.poll_out_refresh);
            mHeaderView.setPadding(0, paddingTop, 0, 0);
            mImageArrow.setVisibility(View.VISIBLE);
            mImageArrow.clearAnimation();
            if(mAction == ACTION_DOWN_UP){
                mImageArrow.startAnimation(mRotateAnimation);
            }
            if(mAction == ACTION_UP_DOWN){
                mImageArrow.startAnimation(mReverseAnimation);
            }
            mAction = 0;
            break;
        case STATE_ARROW_DOWN:
            Log.d(TAG, "STATE_ARROW_DOWN");
            mProgressBar.setVisibility(View.GONE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);
            mHeaderView.setPadding(0, paddingTop, 0, 0);
            mTipsTextView.setText(R.string.pull_down_to_refresh);
            mImageArrow.setVisibility(View.VISIBLE);
            mImageArrow.clearAnimation();
            if(mAction == ACTION_DOWN_UP){
                mImageArrow.startAnimation(mRotateAnimation);
            }
            if(mAction == ACTION_UP_DOWN){
                mImageArrow.startAnimation(mReverseAnimation);
            }
            mAction = 0;
            break;

        case STATE_REFRESHING:

            mHeaderView.setPadding(0, 0, 0, 0);

            mProgressBar.setVisibility(View.VISIBLE);
            mImageArrow.clearAnimation();
            mImageArrow.setVisibility(View.GONE);
            mTipsTextView.setText(R.string.refreshing);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);

            break;
        case STATE_DONE:
            mHeaderView.setPadding(0, -1 * mHeadContentHeight, 0, 0);

            mProgressBar.setVisibility(View.GONE);
            mImageArrow.clearAnimation();
            mImageArrow.setImageResource(R.drawable.arrow);
            mTipsTextView.setText("����ˢ��");
            mLastUpdatedTextView.setVisibility(View.VISIBLE);

            break;
        }
    }
    private void changeHeaderViewByState() {
        switch (mState) {
        case STATE_ARROW_UP:
            mImageArrow.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);

            mImageArrow.clearAnimation();
            mImageArrow.startAnimation(mRotateAnimation);

            mTipsTextView.setText(R.string.poll_out_refresh);

            break;
        case STATE_ARROW_DOWN:
            mProgressBar.setVisibility(View.GONE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);
            mImageArrow.clearAnimation();
            mImageArrow.setVisibility(View.VISIBLE);
            if (mBacked) {
                mBacked = false;
                mImageArrow.clearAnimation();
                mImageArrow.startAnimation(mReverseAnimation);

                mTipsTextView.setText(R.string.pull_down_to_refresh);
            } else {
                mTipsTextView.setText(R.string.pull_down_to_refresh);
            }
            break;

        case STATE_REFRESHING:

            mHeaderView.setPadding(0, 0, 0, 0);

            mProgressBar.setVisibility(View.VISIBLE);
            mImageArrow.clearAnimation();
            mImageArrow.setVisibility(View.GONE);
            mTipsTextView.setText(R.string.refreshing);
            mLastUpdatedTextView.setVisibility(View.VISIBLE);

            break;
        case STATE_DONE:
            mHeaderView.setPadding(0, -1 * mHeadContentHeight, 0, 0);

            mProgressBar.setVisibility(View.GONE);
            mImageArrow.clearAnimation();
            mImageArrow.setImageResource(R.drawable.arrow);
            mTipsTextView.setText("����ˢ��");
            mLastUpdatedTextView.setVisibility(View.VISIBLE);

            break;
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
        mRefreshable = true;
    }

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public void refreshComplete() {
        mState = STATE_DONE;
        String time = getResources().getString(R.string.cur_refresh);
        time += " : " + new Date().toLocaleString();
        mLastUpdatedTextView.setText(time);
        changeHeaderViewByState();
    }

    private void onRefresh() {
        if (mRefreshListener != null) {
            mRefreshListener.onRefresh();
        }
    }

    public void setRefreshState(){
        mState = STATE_REFRESHING;
        changeHeaderViewByState();
    }
    private void measureView(View child) {
        AbsListView.LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT);
            child.setLayoutParams(p);
        }
        int childWidthSpec = AbsListView.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void setAdapter(BaseAdapter adapter) {
        String lastDate = getResources().getString(R.string.last_refresh);
        lastDate += " : " + new Date().toLocaleString();
        mLastUpdatedTextView.setText(lastDate);
        super.setAdapter(adapter);
    }

}
