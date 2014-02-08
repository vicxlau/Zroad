package com.example.zroad_t1;

import com.zroad.utils.PlacesAutoCompleteAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

public class AutocompleteActivity extends Activity implements OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_autocomplete);

	    AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.place_autocomplete);
	    autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.place_autocomplete_adapter_layout));
	    autoCompView.setOnItemClickListener((OnItemClickListener) this);
	}

	@Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.autocomplete, menu);
		return true;
	}
	
}

