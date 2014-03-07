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

import com.google.android.gms.maps.model.LatLng;
import com.zroad.interfaces.AsyncTaskListener;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class DestInfoController extends android.os.AsyncTask<String,Void, String>{
	private final String AUTOCOMPLETE_PLACES_API_BASE="https://maps.googleapis.com/maps/api/place/details/json?sensor=false";
	private final String MARKER_PLACES_API_BASE="https://maps.googleapis.com/maps/api/place/nearbysearch/json?radius=1&sensor=true";
	private final String GOOGLE_API_KEY = Constants.GOOGLE_API_KEY;
	private final String LOG_TAG = "DestInfoController";
	
	private AsyncTaskListener listener;
	private Double lat;
	private Double lng;
	private String ref;
	private LatLng marker_loc;
	private int type;
	private int index=0;
	private String marker_name;
	
//	public DestInfoController(int i) {
//		index = i; 
	public DestInfoController(FragmentActivity act, int index, ArrayList<String> ref_list){
		ref = getRef(index,ref_list);
		type = Constants.DEST_AUTOCOMPLETE_INFO;
		listener = (AsyncTaskListener) act;
	}

	public DestInfoController(FragmentActivity act, LatLng location) {
		marker_loc = location;
		type = Constants.DEST_MARKER_INFO;
		listener = (AsyncTaskListener) act;
	}
	
	@Override
	protected String doInBackground(String... str) {
		String param = type==Constants.DEST_MARKER_INFO?"":ref.toString();
		resultParse(getService(param));
		return null;
	}
	
	@Override
	protected void onPostExecute(String str) {
		super.onPostExecute(str);

		switch(type){
			case Constants.DEST_MARKER_INFO:
				listener.onTaskComplete(marker_name);
				break;
			default:
				listener.onTaskComplete(new LatLng(lat,lng));
				break;
		}
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
	    	
	    	StringBuilder sb;
			switch(type){
				case Constants.DEST_MARKER_INFO:
					sb = new StringBuilder(MARKER_PLACES_API_BASE);
					sb.append("&location="+marker_loc.latitude+","+marker_loc.longitude);
					break;
				default:
					sb = new StringBuilder(AUTOCOMPLETE_PLACES_API_BASE);
					sb.append("&reference=" + ref);
					break;
			}
			sb.append("&key=" + GOOGLE_API_KEY);
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

			switch(type){
				case Constants.DEST_MARKER_INFO:
					marker_name = jObj.getJSONArray("results").getJSONObject(0).getString("name");
					break;
				default:
					lat = (Double)jObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat");
					lng = (Double)jObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng");
					break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Cannot process JSON results", e);
		}
	}
	//endregion
}
