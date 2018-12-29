package com.mobiistar.starbud.model.ota;

import android.bluetooth.BluetoothDevice;

/**
 * Description: Ota operation callback.
 * Date：18-11-19-上午10:01
 * Author: black
 */
public interface OtaCallback {
    /**
     * On search the ble device.
     *
     * @param device device
     */
    void onSearchDevice(BluetoothDevice device);

    /**
     * On connect the device success.
     *
     * @param fwSWV firmware software version.
     * @param fwHWV firmware hardware version.
     * @param mtu   the MTU value.
     */
    void onConnectSuccess(int fwSWV, int fwHWV, int mtu);

    /**
     * On connect the device failed.
     */
    void onConnectFailed();

    /**
     * On sending ota data to the device.
     *
     * @param progress the progress of sending.
     */
    void onSendProgress(int progress);

    /**
     * On send ota data to the device successfully.
     */
    void onSendSuccess();

    /**
     * On send ota data to the device failed.
     */
    void onSendFailed();

    /**
     * For unknown case.
     */
    void onError();
}
