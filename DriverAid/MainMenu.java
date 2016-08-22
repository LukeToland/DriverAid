package com.lyit.project;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends ActionBarActivity {
	
	//Buttons
	Button speed;
	Button map;
	Button settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		
		speed = (Button) findViewById(R.id.btnSpeed);
		map = (Button) findViewById(R.id.btnMapView);
		settings = (Button) findViewById(R.id.btnSettings);
		
		
		//Speed click
		speed.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), MainActivity.class);
				startActivityForResult(intent, 0);
				
			}
			
		});
		
		//Map click
		map.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), GMapView.class);
				startActivityForResult(intent, 0);
				
			}
			
		});
		
		//Settings click
		settings.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//Intent intent = new Intent(v.getContext(), GMapView.class);
				//startActivityForResult(intent, 0);
				
			}
			
		});
	}
	
	@Override
	public void onBackPressed()
	{
		//Do nothing
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
