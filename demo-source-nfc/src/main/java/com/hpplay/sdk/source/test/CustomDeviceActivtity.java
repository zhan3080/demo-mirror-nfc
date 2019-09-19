package com.hpplay.sdk.source.test;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hpplay.sdk.source.browse.api.LelinkMultiServiceInfo;
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.hpplay.sdk.source.test.adapter.BrowseAdapter;
import com.hpplay.sdk.source.test.bean.MessageDeatail;
import com.hpplay.sdk.source.test.nfc.CardReaderFragment;
import com.hpplay.sdk.source.test.utils.Logger;
import com.hpplay.sdk.source.test.utils.SettingsCompat;
import com.hpplay.sdk.source.test.utils.ToastUtil;

import org.json.JSONObject;

public class CustomDeviceActivtity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "CustomDeviceActivtity";

    private RecyclerView mRecyclerView;
    private BrowseAdapter mBrowseAdapter;
    private EditText mIpEdit, mPortEdit;
    private RadioGroup mRgResolution, mRgBitRate, mRgMirrorAudio;
    private LelinkServiceInfo mLelinkMultiServiceInfo;
    private EditText mSubEditIP, mSubPortIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_custom_dev);
        super.onCreate(savedInstanceState);
        initViews();
        initEvents();
        initDatas();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        CardReaderFragment fragment = new CardReaderFragment();
        transaction.replace(R.id.sample_content_fragment1, fragment);
        transaction.commit();
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRgResolution = (RadioGroup) findViewById(R.id.rg_resolution);
        mRgBitRate = (RadioGroup) findViewById(R.id.rg_bitrate);
        mRgMirrorAudio = (RadioGroup) findViewById(R.id.rg_mirror_audio);
        mIpEdit = (EditText) findViewById(R.id.ip_edit);
        mPortEdit = (EditText) findViewById(R.id.port_edit);
    }

    private void initEvents() {
        findViewById(R.id.btn_start_multi_mirror).setOnClickListener(this);
        findViewById(R.id.btn_append_multi_mirror).setOnClickListener(this);
        findViewById(R.id.btn_remove_multi_mirror).setOnClickListener(this);
        findViewById(R.id.btn_stop_multi_mirror).setOnClickListener(this);
        findViewById(R.id.add_sub_devs).setOnClickListener(this);
        findViewById(R.id.pause_multi_mirror).setOnClickListener(this);
        findViewById(R.id.resume_multi_mirror).setOnClickListener(this);

    }


    private boolean canStartMirror() {
        // 判断是否有悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canDrawOverLays = SettingsCompat.canDrawOverlays(getApplicationContext());
            if (!canDrawOverLays) {
                // 无悬浮窗权限
                SettingsCompat.manageDrawOverlays(getApplicationContext());
                return false;
            }
        }

        // 判断是否需要申请声音权限
        boolean audioEnable = isAudioEnable();
        if (audioEnable) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                // 不同意，则去申请权限
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                return false;
            }
        }

        return true;
    }

    private void initDatas() {
        mBrowseAdapter = new BrowseAdapter(getApplicationContext());
        mRecyclerView.setAdapter(mBrowseAdapter);
        mBrowseAdapter.setOnItemClickListener(new BrowseAdapter.OnItemClickListener() {

            @Override
            public void onClick(int position, LelinkServiceInfo info) {
                info.setConnect(!info.isConnect());
                mBrowseAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onLongClick(int position, LelinkServiceInfo info) {
                // 长按
                mBrowseAdapter.removeData(position);
                return true;
            }

        });

        // 初始化browse RecyclerView
        // 设置Adapter
        if (ContextCompat.checkSelfPermission(CustomDeviceActivtity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED) {
            initLelinkHelper();
        } else {
            // 若没有授权，会弹出一个对话框（这个对话框是系统的，开发者不能自己定制），用户选择是否授权应用使用系统权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_MUST_PERMISSION);
        }

    }

    @Override
    protected void initLelinkHelper() {
        mLelinkHelper = MyApplication.getMyApplication().getLelinkHelper();
        mLelinkHelper.setUIUpdateListener(mUIUpdateListener);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_multi_mirror:
                startMultiMirror();
                break;
            case R.id.btn_append_multi_mirror:
                appendMultiMirror();
                break;
            case R.id.btn_remove_multi_mirror:
                removeMultiMirror();
                break;
            case R.id.btn_stop_multi_mirror:
                stopMultiMirror();
                break;
            case R.id.add_sub_devs:
                showAddSubDevDialog();
                break;
            case R.id.pause_multi_mirror:
                pauseMultiMirror();
                break;
            case R.id.resume_multi_mirror:
                resumeMultiMirror();
                break;

        }
    }


    private void pauseMultiMirror() {
        if (null != mLelinkHelper) {
            mLelinkHelper.pauseMultiMirror();
        }

    }

    private void resumeMultiMirror() {
        if (null != mLelinkHelper) {
            mLelinkHelper.resumeMultiMirror();
        }

    }

    public void showAddSubDevDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomDeviceActivtity.this);
        builder.setTitle("添加设备");
        View view = LayoutInflater.from(this).inflate(R.layout.layout_aad_devs_info, null, false);
        mSubEditIP = (EditText) view.findViewById(R.id.sub_ip_edit);
        mSubPortIp = (EditText) view.findViewById(R.id.sub_port_edit);
        builder.setView(view);
        // 设置图标
        builder.setNegativeButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(mSubEditIP.getText().toString()) || TextUtils.isEmpty(mSubPortIp.getText().toString())) {
                    ToastUtil.show(getApplicationContext(), "子设备ip和端口不能为空");
                    return;
                }
                LelinkServiceInfo serviceInfo = new LelinkServiceInfo();
                serviceInfo.setIp(mSubEditIP.getText().toString());
                serviceInfo.setPort(Integer.valueOf(mSubPortIp.getText().toString()));
                mBrowseAdapter.updateData(serviceInfo);
            }
        });
        builder.create();
        builder.show();
    }


    @Override
    protected void startMultiMirror() {
        if (!canStartMirror()) {
            return;
        }
        if (TextUtils.isEmpty(mIpEdit.getText().toString()) || TextUtils.isEmpty(mPortEdit.getText().toString())) {
            ToastUtil.show(getApplicationContext(), "主设备ip和端口不能为空");
            return;
        }
        LelinkServiceInfo masterInfo = new LelinkServiceInfo();
        masterInfo.setIp(mIpEdit.getText().toString());
        masterInfo.setPort(Integer.valueOf(mPortEdit.getText().toString()));
        LelinkMultiServiceInfo multiServiceInfo = new LelinkMultiServiceInfo();
        multiServiceInfo.setMasterLelinkServiceInfo(masterInfo);
        multiServiceInfo.setRemoteLelinkServiceInfos(mBrowseAdapter.getSelectInfos());
        mLelinkMultiServiceInfo = multiServiceInfo;
        mLelinkHelper.connect(mLelinkMultiServiceInfo);
    }

    public void startMirrorByNfc(String info) {
        Log.i(TAG, "startMirrorByNfc info:" + info);
        JSONObject jobj = null;
        String ip = null;
        int port = 0;
        try {
            jobj = new JSONObject(info);
            ip = jobj.getString("ip");
            port = jobj.getInt("port");
        } catch (Exception e) {
            Log.w(TAG, "startMirrorByNfc info error " + e);
        }
        Log.i(TAG, "startMirrorByNfc ip:" + ip + ",port:" + port);
        LelinkServiceInfo masterInfo = new LelinkServiceInfo();
        masterInfo.setIp(ip);
        masterInfo.setPort(port);
        LelinkMultiServiceInfo multiServiceInfo = new LelinkMultiServiceInfo();
        multiServiceInfo.setMasterLelinkServiceInfo(masterInfo);
        multiServiceInfo.setRemoteLelinkServiceInfos(mBrowseAdapter.getSelectInfos());
        mLelinkMultiServiceInfo = multiServiceInfo;
        mLelinkHelper.connect(mLelinkMultiServiceInfo);
    }

    private void appendMultiMirror() {
        if (!canStartMirror()) {
            return;
        }
        LelinkServiceInfo[] selectInfos = mBrowseAdapter.getSelectInfos();
        mLelinkHelper.appendMultiMirror(selectInfos);
    }

    private void removeMultiMirror() {
        if (!canStartMirror()) {
            return;
        }
        LelinkServiceInfo[] selectInfos = mBrowseAdapter.getSelectInfos();
        mLelinkHelper.removeMultiMirror(selectInfos);
    }

    private void stopMultiMirror() {
        if (null != mLelinkHelper) {
            mLelinkHelper.stopMirror();
        }
        mLelinkMultiServiceInfo = null;
    }

    private int getResolutionLevelSetting() {
        int resolutionLevel = 0;
        int resolutionCheckId = mRgResolution.getCheckedRadioButtonId();
        switch (resolutionCheckId) {
            case R.id.rb_resolution_height:
                resolutionLevel = AllCast.RESOLUTION_HEIGHT;
                break;
            case R.id.rb_resolution_middle:
                resolutionLevel = AllCast.RESOLUTION_MIDDLE;
                break;
            case R.id.rb_resolution_low:
                resolutionLevel = AllCast.RESOLUTION_AUTO;
                break;
        }
        return resolutionLevel;
    }

    private int getBitRateLevelSetting() {
        int bitrateLevel = 0;
        int bitrateCheckId = mRgBitRate.getCheckedRadioButtonId();
        switch (bitrateCheckId) {
            case R.id.rb_bitrate_height:
                bitrateLevel = AllCast.BITRATE_HEIGHT;
                break;
            case R.id.rb_bitrate_middle:
                bitrateLevel = AllCast.BITRATE_MIDDLE;
                break;
            case R.id.rb_bitrate_low:
                bitrateLevel = AllCast.BITRATE_LOW;
                break;
        }
        return bitrateLevel;
    }

    private boolean isAudioEnable() {
        int audioCheckId = mRgMirrorAudio.getCheckedRadioButtonId();
        boolean audioEnable = false;
        switch (audioCheckId) {
            case R.id.rb_mirror_audio_on:
                audioEnable = true;
                break;
            case R.id.rb_mirror_audio_off:
                audioEnable = false;
                break;
        }
        return audioEnable;
    }

    private void startMirror() {
        if (null == mLelinkHelper) {
            Logger.test(TAG, "start mirror click error not_init");
            ToastUtil.show(getApplicationContext(), "未初始化");
            return;
        }
        if (mLelinkMultiServiceInfo == null) {
            ToastUtil.show(getApplicationContext(), "开始镜像错误");
            return;
        }

        // 分辨率
        int resolutionLevel = getResolutionLevelSetting();

        // 比特率
        int bitrateLevel = getBitRateLevelSetting();

        // 音频
        boolean audioEnable = isAudioEnable();

        // 开启镜像声音需要权限
        if (audioEnable) {
            if (ContextCompat.checkSelfPermission(CustomDeviceActivtity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED) {
                // 同意权限
                Logger.test(TAG, "star mirror name:PinCode"
                        + " resolutionLevel:" + resolutionLevel
                        + " bitrateLevel:" + bitrateLevel
                        + " audioEnable:" + audioEnable);
                mLelinkHelper.startMirror(CustomDeviceActivtity.this, mLelinkMultiServiceInfo, resolutionLevel, bitrateLevel,
                        audioEnable);
            } else {
                // 不同意，则去申请权限
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        } else {
            mLelinkHelper.startMirror(CustomDeviceActivtity.this, mLelinkMultiServiceInfo, resolutionLevel, bitrateLevel,
                    audioEnable);
        }
    }

    private void stopMirror() {
        if (null == mLelinkHelper) {
            Logger.test(TAG, "start mirror click error not_init");
            ToastUtil.show(getApplicationContext(), "未初始化");
            return;
        }
        Logger.test(TAG, "stopMirror click");
        if (null != mLelinkHelper) {
            mLelinkHelper.stopMirror();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(TAG, "onDestroy");
        stopMirror();
    }


    private IUIUpdateListener mUIUpdateListener = new IUIUpdateListener() {

        @Override
        public void onUpdate(int what, MessageDeatail deatail) {
            Logger.d(TAG, "onUpdateText what:" + what + "text:" + deatail.text + "\n\n");
            switch (what) {
                case IUIUpdateListener.STATE_SEARCH_SUCCESS:
                    Logger.test(TAG, "PinCode parse success:" + deatail.text);
                    Logger.d(TAG, "ToastUtil " + deatail.text);
                    ToastUtil.show(getApplicationContext(), deatail.text);
                    mLelinkMultiServiceInfo = (LelinkServiceInfo) deatail.obj;
                    break;
                case IUIUpdateListener.STATE_SEARCH_ERROR:
                    Logger.test(TAG, "PinCode parse error:" + deatail.text);
                    Logger.d(TAG, "ToastUtil " + deatail.text);
                    ToastUtil.show(getApplicationContext(), deatail.text);
                    break;
                case IUIUpdateListener.STATE_CONNECT_SUCCESS:
                    Logger.test(TAG, "connect success:" + deatail.text);
                    Logger.d(TAG, "ToastUtil " + deatail.text);
                    ToastUtil.show(getApplicationContext(), deatail.text);
                    startMirror();
                    break;
                case IUIUpdateListener.STATE_DISCONNECT:
                    Logger.test(TAG, "disConnect success:" + deatail.text);
                    Logger.d(TAG, "ToastUtil " + deatail.text);
                    ToastUtil.show(getApplicationContext(), deatail.text);
                    break;
                case IUIUpdateListener.STATE_CONNECT_FAILURE:
                    Logger.test(TAG, "connect failure:" + deatail.text);
                    Logger.d(TAG, "ToastUtil " + deatail.text);
                    ToastUtil.show(getApplicationContext(), deatail.text);
                    break;
                case IUIUpdateListener.STATE_PLAY:
                    Logger.test(TAG, "callback play");
                    Logger.d(TAG, "ToastUtil 开始播放");
                    ToastUtil.show(getApplicationContext(), "开始播放");
                    break;
                case IUIUpdateListener.STATE_STOP:
                    Logger.test(TAG, "callback stop");
                    Logger.d(TAG, "ToastUtil 播放结束");
                    ToastUtil.show(getApplicationContext(), "播放结束");
                    break;
                case IUIUpdateListener.STATE_PLAY_ERROR:
                    Logger.test(TAG, "callback error:" + deatail.text);
                    ToastUtil.show(getApplicationContext(), "播放错误：" + deatail.text);
                    break;
                case IUIUpdateListener.STATE_CONFERENCE_MULTI_MIRROR:
                    Logger.test(TAG, "callback conference multi-mirror state:" + deatail.text);
                    ToastUtil.show(getApplicationContext(), "会议室版本一投多状态：" + deatail.text);
                    break;
            }
        }

    };

}
