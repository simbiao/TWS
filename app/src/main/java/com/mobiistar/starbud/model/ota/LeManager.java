package com.mobiistar.starbud.model.ota;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.UUID;

/**
 * Description: BLE operation manager.
 * Date：18-11-16-下午3:39
 * Author: black
 */
public class LeManager {

    private static volatile LeManager sLeManager;

    private LeConnector mConnectorX;

    private LeManager() {
        mConnectorX = new LeConnector();
    }

    public static LeManager getLeManager() {
        if (sLeManager == null) {
            synchronized (LeManager.class) {
                if (sLeManager == null) {
                    sLeManager = new LeManager();
                }
            }
        }
        return sLeManager;
    }

    public void addConnectCallback(LeConnectCallback connectCallback) {
        mConnectorX.addConnectCallback(connectCallback);
    }

    public void removeConnectCallback(LeConnectCallback connectCallback) {
        mConnectorX.removeConnectCallback(connectCallback);
    }

    public boolean connect(Context context, BluetoothDevice device) {
        return mConnectorX.connect(context, device);
    }

    public boolean connect(Context context, String address) {
        return mConnectorX.connect(context, address);
    }

    public boolean discoverServices() {
        return mConnectorX.discoverServices();
    }

    public boolean requestMtu(int mtu) {
        return mConnectorX.requestMtu(mtu);
    }

    public boolean enableCharacteristicNotify(UUID service, UUID rxCharacteristic, UUID descriptor) {
        return mConnectorX.enableCharacteristicNotify(service, rxCharacteristic, descriptor);
    }

    public boolean setWriteCharacteristic(UUID service, UUID characteristic) {
        return mConnectorX.setWriteCharacteristic(service, characteristic);
    }


    public void close() {
        mConnectorX.close();
    }
    public void closeUnNotify() {
        mConnectorX.closeUnNotify();
    }

    public boolean isConnected() {
        return mConnectorX.isConnected();
    }

    public boolean refresh() {
        return mConnectorX.refresh();
    }

    public boolean write(byte[] data) {
        return mConnectorX.write(data);
    }
}
