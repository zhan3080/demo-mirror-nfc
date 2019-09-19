package com.hpplay.sdk.source.test;

import android.app.Activity;
import android.content.Context;

import com.hpplay.common.utils.LeLog;
import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.ILelinkMirrorManager;
import com.hpplay.sdk.source.api.ILelinkPlayerListener;
import com.hpplay.sdk.source.api.IRelevantInfoListener;
import com.hpplay.sdk.source.api.LelinkPlayer;
import com.hpplay.sdk.source.api.LelinkPlayerInfo;
import com.hpplay.sdk.source.browse.api.IAPI;
import com.hpplay.sdk.source.browse.api.IConferenceFuzzyMatchingPinCodeListener;
import com.hpplay.sdk.source.browse.api.ILelinkServiceManager;
import com.hpplay.sdk.source.browse.api.IPinCodeListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.hpplay.sdk.source.browse.api.LelinkServiceManager;
import com.hpplay.sdk.source.browse.api.LelinkSetting;

/**
 * Created by Zippo on 2018/5/16.
 * Date: 2018/5/16
 * Time: 14:33:06
 */
public class AllCast {

    private static final String TAG = "AllCast";

    public static final int RESOLUTION_HEIGHT = ILelinkMirrorManager.RESOLUTION_HIGH;
    public static final int RESOLUTION_MIDDLE = ILelinkMirrorManager.RESOLUTION_MID;
    public static final int RESOLUTION_AUTO = ILelinkMirrorManager.RESOLUTION_AUTO;
    public static final int BITRATE_HEIGHT = ILelinkMirrorManager.BITRATE_HIGH;
    public static final int BITRATE_MIDDLE = ILelinkMirrorManager.BITRATE_MID;
    public static final int BITRATE_LOW = ILelinkMirrorManager.BITRATE_LOW;

    private IPinCodeListener mPinCodeListener;
    private ILelinkServiceManager mLelinkServiceManager;
    private LelinkPlayer mLelinkPlayer;

    public AllCast(Context context, String appid, String appSecret) {
        initLelinkService(context, appid, appSecret);
    }

    public void setPinCodeListener(IPinCodeListener l) {
        this.mPinCodeListener = l;
    }

    public void setConnectListener(IConnectListener listener) {
        mLelinkPlayer.setConnectListener(listener);
    }

    public void setPlayerListener(ILelinkPlayerListener listener) {
        mLelinkPlayer.setPlayerListener(listener);
        mLelinkPlayer.setRelevantInfoListener(new IRelevantInfoListener() {
            @Override
            public void onSendRelevantInfoResult(int option, String result) {
                LeLog.d(TAG, "option : " + option + " result: " + result);
            }

        });
    }

    private void initLelinkService(Context context, String appid, String appSecret) {
        LelinkSetting lelinkSetting = new LelinkSetting.LelinkSettingBuilder(appid, appSecret)
                .build();
        mLelinkServiceManager = LelinkServiceManager.getInstance(context);
        mLelinkServiceManager.setDebug(true);
        mLelinkServiceManager.setLelinkSetting(lelinkSetting);
        mLelinkServiceManager.setOption(IAPI.OPTION_5, false);
        initLelinkPlayer(context);
    }

    public void setFuzzyMatchingPinCodeListener(IConferenceFuzzyMatchingPinCodeListener l) {
        mLelinkServiceManager.setOption(IAPI.OPTION_23, l);
    }

    private void initLelinkPlayer(Context pContext) {
        mLelinkPlayer = new LelinkPlayer(pContext);
    }

    public void refreshConferenceServer(String serverUrl) {
        mLelinkServiceManager.setOption(IAPI.OPTION_11, serverUrl);
    }

    public void fuzzyMatching(String pinCode) {
        mLelinkServiceManager.setOption(IAPI.OPTION_24, pinCode);
    }

    public void connect(LelinkServiceInfo pInfo) {
        mLelinkPlayer.connect(pInfo);
    }

    public void disConnect(LelinkServiceInfo pInfo) {
        if (pInfo != null) {
            mLelinkPlayer.disConnect(pInfo);
        }
    }

    public void appendMultiMirror(LelinkServiceInfo... serviceInfos) {
        if (mLelinkPlayer != null) {
            mLelinkPlayer.setOption(IAPI.OPTION_14, serviceInfos);
        }
    }

    public void removeMultiMirror(LelinkServiceInfo... serviceInfos) {
        if (mLelinkPlayer != null) {
            mLelinkPlayer.setOption(IAPI.OPTION_20, serviceInfos);
        }
    }

    public void startMirror(Activity pActivity, LelinkServiceInfo lelinkServiceInfo,
                            int resolutionLevel, int bitrateLevel, boolean isAudioEnnable) {
        if (mLelinkPlayer != null) {
            LelinkPlayerInfo lelinkPlayerInfo = new LelinkPlayerInfo();
            lelinkPlayerInfo.setType(LelinkPlayerInfo.TYPE_MIRROR);
            lelinkPlayerInfo.setActivity(pActivity);
            lelinkPlayerInfo.setLelinkServiceInfo(lelinkServiceInfo);
            lelinkPlayerInfo.setMirrorAudioEnable(isAudioEnnable);
            lelinkPlayerInfo.setResolutionLevel(resolutionLevel);
            lelinkPlayerInfo.setBitRateLevel(bitrateLevel);
            mLelinkPlayer.setDataSource(lelinkPlayerInfo);
            mLelinkPlayer.start();
        }
    }

    public void stopMirror() {
        if (mLelinkPlayer != null) {
            mLelinkPlayer.stop();
        }
    }
    public void pauseMirror() {
        if (mLelinkPlayer != null) {
            mLelinkPlayer.pause();
        }
    }

    public void resumeMirror() {
        if (mLelinkPlayer != null) {
            mLelinkPlayer.resume();
        }
    }

}