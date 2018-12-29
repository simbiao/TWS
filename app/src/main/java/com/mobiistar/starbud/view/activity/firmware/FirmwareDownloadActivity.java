package com.mobiistar.starbud.view.activity.firmware;

import android.content.Intent;

import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.view.activity.download.DownloadActivity;

/**
 * Description: Firmware download activity.
 * Date：18-11-15-上午10:35
 * Author: black
 */
public class FirmwareDownloadActivity extends DownloadActivity {
    private String mProductName;

    @Override
    protected void init() {
        super.init();

        mProductName = getIntent().getStringExtra(StarUtil.KEY_PRODUCT_NAME);
    }

    @Override
    protected void finishDownload(String zipUrl) {
        Intent intent = new Intent(mContext, BeforeOtaActivity.class);
        intent.putExtra(StarUtil.KEY_DIRECTION, StarUtil._LEFT);
        intent.putExtra(StarUtil.KEY_PRODUCT_NAME, mProductName);
        startActivity(intent);
        finish();
    }
}
