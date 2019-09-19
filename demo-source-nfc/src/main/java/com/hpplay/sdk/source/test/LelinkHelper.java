package com.hpplay.sdk.source.test;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.hpplay.common.utils.LeLog;
import com.hpplay.sdk.source.api.IConferenceMirrorListener;
import com.hpplay.sdk.source.api.IConnectListener;
import com.hpplay.sdk.source.api.ILelinkPlayerListener;
import com.hpplay.sdk.source.browse.api.IConferenceFuzzyMatchingPinCodeListener;
import com.hpplay.sdk.source.browse.api.IPinCodeListener;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.hpplay.sdk.source.test.bean.MessageDeatail;
import com.hpplay.sdk.source.test.utils.Logger;

import java.util.List;

/**
 * Created by Zippo on 2018/10/13.
 * Date: 2018/10/13
 * Time: 17:08:24
 */
public class LelinkHelper {

    private static final String TAG = "LelinkHelper";

    private static final String APP_ID = "9999";
    private static final String APP_SECRET = "68bbd5646a32df651db861930f63158e";

    private static LelinkHelper sLelinkHelper;
    private Context mContext;
    private UIHandler mUIHandler;
    private AllCast mAllCast;

    public static LelinkHelper getInstance(Context context) {
        if (sLelinkHelper == null) {
            sLelinkHelper = new LelinkHelper(context);
        }
        return sLelinkHelper;
    }

    private LelinkHelper(Context context) {
        mContext = context;
        mUIHandler = new UIHandler(Looper.getMainLooper());
        mAllCast = new AllCast(context.getApplicationContext(), APP_ID, APP_SECRET);
        mAllCast.setPinCodeListener(mPinCodeListener);
        mAllCast.setFuzzyMatchingPinCodeListener(mFuzzyMatchingPinCodeListener);
        mAllCast.setConnectListener(mConnectListener);
        mAllCast.setPlayerListener(mPlayerListener);
    }

    public void setUIUpdateListener(IUIUpdateListener listener) {
        mUIHandler.setUIUpdateListener(listener);
    }

    public void refreshConferenceServer(String serverUrl) {
        mAllCast.refreshConferenceServer(serverUrl);
    }

    public void fuzzyMatching(String pinCode) {
        mAllCast.fuzzyMatching(pinCode);
    }

    public void connect(LelinkServiceInfo info) {
        mAllCast.connect(info);
    }

    public void disConnect(LelinkServiceInfo info) {
        mAllCast.disConnect(info);
    }

    public void appendMultiMirror(LelinkServiceInfo... serviceInfos) {
        mAllCast.appendMultiMirror(serviceInfos);
    }

    public void removeMultiMirror(LelinkServiceInfo... serviceInfos) {
        mAllCast.removeMultiMirror(serviceInfos);
    }

    public void startMirror(Activity activity, LelinkServiceInfo info, int resolutionLevel,
                            int bitrateLevel, boolean audioEnable) {
        mAllCast.startMirror(activity, info, resolutionLevel, bitrateLevel, audioEnable);
    }

    public void stopMirror() {
        mAllCast.stopMirror();
    }

    public void pauseMultiMirror() {
        mAllCast.pauseMirror();
    }

    public void resumeMultiMirror() {
        mAllCast.resumeMirror();
    }

    private Message buildMessageDetail(int state, String text) {
        return buildMessageDetail(state, text, null);
    }

    private Message buildMessageDetail(int state, String text, Object object) {
        MessageDeatail deatail = new MessageDeatail();
        deatail.text = text;
        deatail.obj = object;

        Message message = Message.obtain();
        message.what = state;
        message.obj = deatail;
        return message;
    }

