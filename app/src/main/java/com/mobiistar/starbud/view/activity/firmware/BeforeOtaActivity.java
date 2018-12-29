package com.mobiistar.starbud.view.activity.firmware;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;

/**
 * Description: Before ota activity.
 * Date：18-11-15-上午10:33
 * Author: black
 */
public class BeforeOtaActivity extends BaseActivity {

    private String mDirection;
    private String mProductName;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_ota_before;
    }

    @Override
    protected void init() {
        ImageView ivInstructionsAnim = findViewById(R.id.iv_instructions_anim);
        TextView tvInstructionsContent = findViewById(R.id.tv_instructions_content);

        mDirection = getIntent().getStringExtra(StarUtil.KEY_DIRECTION);
        mProductName = getIntent().getStringExtra(StarUtil.KEY_PRODUCT_NAME);

        int drawableResId = isLeft() ? R.drawable.anim_fwu_instructions_left : R.drawable.anim_fwu_instructions_right;
        StarUtil.startAnimation(ivInstructionsAnim, drawableResId);
        int textResId = isLeft() ? R.string.left : R.string.right;
        String instructionsContent = String.format(getString(R.string.fwu_instructions_content), getString(textResId));
        tvInstructionsContent.setText(instructionsContent);
    }

    @Override
    protected void unInit() {

    }

    private boolean isLeft() {
        return StarUtil._LEFT.equals(mDirection);
    }

    private boolean isRight() {
        return StarUtil._RIGHT.equals(mDirection);
    }

    public void onNextStepClick(View view) {
        Intent intent = new Intent(mContext, BleOtaActivity.class);
        intent.putExtra(StarUtil.KEY_DIRECTION, mDirection);
        intent.putExtra(StarUtil.KEY_PRODUCT_NAME, mProductName);
        startActivity(intent);
        finish();
    }

    public void onSkipClick(View view) {
        if (isInDuration(1000)) {
            return;
        }

        Intent intent = null;
        if (isLeft()) {
            intent = new Intent(mContext, BeforeOtaActivity.class);
            intent.putExtra(StarUtil.KEY_DIRECTION, StarUtil._RIGHT);
            intent.putExtra(StarUtil.KEY_PRODUCT_NAME, mProductName);
        } else if (isRight()) {
            intent = new Intent(mContext, FirmwareUpgradeFinishActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }
}
