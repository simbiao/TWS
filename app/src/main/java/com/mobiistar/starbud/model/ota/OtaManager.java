package com.mobiistar.starbud.model.ota;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobiistar.starbud.util.LogUtil;
import com.mobiistar.starbud.util.OtaUtil;
import com.mobiistar.starbud.util.TextUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Description: Ota operation manager.
 * Date：18-11-19-上午9:37
 * Author: black
 */
public class OtaManager {
    private static final String OTA_DEVICE_NAME = "BES_OTA";
    private static final int MAX_CONNECT_COUNT = 10;
    private static final int DEFAULT_MTU = 512;
    private static final int SEND_OTA_DATA_FLAG = 10000;
    private static final int CONNECT_FLAG = 20000;
    private static final int RECONNECT_DURATION = 3000;
    private static final byte[] RECEIVE_OTA_RESPONSE_START = new byte[]{(byte) 0x81, 0x42, 0x45, 0x53, 0x54};
    private static final byte[] RECEIVE_OTA_RESPONSE_PASS = new byte[]{0x11, 0x22};
    private static final byte[] RECEIVE_OTA_RESPONSE_RESEND = new byte[]{0x33, 0x44};
    private static final byte RECEIVE_OTA_RESPONSE_CONFIG = (byte) 0x87;
    private static final byte RECEIVE_OTA_RESPONSE_SEGMENT = (byte) 0x83;
    private static final byte RECEIVE_OTA_RESPONSE_FINAL = (byte) 0x84;
    private static final byte RECEIVE_OTA_RESPONSE_SUCCESS = (byte) 0x01;
    private static final UUID OTA_SERVICE_OTA_UUID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID OTA_CHARACTERISTIC_OTA_UUID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID OTA_DESCRIPTOR_OTA_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final Object mOtaLock = new Object();
    private static OtaManager mOtaManager;
    private OtaCallback mOtaCallback;
    private BluetoothLeScanner mBleScanner;
    private BluetoothDevice mBluetoothDevice;
    private LeManager mLeManager;
    private String mOtaFileUrl;
    private int mConnectCount;
    private int mMTU = DEFAULT_MTU;
    private WeakReference<Context> mContextWR;
    private byte[][][] mOtaData;
    private int mSegmentIndex = 0;
    private int mMtuItemIndex = 0;
    private int mLatestProgress = -1;
    private boolean mHasExit = false;
    private boolean mIsSendingOtaData = false;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_OTA_DATA_FLAG:
                    sendOtaData();
                    break;
                case CONNECT_FLAG:
                    connect();
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    private OtaManager() {
    }

    public static OtaManager getInstance() {
        if (mOtaManager == null) {
            mOtaManager = new OtaManager();
        }
        return mOtaManager;
    }

    public void init(BluetoothLeScanner bleScanner, String otaFileUrl) {
        printLogE("init...");
        mBleScanner = bleScanner;
        mOtaFileUrl = otaFileUrl;
        mLeManager = LeManager.getLeManager();
        mLeManager.addConnectCallback(leConnectCallback);
        mHasExit = false;
        mIsSendingOtaData = false;
        mSegmentIndex = 0;
        mMtuItemIndex = 0;
        mLatestProgress = -1;
    }

    public void unInit() {
        printLogE("unInit...");
        stopScanDevice();
        disconnect();
        mHasExit = true;
    }

    public void setOtaCallback(OtaCallback otaCallback) {
        printLogE("setOtaCallback...otaCallback==" + otaCallback);
        mOtaCallback = otaCallback;
    }

    public void searchDevice() {
        printLogE("searchDevice...");
        startScanDevice();
    }

    public void connectDevice(Context context) {
        printLogE("connectDevice...context==" + context);
        mConnectCount = 0;
        mContextWR = new WeakReference<>(context);
        connect();
    }

    public void disconnectDevice() {
        mConnectCount = 0;
        disconnect();
    }

    public void sendData() {
        printLogE("sendData...mOtaFileUrl==" + mOtaFileUrl);
        mSegmentIndex = 0;
        mMtuItemIndex = 0;
        mLatestProgress = -1;
        sendOtaConfigData();
    }

    private void startScanDevice() {
        printLogE("startScanDevice...mBleScanner==" + mBleScanner);
        if (mBleScanner == null) {
            return;
        }
        ScanFilter scanFilter = new ScanFilter.Builder().setDeviceName(OTA_DEVICE_NAME).build();
        List<ScanFilter> scanFilterList = new ArrayList<>();
        scanFilterList.add(scanFilter);
        ScanSettings scanSettings = new ScanSettings.Builder().build();
        mBleScanner.startScan(scanFilterList, scanSettings, scanCallback);
    }

