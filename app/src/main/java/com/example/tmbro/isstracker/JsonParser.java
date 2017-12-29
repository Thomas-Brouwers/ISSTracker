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

/**
 * Created by tmbro on 11-12-2017.
 */

public class JsonParser {
    /*public List<POI> getAllPOIs(JSONArray array){
        //JSONArray array = getJsonArray("pois_historic_route");
        ArrayList<POI> POIList = new ArrayList<>();
        for (int i = 0; i < array.length();i++){
            try {
                POIList.add(parsePOI(array.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return POIList;
    }

    private POI parsePOI(JSONObject json){
        POI poi = null;
        try {
            poi = new POI(
                    json.getInt("Nummer"),                  //nummer
                    json.getString("Naam"),                 //name
                    getDescription(json.getString("Tekst")),//description
                    json.getString("Foto"),                 //imagename
                    Location.convert(json.getString("OL")), //longitude
                    Location.convert(json.getString("NB")), //latitude
                    getCategory(json.getString("Naam"))     //Category
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return poi;
    }*/
}
