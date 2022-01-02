package cn.edu.lygtc.HeJianWeather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import cn.edu.lygtc.HeJianWeather.bean.JsonBean;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SettingActivity extends AppCompatActivity  implements View.OnClickListener {


    private List<JsonBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();
    private Thread thread;
    private static final int MSG_LOAD_DATA = 0x0001;
    private static final int MSG_LOAD_SUCCESS = 0x0002;
    private static final int MSG_LOAD_FAILED = 0x0003;

    private static boolean isLoaded = false;
    public LocationClient mLocationClient = null;
    private My1LocationListener my1Listener = new My1LocationListener();
    private TextView nowCityTextView;
    private int bg_num=30;
    private int now_bg=1;
    private ImageView BackgroundView;
    private Button changeImageButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mHandler.sendEmptyMessage(MSG_LOAD_DATA);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        initView();
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setCoorType("bd09ll");
//        option.setOpenGps(true); // 打开gps
        option.setIsNeedAddress(true);
        mLocationClient = new LocationClient(getApplicationContext(),option);
        mLocationClient.registerLocationListener(my1Listener);
        getNowCity();
    }
    private void getNowCity(){
        SharedPreferences sp =  getSharedPreferences("location",MODE_PRIVATE);
        String nowCity = sp.getString("locationText","无信息");
        nowCityTextView.setText("当前城市："+nowCity);
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_DATA:
                    if (thread == null) {//如果已创建就不再重新创建子线程了

                        Toast.makeText(SettingActivity.this, "Begin Parse Data", Toast.LENGTH_SHORT).show();
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // 子线程中解析省市区数据
                                initJsonData();
                            }
                        });
                        thread.start();
                    }
                    break;

                case MSG_LOAD_SUCCESS:
                    Toast.makeText(SettingActivity.this, "行政区划获取成功", Toast.LENGTH_SHORT).show();
                    isLoaded = true;
                    break;

                case MSG_LOAD_FAILED:
                    Toast.makeText(SettingActivity.this, "行政区划获取失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void initView() {
        findViewById(R.id.showCityListButton).setOnClickListener(this);
        nowCityTextView = findViewById(R.id.nowCityTextView);
        changeImageButton = findViewById(R.id.ChangeImageButton);
        BackgroundView = findViewById(R.id.settingPageImageView);
        getImageNumber();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showCityListButton:
                if (isLoaded) {
                    showPickerView();
                } else {
                    Toast.makeText(SettingActivity.this, "Please waiting until the data is parsed", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.useBDLoactionButton:
                mLocationClient.start();
                System.out.println("mlocation");
                break;
        }
    }


    private void showPickerView() {// 弹出选择器

        OptionsPickerView pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String opt1tx = options1Items.size() > 0 ?
                        options1Items.get(options1).getPickerViewText() : "";

                String opt2tx = options2Items.size() > 0
                        && options2Items.get(options1).size() > 0 ?
                        options2Items.get(options1).get(options2) : "";

                String opt3tx = options2Items.size() > 0
                        && options3Items.get(options1).size() > 0
                        && options3Items.get(options1).get(options2).size() > 0 ?
                        options3Items.get(options1).get(options2).get(options3) : "";

                String tx = opt1tx + opt2tx + opt3tx;
                Toast.makeText(SettingActivity.this, "基于选单的地址："+tx, Toast.LENGTH_SHORT).show();
                getNowCity();
                SharedPreferences sp = getSharedPreferences("location",MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("locationTextTop",opt1tx);
                editor.putString("locationTextThis",opt2tx);
                editor.putString("locationText",opt2tx+" "+opt3tx);
                editor.commit();
                nowCityTextView.setText(opt2tx+" "+opt3tx);
            }
        })

                .setTitleText("城市选择")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build();

        /*pvOptions.setPicker(options1Items);//一级选择器
        pvOptions.setPicker(options1Items, options2Items);//二级选择器*/
        pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
        pvOptions.show();
    }

    private void initJsonData() {//解析数据


        String JsonData = new GetJsonDataUtil().getJson(this, "province.json");//获取assets目录下的json文件数据

        ArrayList<JsonBean> jsonBean = parseData(JsonData);//用Gson 转成实体

        options1Items = jsonBean;

        for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
            ArrayList<String> cityList = new ArrayList<>();//该省的城市列表（第二级）
            ArrayList<ArrayList<String>> province_AreaList = new ArrayList<>();//该省的所有地区列表（第三极）

            for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                String cityName = jsonBean.get(i).getCityList().get(c).getName();
                cityList.add(cityName);//添加城市
                ArrayList<String> city_AreaList = new ArrayList<>();//该城市的所有地区列表
                city_AreaList.addAll(jsonBean.get(i).getCityList().get(c).getArea());
                province_AreaList.add(city_AreaList);//添加该省所有地区数据
            }

            /**
             * 添加城市数据
             */
            options2Items.add(cityList);

            /**
             * 添加地区数据
             */
            options3Items.add(province_AreaList);
        }

        mHandler.sendEmptyMessage(MSG_LOAD_SUCCESS);

    }


    public ArrayList<JsonBean> parseData(String result) {//Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(MSG_LOAD_FAILED);
        }
        return detail;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
    public class My1LocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            System.out.println("监听运作");
            SharedPreferences sp = getSharedPreferences("location",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("locationTextTop",location.getProvince());
            editor.putString("locationTextThis",location.getDistrict());
            editor.putString("latitude",String.format("%.2f", location.getLatitude()));
            editor.putString("longitude",String.format("%.2f", location.getLongitude()));
            editor.putString("locationText",location.getCity()+" "+location.getDistrict());
            editor.commit();
            Toast.makeText(SettingActivity.this, "基于定位的地址："+location.getProvince()+location.getCity()+location.getDistrict(), Toast.LENGTH_SHORT).show();
            nowCityTextView.setText(location.getCity()+" "+location.getDistrict());
            mLocationClient.stop();
            getNowCity();
        }
    }
    public void clickChangeImage(View v){
        WeatherHttpConnect whc = new WeatherHttpConnect();
        whc.run01("today");
        SharedPreferences sp = getSharedPreferences("backgroundImageNumber",MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        if(now_bg<bg_num){
            now_bg++;
        } else {
            now_bg = 1;
        }
        spEditor.putInt("nowImage",now_bg);
        spEditor.commit();
        File imgFile;
        if(now_bg>=10){
            imgFile = new  File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/HeJianWeather/Image"+now_bg+".png");
        } else {
            imgFile = new  File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/HeJianWeather/Image0"+now_bg+".png");
        }
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            BackgroundView.setImageBitmap(myBitmap);
        }
    }
    public void getImageNumber() {
        SharedPreferences sp = getSharedPreferences("backgroundImageNumber", MODE_PRIVATE);
        bg_num = sp.getInt("backgroundImageNumber", 30);
        int now_bgn = sp.getInt("nowImage", -1);
        if (now_bgn > 0) {
            File imgFile;
            if (now_bgn >= 10) {
                imgFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/HeJianWeather/Image" + now_bgn + ".png");
            } else {
                imgFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/HeJianWeather/Image0" + now_bgn + ".png");
            }

            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                BackgroundView.setImageBitmap(myBitmap);
            }
        }
    }
    public void forceUpdate(View v) {
//        Intent BookIntent = new Intent(this,BookActivity.class);
        long nowTime = System.currentTimeMillis();
        long getTime;
        SharedPreferences sp = getSharedPreferences("backgroundMapDepotUpdate",MODE_PRIVATE);
        getTime = sp.getLong("lastUpdateDate",0);
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(getTime);
        res = simpleDateFormat.format(date);
        Toast.makeText(this, "背景图库强制更新启动(上次更新"+res+")", Toast.LENGTH_SHORT).show();
        ImageHttpConnect httpConnect = new ImageHttpConnect();
        httpConnect.run0();
        SharedPreferences.Editor spEdit = sp.edit();
        spEdit.putLong("lastUpdateDate",nowTime);
        spEdit.commit();
        Toast.makeText(this, "背景图库更新完毕", Toast.LENGTH_SHORT).show();
    }
    public void back(View v){
        Intent BookIntent = new Intent(this, MainActivity.class);
        startActivity(BookIntent);
        finish();
    }
}