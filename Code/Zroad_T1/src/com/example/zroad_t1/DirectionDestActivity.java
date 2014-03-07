package com.example.zroad_t1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.slidinglayer.SlidingLayer;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.zroad.interfaces.MapHandlerListener;
import com.zroad.utils.Constants;
import com.zroad.utils.MapHandler;

import java.io.File;
import java.io.IOException;

public class DirectionDestActivity extends FragmentActivity implements MapHandlerListener, LocationListener, SensorEventListener {

	//region Data Members
	protected ArchitectView architectView;
//	protected SensorAccuracyChangeListener	sensorAccuracyListener;
//	protected Location lastKnownLocaton;
//	protected ILocationProvider locationProvider;
//	protected LocationListener locationListener;
	protected String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "Webservice"+ File.separator +"index.html";

    private LocationManager locationManager;

    private SensorManager sensorManager;
//    private Sensor sensorMagneticField;
     
    //sliding layout
    private SlidingLayer mSlidingLayer;
    private ImageButton image_btn;
    private String mStickContainerToRightLeftOrMiddle;
    private boolean mShowShadow;
    private boolean mShowOffset;
    private TextView estimated_time;

//    private DrawerLayout mDrawerLayout;
//    private ListView mDrawerList;
//    private ActionBarDrawerToggle mDrawerToggle;
    
    //Google Maps
	private GoogleMap map;
	private TextView instruct;
	private MapHandler mapHlr;
	private LatLng dest;
	private int counter = 1 ;
	private double bearing_to_target;
	private LatLng curIndicatorTarget;
	private Boolean flagLocInit = true;
	
	//= new LatLng(22.321840251346423,114.25903029505503);
	
	private final LatLng DEFAULT_CENTER = Constants.MAP_DEFAULT_CENTER;
	
    //endregion
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction_dest);
		this.setTitle("Zroad");
		
		Bundle bundle = this.getIntent().getParcelableExtra("bundle");
		dest = bundle.getParcelable("destination");
		Log.e("DDA", "Dest: "+dest.latitude+","+dest.longitude);

		//region init HTML instruction
/*		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		 // Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, mPlanetTitles));
		
		mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.button_red,R.string.hello_world,R.string.app_name) 
			{
				  
				// Called when a drawer has settled in a completely closed state.
				 public void onDrawerClosed(View view) {
					 Log.i("HTML instruction", "closed");
				 }
				  
				//Called when a drawer has settled in a completely open state.
				 public void onDrawerOpened(View drawerView) {
					 Log.i("HTML instruction", "closed");
				 }
		 	};
				  
				 // Set the drawer toggle as the DrawerListener
				 mDrawerLayout.setDrawerListener(mDrawerToggle);
//				 getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//				 getSupportActionBar().setHomeButtonEnabled(true);
*/
		//endregion
		
		//region init location settings
//		initLocationSetting();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
Toast.makeText(getApplicationContext(), "is GPS enabled: "+String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)),5000).show();
//		Log.e("is GPS enabled", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));

		//endregion
		
		//region init sliding layout
		getPrefs();
        bindViews();
        initState();
        // endregion
        
		//region init map components
		initMap();
		//endregion
		
		//region init architectView
		this.architectView = (ArchitectView)this.findViewById( R.id.architectView );
		final ArchitectConfig config = new ArchitectConfig( Constants.WIKITUDE_SDK_KEY );
		this.architectView.onCreate( config );