    private void stopScanDevice() {
        printLogE("stopScanDevice...mBleScanner==" + mBleScanner);
        if (mBleScanner == null) {
            return;
        }
        mBleScanner.stopScan(scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result != null ? result.getDevice() : null;
            printLogE("onScanResult...device==" + device);
            if (device != null && OTA_DEVICE_NAME.equals(device.getName())) {
                mBluetoothDevice = device;
                stopScanDevice();
                if (mOtaCallback != null) {
                    mOtaCallback.onSearchDevice(device);
                }
            }
        }
    };

    private void connect() {
        printLogE("connect...context==" + mContextWR.get());
        printLogE("connect...mLeManager==" + mLeManager);
        printLogE("connect...mBluetoothDevice==" + mBluetoothDevice);
        printLogE("connect...mConnectCount==" + mConnectCount);
        if (mContextWR.get() == null || mLeManager == null || mBluetoothDevice == null) {
            connectFailed();
            return;
        }

        mConnectCount++;
        if (mConnectCount <= MAX_CONNECT_COUNT) {
            mLeManager.connect(mContextWR.get(), mBluetoothDevice);
        }
    }

    private void reconnect() {
        printLogE("reconnect...context==" + mContextWR.get());
        mHandler.sendEmptyMessageDelayed(CONNECT_FLAG, RECONNECT_DURATION);
    }


    private void disconnect() {
        printLogE("disconnect...mLeManager==" + mLeManager);
        if (mLeManager == null) {
            return;
        }
        mLeManager.close();
    }

    private void connectFailed() {
        printLogE("connectFailed...mOtaCallback==" + mOtaCallback);
        if (mOtaCallback != null) {
            mOtaCallback.onConnectFailed();
        }
    }

    private void connectSuccess(int fwSWV, int fwHWV, int mtu) {
        printLogE("connectSuccess...mOtaCallback==" + mOtaCallback);
        if (mOtaCallback != null) {
            mOtaCallback.onConnectSuccess(fwSWV, fwHWV, mtu);
        }
    }

    private void sendOtaFileInfo() {
        printLogE("sendOtaFileInfo...0...mOtaFileUrl==" + mOtaFileUrl);
        if (mLeManager != null && !TextUtil.isEmpty(mOtaFileUrl)) {
            printLogE("sendOtaFileInfo...1...mOtaFileUrl==" + mOtaFileUrl);
            byte[] otaFileInfo = OtaUtil.getOtaFileInfo(mOtaFileUrl);
            if (otaFileInfo != null && otaFileInfo.length > 0) {
                printLogE("sendOtaFileInfo...2...mOtaFileUrl==" + mOtaFileUrl);
                boolean result = mLeManager.write(otaFileInfo);
                printLogE("sendOtaFileInfo...result==" + result);
                return;
            }
        }
        printLogE("sendOtaFileInfo...3...mOtaFileUrl==" + mOtaFileUrl);
        reconnect();
    }

    private void sendFailed() {
        printLogE("sendFailed...mOtaCallback==" + mOtaCallback);
        if (mOtaCallback != null) {
            mOtaCallback.onSendFailed();
        }
    }

    private void sendSuccess() {
        printLogE("sendSuccess...mOtaCallback==" + mOtaCallback);
        if (mOtaCallback != null) {
            mOtaCallback.onSendSuccess();
        }
    }

    private void unknownError() {
        printLogE("unknownError...mOtaCallback==" + mOtaCallback);
        if (mOtaCallback != null) {
            mOtaCallback.onError();
        }
    }

    private void sendOtaConfigData() {
        printLogE("sendOtaConfigData...mOtaFileUrl==" + mOtaFileUrl);
        if (mLeManager != null && !TextUtil.isEmpty(mOtaFileUrl)) {
            byte[] otaConfigData = OtaUtil.getOtaConfigData(mOtaFileUrl);
            if (otaConfigData != null && otaConfigData.length > 0) {
                boolean result = mLeManager.write(otaConfigData);
                printLogE("sendOtaFileInfo...result==" + result);
                return;
            }
        }
        sendFailed();
    }

    private void getOtaData() {
        printLogE("getOtaData...mOtaFileUrl==" + mOtaFileUrl);
        printLogE("getOtaData...mMtu==" + mMTU);
        mOtaData = OtaUtil.getOtaData(mOtaFileUrl, mMTU);
        printLogE("getOtaData...otaData.length==" + mOtaData.length);
    }

