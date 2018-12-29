package com.mobiistar.starbud.view.activity.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import android.widget.ListView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.bean.VersionInfo;
import com.mobiistar.starbud.model.StarModelCallback;
import com.mobiistar.starbud.model.StarModelHelp;
import com.mobiistar.starbud.util.BluetoothUtil;
import com.mobiistar.starbud.util.JsonUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.TextUtil;

import java.util.List;

/**
 * Description:
 * Date：18-11-2-下午3:39
 * Author: black
 */
public class SearchActivity extends BluetoothActivity {

    public static final String ACTION_REFRESH = "action_refresh";

    private SearchDeviceAdapter pairedAdapter;
    private SearchDeviceAdapter unpairedAdapter;
    private AnimationDrawable mAnimationDrawable;
    private boolean isShowing;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    protected void init() {

        ListView lvPaired = findViewById(R.id.lv_paired);
        ListView lvUnpaired = findViewById(R.id.lv_unpaired);
        ImageView ivConnectingAnim = findViewById(R.id.iv_connecting_anim);
        pairedAdapter = new SearchDeviceAdapter(this);
        unpairedAdapter = new SearchDeviceAdapter(this);
        lvPaired.setAdapter(pairedAdapter);
        lvUnpaired.setAdapter(unpairedAdapter);

        mAnimationDrawable = (AnimationDrawable) ivConnectingAnim.getDrawable();
        mAnimationDrawable.start();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(receiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_REFRESH);
        registerReceiver(refreshReceiver, intentFilter);

        StarModelHelp.getHttpResponse(StarUtil.VERSION_INFO_URL, starModelCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isShowing = true;
        if (adapter != null && !adapter.isDiscovering()) {
            adapter.startDiscovery();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowing = false;
        if (adapter != null && adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }
    }

    @Override
    protected void unInit() {
        mAnimationDrawable.stop();
        if (adapter != null) {
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
        }
        unregisterReceiver(receiver);
        unregisterReceiver(refreshReceiver);
    }

    @Override
    protected void initData() {
        super.initData();
        if (adapter == null) {
            return;
        }
        pairedAdapter.setBluetoothProfile(headset, a2dp);
        unpairedAdapter.setBluetoothProfile(headset, a2dp);
        List<BluetoothDevice> deviceList = BluetoothUtil.getBondedDevices(adapter);
        if (hasConnectedDevice(deviceList)) {
            if (!isInDuration(1000)) {
                Intent intentSkip = new Intent(mContext, DetailActivity.class);
                startActivity(intentSkip);
            }
            finish();
            return;
        }
        pairedAdapter.setData(deviceList);

        if (!adapter.isDiscovering()) {
            adapter.startDiscovery();
        }
    }

    private boolean hasConnectedDevice(List<BluetoothDevice> deviceList) {
        if (deviceList != null) {
            for (BluetoothDevice device : deviceList) {
                if (BluetoothUtil.isConnected(device, a2dp, headset)) {
                    return true;
                }
            }
        }
        return false;
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
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        getProfileProxy();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    printLogE("onReceive...ACTION_DISCOVERY_FINISHED...adapter==" + adapter);
                    printLogE("onReceive...ACTION_DISCOVERY_FINISHED...isShowing==" + isShowing);
                    if (adapter != null && isShowing) {
                        unpairedAdapter.setData(null);
                        adapter.startDiscovery();
                    }
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    printLogE("onReceive...ACTION_FOUND...device==" + (device != null ? device.getName() : null));
                    if (device != null && StarUtil.canShow(device) && !containDeviceInBonded(device)) {
                        unpairedAdapter.addDevice(device);
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    printLogE("onReceive...ACTION_BOND_STATE_CHANGED...device==" + device);
                    if (device != null && !StarUtil.canShow(device)) {
                        break;
                    }
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    printLogE("onReceive...ACTION_BOND_STATE_CHANGED...bondState==" + bondState);
                    if (bondState == BluetoothDevice.BOND_BONDING) {
                        unpairedAdapter.addDevice(device);
                    } else if (bondState == BluetoothDevice.BOND_BONDED) {
                        pairedAdapter.addDevice(device);
                        unpairedAdapter.removeDevice(device);
                    } else {
                        pairedAdapter.removeDevice(device);
                    }
                    break;
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

    private boolean containDeviceInBonded(BluetoothDevice device) {
        String address = device != null ? device.getAddress() : null;
        if (!TextUtil.isEmpty(address)) {
            List<BluetoothDevice> deviceList = BluetoothUtil.getBondedDevices(adapter);
            for (BluetoothDevice deviceTemp : deviceList) {
                if (deviceTemp != null && address.equals(deviceTemp.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    private BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            printLogE("refreshReceiver...onReceive...getAction==" + intent.getAction());
            if (ACTION_REFRESH.equals(intent.getAction())) {
                initData();
            }
        }
    };

    private StarModelCallback starModelCallback = new StarModelCallback() {
        @Override
        public void onFailed(int code) {
        }

        @Override
        public void onProgress(int progress) {
        }

        @Override
        public void onSuccess(String result) {
            printLogE("onSuccess...result==" + result);
            try {
                VersionInfo versionInfo = JsonUtil.parseObject(result, VersionInfo.class);
                StarUtil.setWhiteList(versionInfo.getProductNameWhiteList());
            } catch (Exception e) {
                printLogE(e);
            }
        }
    };
}
