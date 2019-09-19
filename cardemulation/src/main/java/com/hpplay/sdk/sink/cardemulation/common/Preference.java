package com.hpplay.sdk.sink.cardemulation.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Method;

public class Preference {
    private static final String TAG = "Preference";
    private static Preference preference;
    private SharedPreferences mPre;
    private static String preferenceName = "castData";

    private final String SERVER_INFO = "Server_Info";

    public static Preference getInstance() {
        if (preference == null) {
            Application application = getApplication();
            if (application != null) {
                preference = new Preference(application);
            }
        }
        return preference;
    }

    private Preference(Context context) {
        mPre = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);

    }

    public static Application getApplication() {
        Application application = null;
        Class<?> activityThreadClass;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            final Method method2 = activityThreadClass.getMethod(
                    "currentActivityThread", new Class[0]);
            // 得到当前的ActivityThread对象
            Object localObject = method2.invoke(null, (Object[]) null);
            final Method method = activityThreadClass
                    .getMethod("getApplication");
            application = (Application) method.invoke(localObject, (Object[]) null);

        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return application;
    }


    public void setServerInfo(String str) {
        mPre.edit().putString(SERVER_INFO,str).commit();
    }

    public String getServerInfo() {
        String ServerStr = mPre.getString(SERVER_INFO,"");
        return ServerStr;
    }

}
