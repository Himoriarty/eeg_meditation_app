package com.aliyun.oss.ossdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * by moos on 2018/04/20
 */
public class CommunityActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private SharedPreferences pref;
    private String userId;

    private android.support.v7.widget.Toolbar toolbar;
    private CommentExpandableListView expandableListView;
    private CommentExpandAdapter adapter;
    private CommentBean commentBean;
    private List<CommentDetailBean> commentsList;
    private String testJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        pref = getSharedPreferences("User", Context.MODE_PRIVATE);
        boolean isRemember = pref.getBoolean("remember_password",false);

        if(isRemember) {
            userId = pref.getString("name","");
        }
        else{
            showToast("请填写用户账号！", Toast.LENGTH_LONG);
        }

        try{
            List<userInfo> mItems = new postFetchItemsTask().execute().get();
            userInfo userinfo = mItems.get(0);
            //接受永久消息

            testJson = userinfo.getPostString();
        }catch (InterruptedException ie){

        }catch (ExecutionException ee){

        }

        Log.i("callback postString", testJson);
        initView();
    }

    private void initView() {

        //显示用户的心得
        String words = pref.getString("shareword","");
        TextView mTextViewShraeWords = (TextView) findViewById(R.id.detail_page_story);
        mTextViewShraeWords.setText(words);


        toolbar = (Toolbar) findViewById(R.id.toolbar_c);
        expandableListView = (CommentExpandableListView)findViewById(R.id.detail_page_lv_comment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("心得");
        commentsList = generateTestData();
        initExpandableListView(commentsList);
    }

    /**
     * 初始化评论和回复列表
     */
    private void initExpandableListView(final List<CommentDetailBean> commentList){
        expandableListView.setGroupIndicator(null);
        //默认展开所有回复
        adapter = new CommentExpandAdapter(this, commentList);
        expandableListView.setAdapter(adapter);
        for(int i = 0; i<commentList.size(); i++){
            expandableListView.expandGroup(i);
        }
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                boolean isExpanded = expandableListView.isGroupExpanded(groupPosition);
                Log.e(TAG, "onGroupClick: 当前的评论id>>>"+commentList.get(groupPosition).getId());
                return true;
            }
        });


        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                //toast("展开第"+groupPosition+"个分组");

            }
        });

    }

    /**
     * func:生成测试数据
     * @return 评论数据
     */
    private List<CommentDetailBean> generateTestData(){
        Gson gson = new Gson();
        commentBean = gson.fromJson(testJson, CommentBean.class);
        List<CommentDetailBean> commentList = commentBean.getData().getList();
        return commentList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent i = new Intent(CommunityActivity.this, MainActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class postFetchItemsTask extends AsyncTask<Void, Void, List<userInfo> > {
        @Override
        protected List<userInfo>  doInBackground(Void... params){
            return new InfoFetchServer().fecthItems("getposts/",userId);
        }
    }

    //显示短时弹出提示信息
    public void showToast(final String msg, final int timeStyle) {
        CommunityActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

}
