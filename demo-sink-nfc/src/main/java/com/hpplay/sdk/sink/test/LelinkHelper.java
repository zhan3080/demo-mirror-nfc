package com.hpplay.sdk.sink.test;

import android.content.Context;
import android.util.Log;

import com.hpplay.sdk.sink.api.CastInfo;
import com.hpplay.sdk.sink.api.ClientInfo;
import com.hpplay.sdk.sink.api.IAPI;
import com.hpplay.sdk.sink.api.IServerListener;
import com.hpplay.sdk.sink.api.LelinkCast;
import com.hpplay.sdk.sink.api.ServerInfo;
import com.hpplay.sdk.sink.feature.IAuthCodeCallback;
import com.hpplay.sdk.sink.test.common.Preference;
import com.hpplay.sdk.sink.test.common.Utils;

import org.json.JSONObject;

public class LelinkHelper {
    private final static String TAG = "LelinkHelper";
    private static LelinkHelper mLelinkHelper;
    private final String APP_KEY = "14";
    private final String APP_SECRET = "6d02e892ee1f8678b08d62fadd7392bb";
    private Context mContext;
    private LelinkCast mLelinkCast;

    public static LelinkHelper getInstance(Context context) {
        if (mLelinkHelper == null) {
            mLelinkHelper = new LelinkHelper(context);
        }
        return mLelinkHelper;
    }

    private LelinkHelper(Context context) {
        mContext = context;
        mLelinkCast = new LelinkCast(context, APP_KEY, APP_SECRET);
    }

    private IServerListener mIServerListener = new IServerListener() {
        @Override
        public void onStart(int i, ServerInfo serverInfo) {
            Log.i(TAG, "onStart name:" + serverInfo.deviceName + ", port:" + serverInfo.serverPort);
            Log.i(TAG, "onStart:" + serverInfo);
            saveServerInfo(serverInfo.serverPort);
        }

        @Override
        public void onStop(int i) {

        }

        @Override
        public void onError(int i, int i1, int i2) {

        }

        @Override
        public void onAuthSDK(int i, int i1) {

        }

        @Override
        public void onCast(int i, CastInfo castInfo) {
            String msg = "castInfo:" + castInfo.castType + castInfo.mimeType + castInfo.infoType;
            Log.i(TAG, "onStart:" + msg);
        }

        @Override
        public void onAuthConnect(int i, String s, int i1) {

        }

        @Override
        public void onConnect(int i, ClientInfo clientInfo) {

        }

        @Override
        public void onDisconnect(int i, ClientInfo clientInfo) {

        }
    };

    public void startServer(String name) {
        Log.i(TAG, "startServer name:" + name);
        ServerInfo info = getOption(IAPI.OPTION_SERVERINFO, ServerInfo.class);
        if (info == null || info.serviceStatus == ServerInfo.SERVER_IDLE) {
            setOption(IAPI.OPTION_DEIVCENAME, name);
            setOption(IAPI.OPTION_SERVERLISTENER, mIServerListener);
            performAction(IAPI.ACTION_STARTSERVER);
        }

    }

    public void stopServer() {
        performAction(IAPI.ACTION_STOPSERVER);
    }

    private void saveServerInfo(int port){
        String ip = Utils.getIP(mContext);
        try {
            JSONObject jobj = new JSONObject();
            jobj.put("ip",ip);
            jobj.put("port",port);
            Preference.getInstance().setServerInfo(jobj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public int setOption(int option, Object... values) {
        Object result = mLelinkCast.setOption(option, values);
        int callResult = Integer.valueOf(result.toString());
        if (callResult == IAPI.INVALID_CALL) {
            Log.w(TAG, "setOption invalid call, option: " + option);
        }
        return callResult;
    }

    public int performAction(int action, Object... values) {
        Object result = mLelinkCast.performAction(action, values);
        int callResult = Integer.valueOf(result.toString());
        if (callResult == IAPI.INVALID_CALL) {
            Log.w(TAG, "performAction invalid call, action: " + action);
        }
        return callResult;
    }

    public <T> T getOption(int option, Class<T> classOfT) {
        if (mLelinkCast != null) {
            Object object = mLelinkCast.getOption(option);
            try {
                return classOfT.cast(object);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
        return null;
    }
}
