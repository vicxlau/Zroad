package com.zroad.interfaces;

import java.util.HashMap;
import java.util.List;
import com.google.android.gms.maps.model.LatLng;

public interface AsyncTaskListener {
	public void onTaskComplete(LatLng result);
	public void onTaskComplete(String result);
	public void onTaskComplete(List<List<HashMap<String,String>>> result,List<String>instructions,List<String>durations,String google_warning);
}
