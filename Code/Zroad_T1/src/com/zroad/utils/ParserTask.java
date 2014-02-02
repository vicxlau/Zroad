package com.zroad.utils;

/*
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;
public  class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

	// Parsing the data in non-ui thread
	@Override
	protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

		JSONObject jObject;
		List<List<HashMap<String, String>>> routes = null;

		try{
			jObject = new JSONObject(jsonData[0]);
			DirectionsJSONParser parser = new DirectionsJSONParser();

			// Starts parsing data
			routes = parser.parse(jObject);
		}catch(Exception e){
			e.printStackTrace();
		}
		return routes;
	}

	// Executes in UI thread, after the parsing process
	@Override
	protected void onPostExecute(List<List<HashMap<String, String>>> result) {
		ArrayList<LatLng> points = null;
		PolylineOptions lineOptions = null;
		MarkerOptions markerOptions = new MarkerOptions();

		// Traversing through all the routes
		for(int i=0;i<result.size();i++){
			points = new ArrayList<LatLng>();
			lineOptions = new PolylineOptions();

			// Fetching i-th route
			List<HashMap<String, String>> path = result.get(i);

			// Fetching all the points in i-th route
			for(int j=0;j<path.size();j++){
				HashMap<String,String> point = path.get(j);

				double lat = Double.parseDouble(point.get("lat"));
				double lng = Double.parseDouble(point.get("lng"));
				LatLng position = new LatLng(lat, lng);

				points.add(position);
			}

			// Adding all the points in the route to LineOptions
			lineOptions.addAll(points);
			lineOptions.width(2);
			lineOptions.color(Color.BLUE);


			if(result.size()<1){
//				Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
				return;
			}

			// Drawing polyline in the Google Map for the i-th route
//			map.addPolyline(lineOptions);
		}
	}
}    	

*/