package com.aliyun.oss.ossdemo;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by moriarty on 18-5-14.
 */

public class UpdateThread extends Thread {
    private String updateMode;
    private String string0;
    private String string1;
    private String string2;
    private String string3;
    private String string4;

    private String callBack;

    public String getCallBack() {
        return callBack;
    }

    public void setCallBack(String callBack) {
        this.callBack = callBack;
    }

    public UpdateThread(String updateMode, String string0, String string1,
                        String string2, String string3, String string4){
        this.updateMode = updateMode;
        this.string0 = string0;
        this.string1 = string1;
        this.string2 = string2;
        this.string3 = string3;
        this.string4 = string4;
    }

    @Override
    public void run()
    {
        switch (updateMode){
            case "user":
                try {
                    Log.i("check", string0);

                    JSONObject userJSON = new JSONObject();
                    userJSON.put("userid",string0);
                    userJSON.put("nickname",string1);
                    userJSON.put("age",string2);
                    userJSON.put("gender",0);
                    userJSON.put("slogan",string3);
                    userJSON.put("pictureurl"," ");

                    String content = String.valueOf(userJSON);

                    String url = "http://47.101.35.106/api/updateuserinfo/";
                    HttpURLConnection updateconnection = (HttpURLConnection) new URL(url).openConnection();
                    updateconnection.setConnectTimeout(5000);
                    updateconnection.setRequestMethod("PUT");
                    updateconnection.setDoOutput(true);
                    updateconnection.setRequestProperty("User-Agent", "Fiddler");
                    updateconnection.setRequestProperty("Content-Type", "application/json");
                    updateconnection.setRequestProperty("Charset", "UTF-8");

                    OutputStream os = updateconnection.getOutputStream();
                    os.write(content.getBytes());
                    os.close();
                    /**
                     * 服务器返回结果
                     * 判断账户是否注册
                     */
                    InputStream is =updateconnection.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len=0 ;
                    while(-1 != (len = is.read(buffer))){
                        baos.write(buffer,0,len);
                        baos.flush();
                    }
                    String result = baos.toString("utf-8");
                    Log.i("response", result);
                    String re = getJsonData(result, "success");
                    setCallBack(re);

                    Log.i("success",re);

                }catch (Exception e)
                {

                }
                break;
            case "train":
                try {
                    JSONObject userJSON = new JSONObject();
                    userJSON.put("userid",string0);
                    userJSON.put("type",Integer.valueOf(string1));
                    userJSON.put("summary",string2);
                    userJSON.put("duration",Integer.valueOf(string3));
                    userJSON.put("EEGFileURL",string4);
                    userJSON.put("public",1);

                    String content = String.valueOf(userJSON);

                    String url = "http://47.101.35.106/api/sendresult/";
                    HttpURLConnection updateconnection = (HttpURLConnection) new URL(url).openConnection();
                    updateconnection.setConnectTimeout(5000);
                    updateconnection.setRequestMethod("POST");
                    updateconnection.setDoOutput(true);
                    updateconnection.setRequestProperty("User-Agent", "Fiddler");
                    updateconnection.setRequestProperty("Content-Type", "application/json");
                    updateconnection.setRequestProperty("Charset", "UTF-8");

                    OutputStream os = updateconnection.getOutputStream();
                    os.write(content.getBytes());
                    os.close();
                    /**
                     * 服务器返回结果
                     * 判断账户是否注册
                     */
                    InputStream is =updateconnection.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len=0 ;
                    while(-1 != (len = is.read(buffer))){
                        baos.write(buffer,0,len);
                        baos.flush();
                    }
                    String result = baos.toString("utf-8");
                    Log.i("response", result);
                    String re = getJsonData(result, "success");
                    setCallBack(re);

                    Log.i("success",re);

                }catch (Exception e)
                {

                }
                break;
        }


    }

    //解析json数据包
    private static String getJsonData(String response, String keyWord){
        String jsonData = null;
        try{
            JSONObject jsonObject = new JSONObject(response);
            jsonData = jsonObject.getString(keyWord);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonData;
    }
}
