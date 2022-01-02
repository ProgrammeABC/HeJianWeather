package cn.edu.lygtc.HeJianWeather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoadActivity extends AppCompatActivity {
    private TextView loadInfo;
    MyAppDBHelper DataBaseHelper;
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    private Boolean request = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        loadInfo = findViewById(R.id.textView);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        DataBaseHelper = new MyAppDBHelper(this);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setOpenGps(true); // 打开gps
        option.setIsNeedAddress(true);
        mLocationClient = new LocationClient(getApplicationContext(),option);
        mLocationClient.registerLocationListener(myListener);
        requestPermission();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                jump();
            }
        },5000);

    }
    public void jump(){
        SharedPreferences sp = getSharedPreferences("location",MODE_PRIVATE);
        long getTime = sp.getLong("time",0);
        long nowTime = System.currentTimeMillis();
        String lat = sp.getString("latitude","-1");
        System.out.println(sp.getBoolean("OnceOnLoad",true));
        if(sp.getBoolean("OnceOnLoad",true)){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (request){
                startActivity(new Intent(LoadActivity.this, MainActivity.class));
                finish();
            }else {
                jump();
            }
        }else{
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(nowTime-getTime>60*1000||lat.equals("-1")){
                    System.out.println("if"+getTime+""+lat);
                    requestPermission();
                    jump();
                    break;
                }else{
                    System.out.println("else");
                    Intent BookIntent = new Intent(this, MainActivity.class);
                    startActivity(BookIntent);
                    finish();
                    break;
                }
            }
        }
        UpdateBackground();
    }
    public void UpdateBackground() {
//        Intent BookIntent = new Intent(this,BookActivity.class);
        long nowTime = System.currentTimeMillis();
        long getTime;
        SharedPreferences sp = getSharedPreferences("backgroundMapDepotUpdate",MODE_PRIVATE);
        getTime = sp.getLong("lastUpdateDate",0);
        if(nowTime-getTime>24*60*60*1000){
            loadInfo.setText("检查：背景图库更新启动");
//            Toast.makeText(this, "背景图库更新启动", Toast.LENGTH_SHORT).show();
            ImageHttpConnect httpConnect = new ImageHttpConnect();
            httpConnect.setDataBaseHelper(DataBaseHelper);
            httpConnect.run0();
            Editor spEdit = sp.edit();
            spEdit.putLong("lastUpdateDate",nowTime);
            spEdit.commit();
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
//            loadInfo.setText("背景图库更新完毕！");
        } else {
            String res;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(getTime);
            res = simpleDateFormat.format(date);
//            Toast.makeText(this, "背景图库已是最新！(上次更新"+res+")", Toast.LENGTH_LONG).show();
            loadInfo.setText("检查：背景图库已是最新！(上次更新"+res+")");
        }
    }
private void requestPermission(){
    List<String> permissionList = new ArrayList<>();
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.READ_EXTERNAL_STORAGE);
    }
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.ACCESS_FINE_LOCATION);
    }
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.ACCESS_WIFI_STATE);
    }
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.ACCESS_NETWORK_STATE)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.ACCESS_NETWORK_STATE);
    }
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.CHANGE_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.CHANGE_WIFI_STATE);
    }
    if (ContextCompat.checkSelfPermission(LoadActivity.this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED){
        permissionList.add( Manifest.permission.INTERNET);
    }
    if (!permissionList.isEmpty()){
        String[] permissions = permissionList.toArray(new String[permissionList.size()]);
        ActivityCompat.requestPermissions(LoadActivity.this,permissions,1);
    } else {
        requestLocation();
    }
}
private void requestLocation(){
    mLocationClient.start();
    System.out.println("mlocation");
}
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
    switch (requestCode) {
        case 1:
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    System.out.println(result);
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "请授予全部权限！", Toast.LENGTH_LONG).show();
                        requestPermission();
                        return;
                    }
                }
                requestLocation();
            } else {
                Toast.makeText(this, "发生未知错误", Toast.LENGTH_LONG).show();
                finish();
            }
            break;
        default:
    }
}

public class MyLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {
        System.out.println("监听运作");
        SharedPreferences sp = getSharedPreferences("location",MODE_PRIVATE);
        long nowTime = System.currentTimeMillis();
        Editor editor = sp.edit();
        StringBuilder currentPosition = new StringBuilder();
        editor.putString("latitude",String.format("%.2f", location.getLatitude()));
        editor.putString("longitude",String.format("%.2f", location.getLongitude()));
        editor.putString("locationText",location.getCity()+" "+location.getDistrict());
        editor.putLong("time",nowTime);
        editor.commit();
        currentPosition.append("定位信息：纬度: ").append(location.getLatitude()).
                append(" ");
        currentPosition.append("经度：").append(location.getLongitude()).
                append(" ");
//        currentPosition. append ("定位方式：");
        if(!"".equals(location.getLatitude())){
            request = true;
        }
//        if (location.getLocType() == BDLocation.TypeGpsLocation) {
//            currentPosition.append("GPS");
//        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
//            currentPosition.append("网络");
//        }
       loadInfo.setText(currentPosition);
        mLocationClient.stop();
    }
}

}