package com.mobiistar.starbud.view.activity.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.BluetoothUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.TextUtil;

/**
 * Description:
 * Date：18-11-7-下午4:38
 * Author: black
 */
public class PairedDeviceActivity extends BluetoothActivity {

    private static final int RENAME_REQUEST_CODE = 20000;

    private BluetoothDevice mDevice;
    private TextView tvName;
    private Switch switchAudio;
    private Switch switchMedia;
    private View audioLay;
    private View mediaLay;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_paired_device;
    }

    @Override
    protected void init() {
        tvName = findViewById(R.id.tv_name);
        switchAudio = findViewById(R.id.switch_audio);
        switchMedia = findViewById(R.id.switch_media);
        View mainLay = findViewById(R.id.main_lay);
        TextView tvCancelPaired = findViewById(R.id.tv_cancel_paired);
        audioLay = findViewById(R.id.audio_lay);
        mediaLay = findViewById(R.id.media_lay);

        mDevice = getDeviceFromIntent(getIntent());
        if (mDevice == null) {
            mainLay.setVisibility(View.GONE);
            tvCancelPaired.setVisibility(View.GONE);
            return;
        }
        tvName.setText(BluetoothUtil.getAliasName(mDevice));
        switchAudio.setOnCheckedChangeListener(onCheckedChangeListener);
        switchMedia.setOnCheckedChangeListener(onCheckedChangeListener);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void initData() {
        super.initData();

        updateStatus(audioLay, switchAudio, BluetoothUtil.getConnectionState(mDevice, a2dp));
        updateStatus(mediaLay, switchMedia, BluetoothUtil.getConnectionState(mDevice, headset));
    }

    private void updateStatus(View viewLay, Switch switchProfile, int connectionState) {
        boolean isEnabled = connectionState == BluetoothProfile.STATE_DISCONNECTED || connectionState == BluetoothProfile.STATE_CONNECTED;
        boolean isChecked = connectionState == BluetoothProfile.STATE_CONNECTING || connectionState == BluetoothProfile.STATE_CONNECTED;
        printLogE("updateStatus...switchProfile...connectionState==" + connectionState);
        printLogE("updateStatus...switchProfile...isChecked==" + isChecked);
        printLogE("updateStatus...switchProfile...isEnabled==" + isEnabled);
        int trackResId = isChecked ? R.mipmap.switch_bg_on : R.mipmap.switch_bg_off;
        viewLay.setEnabled(isEnabled);
        switchProfile.setEnabled(isEnabled);
        switchProfile.setTrackResource(trackResId);
        switchProfile.setChecked(isChecked);
    }

    @Override
    protected void unInit() {
        unregisterReceiver(receiver);
    }

    private BluetoothDevice getDeviceFromIntent(Intent intent) {
        try {
            return intent.getParcelableExtra(StarUtil.KEY_BLUETOOTH_DEVICE);
        } catch (Exception e) {
            printLogE(e);
        }
        return null;
    }

    public void onBackClick(View view) {
        finish();
    }

    public void onCancelPairedClick(View view) {
        BluetoothUtil.unPaired(mDevice);
        finish();
    }

    public void onRenameClick(View view) {
        Intent intent = new Intent(mContext, RenameActivity.class);
        intent.putExtra(StarUtil.KEY_BLUETOOTH_DEVICE, mDevice);
        startActivityForResult(intent, RENAME_REQUEST_CODE);
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == null || !buttonView.isPressed()) {
                return;
            }
            if (buttonView.equals(switchAudio)) {
                printLogE("onCheckedChanged...switchAudio...isChecked==" + isChecked);
                updateSwitchStatus(switchAudio, isChecked, a2dp);
            } else if (buttonView.equals(switchMedia)) {
                printLogE("onCheckedChanged...switchMedia...isChecked==" + isChecked);
                updateSwitchStatus(switchMedia, isChecked, headset);
            }
        }
    };

    private void updateSwitchStatus(Switch switchProfile, boolean isChecked, BluetoothProfile profile) {
        boolean result = isChecked ? BluetoothUtil.connectDevice(mDevice, profile) : BluetoothUtil.disconnectDevice(mDevice, profile);
        printLogE("updateSwitchStatus...switchProfile...isChecked==" + isChecked);
        printLogE("updateSwitchStatus...switchProfile...result==" + result);
        isChecked = result == isChecked;
        int trackResId = isChecked ? R.mipmap.switch_bg_on : R.mipmap.switch_bg_off;
        switchProfile.setTrackResource(trackResId);
        switchProfile.setChecked(isChecked);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RENAME_REQUEST_CODE) {
            BluetoothDevice device = getDeviceFromIntent(getIntent());
            printLogE("onActivityResult...device==" + device);
            if (device != null) {
                mDevice = device;
                tvName.setText(BluetoothUtil.getAliasName(mDevice));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        sendBroadcast(new Intent(SearchActivity.ACTION_REFRESH));
        super.finish();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            printLogE("onReceive...action==" + action);
            if (TextUtil.isEmpty(action)) {
                return;
            }
            BluetoothDevice device;
            switch (action) {
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    printLogE("onReceive...ACTION_BOND_STATE_CHANGED...device==" + device);
                    if (StarUtil.canShow(device)) {
                        initData();
                    }
                    break;
            }
        }
    };
}
