package com.zroad.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zroad.interfaces.AsyncTaskListener;
import com.zroad.interfaces.MapHandlerListener;

public class MapHandler implements AsyncTaskListener {
	
	final String LOG_TAG = "MapHandler";
	
	LatLng bound1 = new LatLng(22.321534053237787,114.16566754456949);
	LatLng bound2 = new LatLng(22.317067780105184,114.17223359223794);
	
	GoogleMap map;
//	Context mapCon;
	Location curLoc;
	LatLng dest;
	MapHandlerListener listener;
	
	public MapHandler(GoogleMap m,MapHandlerListener act){
		map=m;
		listener = (MapHandlerListener)act;
//		mapCon=c;
	}

	public Location getCurrentLoc(){
		return curLoc;
	}
	
	public LatLng getCurrent(){
		return new LatLng(curLoc.getLatitude(),curLoc.getLongitude());
	}
	
	public LatLng getDestination(){
		return dest;
	}
	
	public GoogleMap initMap(){
		// Enable MyLocation Button in the Map
		map.setMyLocationEnabled(true);

		// Set the camera to the greatest possible zoom level that includes the bounds
//		map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(bound1,bound2), 0));
		return map;
	}

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

	public GoogleMap addRoute(List<List<HashMap<String,String>>> result){
		addDestMarker(dest);
		
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

			// Drawing polyline in the Google Map for the i-th route
			map.addPolyline(lineOptions);
		}
		return map;
	}
	
	public void addRouteWithRouteCtr(){
		if(curLoc!=null && dest!=null){
			addDestMarker(dest);
			RouteInfoController routeCtr = new RouteInfoController(new LatLng(curLoc.getLatitude(),curLoc.getLongitude()),dest,this);
			routeCtr.execute("");
		}else{
			Log.e(LOG_TAG, "No cur / dest for drawing route");
		}
	}

	//region private methods 
	private void addDestMarker(LatLng point){

		CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
		CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(12);

		// Showing the user input location in the Google Map
		map.moveCamera(cameraPosition);
//		map.animateCamera(cameraZoom);

		// Destination Marker
		MarkerOptions destMark = new MarkerOptions();
		destMark.position(point);
		destMark.title("Position");
		destMark.snippet("Latitude:"+point.latitude+",Longitude:"+point.longitude);
		
		// Current Marker
		MarkerOptions curMark = new MarkerOptions();
		curMark.position(new LatLng(curLoc.getLatitude(),curLoc.getLongitude()));

		// Adding the marker in the Google Map
		map.clear();
		map.addMarker(curMark);
		map.addMarker(destMark);

	}
		
	private void drawRoute(List<List<HashMap<String,String>>> result){

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
			
			// Drawing polyline in the Google Map for the i-th route
			map.addPolyline(lineOptions);
		}
	}
	//endregion

	//region override methods of AsyncTaskListener
	@Override
	public void onTaskComplete(ArrayList<Double> result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskComplete(List<List<HashMap<String, String>>> result) {
		drawRoute(result);
		listener.onMapHlrLocationChanged(map);
		//cannot go back to DDA!!!!
	}
}
