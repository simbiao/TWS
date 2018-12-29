package com.mobiistar.starbud.util;

import android.util.Log;

import java.io.Closeable;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.CRC32;

/**
 * Description: Ota operation.
 * Date：18-11-16-下午3:39
 * Author: black
 */
public class OtaUtil {
    public static final UUID OTA_SERVICE_OTA_UUID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final UUID OTA_CHARACTERISTIC_OTA_UUID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    public static final UUID OTA_DESCRIPTOR_OTA_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final String LOG_TAG = OtaUtil.class.getSimpleName();

    public static byte[] getOtaFileInfo(String otaFilePath) {
        printLogE("getOtaFileInfo...otaFilePath==" + otaFilePath);
        byte[] otaFileInfo = null;
        try {
            byte[] tempData = FileUtil.readByteFromFile(otaFilePath);
            printLogE("getOtaFileInfo...tempData==" + (tempData != null ? tempData.length : 0));
            int dataSize = tempData.length - 4;
            printLogE("getOtaFileInfo...dataSize==" + dataSize);
            byte[] data = Arrays.copyOf(tempData, dataSize);
            long crc32 = crc32(data, 0, dataSize);
            otaFileInfo = new byte[]{(byte) 0x80, 0x42, 0x45, 0x53, 0x54,
                    (byte) dataSize, (byte) (dataSize >> 8), (byte) (dataSize >> 16), (byte) (dataSize >> 24),
                    (byte) crc32, (byte) (crc32 >> 8), (byte) (crc32 >> 16), (byte) (crc32 >> 24)};
        } catch (Exception e) {
            e.printStackTrace();
            printLogE(e);
        }
        printLogE("getOtaFileInfo...otaFileInfo==" + Arrays.toString(otaFileInfo));
        return otaFileInfo;
    }

    public static byte[] getOtaConfigData(String otaFilePath) {
        printLogE("getOtaConfigData...otaFilePath==" + otaFilePath);
        byte[] otaConfigData = null;
        try {
            byte[] data = FileUtil.readByteFromFile(otaFilePath);

            int configLength = 92;
            byte[] config = new byte[configLength];
            int lengthOfFollowingData = configLength - 4;
            config[0] = (byte) lengthOfFollowingData;
            config[1] = (byte) (lengthOfFollowingData >> 8);
            config[2] = (byte) (lengthOfFollowingData >> 16);
            config[3] = (byte) (lengthOfFollowingData >> 24);
            config[4] = data[data.length - 4];
            config[5] = data[data.length - 3];
            config[6] = data[data.length - 2];
            config[8] = 0x01;
            long crc32 = crc32(config, 0, configLength - 4);
            config[88] = (byte) crc32;
            config[89] = (byte) (crc32 >> 8);
            config[90] = (byte) (crc32 >> 16);
            config[91] = (byte) (crc32 >> 24);

            otaConfigData = new byte[configLength + 1];
            otaConfigData[0] = (byte) 0x86;
            System.arraycopy(config, 0, otaConfigData, 1, configLength);
        } catch (Exception e) {
            printLogE(e);
        }
        printLogE("getOtaConfigData...otaConfigData==" + Arrays.toString(otaConfigData));
        return otaConfigData;
    }

