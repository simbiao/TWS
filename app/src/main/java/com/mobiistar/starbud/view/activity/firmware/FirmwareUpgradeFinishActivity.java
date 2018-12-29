package com.mobiistar.starbud.view.activity.firmware;

import android.view.View;
import android.widget.ImageView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;

/**
 * Description:Firmware finish activity.
 * Date：18-11-15-上午10:40
 * Author: black
 */
public class FirmwareUpgradeFinishActivity extends BaseActivity {
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_firmware_upgrade_finish;
    }

    @Override
    protected void init() {
        ImageView ivInstructionsAnim = findViewById(R.id.iv_instructions_anim);
        StarUtil.startAnimation(ivInstructionsAnim, R.drawable.anim_fwu_tws_pairing);
    }

    @Override
    protected void unInit() {

    }

    public void onCloseClick(View view) {
        finish();
    }
}
