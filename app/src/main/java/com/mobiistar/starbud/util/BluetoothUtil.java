package com.mobiistar.starbud.util;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.os.Environment;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Description: Bluetooth operation.
 * Date：18-11-2-下午4:14
 * Author: black
 */
public class BluetoothUtil {

    private static final String LOG_TAG = BluetoothUtil.class.getSimpleName();

    private static void printLog(String content) {
        LogUtil.printLog(Log.ERROR, LOG_TAG, content);
    }

    private static void printLogE(Exception e) {
        LogUtil.printLog(Log.ERROR, LOG_TAG, e);
    }

    private static Object invoke(Object cls, String methodName, Object[] param) {
        try {
            if (param == null) {
                param = new Object[0];
            }
            if (param.length == 0) {
                Method method = cls.getClass().getMethod(methodName);
                method.setAccessible(true);
                return method.invoke(cls);
            } else if (param.length == 1) {
                Method method = cls.getClass().getMethod(methodName, param[0].getClass());
                method.setAccessible(true);
                return method.invoke(cls, param[0]);
            } else if (param.length == 2) {
                Method method = cls.getClass().getMethod(methodName, param[0].getClass(), param[1].getClass());
                method.setAccessible(true);
                return method.invoke(cls, param[0], param[1]);
            } else if (param.length == 3) {
                Method method = cls.getClass().getMethod(methodName, param[0].getClass(), param[1].getClass(), param[2].getClass());
                method.setAccessible(true);
                return method.invoke(cls, param[0], param[1], param[2]);
            }
        } catch (Exception e) {
            printLogE(e);
        }
        return null;
    }

    public static String getName(BluetoothDevice device) {
        String resultName = device.getName();
        if (TextUtil.isEmpty(resultName)) {
            resultName = device.getAddress();
        }
        if (TextUtil.isEmpty(resultName)) {
            resultName = "";
        }
        printLog("getName...resultName==" + resultName);
        return resultName;
    }

    public static String getAliasName(BluetoothDevice device) {
        String resultAliasName = "";
        String methodName = "getAliasName";
        Object result = invoke(device, methodName, null);
        if (result instanceof String) {
            resultAliasName = (String) result;
        }
        if (TextUtil.isEmpty(resultAliasName)) {
            resultAliasName = getName(device);
        }
        printLog("getAliasName...resultAliasName==" + resultAliasName);
        return resultAliasName;
    }

    public static int getConnectionState(BluetoothDevice device, BluetoothA2dp a2dp) {
        int connectionState = BluetoothProfile.STATE_DISCONNECTED;
        if (device == null || a2dp == null) {
            return connectionState;
        }
        String methodName = "getConnectionState";
        Object result = invoke(a2dp, methodName, new Object[]{device});
        if (result instanceof Integer) {
            connectionState = (int) result;
        }
        printLog("getConnectionState...connectionState==" + connectionState);
        return connectionState;
    }

    public static int getConnectionState(BluetoothDevice device, BluetoothHeadset headset) {
        int connectionState = BluetoothProfile.STATE_DISCONNECTED;
        if (device == null || headset == null) {
            return connectionState;
        }
        String methodName = "getConnectionState";
        Object result = invoke(headset, methodName, new Object[]{device});
        if (result instanceof Integer) {
            connectionState = (int) result;
        }
        printLog("getConnectionState...connectionState==" + connectionState);
        return connectionState;
    }

    public static boolean isConnected(BluetoothDevice device, BluetoothA2dp a2dp, BluetoothHeadset headset) {
        int connectionStateA2dp = getConnectionState(device, a2dp);
        int connectionStateHeadset = getConnectionState(device, headset);
        printLog("isConnected...connectionStateA2dp==" + connectionStateA2dp);
        printLog("isConnected...connectionStateHeadset==" + connectionStateHeadset);
        return connectionStateA2dp == BluetoothProfile.STATE_CONNECTED || connectionStateHeadset == BluetoothProfile.STATE_CONNECTED;
    }

    public static int getConnectionStateEx(BluetoothDevice device, BluetoothA2dp a2dp, BluetoothHeadset headset) {
        int connectionStateA2dp = getConnectionState(device, a2dp);
        int connectionStateHeadset = getConnectionState(device, headset);
        printLog("getConnectionStateEx...connectionStateA2dp==" + connectionStateA2dp);
        printLog("getConnectionStateEx...connectionStateHeadset==" + connectionStateHeadset);
        int connectionState;
        if (connectionStateA2dp == BluetoothProfile.STATE_CONNECTING || connectionStateHeadset == BluetoothProfile.STATE_CONNECTING) {
            connectionState = BluetoothProfile.STATE_CONNECTING;
        } else if (connectionStateA2dp == BluetoothProfile.STATE_DISCONNECTING || connectionStateHeadset == BluetoothProfile.STATE_DISCONNECTING) {
            connectionState = BluetoothProfile.STATE_DISCONNECTING;
        } else if (connectionStateA2dp == BluetoothProfile.STATE_CONNECTED || connectionStateHeadset == BluetoothProfile.STATE_CONNECTED) {
            connectionState = BluetoothProfile.STATE_CONNECTED;
        } else {
            connectionState = BluetoothProfile.STATE_DISCONNECTED;
        }
        return connectionState;
    }

