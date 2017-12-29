package com.example.tmbro.isstracker;

/**
 * Created by tmbro on 28-12-2017.
 */

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonParser {
    private double lon;
    private double lat;

    public JsonParser(JSONObject json){
        try {
            lon = json.getDouble("longitude");
            lat = json.getDouble("latitude");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }
}
