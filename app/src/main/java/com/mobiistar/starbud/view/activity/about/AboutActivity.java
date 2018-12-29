package com.mobiistar.starbud.view.activity.about;

import android.view.View;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.view.activity.base.BaseActivity;

/**
 * Description: About activity.
 * Date：18-11-2-下午3:39
 * Author: black
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void unInit() {

    }

    public void onCloseClick(View view) {
        finish();
    }
}
