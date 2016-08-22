package com.lyit.project;

import android.R.layout;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.ActionBarActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.os.*;
import android.provider.DocumentsContract.Document;

import org.apache.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MainActivity extends ActionBarActivity implements OnInitListener {
	
	//Form fields
	TextView txtLat;
	TextView txtLong;
	TextView txtSpeed;
	TextView txtcurLimit;
	TextView txtRoadName;
	TextView txtRoadRef;
	
	//Current speed 
	double currentSpeed;
	static String currentSpeedLimit;
	static String currentRoadName;
	static String tempZone;
	static String tempRoad;
	//Queries
	private static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter?data=";
	//String query = "[out:json];way(around:20,54.950980649739634,-7.721457478884304)[\"highway\"];(._;node(w););out;";
	String encodedQuery;
	String submission;
	
	TextToSpeech tts;
	
	public MainActivity()
	{
		
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Speedometer");
		setContentView(R.layout.activity_main);

		txtSpeed = (TextView) findViewById(R.id.carSpeedTxt);
		txtRoadName = (TextView) findViewById(R.id.txtRoadName);
		txtRoadRef = (TextView) findViewById(R.id.txtRoadRef);
		txtcurLimit = (TextView) findViewById(R.id.curLimitTxt);
		
		
		//Location manager for getting location specific information
		LocationManager ls = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener li = new mylocationListener();
		ls.requestLocationUpdates(LocationManager.GPS_PROVIDER, 8000, 0, li);//Gets speed limit every 8 seconds 
		
		
		//Location manager for getting current speed
		LocationManager ss = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener si = new speedListener();
		ss.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, si);//Gets speed every second - More accuracy
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	
	//Location listener for speed
	class speedListener implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			
			//Get speed, then change it to km/h
			double speed = location.getSpeed();
			currentSpeed =(int) ((speed*3600)/1000);
			txtSpeed.setText(currentSpeed + " km/h");
			
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



	//Location listener for speed limits
	class mylocationListener implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location) {
			if(location != null)
			{ 
				//Get position
				double lon = location.getLongitude();
				double lat = location.getLatitude();
				
				/* 1. Create a query
				 * 2. Insert lat & lon position
				 * 3. Encode query
				 * 4. Add query to API URL
				 */
				String query = "[out:json];way(around:20,"+lat+","+lon+")[\"highway\"];(._;node(w););out;";
				try {
					encodedQuery = URLEncoder.encode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				submission = OVERPASS_API+encodedQuery;
				
				//Create async task to retrieve info
				new SpeedTask().execute();

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

	class SpeedTask extends AsyncTask<String, String, Void> {

	    private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
	    InputStream inputStream = null;
	    String result = ""; 

	    protected void onPreExecute() {
	       
	    }
	    
	    @Override
	    protected Void doInBackground(String... params) {
	    	//android.os.Debug.waitForDebugger();
	    	//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	        //String url_select = "http://yoururlhere.com";
	    	
	    	
	    	StringBuilder builder = new StringBuilder(); 
			HttpClient client = new DefaultHttpClient(); 
			//HttpGet httpGet = new HttpGet(encode2); 
			
			//Using the encoded URL, create a web request to retrieve data
			HttpGet httpGet = new HttpGet(submission);
			try { 
				HttpResponse response = client.execute(httpGet); 
				StatusLine statusLine = response.getStatusLine(); 
				int statusCode = statusLine.getStatusCode(); 
				
				if (statusCode == 200) { 
					HttpEntity entity = response.getEntity(); 
					InputStream content = entity.getContent(); 
					BufferedReader reader = new BufferedReader(new InputStreamReader(content)); 
					String line; 
					
					while ((line = reader.readLine()) != null) { 
						
						builder.append(line); } } 
				else { 
					Log.e("==>", "Failed to download file"); } 
				} catch (ClientProtocolException e) { 
					e.printStackTrace(); 
				} catch (IOException e) { 
					e.printStackTrace(); } 
				result = builder.toString();
			return null;
	    } // protected Void doInBackground(String... params)
	    
	    protected void onPostExecute(Void v) {
	        //parse JSON data
	    	try {
	    	    JSONObject parentObject = new JSONObject(result);
	    	    
	    	    JSONArray speedJSON = parentObject.getJSONArray("elements"); 
	    	    
	    	    /*
	    	     * Loop through array
	    	     * 1. For each JSON object, check if the object contains "tags"
	    	     * 2. If object contains "tags", step into object and retrieve "maxspeed"
	    	     * "tags" is hardcoded by API into JSON array
	    	     */
	    	    
	    	    for (int i = 0; i < speedJSON.length(); i++) {
	    	    	
	    	        JSONObject element = (JSONObject) speedJSON.get(i);
	    	        
	    	        if (!element.isNull("tags")) {
	    	        	//JSONObject tags = (JSONObject) element.get("tags");
	    	        	JSONObject tags = element.getJSONObject("tags");
	    	       
	    	        	//Maxspeed
	    	        	if(tags.has("maxspeed")){
	    	        		currentSpeedLimit = tags.getString("maxspeed");
	    	        		currentRoadName = tags.getString("name");
	    	      
	    	        		tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
	    	        			@Override
	    	        			public void onInit(int status) {
	    	        				// TODO Auto-generated method stub
	    	        				if(status == TextToSpeech.SUCCESS){
	    	        					int result=tts.setLanguage(Locale.getDefault());
	    	        					if(result==TextToSpeech.LANG_MISSING_DATA ||
	    	        							result==TextToSpeech.LANG_NOT_SUPPORTED){
	    	        						Log.e("error", "This Language is not supported");
	    	        					}
	    	        					else{
	    	        						tts.speak("Now entering "+currentSpeedLimit.toString() + " kilometres per hour", TextToSpeech.QUEUE_FLUSH, null);
	    	        					}
	    	        				}
	    	        				else
	    	        					System.out.print("Initilization Failed!");
	    	        			}
	    	        		});
	   
	    	        		//Display current speed limit
	    	        		txtcurLimit.setText(currentSpeedLimit);
	    	        	}
	    	        	else
	    	        	{
	    	        		txtcurLimit.setText("-");
	    	        	}

	    	        	//Road name
	    	        	if(tags.has("name")){
	    	        		String road = tags.getString("name");
	    	        		tempRoad=road;
	    	        		txtRoadName.setText(road+"");
	    	        	}
	    	        	else
	    	        	{
	    	        		txtRoadName.setText("No road name found");
	    	        	}
	    	            
	    	            //Road reference, e.g. R290
	    	        	if(tags.has("ref")){
	    	        		String roadRef = tags.getString("ref");
	    	        		txtRoadRef.setText(roadRef+"");
	    	        	}
	    	        	else
	    	        	{
	    	        		txtRoadRef.setText("No reference found");
	    	        	}
	    	        	
	    	        	//Hold current road in a variable outside of loop
	    	        	tempZone = tags.getString("maxspeed");
	    	        	break;
	    	        } else {
	    	            
	    	        }
	    	    }
	    	    
	    		//Output a beep when current speed has exceeded current speed limit
        		if(currentSpeed!=0 && currentSpeedLimit!=null &&(currentSpeed) > Double.parseDouble(currentSpeedLimit))
        		{
        			//Change text colour & set tone
        			txtSpeed.setTextColor(Color.RED);
        			ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        			toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); 
        		}
        		else
        		{
        			//Remain blue
        			txtSpeed.setTextColor(Color.BLUE);
        		}
	    	    

	    	} catch (JSONException e) {
	    	    // TODO Auto-generated catch block
	    	    e.printStackTrace();
	    	} 
	       
	    } // protected void onPostExecute(Void v)

	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}
}
