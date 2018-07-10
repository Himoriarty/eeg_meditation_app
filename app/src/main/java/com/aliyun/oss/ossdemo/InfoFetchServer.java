package com.aliyun.oss.ossdemo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.in;


/**
 * Created by moriarty on 18-5-6.
 */

public class InfoFetchServer{
    private static final String TAG = "InfoFetchServer";

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+
                ": with "+ urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<userInfo> fecthItems(String suffix, String userId){
        List<userInfo> items = new ArrayList<>();
        try{
            String url = Uri.parse("http://47.101.35.106/api/" + suffix)
                    .buildUpon()
                    .appendQueryParameter("userid",userId)
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);

            if(suffix == "getuserinfo/"){
                parseUsers(items,jsonBody);
            }else if(suffix == "get4week/"){
                parseSigns(items, jsonBody);
            }else if (suffix == "getmails/"){
                parseMesgs(items, jsonBody);
            }else if (suffix == "getposts/"){
                parsePosts(items, jsonBody);
            }

        }catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        }catch (JSONException je){
            Log.e(TAG, "Failed to parse JSOn", je);
        }
        return items;
    }

    private void parseUsers(List<userInfo> items, JSONObject jsonBody) throws IOException, JSONException{
        userInfo userinfo = new userInfo();

        userinfo.setAgeValue(jsonBody.getString("age"));
        userinfo.setGender(jsonBody.getString("gender"));
        userinfo.setNickName(jsonBody.getString("nickname"));
        userinfo.setPictureurl(jsonBody.getString("pictureurl"));
        userinfo.setSlogan(jsonBody.getString("slogan"));

        items.add(userinfo);
    }

    private void parseSigns(List<userInfo> items, JSONObject jsonBody) throws IOException, JSONException{
        userInfo userinfo = new userInfo();
        userinfo.setSuccessSign(jsonBody.getString("success"));
        userinfo.setAlltime(jsonBody.getString("alltime"));

        JSONArray daysJsonArray = jsonBody.getJSONArray("records");
        int len = daysJsonArray.length();
        List<Integer> daysArray = new ArrayList();
        for(int i=0; i < len; i++){
            JSONObject daysObject = daysJsonArray.getJSONObject(i);
            Integer signday = Integer.valueOf(daysObject.getString("day"));
            if (daysArray.contains(signday)){
                continue;
            }else{
                daysArray.add(signday);
            }
        }

        Integer[] days = daysArray.toArray(new Integer[daysArray.size()]);
        userinfo.setDays(days);
        items.add(userinfo);
    }

    private void parseMesgs(List<userInfo> items, JSONObject jsonBody) throws IOException, JSONException{
        userInfo userinfo = new userInfo();

        String longMesg = jsonBody.getString("instruction");
        userinfo.setLongMesg(longMesg);

        JSONArray shortMesgArray = jsonBody.getJSONArray("mails");
        int len_short = shortMesgArray.length();
        String[] shortMesg = new String[len_short];
        for(int i=0; i<len_short;i++){
            JSONObject shortMesgObject = shortMesgArray.getJSONObject(i);
            shortMesg[i] = shortMesgObject.getString("content");
        }
        userinfo.setShortMesg(shortMesg);

        items.add(userinfo);
    }
    private void parsePosts(List<userInfo> items, JSONObject jsonBody) throws IOException, JSONException{
        userInfo userinfo = new userInfo();

        JSONArray postsArray = jsonBody.getJSONArray("posts");

        JSONArray postChangArray = new JSONArray();
        int total = postsArray.length();
        JSONObject postObject = new JSONObject();
        JSONObject postChangeObject = new JSONObject();
        for(int i=0; i<total;i++){
            postObject.put("total", total);
            JSONObject postEachObject = postsArray.getJSONObject(i);
            postEachObject.put("id", total-i);
            postChangArray.put(postEachObject);


        }
        postObject.put("posts",postChangArray);
        postChangeObject.put("data",postObject);
        String postString = postChangeObject.toString();
        userinfo.setPostString(postString);

        items.add(userinfo);
    }


}
