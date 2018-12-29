package com.mobiistar.starbud.view.activity.version;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.bean.AppInfo;
import com.mobiistar.starbud.bean.VersionInfo;
import com.mobiistar.starbud.model.StarModelCallback;
import com.mobiistar.starbud.model.StarModelHelp;
import com.mobiistar.starbud.util.FileUtil;
import com.mobiistar.starbud.util.JsonUtil;
import com.mobiistar.starbud.util.NetUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.ToastUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;
import com.mobiistar.starbud.view.activity.download.DownloadActivity;
import com.mobiistar.starbud.view.widget.CheckingPopupWindow;
import com.mobiistar.starbud.view.widget.UpdatePopupWindow;

import java.io.File;

/**
 * Description: Version activity.
 * Date：18-11-2-下午3:39
 * Author: black
 */
public class VersionActivity extends BaseActivity {

    private UpdatePopupWindow mUpdatePopupWindow;
    private CheckingPopupWindow mCheckingPopupWindow;
    private static final int APP_DOWNLOAD_REQUEST_CODE = 2000;
    private String mVersionSavePath;
    private String mApkSavePath;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_version;
    }

    @Override
    protected void init() {
        TextView tvVersion = findViewById(R.id.tv_version);
        PackageInfo packageInfo = StarUtil.getCurrentPackageInfo(this);
        String version = packageInfo != null ? packageInfo.versionName : "";
        tvVersion.setText(version);

        mVersionSavePath = StarUtil.getVersionDir() + File.separator + StarUtil.VERSION_INFO_NAME;
    }

    @Override
    protected void unInit() {

        if (mCheckingPopupWindow != null && mCheckingPopupWindow.isShowing()) {
            mCheckingPopupWindow.dismiss();
        }
        if (mUpdatePopupWindow != null && mUpdatePopupWindow.isShowing()) {
            mUpdatePopupWindow.dismiss();
        }
    }

    public void onCloseClick(View view) {
        finish();
    }

    public void onCheckVersionClick(View view) {
        checkVersionInfo();
    }

    private void checkVersionInfo() {
        if (!NetUtil.hasNetwork(mContext)) {
            ToastUtil.showToast(mContext, R.string.please_check_network);
        }
        if (!StarUtil.hasFilePermission(mContext)) {
            printLogE("checkVersionInfo...hasPermission==" + false);
            return;
        }
        showCheckingPopupWindow();
        StarModelHelp.downloadFile(StarUtil.VERSION_INFO_URL, mVersionSavePath, starModelCallback, 0);
        printLogE("checkVersionInfo...mVersionSavePath==" + mVersionSavePath);
    }

    private StarModelCallback starModelCallback = new StarModelCallback() {
        @Override
        public void onFailed(int code) {
            printLogE("onFailed...code==" + code);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissCheckingPopupWindow();
                    ToastUtil.showToast(mContext, R.string.please_check_network);
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
            printLogE("onSuccess...mVersionSavePath==" + mVersionSavePath);
            dismissCheckingPopupWindow();
            dealVersionInfo();
        }
    };

    private void dealVersionInfo() {
        String content = FileUtil.readFromFile(mVersionSavePath);
        printLogE("dealVersionInfo...content==" + content);
        try {
            VersionInfo versionInfo = JsonUtil.parseObject(content, VersionInfo.class);
            final AppInfo appInfo = versionInfo.getAppInfo();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int currentVersionCode = StarUtil.getCurrentPackageInfo(mContext).versionCode;
                    if (currentVersionCode >= appInfo.getVersionCode()) {
                        ToastUtil.showToast(mContext, R.string.this_is_the_latest_version);
                    } else {
                        showUpdatePopupWindow(appInfo);
                    }
                }
            });
        } catch (Exception e) {
            printLogE(e);
        }
    }

    private void showUpdatePopupWindow(final AppInfo appInfo) {
        if (appInfo == null) {
            return;
        }
        mApkSavePath = StarUtil.getVersionDir() + File.separator + appInfo.getApkUrl();
        printLogE("showUpdatePopupWindow...mApkSavePath==" + mApkSavePath);
        if (mUpdatePopupWindow == null) {
            mUpdatePopupWindow = new UpdatePopupWindow(this);
            mUpdatePopupWindow.setOnClickListener(new UpdatePopupWindow.OnClickListener() {
                @Override
                public void onCancelClick() {
                    printLogE("onCancelClick...mApkSavePath==" + mApkSavePath);
                    mUpdatePopupWindow.dismiss();
                }

                @Override
                public void onConfirmClick() {
                    printLogE("onConfirmClick...mApkSavePath==" + mApkSavePath);
                    mUpdatePopupWindow.dismiss();
                    dealConfirmUpdate(StarUtil.MOBIISTAR_URL + appInfo.getApkUrl(), appInfo.getApkLength());
                }
            });
        }
        mUpdatePopupWindow.setVersion(appInfo.getVersionName());
        mUpdatePopupWindow.setContent(appInfo.getDescription());
        mUpdatePopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void showCheckingPopupWindow() {
        printLogE("showUpdatePopupWindow...mCheckingPopupWindow==" + mCheckingPopupWindow);
        if (mCheckingPopupWindow == null) {
            mCheckingPopupWindow = new CheckingPopupWindow(this);
        }
        mCheckingPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void dismissCheckingPopupWindow() {
        printLogE("dismissCheckingPopupWindow...mCheckingPopupWindow==" + mCheckingPopupWindow);
        if (mCheckingPopupWindow != null && mCheckingPopupWindow.isShowing()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCheckingPopupWindow.dismiss();
                }
            });
        }
    }

    private void dealConfirmUpdate(String apkNetUrl, long fileLength) {
        boolean hasUnZipApk = StarUtil.hasUnzip(mApkSavePath);
        printLogE("dealConfirmUpdate...mApkSavePath==" + mApkSavePath);
        printLogE("dealConfirmUpdate...hasUnZipApk==" + hasUnZipApk);
        if (hasUnZipApk) {
            // Install
            FileUtil.installApkFile(mContext, mApkSavePath);
        } else {
            // Download and unzip.
            Intent intent = new Intent();
            intent.putExtra(StarUtil.KEY_NET_URL, apkNetUrl);
            intent.putExtra(StarUtil.KEY_SAVE_URL, mApkSavePath);
            intent.putExtra(StarUtil.KEY_FILE_LENGTH, fileLength);
            intent.setClass(mContext, VersionDownloadActivity.class);
            startActivityForResult(intent, APP_DOWNLOAD_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APP_DOWNLOAD_REQUEST_CODE && resultCode == RESULT_OK) {
            boolean hasUnZipApk = StarUtil.hasUnzip(mApkSavePath);
            printLogE("onActivityResult...mApkSavePath==" + mApkSavePath);
            printLogE("onActivityResult...hasUnZipApk==" + hasUnZipApk);
            if (hasUnZipApk) {
                // Install
                FileUtil.installApkFile(mContext, mApkSavePath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
