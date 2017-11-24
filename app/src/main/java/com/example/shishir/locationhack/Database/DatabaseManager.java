package com.example.shishir.locationhack.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.shishir.locationhack.Model_Class.MyLocation;

import java.util.ArrayList;

/**
 * Created by Shishir on 11/24/2017.
 */

public class DatabaseManager {
    private MyLocation myLocation;
    private DatabaseHelper helper;
    private SQLiteDatabase database;


    public DatabaseManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    private void open() {
        database = helper.getWritableDatabase();

    }

    private void close() {
        helper.close();
    }


    public boolean addLocation(MyLocation location) {

        this.open();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COL_DAY, location.getDay());
        contentValues.put(DatabaseHelper.COL_TIME, location.getTime());
        contentValues.put(DatabaseHelper.COL_LAT, location.getLat());
        contentValues.put(DatabaseHelper.COL_LONG, location.getLng());

        long inserted = database.insert(DatabaseHelper.TABLE_LOCATION, null, contentValues);
        this.close();

        return inserted != -1;
    }


    public ArrayList<MyLocation> getAllLocation() {

        this.open();
        ArrayList<MyLocation> notificationList = new ArrayList<MyLocation>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_LOCATION, null, null, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToLast();
            int init = cursor.getCount() - 1;

            for (int i = init; i >= 0; i--) {

                String day = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_DAY));
                String time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_TIME));
                String lat = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_LAT));
                String lng = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_LONG));


                notificationList.add(new MyLocation(day, time, lat, lng));
                cursor.moveToPrevious();
            }

        }
        this.close();
        return notificationList;
    }


    public void deleteLocationAll() {
        this.open();
        database.delete(DatabaseHelper.TABLE_LOCATION, null, null);
        this.close();
    }

    public void deleteLocation(String dateStr) {
        this.open();
        database.delete(DatabaseHelper.TABLE_LOCATION, DatabaseHelper.COL_DAY + " =?", new String[]{dateStr});
        this.close();
    }

}
