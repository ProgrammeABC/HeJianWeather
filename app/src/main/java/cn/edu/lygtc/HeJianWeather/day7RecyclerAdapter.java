package cn.edu.lygtc.HeJianWeather;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class day7RecyclerAdapter extends RecyclerView.Adapter<day7RecyclerAdapter.MyViewHolder> {
    private String [] dates = {"1","1","1","1","1","1","1"} ;
    private String [] temps = {"0℃/0℃","0℃/0℃","0℃/0℃","0℃/0℃","0℃/0℃","0℃/0℃","0℃/0℃"} ;
    private String [] images = {"1","1","1","1","1","1","1"} ;
    private Resources myRes;
    private String myPN;
    private String nowCityGPS;

    public void setNowCityGPS(String nowCityGPS) {
        this.nowCityGPS = nowCityGPS;
    }

    //    private MyViewHolder holderAll;
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        getWeatherData();
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.day7recyclerview_item,parent,false));
//        holderAll = holder;
        myRes = parent.getContext().getResources();
        myPN = parent.getContext().getPackageName();
        return holder;
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.Date.setText(dates[position]);
        holder.TEMP.setText(temps[position]);
        String icon_path = "vd_"+images[position];
        int resid = myRes.getIdentifier(icon_path , "drawable", myPN);
        holder.State.setImageResource(resid);
    }
    public int getItemCount(){
        return 7;
    }
    class MyViewHolder extends ViewHolder{
        TextView Date;
        TextView TEMP;
        ImageView State;
        public MyViewHolder(View view){
            super(view);
            Date = view.findViewById(R.id.day7DateTextView);
            TEMP = view.findViewById(R.id.day7TemperatureTextView);
            State = view.findViewById(R.id.day7WeatherStatusImageView);
        }
    }
    /**
     * 添加并更新数据，同时具有动画效果
     */
    public void getWeatherData(){
        long OneDay = 60*60*24*1000;
        long now = System.currentTimeMillis();
        String[] day7TempMax,day7TempMin,day7Image,day7Text;
        SimpleDateFormat sDateFMD = new SimpleDateFormat("MM月dd日");
        SimpleDateFormat sDateFE = new SimpleDateFormat("E");
        WeatherHttpConnect whc = new WeatherHttpConnect();
        whc.setNowCityGPS(nowCityGPS);
        List<String[]> ls = whc.run01("day7");
        day7TempMin = ls.get(0);
        day7TempMax = ls.get(1);
        day7Image = ls.get(2);
        day7Text = ls.get(3);
        System.out.println("day7TempMax:"+ls);
        for (int i=0;i<7;i++){
            Date virtualNow = new Date(now + OneDay*i);
            if(i==0){
                dates[i] = sDateFMD.format(virtualNow)+"|今天";
            } else if(i==1){
                dates[i] = sDateFMD.format(virtualNow)+"|明天";
            } else{
                dates[i] = sDateFMD.format(virtualNow)+"|"+sDateFE.format(virtualNow);
            }
            temps[i] = day7TempMin[i]+"℃/"+day7TempMax[i]+"℃";
            images[i] = day7Image[i];
//            if("1".equals(dates[1])){
//                onBindViewHolder(holderAll,i);
//            }
        }
    }
    public void refreshData(){
        getWeatherData();
        notifyDataSetChanged();
    }
}
