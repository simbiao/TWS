package com.mobiistar.starbud.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.mobiistar.starbud.R;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Description: File operation.
 * Date：18-11-5-上午10:25
 * Author: black
 */
public class FileUtil {

    private static final String LOG_TAG = File.class.getSimpleName();
    private static final String MIME_TYPE_APPLICATION = "application/vnd.android.package-archive";
    private static final String FILE_TYPE_APK = ".apk";
    private static final String FILE_TYPE_ZIP = ".zip";
    private static final int BUFFER_SIZE = 4096;

    private static boolean checkDir(File file) {
        boolean checkResult = false;
        if (file != null) {
            if (file.exists()) {
                checkResult = file.isDirectory();
            } else {
                try {
                    checkResult = file.mkdirs();
                } catch (Exception e) {
                    printLogE(e);
                }
            }
        }
        return checkResult;
    }

    public static boolean checkFile(File file) {
        boolean checkResult = false;
        if (file != null) {
            if (checkDir(file.getParentFile())) {
                if (file.exists()) {
                    checkResult = file.isFile();
                } else {
                    try {
                        checkResult = file.createNewFile();
                    } catch (Exception e) {
                        printLogE(e);
                    }
                }
            }
        }
        return checkResult;
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

    public static boolean saveToFile(String content, File file) {
        if (!checkFile(file)) {
            return false;
        }

        boolean saveResult = false;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.flush();
            saveResult = true;
        } catch (Exception e) {
            printLogE(e);
        } finally {
            closeStream(fileWriter);
        }
        return saveResult;
    }

    public static boolean saveToFile(String content, String filePath) {
        return saveToFile(content, new File(filePath));
    }

