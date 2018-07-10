package com.aliyun.oss.ossdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.AlgoSdk.NskAlgoType;
import com.neurosky.connection.TgStreamReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Created by moriarty on 18-1-11.
 */


public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivityTag";

    private BluetoothAdapter mBluetoothAdapter;


    //个人信息

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private Toolbar usertoolbar;

    private DrawerLayout mDrawerLayout;
    private Button mSaveButton;
    private TextView mTextViewNumber;
    private TextView mTextViewNickname;
    private TextView mTextViewAge;
    private TextView mTextViewWords;

    private userInfo userinfo = new userInfo();

    private String userId;
    private String nickName;
    private String ageValue;
    private String slogan;
    private String pictureurl;
    private String changeNickname;
    private String changeSolgan;
    private String changeAge;

    //开始禅修
    private Button headsetButton;

    //长久消息
    private TextView mTextViewLongMesg;
    private String longMesg;

    //即时消息
    private TextView mTextViewShortMesg;
    private String[] shortMesg;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView(){
        //滑动菜单

        usertoolbar = (Toolbar)findViewById(R.id.toolbar) ;
        setSupportActionBar(usertoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
//右上打卡菜单按钮

//左滑个人信息
        mTextViewNumber = (TextView) findViewById(R.id.user_number_content);
        mTextViewNickname = (TextView)findViewById(R.id.nick_name_content);
        mTextViewAge = (TextView)findViewById(R.id.user_age_content);
        mTextViewWords = (TextView)findViewById(R.id.user_words_content);

        //若已保存账号则直接获取
        pref = getSharedPreferences("User", Context.MODE_PRIVATE);
        boolean isRemember = pref.getBoolean("remember_password", false);
        if(isRemember){
            userId = pref.getString("name","");
            ageValue = pref.getString("age", "");
            nickName = pref.getString("nickname", "");
            slogan = pref.getString("slogan", "");

            mTextViewNumber.setText(userId);
            mTextViewNickname.setText(nickName);
            mTextViewAge.setText(ageValue);
            mTextViewWords.setText(slogan);
        }
        //保存个人信息
        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//?上传个人信息到数据库.  先让用户填写账号，点击保存时，获取填写的账号信息和其他信息，上传至服务器，确认账号是否已注册
//若返回真，则将类userInfo中的userId设置为现有账号。
                //获取用户更改后的信息
                changeNickname = mTextViewNickname.getText().toString();
                changeAge = mTextViewAge.getText().toString();
                changeSolgan = mTextViewWords.getText().toString();
                //获取用户账号
                userId = mTextViewNumber.getText().toString();


//                if(userId != "201708XM0628"){
//                    showToast("请填写你的用户账号",Toast.LENGTH_LONG);
//                }else{
                    //上传用户信息，并获取回传值success
                int callBack = 0;
                try{
                    UpdateThread updateThread = new UpdateThread("user",userId, changeNickname, changeAge, changeSolgan,"");
                    updateThread.start();
                    updateThread.join();
                    callBack = Integer.valueOf(updateThread.getCallBack());
                }catch (InterruptedException ie){
                    Log.i("Interrupted",ie.getMessage());
                }

                if (callBack == 1){
                    userinfo.setUserId(userId);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("name", userId);
                    editor.putString("nickname", changeNickname);
                    editor.putString("age", changeAge);
                    editor.putString("slogan",changeSolgan);
                    editor.putBoolean("remember_password", true);
                    editor.commit();
                    //关闭滑动菜单
                    mDrawerLayout.closeDrawers();
                }else{
                    showToast("用户账号未注册，请重新填写",Toast.LENGTH_LONG);
                }




            }
        });


//佩戴头环，连接蓝牙
        headsetButton = (Button) findViewById(R.id.headsetButton);
//
        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                headsetButton.setEnabled(false);
                showToast("请连接您的蓝牙，然后重新运行 !", Toast.LENGTH_LONG);
            }else {
                headsetButton.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }

        headsetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent i = new Intent(MainActivity.this, EEGDataActivity.class);
                startActivity(i);
            }
        });

        //获取长短时信息
        try{
            List<userInfo> mItems = new mesgFetchItemsTask().execute().get();
            userInfo userinfo = mItems.get(0);
            //接受永久消息

            mTextViewLongMesg = (TextView) findViewById(R.id.longMesg);

            longMesg = userinfo.getLongMesg();
            mTextViewLongMesg.setText(longMesg);

//接受即时消息
            mTextViewShortMesg = (TextView) findViewById(R.id.shortMesg);
            shortMesg = userinfo.getShortMesg();
            int len = shortMesg.length;
            String temp = "";
            for(int i=0;i<len;i++){
                temp = temp + shortMesg[i];
                temp = temp + "\n";
            }
            mTextViewShortMesg.setText(temp);
        }catch (InterruptedException ie){
            mTextViewLongMesg.setText(" ");
            mTextViewShortMesg.setText(" ");
        }catch (ExecutionException ee){
            mTextViewLongMesg.setText(" ");
            mTextViewShortMesg.setText(" ");
        }



    }//initView


//采用AsyncTask实现后台线程访问web服务器
    private class userFetchItemsTask extends AsyncTask<Void, Void, List<userInfo> >{
        @Override
        protected List<userInfo> doInBackground(Void... params){
            return new InfoFetchServer().fecthItems("getuserinfo/",userId);

        }

//        @Override
//        protected void onPostExecute(List<userInfo> items){
//            mItems = items;
//        }
    }
    private class mesgFetchItemsTask extends AsyncTask<Void, Void, List<userInfo> >{
        @Override
        protected List<userInfo>  doInBackground(Void... params){
            return new InfoFetchServer().fecthItems("getmails/",userId);
        }
    }




//显示短时弹出提示信息
    public void showToast(final String msg, final int timeStyle) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case  R.id.calendar_item:
//打开打卡日历
                Intent intent=new  Intent(MainActivity.this,SignDateDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(intent);
                break;
            case  android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.LEFT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
