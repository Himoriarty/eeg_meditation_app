package com.aliyun.oss.ossdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by moriarty on 18-5-10.
 */

public class CheckAdapter extends BaseAdapter {
    private Context mContext;
    private List<CheckBean> checkBeanList;

    public CheckAdapter(Context context) {
        mContext = context;
    }

    public void setListDate(List<CheckBean> checklist) {
        checkBeanList = checklist;
    }

    @Override
    public int getCount() {
        return checkBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return checkBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MyHoder hoder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.check_item, null);
            hoder = new MyHoder();
            hoder.day = (TextView) convertView.findViewById(R.id.item_day);
            hoder.status = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(hoder);
        } else {
            hoder = (MyHoder) convertView.getTag();
        }

        hoder.day.setText("day" + checkBeanList.get(position).day);
        if (checkBeanList.get(position).check_status == CheckBean.CHECK_NO) {
            hoder.status.setImageResource(R.mipmap.check_no);
//            hoder.status.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(mContext, "恭喜你，签到成功！", Toast.LENGTH_SHORT).show();
//                    hoder.status.setImageResource(R.mipmap.checked);
//                    hoder.status.setClickable(false);
//                    checkBeanList.get(position).check_status = CheckBean.CHECKED;
//                    /* 在此做一些签到请求的处理 */
//                }
//            });
        } else if (checkBeanList.get(position).check_status == CheckBean.CHECK_WAIT) {
            hoder.status.setImageResource(R.mipmap.check_wait);
        } else if (checkBeanList.get(position).check_status == CheckBean.CHECKED) {
            hoder.status.setImageResource(R.mipmap.checked);
        }
        return convertView;
    }

    private static class MyHoder {
        TextView day;
        ImageView status;
    }
}
