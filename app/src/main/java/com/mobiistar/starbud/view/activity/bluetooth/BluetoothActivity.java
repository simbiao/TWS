package com.mobiistar.starbud.view.activity.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;

import com.mobiistar.starbud.view.activity.base.BaseActivity;

/**
 * Description: Bluetooth activity.
 * Date：18-11-8-下午2:54
 * Author: black
 */
public abstract class BluetoothActivity extends BaseActivity {
    protected BluetoothHeadset headset;
    protected BluetoothA2dp a2dp;
    protected BluetoothAdapter adapter;
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.isEnabled()) {
            getProfileProxy();
        } else {
            adapter.enable();
        }
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter.closeProfileProxy(BluetoothProfile.A2DP, a2dp);
            adapter.closeProfileProxy(BluetoothProfile.HEADSET, headset);
        }
        super.onDestroy();
    }

    protected void getProfileProxy() {
        if (adapter == null) {
            return;
        }
        if (a2dp == null) {
            adapter.getProfileProxy(this, serviceListener, BluetoothProfile.A2DP);
        }
        if (headset == null) {
            adapter.getProfileProxy(this, serviceListener, BluetoothProfile.HEADSET);
        }
    }

    private BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP && proxy instanceof BluetoothA2dp) {
                a2dp = (BluetoothA2dp) proxy;
            } else if (profile == BluetoothProfile.HEADSET && proxy instanceof BluetoothHeadset) {
                headset = (BluetoothHeadset) proxy;
            }

            if (a2dp != null && headset != null) {
                initData();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                a2dp = null;
            } else if (profile == BluetoothProfile.HEADSET) {
                headset = null;
            }
        }
    };

    protected void initData() {

    }
}
