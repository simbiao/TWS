package com.mobiistar.starbud.model.ota;

/**
 * Description: BLE connect operation callback.
 * Date：18-11-16-下午3:39
 * Author: black
 */
public interface LeConnectCallback {

    void onConnectionStateChanged(boolean connected);

    void onServicesDiscovered(int status);

    void onMtuChanged(int status, int mtu);

    void onCharacteristicNotifyEnabled(int status);

    void onWritten(int status);

    void onReceive(byte[] data);
}
