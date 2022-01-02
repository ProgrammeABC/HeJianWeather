package cn.edu.lygtc.HeJianWeather;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageHttpConnect extends Thread {
    MyAppDBHelper DataBaseHelper;
    Bitmap bMap;
    String ImageName;
    Boolean loading = false;

    public void setDataBaseHelper(MyAppDBHelper dataBaseHelper) {
        DataBaseHelper = dataBaseHelper;
    }

    public Bitmap GetImageInputStream(String imageurl){
        URL url;
        HttpURLConnection connection=null;
        Bitmap bitmap=null;
        try {
            url = new URL(imageurl);
            connection=(HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(6000); //超时设置
            connection.setDoInput(true);
            connection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream=connection.getInputStream();
            bitmap= BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void SavaImage(Bitmap bitmap, String path){
        File file=new File(path);
        FileOutputStream fileOutputStream=null;
        //文件夹不存在，则创建它
        if(!file.exists()){
            file.mkdir();
        }
        try {
            fileOutputStream=new FileOutputStream(path+ImageName+".png");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("异步线程七点");
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("https://service.picasso.adesk.com/v1/vertical/category/4e4d610cdf714d2966000002/vertical?limit=30&adult=false&first=1&order=new");
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
            String status = jo.getString("msg");

            if("success".equals(status)){
                System.out.println("获取壁纸成功！");
                JSONArray image_source = jo.getJSONObject("res").getJSONArray("vertical");
                String SDCardDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/HeJianWeather/";
                SQLiteDatabase db;
                db = DataBaseHelper.getReadableDatabase();
                for(int i = 0;i < image_source.length(); i++){
                    JSONObject image_unit = image_source.getJSONObject(i);
                    String image_link = image_unit.get("img").toString();
                    Log.e("parseJSONWithJSONObject: "+(i+1), image_link);
                    bMap = GetImageInputStream(image_link);
                    if(i>=10){
                        ImageName = "IMage"+(i+1);
                    } else {
                        ImageName = "IMage0"+(i+1);
                    }
                    SavaImage(bMap, SDCardDir);
                    db.execSQL("INSERT INTO background(uri)VALUES(?)", new String[]{ImageName});
                }
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void run0(){
        sendRequestWithHttpURLConnection();
        while(!loading){
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        loading = false;
    }
}