    private void sendOtaData() {
        synchronized (mOtaLock) {
            // has exit.
            if (mHasExit) {
                printLogE("sendOtaData...mHasExit==");
                return;
            }

            // deal null
            if (mLeManager == null || mOtaData == null || mOtaData[mSegmentIndex] == null) {
                printLogE("sendOtaData...mLeManager==" + mLeManager);
                printLogE("sendOtaData...mOtaData is null==" + (mOtaData == null));
                printLogE("sendOtaData...mOtaData[" + mSegmentIndex + "] is null==" + (mOtaData[mSegmentIndex] == null));
                if (mOtaCallback != null) {
                    mOtaCallback.onSendFailed();
                }
                return;
            }

            // send ota data
            int length = mOtaData[mSegmentIndex].length;
            if (mMtuItemIndex >= 0 && mMtuItemIndex < length) {
                mIsSendingOtaData = true;
                boolean result = mLeManager.write(mOtaData[mSegmentIndex][mMtuItemIndex]);
                // The real write result is in the method onWritten. If result is false, maybe the earphone has not power now.
                if (!result) {
                    //mBluetoothGatt == null.
                    printLogE("sendOtaData...mBluetoothGatt == null, unknownError");
                    unknownError();
                }
            }

            // sending progress
            int progress = mSegmentIndex * 100 / (mOtaData.length - 1);
            printLogE("sendOtaData...Index==[" + mOtaData.length + "," + mSegmentIndex + " | " + length + "," + mMtuItemIndex + "] | progress==" + progress);
            progress = progress < 0 ? 0 : progress;
            if (mOtaCallback != null && progress != mLatestProgress) {
                mLatestProgress = progress;
                mOtaCallback.onSendProgress(mLatestProgress);
            }

            // send ota data over,do not need reconnect again.
            if (mSegmentIndex >= mOtaData.length - 1 && mMtuItemIndex >= length) {
                mConnectCount = MAX_CONNECT_COUNT;
            }
        }
    }

    private enum RECEIVE_OTA_RESPONSE {
        START,
        CONFIG_SUCCESS,
        CONFIG_FAILURE,
        SEGMENT_PASS,
        SEGMENT_RESEND,
        SEGMENT_SEND_SUCCESS,
        SEGMENT_SEND_FAILURE,
        FINAL_SUCCESS,
        FINAL_FAILURE,
        UNKNOWN
    }

