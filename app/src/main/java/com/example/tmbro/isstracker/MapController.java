package com.example.tmbro.isstracker;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tmbro on 28-12-2017.
 */

public class MapController {

    private double satLong;
    private double satLat;

    public MapController(String url, Context context) {

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            satLong = new JsonParser(response).getLon();
                            satLat = new JsonParser(response).getLat();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }
                    );

        MySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    public double getSatLong() {
        return satLong;
    }

    public double getSatLat() {
        return satLat;
    }
}