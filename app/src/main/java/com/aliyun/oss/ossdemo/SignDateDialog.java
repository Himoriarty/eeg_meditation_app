package com.aliyun.oss.ossdemo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class SignDateDialog extends Activity {
    private List<CheckBean> checkBeanList;
    private CheckAdapter mAdapter;
    private GridView mGridview;

    //打卡窗口标题
    private TextView signTitle;

    private SharedPreferences pref;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
//        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setAttributes(params);
        setContentView(R.layout.dialog_sign);

        initDate();
    }

    private void initDate() {
        pref = getSharedPreferences("User", Context.MODE_PRIVATE);
        boolean isRemember = pref.getBoolean("remember_password",false);

        if(isRemember) {
            userId = pref.getString("name", "");
        }
        else{
            showToast("请填写用户账号！", Toast.LENGTH_LONG);
        }

        try{
            List<userInfo> mItems = new signFetchItemsTask().execute().get();
            userInfo userinfo = mItems.get(0);
            String alltime = userinfo.getAlltime();
            signTitle = (TextView)findViewById(R.id.alltime);
            signTitle.setText("过去28天打坐总时间: " + alltime + " 分钟");

            int day = 28; // 获取需要标记的天数
            Integer[] daysArray = userinfo.getDays();
            int len = daysArray.length;
            int maxday = 0;
            if(len > 0){
                maxday = Collections.max(Arrays.asList(daysArray));
            }

            checkBeanList = new ArrayList<CheckBean>();

            for (int i = 1; i < day+1; i++) {
                CheckBean checkBean = new CheckBean();
                if(i <= maxday){
                    //如果第i天有签到就为1
                    int flag = 0;
                    for (int j=0; j < len; j++){
                        if (i == daysArray[j]){
                            checkBean.day = i;
                            checkBean.check_status = CheckBean.CHECKED;
                            flag = 1;
                        }
                    }
                    if (flag == 0){
                        checkBean.day = i;
                        checkBean.check_status = CheckBean.CHECK_NO;
                    }
                }
                else {
                    checkBean.day = i;
                    checkBean.check_status = CheckBean.CHECK_WAIT;
                }

                checkBeanList.add(checkBean);
            }

        }catch (InterruptedException ie){

        }catch (ExecutionException ee){

        }

        mAdapter = new CheckAdapter(SignDateDialog.this);
        mAdapter.setListDate(checkBeanList);

        mGridview = (GridView) findViewById(R.id.gv_sign_date);
        mGridview.setAdapter(mAdapter);
    }

    private class signFetchItemsTask extends AsyncTask<Void, Void, List<userInfo>> {
        @Override
        protected List<userInfo> doInBackground(Void... params){
            return new InfoFetchServer().fecthItems("get4week/",userId);
        }
    }

    //显示短时弹出提示信息
    public void showToast(final String msg, final int timeStyle) {
        SignDateDialog.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }
}