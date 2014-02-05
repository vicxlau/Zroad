package com.example.zroad_t1;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zroad.utils.DestAutoCompleteAdapter;
import com.zroad.utils.DestInfoController;
import com.zroad.utils.PlacesAutoCompleteAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.zroad.interfaces.*;

public class SearchDestActivity extends FragmentActivity implements OnItemClickListener, AsyncTaskListener {

	GoogleMap map;
	DestAutoCompleteAdapter autoApt;
	LatLng bound1 = new LatLng(22.321534053237787,114.16566754456949);
	LatLng bound2 = new LatLng(22.317067780105184,114.17223359223794);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_dest);

		// Getting componenets from the activity_main.xml
		autoApt = new DestAutoCompleteAdapter(this, R.layout.place_autocomplete_adapter_layout);
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		// region initcComponents
		AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.place_autocomplete);
//	    autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.place_autocomplete_adapter_layout));
	    autoCompView.setAdapter(autoApt);
	    autoCompView.setOnItemClickListener((OnItemClickListener) this);
	    map.setOnMapClickListener(mapLtr);
	    initMap();
	    //endregion

	}

	//region event listeners
	OnMapClickListener mapLtr = new OnMapClickListener() {
		@Override
		public void onMapClick(LatLng point) {
			Double lat = point.latitude;
			Double lng = point.longitude;
Log.i("OnMapClick", "Lat: "+lat+"; Lng: "+lng);
			
			// Delete previous marker on Google Map
			map.clear();
			
			// Draws destination marker on the Google Map
			addDestMarker(lat,lng);
		}
	};
	
	@Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {       
        DestInfoController cont = new DestInfoController(this,position,autoApt.getRefList());
        cont.execute(autoApt.getRefList().get(position));
        
//        String str = (String) adapterView.getItemAtPosition(position);
//        Log.i("OnItemClick",str+";"+autoApt.getRefList().get(position));
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

	@Override
	public void onTaskComplete(ArrayList<Double> result){
//		Log.i("OnTaskClick","Dest: "+result.get(0) +","+ result.get(1));
		Double lat = result.get(0); 
		Double lng = result.get(1);
		Log.e("OnTaskClick","Dest: "+lat+","+lng);
//		addDestMarker(lat,lng);
		LatLng dest = new LatLng(lat,lng);
		
		// Start Direction Activity
		Intent intent = new Intent(getApplicationContext(), BlankActivity.class);
		intent.putExtra("destination",dest);
		startActivity(intent);
	}
	//endregion
	
	//region private methods
	private void initMap(){
		// Set the camera to the greatest possible zoom level that includes the bounds
		map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(bound1,bound2), 0));	
	}
	
	private void addDestMarker(double latitude,double longitude){

		LatLng point = new LatLng(latitude, longitude);

		CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
		CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(12);

		// Showing the user input location in the Google Map
		map.moveCamera(cameraPosition);
//		map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPoints.get(0),15));
//		map.animateCamera(cameraZoom);

		MarkerOptions options = new MarkerOptions();
		options.position(point);
		options.title("Position");
		options.snippet("Latitude:"+latitude+",Longitude:"+longitude);

		// Adding the marker in the Google Map
		map.clear();
		map.addMarker(options);

	}
	//endregion
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.current_poi, menu);
		return true;
	}

}
