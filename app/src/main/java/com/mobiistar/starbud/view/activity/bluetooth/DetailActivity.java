package com.mobiistar.starbud.view.activity.bluetooth;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.util.BluetoothUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.TextUtil;
import com.mobiistar.starbud.util.ToastUtil;
import com.mobiistar.starbud.view.activity.about.AboutActivity;
import com.mobiistar.starbud.view.activity.firmware.FirmwareUpgradeActivity;
import com.mobiistar.starbud.view.activity.version.VersionActivity;

import java.util.List;

/**
 * Description: Detail activity.
 * Date：18-11-2-下午3:39
 * Author: black
 */
public class DetailActivity extends BluetoothActivity {
    public static final String ACTION_BATTERY_LEVEL_CHANGED = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";
    private BluetoothDevice mDevice;
    private ImageView ivConnectionState;
    private TextView tvSwitch;
    private View batteryLay;
    private ImageView ivBatteryLevel;
    private TextView tvPercentageBatteryLevel;
    private ImageView ivMore;
    private PopupWindow morePopupWindow;
    private boolean isMorePWShowing;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_detail;
    }

    @Override
    protected void init() {
        StarUtil.setHasEnterDetail(true);

        ivConnectionState = findViewById(R.id.iv_connection_state);
        tvSwitch = findViewById(R.id.tv_switch);
        batteryLay = findViewById(R.id.battery_lay);
        ivBatteryLevel = findViewById(R.id.iv_battery_level);
        tvPercentageBatteryLevel = findViewById(R.id.tv_percentage_battery_level);
        ivMore = findViewById(R.id.iv_more);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_BATTERY_LEVEL_CHANGED);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void initData() {
        super.initData();
        if (adapter == null || a2dp == null || headset == null) {
            return;
        }
        int connectionStateA2dp = adapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int connectionStateHeadset = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        boolean disconnected = connectionStateA2dp != BluetoothProfile.STATE_CONNECTED && connectionStateHeadset != BluetoothProfile.STATE_CONNECTED;
        printLogE("initData...connectionStateA2dp==" + connectionStateA2dp);
        printLogE("initData...connectionStateHeadset==" + connectionStateHeadset);
        printLogE("initData...disconnected==" + disconnected);
        mDevice = getConnectedDevice();
        if (mDevice == null) {
            disconnected = true;
        }
        printLogE("initData...disconnected==" + disconnected);
        if (disconnected) {
            ivConnectionState.setImageResource(R.mipmap.ic_unconnected_state);
            tvSwitch.setText(R.string.connect_to_device);
        } else {
            ivConnectionState.setImageResource(R.mipmap.ic_connected_state);
            tvSwitch.setText(R.string.switch_to_another_device);
        }
        showBatteryLay();
    }

    private void showBatteryLay() {
        batteryLay.setVisibility(View.GONE);
        if (adapter == null) {
            return;
        }
        int connectionStateHeadset = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int batteryLevel = BluetoothUtil.getBatteryLevel(mDevice);
        printLogD("showBatteryLay...batteryLevel==" + batteryLevel);
        if (connectionStateHeadset == BluetoothProfile.STATE_CONNECTED && batteryLevel >= 0 && batteryLevel <= 100) {
            batteryLay.setVisibility(View.VISIBLE);
            Bitmap bitmap = StarUtil.createBatteryLevelBitmap(batteryLevel);
            ivBatteryLevel.setImageBitmap(bitmap);
            String percentageBatteryLevel = String.format(getString(R.string.percentage_reserved_charge), StarUtil.formatPercentage(batteryLevel));
            tvPercentageBatteryLevel.setText(percentageBatteryLevel);
            if (batteryLevel <= 15) {
                ToastUtil.showToast(DetailActivity.this, R.string.low_battery_hint);
            }
        }
    }

    private BluetoothDevice getConnectedDevice() {
        if (adapter != null) {
            List<BluetoothDevice> deviceList = BluetoothUtil.getBondedDevices(adapter);
            for (BluetoothDevice device : deviceList) {
                if (BluetoothUtil.isConnected(device, a2dp, headset)) {
                    return device;
                }
            }
        }
        return null;
    }

    @Override
    protected void unInit() {
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtil.isEmpty(action)) {
                return;
            }
            BluetoothDevice device;
            boolean isA2dp = false;
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        getProfileProxy();
                    }
                    break;
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    isA2dp = true;
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (StarUtil.canShow(device)) {
                        dealConnectionStateChanged(device, isA2dp);
                    }
                    break;
                case ACTION_BATTERY_LEVEL_CHANGED:
                    showBatteryLay();
                    break;
                default:
                    break;

            }
        }
    };

    private void dealConnectionStateChanged(BluetoothDevice device, boolean isA2dp) {
        int connectionA2dp = BluetoothUtil.getConnectionState(device, a2dp);
        int connectionHeadset = BluetoothUtil.getConnectionState(device, headset);
        boolean isConnected = false;
        if (isA2dp) {
            if (connectionA2dp == BluetoothProfile.STATE_CONNECTED && connectionHeadset != BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
            }
        } else {
            if (connectionA2dp != BluetoothProfile.STATE_CONNECTED && connectionHeadset == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
            }
        }
        if (isConnected) {
            ToastUtil.showToast(DetailActivity.this, R.string.auto_connected_hint);
        }
        printLogE("onReceive...connectionA2dp==" + connectionA2dp);
        printLogE("onReceive...connectionHeadset==" + connectionHeadset);
        if (a2dp == null || headset == null) {
            getProfileProxy();
        } else {
            initData();
        }
    }

    public void onSwitchClick(View view) {
        BluetoothUtil.unPaired(mDevice);
        startActivity(new Intent(mContext, SearchActivity.class));
        finish();
    }

    public void onMoreClick(View view) {
        if (morePopupWindow == null) {
            View moreView = View.inflate(this, R.layout.popup_window_more_lay, null);
            moreView.findViewById(R.id.tv_version).setOnClickListener(onMPWClickListener);
            moreView.findViewById(R.id.tv_firmware_upgrade).setOnClickListener(onMPWClickListener);
            moreView.findViewById(R.id.tv_about).setOnClickListener(onMPWClickListener);
            morePopupWindow = new PopupWindow(this);
            morePopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            morePopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            morePopupWindow.setBackgroundDrawable(new ColorDrawable(0));
            morePopupWindow.setContentView(moreView);
            morePopupWindow.setOutsideTouchable(true);
            morePopupWindow.setTouchable(true);
        }
        if (isMorePWShowing) {
            isMorePWShowing = false;
            morePopupWindow.dismiss();
        } else {
            isMorePWShowing = true;
            morePopupWindow.showAsDropDown(ivMore, 0, -10);
        }
    }

    private View.OnClickListener onMPWClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            morePopupWindow.dismiss();
            Intent intent = null;
            switch (v.getId()) {
                case R.id.tv_version:
                    intent = new Intent(mContext, VersionActivity.class);
                    break;
                case R.id.tv_firmware_upgrade:
                    if (mDevice == null || TextUtil.isEmpty(mDevice.getName())){
                        ToastUtil.showToast(mContext, R.string.please_connect_device);
                        break;
                    }
                    intent = new Intent(mContext, FirmwareUpgradeActivity.class);
                    intent.putExtra(StarUtil.KEY_PRODUCT_NAME, mDevice.getName());
                    intent.putExtra(StarUtil.KEY_BLUETOOTH_DEVICE, mDevice);
                    break;
                case R.id.tv_about:
                    intent = new Intent(mContext, AboutActivity.class);
                    break;
                default:
                    break;
            }
            if (intent != null) {
                startActivity(intent);
            }
        }
    };
}
