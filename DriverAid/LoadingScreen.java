package com.lyit.project;

import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

@SuppressLint("NewApi")
public class LoadingScreen extends ActionBarActivity {

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading_screen);

		
		
		//Check if device has GPS enable
		PackageManager packageManager = this.getPackageManager();
		boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
		
		
		if(isLocationEnabled(getApplicationContext()))
		{
			Intent intent = new Intent(this, MainMenu.class);
			startActivityForResult(intent, 0);
		}
		else
		{
			//Inform the user GPS is not enable
			new AlertDialog.Builder(this)
		    .setTitle("Error")
		    .setMessage("Please enable GPS & Location services on device")
		    .setIcon(android.R.drawable.ic_dialog_alert)
		     .show();
		}
		//getActionBar().hide();
	}

	public static boolean isLocationEnabled(Context context) {
	    int locationMode = 0;
	    String locationProviders;

	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
	        try {
	            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

	        } catch (SettingNotFoundException e) {
	            e.printStackTrace();
	        }

	        return locationMode != Settings.Secure.LOCATION_MODE_OFF;

	    }else{
	        locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	        return !TextUtils.isEmpty(locationProviders);
	    }


	} 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.loading_screen, menu);
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
