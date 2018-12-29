package com.mobiistar.starbud.view.activity.start;

import android.content.Intent;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.view.activity.base.BaseActivity;
import com.mobiistar.starbud.view.activity.firmware.BleOtaActivity;
import com.mobiistar.starbud.view.activity.firmware.FirmwareUpgradeActivity;

/**
 * Description: Splash activity, the first activity.
 * Date：18-11-2-下午3:39
 * Author: black
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init() {
        Intent intent = new Intent();
        intent.setClass(this, GuideActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void unInit() {

    }
}
