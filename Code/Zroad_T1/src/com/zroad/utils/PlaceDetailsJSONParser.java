package com.zroad.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaceDetailsJSONParser {

	public List<HashMap<String,String>> parse(JSONObject jObj){
		Double lat = Double.valueOf(0);
		Double lng = Double.valueOf(0);
		
		HashMap<String,String> hm = new HashMap<String,String>();
		List<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		
		try{
			lat = (Double)jObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat");
			lng = (Double)jObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng");
		}catch(JSONException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		hm.put("lat", Double.toString(lat));
		hm.put("lng", Double.toString(lng));
		list.add(hm);
		return list;
	}
}
