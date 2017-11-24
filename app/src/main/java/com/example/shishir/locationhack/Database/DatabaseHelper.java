package com.example.shishir.locationhack.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Shishir on 11/24/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "location_hacker.db";


    public static final String TABLE_LOCATION = "locationt";
    public static final String COL_D_ID = "_id";
    public static final String COL_LAT = "lat";
    public static final String COL_LONG = "long";
    public static final String COL_TIME = "time";
    public static final String COL_DAY = "day";


    String CREATE_TABLE_DOCTOR = "create table " + TABLE_LOCATION + " ( " + COL_D_ID +
            " integer primary key," + COL_DAY + " text," + COL_TIME + " text," +
            COL_LAT + " text," + COL_LONG + " text )";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DOCTOR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXITS " + TABLE_LOCATION);
    }
}
