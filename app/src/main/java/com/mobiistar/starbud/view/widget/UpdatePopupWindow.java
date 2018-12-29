package com.mobiistar.starbud.view.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mobiistar.starbud.R;

/**
 * Description: Update popupWindow for app update and firmware update.
 * Date：18-11-14-上午9:44
 * Author: black
 */
public class UpdatePopupWindow extends PopupWindow {
    private TextView tvTitle;
    private TextView tvVersion;
    private TextView tvContent;
    private OnClickListener mOnClickListener;

    public UpdatePopupWindow(Context context) {
        View view = View.inflate(context, R.layout.popup_window_update_lay, null);
        tvTitle = view.findViewById(R.id.tv_title);
        tvVersion = view.findViewById(R.id.tv_version);
        tvContent = view.findViewById(R.id.tv_content);
        view.findViewById(R.id.tv_cancel).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_confirm).setOnClickListener(onClickListener);

        tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setBackgroundDrawable(new ColorDrawable(0));
        setContentView(view);
        setOutsideTouchable(true);
        setTouchable(true);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnClickListener == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.tv_cancel:
                    mOnClickListener.onCancelClick();
                    break;
                case R.id.tv_confirm:
                    mOnClickListener.onConfirmClick();
                    break;
                default:
                    break;
            }
        }
    };

    public interface OnClickListener {
        void onCancelClick();

        void onConfirmClick();
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public void setVersion(String version) {
        tvVersion.setText(version);
    }

    public void setContent(String content) {
        tvContent.setText(content);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
