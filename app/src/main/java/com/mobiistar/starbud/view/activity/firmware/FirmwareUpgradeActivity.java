package com.mobiistar.starbud.view.activity.firmware;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.bean.ProductInfo;
import com.mobiistar.starbud.bean.VersionInfo;
import com.mobiistar.starbud.model.StarModelCallback;
import com.mobiistar.starbud.model.StarModelHelp;
import com.mobiistar.starbud.util.BluetoothUtil;
import com.mobiistar.starbud.util.FileUtil;
import com.mobiistar.starbud.util.JsonUtil;
import com.mobiistar.starbud.util.NetUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.TextUtil;
import com.mobiistar.starbud.util.ToastUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;

import java.io.File;
import java.util.List;

/**
 * Description:
 * Date：18-11-2-下午3:39
 * Author: black
 */
public class FirmwareUpgradeActivity extends BaseActivity {

    private String mProductName;
    private BluetoothDevice mDevice;
    private ProductInfo mProductInfo;
    private TextView tvVersion;
    private TextView tvReleaseDate;
    private TextView tvUpdateContent;
    private TextView tvNextStep;
    private View fwInfoLay;
    private View checkingVersionLay;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_firmware_upgrade;
    }

    @Override
    protected void init() {
        tvVersion = findViewById(R.id.tv_version);
        tvReleaseDate = findViewById(R.id.tv_release_date);
        tvUpdateContent = findViewById(R.id.tv_update_content);
        tvNextStep = findViewById(R.id.tv_next_step);
        fwInfoLay = findViewById(R.id.fw_info_lay);
        checkingVersionLay = findViewById(R.id.checking_version_lay);

        String versionSavePath = StarUtil.getVersionDir() + File.separator + StarUtil.VERSION_INFO_NAME;
        mProductName = getIntent().getStringExtra(StarUtil.KEY_PRODUCT_NAME);
        mDevice = getIntent().getParcelableExtra(StarUtil.KEY_BLUETOOTH_DEVICE);
        fwInfoLay.setVisibility(View.GONE);
        checkVersionInfo(versionSavePath);
    }

    @Override
    protected void unInit() {

    }

    public void onNextStepClick(View view) {
        if (mProductInfo == null) {
            return;
        }

        if (!StarUtil.checkPermission(mContext)) {
            return;
        }

        if (mProductInfo.getFwVersionCode() <= 1) {
            finish();
            return;
        }

        BluetoothUtil.unPaired(mDevice);

        String fwFileSvePath = StarUtil.getVersionDir() + File.separator + mProductInfo.getFwFileUrl();
        if (StarUtil.hasUnzip(fwFileSvePath)) {
            // OTA
            Intent intent = new Intent(mContext, BeforeOtaActivity.class);
            intent.putExtra(StarUtil.KEY_DIRECTION, StarUtil._LEFT);
            intent.putExtra(StarUtil.KEY_PRODUCT_NAME, mProductName);
            startActivity(intent);
        } else {
            // Download
            Intent intent = new Intent(mContext, FirmwareDownloadActivity.class);
            intent.putExtra(StarUtil.KEY_PRODUCT_NAME, mProductName);
            intent.putExtra(StarUtil.KEY_NET_URL, StarUtil.MOBIISTAR_URL + mProductInfo.getFwFileUrl());
            intent.putExtra(StarUtil.KEY_SAVE_URL, fwFileSvePath);
            intent.putExtra(StarUtil.KEY_FILE_LENGTH, mProductInfo.getFwFileLength());
            startActivity(intent);
        }
    }

    private void checkVersionInfo(String versionSavePath) {
        if (!NetUtil.hasNetwork(mContext)) {
            ToastUtil.showToast(mContext, R.string.please_check_network);
        }
        if (!StarUtil.hasFilePermission(mContext)) {
            printLogE("checkVersionInfo...hasPermission==" + false);
            return;
        }
        checkingVersionLay.setVisibility(View.VISIBLE);
        StarModelHelp.downloadFile(StarUtil.VERSION_INFO_URL, versionSavePath, starModelCallback, 0);
        printLogE("checkVersionInfo...mVersionSavePath==" + versionSavePath);
    }

    private StarModelCallback starModelCallback = new StarModelCallback() {
        @Override
        public void onFailed(int code) {
            printLogE("onFailed...code==" + code);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(mContext, R.string.please_check_network);
                    dismissCheckingPopupWindow();
                }
            });
        }

        @Override
        public void onProgress(int progress) {
            printLogE("onProgress...progress==" + progress);
        }

        @Override
        public void onSuccess(String result) {
            printLogE("onSuccess...result==" + result);
            dismissCheckingPopupWindow();
            dealVersionInfo(result);
        }
    };

    private void dismissCheckingPopupWindow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkingVersionLay.setVisibility(View.GONE);
            }
        });
    }

    private void dealVersionInfo(String versionSavePath) {
        String content = FileUtil.readFromFile(versionSavePath);
        printLogE("dealVersionInfo...content==" + content);
        try {
            VersionInfo versionInfo = JsonUtil.parseObject(content, VersionInfo.class);
            List<ProductInfo> productInfoList = versionInfo.getProductInfoList();
            printLogE("dealVersionInfo...productInfoList==" + productInfoList);
            if (productInfoList == null || productInfoList.isEmpty() || TextUtil.isEmpty(mProductName)) {
                return;
            }
            mProductInfo = null;
            for (ProductInfo productInfo : productInfoList) {
                printLogE("dealVersionInfo...productInfo==" + productInfo);
                if (productInfo != null && mProductName.equals(productInfo.getProductName())) {
                    mProductInfo = productInfo;
                    break;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProductInfo();
                }
            });
        } catch (Exception e) {
            printLogE(e);
        }
    }

    private void showProductInfo() {
        if (mProductInfo == null) {
            ToastUtil.showToast(mContext, R.string.ota_error_toast);
            return;
        }
        fwInfoLay.setVisibility(View.VISIBLE);
        String fwReleaseDate = StarUtil.formatTime(mProductInfo.getFwReleaseDate());
        String version = mProductInfo.getFwVersionName() + " | " + mProductInfo.getFwVersionCode();
        tvVersion.setText(version);
        tvReleaseDate.setText(fwReleaseDate);
        tvUpdateContent.setText(mProductInfo.getDescription());
        int nextStepREsId = mProductInfo.getFwVersionCode() <= 1 ? R.string.close : R.string.next_step;
        tvNextStep.setText(nextStepREsId);
        tvNextStep.setVisibility(View.VISIBLE);
        StarUtil.setFWVersionCode(mProductInfo.getFwVersionCode());
        String fwFileSvePath = StarUtil.getVersionDir() + File.separator + mProductInfo.getFwFileUrl();
        StarUtil.setFWSaveUrl(fwFileSvePath);
    }
}
