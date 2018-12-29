package com.mobiistar.starbud.view.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.mobiistar.starbud.R;

/**
 * Description: Checking version popupWindow for app update.
 * Date：18-11-20-下午4:37
 * Author: black
 */
public class CheckingPopupWindow extends PopupWindow {

    public CheckingPopupWindow(Context context) {
        View view = View.inflate(context, R.layout.popup_window_checking_version, null);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new ColorDrawable(0));
        setContentView(view);
        setOutsideTouchable(true);
        setTouchable(true);
    }
}
