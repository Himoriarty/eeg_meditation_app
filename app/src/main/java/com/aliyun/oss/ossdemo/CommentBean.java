package com.aliyun.oss.ossdemo;

import java.util.List;


/**
 * Created by moriarty on 18-5-7.
 */

public class CommentBean {

    private Data data;
    public void setData(Data data) {
        this.data = data;
    }
    public Data getData() {
        return data;
    }

    public class Data {

        private int total;
        private List<CommentDetailBean> posts;
        public void setTotal(int total) {
            this.total = total;
        }
        public int getTotal() {
            return total;
        }

        public void setList(List<CommentDetailBean> posts) {
            this.posts = posts;
        }
        public List<CommentDetailBean> getList() {
            return posts;
        }

    }

}