    private RECEIVE_OTA_RESPONSE getOtaResponseFlag(byte[] data) {
        RECEIVE_OTA_RESPONSE receiveOtaResponse = RECEIVE_OTA_RESPONSE.UNKNOWN;
        try {
            byte firstByte = data[0];
            byte secondByte = data[1];
            if (OtaUtil.startsWith(data, RECEIVE_OTA_RESPONSE_START)) {
                receiveOtaResponse = RECEIVE_OTA_RESPONSE.START;
            } else if (OtaUtil.isEqual(RECEIVE_OTA_RESPONSE_PASS, data)) {
                receiveOtaResponse = RECEIVE_OTA_RESPONSE.SEGMENT_PASS;
            } else if (OtaUtil.isEqual(RECEIVE_OTA_RESPONSE_RESEND, data)) {
                receiveOtaResponse = RECEIVE_OTA_RESPONSE.SEGMENT_RESEND;
            } else {
                switch (firstByte) {
                    case RECEIVE_OTA_RESPONSE_CONFIG:
                        receiveOtaResponse = secondByte == RECEIVE_OTA_RESPONSE_SUCCESS ?
                                RECEIVE_OTA_RESPONSE.CONFIG_SUCCESS : RECEIVE_OTA_RESPONSE.CONFIG_FAILURE;
                        break;
                    case RECEIVE_OTA_RESPONSE_SEGMENT:
                        if (data.length == 4 && data[2] == RECEIVE_OTA_RESPONSE_FINAL) {
                            receiveOtaResponse = data[3] == RECEIVE_OTA_RESPONSE_SUCCESS ?
                                    RECEIVE_OTA_RESPONSE.FINAL_SUCCESS : RECEIVE_OTA_RESPONSE.FINAL_FAILURE;
                        } else {
                            receiveOtaResponse = secondByte == RECEIVE_OTA_RESPONSE_SUCCESS ?
                                    RECEIVE_OTA_RESPONSE.SEGMENT_SEND_SUCCESS : RECEIVE_OTA_RESPONSE.SEGMENT_SEND_FAILURE;
                        }
                        break;
                    case RECEIVE_OTA_RESPONSE_FINAL:
                        receiveOtaResponse = secondByte == RECEIVE_OTA_RESPONSE_SUCCESS ?
                                RECEIVE_OTA_RESPONSE.FINAL_SUCCESS : RECEIVE_OTA_RESPONSE.FINAL_FAILURE;
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        printLogE("getOtaResponseFlag...receiveOtaResponse==" + receiveOtaResponse);
        return receiveOtaResponse;
    }

    private void dealReceiveData(byte[] data) {
        printLogE("dealReceiveData...data.length==" + (data != null ? data.length : 0));
        if (data == null) {
            return;
        }
        try {
            RECEIVE_OTA_RESPONSE receiveOtaResponse = getOtaResponseFlag(data);
            printLogE("dealReceiveData...receiveOtaResponse==" + receiveOtaResponse);
            switch (receiveOtaResponse) {
                case START:
                    int softwareVersion = ((data[5] & 0xFF) | ((data[6] & 0xFF) << 8));
                    int hardwareVersion = ((data[7] & 0xFF) | ((data[8] & 0xFF) << 8));
                    mMTU = (data[9] & 0xFF) | ((data[10] & 0xFF) << 8);
                    connectSuccess(softwareVersion, hardwareVersion, mMTU);
                    break;
                case CONFIG_SUCCESS:
                    getOtaData();
                    sendOtaData();
                    break;
                case CONFIG_FAILURE:
                    sendFailed();
                    break;
                case SEGMENT_PASS:
                case SEGMENT_SEND_SUCCESS:
                    mSegmentIndex++;
                case SEGMENT_RESEND:
                case SEGMENT_SEND_FAILURE:
                    mMtuItemIndex = 0;
                    sendOtaData();
                    break;
                case FINAL_SUCCESS:
                    sendSuccess();
                    break;
                case FINAL_FAILURE:
                    sendFailed();
                    break;
                case UNKNOWN:
                    unknownError();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LeConnectCallback leConnectCallback = new LeConnectCallback() {

        @Override
        public void onConnectionStateChanged(boolean connected) {
            printLogE("onConnectionStateChanged...connected==" + connected);
            if (mLeManager != null && connected) {
                mLeManager.discoverServices();
                return;
            }
            printLogE("onConnectionStateChanged...3...connected==" + connected);
            reconnect();
        }

        @Override
        public void onServicesDiscovered(int status) {
            printLogE("onServicesDiscovered...status==" + status);
            if (mLeManager != null && LeConnector.LE_SUCCESS == status) {
                boolean result = mLeManager.setWriteCharacteristic(OTA_SERVICE_OTA_UUID, OTA_CHARACTERISTIC_OTA_UUID);
                printLogE("onServicesDiscovered...setWriteCharacteristic...result==" + result);
                if (result) {
                    mLeManager.requestMtu(DEFAULT_MTU);
                    return;
                }
            }
            printLogE("onServicesDiscovered...3...status==" + status);
            reconnect();
        }

        @Override
        public void onMtuChanged(int status, int mtu) {
            printLogE("onMtuChanged...status==" + status);
            printLogE("onMtuChanged...mtu==" + mtu);
            if (mLeManager != null) {
                boolean result = mLeManager.enableCharacteristicNotify(OTA_SERVICE_OTA_UUID, OTA_CHARACTERISTIC_OTA_UUID, OTA_DESCRIPTOR_OTA_UUID);
                printLogE("onMtuChanged...enableCharacteristicNotification...result==" + result);
                if (result) {
                    return;
                }
            }
            printLogE("onMtuChanged...3...status==" + status);
            reconnect();
        }

        @Override
        public void onCharacteristicNotifyEnabled(int status) {
            printLogE("onCharacteristicNotifyEnabled...status==" + status);
            if (status == LeConnector.LE_SUCCESS) {
                sendOtaFileInfo();
                return;
            }
            printLogE("onCharacteristicNotifyEnabled...3...status==" + status);
            reconnect();

        }

        @Override
        public void onWritten(int status) {
            printLogE("onWritten...status==" + status);
            printLogE("onWritten...mIsSendingOtaData==" + mIsSendingOtaData);
            if (mIsSendingOtaData) {
                mIsSendingOtaData = false;
                boolean realWriteResult = LeConnector.LE_SUCCESS == status;
                printLogE("onWritten...Index==" + mSegmentIndex + " | " + mMtuItemIndex + " | realWriteResult==" + realWriteResult);
                if (LeConnector.LE_SUCCESS == status) {
                    mMtuItemIndex++;
                    mHandler.sendEmptyMessageDelayed(SEND_OTA_DATA_FLAG, 80);
                } else {
                    mHandler.sendEmptyMessageDelayed(SEND_OTA_DATA_FLAG, 80);
                }
            }
        }

        @Override
        public void onReceive(byte[] data) {
            printLogE("onReceive...data.length==" + (data != null ? data.length : 0));
            dealReceiveData(data);
        }
    };

    private void printLogE(String logContent) {
        LogUtil.printLog(Log.ERROR, getClass().getSimpleName(), logContent);
    }
}
