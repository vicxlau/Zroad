package com.example.zroad_t1;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.slidinglayer.SlidingLayer;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.wikitude.architect.SensorAccuracyChangeListener;
import com.zroad.location.ILocationProvider;
import com.zroad.location.LocationProvider;
import com.zroad.utils.Constants;

import java.io.File;
import java.io.IOException;

public class BlankActivity extends Activity {

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blank);

        //sliding layout
        getPrefs();
        bindViews();
        initState();

		this.setTitle("Zroad");
		this.architectView = (ArchitectView)this.findViewById( R.id.architectView );
		final ArchitectConfig config = new ArchitectConfig( Constants.WIKITUDE_SDK_KEY );
		this.architectView.onCreate( config );
		this.sensorAccuracyListener = new SensorAccuracyChangeListener() {
			@Override
			public void onCompassAccuracyChanged( int accuracy ) {
				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, Height = 3 */
				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && BlankActivity.this != null && !BlankActivity.this.isFinishing() ) {
					Toast.makeText( BlankActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
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
					BlankActivity.this.lastKnownLocaton = location;
				if ( BlankActivity.this.architectView != null ) {
					if ( location.hasAltitude() ) {
						BlankActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
					} else {
						BlankActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
					}
				}
				}
			}
		};

		this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
		this.locationProvider = new LocationProvider( this, this.locationListener );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.blank, menu);
		return true;
	}


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

	@Override
	protected void onPostCreate( final Bundle savedInstanceState ) {
		super.onPostCreate( savedInstanceState );
		if ( this.architectView != null ) {
			this.architectView.onPostCreate();
		}

		try {
			this.architectView.load( getARchitectWorldPath() );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public String getARchitectWorldPath() {
		return EXTRAS_KEY_ACTIVITY_ARCHITECT_WORLD_URL;
	}

    //sliding layout

    /**
     * View binding
     */
    private void bindViews() {
        mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        swipeText = (TextView) findViewById(R.id.swipeText);
    }

    /**
     * Get current value for preferences
     */
    private void getPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mStickContainerToRightLeftOrMiddle = prefs.getString("layer_location", "right");
        mShowShadow = prefs.getBoolean("layer_has_shadow", true);
        mShowOffset = prefs.getBoolean("layer_has_offset", true);
    }

    /**
     * Initializes the origin state of the layer
     */
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

}
