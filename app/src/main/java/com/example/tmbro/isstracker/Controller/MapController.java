package com.example.tmbro.isstracker.Controller;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tmbro on 28-12-2017.
 */

public class MapController {

    private List<Double> satLong;
    private List<Double> satLat;

    public MapController(String url, Context context) {
        satLong = new ArrayList<>();
        satLat = new ArrayList<>();

            JsonArrayRequest jsObjRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            for(int i = 0 ; i < response.length();i++)
                                try {
                                    satLong.add(new JsonParser(response.getJSONObject(i)).getLon());
                                    satLat.add(new JsonParser(response.getJSONObject(i)).getLat());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

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

    public List<Double> getSatLong() {
        return satLong;
    }

    public List<Double> getSatLat() {
        return satLat;
    }
}