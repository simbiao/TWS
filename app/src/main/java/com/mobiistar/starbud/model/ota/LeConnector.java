package com.mobiistar.starbud.model.ota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.mobiistar.starbud.util.LogUtil;
import com.mobiistar.starbud.util.OtaUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description: BLE connect operator.
 * Date：18-11-16-下午3:39
 * Author: black
 */
public class LeConnector {

    public static final int LE_SUCCESS = 0;
    public static final int LE_ERROR = 1;

    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTED = 0;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristicTx;

    private List<LeConnectCallback> mConnectCallbacks;

    private final Object mStateLock = new Object();
    private final Object mCallbackLock = new Object();
    private int mConnState = STATE_DISCONNECTED;

    private UUID mDescriptor;

    public LeConnector() {
        mConnectCallbacks = new ArrayList<>();
    }

    public void addConnectCallback(LeConnectCallback connectCallback) {
        synchronized (mCallbackLock) {
            if (!mConnectCallbacks.contains(connectCallback)) {
                mConnectCallbacks.add(connectCallback);
            }
        }
    }

    public void removeConnectCallback(LeConnectCallback connectCallback) {
        synchronized (mCallbackLock) {
            mConnectCallbacks.remove(connectCallback);
        }
    }

    public boolean connect(Context context, String address) {
        return connect(context, BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
    }

    public boolean connect(Context context, BluetoothDevice device) {
        printLogE("connect " + device + "; " + mConnState);
        synchronized (mStateLock) {
            if (mConnState != STATE_DISCONNECTED) {
                return true;
            }
            mConnState = STATE_CONNECTING;
        }
        mBluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);
        return mBluetoothGatt != null;
    }

    public boolean discoverServices() {
        printLogE("discoverServices");
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.discoverServices();
        }
        return false;
    }


    public boolean requestMtu(int mtu) {
        printLogE("requestMtu");
        if (mBluetoothGatt != null) {
            return mBluetoothGatt.requestMtu(mtu);
        }
        return false;
    }

    public boolean enableCharacteristicNotify(UUID service, UUID rxCharacteristic, UUID descriptor) {
        printLogE("enableCharacteristicNotify()");
        if (mBluetoothGatt != null) {
            BluetoothGattService gattService = mBluetoothGatt.getService(service);
            if (gattService == null) {
                return false;
            }
            BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(rxCharacteristic);
            if (gattCharacteristic == null) {
                return false;
            }
            BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(descriptor);
            if (gattDescriptor == null) {
                return false;
            }
            if (!mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true)) {
                printLogE(" enableCharacteristicNotify  mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true) is false");
                return false;
            }
            if (!gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                printLogE(" enableCharacteristicNotify  gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) is false");
                return false;
            }
            mDescriptor = descriptor;
            return mBluetoothGatt.writeDescriptor(gattDescriptor);
        }
        return false;
    }

    public void close() {
        printLogE("close()");
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        notifyConnectionStateChanged(false);
        mBluetoothGatt = null;
    }

    public void closeUnNotify() {
        printLogE("closeUnNotify()");
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
    }

    public boolean isConnected() {
        return mConnState == STATE_CONNECTED;
    }

    public boolean refresh() {
        try {
            if (mBluetoothGatt != null) {
                Method refresh = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                return ((Boolean) refresh.invoke(mBluetoothGatt, new Object[0])).booleanValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setWriteCharacteristic(UUID service, UUID characteristic) {
        printLogE("setWriteCharacteristic service " + service.toString() + "; characteristic " + characteristic.toString());
        if (mBluetoothGatt == null) {
            return false;
        }
        BluetoothGattService gattService = mBluetoothGatt.getService(service);
        if (gattService == null) {
            return false;
        }
        mCharacteristicTx = gattService.getCharacteristic(characteristic);
        if (mCharacteristicTx == null) {
            return false;
        }
        mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        return true;
    }

    public boolean write(byte[] data) {
        printLogE("write...mBluetoothGatt" + mBluetoothGatt);
        if (mBluetoothGatt != null) {
            mCharacteristicTx.setValue(data);
            mCharacteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            mBluetoothGatt.writeCharacteristic(mCharacteristicTx);
            return true;
        }
        printLogE("write  (mBluetoothGatt == null)");
        return false;
    }

    private void notifyConnectionStateChanged(boolean connected) {
        synchronized (mStateLock) {
            if (connected && mConnState != STATE_CONNECTED) {
                for (LeConnectCallback callback : mConnectCallbacks) {
                    callback.onConnectionStateChanged(true);
                }
                mConnState = STATE_CONNECTED;
            } else if (!connected && mConnState != STATE_DISCONNECTED) {
                for (LeConnectCallback callback : mConnectCallbacks) {
                    callback.onConnectionStateChanged(false);
                }
                mConnState = STATE_DISCONNECTED;
            }
        }
    }

    private void notifyServicesDiscovered(int status) {
        synchronized (mCallbackLock) {
            for (LeConnectCallback callback : mConnectCallbacks) {
                callback.onServicesDiscovered(status);
            }
        }
    }

    private void notifyCharacteristicNotifyEnabled(int status) {
        synchronized (mCallbackLock) {
            for (LeConnectCallback callback : mConnectCallbacks) {
                callback.onCharacteristicNotifyEnabled(status);
            }
        }
    }

    private void notifyMtuChanged(int status, int mtu) {
        synchronized (mCallbackLock) {
            for (LeConnectCallback callback : mConnectCallbacks) {
                callback.onMtuChanged(status, mtu);
            }
        }
    }

    private void notifyWrite(int status) {
        synchronized (mCallbackLock) {
            for (LeConnectCallback callback : mConnectCallbacks) {
                callback.onWritten(status);
            }
        }
    }

    private void notifyReceive(byte[] data) {
        synchronized (mCallbackLock) {
            for (LeConnectCallback callback : mConnectCallbacks) {
                callback.onReceive(data);
            }
        }
    }

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            printLogE("onConnectionStateChange " + status + "; " + newState + "; " + gatt);
            mBluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                notifyConnectionStateChanged(true);
            } else {
                close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            printLogE("onServicesDiscovered " + gatt + "; " + status);
            mBluetoothGatt = gatt;
            if (status != BluetoothGatt.GATT_SUCCESS) {
                notifyServicesDiscovered(LE_ERROR);
                refresh();
                close();
            } else {
                notifyServicesDiscovered(LE_SUCCESS);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            printLogE("onCharacteristicWrite status is " + status + OtaUtil.toHex(characteristic.getValue())); //打印等效与延时。故不能打印太多在此处。
            printLogE("onCharacteristicWrite status is " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                notifyWrite(LE_SUCCESS);
            } else {
                notifyWrite(LE_ERROR);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic != null) {
                printLogE("onCharacteristicChanged " + OtaUtil.toHex(characteristic.getValue()));
            } else {
                printLogE("onCharacteristicChanged characteristic is null");
            }
            notifyReceive(characteristic.getValue());
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            printLogE("onDescriptorWrite status is " + status);
            if (descriptor.getUuid().equals(mDescriptor)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    notifyCharacteristicNotifyEnabled(LE_SUCCESS);
                } else {
                    notifyCharacteristicNotifyEnabled(LE_ERROR);
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            printLogE("onMtuChanged " + mtu + "; " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                notifyMtuChanged(LE_SUCCESS, mtu);
            } else {
                notifyMtuChanged(LE_ERROR, mtu);
            }
        }
    };

    protected void printLogE(String logContent) {
        LogUtil.printLog(Log.ERROR, getClass().getSimpleName(), logContent);
    }
}
