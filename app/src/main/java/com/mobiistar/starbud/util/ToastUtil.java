package com.mobiistar.starbud.util;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiistar.starbud.R;

/**
 * Description: Toast operation.
 * Date：18-11-7-下午2:40
 * Author: black
 */
public class ToastUtil {

    public static void showToast(Context context, String message) {
        Toast toast = new Toast(context);
        View view = View.inflate(context, R.layout.toast_lay, null);
        TextView textView = view.findViewById(R.id.tv_content);
        textView.setText(message);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showToast(Context context, int messageResId) {
        String message = context.getString(messageResId);
        showToast(context, message);
    }
}
