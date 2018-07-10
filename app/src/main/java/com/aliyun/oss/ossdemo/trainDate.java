package com.aliyun.oss.ossdemo;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by moriarty on 18-1-19.
 */

public class trainDate {
        private static String mYear;
        private static String mMonth;
        private static String mDay;

        public static String stringDate(){
            final Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
            mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
            mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
            return mYear + "年" + mMonth + "月" + mDay+"日";
        }

        public static String fileDate(){
            final Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
            int month = c.get(Calendar.MONTH) + 1;// 获取当前月份
            int day = c.get(Calendar.DAY_OF_MONTH);
            // 获取当前月份的日期号码
            if(month < 10){
                mMonth = "0" + String.valueOf(month);
            }else {
                mMonth = String.valueOf(month);
            }
            if(day < 10){
                mDay = "0"+ String.valueOf(day);
            }else{
                mDay = String.valueOf(day);
            }

            return mYear  + mMonth + mDay;
        }

}
