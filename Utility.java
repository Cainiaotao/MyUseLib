package com.example.tantao.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.tantao.coolweather.base.City;
import com.example.tantao.coolweather.base.Province;
import com.example.tantao.coolweather.db.CoolWeatherDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 * Created by tantao on 2016/4/25.
 */
public class Utility {

    /**
     * 获取包的版本
     * @param context
     * @return
     */
    public static String getVersion(Context context){
        try {
            PackageManager manager=context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        }catch (Exception e){
            e.printStackTrace();
            return "not find version";
        }
    }

    /**
     * 获取版本代号
     * @param context
     * @return
     */
    public static int getVersionCode(Context context){
        try{
            PackageManager manager=context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = info.versionCode;
            return versionCode;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }



    /**
     * 检查联网状态
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context){
        if (context!=null){
            ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if (networkInfo!=null){
                return networkInfo.isAvailable();
            }
        }
        return false;
    }



    /**
     * 解析json数据存入省、市
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response){
        if (!TextUtils.isEmpty(response))
        {
            try{
                JSONArray jsonArray=new JSONArray(response);
                StringBuilder reader=new StringBuilder();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String provincestr = jsonObject.getString("省");
                    JSONArray cities = jsonObject.getJSONArray("市");
                    for (int j = 0; j < cities.length(); j++) {
                        JSONObject jsonObjectCity = cities.getJSONObject(j);
                        String citycode = jsonObjectCity.getString("编码");
                        String cityname = jsonObjectCity.getString("市名");
                        // String result = provincestr + "." + city + "\t" + code + "\n";
                        City city=new City();
                        city.setCityName(cityname);
                        city.setCityCode(citycode);
                        city.setProvinceId(i);
                        coolWeatherDB.saveCity(city);
                    }
                    Province province=new Province();
                    province.setProvinceName(provincestr);
                    province.setProvinceCode(String.valueOf(i));
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 解析json数据获取天气
     * @param context
     * @param request
     */
    public static void handleWeatherResponse(Context context,String request){
        try{
            JSONObject jsonObject=new JSONObject(request);
            JSONObject weahterinfo=jsonObject.getJSONObject("weatherinfo");
            String cityName=weahterinfo.getString("city");
            String cityId=weahterinfo.getString("cityid");
            String temp1=weahterinfo.getString("temp1");
            String temp2=weahterinfo.getString("temp2");
            String weather=weahterinfo.getString("weather");
            String time=weahterinfo.getString("ptime");
            saveWeatherInfo(context,cityName,cityId,temp1,temp2,weather,time);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 保存天气到本地
     * @param context
     * @param cityName
     * @param cityId
     * @param temp1
     * @param temp2
     * @param weatherDesp
     * @param publishtime
     */
    public static void saveWeatherInfo(Context context,String cityName,String cityId,String temp1,String temp2,String weatherDesp,String publishtime){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", cityId);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishtime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }

}
