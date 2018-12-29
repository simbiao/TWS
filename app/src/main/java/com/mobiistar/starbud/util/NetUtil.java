package com.mobiistar.starbud.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Description: Net operation.
 * Date：18-11-14-下午2:48
 * Author: black
 */
public class NetUtil {

    public static boolean hasNetwork(Context context) {
        ConnectivityManager connectivityManager = context != null ? (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE) : null;
        NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }
}
