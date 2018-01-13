package com.example.tmbro.isstracker;

import android.content.Context;
import android.os.AsyncTask;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Trist on 29-12-2017.
 */

public class UpdateThread extends AsyncTask<Context, Integer, double[]> {
List<Double> lat;
List<Double> lon;
Context currentContext;
Timestamp timestamp;

    public UpdateThread(Context context){
        currentContext = context;
    }

    @Override
    protected double[] doInBackground(Context... integers) {

        MapController mCon = new MapController("https://api.wheretheiss.at/v1/satellites/25544", currentContext);
        timestamp = new Timestamp(0);

            while (true) {
                Long tsLong = System.currentTimeMillis()/1000;
                lat = mCon.getSatLat();
                lon = mCon.getSatLong();
                mCon = new MapController("https://api.wheretheiss.at/v1/satellites/25544/positions?timestamps=" + (tsLong - 2000) + "," + (tsLong - 1500) + "," + (tsLong - 1000) + "," + (tsLong - 500) + "," + (tsLong) + "," + (tsLong + 500) + "," + (tsLong + 1000) + "," + (tsLong + 1500) + "," + (tsLong + 2000), currentContext);
                 try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    public List<Double> lat() {
        return lat;
    }

    public List<Double> lon() {
        return lon;
    }
}