    public static boolean saveToFile(Throwable e, File file) {
        if (!checkFile(file)) {
            return false;
        }

        boolean saveResult = false;
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new FileWriter(file, true), true);
            e.printStackTrace(printWriter);
            printWriter.flush();
            saveResult = true;
        } catch (Exception e1) {
            printLogE(e1);
        } finally {
            closeStream(printWriter);
        }
        return saveResult;
    }

    public static boolean saveToFile(Throwable e, String filePath) {
        return saveToFile(e, new File(filePath));
    }

    public static String readFromFile(File file) {
        String content = "";
        if (file != null && file.exists()) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
                char[] buffer = new char[BUFFER_SIZE];
                int result;
                StringBuilder stringBuilder = new StringBuilder();
                while ((result = fileReader.read(buffer, 0, buffer.length)) != -1) {
                    stringBuilder.append(buffer, 0, result);
                }
                content = stringBuilder.toString();
            } catch (Exception e) {
                printLogE(e);
            } finally {
                closeStream(fileReader);
            }
        }
        return content;
    }

    public static String readFromFile(String filePath) {
        return readFromFile(new File(filePath));
    }

    public static boolean saveToFile(InputStream inputStream, File file) {
        if (!checkFile(file)) {
            return false;
        }

        boolean saveResult = false;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int result;
            while ((result = inputStream.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, result);
            }
            outputStream.flush();
            saveResult = true;
        } catch (Exception e) {
            printLogE(e);
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }
        return saveResult;
    }

    public static boolean saveToFile(InputStream inputStream, String filePath) {
        return saveToFile(inputStream, new File(filePath));
    }

    public static String getParentPath(String filePath) {
        String parentPath = "";
        if (!TextUtil.isEmpty(filePath)) {
            parentPath = new File(filePath).getParent();
        }
        return parentPath;
    }

    public static String getFileName(String filePath) {
        String fileName = "";
        String line = "/";
        String dot = ".";
        if (!TextUtil.isEmpty(filePath) && !filePath.endsWith(line) && !filePath.endsWith(dot)) {
            int indexLine = filePath.lastIndexOf("/");
            if (indexLine != -1) {
                filePath = filePath.substring(indexLine + 1);
            }
            int indexDot = filePath.lastIndexOf(".");
            if (indexDot != -1) {
                filePath = filePath.substring(0, indexDot);
            }
            fileName = filePath;
        }
        return fileName;
    }

    private static String getRealApkFile(String apkFilePath) {
        String realApkFile = "";
        try {
            File apkFile = new File(apkFilePath);
            if (!TextUtil.isEmpty(apkFilePath) && apkFile.exists()) {
                if (apkFilePath.endsWith(FILE_TYPE_APK)) {
                    realApkFile = apkFilePath;
                } else if (apkFilePath.endsWith(FILE_TYPE_ZIP)) {
                    String dirName = getFileName(apkFilePath);
                    realApkFile = apkFile.getParent() + File.separator + dirName + File.separator + dirName + FILE_TYPE_APK;
                }
            }
        } catch (Exception e) {
            printLogE(e);
        }
        return realApkFile;
    }

    public static boolean installApkFile(Context context, String apkFilePath) {
        printLogE("installApkFile...apkFilePath==" + apkFilePath);
        apkFilePath = getRealApkFile(apkFilePath);
        printLogE("installApkFile...realApkFilePath==" + apkFilePath);
        boolean installResult = false;
        try {
            File apkFile = new File(apkFilePath);
            if (context != null && !TextUtil.isEmpty(apkFilePath) && apkFile.exists()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authorities), apkFile);
                    printLogE("installApkFile...contentUri==" + contentUri);
                    intent.setDataAndType(contentUri, MIME_TYPE_APPLICATION);
                } else {
                    printLogE("installApkFile...apkFile==" + apkFile);
                    intent.setDataAndType(Uri.fromFile(apkFile), MIME_TYPE_APPLICATION);
                }
                printLogE("installApkFile...intent==" + intent);
                context.startActivity(intent);
                installResult = true;
            }
        } catch (Exception e) {
            printLogE(e);
        }
        printLogE("installApkFile...installResult==" + installResult);
        return installResult;
    }

    public static boolean unzipFile(String zipFilePath) {
        printLogE("unzipFile...zipFilePath==" + zipFilePath);
        boolean unzipResult = false;
        try {
            // ZipFile
            File file = new File(zipFilePath);
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();

            // UnzipDir
            String dirName = FileUtil.getFileName(zipFilePath);
            File unzipDir = new File(file.getParentFile(), dirName);
            FileUtil.checkDir(unzipDir);
            String unzipDirPath = unzipDir.getAbsolutePath() + File.separator;

            // Unzip
            ZipEntry zipEntry;
            File tempFile;
            boolean createResult = true;
            while (entryEnumeration.hasMoreElements() && createResult) {
                zipEntry = entryEnumeration.nextElement();
                tempFile = new File(unzipDirPath + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    createResult = tempFile.mkdirs();
                } else {
                    createResult = tempFile.createNewFile();
                    saveToFile(zipFile.getInputStream(zipEntry), tempFile);
                }
            }
            unzipResult = true;
        } catch (Exception e) {
            printLogE(e);
        }
        printLogE("unzipFile...unzipResult==" + unzipResult);
        return unzipResult;
    }

    public static byte[] readByteFromFile(String filePath) {
        printLogE("filePath...filePath==" + filePath);
        return readByteFromFile(new File(filePath));
    }

    public static byte[] readByteFromFile(File file) {
        printLogE("readByteFromFile...file==" + file);
        byte[] byteData = null;
        if (file != null && file.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                int length = inputStream.available();
                if (length > 1024 * 1024) {
                    byte[] totalData = null;
                    byte[] tempTotalData;
                    byte[] tempData = new byte[4096];
                    int result;
                    while ((result = inputStream.read(tempData, 0, tempData.length)) != -1) {
                        tempTotalData = totalData == null ? new byte[result] : new byte[totalData.length + result];
                        System.arraycopy(tempData, 0, tempTotalData, tempTotalData.length - result, result);
                        totalData = tempTotalData;
                    }
                    byteData = totalData;
                } else if (length > 0) {
                    byteData = new byte[length];
                    length = inputStream.read(byteData, 0, length);
                }
                printLogE("readByteFromFile...length==" + length);
            } catch (Exception e) {
                printLogE(e);
            } finally {
                closeStream(inputStream);
            }
        }
        printLogE("readByteFromFile...byteData==" + (byteData != null ? byteData.length : 0));
        printLogE("readByteFromFile...file==" + file);
        return byteData;
    }

    public static void deleteFile(String filePath) {
        printLogE("deleteFile...filePath==" + filePath);
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                printLogE("deleteFile...deleteResult==" + file.delete());
            }
        } catch (Exception e) {
            printLogE(e);
        }
    }
}
