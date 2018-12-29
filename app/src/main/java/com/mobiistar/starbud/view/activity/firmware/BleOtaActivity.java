package com.mobiistar.starbud.view.activity.firmware;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.model.ota.OtaCallback;
import com.mobiistar.starbud.model.ota.OtaManager;
import com.mobiistar.starbud.util.BluetoothUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.ToastUtil;

/**
 * Description: Ble ota activity.
 * Date：18-11-15-上午10:33
 * Author: black
 */
public class BleOtaActivity extends BaseOtaActivity {

    private View ivSearch;
    private TextView tvSearchResult;
    private View upgradeLay;
    private TextView tvUpgrade;
    private TextView tvUpgradeState;
    private ProgressBar pbUpgradeProgress;
    private TextView tvUpgradeStatistic;
    private OtaManager mOtaManager;
    private int mEarphoneFWSWVCode = -1;
    private boolean hasSearched;
    private boolean isSearching;
    protected long beginTime = 0;
    protected long endTime = 0;
    protected BluetoothDevice mDevice;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_ota_ble;
    }

    @Override
    protected void init() {
        super.init();

        ivSearch = findViewById(R.id.iv_search);
        tvSearchResult = findViewById(R.id.tv_search_result);
        upgradeLay = findViewById(R.id.upgrade_lay);
        tvUpgrade = findViewById(R.id.tv_upgrade);
        tvUpgradeState = findViewById(R.id.tv_upgrade_state);
        pbUpgradeProgress = findViewById(R.id.pb_upgrade_progress);
        tvUpgradeStatistic = findViewById(R.id.tv_upgrade_statistic);

        mOtaManager = OtaManager.getInstance();
        mOtaManager.init(adapter.getBluetoothLeScanner(), mOtaFileUrl);
        mOtaManager.setOtaCallback(otaCallback);
    }

    @Override
    protected void unInit() {
        printLogE("unInit...");
        if (mOtaManager != null) {
            mOtaManager.unInit();
        }
    }

    public void onSearchClick(View view) {
        printLogE("onSearchClick...view==" + view);
        hasSearched = false;
        isSearching = true;
        tvSearchResult.setVisibility(View.VISIBLE);
        tvSearchResult.setText(R.string.ota_searching);
        searchDevice();
    }

    public void onUpgradeClick(View view) {
        printLogE("onUpgradeClick...mEarphoneFWSWVCode==" + mEarphoneFWSWVCode);
        if (mEarphoneFWSWVCode < 0) {
            ToastUtil.showToast(mContext, R.string.ota_search_please);
            return;
        } else if (StarUtil.getFWVersionCode() <= mEarphoneFWSWVCode) {
            ToastUtil.showToast(mContext, R.string.this_is_the_latest_version);
            return;
        }
        isSearching = false;
        tvUpgrade.setEnabled(false);
        disconnectDevice();
    }

    public void onNextStepClick(View view) {
        BluetoothUtil.unPaired(mDevice);
        super.onNextStepClick(view);
    }

    private void searchDevice() {
        printLogE("searchDevice...");
        if (mOtaManager != null) {
            mOtaManager.searchDevice();
        }
    }

    private void connectDevice() {
        printLogE("connectDevice...");
        if (mOtaManager != null) {
            mOtaManager.connectDevice(mContext);
        }
    }

    private void disconnectDevice() {
        printLogE("disconnectDevice...");
        tvUpgradeState.setVisibility(View.VISIBLE);
        tvUpgradeState.setText(R.string.ota_upgrade_connecting);
        if (mOtaManager != null) {
            mOtaManager.disconnectDevice();
        }
    }

    private void sendData() {
        printLogE("sendData...");
        beginTime = System.currentTimeMillis();
        dealUpgradeProgress(0);
        if (mOtaManager != null) {
            mOtaManager.sendData();
        }
    }

    private void dealUpgradeProgress(int progress) {
        printLogE("dealUpgradeProgress...progress==" + progress);
        String upgradeState = String.format(getString(R.string.ota_upgrade_upgrading), StarUtil.formatPercentage(progress));
        tvUpgradeState.setText(upgradeState);
        pbUpgradeProgress.setProgress(progress);
    }

    private void showView(final View view, final int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visibility);
            }
        });
    }

    private void searchFailure() {
        printLogE("searchFailure...");
        tvSearchResult.setText(R.string.ota_search_failure);
        showView(ivSearch, View.VISIBLE);
    }

    private void upgradeFailure() {
        printLogE("upgradeFailure...");
        tvUpgrade.setEnabled(true);
        tvUpgradeState.setText(R.string.ota_upgrade_failure);
        showView(tvUpgradeStatistic, View.GONE);
    }

    private OtaCallback otaCallback = new OtaCallback() {
        @Override
        public void onSearchDevice(BluetoothDevice device) {
            printLogE("OtaCallback...onSearchDevice...device==" + device);
            printLogE("OtaCallback...onSearchDevice...hasSearched==" + hasSearched);
            if (!hasSearched) {
                hasSearched = true;
                mDevice = device;
                connectDevice();
            }
        }

        @Override
        public void onConnectSuccess(int fwSWV, int fwHWV, int mtu) {
            printLogE("OtaCallback...onConnectSuccess...fwSWV==" + fwSWV);
            printLogE("OtaCallback...onConnectSuccess...fwHWV==" + fwHWV);
            printLogE("OtaCallback...onConnectSuccess...mtu==" + mtu);
            printLogE("OtaCallback...onConnectSuccess...isSearching==" + isSearching);
            if (isSearching) {
                mEarphoneFWSWVCode = fwSWV;
                String earphoneFWVersion = String.format(getString(R.string.ota_earphone_fw_version), mEarphoneFWSWVCode);
                tvSearchResult.setText(earphoneFWVersion);
                showView(ivSearch, View.GONE);
                showView(upgradeLay, View.VISIBLE);
            } else {
                sendData();
            }
        }

        @Override
        public void onConnectFailed() {
            printLogE("OtaCallback...onConnectFailed...");
            if (isSearching) {
                searchFailure();
            } else {
                upgradeFailure();
            }
        }

        @Override
        public void onSendProgress(int progress) {
            printLogE("OtaCallback...onSendProgress...progress==" + progress);
            dealUpgradeProgress(progress);
        }

        @Override
        public void onSendSuccess() {
            printLogE("OtaCallback...onSendSuccess...");
            endTime = System.currentTimeMillis();
            tvUpgradeState.setText(R.string.ota_upgrade_success);
            long takeMinutes = (System.currentTimeMillis() - beginTime) / 1000 / 60;
            printLogE("OtaCallback...onSendSuccess...takeMinutes==" + takeMinutes);
            String upgradeStatistic = String.format(getString(R.string.ota_upgrade_statistic), takeMinutes);
            showView(tvUpgradeStatistic, View.VISIBLE);
            tvUpgradeStatistic.setText(upgradeStatistic);
            tvUpgrade.setEnabled(true);
        }

        @Override
        public void onSendFailed() {
            printLogE("OtaCallback...onSendFailed...");
            upgradeFailure();
        }

        @Override
        public void onError() {
            printLogE("OtaCallback...onError...isSearching==" + isSearching);
            if (isSearching) {
                ToastUtil.showToast(mContext, R.string.ota_error_toast);
                searchFailure();
            } else {
                ToastUtil.showToast(mContext, R.string.ota_charge_retry);
                upgradeFailure();
            }
        }
    };
}