    public static boolean connectDevice(BluetoothDevice device, BluetoothA2dp a2dp) {
        if (device == null || a2dp == null) {
            return false;
        }
        boolean connectResult = false;
        String methodName = "connect";
        Object result = invoke(a2dp, methodName, new Object[]{device});
        if (result instanceof Boolean) {
            connectResult = (boolean) result;
        }
        printLog("connectDevice...connectResult==" + connectResult);
        return connectResult;
    }

    public static boolean connectDevice(BluetoothDevice device, BluetoothHeadset headset) {
        if (device == null || headset == null) {
            return false;
        }
        boolean connectResult = false;
        String methodName = "connect";
        Object result = invoke(headset, methodName, new Object[]{device});
        if (result instanceof Boolean) {
            connectResult = (boolean) result;
        }
        printLog("connectDevice...connectResult==" + connectResult);
        return connectResult;
    }

    public static boolean connectDevice(BluetoothDevice device, BluetoothProfile profile) {
        boolean connectResult = false;
        if (profile instanceof BluetoothA2dp) {
            connectResult = connectDevice(device, (BluetoothA2dp) profile);
        } else if (profile instanceof BluetoothHeadset) {
            connectResult = connectDevice(device, (BluetoothHeadset) profile);
        }
        return connectResult;
    }

    public static boolean disconnectDevice(BluetoothDevice device, BluetoothProfile profile) {
        boolean disconnectResult = false;
        if (profile instanceof BluetoothA2dp) {
            disconnectResult = disconnectDevice(device, (BluetoothA2dp) profile);
        } else if (profile instanceof BluetoothHeadset) {
            disconnectResult = disconnectDevice(device, (BluetoothHeadset) profile);
        }
        return disconnectResult;
    }

    public static boolean disconnectDevice(BluetoothDevice device, BluetoothA2dp a2dp) {
        if (device == null || a2dp == null) {
            return false;
        }
        boolean disconnectResult = false;
        String methodName = "disconnect";
        Object result = invoke(a2dp, methodName, new Object[]{device});
        if (result instanceof Boolean) {
            disconnectResult = (boolean) result;
        }
        printLog("disconnectDevice...disconnectResult==" + disconnectResult);
        return disconnectResult;
    }

    public static boolean disconnectDevice(BluetoothDevice device, BluetoothHeadset headset) {
        if (device == null || headset == null) {
            return false;
        }
        boolean disconnectResult = false;
        String methodName = "disconnect";
        Object result = invoke(headset, methodName, new Object[]{device});
        if (result instanceof Boolean) {
            disconnectResult = (boolean) result;
        }
        printLog("disconnectDevice...disconnectResult==" + disconnectResult);
        return disconnectResult;
    }

    public static int getBatteryLevel(BluetoothDevice device) {
        if (device == null) {
            return -1;
        }
        int resultBatteryLevel = -1;
        String methodName = "getBatteryLevel";
        Object result = invoke(device, methodName, null);
        if (result instanceof Integer) {
            resultBatteryLevel = (int) result;
        }
        printLog("getBatteryLevel...resultBatteryLevel==" + resultBatteryLevel);
        return resultBatteryLevel;
    }

    public static boolean cancelBondProcess(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        boolean resultCancel = false;
        String methodName = "cancelBondProcess";
        Object result = invoke(device, methodName, null);
        if (result instanceof Boolean) {
            resultCancel = (boolean) result;
        }
        printLog("cancelBondProcess...resultCancel==" + resultCancel);
        return resultCancel;
    }

    public static boolean removeBond(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        boolean resultRemove = false;
        String methodName = "removeBond";
        Object result = invoke(device, methodName, null);
        if (result instanceof Boolean) {
            resultRemove = (boolean) result;
        }
        printLog("removeBond...resultRemove==" + resultRemove);
        return resultRemove;
    }

    public static void unPaired(BluetoothDevice device) {
        if (device != null) {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                removeBond(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                cancelBondProcess(device);
            }
        }
    }

    public static boolean setAlias(BluetoothDevice device, String newAliasName) {
        if (device == null || TextUtil.isEmpty(newAliasName)) {
            return false;
        }
        boolean resultSetAlias = false;
        String methodName = "setAlias";
        Object result = invoke(device, methodName, new Object[]{newAliasName});
        if (result instanceof Boolean) {
            resultSetAlias = (boolean) result;
        }
        printLog("setAlias...resultSetAlias==" + resultSetAlias);
        return resultSetAlias;
    }

    public static List<BluetoothDevice> getBondedDevices(BluetoothAdapter adapter) {
        List<BluetoothDevice> deviceList = new ArrayList<>();
        if (adapter != null) {
            Set<BluetoothDevice> deviceSet = adapter.getBondedDevices();
            for (BluetoothDevice device : deviceSet) {
                if (StarUtil.canShow(device)) {
                    deviceList.add(device);
                }
                Environment.getRootDirectory();
            }
        }
        return deviceList;
    }
}
