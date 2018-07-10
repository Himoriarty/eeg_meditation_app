package com.aliyun.oss.ossdemo;


/**
 * Created by moriarty on 18-5-7.
 */

public class CommentDetailBean {
    private int id;
    private String nickname;
    private String headpic;
    private String summary;
    private String timedate;

    public CommentDetailBean(String nickname,  String summary, String timedate) {
        this.nickname = nickname;
        this.summary = summary;
        this.timedate = timedate;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return nickname;
    }

    public void setHeadpic(String headpic) {
        this.headpic = headpic;
    }
    public String getHeadpic() {
        return headpic;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
    public String getSummary() {
        return summary;
    }

    public void setTimedate(String timedate) {
        this.timedate = timedate;
    }
    public String getTimedate() {
        return timedate;
    }
}
