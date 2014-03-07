package com.zroad.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.example.zroad_t1.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zroad.interfaces.AsyncTaskListener;
import com.zroad.interfaces.MapHandlerListener;

public class MapHandler implements AsyncTaskListener {
	
	//region Data Members
	final String LOG_TAG = "MapHandler";
	final private LatLng DEFAULT_CENTER = Constants.MAP_DEFAULT_CENTER;
	final BitmapDescriptor MARKER_ICON = BitmapDescriptorFactory.fromResource(Constants.MAP_MARKER_ICON); 
//	LatLng bound1 = new LatLng(22.321534053237787,114.16566754456949);
//	LatLng bound2 = new LatLng(22.317067780105184,114.17223359223794);
		
	private GoogleMap map;
	private Location curLoc;
	private LatLng dest;
	private MapHandlerListener listener;

	private List<LatLng> routes;
	private List<String> instructions;
	private List<String> durations;
	private String warning;
	
	private int curRouteIndex;
//	private LatLngBounds curLeg;
//	private Context mapCon; // For getting current location here
	
	//endregion
	
	public MapHandler(GoogleMap m,MapHandlerListener act){
		map=m;
		listener = (MapHandlerListener)act;
//		mapCon=c;
	}

	//region public methods
	public GoogleMap initMap(){
		// Set the camera to the greatest possible zoom level that includes the bounds
//		map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(bound1,bound2), 0));
		
		// Enable MyLocation Button in the Map
		map.setMyLocationEnabled(true);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER, 17));
		curRouteIndex = 0;
	    return map;
	}

	public GoogleMap showDestMarker(){
		addDestMarker(true);
		return map;
	}
	
	public GoogleMap addRoute_(List<List<HashMap<String,String>>> result){
		addDestMarker(false);
		addCurMarker();
		
		List<LatLng> points = null;
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

			// Drawing polyline in the Google Map for the i-th route
			map.addPolyline(lineOptions);
		}
		return map;
	}
	
	public void addRouteWithRouteCtr(){
		if(curLoc!=null && dest!=null){
			addDestMarker(false);
			addCurMarker();
			RouteInfoController routeCtr = new RouteInfoController(new LatLng(curLoc.getLatitude(),curLoc.getLongitude()),dest,this);
			routeCtr.execute("");
		}else{
			Log.e(LOG_TAG, "No cur / dest for drawing route");
		}
	}
	
	public LatLng updateIndicatorTarget(){
		return setCurrentLeg();
		
//		For Testing cET()
//		LatLng temp = setCurrentLeg();
//		calEstimatedTime();
//		return temp;
	}
	
	public String getCurrentInstruction(){
		return instructions.get(curRouteIndex).toString();
	}
	//endregion
	
	//region Get Methods
	public Location getCurrentLoc(){
		return curLoc;
	}
	
	public LatLng getCurrent(){
		return new LatLng(curLoc.getLatitude(),curLoc.getLongitude());
	}
	
	public LatLng getDestination(){
		return dest;
	}
	
	public String getWarning(){
		return warning==null?"":warning;
	}
	
	public int getEstimatedTime(){
		return calEstimatedTime();
	}
	//endregion

	//region Set Methods
	public GoogleMap setCurrent(Location loc){
		curLoc = loc;
		return map;
	}
	
	public GoogleMap setDestination(double lat,double lng){
		return setDestination(new LatLng(lat,lng));
	}
	
	public GoogleMap setDestination(LatLng point){
		dest = point;
		return map;
	}
	//endregion
	
	//region private methods 
	private void addDestMarker(boolean draggable){
		if(dest!=null){
			CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(dest);
			CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(12);
	
			// Showing the user input location in the Google Map
			map.moveCamera(cameraPosition);
	//		map.animateCamera(cameraZoom);
	
			// Destination Marker
			MarkerOptions destMark = new MarkerOptions();
			destMark.position(dest);
			destMark.draggable(draggable);
			destMark.icon(MARKER_ICON);
//			destMark.title("Position");
//			destMark.snippet("Latitude:"+dest.latitude+",Longitude:"+dest.longitude);
	
			// Adding the marker in the Google Map
			map.clear();
			map.addMarker(destMark);
		}else{
			Log.e(LOG_TAG, "Dest is null @addDestMarker()");
		}
	}
	
	private void addCurMarker(){
		if(curLoc!=null){
			// Current Marker
			MarkerOptions curMark = new MarkerOptions();
			curMark.position(new LatLng(curLoc.getLatitude(),curLoc.getLongitude()));
			curMark.icon(MARKER_ICON);
			map.addMarker(curMark);
		}else{
			Log.e(LOG_TAG, "CurLoc is null @addCurMarker()");
		}
	}
		
	private void drawRoute(List<List<HashMap<String,String>>> result){

		List<LatLng> points = null;
		PolylineOptions lineOptions = null;
		
		// Traversing through all the routes
		for(int i=0;i<result.size();i++){
//			if(i==1)curRouteIndex=i;

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
			
			routes = points;
			// Drawing polyline in the Google Map for the i-th route
			map.addPolyline(lineOptions);
		}
	}

	private int calEstimatedTime(){
		int time =0;
		for(int i=durations.size()-1; i>=curRouteIndex;i--){
			String cur_dur = durations.get(i);
//			Log.e(LOG_TAG, "After substring:"+cur_dur.substring(0, cur_dur.indexOf(' ')));
			time += Integer.parseInt(cur_dur.substring(0, cur_dur.indexOf(' ')));
		}
		return time;
	}
	
	private LatLngBounds getCurRouteBound(){
//		return new LatLngBounds(routes.get(curRouteIndex),routes.get(curRouteIndex+1));
		return LatLngBounds.builder().include(routes.get(curRouteIndex)).include(routes.get(curRouteIndex+1)).build();
	}
	
	private LatLng setCurrentLeg(){
		Log.e(LOG_TAG, "before getCurBound()");
		if(!getCurRouteBound().contains(getCurrent())){
//			Method 1
//			int ind1 = routes.indexOf(curLeg.northeast);
//			int ind2 = routes.indexOf(curLeg.southwest);
//			int index = ind1>ind2?ind1:ind2;
//			curLeg = new LatLngBounds(routes.get(index), routes.get(index+1));
//			return routes.get(index+1);
			
			curRouteIndex=curRouteIndex+1>routes.size()?curRouteIndex:curRouteIndex++;			
		}
//		Log.e("current instruction:",instructions.get(curRouteIndex));
		return routes.get(curRouteIndex+1);
	}
	
	//endregion

	//region override methods of AsyncTaskListener
	@Override
	public void onTaskComplete(String result) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onTaskComplete(LatLng result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskComplete(List<List<HashMap<String, String>>> result,List<String>instructions,List<String>durations,String google_warning) {
		drawRoute(result);
		this.instructions = instructions;
		this.durations = durations;
		this.warning = google_warning;
		listener.onMapHlrLocationChanged(map);
	}
	//endregion
}
