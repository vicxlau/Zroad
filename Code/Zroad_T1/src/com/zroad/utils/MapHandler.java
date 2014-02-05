package com.zroad.utils;

import android.content.Context;
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

public class MapHandler {
	LatLng bound1 = new LatLng(22.321534053237787,114.16566754456949);
	LatLng bound2 = new LatLng(22.317067780105184,114.17223359223794);
	
	GoogleMap map;
	Context mapCon;
	LatLng cur;
	LatLng dest;
	
	public MapHandler(GoogleMap m,Context c){
		map=m;
		mapCon=c;
		cur=getCurrentLocation();
	}

	public GoogleMap initMap(){
		// Enable MyLocation Button in the Map
		map.setMyLocationEnabled(true);

		// Set the camera to the greatest possible zoom level that includes the bounds
//		map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(bound1,bound2), 0));
		return map;
	}

	public GoogleMap setDest(double lat,double lng){
		addDestMarker(new LatLng(lat,lng));
		return map;
	}
	
	public GoogleMap setDest(LatLng point){
		addDestMarker(point);
		return map;
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
		
		// Destination Marker
		MarkerOptions curMark = new MarkerOptions();
		curMark.position(getCurrentLocation());

		// Adding the marker in the Google Map
		map.clear();
		map.addMarker(curMark);
		map.addMarker(destMark);

	}
	
	private LatLng getCurrentLocation(){
		
		LocationListener locLtr = new LocationListener(){
        	@Override
			public void onLocationChanged(Location loc) {
				Log.i("Location Changed", "New Lat: "+loc.getLatitude()+"; New Lng: "+loc.getLongitude());
//				curLocMgr.requestLocationUpdates(curLocProv, 20000, 0, this);
			}
        	
        	// region unimpletement override methods
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			//endregion
        };
        
		// current location
        LocationManager curLocMgr = (LocationManager) mapCon.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String curLocProv = LocationManager.NETWORK_PROVIDER;
        Location curLoc = curLocMgr.getLastKnownLocation(curLocProv);
        
        while(curLoc == null) { 
        	curLocMgr.requestLocationUpdates(curLocProv, 60000, 1, locLtr); 
        	Log.e("Current Location", "Current Location is null");
        }
        
        if(curLoc!=null)
        	Log.i("Current Location", "Cur Lat: "+curLoc.getLatitude()+"; Cur Lng: "+curLoc.getLongitude());
		
		return new LatLng(curLoc.getLatitude(),curLoc.getLongitude());
	}
	//endregion
}