//		this.architectView.setLocation(mapHlr.getCurrent().latitude, mapHlr.getCurrent().longitude, 1000);
/*
		this.sensorAccuracyListener = new SensorAccuracyChangeListener() {
			@Override
			public void onCompassAccuracyChanged( int accuracy ) {
				// UNRELIABLE = 0, LOW = 1, MEDIUM = 2, Height = 3
				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH && DirectionDestActivity.this != null && !DirectionDestActivity.this.isFinishing() ) {
					Toast.makeText( DirectionDestActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
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
					DirectionDestActivity.this.lastKnownLocaton = location;					
					if ( DirectionDestActivity.this.architectView != null ) {
						if ( location.hasAltitude() ) {
							DirectionDestActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
						} else {
							DirectionDestActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
						}
					}
					
					
				}
			}
		};
 */
		//this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
		//this.locationProvider = new LocationProvider( this, this.locationListener );
		// endregion

        // initialize your android device sensor capabilities
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // region HTML instruction
        instruct = (TextView) findViewById(R.id.instruct_txt);
        // endregion
        
        // region old drawers
        
		// region NavDrawer Trial (LEFT/RIGHT ONLY)
        /*
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.drawer_list_item, mPlanetTitles));
//        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
//        image_btn 
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setHomeButtonEnabled(true);
        

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.button_red,R.string.hello_world,R.string.app_name) 
		{
			  
			// Called when a drawer has settled in a completely closed state.
			 public void onDrawerClosed(View view) {
				 Log.i("HTML instruction", "closed");
			 }
			  
			//Called when a drawer has settled in a completely open state.
			 public void onDrawerOpened(View drawerView) {
				 Log.i("HTML instruction", "closed");
			 }
	 	};
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(Gravity.Start);
*/
		//endregion

        //region Sliding Drawer Trial

        /*
        SlidingDrawer instruct = (SlidingDrawer) findViewById(R.id.instruct_drawer);
//        instruct.set
        instruct.open();
        */
        //endregion
        
        // endregion
	}
	
	@Override
	protected void onPostCreate( final Bundle savedInstanceState ) {
		super.onPostCreate( savedInstanceState );
		if ( this.architectView != null ) {
			this.architectView.onPostCreate();
		}

		try {
			this.architectView.load( getARchitectWorldPath() );
			//Log.e("DDAct", getARchitectWorldPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	//region private methods
	private void initLocationSetting(){
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

//		Criteria crta = new Criteria(); 
//		crta.setAccuracy(Criteria.ACCURACY_FINE); 
//		crta.setAltitudeRequired(false); 
//		crta.setBearingRequired(true); 
//		crta.setCostAllowed(false); 
//		crta.setPowerRequirement(Criteria.POWER_LOW); 
//		String provider = locationManager.getBestProvider(crta, true); 

//		Location loc = locationManager.getLastKnownLocation(provider);
//		setARMapComponent(loc.getLatitude(),loc.getLongitude());
		
//		locationManager.requestLocationUpdates( provider,
//		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,

		locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,
                1000,   // 3 sec
                10f, this);
	}
	
	private void setARComponent(Location loc){
		double lat = loc.getLatitude();
		double lng = loc.getLongitude();
		this.architectView.setLocation(lat, lng, 1000);

	}
	//endregion
	
	//region override methods of MapHandlerListener
	@Override
	public void onMapHlrLocationChanged(GoogleMap result){
		map = result;
		curIndicatorTarget = mapHlr.updateIndicatorTarget();
		Toast.makeText(getApplicationContext(), "Estimated Time: "+mapHlr.getEstimatedTime()+"mins", 5000).show();
		Log.e("Init indicatorTarget",curIndicatorTarget.latitude+" VS "+dest.latitude);
//		this.architectView.callJavascript("changeStatus(\""+mapHlr.getWarning()+"\");");

//        Toast.makeText(getBaseContext(),"Return map from MapHlr with route",Toast.LENGTH_LONG).show();
	}
	//endregion
	
	//region override methods of LocationListener
	@Override
	public void onLocationChanged(Location location) {
        setARComponent(location);

		mapHlr.setCurrent(location);
		if(flagLocInit){
			mapHlr.addRouteWithRouteCtr();
			flagLocInit=false;
		}else{
			curIndicatorTarget = mapHlr.updateIndicatorTarget();
			estimated_time.setText(mapHlr.getEstimatedTime()+"mins left");
Toast.makeText(getApplicationContext(), "Estimated Time: "+mapHlr.getEstimatedTime()+"mins", 5000).show();
			instruct.setText(Html.fromHtml(mapHlr.getCurrentInstruction()));
			Log.e("update indicatorTarget",curIndicatorTarget.latitude+" VS "+dest.latitude);

			Location TargetLoc = new Location("destination");
			TargetLoc.setLatitude(curIndicatorTarget.latitude);
			TargetLoc.setLongitude(curIndicatorTarget.longitude);
			
			double lat = location.getLatitude(); 
			double lng = location.getLongitude();
			double dist = location.distanceTo(TargetLoc);
			bearing_to_target = location.bearingTo(TargetLoc);
			Log.e("Loc Changed",
					counter++ + " Bearing:"+bearing_to_target+" Distance: "+dist+" Latitude: "+lat+"Longitude: "+lng
					);
		}
        
		
//        Toast.makeText(getBaseContext(),
//        		,Toast.LENGTH_LONG).show();

        
//        this.architectView.callJavascript("changeStatus('hello');");
//        this.architectView.callJavascript("rotateZroadIndicator("+(counter*20)+");");
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	//endregion

	//region override methods of SensorEventListener
	@Override
	public void onSensorChanged(SensorEvent event) {
//		Log.e("Sensor Changed",mapHlr.getCurrentLoc().);
//	        Log.i("aa", event.values[0]+"");
		

//		Log.e("Sensor Changed",event.values[0]+"");
//	    // get the angle around the z-axis rotated
//	    float degree = Math.round(bearing_to_dest - event.values[0]);
//	    Log.e("Sensor Changed","Heading: " + Float.toString(degree) + " degrees");
//	    this.architectView.callJavascript("rotateZroadIndicator("+degree+");");
		
		synchronized (this) {
		float heading = event.values[0];
//		float heading_round = Math.round(heading);
//		Log.e("Sensor Changed",Float.toString(heading_round)+"===========");
		
	    float degree = Math.round(bearing_to_target-heading);
//	    Log.e("Sensor Changed","Heading: " + degree + " degrees");
	    this.architectView.callJavascript("rotateZroadIndicator("+degree+");");

	    /*
	    // create a rotation animation (reverse turn degree degrees)
	    RotateAnimation ra = new RotateAnimation(
	            currentDegree, 
	            -degree,
	            Animation.RELATIVE_TO_SELF, 0.5f, 
	            Animation.RELATIVE_TO_SELF,
	            0.5f);
	    // how long the animation will take place
	    ra.setDuration(210);
	    // set the animation after the end of the reservation status
	    ra.setFillAfter(true);
	    // Start the animation
	    image.startAnimation(ra);
	    currentDegree = -degree;
	    */
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	//endregion
	
	//region reaction mechanism

	@Override
	protected void onResume() {
		super.onResume();
		if(this.architectView!=null){
			this.architectView.onResume();
		}

		//onResume is is always called after onStart, even if the app hasn't been paused

//		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//		sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		

        // for the system's orientation sensor registered listeners
        
		if(this.sensorManager!=null){
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
					SensorManager.SENSOR_DELAY_GAME);
//			sensorManager.registerListener(this,sensorMagneticField,SensorManager.SENSOR_DELAY_NORMAL);
		}
		if(this.locationManager!=null){
//			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);

//			Criteria crta = new Criteria(); 
//			crta.setAccuracy(Criteria.ACCURACY_FINE); 
//			crta.setAltitudeRequired(false); 
//			crta.setBearingRequired(true); 
//			crta.setCostAllowed(false); 
//			crta.setPowerRequirement(Criteria.POWER_LOW); 
//			String provider = locationManager.getBestProvider(crta, true); 

			
//			Location loc = locationManager.getLastKnownLocation(provider);
//			setARMapComponent(loc.getLatitude(),loc.getLongitude());
			
//			locationManager.requestLocationUpdates( provider,
//			locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 1, this);	
		}
//		if ( this.locationProvider != null ) {
//			this.locationProvider.onResume();
//		}
		
		
		super.onResume();
		
        //sliding layout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(this.architectView!=null){
			this.architectView.onPause();
		}

		if(this.sensorManager!=null){
			sensorManager.unregisterListener(this);
//			sensorManager.unregisterListener(this,sensorMagneticField);
		}
		  
//		if ( this.locationProvider != null ) {
//			this.locationProvider.onPause();
//		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(this.architectView!=null){
//			if ( this.sensorAccuracyListener != null ) {
//				this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
//			}
			this.architectView.onDestroy();
		}
		if(this.sensorManager!=null){
			sensorManager.unregisterListener(this);
//			sensorManager.unregisterListener(this,sensorMagneticField);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if(this.architectView!=null){
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
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mapHlr = new MapHandler(map,this);
		map = mapHlr.initMap();
		mapHlr.setDestination(dest);
		
		/*
		mapHlr = new MapHandler(map,c);
		mapHlr.addRouteWithRouteCtr();
		*/
//		RouteInfoController routeCtr = new RouteInfoController(mapHlr.getCurrent(),mapHlr.getDestination(),this);
//		routeCtr.execute("");
	}
/*	
	private void getCurrentLocation(){
		
		LocationListener locLtr = new LocationListener(){
        	@Override
			public void onLocationChanged(Location loc) {
				cur = new LatLng(loc.getLatitude(),loc.getLongitude());
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
        String curLocProv = LocationManager.NETWORK_PROVIDER;
        Location curLoc = curLocMgr.getLastKnownLocation(curLocProv);
        
        while(curLoc == null) { 
        	curLocMgr.requestLocationUpdates(curLocProv, 60000, 1, locLtr); 
        	Log.e("Current Location", "Current Location is null");
        }
        
        if(curLoc!=null)
        	Log.i("Current Location", "Cur Lat: "+curLoc.getLatitude()+"; Cur Lng: "+curLoc.getLongitude());
		
		cur = new LatLng(curLoc.getLatitude(),curLoc.getLongitude());
	}
*/
    //endregion
    
    //region sliding layout methods

    //View binding
    private void bindViews() {
        mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        
        image_btn = (ImageButton) findViewById(R.id.buttonOpen);
		image_btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg){
				if (!mSlidingLayer.isOpened()) {
                    mSlidingLayer.openLayer(true);
                    findViewById(R.id.buttonOpen).setBackgroundResource(R.drawable.down_arrow);
                }else{
                	mSlidingLayer.closeLayer(true);
                	findViewById(R.id.buttonOpen).setBackgroundResource(R.drawable.up_arrow);
                }
			}
		});
    }

    //Get current value for preferences
    private void getPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mStickContainerToRightLeftOrMiddle = prefs.getString("layer_location", "bottom");
        mShowShadow = prefs.getBoolean("layer_has_shadow", false);
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
        mSlidingLayer.setLayoutParams(rlp);
        mSlidingLayer.setSlidingEnabled(false);

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
                }else{
                  mSlidingLayer.closeLayer(true);
                }
                break;
//            case R.id.buttonClose:
//                if (mSlidingLayer.isOpened()) {
//                    mSlidingLayer.closeLayer(true);
//                }
//                break;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.direction_dest, menu);
		estimated_time = (TextView) menu.findItem(R.id.estimated_time).getActionView();
		estimated_time.setText(R.string.estimated_time_loading_txt);
		
	    return super.onCreateOptionsMenu(menu);
	}
}
