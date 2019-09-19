package com.hpplay.sdk.source.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.TextView;

import com.hpplay.common.utils.DeviceUtil;
import com.hpplay.common.utils.NetworkUtil;
import com.hpplay.sdk.source.test.utils.ToastUtil;

import java.lang.ref.WeakReference;

public abstract class BaseActivity extends Activity {

    private static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    protected static final int REQUEST_MUST_PERMISSION = 1;
    protected static final int REQUEST_RECORD_AUDIO_PERMISSION = 4;
    private TextView mTvVersion, mTvWifi, mTvIp;
    private NetworkReceiver mNetworkReceiver;
    public LelinkHelper mLelinkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTvVersion = (TextView) findViewById(R.id.tv_version);
        mTvWifi = (TextView) findViewById(R.id.tv_wifi);
        mTvIp = (TextView) findViewById(R.id.tv_ip);
        initNetwork();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mNetworkReceiver) {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
    }

    void initNetwork() {
        mNetworkReceiver = new NetworkReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, intentFilter);

        mTvVersion.setText("SDK:" + com.hpplay.sdk.source.api.BuildConfig.BUILD_TYPE
                + "-" + com.hpplay.sdk.source.api.BuildConfig.VERSION_NAME);
        refreshWifiName();
    }

    public void refreshWifiName() {
        mTvWifi.setText("WiFi:" + NetworkUtil.getNetWorkName(getApplicationContext()));
        mTvIp.setText(DeviceUtil.getIPAddress(getApplicationContext()));
    }


    private static class NetworkReceiver extends BroadcastReceiver {

        private WeakReference<BaseActivity> mReference;

        public NetworkReceiver(BaseActivity preference) {
            mReference = new WeakReference<>(preference);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == mReference || null == mReference.get()) {
                return;
            }
            BaseActivity activity = mReference.get();
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equalsIgnoreCase(action) ||
                    WIFI_AP_STATE_CHANGED_ACTION.equalsIgnoreCase(action)) {
                activity.refreshWifiName();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_MUST_PERMISSION) {
            boolean denied = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied = true;
                }
            }
            if (denied) {
                // 拒绝
                ToastUtil.show(getApplicationContext(), "您拒绝了权限");
            } else {
                // 允许
                initLelinkHelper();
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            boolean denied = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied = true;
                }
            }
            if (denied) {
                // 拒绝
                ToastUtil.show(getApplicationContext(), "您录制音频的权限");
            } else {
                // 允许
//                startMirror();
                startMultiMirror();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    protected abstract void initLelinkHelper();

    protected abstract void startMultiMirror();
}
