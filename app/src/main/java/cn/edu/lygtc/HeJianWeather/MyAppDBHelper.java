package cn.edu.lygtc.HeJianWeather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyAppDBHelper extends SQLiteOpenHelper {
        public MyAppDBHelper(Context context){
            super(context, "HeJianWeather.db", null, 1);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE background(_id INTEGER PRIMARY KEY AUTOINCREMENT, uri VARCHAR(20))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
}
