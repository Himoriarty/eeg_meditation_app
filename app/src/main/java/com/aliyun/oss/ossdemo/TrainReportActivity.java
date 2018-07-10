package com.aliyun.oss.ossdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by moriarty on 18-1-11.
 */

public class TrainReportActivity extends Activity{
    private final static String TAG = "TrainReportActivity";

    private final static String ACCEPT = "EEGDataActivityTag";

    private List<Object> eegInfo;

    private TextView mTextViewUsernickName;
    private String userNickname ;
    private SharedPreferences pref;
    private int trainlength;
    private String userId;

    private TextView mTextDate;
    trainDate traindate = new trainDate();
    private String train_date = traindate.stringDate();

    //显示本次训练时长
    private TextView mTextTimeLength;
    //显示训练数据结果
    //MPandroid图表
    private LineChart mChart;
    private int[] mColors = new int[]{Color.RED, Color.GREEN,
            Color.BLUE, Color.BLACK,Color.CYAN};
    private String[] label = new String[]{"med","att"};

    private RadioButton mZhuanzhuButton;
    private RadioButton mZhengnianButton;
    private RadioButton mZuowangButton;
    private Boolean zhuanzhu = false;
    private Boolean zhengnian = false;
    private Boolean zuowang = false;
    private int type = 4;

    private EditText mWordsTextView;
    private String shareWords;

    private Button mEndButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_report);

        eegInfo = (List<Object>) getIntent().getSerializableExtra(ACCEPT);

        pref = getSharedPreferences("User", Context.MODE_PRIVATE);
        boolean isRemember = pref.getBoolean("remember_password",false);

        if(isRemember) {
            userNickname = pref.getString("nickname", "");
            userId = pref.getString("name","");
        }
        else{
            showToast("请填写用户账号！", Toast.LENGTH_LONG);
        }

        initView();
    }

    private void initView(){
//获取用户昵称
        mTextViewUsernickName = (TextView)findViewById(R.id.train_userId);
        mTextViewUsernickName.setText(userNickname);
//获取训练日期
        mTextDate = (TextView)findViewById(R.id.train_date);
        mTextDate.setText(train_date);

        //获取本次训练时长
        mTextTimeLength = (TextView)findViewById(R.id.train_timelength);
        trainlength = (int)eegInfo.get(1);
        mTextTimeLength.setText(String.valueOf(trainlength));

//获取训练数据和图标
        mChart = (LineChart) findViewById(R.id.line_chart);
        setLineChart(mChart);
        loadLineChartData(mChart);


//获取冥想方式
        mZhuanzhuButton = (RadioButton)findViewById(R.id.focus);
        mZhengnianButton = (RadioButton)findViewById(R.id.zhengnian);
        mZuowangButton = (RadioButton)findViewById(R.id.zuowang);

        if(mZhuanzhuButton.isChecked()){
            type = 1;
            zhuanzhu = true;
        }else if(mZhengnianButton.isChecked()){
            type = 2;
            zhengnian = true;
        }else if(mZuowangButton.isChecked()){
            type = 3;
            zuowang = true;
        }
//获取用户训练感言
        mWordsTextView = (EditText) findViewById(R.id.share_words_view);



//结束训练
        mEndButton = (Button)findViewById(R.id.end_train);
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eegurl = (String) eegInfo.get(2);
                //该类的构造函数变量名称与这里不符，只是借用，减少代码量。
                //？上传训练结果到数据库

                //分享心得
                shareWords = mWordsTextView.getText().toString();
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("shareword", shareWords);
                editor.commit();

                //上传用户所有数据
                UpdateThread updateThread = new UpdateThread("train",
                        userId,String.valueOf(type),shareWords,
                        String.valueOf(trainlength/60),eegurl);
                updateThread.start();


                Intent i = new Intent(TrainReportActivity.this, CommunityActivity.class);
                startActivity(i);
            }
        });
    }

    public static Intent newIntent(Context packageContext, List<Object> eeginfo){
        Intent i = new Intent(packageContext, TrainReportActivity.class);
        i.putExtra(ACCEPT,(Serializable)eeginfo);
        return i;
    }

    public void showToast(final String msg, final int timeStyle) {
        TrainReportActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    //MP 图表
    private void loadLineChartData(final LineChart chart){
        //所有数据的list
        ArrayList<LineDataSet> allLinesList = new ArrayList<>();

        int len = 0;
        for (int j=0; j<2;j++){
            ArrayList<Entry> signalList = new ArrayList<>();
            ArrayList signalData = (ArrayList)eegInfo.get(j+3);
            if(signalData.size() > len){
                len = signalData.size();
            }
            for(int i=0; i<signalData.size(); i++){
                signalList.add(new Entry((int)signalData.get(i),i));
            }
            LineDataSet set = new LineDataSet(signalList, label[j]);
            set.setColor(mColors[j]);
            set.setCircleColor(mColors[j]);
            set.setValueTextSize(12f);
            set.setValueTextColor(mColors[j]);
            set.setLineWidth(2f);
            set.setCircleSize(3f);
            set.setFillAlpha(128);
            set.setFillColor(mColors[j]);
            set.setHighLightColor(mColors[j]);
            allLinesList.add(set);
        }
        //LineData表示一个LineChart的所有数据(即一个LineChart中所有折线的数据)
        LineData mChartData = new LineData(getXAxisShowLable(len), allLinesList);
        // set data
        chart.setData(mChartData);

    }


    private void setLineChart(LineChart chart) {
        chart.setDrawGridBackground(false);
        chart.setDescription("注意力和放松值");
        chart.setNoDataText("打坐状态检测");
        chart.setDescriptionColor(Color.GREEN);
        chart.setTouchEnabled(true);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setDragEnabled(true);

//
        Legend legend = chart.getLegend();
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLUE);

        // 设置x轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setDrawGridLines(true);
        xAxis.setSpaceBetweenLabels(10);

        // 设置左侧坐标轴
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setGridColor(Color.WHITE);

        // 设置右侧坐标轴
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private ArrayList<String> getXAxisShowLable(int len) {
        ArrayList<String> m = new ArrayList<String>();

        for(int i=0; i<len; i++){
            m.add(String.valueOf(i));
        }
        return m;
    }
}
