package com.aliyun.oss.ossdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;

import com.neurosky.AlgoSdk.NskAlgoSdk;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;


import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;

import static java.lang.Character.getType;

public class EEGDataActivity extends Activity {

    private static final String TAG = "EEGDataActivityTag";

    //计时器
    private long baseTimer = 0;
    private TextView timeView;
    private Timer mTimer;
    private TimerTask mTimerTask;
    //头还连接上之后启动计时
    private boolean timeStart = false;

    //训练日期
    trainDate date = new trainDate();
    private String train_date = date.fileDate();

    //获取用户ID
    private SharedPreferences pref;
    private String userId;
    private int time=0;
    //记录注意力值和放松度值传到训练报告界面显示出来
    private ArrayList medData = new ArrayList();
    private ArrayList attData = new ArrayList();
    //保存所有数据，上传至oss
    private ArrayList recordListValue;


    // Brainlink SDK handles
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;

    // UI components
    private Button endButton;
    private Button connectButton;

    //oss SDK
    private static final String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private static final String bucket = "zen-android";
    private static final String accessKeyId = "***********";
    private static final String accessKeySecret = "*************";
    //负责所有的界面更新
    private UIDisplayer UIDisplayer;
    //OSS的上传下载
    private OssService ossService;


    //初始化一个OssService用来上传下载
    public OssService initOSS(String endpoint, UIDisplayer displayer) {
        //如果希望直接使用accessKey来访问的时候，可以直接使用OSSPlainTextAKSKCredentialProvider来鉴权。
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);
        return new OssService(oss, bucket, displayer);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eegdata);


        //个人信息
        pref = getSharedPreferences("User", Context.MODE_PRIVATE);
        boolean isRemember = pref.getBoolean("remember_password",false);

        if(isRemember) {
            userId = pref.getString("name", "");
        }
        else{
            showToast("请填写用户账号！", Toast.LENGTH_LONG);
        }
        //设置蓝牙连接

        EEGinit();

//计时器
        timeView = (TextView)this.findViewById(R.id.timeView);
        final Handler startTimehandler = new Handler() {
            public void handleMessage(Message msg) {
                if (null != timeView) {
                    timeView.setText((String) msg.obj);
                }
            }
        };

        mTimer = new Timer("禅修计时器");
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (baseTimer !=0 ){
                    time = (int) ((SystemClock.elapsedRealtime() - EEGDataActivity.this.baseTimer) / 1000);
                }else {
                    time = 0;
                }
                timeFormat(time, startTimehandler);
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 1000, 1000L);



        //oss上传 show
        UIDisplayer = new UIDisplayer(this);
        ossService = initOSS(endpoint, UIDisplayer);
        //设置上传的callback地址，目前暂时只支持putObject的回调
