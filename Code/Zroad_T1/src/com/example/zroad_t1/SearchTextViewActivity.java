package com.example.zroad_t1;

//=================================================================================
//Fail version
//This activity does not allow space for showing suggested items
//By Vicx Lau
//=================================================================================


import android.app.Activity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zroad.utils.PlaceDetailsJSONParser;
import com.zroad.utils.PlaceJSONParser;

public class SearchTextViewActivity extends Activity {

	AutoCompleteTextView atvPlaces;

	DownloadTask placesDownloadTask;
	DownloadTask placeDetailsDownloadTask;
	ParserTask placesParserTask;
	ParserTask placeDetailsParserTask;

	final int PLACES=0;
	final int PLACES_DETAILS=1;
	final String API_KEY = "AIzaSyBhfUqAFsL5gjvpwsYTLmfGo9_2f5fAF8Y";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_text_view);

		// Getting a reference to the AutoCompleteTextView
		atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
		atvPlaces.setThreshold(1);

		// Adding textchange listener
		atvPlaces.addTextChangedListener(txtWtr);
		
		atvPlaces.setOnItemClickListener(txtOnItemClkLtr);
	}

	//region URL functions
	private String getAutoCompleteUrl(String place){
		try{
			// Obtain browser key from https://code.google.com/apis/console
			String key = "key="+API_KEY;
			
			// place to be be searched
			String input = "input="+URLEncoder.encode(place,"utf-8");
	
			// place type to be searched
			String types = "types=geocode";
	
			// Sensor enabled
			String sensor = "sensor=false";
	
			// Building the parameters to the web service
			String parameters = input+"&"+sensor+"&"+key;
	
			// Output format
			String output = "json";
	
			// Building the url to the web service
			String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
	
			return url;
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String getPlaceDetailsUrl(String ref){

		// Obtain browser key from https://code.google.com/apis/console
		String key = "key="+API_KEY;

		// reference of place
		String reference = "reference="+ref;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = reference+"&"+sensor+"&"+key;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;

		return url;
	}

	// A method to download json data from url
	private String downloadUrl(String strUrl) throws IOException{
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try{
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while( ( line = br.readLine()) != null){
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		}catch(Exception e){
			Log.d("Exception while downloading url", e.toString());
		}finally{
			iStream.close();
			urlConnection.disconnect();
		}
		
		Log.i("Ajax Place",data);
		return data;
	}
	
	//endregion
	
	//region methods of listeners
	private TextWatcher txtWtr = new TextWatcher(){
		@Override
		public void afterTextChanged(Editable s) {
			Log.i("Dest changed", s.toString());
			placesDownloadTask = new DownloadTask(PLACES);
			String url = getAutoCompleteUrl(s.toString());
			placesDownloadTask.execute(url);
			
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
	};

	private OnItemClickListener txtOnItemClkLtr = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
				long id) {

			ListView lv = (ListView) arg0;
			SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

			HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);

			// Creating a DownloadTask to download Places details of the selected place
			placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

			// Getting url to the Google Places details api
			String url = getPlaceDetailsUrl(hm.get("reference"));

			// Start downloading Google Place Details
			// This causes to execute doInBackground() of DownloadTask class
			placeDetailsDownloadTask.execute(url);

		}
	};
	//endregion
	
	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String>{

		private int downloadType=0;

		// Constructor
		public DownloadTask(int type){
			this.downloadType = type;
		}

		@Override
		protected String doInBackground(String... url) {
Log.i("DownloadTask.doInBackground",url.toString());
			// For storing data from web service
			String data = "";

			try{
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			}catch(Exception e){
				Log.d("Background Task",e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
Log.i("DownloadTask.onPostExecute",result.toString());
			switch(downloadType){
			case PLACES:
				// Creating ParserTask for parsing Google Places
				placesParserTask = new ParserTask(PLACES);

				// Start parsing google places json data
				// This causes to execute doInBackground() of ParserTask class
				placesParserTask.execute(result);

				break;

			case PLACES_DETAILS :
				// Creating ParserTask for parsing Google Places
				placeDetailsParserTask = new ParserTask(PLACES_DETAILS);

				// Starting Parsing the JSON string
				// This causes to execute doInBackground() of ParserTask class
				placeDetailsParserTask.execute(result);
			}
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

		int parserType = 0;

		public ParserTask(int type){
			this.parserType = type;
		}

		@Override
		protected List<HashMap<String, String>> doInBackground(String... jsonData) {
Log.i("ParserTask.doInBackground",jsonData.toString());
			JSONObject jObject;
			List<HashMap<String, String>> list = null;

			try{
				jObject = new JSONObject(jsonData[0]);

				switch(parserType){
				case PLACES :
					PlaceJSONParser placeJsonParser = new PlaceJSONParser();
					// Getting the parsed data as a List construct
					list = placeJsonParser.parse(jObject);
					break;
				case PLACES_DETAILS :
					PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
					// Getting the parsed data as a List construct
					list = placeDetailsJsonParser.parse(jObject);
				}

			}catch(Exception e){
				Log.d("Exception",e.toString());
			}
			return list;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> result) {
			switch(parserType){
			case PLACES :
				Log.i("ParserTask.onPostExecute","PLACES");
				String[] from = new String[] { "description"};
//				int[] to = new int[] { android.R.id.text1 };
				int[] to = new int[] { R.id.txt };

				// Creating a SimpleAdapter for the AutoCompleteTextView
//				SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);
				SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, R.layout.custom_autocomplete_layout, from, to);

				// Setting the adapter
				atvPlaces.setAdapter(adapter);
				
//				TextView txtv1 = (TextView) findViewById(R.id.textView1);
//				txtv1.setText(result.get(0).get("description"));
				
				break;
			case PLACES_DETAILS :
				Log.i("PlaceDetail", result.get(0).get("description"));
/*				
				HashMap<String, String> hm = result.get(0);

				// Getting latitude from the parsed data
				double latitude = Double.parseDouble(hm.get("lat"));

				// Getting longitude from the parsed data
				double longitude = Double.parseDouble(hm.get("lng"));

				LatLng point = new LatLng(latitude, longitude);

				CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
				CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

				MarkerOptions options = new MarkerOptions();
				options.position(point);
				options.title("Position");
				options.snippet("Latitude:"+latitude+",Longitude:"+longitude);
*/
				break;
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_text_view, menu);
		return true;
	}

}
