package com.mobiistar.starbud.view.activity.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.BluetoothUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.TextUtil;
import com.mobiistar.starbud.util.ToastUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;

/**
 * Description:
 * Date：18-11-8-上午11:16
 * Author: black
 */
public class RenameActivity extends BaseActivity {
    private BluetoothDevice mDevice;
    private EditText etName;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_rename;
    }

    @Override
    protected void init() {
        setStatusBarColor(Color.TRANSPARENT);

        etName = findViewById(R.id.et_name);

        mDevice = getDeviceFromIntent(getIntent());
        if (mDevice == null) {
            return;
        }
        etName.setText(BluetoothUtil.getAliasName(mDevice));
        etName.setSelection(etName.getText().length());
    }

    @Override
    protected void unInit() {

    }

    public void onCancelClick(View view) {
        finish();
    }

    public void onConfirmClick(View view) {
        String newName = etName.getText().toString().trim();
        if (TextUtil.isEmpty(newName)) {
            ToastUtil.showToast(this, R.string.empty_name_hint);
            return;
        }
        boolean resultSetAlias = BluetoothUtil.setAlias(mDevice, newName);
        if (resultSetAlias) {
            Intent intent = new Intent();
            intent.putExtra(StarUtil.KEY_BLUETOOTH_DEVICE, mDevice);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            ToastUtil.showToast(this, R.string.rename_failed);
        }
    }

    private BluetoothDevice getDeviceFromIntent(Intent intent) {
        try {
            return intent.getParcelableExtra(StarUtil.KEY_BLUETOOTH_DEVICE);
        } catch (Exception e) {
            printLogE(e);
        }
        return null;
    }

}
