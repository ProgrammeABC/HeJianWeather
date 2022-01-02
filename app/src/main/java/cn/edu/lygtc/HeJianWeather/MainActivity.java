package cn.edu.lygtc.HeJianWeather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import cn.edu.lygtc.HeJianWeather.RefreshableView.*;

public class MainActivity extends AppCompatActivity{
    private ImageView imageView;
    private ImageView weatherStateImageView;
    private ScrollView scrollView;
    private TextView nowTempTextView;
    private TextView nowStateTextView;
    private RefreshableView refreshableView;
    public Handler handler;
    private day7RecyclerAdapter d7adapter;
    private RecyclerView day7Weather;
    private int bg_num=30;
    private int now_bg=1;
    private day7RecyclerAdapter day7adapterAll;
    private TextView nowCityTextView;
    private String nowCityGPS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        getImageNumber();
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        SharedPreferences sp = getSharedPreferences("location",MODE_PRIVATE);
        nowCityGPS = sp.getString("longitude","119.25")+","+sp.getString("latitude","34.66");
        refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
        scrollView = findViewById(R.id.scrollView);
        refreshableView.setOnRefreshListener(new PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("buttonText","更改后的按钮文本");
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    System.out.println("ok=fine");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshableView.finishRefreshing();
            }
        }, 0);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                LoadData();
                day7adapterAll.refreshData();
                day7adapterAll.setNowCityGPS(nowCityGPS);
                getImageNumber();
                return false;
            }
        });
        day7Weather = findViewById(R.id.day7RecyclerView);
        day7Weather.setLayoutManager(new LinearLayoutManager(this));
        d7adapter = new day7RecyclerAdapter();
        d7adapter.setNowCityGPS(nowCityGPS);
        day7adapterAll = d7adapter;
        day7Weather.setAdapter(d7adapter);
        LoadData();
    }
    public void init(){
        imageView = findViewById(R.id.imageView);
        nowTempTextView = findViewById(R.id.temperatureTextView);
        weatherStateImageView = findViewById(R.id.weatherStatusImageView);
        nowStateTextView = findViewById(R.id.weatherStatusTextView);
        nowCityTextView = findViewById(R.id.cityTextView);
        SharedPreferences sp = getSharedPreferences("location",MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean("OnceOnLoad",false);
        editor.commit();
    }
    public void LoadData(){
        SharedPreferences sp = getSharedPreferences("location",MODE_PRIVATE);
        String img_id,now_temp,icon_path,now_text;
        WeatherHttpConnect whc =  new WeatherHttpConnect();
        whc.setNowCityGPS(nowCityGPS);
        String locationText = sp.getString("locationText","定位失败");
        if(locationText.equals("null null")){
            locationText = whc.run_location("119.25,34.66");
        }
        List<String[]> ls = whc.run01("today");
        String[] requestData = ls.get(0);
        now_temp = requestData[0];
        img_id = requestData[1];
        now_text = requestData[2];
        System.out.println("imgid:"+img_id+"nowtext"+now_text);
        icon_path = "vd_"+img_id;
        int resid = getResources().getIdentifier(icon_path , "drawable", getPackageName());
        System.out.println("mainnow:"+now_temp);
        nowTempTextView.setText(now_temp);
        weatherStateImageView.setImageResource(resid);
        nowStateTextView.setText(now_text);
        nowCityTextView.setText(locationText);
    }
    public void getImageNumber(){
        SharedPreferences sp = getSharedPreferences("backgroundImageNumber", MODE_PRIVATE);
        bg_num = sp.getInt("backgroundImageNumber",30);
        int now_bgn = sp.getInt("nowImage",-1);
        if(now_bgn>0){
            File imgFile;
            if(now_bgn>=10){
                imgFile = new  File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/HeJianWeather/Image"+now_bgn+".png");
            } else {
                imgFile = new  File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/HeJianWeather/Image0"+now_bgn+".png");
            }

            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        }
    }
    public void GoToSettingPage(View v){
        Intent BookIntent = new Intent(this, SettingActivity.class);
        startActivity(BookIntent);
    }
}