//        ossService.setCallbackAddress(callbackAddress);

        recordListValue = new ArrayList();

        //训练结束
        endButton = (Button) this.findViewById(R.id.endButton);
        endButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new Thread(){
                    @Override
                    public void run(){
                        try{
                            int n_row = recordListValue.size()/522;
                            for(int i=n_row*522; i<n_row;i++){
                                recordListValue.remove(i);
                            }
                            Log.d(TAG,"dsjdaosidsa"+(int)recordListValue.get(0));
//                saveData(recordValue);
                            byte[] convertTobyte = new byte[recordListValue.size()*4];
                            if (recordListValue.size() > 0) {
                                for (int i = 0; i < recordListValue.size()*4-3; i+=4) {
                                    byte[] int_to_byte = intToByteArray1((int)recordListValue.get(i/4));
                                    convertTobyte[i] = int_to_byte[0];
                                    convertTobyte[i+1] = int_to_byte[1];
                                    convertTobyte[i+2] = int_to_byte[2];
                                    convertTobyte[i+3] = int_to_byte[3];
                                }
                            }
// 上传后的文件名   data_object
                            String data_object = userId + train_date+time;
                            ossService.asyncPutData(data_object, convertTobyte);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();


            //转向训练报告

            stopTimer();
            ArrayList<Object> eeginfo = new ArrayList<Object>();
            eeginfo.add(userId);
            eeginfo.add(time);
            eeginfo.add(userId+train_date);
            eeginfo.add(medData);
            eeginfo.add(attData);

            onDestroy();
            Intent i = TrainReportActivity.newIntent(EEGDataActivity.this, eeginfo);
            startActivity(i);


            }

        });

    }

    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        count = 0;
    }

    private void timeFormat(int time, Handler handler){
        String hh = new DecimalFormat("00").format(time/3600);
        String mm = new DecimalFormat("00").format(time%3600/60);
        String ss = new DecimalFormat("00").format(time%60);
        String timeFormat = new String(hh+":"+mm+":"+ss);
        Message msg = new Message();
        msg.obj = timeFormat;
        handler.sendMessage(msg);
    }

    private void EEGinit(){
//        try {
//            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
//            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//                showToast("请连接您的蓝牙，然后重新运行 !", Toast.LENGTH_LONG);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i(TAG, "error:" + e.getMessage());
//            return;
//        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);
        if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

            // Prepare for connecting
            tgStreamReader.stop();
            tgStreamReader.close();
        }

        tgStreamReader.connect();

    }
    private byte[] intToByteArray1(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)(((i >> 24) & 0xFF));
        result[1] = (byte)(((i >> 16) & 0xFF));
        result[2] = (byte)(((i >> 8) & 0xFF));
        result[3] = (byte)(((i) & 0xFF));
        return result;
    }

    @Override
    protected void onDestroy() {
        //(6) use close() to release resource
        if(tgStreamReader != null){
            tgStreamReader.close();
            tgStreamReader = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    //获取脑电数据通信配置

    private TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTING:
                    // Do something when connecting
                    showToast("正在连接,你有二十秒准备时间",Toast.LENGTH_LONG);
                    break;
                case ConnectionStates.STATE_CONNECTED:
                    // Do something when connected
                    tgStreamReader.start();
                    // 连接上头环之后，计时开始
                    EEGDataActivity.this.baseTimer = SystemClock.elapsedRealtime();
                    showToast("头环已连接", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_WORKING:
                    // Do something when working

                    //(9) demo of recording raw data , stop() will call stopRecordRawData,
                    //or you can add a button to control it.
                    //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
//                    tgStreamReader.startRecordRawData();

                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    // Do something when getting data timeout

                    //(9) demo of recording raw data, exception handling
//                    tgStreamReader.stopRecordRawData();

                    showToast("获取数据超时！", Toast.LENGTH_SHORT);
                    if (tgStreamReader != null && tgStreamReader.isBTConnected()) {
                        tgStreamReader.stop();
                        tgStreamReader.close();
                    }


                    break;
                case ConnectionStates.STATE_STOPPED:
                    // Do something when stopped
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.

                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    // Do something when disconnected
                    break;
                case ConnectionStates.STATE_ERROR:
                    // Do something when you get error message
                    break;
                case ConnectionStates.STATE_FAILED:
                    // Do something when you get failed message
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again
                    break;
                default:
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);
        }

        @Override
        public void onRecordFail(int flag) {
            // You can handle the record error message here
            Log.e(TAG,"onRecordFail: " +flag);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // You can handle the bad packets here.
        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // You can handle the received data here
            // You can feed the raw data to algo sdk here if necessary.
            //Log.i(TAG,"onDataReceived");
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
        }
    };

    private boolean isPressing = false;
    private static final int MSG_UPDATE_STATE = 1002;
    int count = 0;

    private Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            // (8) demo of MindDataType
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    if (count>0){
                        recordListValue.add(msg.arg1);
                    }

                    break;
                case MindDataType.CODE_MEDITATION:
                    if(msg.arg1 != 0){
                        medData.add(msg.arg1);
                    }

                    break;
                case MindDataType.CODE_ATTENTION:
                    if (count>0){
                        if(msg.arg1!=0){
                            attData.add(msg.arg1);
                        }
                        recordListValue.add(msg.arg1);
                        Log.d(TAG, "CODE_ATTENTION " + msg.arg1 + " shujuleixing " + getType(msg.arg1));
                    }
                    count+=1;
                    break;
                case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower)msg.obj;

                    if(power.isValidate() && count>0){
                        recordListValue.add(power.lowAlpha);
                        recordListValue.add(power.highAlpha);
                        recordListValue.add(power.lowBeta);
                        recordListValue.add(power.highBeta);
                        recordListValue.add(power.lowGamma);
                        recordListValue.add(power.middleGamma);
                        recordListValue.add(power.theta);
                        recordListValue.add(power.delta);
                        Log.d(TAG, "CODE_ALPHA " + power.theta+"CODE_GAMA"+power.lowGamma);
                    }
                    break;
                case MindDataType.CODE_POOR_SIGNAL://
                    if (count>0){
                        recordListValue.add(msg.arg1);
                        Log.d(TAG, "CODE_poorsignal"+msg.arg1);
                    }

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void showToast(final String msg, final int timeStyle) {
        EEGDataActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }
}




