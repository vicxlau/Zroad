package com.zroad.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.zroad_t1.R;

import android.content.res.Resources;
import android.util.Log;

public class AutoCompleteController {

//	final String AUTOCOMPLETE_API_BASE= Resources.getSystem().getString(R.string.AUTOCOMPLETE_API_BASE);
//	final String GOOGLE_API_KEY = Resources.getSystem().getString(R.string.GOOGLE_API_KEY);
	final String AUTOCOMPLETE_API_BASE="https://maps.googleapis.com/maps/api/place/autocomplete/json";
	final String GOOGLE_API_KEY = "AIzaSyBhfUqAFsL5gjvpwsYTLmfGo9_2f5fAF8Y";
	final String LOG_TAG = "AutoCompleteController";
	ArrayList<String> autocomplete_list;
	ArrayList<String> ref_list;
	
	public AutoCompleteController(String input){
		resultParse(getService(input));
	}
	
	public ArrayList<String> getRefList(){
		return ref_list;
	}
	
	public ArrayList<String> getAutoCompleteList(){
		return autocomplete_list;
	}
	
    private String getService(String input){
	    HttpURLConnection conn = null;
	    StringBuilder result = new StringBuilder();
	    try {
	        StringBuilder sb = new StringBuilder(AUTOCOMPLETE_API_BASE);
	        sb.append("?sensor=false&key=" + GOOGLE_API_KEY);
	        sb.append("&input=" + URLEncoder.encode(input, "utf8"));
//Log.i(LOG_TAG, "URL: "+sb.toString());
	        URL url = new URL(sb.toString());
	        conn = (HttpURLConnection) url.openConnection();
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());

	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = in.read(buff)) != -1) {
	            result.append(buff, 0, read);
	        }
	    } catch (MalformedURLException e) {
	        Log.e(LOG_TAG, "Error processing Places API URL", e);
	        
	    } catch (IOException e) {
	        Log.e(LOG_TAG, "Error connecting to Places API", e);
	        
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }
//Log.i(LOG_TAG, "AJAX RESULT: "+result.toString());	    
	    return result.toString();
    }

	private void resultParse(String result) {
	    autocomplete_list = null;
	    ref_list = null;
	    
	    try {
	        // Create a JSON object hierarchy from the results
	        JSONObject jsonObj = new JSONObject(result);
	        JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

	        // Extract the Place descriptions from the results
	        autocomplete_list = new ArrayList<String>(predsJsonArray.length());
	        ref_list = new ArrayList<String>(predsJsonArray.length());
	        for (int i = 0; i < predsJsonArray.length(); i++) {
	        	JSONObject obj = predsJsonArray.getJSONObject(i);
	            autocomplete_list.add(obj.getString("description"));
	            ref_list.add(obj.getString("reference"));
	        }
	    } catch (JSONException e) {
	        Log.e(LOG_TAG, "Cannot process JSON results", e);
	    }
	}
}
