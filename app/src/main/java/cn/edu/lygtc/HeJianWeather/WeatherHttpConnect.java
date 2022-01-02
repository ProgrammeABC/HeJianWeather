package cn.edu.lygtc.HeJianWeather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class WeatherHttpConnect extends Thread{
    private String hfURL;
    private String hfKey = "b522c45dc1234bac892fe445c8c4a711";
    private String nowCity="101010100";
    private String nowCityGPS;
    private Boolean loading=false;
    private String nowTemp,nowImage,nowText,dataTypeAll,locationText;
    private String[] day7TempMax = new String[7],day7TempMin = new String[7],day7Image = new String[7],day7Text = new String[7];

    public void setNowCityGPS(String nowCityGPS) {
        this.nowCityGPS = nowCityGPS;
    }

    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL(hfURL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    parseJSONWithJSONObject(response.toString());
                    // showResponse(response.toString());
                    //Ui线程
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                        loading = true;
                    }
                }
            }
        }).start();
    }
    private void parseJSONWithJSONObject(String jsonData) {
        try {
            JSONObject jo = new JSONObject(jsonData);
            String status = jo.getString("code");
            System.out.println("ls1"+dataTypeAll+status);
            if("200".equals(status)){
                System.out.println("获取数据成功！");
                switch (dataTypeAll){
                    case "today":
                        JSONObject now_source = jo.getJSONObject("now");
                        System.out.println("nowtemp:"+now_source.get("temp"));
                        nowTemp = now_source.get("temp").toString();
                        nowImage = now_source.get("icon").toString();
                        nowText = now_source.get("text").toString();
                        break;
                    case "day7":
                        JSONArray day7_source = jo.getJSONArray("daily");
                        System.out.println("day7_source:"+day7_source);
                        for(int i = 0; i < 7 ; i++){
                            JSONObject day7Item = day7_source.getJSONObject(i);
                            System.out.println("day7Item:"+day7Item);
                            System.out.println("day7Item"+i+"tempMax"+day7Item.get("tempMax").toString());
                            day7TempMax[i] = day7Item.get("tempMax").toString();
                            day7TempMin[i] = day7Item.get("tempMin").toString();
                            day7Image[i] = day7Item.get("iconDay").toString();
                            day7Text[i] = day7Item.get("textDay").toString();
                        }
                        System.out.println("day7TempMaxAll:"+day7TempMax[1]);
                        break;
                    case "location":
                        JSONArray location_source = jo.getJSONArray("location");
                        JSONObject locations = location_source.getJSONObject(0);
                        locationText = locations.getString("adm2")+" "+locations.getString("name");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<String[]> run01(String dataType){
        String baseurl = "https://devapi.qweather.com/v7/weather/";
        dataTypeAll = dataType;
        switch (dataType){
            case "today":
                hfURL = baseurl+"now?"+"key="+hfKey+"&location="+nowCityGPS;
                sendRequestWithHttpURLConnection();
                while(!loading){
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                List<String[]> lsToday = new LinkedList<>();
                lsToday.add(new String[]{nowTemp, nowImage, nowText});
                loading = false;
                return lsToday;
            case "day7":
                hfURL = baseurl+"7d?"+"key="+hfKey+"&location="+nowCityGPS;
                sendRequestWithHttpURLConnection();
                while(!loading){
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                List<String[]> lsDay7 = new LinkedList<>();
                lsDay7.add(day7TempMin);
                lsDay7.add(day7TempMax);
                lsDay7.add(day7Image);
                lsDay7.add(day7Text);
                loading = false;
                return lsDay7;
            case "as":
                break;
        }
        return null;
    }
    public String run_location(String nowCityGPS){
        System.out.println(nowCityGPS);
        dataTypeAll = "location";
        hfURL = "https://geoapi.qweather.com/v2/city/lookup?"+"key="+hfKey+"&location="+nowCityGPS;
        sendRequestWithHttpURLConnection();
        while(!loading){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        loading = false;
        System.out.println(locationText);
        return locationText;
    }
}
