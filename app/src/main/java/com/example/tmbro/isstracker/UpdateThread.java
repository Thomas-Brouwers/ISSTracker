package com.example.tmbro.isstracker;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Trist on 29-12-2017.
 */

public class UpdateThread extends AsyncTask<Integer, Integer, Void> {

    @Override
    protected Void doInBackground(Integer... integers) {

            while (true) {
                Log.d("SUCCES", "Thread runs");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

    }
}

