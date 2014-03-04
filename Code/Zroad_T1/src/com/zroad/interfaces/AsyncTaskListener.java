package com.zroad.interfaces;

import java.util.HashMap;
import java.util.List;

public interface AsyncTaskListener {
	public void onTaskComplete(List<Double> result);
	public void onTaskComplete(List<List<HashMap<String,String>>> result,List<String>instructions);
}
