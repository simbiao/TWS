package com.mobiistar.starbud.view.activity.download;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.mobiistar.starbud.R;
import com.mobiistar.starbud.model.StarModelCallback;
import com.mobiistar.starbud.model.StarModelHelp;
import com.mobiistar.starbud.util.FileUtil;
import com.mobiistar.starbud.util.StarUtil;
import com.mobiistar.starbud.util.ToastUtil;
import com.mobiistar.starbud.view.activity.base.BaseActivity;

/**
 * Description: Download activity.
 * Date：18-11-14-上午10:04
 * Author: black
 */
public class DownloadActivity extends BaseActivity {

    private TextView tvProgress;

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_download;
    }

    @Override
    protected void init() {
        tvProgress = findViewById(R.id.tv_progress);
        Intent intent = getIntent();
        String netUrl = intent.getStringExtra(StarUtil.KEY_NET_URL);
        String saveUrl = intent.getStringExtra(StarUtil.KEY_SAVE_URL);
        long fileLength = intent.getLongExtra(StarUtil.KEY_FILE_LENGTH, 0);
        if (StarUtil.hasDownload(saveUrl)) {
            unZipInThread(saveUrl);
        } else {
            FileUtil.deleteFile(saveUrl);
            StarModelHelp.downloadFile(netUrl, saveUrl, starModelCallback, fileLength);
        }
    }

    @Override
    protected void unInit() {

    }

    public void onCloseClick(View view) {
        finish();
    }

    private StarModelCallback starModelCallback = new StarModelCallback() {
        @Override
        public void onFailed(int code) {
            printLogE("onFailed...code==" + code);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(mContext, R.string.download_failed);
                }
            });
        }

        @Override
        public void onProgress(int progress) {
            printLogE("onProgress...progress==" + progress);
            showProgress(progress);
        }

        @Override
        public void onSuccess(String result) {
            printLogE("onSuccess...result==" + result);
            StarUtil.setHasDownload(result, true);
            unZip(result);
        }
    };

    private void unZipInThread(final String zipUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                unZip(zipUrl);
            }
        }).start();
    }

    private void unZip(String zipUrl) {
        boolean unzipResult = StarUtil.hasUnzip(zipUrl) || FileUtil.unzipFile(zipUrl);
        StarUtil.setHasUnzip(zipUrl, unzipResult);
        finishDownload(zipUrl);
    }

    private void showProgress(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvProgress.setText(StarUtil.formatPercentage(progress));
            }
        });
    }

    protected void finishDownload(String zipUrl) {
        setResult(RESULT_OK);
        finish();
    }
}
