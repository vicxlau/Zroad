package com.zroad.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;

public interface AsyncTaskListener {
	public void onTaskComplete(ArrayList<Double> result);
	public void onTaskComplete(List<List<HashMap<String,String>>> result);
}
