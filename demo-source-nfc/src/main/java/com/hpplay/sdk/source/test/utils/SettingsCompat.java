package com.hpplay.sdk.source.test.utils;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by Zippo on 2019/3/9.
 * Date: 2019/3/9
 * Time: 7:48:02
 */
public class SettingsCompat {

    private static final String TAG = "SettingsCompat";

    private static final int OP_WRITE_SETTINGS = 23;
    private static final int OP_SYSTEM_ALERT_WINDOW = 24;

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return checkOp(context, OP_SYSTEM_ALERT_WINDOW);
        } else {
            return true;
        }
    }

    public static boolean canWriteSettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return checkOp(context, OP_WRITE_SETTINGS);
        } else {
            return true;
        }
    }

    public static boolean setDrawOverlays(Context context, boolean allowed) {
        return setMode(context, OP_SYSTEM_ALERT_WINDOW, allowed);
    }

    public static boolean setWriteSettings(Context context, boolean allowed) {
        return setMode(context, OP_WRITE_SETTINGS, allowed);
    }

    public static void manageDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }

    public static void manageWriteSettings(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }

    private static boolean checkOp(Context context, int op) {
        AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            Method method = AppOpsManager.class.getDeclaredMethod("checkOp", int.class, int.class, String.class);
            return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return false;
    }

    // 可设置Android 4.3/4.4的授权状态
    private static boolean setMode(Context context, int op, boolean allowed) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }

        AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            Method method = AppOpsManager.class.getDeclaredMethod("setMode", int.class, int.class, String.class, int.class);
            method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName(), allowed ? AppOpsManager.MODE_ALLOWED : AppOpsManager
                    .MODE_IGNORED);
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));

        }
        return false;
    }

    private static boolean startSafely(Context context, Intent intent) {
        if (context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } else {
            Log.e(TAG, "Intent is not available! " + intent);
            return false;
        }
    }

}