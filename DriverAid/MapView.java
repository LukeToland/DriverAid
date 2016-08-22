package com.lyit.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lyit.project.MainActivity.mylocationListener;

import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MapView extends ActionBarActivity  {

	//Map reference
	GoogleMap map;
	Marker currentMark;
	static String url;
	
	
	//Current location
	static double currLat;
	static double currLon;
	
	//Destination
	private static double destLat;
	private static double destLon;
	private String destLongitude;
	private String destLatitude;
	private String destAddress;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapsInitializer.initialize(this.getApplicationContext());
		setTitle("Journey Planner");
		setContentView(R.layout.activity_map_view);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
		

		LocationManager ls = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener li = new mylocationListener();
		ls.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, li);

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();	
	

		//When map is created, zoom in on current location
		zoomIn();
		
		//OK button click
		Button btnAddress = (Button) findViewById(R.id.btnAddress);
		
		
		//Speed click
		btnAddress.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				try{
					//Latitude
					EditText txtLat = (EditText)findViewById(R.id.txtLat);
					destLatitude = txtLat.getText().toString();
					//getLatLongFromAddress(value);

					//Latitude
					EditText txtLong = (EditText)findViewById(R.id.txtLong);
					destLongitude = txtLong.getText().toString();

					url = makeURL(currLat+"", currLon+"", destLatitude+"", destLongitude+"");

					//Destination marker
					LatLng dest = new LatLng(Double.parseDouble(destLatitude), Double.parseDouble(destLongitude));
					map.addMarker(new MarkerOptions()
					.position(dest)
					.title("Destination"));

					new pathAsync(url).execute();
				}catch(Exception e){
					//Inform the user GPS is not enable
					new AlertDialog.Builder(MapView.this)
					.setTitle("Error")
					.setMessage("Please try again")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
				}
			}			
		});

	}
	//Location listener
	//When location changes, zoom in on current location
	class mylocationListener implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			currLat = location.getLatitude();
			currLon = location.getLongitude();

			zoomIn();

			//Get url
			if(destLongitude != "" && destLatitude!=""){
				//url = makeURL(currLat+"", currLon+"", destLongitude, destLatitude);
				//new connectAsyncTask(url).execute();
				
				if((currLat+"" == destLatitude)&& (currLon+"" == destLongitude))
				{
					//Show option pane to save journey
					
				}
			}
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_view, menu);
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

	/*
	 * Pass address in
	 * Retrieve geopoints
	 */
	public static void getLatLongFromAddress(String destination) {
		String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
				destination + "&sensor=false";
		HttpGet httpGet = new HttpGet(uri);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(stringBuilder.toString());

			destLat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lng");

			destLon = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lat");
			
			url = makeURL(currLat+"", currLon+"", destLat+"", destLon+"");

			new MapView().new connectAsyncTask(url);

//			Log.d("latitude", "" + lat);
//			Log.d("longitude", "" + lng);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	//Draw path
	public static String makeURL (String sourcelat, String sourcelog, String destlat, String destlog ){
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");// from
		urlString.append(sourcelat);
		urlString.append(",");
		urlString
		.append(( sourcelog));
		urlString.append("&destination=");// to
		urlString
		.append(( destlat));
		urlString.append(",");
		urlString.append(( destlog));
		urlString.append("&sensor=false&mode=driving&alternatives=true");
		return urlString.toString();
	}

	public void drawPath(String  result) {

		try {
			//Tranform the string into a json object
			final JSONObject json = new JSONObject(result);
			JSONArray routeArray = json.getJSONArray("routes");
			JSONObject routes = routeArray.getJSONObject(0);
			JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
			String encodedString = overviewPolylines.getString("points");
			List<LatLng> list = decodePoly(encodedString);

			for(int z = 0; z<list.size()-1;z++){
				LatLng src= list.get(z);
				LatLng dest= list.get(z+1);
				Polyline line = map.addPolyline(new PolylineOptions()
				.add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
				.width(2)
				.color(Color.BLUE).geodesic(true));
			}

		} 
		catch (JSONException e) {

		}
	} 

	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng( (((double) lat / 1E5)),
					(((double) lng / 1E5) ));
			poly.add(p);
		}

		return poly;
	}

	class connectAsyncTask extends AsyncTask<Void, Void, String>{
		private ProgressDialog progressDialog;
		String url;
		connectAsyncTask(String urlPass){
			url = urlPass;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			//progressDialog = new ProgressDialog(MapView.this);
			//progressDialog.setMessage("Fetching route, Please wait...");
			//progressDialog.setIndeterminate(true);
			//progressDialog.show();
		}
		@Override
		protected String doInBackground(Void... params) {
			JSONParser jParser = new JSONParser();
			String json = jParser.getJSONFromUrl(url);
			return json;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);   
			/*if (progressDialog != null) {
	            if (progressDialog.isShowing()) {
	            	progressDialog.dismiss();
	            	progressDialog = null;
	            }
	        }*/

			if(result!=null){
				drawPath(result);
			}
		}
	}
	
	class pathAsync extends AsyncTask<Void, Void, String>{
		private ProgressDialog progressDialog;
		String url;
		pathAsync(String urlPass){
			url = urlPass;
		}
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = new ProgressDialog(MapView.this);
			progressDialog.setMessage("Fetching route, Please wait...");
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}
		@Override
		protected String doInBackground(Void... params) {
			JSONParser jParser = new JSONParser();
			String json = jParser.getJSONFromUrl(url);
			return json;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);   
			if (progressDialog != null) {
	            if (progressDialog.isShowing()) {
	            	progressDialog.dismiss();
	            	progressDialog = null;
	            }
	        }

			if(result!=null){
				drawPath(result);
			}
		}
	}




	private Location getMyLocation() {
		// Get location from GPS if it's available
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		// Location wasn't found, check the next most accurate place for the current location
		if (myLocation == null) {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			// Finds a provider that matches the criteria
			String provider = lm.getBestProvider(criteria, true);
			// Use the provider to get the last known location
			myLocation = lm.getLastKnownLocation(provider);
		}

		return myLocation;
	}

	//Zoom to current location
	/*
	 * The following method contains try/catch clauses as during testing an error occurred on a 
	 * testing device which needed to have Google Play services updated
	 */
	public void zoomIn()
	{
		
		/*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();

		Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));*/
		Location location = getMyLocation();
		if (location != null)
		{
			try{
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(location.getLatitude(), location.getLongitude()), 13));

				CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
				.zoom(13)                   // Sets the zoom
				.bearing(360)                // Sets the orientation of the camera to east
				.tilt(50)                   // Sets the tilt of the camera to 50 degrees
				.build();                   // Creates a CameraPosition from the builder
				map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


				//Animate marker on location change
				LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
				if(currentMark!=null)
				{
					currentMark.setPosition(ltlng);
				}
				else
				{
					/*
					 * 1. Get current location 
					 * 2. Add marker to map
					 */
					currentMark = map.addMarker(new MarkerOptions()
					.position(ltlng)
					.title("Current location"));				
				}
			}catch(Exception e)
			{

			}
		}
		else
		{
			Location curLocation = getMyLocation();
			try{
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(curLocation.getLatitude(), curLocation.getLongitude()), 13));

				CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(curLocation.getLatitude(), curLocation.getLongitude()))      // Sets the center of the map to location user
				.zoom(13)                   // Sets the zoom
				.bearing(360)                // Sets the orientation of the camera to east
				.tilt(50)                   // Sets the tilt of the camera to 50 degrees
				.build();                   // Creates a CameraPosition from the builder
				map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

				//Source Marker
				/*LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
				map.addMarker(new MarkerOptions()
				.position(ltlng)
				.title("Current location"));*/

				//Animate marker on location change
				LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
				if(currentMark!=null)
				{
					currentMark.setPosition(ltlng);
				}
				else
				{
					/*
					 * 1. Get current location 
					 * 2. Add marker to map
					 */
					currentMark = map.addMarker(new MarkerOptions()
					.position(ltlng)
					.title("Current location"));				
				}

			}catch(Exception e){

			}
		}
		//createRoute();
	}
}