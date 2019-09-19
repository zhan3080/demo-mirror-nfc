package com.hpplay.sdk.sink.cardemulation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hpplay.sdk.sink.cardemulation.common.Preference;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button nfcButton;
    private EditText ipEdit;
    private EditText portEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipEdit = findViewById(R.id.ip);
        portEdit = findViewById(R.id.port);
        nfcButton = findViewById(R.id.creatBtn);
        nfcButton.setOnClickListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        String mServerInfoStr = Preference.getInstance().getServerInfo();
        JSONObject jobj = null;
        String ip = null;
        int port = 0;
        try {
            jobj = new JSONObject(mServerInfoStr);
            ip = jobj.getString("ip");
            port = jobj.getInt("port");
        } catch (Exception e) {
            Log.w(TAG, "initData mServerInfoStr error " + e);
        }
        Log.i(TAG, "initData ip:" + ip + ",port:" + port);
        if (!TextUtils.isEmpty(ip) && ipEdit != null) {
            ipEdit.setText(ip);
        }
        if (port != 0 && portEdit != null) {
            portEdit.setText(String.valueOf(port));
        }
    }

    private void createNfc() {
        String ip = null;
        int port = 0;
        ip = ipEdit.getText().toString();
        try {
            String portStr = portEdit.getText().toString();
            port = Integer.parseInt(portStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "createNfc ip:" + ip);
        Log.i(TAG, "createNfc port:" + port);
        if (TextUtils.isEmpty(ip)) {
            Log.w(TAG, "ip can not be null");
            return;
        }
        if (port == 0) {
            port = 52266;
        }
        try {
            JSONObject jobj = new JSONObject();
            jobj.put("ip", ip);
            jobj.put("port", port);
            Preference.getInstance().setServerInfo(jobj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.creatBtn:
                    createNfc();
                    break;
                default:
                    break;
            }
        }
    };
}
