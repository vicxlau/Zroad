package com.zroad.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.zroad.interfaces.AsyncTaskListener;

import android.os.AsyncTask;
import android.util.Log;

public class RouteInfoController extends AsyncTask<String,Void, List<List<HashMap<String, String>>>>{
	private final String PLACES_API_BASE="https://maps.googleapis.com/maps/api/directions/json?sensor=false&mode=walking";
	private final String LOG_TAG = "RouteInfoController";
	
	private LatLng cur;
	private LatLng dest;
	private List<String> instructions;
	private List<String> durations;
	private String google_warning="";
	
	private AsyncTaskListener listener;
	
	public RouteInfoController(LatLng c,LatLng d,AsyncTaskListener act) {
		cur=c;
		dest=d;
		listener = (AsyncTaskListener) act;
	}

	@Override
	protected List<List<HashMap<String, String>>> doInBackground(String... str) {
		try {
			JSONObject jObj = new JSONObject(getService());
			return parse(jObj);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(List<List<HashMap<String, String>>> result) {
    	listener.onTaskComplete(result,instructions,durations,google_warning);
	}

	//region private methods
    private String getService(){
	    HttpURLConnection conn = null;
	    StringBuilder result = new StringBuilder();
	    try {
//	    	String url_str = PLACES_API_BASE+"&reference="+ref+"&key="+GOOGLE_API_KEY;
//	    	URL url = new URL(url_str);
//	    	Log.i(LOG_TAG, "URL: "+url_str);
	    	
	    	StringBuilder sb = new StringBuilder(PLACES_API_BASE);
	        sb.append("&origin="
    					+cur.latitude+","+cur.longitude
    					+"&destination="
    					+dest.latitude+","+dest.longitude);
//	        sb.append("&key=" + GOOGLE_API_KEY);
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
	        Log.e(LOG_TAG, "Error processing Direction API URL", e);
	        
	    } catch (IOException e) {
	        Log.e(LOG_TAG, "Error connecting to Direction API", e);
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
    
    private List<List<HashMap<String,String>>> parse(JSONObject jObject){
		
		List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
		List<String> html_instructions = new ArrayList<String>();
		durations = new ArrayList<String>();
		JSONArray jRoutes = null;
		JSONArray jLegs = null;
		JSONArray jSteps = null;	
		
		try {			
			
			jRoutes = jObject.getJSONArray("routes");
			
			/** Traversing all routes */
			for(int i=0;i<jRoutes.length();i++){
				JSONObject cur_jRoute = (JSONObject)jRoutes.get(i);
				
				// Traversing all warnings of the current route 
				JSONArray jWarnings = cur_jRoute.getJSONArray("warnings");
				for(int w=0; w<jWarnings.length();w++){
					google_warning += jWarnings.getString(w) +"/n";
				}
				
				jLegs = cur_jRoute.getJSONArray("legs");
				List path = new ArrayList<HashMap<String, String>>();
				
				/** Traversing all legs */
				for(int j=0;j<jLegs.length();j++){
					jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
					
					/** Traversing all steps
					 *  Each step consists:
					 *  one polyline ( st.line = 2pts ; curve line = >2pts)
					 *  one html_instruction
					 *  
					 *  List<Hash<String,String> pts_in_polyline
					 *  String html_instruction
					 *  
					 *  List<List<Hash<String,String>> pts_in_list_polylines
					 *  List<String> list_html_instruction
					 * */
					for(int k=0;k<jSteps.length();k++){
						JSONObject cur_jStep = (JSONObject)jSteps.get(k);
						String polyline = "";
						polyline = (String)((JSONObject)cur_jStep.get("polyline")).get("points");
						html_instructions.add((String)cur_jStep.get("html_instructions"));
						durations.add((String)((JSONObject)cur_jStep.get("duration")).get("text"));
						List<LatLng> list = decodePoly(polyline);
//Log.e(LOG_TAG, "1 instruction ; "+list.size()+" pts");						
						/** Traversing all points */
						for(int l=0;l<list.size();l++){
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
							hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
							path.add(hm);						
						}								
					}
					routes.add(path);
				}
			}
			
		} catch (JSONException e) {			
			e.printStackTrace();
		}catch (Exception e){			
		}
		
		instructions = html_instructions;
		return routes;
	}	
	
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
    //endregion
}
