package com.aliyun.oss.ossdemo;

import android.app.Activity;
import android.app.AlertDialog;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by OSS on 2017/12/26.
 * 完成上传，进度条更新等操作。
 */
public class UIDisplayer {

    private ProgressBar bar;
    private Activity activity;

    private Handler handler;



    private static final int UPLOAD_OK = 3;
    private static final int UPLOAD_FAIL = 4;
    private static final int UPDATE_PROGRESS = 5;
    private static final int DISPLAY_INFO = 7;
    private static final int SETTING_OK = 88;


    /* 必须在UI线程中初始化handler */
    public UIDisplayer(Activity activity){
//        this.bar = bar;
//        this.infoView = infoView;
        this.activity = activity;

        handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage (Message inputMessage){

                String info;
            switch (inputMessage.what) {


                case UPLOAD_OK:
                    new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("上传成功").setMessage("upload to OSS OK!").show();
                    break;
                case UPLOAD_FAIL:
                    info = (String) inputMessage.obj;
                    new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("上传失败").setMessage(info).show();
                    break;
                case SETTING_OK:
                    new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("设置成功").setMessage("设置域名信息成功,现在<选择图片>, 然后上传图片").show();
                    break;
                case UPDATE_PROGRESS:
//                    bar.setProgress(inputMessage.arg1);
                    //Log.d("UpdateProgress", String.valueOf(inputMessage.arg1));
                    break;

                default:
                    break;
            }

        }
        };

    }

    public void settingOK() {
        Message mes = handler.obtainMessage(SETTING_OK);
        mes.sendToTarget();
    }

    //上传成功
    public void uploadComplete() {
        Message mes = handler.obtainMessage(UPLOAD_OK);
        mes.sendToTarget();
    }

    //上传失败，显示对应的失败信息
    public void uploadFail(String info) {
        Message mes = handler.obtainMessage(UPLOAD_FAIL, info);
        mes.sendToTarget();
    }

    //更新进度，取值范围为[0,100]
    public void updateProgress(int progress) {
        //Log.d("UpdateProgress", String.valueOf(progress));
        if (progress > 100) {
            progress = 100;
        }
        else if (progress < 0) {
            progress = 0;
        }

        Message mes = handler.obtainMessage(UPDATE_PROGRESS, progress);
        mes.arg1 = progress;
        mes.sendToTarget();
    }

    //在主界面输出文字信息
    public void displayInfo(String info) {
        Message mes = handler.obtainMessage(DISPLAY_INFO, info);
        mes.sendToTarget();
    }
}
