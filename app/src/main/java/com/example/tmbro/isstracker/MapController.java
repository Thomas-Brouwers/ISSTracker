package com.example.tmbro.isstracker;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by tmbro on 28-12-2017.
 */

public class MapController {
    private static MapController instance = new MapController();
    public final static String KEY_PREFERENCES = "myPreferences";
    public final static String KEY_SAVENAME = "poidata";
    public final static String LOG_SHAREDPREF = "SharedPreferences";
    private boolean init = false;

    public void init(InputStream poiInputStream) {
        if (!init) {
            //pois = new JsonParser().getAllPOIs(getJsonArray(poiInputStream));
            init = true;
        }
    }

    public boolean isInit() {
        return init;
    }

    private MapController() {

    }

    public static MapController getInstance() {
        return instance;
    }

    private JSONArray getJsonArray(InputStream ins) {
        JSONArray jsonArray = null;
        try {
            int size = 0;
            size = ins.available();
            byte[] buffer = new byte[size];
            ins.read(buffer);
            ins.close();
            String textJson = new String(buffer, "UTF-8");
            jsonArray = new JSONArray(textJson);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonArray;
    }
}