    private IPinCodeListener mPinCodeListener = new IPinCodeListener() {

        @Override
        public void onParceResult(int resultCode, LelinkServiceInfo info) {
            Logger.d(TAG, "addPinCodeServiceInfo resultCode:" + resultCode + " info:" + info);
            if (resultCode == IPinCodeListener.PARCE_SUCCESS) {
                if (null != mUIHandler) {
                    String text = "Pin码解析成功";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_SUCCESS, text, info));
                }
            } else if (resultCode == IPinCodeListener.PARCE_ERROR) {
                if (null != mUIHandler) {
                    String text = "Pin码错误";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_ERROR, text, info));
                }
            } else {
                if (null != mUIHandler) {
                    String text = "Pin码解析失败";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_SEARCH_ERROR, text, info));
                }
            }
        }

    };

    private IConferenceFuzzyMatchingPinCodeListener mFuzzyMatchingPinCodeListener = new IConferenceFuzzyMatchingPinCodeListener() {

        @Override
        public void onParceResult(int resultCode, List<LelinkServiceInfo> list) {
            LeLog.i(TAG, "fuzzyMatching onParceResult resultCode:" + resultCode + " list:" + list);
            if (resultCode == IPinCodeListener.PARCE_SUCCESS) {
                if (null != mUIHandler) {
                    String text = "模糊搜索成功";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONFERENCE_FUZZY_SEARCH, text, list));
                }
            } else {
                if (null != mUIHandler) {
                    String text = "模糊搜索失败";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONFERENCE_FUZZY_SEARCH, text, list));
                }
            }
        }

    };

    private IConnectListener mConnectListener = new IConnectListener() {

        @Override
        public void onConnect(final LelinkServiceInfo serviceInfo, final int extra) {
            Logger.d(TAG, "onConnect:" + serviceInfo.getName());
            if (null != mUIHandler) {
                String text;
                if (TextUtils.isEmpty(serviceInfo.getName())) {
                    text = "pin码连接Lelink成功";
                } else {
                    text = serviceInfo.getName() + "连接Lelink成功";
                }
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONNECT_SUCCESS, text));
            }
        }

        @Override
        public void onDisconnect(LelinkServiceInfo serviceInfo, int what, int extra) {
            Logger.d(TAG, "onDisconnect:" + serviceInfo.getName() + " disConnectType:" + what + " extra:" + extra);
            if (what == IConnectListener.CONNECT_INFO_DISCONNECT) {
                if (null != mUIHandler) {
                    String text;
                    if (TextUtils.isEmpty(serviceInfo.getName())) {
                        text = "pin码连接断开";
                    } else {
                        text = serviceInfo.getName() + "连接断开";
                    }
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_DISCONNECT, text));
                }
            } else if (what == IConnectListener.CONNECT_ERROR_FAILED) {
                String text = null;
                if (extra == IConnectListener.CONNECT_ERROR_IO) {
                    text = serviceInfo.getName() + "连接失败";
                } else if (extra == IConnectListener.CONNECT_CONFRENCE_CHECK_LAN) {
                    text = "检查不是同一局域网";
                } else if (extra == IConnectListener.CONNECT_PINCODE_ERROR) {
                    text = "投影码错误";
                } else if (extra == IConnectListener.CONNECT_REQUEST_FAILED) {
                    text = "获取投影码失败";
                }
                if (null != mUIHandler) {
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONNECT_FAILURE, text));
                }
            }
        }

    };

    private IConferenceMirrorListener mPlayerListener = new IConferenceMirrorListener() {

        @Override
        public void onLoading() {
            // ignore
        }

        @Override
        public void onStart() {
            Logger.d(TAG, "onStart:");
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_PLAY, "开始播放"));
            }
        }

        @Override
        public void onPause() {
            // ignore
        }

        @Override
        public void onCompletion() {
            // ignore
        }

        @Override
        public void onStop() {
            Logger.d(TAG, "onStop");
            if (null != mUIHandler) {
                mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_STOP, "播放结束"));
            }
        }

        @Override
        public void onSeekComplete(int pPosition) {
            // ignore
        }

        @Override
        public void onInfo(int what, int extra) {
            Logger.d(TAG, "onInfo what:" + what + " extra:" + extra);
        }

        @Override
        public void onError(int what, int extra) {
            Logger.d(TAG, "onError what:" + what + " extra:" + extra);
            String text = null;
            if (what == ILelinkPlayerListener.MIRROR_ERROR_INIT) {
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_UNSUPPORTED) {
                    text = "不支持镜像";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_REJECT_PERMISSION) {
                    text = "镜像权限拒绝";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_DEVICE_UNSUPPORTED) {
                    text = "设备不支持镜像";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                }
            } else if (what == ILelinkPlayerListener.MIRROR_ERROR_PREPARE) {
                if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_INFO) {
                    text = "获取镜像信息出错";
                } else if (extra == ILelinkPlayerListener.MIRROR_ERROR_GET_PORT) {
                    text = "获取镜像端口出错";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_INPUT_SCREENCODE, text));
                    return;
                } else if (extra == ILelinkPlayerListener.GRAP_UNSUPPORTED) {
                    text = "投屏码模式不支持抢占";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_PLAY) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "播放无响应";
                } else if (extra == ILelinkPlayerListener.NEED_SCREENCODE) {
                    text = "请输入投屏码";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_INPUT_SCREENCODE, text));
                    return;
                } else if (extra == ILelinkPlayerListener.RELEVANCE_DATA_UNSUPPORTED) {
                    text = "老乐联不支持数据透传,请升级接收端的版本！";
                    mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.RELEVANCE_DATA_UNSUPPORT, text));
                    return;
                } else if (extra == ILelinkPlayerListener.GRAP_UNSUPPORTED) {
                    text = "投屏码模式不支持抢占";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_STOP) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "退出 播放无响应";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_PAUSE) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "暂停无响应";
                }
            } else if (what == ILelinkPlayerListener.PUSH_ERROR_RESUME) {
                if (extra == ILelinkPlayerListener.PUSH_ERROR_NOT_RESPONSED) {
                    text = "恢复无响应";
                }
            } else if (what == ILelinkPlayerListener.CONFERENCE_PINCODE_CONVERTTODEV_ERROR) {
                text = extra + " 连接失败！";
            }
            mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_PLAY_ERROR, text));
        }

        @Override
        public void onVolumeChanged(float percent) {
            // ignore
        }

        @Override
        public void onPositionUpdate(long duration, long position) {
            // ignore
        }

        @Override
        public void onAction(int status, int extra, String params) {
            String text;
            if (extra == IConferenceMirrorListener.CONFERENCE_CONNECT_FAILURE) {
                text = params + "连接失败";
            } else if (extra == IConferenceMirrorListener.CONFERENCE_CONNECT_DISCONNECT_BY_REJECT) {
                text = params + "连接被拒绝，投屏不允许抢占";
            } else if (extra == IConferenceMirrorListener.CONFERENCE_CONNECT_DISCONNECT) {
                text = params + "发送端主动断开";
            } else if (extra == IConferenceMirrorListener.CONFERENCE_CONNECT_DISCONNECT_BY_SINK) {
                text = params + "接收端主动断开";
            } else if (extra == IConferenceMirrorListener.CONFERENCE_CONNECT_DISCONNECT_BY_PREEMPT) {
                text = params + "被抢占断开";
            } else if (extra == IConferenceMirrorListener.CONFERENCE_CONNECT_DISCONNECT_BY_SERVER) {
                text = params + "从服务器后台断开";
            } else if (extra == IConferenceMirrorListener.CONFERENCE_CONNECT_DISCONNECT_NOT_FIND) {
                text = params + "未找到设备";
            } else if (extra == IConferenceMirrorListener.CONFERENCE_CAST_SUCESS) {
                text = params + "投屏成功";
            } else {
                text = params + "未知异常断开";
            }
            mUIHandler.sendMessage(buildMessageDetail(IUIUpdateListener.STATE_CONFERENCE_MULTI_MIRROR, text));
        }

    };

    private static class UIHandler extends Handler {

        private IUIUpdateListener mUIUpdateListener;

        private UIHandler(Looper looper) {
            super(looper);
        }

        private void setUIUpdateListener(IUIUpdateListener l) {
            mUIUpdateListener = l;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MessageDeatail detail = (MessageDeatail) msg.obj;
            if (null != mUIUpdateListener) {
                mUIUpdateListener.onUpdate(msg.what, detail);
            }
        }
    }

}
