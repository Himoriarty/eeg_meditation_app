package com.aliyun.oss.ossdemo;


/**
 * Created by moriarty on 18-1-18.
 */

public class userInfo{

    private static final String TAG = "userInfoTag";

    //打卡签到显示
    private String successSign;
    private String alltime;
    private Integer[] days;

    public String getSuccessSign() {
        return successSign;
    }

    public void setSuccessSign(String successSign) {
        this.successSign = successSign;
    }

    public String getAlltime() {
        return alltime;
    }

    public void setAlltime(String alltime) {
        this.alltime = alltime;
    }

    public Integer[] getDays() {
        return days;
    }

    public void setDays(Integer[] days) {
        this.days = days;
    }

//接收长短时信息
    private String[] shortMesg;
    private String longMesg;

    public String[] getShortMesg() {
        return shortMesg;
    }

    public void setShortMesg(String[] shortMesg) {
        this.shortMesg = shortMesg;
    }

    public void setLongMesg(String longMesg) {
        this.longMesg = longMesg;
    }
    public String getLongMesg() {
        return longMesg;
    }

    private String postString;

    public String getPostString() {
        return postString;
    }

    public void setPostString(String postString) {
        this.postString = postString;
    }

    //用户个人信息
    private String userId;
    private String nickName = "zen";
    private String gender = "male";
    private String ageValue = "20";
    private String slogan = "dozero";
    private String pictureurl = "https:///sdsds";
    //user gender
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

//  user age

    public String getAgeValue() {
        return ageValue;
    }

    public void setAgeValue(String ageValue) {
        this.ageValue = ageValue;
    }

    //user slogan
    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    //user picture
    public String getPictureurl() {
        return pictureurl;
    }

    public void setPictureurl(String pictureurl) {
        this.pictureurl = pictureurl;
    }

    //user id
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    //nickname
    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }


    //训练时长
    private int each_time;

    public int getEach_time() {
        return each_time;
    }

    public void setEach_time(int each_time) {
        this.each_time = each_time;
    }

    //心得
    private String words;

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }
}
