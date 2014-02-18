package com.example.zroad_t1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.slidinglayer.SlidingLayer;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.wikitude.architect.SensorAccuracyChangeListener;
import com.zroad.interfaces.AsyncTaskListener;
import com.zroad.location.ILocationProvider;
import com.zroad.location.LocationProvider;
import com.zroad.utils.Constants;
import com.zroad.utils.MapHandler;
import com.zroad.utils.RouteInfoController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionDestActivity extends FragmentActivity implements AsyncTaskListener {

	//region Data Members
	protected ArchitectView architectView;
	protected SensorAccuracyChangeListener	sensorAccuracyListener;
	protected Location lastKnownLocaton;
	protected ILocationProvider locationProvider;
	protected LocationListener locationListener;
	protected String EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL = "Webservice"+ File.separator +"index.html";

    //sliding layout
    private SlidingLayer mSlidingLayer;
    private String mStickContainerToRightLeftOrMiddle;
    private boolean mShowShadow;
    private boolean mShowOffset;
    
    //Google Maps
	private GoogleMap map;
	private MapHandler mapHlr; 
	private LatLng cur;
	private LatLng dest = new LatLng(22.321840251346423,114.25903029505503);
	
	private final LatLng DEFAULT_CENTER = new LatLng(22.334341616815642,114.17366262290966);
	
    //endregion
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_direction_dest);
		this.setTitle("Zroad");
		
		Bundle bundle = this.getIntent().getParcelableExtra("bundle");
		dest = bundle.getParcelable("destination");
		Log.e("DDA", "Dest: "+dest.latitude+","+dest.longitude);

		//region init sliding layout
		getPrefs();
        bindViews();
        initState();
        // endregion
        
		//region init architectView
		this.architectView = (ArchitectView)this.findViewById( R.id.architectView );
		final ArchitectConfig config = new ArchitectConfig( Constants.WIKITUDE_SDK_KEY );
		this.architectView.onCreate( config );
		this.sensorAccuracyListener = new SensorAccuracyChangeListener() {
			@Override
			public void onCompassAccuracyChanged( int accuracy ) {
				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, Height = 3 */
				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && DirectionDestActivity.this != null && !DirectionDestActivity.this.isFinishing() ) {
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

		this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
		this.locationProvider = new LocationProvider( this, this.locationListener );
		// endregion
		
		//region init map components
		initMap(this);
		//endregion

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

	//region listeners

	@Override
	public void onTaskComplete(ArrayList<Double> result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskComplete(List<List<HashMap<String, String>>> result) {
		map=mapHlr.addRoute(result);
	}
	//endregion
	
	//region reaction mechanism

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
	private void initMap(Context c){
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mapHlr = new MapHandler(map,c);
		map = mapHlr.initMap();
		map = mapHlr.setDest(dest);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER, 14));
		RouteInfoController routeCtr = new RouteInfoController(mapHlr.getCurrent(),mapHlr.getDestination(),this);
		routeCtr.execute("");
//		map = mapHlr.setDest(dest);
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
        
        ImageButton image_btn = (ImageButton) findViewById(R.id.buttonOpen);
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

}
