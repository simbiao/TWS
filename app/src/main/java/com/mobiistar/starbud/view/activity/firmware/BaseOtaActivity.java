package com.mobiistar.starbud.view.activity.firmware;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.view.View;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.FileUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.TextUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;

import java.io.File;

/**
 * Description: Ota activity.
 * Date：18-11-15-上午10:33
 * Author: black
 */
public class BaseOtaActivity extends BaseActivity {
    protected BluetoothAdapter adapter;
    private String mDirection;
    private String mProductName;
    protected String mOtaFileUrl;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_ota_base;
    }

    @Override
    protected void init() {
        mDirection = getIntent().getStringExtra(StarUtil.KEY_DIRECTION);
        mProductName = getIntent().getStringExtra(StarUtil.KEY_PRODUCT_NAME);

        getOtaFileUrl();

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
    }

    @Override
    protected void unInit() {

    }

    protected void getOtaFileUrl() {
        String fileName = "";
        String fwSaveUrl = StarUtil.getFWSaveUrl();
        String dirPath = FileUtil.getParentPath(fwSaveUrl) + File.separator + FileUtil.getFileName(fwSaveUrl) + File.separator;
        if (isLeft()) {
            fileName = changeName(mProductName) + StarUtil._LEFT + StarUtil.EXTENSION_NAME_BIN;
        } else if (isRight()) {
            fileName = changeName(mProductName) + StarUtil._RIGHT + StarUtil.EXTENSION_NAME_BIN;
        }
        if (!TextUtil.isEmpty(fileName)) {
            mOtaFileUrl = dirPath + fileName;
        }
        printLogE("getOtaFileUrl...mOtaFileUrl==" + mOtaFileUrl);
    }

    private String changeName(String name) {
        if (TextUtil.isEmpty(name)) {
            return "";
        } else {
            return name.replace(" ", "_");
        }
    }

    private boolean isLeft() {
        return StarUtil._LEFT.equals(mDirection);
    }

    private boolean isRight() {
        return StarUtil._RIGHT.equals(mDirection);
    }

    public void onNextStepClick(View view) {
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
