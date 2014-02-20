package com.example.zroad_t1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.zroad.interfaces.*;

public class SearchDestActivity extends FragmentActivity implements OnItemClickListener, AsyncTaskListener {

	GoogleMap map;
	AutoCompleteTextView autoCompView;
	DestAutoCompleteAdapter autoApt;
	LatLng mapMarker;
//	LatLng bound1 = new LatLng(22.321534053237787,114.16566754456949);
//	LatLng bound2 = new LatLng(22.317067780105184,114.17223359223794);
	
 	LatLng bound1 = new LatLng(22.33872794764549,114.17164560173046);
	LatLng bound2 = new LatLng(22.335770662826743,114.1765164933015);
	
	final LatLng DEFAULT_CENTER = new LatLng(22.334341616815642,114.17366262290966);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_dest);

/*		
		// region init AutoComplete Components in layout
		autoApt = new DestAutoCompleteAdapter(this, R.layout.place_autocomplete_adapter_layout);
		autoCompView = (AutoCompleteTextView) findViewById(R.id.place_autocomplete);
//	    autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.place_autocomplete_adapter_layout));
	    autoCompView.setAdapter(autoApt);
	    autoCompView.setOnItemClickListener((OnItemClickListener) this);
	    ImageButton search_btn = (ImageButton) findViewById(R.id.search_btn);
	    search_btn.setOnClickListener(search_btn_onClickLtr);
	    //endregion
 */	    
	    
	    //region init Map Component
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
	    map.setOnMapClickListener(mapLtr);
	    initMap();
	    //endregion

	}

	//region event listeners
	OnClickListener search_btn_onClickLtr = new OnClickListener(){
        @Override
        public void onClick(View v) {
        	redirect(mapMarker);
        }
    };
	
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
			changeSearchTextView(lat+","+lng);
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
		redirect(dest);
	}
	//endregion
	
	//region private methods
	private void changeSearchTextView(String str){
		autoCompView.setHint(str);
	}
	private void redirect(LatLng dest){
		
		Intent intent = new Intent(getApplicationContext(), DirectionDestActivity.class);
//		Bundle bundle=new Bundle();
//		bundle.putDouble("Lat", lat);
//		bundle.putDouble("Lng", lng);
//		intent.putExtras(bundle);
		
		Bundle args = new Bundle();
		args.putParcelable("destination", dest);
		intent.putExtra("bundle",args);
		startActivity(intent);
	}
	
	private void initMap(){
		// Set the camera to the greatest possible zoom level that includes the bounds
//		map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(bound1,bound2), 10));
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER,16));
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
		options.draggable(true);
		options.snippet("Latitude:"+latitude+",Longitude:"+longitude);

		// Adding the marker in the Google Map
		map.clear();
		map.addMarker(options);

		mapMarker = point;
	}
	//endregion
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);

		// region init Components in Menu
		autoApt = new DestAutoCompleteAdapter(this, R.layout.place_autocomplete_adapter_layout);
		autoCompView = (AutoCompleteTextView) menu.findItem(R.id.place_autocomplete).getActionView();
	    autoCompView.setAdapter(autoApt);
	    autoCompView.setOnItemClickListener((OnItemClickListener) this);
	    
	    ImageButton search_btn = (ImageButton) menu.findItem(R.id.search_btn).getActionView();
	    search_btn.setOnClickListener(search_btn_onClickLtr);
	    //endregion
		
		return true;
	}

	@Override
	public void onTaskComplete(List<List<HashMap<String, String>>> result) {
		// TODO Auto-generated method stub
		
	}
}
