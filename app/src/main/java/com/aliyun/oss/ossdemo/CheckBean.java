package com.aliyun.oss.ossdemo;

/**
 * Created by moriarty on 18-5-10.
 */

import java.io.Serializable;

public class CheckBean implements Serializable {
    // 根据自己的需求可以做补签的字段设置

    public static final int CHECKED = 3; //已签到
    public static final int CHECK_NO = 2; //没有签到
    public static final int CHECK_WAIT = 1; //等待签到 （时间没到无法签到）

    public int day;
    public int check_status;
}
