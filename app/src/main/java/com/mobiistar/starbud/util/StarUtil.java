package com.mobiistar.starbud.util;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.mobiistar.starbud.R;
import com.mobiistar.starbud.app.StarApp;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Description: StarUtil for the app.
 * Date：18-11-2-下午4:14
 * Author: black
 */
public class StarUtil {
    public static final String MOBIISTAR_URL = "http://www.vsunmobile.com/TWS/Mobiistar/";
    public static final String VERSION_INFO_NAME = "versionInfo.txt";
    public static final String VERSION_INFO_URL = MOBIISTAR_URL + VERSION_INFO_NAME;
    private static final String LOG_TAG = StarUtil.class.getSimpleName();
    private static final String ROOT_DIR = "Starbud";
    private static final String CRASH_DIR = "Crash";
    private static final String VERSION_DIR = "Version";
    private static final String PREFERENCES_NAME = "Star";
    private static final String KEY_HAS_ENTER_DETAIL_ACTIVITY = "HasEnterDetailActivity";
    private static final String KEY_HAS_DOWNLOAD = "key_has_download";
    private static final String KEY_HAS_UNZIP = "key_has_unzip";
    public static final String KEY_BLUETOOTH_DEVICE = "key_bluetooth_device";
    public static final String KEY_SELECTED_INDEX = "key_selected_index";
    public static final String KEY_IMAGE_RES_ID_ARRAY = "key_image_res_id_array";
    public static final String KEY_NET_URL = "key_net_url";
    public static final String KEY_SAVE_URL = "key_save_url";
    public static final String KEY_FILE_LENGTH = "key_file_length";
    public static final String KEY_PRODUCT_NAME = "key_product_name";
    public static final String KEY_DIRECTION = "key_direction";
    public static final String KEY_FW_VERSION_CODE = "key_fw_version_code";
    public static final String KEY_FW_SAVE_URL = "key_fw_save_url";
    private static final String KEY_PRODUCT_WHITE_LIST = "key_product_white_list";
    public static final String _RIGHT = "_R";
    public static final String _LEFT = "_L";
    private static final String NAME_T1S = "T1S";
    private static final String NAME_LENO_AIR = "Lenovo Air";
    public static final String EXTENSION_NAME_LOG = ".log";
    public static final String EXTENSION_NAME_BIN = ".bin";
    public static final int CHECK_PERMISSION_REQUEST_CODE = 10000;
    public static final int IMAGE_BROWSE_REQUEST_CODE = 10000;
    public static final long DOUBLE_CLICK_DURATION = 300;
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String[] PERMISSION_ALL = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static String getRootDirPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + ROOT_DIR;
    }

    public static String getCrashDir() {
        return getRootDirPath() + File.separator + CRASH_DIR;
    }

    public static String getVersionDir() {
        return getRootDirPath() + File.separator + VERSION_DIR;
    }


    /**
     * Get SharedPreferences
     *
     * @return SharedPreferences
     */
    private static SharedPreferences getSharedPreferences() {
        return StarApp.getInstance().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Has enter detail activity.
     *
     * @param hasEnter hasEnter
     */
    public static void setHasEnterDetail(boolean hasEnter) {
        getSharedPreferences().edit().putBoolean(KEY_HAS_ENTER_DETAIL_ACTIVITY, hasEnter).apply();
    }

    /**
     * Has enter detail activity.
     *
     * @return boolean
     */
    public static boolean hasEnterDetail() {
        return getSharedPreferences().getBoolean(KEY_HAS_ENTER_DETAIL_ACTIVITY, false);
    }

    /**
     * Has download the file.
     *
     * @param filePath    filePath     *
     * @param hasDownload hasDownload
     */
    public static void setHasDownload(String filePath, boolean hasDownload) {
        getSharedPreferences().edit().putBoolean(KEY_HAS_DOWNLOAD + filePath, hasDownload).apply();
    }

    /**
     * Has download the file.
     *
     * @return boolean
     */
    public static boolean hasDownload(String filePath) {
        return getSharedPreferences().getBoolean(KEY_HAS_DOWNLOAD + filePath, false);
    }

    /**
     * Has unzip the file.
     *
     * @param filePath filePath     *
     * @param hasUnzip hasUnzip
     */
    public static void setHasUnzip(String filePath, boolean hasUnzip) {
        getSharedPreferences().edit().putBoolean(KEY_HAS_UNZIP + filePath, hasUnzip).apply();
    }

    /**
     * Has unzip the file.
     *
     * @return boolean
     */
    public static boolean hasUnzip(String filePath) {
        return getSharedPreferences().getBoolean(KEY_HAS_UNZIP + filePath, false);
    }

    /**
     * Set firmware version code.
     *
     * @param fwVersionCode firmware version code
     */
    public static void setFWVersionCode(int fwVersionCode) {
        getSharedPreferences().edit().putInt(KEY_FW_VERSION_CODE, fwVersionCode).apply();
    }

    /**
     * Get firmware version code.
     *
     * @return int
     */
    public static int getFWVersionCode() {
        return getSharedPreferences().getInt(KEY_FW_VERSION_CODE, 0);
    }

    /**
     * Set irmware save url.
     *
     * @param fwSaveUrl firmware save url
     */
    public static void setFWSaveUrl(String fwSaveUrl) {
        getSharedPreferences().edit().putString(KEY_FW_SAVE_URL, fwSaveUrl).apply();
    }

    /**
     * Get product white list.
     *
     * @return List<String>
     */
    public synchronized static List<String> getWhiteList() {
        return JSON.parseArray(getSharedPreferences().getString(KEY_PRODUCT_WHITE_LIST, "[]"), String.class);
    }

    /**
     * Set product white list.
     *
     * @param WhiteList product white list.
     */
    public synchronized static void setWhiteList(List<String> WhiteList) {
        getSharedPreferences().edit().putString(KEY_PRODUCT_WHITE_LIST, JSON.toJSONString(WhiteList)).apply();
    }

    /**
     * Get irmware save url.
     *
     * @return String
     */
    public static String getFWSaveUrl() {
        return getSharedPreferences().getString(KEY_FW_SAVE_URL, null);
    }

    /**
     * Formats an integer from 0..100 as a percentage.
     */
    public static String formatPercentage(int percentage) {
        return formatPercentage(((double) percentage) / 100.0);
    }

    /**
     * Formats a double from 0.0..1.0 as a percentage.
     */
    public static String formatPercentage(double percentage) {
        return NumberFormat.getPercentInstance().format(percentage);
    }

    /**
     * Create battery level internal bitmap.
     *
     * @param batteryLevel battery level
     * @return Bitmap
     */
    public static Bitmap createBatteryLevelBitmap(int batteryLevel) {
        int width = StarApp.getInstance().getResources().getDimensionPixelSize(R.dimen._129dp);
        int height = StarApp.getInstance().getResources().getDimensionPixelSize(R.dimen._50dp);
        int radius = StarApp.getInstance().getResources().getDimensionPixelSize(R.dimen._4dp);
        int normalColor = StarApp.getInstance().getResources().getColor(R.color.colorGreen, StarApp.getInstance().getTheme());
        int lowerColor = StarApp.getInstance().getResources().getColor(R.color.colorRed, StarApp.getInstance().getTheme());
        int batteryLevelColor = batteryLevel <= 15 ? lowerColor : normalColor;
        int batteryLevelWidth = width * batteryLevel / 100;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(batteryLevelColor);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setStyle(Paint.Style.FILL);

        Canvas canvas = new Canvas(bitmap);
        Rect clipRect = new Rect(0, 0, batteryLevelWidth, height);
        canvas.clipRect(clipRect);
        RectF roundRect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(roundRect, radius, radius, paint);
        canvas.save();
        return bitmap;
    }

    /**
     * Judge has file permission or not.
     *
     * @param context context
     * @return boolean
     */
    public static boolean hasFilePermission(Context context) {
        return hasPermission(context, PERMISSION_WRITE_EXTERNAL_STORAGE);
    }

    /**
     * Judge has permission or not.
     *
     * @param context    context
     * @param permission the permission
     * @return boolean
     */
    public static boolean hasPermission(Context context, String permission) {
        boolean hasPermission = false;
        if (context != null && !TextUtil.isEmpty(permission)) {
            hasPermission = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            if (!hasPermission && context instanceof Activity) {
                ((Activity) context).requestPermissions(new String[]{permission}, CHECK_PERMISSION_REQUEST_CODE);
            }
        }
        return hasPermission;
    }

    /**
     * Check the app's permission.
     *
     * @param context context
     * @return boolean
     */
    public static boolean checkPermission(Context context) {
        List<String> permissionList = new ArrayList<>();
        boolean hasPermission;
        for (String permission : PERMISSION_ALL) {
            hasPermission = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            if (!hasPermission) {
                permissionList.add(permission);
            }
        }
        if (!permissionList.isEmpty() && context instanceof Activity) {
            String[] permissionArray = new String[permissionList.size()];
            permissionList.toArray(permissionArray);
            ((Activity) context).requestPermissions(permissionArray, CHECK_PERMISSION_REQUEST_CODE);
        }
        return permissionList.isEmpty();
    }

    /**
     * Get the quick guide img resIdArray.
     *
     * @return int[]
     */
    public static int[] getQuickGuideResIdArray() {
        return new int[]{
                R.mipmap.quick_guide_01,
                R.mipmap.quick_guide_02,
                R.mipmap.quick_guide_03,
                R.mipmap.quick_guide_04,
                R.mipmap.quick_guide_05,
                R.mipmap.quick_guide_06,
                R.mipmap.quick_guide_07,
                R.mipmap.quick_guide_08,
                R.mipmap.quick_guide_09,
                R.mipmap.quick_guide_10
        };
    }

    /**
     * Get the search ready connect img resIdArray.
     *
     * @return int[]
     */
    public static int[] getSearchReadyConnectResIdArray() {
        return new int[]{
                R.mipmap.search_ready_connect_01,
                R.mipmap.search_ready_connect_02,
                R.mipmap.search_ready_connect_03,
                R.mipmap.search_ready_connect_04,
                R.mipmap.search_ready_connect_05,
                R.mipmap.search_ready_connect_06,
                R.mipmap.search_ready_connect_07,
                R.mipmap.search_ready_connect_08,
                R.mipmap.search_ready_connect_09,
                R.mipmap.search_ready_connect_10,
                R.mipmap.search_ready_connect_11,
                R.mipmap.search_ready_connect_12,
                R.mipmap.search_ready_connect_13,
                R.mipmap.search_ready_connect_14,
                R.mipmap.search_ready_connect_15,
                R.mipmap.search_ready_connect_16
        };
    }

    public static boolean canShow(String deviceName) {
        return NAME_T1S.equals(deviceName) || NAME_LENO_AIR.equals(deviceName)
                || (getWhiteList() != null && getWhiteList().contains(deviceName));
    }

    public static boolean canShow(BluetoothDevice device) {
        String deviceName = null;
        if (device != null) {
            deviceName = device.getName();
        }
        return canShow(deviceName);
    }

    public static PackageInfo getCurrentPackageInfo(Context context) {
        if (context == null) {
            return null;
        } else {
            return getPackageInfo(context, context.getPackageName());
        }
    }

    public static PackageInfo getPackageInfo(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void selfRotation(Context context, ImageView imageView) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.self_rotation);
        LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
        animation.setInterpolator(lin);
        imageView.startAnimation(animation);
    }

    public static void startAnimation(ImageView imageView, int drawableResId) {
        imageView.setImageResource(drawableResId);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
        animationDrawable.start();
    }

    public static boolean isDoubleClick(View view) {
        try {
            Object tag = view.getTag(view.getId());
            long currentTime = SystemClock.uptimeMillis();
            long latestClickTime = tag instanceof Long ? (long) tag : currentTime;
            view.setTag(view.getId(), currentTime);
            if (currentTime - latestClickTime > 0 && currentTime - latestClickTime < DOUBLE_CLICK_DURATION) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String formatTime(long time) {
        TimeZone timeZone = TimeZone.getDefault();
        String zone = timeZone.getDisplayName(false, TimeZone.SHORT, Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date(time);
        String data = simpleDateFormat.format(date);
        return data + "  " + zone;
    }

    public static String formatTimeEx(long time) {
        TimeZone timeZone = TimeZone.getDefault();
        String zone = timeZone.getDisplayName(false, TimeZone.SHORT, Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss", Locale.getDefault());
        Date date = new Date(time);
        String data = simpleDateFormat.format(date);
        return data + "  " + zone;
    }
}
