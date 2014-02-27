package com.zroad.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.zroad.interfaces.AsyncTaskListener;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class DestInfoController extends android.os.AsyncTask<String,Void, String>{
	private final String PLACES_API_BASE="https://maps.googleapis.com/maps/api/place/details/json?sensor=false";
	private final String GOOGLE_API_KEY = Constants.GOOGLE_API_KEY;
	private final String LOG_TAG = "DestInfoController";
	
	private AsyncTaskListener listener;
	private Double lat;
	private Double lng;
	private String ref;
	
	private int index=0;
	
//	public DestInfoController(int i) {
//		index = i; 
	public DestInfoController(FragmentActivity act, int index, ArrayList<String> ref_list) {
		ref = getRef(index,ref_list);
		listener = (AsyncTaskListener) act;
	}

	@Override
	protected String doInBackground(String... str) {
		resultParse(getService(ref.toString()));
		return null;
	}
	
	@Override
	protected void onPostExecute(String str) {
		super.onPostExecute(str);

		ArrayList<Double> result = new ArrayList<Double>();
		result.add(lat);
		result.add(lng);
		listener.onTaskComplete(result);
	}
	
	//region private methods
	private String getRef(int index,ArrayList<String> ref_list){
		return ref_list.get(index);
	}
	
    private String getService(String ref){
	    HttpURLConnection conn = null;
	    StringBuilder result = new StringBuilder();
	    try {
//	    	String url_str = PLACES_API_BASE+"&reference="+ref+"&key="+GOOGLE_API_KEY;
//	    	URL url = new URL(url_str);
//	    	Log.i(LOG_TAG, "URL: "+url_str);
	    	
	    	StringBuilder sb = new StringBuilder(PLACES_API_BASE);
	        sb.append("&key=" + GOOGLE_API_KEY);
	        sb.append("&reference=" + ref);
Log.i(LOG_TAG, "URL: "+sb.toString());

	        URL url = new URL(sb.toString());
	        conn = (HttpURLConnection) url.openConnection();
	        InputStreamReader inReader = new InputStreamReader(conn.getInputStream());

	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = inReader.read(buff)) != -1) {
	            result.append(buff, 0, read);
	        }
	        
	    } catch (MalformedURLException e) {
	        Log.e(LOG_TAG, "Error processing Places API URL", e);
	        
	    } catch (IOException e) {
	        Log.e(LOG_TAG, "Error connecting to Places API", e);
	    }catch(Exception e){
	    	Log.e(LOG_TAG, "Error", e);	    	
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }
Log.i(LOG_TAG, "AJAX RESULT: "+result.toString());	    
	    return result.toString();
    }

	private void resultParse(String result){
		JSONObject jObj;
		try {
			jObj = new JSONObject(result);
			lat = (Double)jObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat");
			lng = (Double)jObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng");
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Cannot process JSON results", e);
		}
	}
	//endregion
}
