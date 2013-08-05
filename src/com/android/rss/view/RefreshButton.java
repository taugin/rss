package com.android.rss.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class RefreshButton extends ImageButton {

    public RefreshButton(Context context) {
        super(context);
    }
    public RefreshButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public RefreshButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public void setPressed(boolean pressed) {
        View parent = (View) getParent();
        if(parent.isPressed()){
            pressed = false;
        }
        super.setPressed(pressed);
    }
}
