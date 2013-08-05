package com.android.rss.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class UrlInputView extends EditText {

    private OnHideInputViewListener mOnHideInputViewListener;
    public UrlInputView(Context context) {
        super(context);
    }
    public UrlInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public UrlInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            if(mOnHideInputViewListener != null){
                mOnHideInputViewListener.hideInputView();
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
    public void setOnHideInputViewListener(OnHideInputViewListener listener){
        mOnHideInputViewListener = listener;
    }
    public interface OnHideInputViewListener{
        public void hideInputView();
    }
    
}
