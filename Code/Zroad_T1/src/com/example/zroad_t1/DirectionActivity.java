package com.example.zroad_t1;

import com.example.zroad_t1.util.SystemUiHider;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.slidinglayer.SlidingLayer;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.wikitude.architect.SensorAccuracyChangeListener;
import com.zroad.location.ILocationProvider;
import com.zroad.location.LocationProvider;
import com.zroad.utils.Constants;
import com.zroad.utils.DirectionsJSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class DirectionActivity extends FragmentActivity {

	//region Data Members
	protected ArchitectView architectView;
	protected SensorAccuracyChangeListener	sensorAccuracyListener;
	protected Location lastKnownLocaton;
	protected ILocationProvider locationProvider;
	protected LocationListener locationListener;
	protected String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "Webservice"+ File.separator +"index.html";

    //sliding layout
    private SlidingLayer mSlidingLayer;
    private TextView swipeText;
    private String mStickContainerToRightLeftOrMiddle;
    private boolean mShowShadow;
    private boolean mShowOffset;
    
    //Google Maps
	private GoogleMap map;
	
	// sample location
	/*
	    private static final LatLng MELBOURNE = new LatLng(-37.81319, 144.96298);
	    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);
	    private static final LatLng ADELAIDE = new LatLng(-34.92873, 138.59995);
	    private static final LatLng PERTH = new LatLng(-31.95285, 115.85734);
	
	    private static final LatLng LHR = new LatLng(51.471547, -0.460052);
	    private static final LatLng LAX = new LatLng(33.936524, -118.377686);
	    private static final LatLng JFK = new LatLng(40.641051, -73.777485);
	    private static final LatLng AKL = new LatLng(-37.006254, 174.783018);
	 */
    private static final int WIDTH_MAX = 50;
    private static final int HUE_MAX = 360;
    private static final int ALPHA_MAX = 255;

    private Polyline mMutablePolyline;
    private SeekBar mColorBar;
    private SeekBar mAlphaBar;
    private SeekBar mWidthBar;
    
    // Draw walking route
    private int mMode=0;
    ArrayList<LatLng> markerPoints;
    

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	//If set, will toggle the system UI visibility upon interaction. Otherwise,
	//will show the system UI visibility upon interaction.
	 
	private static final boolean TOGGLE_ON_CLICK = true;

	//The flags to pass to {@link SystemUiHider#getInstance}.
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	//The instance of the {@link SystemUiHider} for this activity.
	private SystemUiHider mSystemUiHider;

    //endregion
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction);
		
		//region fullscreen setups
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		
		//endregion
		
        //region init sliding layout
        getPrefs();
        bindViews();
        initState();
        // endregion
        
        //initMap();
        
		//region init architectView
		this.architectView = (ArchitectView)this.findViewById( R.id.architectView );
		final ArchitectConfig config = new ArchitectConfig( Constants.WIKITUDE_SDK_KEY );
		this.architectView.onCreate( config );
		this.sensorAccuracyListener = new SensorAccuracyChangeListener() {
			@Override
			public void onCompassAccuracyChanged( int accuracy ) {
				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, Height = 3 */
				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && DirectionActivity.this != null && !DirectionActivity.this.isFinishing() ) {
					Toast.makeText( DirectionActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
				}
			}
		};
		this.locationListener = new LocationListener() {

			@Override
			public void onStatusChanged( String provider, int status, Bundle extras ) {
			}

			@Override
			public void onProviderEnabled( String provider ) {
			}

			@Override
			public void onProviderDisabled( String provider ) {
			}

			@Override
			public void onLocationChanged( final Location location ) {
				if (location!=null) {
					DirectionActivity.this.lastKnownLocaton = location;
				if ( DirectionActivity.this.architectView != null ) {
					if ( location.hasAltitude() ) {
						DirectionActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
					} else {
						DirectionActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
					}
				}
				}
			}
		};

		this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
		this.locationProvider = new LocationProvider( this, this.locationListener );
		// endregion

		//region init search locations
		initMarkerPoints();
		//endregion
		
		//region init map components
		initMap();
		// Setting onclick event listener for the map
		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				Log.i("LatLng Info", "Lat: "+point.latitude+"; Lng: "+point.longitude);
				/*

				// Already two locations
				if(markerPoints.size()>1){
					markerPoints.clear();
					map.clear();
				}

				// Adding new item to the ArrayList
				markerPoints.add(point);

				// Draws Start and Stop markers on the Google Map
				drawStartEndMarkers();

				// Checks, whether start and end locations are captured
				if(markerPoints.size() >= 2){
					LatLng origin = markerPoints.get(0);
					LatLng dest = markerPoints.get(1);

					// Getting URL to the Google Directions API
					String url = getDirectionsUrl(origin, dest);

					DownloadTask downloadTask = new DownloadTask();

					// Start downloading json data from Google Directions API
					downloadTask.execute(url);
				}
				 */	
			}
		});
		//endregion

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if ( this.architectView != null ) {
			this.architectView.onPostCreate();
		}

		try {
			this.architectView.load( getARchitectWorldPath() );
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}


	//region reaction mechanism
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.blank, menu);
		return true;
	}
	*/
	@Override
	protected void onResume() {
		super.onResume();
		if ( this.architectView != null ) {
			this.architectView.onResume();
		}

		if ( this.locationProvider != null ) {
			this.locationProvider.onResume();
		}

        //sliding layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}

	@Override
	protected void onPause() {
		super.onPause();
		if ( this.architectView != null ) {
			this.architectView.onPause();
		}
		if ( this.locationProvider != null ) {
			this.locationProvider.onPause();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if ( this.architectView != null ) {
			if ( this.sensorAccuracyListener != null ) {
				this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
			}
			this.architectView.onDestroy();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if ( this.architectView != null ) {
			this.architectView.onLowMemory();
		}
	}

	//endregion

	//region ArchitectView Setup
	public String getARchitectWorldPath() {
		return EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL;
	}

	//endregion
	
    //region Map Path & Marker Setups
	private void initMap(){
		// Getting reference to SupportMapFragment of the activity_main
		SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

		// Getting Map for the SupportMapFragment
		map = fm.getMap();

		// Enable MyLocation Button in the Map
		map.setMyLocationEnabled(true);

		// Draws Start and Stop markers on the Google Map
		drawStartEndMarkers();

		// Checks, whether start and end locations are captured
		if(markerPoints.size() >= 2){
			LatLng origin = markerPoints.get(0);
			LatLng dest = markerPoints.get(1);

			// Getting URL to the Google Directions API
			String url = getDirectionsUrl(origin, dest);

			DownloadTask downloadTask = new DownloadTask();

			// Start downloading json data from Google Directions API
			downloadTask.execute(url);
			
			// move Map Camera
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLngBounds(markerPoints.get(0),markerPoints.get(1)).getCenter(), 10));
		    //map.animateCamera(cameraUpdate);
		    //locationManager.removeUpdates(this);
		}else if(markerPoints.size()==1){
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPoints.get(0),15));
		}
	}
	
	private void initMarkerPoints(){
		markerPoints = new ArrayList<LatLng>();
		
//		markerPoints.add(new LatLng(22.318196209668695,114.16893046349287));
//		markerPoints.add(new LatLng(22.318330198858924,114.1697109863162));
		
//		markerPoints.add(new LatLng(22.315196306899683,114.17118653655052));
//		markerPoints.add(new LatLng(22.323395019978566,114.16967008262873));
		
		LocationListener locLtr = new LocationListener(){
        	@Override
			public void onLocationChanged(Location loc) {
				markerPoints.add(new LatLng(loc.getLatitude(),loc.getLongitude()));
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
        LocationManager curLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
//        String curLocProv = curLocMgr.getBestProvider(criteria, true); // fail
        String curLocProv = LocationManager.NETWORK_PROVIDER;
        Location curLoc = curLocMgr.getLastKnownLocation(curLocProv);
//        Location curLoc = curLocMgr.getLastKnownLocation("gps");
        
        while(curLoc == null) { 
//        	curLocMgr.requestLocationUpdates("gps", 60000, 1, locLtr); 
        	curLocMgr.requestLocationUpdates(curLocProv, 60000, 1, locLtr); 
        	Log.e("Current Location", "Current Location is null");
        }
        
        if(curLoc!=null)
        	Log.i("Current Location", "Cur Lat: "+curLoc.getLatitude()+"; Cur Lng: "+curLoc.getLongitude());
		
        //fail klt
//        markerPoints.add(new LatLng(22.336059989897162,114.17529165744781));
        
//        map.setMyLocationEnabled(true);
//        Location curLoc = map.getMyLocation();
        
        Log.i("Get Current Location", "Lat: "+curLoc.getLatitude()+"; Lng: "+curLoc.getLongitude());
		markerPoints.add(new LatLng(curLoc.getLatitude(),curLoc.getLongitude()));
	}
	
    private void drawStartEndMarkers(){
    	for(int i=0;i<markerPoints.size();i++){
    		MarkerOptions opt = new MarkerOptions();
    		opt.position(markerPoints.get(i));

			if(i==0){
				opt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
			}else if(i==1){
				opt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			}

			// Add new marker to the Google Map Android API V2
			map.addMarker(opt);
    	}
    }

    private String getDirectionsUrl(LatLng ori,LatLng dest){
    	String result = "https://maps.googleapis.com/maps/api/directions/json?origin="
    					+ori.latitude+","+ori.longitude
    					+"&destination="
    					+dest.latitude+","+dest.longitude
    					+"&sensor=false"					
						+"&mode=walking";
    	return result;
    }
    
    private String downloadJson(String strUrl){
    	String data = "";
    	InputStream iStream = null;
    	HttpURLConnection urlCon = null;
    	try{
    		URL url = new URL(strUrl);
    		urlCon = (HttpURLConnection) url.openConnection();
    		urlCon.connect();
    		iStream = urlCon.getInputStream();
    		
    		BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
    		StringBuffer sb = new StringBuffer();
    		String line = "";
    		while((line=br.readLine())!= null){
    			sb.append(line);
    		}
    		data = sb.toString();
    		br.close();
    		
    		iStream.close();
    	}catch(Exception e){
    		Log.d("Exception while donwload url", e.toString()); 
    	}finally{
    		urlCon.disconnect();
    	}
		return data;
    }
    
    //endregion
    
    //region sliding layout methods

    //View binding
    private void bindViews() {
        mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        swipeText = (TextView) findViewById(R.id.swipeText);
    }

    //Get current value for preferences
    private void getPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mStickContainerToRightLeftOrMiddle = prefs.getString("layer_location", "right");
        mShowShadow = prefs.getBoolean("layer_has_shadow", true);
        mShowOffset = prefs.getBoolean("layer_has_offset", true);
    }

    //Initializes the origin state of the layer
    private void initState() {

        // Sticks container to right or left
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mSlidingLayer.getLayoutParams();
        int textResource;
        Drawable d;

        if (mStickContainerToRightLeftOrMiddle.equals("right")) {
            textResource = R.string.app_name;
            d = getResources().getDrawable(R.drawable.ic_launcher);

            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else if (mStickContainerToRightLeftOrMiddle.equals("left")) {
            textResource = R.string.swipe_left_label;
            d = getResources().getDrawable(R.drawable.container_rocket_left);

            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else if (mStickContainerToRightLeftOrMiddle.equals("top")) {
            textResource = R.string.swipe_up_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_TOP);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rlp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            rlp.height = getResources().getDimensionPixelSize(R.dimen.layer_width);
        } else if (mStickContainerToRightLeftOrMiddle.equals("bottom")) {
            textResource = R.string.swipe_down_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            mSlidingLayer.setStickTo(SlidingLayer.STICK_TO_BOTTOM);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rlp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
            rlp.height = getResources().getDimensionPixelSize(R.dimen.layer_width);
        } else {
            textResource = R.string.swipe_label;
            d = getResources().getDrawable(R.drawable.container_rocket);

            rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
            rlp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }

        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        swipeText.setCompoundDrawables(null, d, null, null);
        swipeText.setText(getResources().getString(textResource));
        mSlidingLayer.setLayoutParams(rlp);

        // Sets the shadow of the container
        if (mShowShadow) {
            mSlidingLayer.setShadowWidthRes(R.dimen.shadow_width);
            mSlidingLayer.setShadowDrawable(R.drawable.sidebar_shadow);
        } else {
            mSlidingLayer.setShadowWidth(0);
            mSlidingLayer.setShadowDrawable(null);
        }
        if(mShowOffset) {
            mSlidingLayer.setOffsetWidth(getResources().getDimensionPixelOffset(R.dimen.offset_width));
        } else {
            mSlidingLayer.setOffsetWidth(0);
        }
    }

    public void buttonClicked(View v) {
        switch (v.getId()) {
            case R.id.buttonOpen:
                if (!mSlidingLayer.isOpened()) {
                    mSlidingLayer.openLayer(true);
                }
                break;
            case R.id.buttonClose:
                if (mSlidingLayer.isOpened()) {
                    mSlidingLayer.closeLayer(true);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mSlidingLayer.isOpened()) {
                    mSlidingLayer.closeLayer(true);
                    return true;
                }

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    //endregion

    //region methods for listeners
	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
	//endregion

    //Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

    	//Downloading data in non-ui thread
    	@Override
    	protected String doInBackground(String... url){
    		String data="";
    		try{
    			data=downloadJson(url[0]);
    		}catch(Exception e){
    			Log.d("Background Task", e.toString());
    		}
    		return data;
    	}
    	
    	//Executes in UI thread, after the execution of
    	//doInBackground()
    	@Override
    	protected void onPostExecute(String result){
    		
    		super.onPostExecute(result);
    		
    		ParserTask parserTask = new ParserTask();
    		
    		//Invokes the thread for parsing the JSON data
    		parserTask.execute(result);
    		
    	}
    }

    //A class to parse the Google Places in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

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
    				Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
    				return;
    			}

    			// Drawing polyline in the Google Map for the i-th route
    			map.addPolyline(lineOptions);
    		}
    	}
    }    	
	
	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
}