    public static byte[][][] getOtaData(String otaFilePath, int mtu) {
        printLogE("getOtaData...otaFilePath==" + otaFilePath);
        printLogE("getOtaData...mtu==" + mtu);
        byte[][][] otaData = null;
        try {
            byte[] tempData = FileUtil.readByteFromFile(otaFilePath);
            int dataSize = tempData.length - 4;
            int packetPayload = calculateBLESinglePacketLen(dataSize, mtu);
            int totalPacketCount = (dataSize + packetPayload - 1) / packetPayload;
            int onePercentBytes = calculateBLEOnePercentBytes(dataSize);
            int crcCount = (dataSize + onePercentBytes - 1) / onePercentBytes;
            otaData = new byte[crcCount + 1][][];
            printLogE("getOtaData...dataSize==" + dataSize);
            printLogE("getOtaData...totalPacketCount==" + totalPacketCount);
            printLogE("getOtaData...onePercentBytes==" + onePercentBytes);
            printLogE("getOtaData...crcCount==" + crcCount);
            printLogE("getOtaData...packetPayload==" + packetPayload);

            byte[] data = Arrays.copyOf(tempData, dataSize);
            int position = 0;
            for (int i = 0; i < crcCount; i++) {
                int crcBytes = onePercentBytes; //要校验百分之一的数据量
                int length = (crcBytes + packetPayload - 1) / packetPayload; //根据MTU ，算出百分之一需要多少个包满足要求
                if (crcCount - 1 == i) { // 最后一包取余数
                    crcBytes = dataSize - position;
                    length = (crcBytes + packetPayload - 1) / packetPayload;
                }
//                printLogE("CRC BYTES = " + crcBytes);
                otaData[i] = new byte[length + 1][]; //加 1 为增加最后结束整包校验命令
                int realySinglePackLen = 0;
                int crcPosition = position;
                int tempCount = 0;
                for (int j = 0; j < length; j++) {
                    realySinglePackLen = packetPayload;
                    if (j == length - 1) { //每百分之一的最后一包取余数
                        realySinglePackLen = (crcBytes % packetPayload == 0) ? packetPayload : crcBytes % packetPayload;
                    }
                    otaData[i][j] = new byte[realySinglePackLen + 1];
                    System.arraycopy(data, position, otaData[i][j], 1, realySinglePackLen);
                    otaData[i][j][0] = (byte) 0x85;
                    position += realySinglePackLen;
                    tempCount += realySinglePackLen;
                }
//                printLogE("getOtaData...tempCount==" + tempCount);
                long crc32 = crc32(data, crcPosition, crcBytes);
                otaData[i][length] = new byte[]{(byte) 0x82, 0x42, 0x45, 0x53, 0x54, (byte) crc32, (byte) (crc32 >> 8), (byte) (crc32 >> 16), (byte) (crc32 >> 24)};
            }
            otaData[crcCount] = new byte[1][];
            otaData[crcCount][0] = new byte[]{(byte) 0x88};
        } catch (Exception e) {
            printLogE(e);
        }
        printLogE("getOtaData...otaData.length==" + (otaData != null ? otaData.length : 0));
        return otaData;
    }

    public static int calculateBLESinglePacketLen(int imageSize, int mtu) {
        if (imageSize != 0 && imageSize < mtu - 1) {
            return imageSize;
        }
        // mtu shouldn't bigger than (512-3)
        return (mtu > 509) ? 508 : (mtu - 1);
    }

    public static int calculateBLEOnePercentBytes(int imageSize) {
        int onePercentBytes = imageSize / 100;
        if (imageSize < 256) {
            onePercentBytes = imageSize;
        } else {
            int rightBytes = 0;
            if (onePercentBytes < 256) {
                rightBytes = 256 - onePercentBytes;
            } else {
                rightBytes = 256 - onePercentBytes % 256;
            }
            if (rightBytes != 0) {
                onePercentBytes = onePercentBytes + rightBytes;
            }
        }

        if (onePercentBytes > 4 * 1024) {
            onePercentBytes = 4 * 1024;
        }
        int tempCount = (imageSize + onePercentBytes - 1) / onePercentBytes;
        Log.e("BES", "imageSize = " + imageSize + " onepercentBytes = " + onePercentBytes + " crc total Count " + tempCount);
        return onePercentBytes;
    }

    public static long crc32(byte[] data, int offset, int length) {
        CRC32 crc32 = new CRC32();
        crc32.update(data, offset, length);
        return crc32.getValue();
    }

    public static String toHex(byte[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buffer.append(String.format("%02x", data[i])).append(",");
        }
        return buffer.toString();
    }

    public static boolean isEqual(byte[] array_1, byte[] array_2) {
        if (array_1 == null) {
            return array_2 == null;
        }
        if (array_2 == null) {
            return false;
        }
        if (array_1 == array_2) {
            return true;
        }
        if (array_1.length != array_2.length) {
            return false;
        }
        for (int i = 0; i < array_1.length; i++) {
            if (array_1[i] != array_2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean startsWith(byte[] data, byte[] param) {
        if (data == null) {
            return param == null;
        }
        if (param == null) {
            return true;
        }
        if (data.length < param.length) {
            return false;
        }
        for (int i = 0; i < param.length; i++) {
            if (data[i] != param[i]) {
                return false;
            }
        }
        return true;
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                printLogE(e);
            }
        }
    }

    private static void printLogE(Exception e) {
        LogUtil.printLog(Log.ERROR, LOG_TAG, e);
    }

    private static void printLogE(String logContent) {
        LogUtil.printLog(Log.ERROR, LOG_TAG, logContent);
    }
}
