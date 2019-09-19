package com.hpplay.sdk.sink.cardemulation.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by duyifeng on 2017/11/24.
 * Device工具类
 */
public class Utils {

    private static final String TAG = "Utils";

    public static String getIP(Context context) {
        String ip = "";
        try {

            // 判断是否是有线网络
            boolean eth0 = false;
            boolean wifi = false;
            boolean mobile = false;
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase("Ethernet")) {
                    eth0 = true;
                } else if (type.equalsIgnoreCase("WIFI")) {
                    wifi = true;
                } else if (type.equalsIgnoreCase("MOBILE")) {
                    mobile = true;
                }
            }

            // 在有些设备上wifi和有线同时存在，获得的ip会有两个
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en == null) {
                return ip;
            }
            while (en.hasMoreElements()) {
                NetworkInterface element = en.nextElement();
                Enumeration<InetAddress> inetAddresses = element.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        ip = inetAddress.getHostAddress().toString();
                        Log.i(TAG, "getIPAddress: " + ip);
                        if (eth0) {
                            if (element.getDisplayName().equals("eth0")) {
                                return ip;
                            }
                        } else if (wifi) {
                            if (element.getDisplayName().equals("wlan0")) {
                                return ip;
                            }
                        } else if (mobile) {
                            return ip;
                        }
                        break;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.w(TAG, ex);
        }
        return ip;
    }

    /* 获取WiFi的SSID */
    /* 判断当前网路有线还是WiFi */
    public static String getNetWorkName(Context context) {

        String wired_network = "有线网络";
        String mobile_network = "移动网络";
        String net_error = "网络错误";

        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase("Ethernet")) {
                    return wired_network;
                } else if (type.equalsIgnoreCase("WIFI")) {
                    String tmpssid = getWifiSSID(context);

                    if (tmpssid.contains("unknown") || tmpssid.contains("0x")) {
                        tmpssid = wired_network;
                    }
                    return tmpssid;
                } else if (type.equalsIgnoreCase("MOBILE")) {
                    return mobile_network;
                } else {
                    return wired_network;
                }
            } else {
                String apName = getAPName(context);
                if (!TextUtils.isEmpty(apName)) {
                    return apName;
                }
                return net_error;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
            return net_error;
        }
    }

    private static String getAPName(Context context) {
        if (!isWifiApOpen(context)) {
            return "";
        }
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(manager);
            return configuration.SSID;
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return "";
    }

    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            int state = (int) method.invoke(manager);
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            int value = (int) field.get(manager);
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return false;
    }

    private static String getWifiSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (TextUtils.isEmpty(wifiInfo.getSSID())) {
            return null;
        }

        String wifiName = wifiInfo.getSSID();
        if (wifiName.contains("\"")) {
            return wifiName.replace("\"", "");
        }

        return wifiName;
    }

    public static int getScreenWidth(Context context) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int HOUR = 1000 * 60 * 60;
    public static int MINUTE = 1000 * 60;
    public static int SECOND = 1000;

    public static String getVideoTimeStr(long timeStamp) {

        if (timeStamp > 0) {

            int hour = (int) timeStamp / HOUR;
            int minute = (int) timeStamp % HOUR / MINUTE;
            int second = (int) timeStamp % HOUR % MINUTE / SECOND;

            String hourStr = String.valueOf(hour);
            if (hour > 0 && hour < 10) {
                hourStr = "0" + hourStr;
            }
            String minuteStr = String.valueOf(minute);
            if (minute < 10) {
                minuteStr = "0" + minuteStr;
            }

            String secondStr = String.valueOf(second);
            if (second < 10) {
                secondStr = "0" + secondStr;
            }

            if (hour > 0) {
                return hourStr + ":" + minuteStr + ":" + secondStr;
            } else
                return minuteStr + ":" + secondStr;

        }

        return "00:00";

    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }


    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws IllegalArgumentException if input length is incorrect
     */
    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
