package de.mmtech.trackmap;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Map2Activity extends FragmentActivity implements OnClickListener, GoogleMap.OnInfoWindowClickListener  {
	
	public static final String MAP_ROUTEPLAYER_UPCOMMING_WAYPOINT_BACKUP_ID = "de.mmtech.trackmap.Map2Activity.cameraWayPointID";

	private SupportMapFragment mMapFragment;
	private GoogleMap mMap;
	private List<LatLng> myWayPoints;
	private Polyline myWayLineOnMap;
	private Route myRoute;
	private LocationChangedReceiver myListenerForNewWayPoints;
	private RoutePlayer myRoutePlayer;
	private TrackingServiceManager myTrackingServiceManager;
	private DataController myDataController;
	private int routePlayerUpcommmingWayPointRestored;
	private int currentCameraPositionRestored;
	private Marker myLastPositionMarker;
	private boolean viewMapIn3D;

	private Button btn_play;
	private Button btn_next;
	private Button btn_previous;
	private Button btn_pauseTracking;
	private Button btn_stopTracking;
	private Button btn_addPoi;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    routePlayerUpcommmingWayPointRestored=-1;
	    currentCameraPositionRestored=-1;
	    myDataController = new DataController();
	    setContentView(R.layout.activity_map2);
	    // setup map
		mMapFragment = new SupportMapFragment();
		try {
			MapsInitializer.initialize(this);
		} catch (GooglePlayServicesNotAvailableException e) {
			// TODO handle error
			e.printStackTrace();
		}
		getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.map, mMapFragment)
			.commit(); 
	}

	@Override
	public void onResume()
	{
		super.onResume();
		// Prepare map-basics
		getMap().clear();
		myWayLineOnMap = getMap().addPolyline(new PolylineOptions().color(Color.RED));
		myWayPoints = new ArrayList <LatLng>();
		getMap().setOnInfoWindowClickListener(this);
		// setup route
		int routeID=getIntent().getIntExtra("routeID", -2);
		switch (routeID) {
		case -2: // Missing Parameter
			finish();
			break;
		case -1: // jump into ongoing tracking
			if (gotRouteSuccessfully(routeID)) {
				initTrackingModus();
			} else {
				showToastWithMessage(getString(R.string.comp_msg_dberror));
			}
			initTrackingModus();
			break;
		default: // new or old route from DB
			if (gotRouteSuccessfully(routeID)) {
				if (myRoute.isTrackingRunning()) {
					initTrackingModus();
				} else {
					initReplayModus();
				}
			} else {
				showToastWithMessage(getString(R.string.comp_msg_dberror));
			}
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d ("debug","Map2Activity: unreg Rec");
		if (myListenerForNewWayPoints!=null) {
			this.unregisterReceiver(myListenerForNewWayPoints);
			myListenerForNewWayPoints=null;
		}
		super.onDestroy();
	}
	
	private boolean gotRouteSuccessfully (int newRouteID){
		if (newRouteID>0) {
			myRoute=myDataController.getRouteWithID(newRouteID);
		} else {
			myRoute=myDataController.getCurrentlyTrackedRoute();
		}
		if (myRoute!=null) {
			Log.d ("debug","Map2Activity: loaded route with ID: "+myRoute.getRouteID()+" Route is active: "+myRoute.isTrackingRunning());
			return true;
		}
		return false;
	}
	
	private void showToastWithMessage (String message) {
		Toast.makeText(this, message , Toast.LENGTH_LONG).show();
	}

	private void showTrackingButtonBar (boolean show) {
        findViewById(R.id.trackingButtonRow).setVisibility(show ? View.VISIBLE : View.GONE);
        findViewById(R.id.replayButtonRow).setVisibility(show ? View.GONE : View.VISIBLE);
	}
	
	private void initReplayModus() {
		showTrackingButtonBar(false);
		viewMapIn3D=true;
		myRoutePlayer=new RoutePlayer();
		if (routePlayerUpcommmingWayPointRestored>=0) {
			// restore routePlayer and CameraPosition on return to activity
			myRoutePlayer.upcommingWayPointIndex=routePlayerUpcommmingWayPointRestored;
			Log.d ("debug","Map2A: initReplayModus: restore PlayerWP id:"+routePlayerUpcommmingWayPointRestored+" camPos:"+currentCameraPositionRestored);
		} else {
			currentCameraPositionRestored=0;
		}
		updateRouteLineAndMarkers();
		cameraOnWayPoint(currentCameraPositionRestored, viewMapIn3D);
		btn_play = (Button) findViewById(R.id.btn_play);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_previous = (Button) findViewById(R.id.btn_previous);

		btn_play.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_previous.setOnClickListener(this);
	}
	
	private void initTrackingModus() {
		if (myTrackingServiceManager==null) {
			myTrackingServiceManager= new TrackingServiceManager();
			myTrackingServiceManager.startTrackingService();
		}
		viewMapIn3D=false;
		showTrackingButtonBar(true);
		initLocationChangedListener();
		updateRouteLineAndMarkers();
		cameraOnLastRouteWaypoint(viewMapIn3D);

		btn_pauseTracking = (Button) findViewById(R.id.btn_pauseTracking);
		btn_stopTracking = (Button) findViewById(R.id.btn_stropTracking);
		btn_addPoi = (Button) findViewById(R.id.btn_addPoi);

		btn_pauseTracking.setOnClickListener(this);
		btn_stopTracking.setOnClickListener(this);
		btn_addPoi.setOnClickListener(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Poi-Editor returns Poi
	    if (requestCode == PoiEditor.POI_RETURN_INTEND_ID) {
	        if (resultCode == RESULT_OK) {
	        		Poi poiReturnedFromEditor=Poi.fromIntent(data);
	        		Log.d ("debug","Map2A: savedPoi: id:"+poiReturnedFromEditor.getPoiID()+" text:"+poiReturnedFromEditor.getNoteText()+ " markerID:"+poiReturnedFromEditor.mapMarkerID);
	        		myRoute.addPoiToRoute(poiReturnedFromEditor);
	        		updateRouteLineAndMarkers();
	        }
	    }
	}

	
	public class LocationChangedReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			WayPoint trackedWayPoint = new WayPoint(
					intent.getIntExtra("waypoint_id", 0),
					myRoute.getRouteID(),
					intent.getDoubleExtra("latitude", 0.0), 
					intent.getDoubleExtra("longitude", 0.0),
					intent.getLongExtra("time", 0));
			myRoute.addWayPoint(trackedWayPoint);
			updateRouteLineAndMarkers();
			cameraOnLastRouteWaypoint(viewMapIn3D);
		}
	}
	
	private void initLocationChangedListener (){
		if (myListenerForNewWayPoints==null) {
			myListenerForNewWayPoints = new LocationChangedReceiver();
			IntentFilter filter = new IntentFilter("locationUpdate");
			this.registerReceiver(myListenerForNewWayPoints, filter);
		}
	}
	
	private class TrackingServiceManager {
		
		private boolean isMyServiceRunning() {
		    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if (TrackingService.class.getName().equals(service.service.getClassName())) {
		            return true;
		        }
		    }
		    return false;
		}

		public void startTrackingService(){
			if (!isMyServiceRunning()){
				Intent trackingServiceIntent = new Intent(Map2Activity.this, TrackingService.class);
				Map2Activity.this.startService(trackingServiceIntent);
			}
			Map2Activity.this.showToastWithMessage(
					Map2Activity.this.getString(
							R.string.trackingService_msg_started));
		}

		public void stopTrackingService () {
			if (isMyServiceRunning()){
				Intent stopTrackingServiceIntent = new Intent(Map2Activity.this, TrackingService.class);
				Map2Activity.this.stopService(stopTrackingServiceIntent);
			}
			Map2Activity.this.showToastWithMessage(
					Map2Activity.this.getString(
							R.string.trackingService_msg_stopped));
		}

		public void toggleTracking() {
			if (isMyServiceRunning()) {
				stopTrackingService();
			} else {
				startTrackingService();
			}
		}
	}
	
	private void updateRouteLineAndMarkers () {
		if (myRoute.getNumberOfWayPoints()>0) {
			// WayPoints
			myWayPoints.clear();
			for (WayPoint looper : myRoute.getWayPointList()) {
				myWayPoints.add(looper.getPosition());
				}
			// Pois
			if (myRoute.getNumberOfPois()>0) {
				for (Poi looper : myRoute.getPoiList()) {
					if (looper.mapMarkerID==null) {
						// (if not done before:) create Marker on map and save its ID to the Poi-object
						looper.mapMarkerID=getMap()
								.addMarker(prepareMarker(looper))
								.getId();
					}
				}
			}
			myWayLineOnMap.setPoints(myWayPoints);
		}
	}
	
	private MarkerOptions prepareMarker(Poi poiForMarker) {
		Log.d ("debug","Map2: set Poi on map ID: "+poiForMarker.getPoiID()+" Note: "+poiForMarker.getNoteText());
		String title="POI";
		BitmapDescriptor iconBitmap=BitmapDescriptorFactory.fromResource(R.drawable.marker_n_red);
		LatLng position =
				myRoute
					.getWayPointWithID(poiForMarker.getWayPointID())
					.getPosition();
		if (!poiForMarker.getFotoPath().equals("")) {
			title+=" + Foto";
			iconBitmap=BitmapDescriptorFactory.fromResource(R.drawable.marker_green_p);
		} 
		if (!poiForMarker.getVideoPath().equals("")) {
			title+=" + Video";
			iconBitmap=BitmapDescriptorFactory.fromResource(R.drawable.marker_v_blue);
		} 
		return new MarkerOptions()
			.position(position)
			.title(title)
			.snippet(poiForMarker.getNoteText())
			.icon(iconBitmap);
	}
	
	private void cameraOnWayPoint (int WayPointListIndex, boolean viewIn3D) {
		if (myRoute.getNumberOfWayPoints()>0) {
			currentCameraPositionRestored=WayPointListIndex;
			LatLng cameraPos = myRoute
					.getWayPointList()
					.get(WayPointListIndex)
					.getPosition();
			LatLng posBefore = cameraPos;				
			if (myRoute.getNumberOfWayPoints()>=2
					&& WayPointListIndex>=1) {
				posBefore =  myRoute
						.getWayPointList()
						.get(WayPointListIndex-1)
						.getPosition();
			}
			positionCamera(cameraPos, posBefore, viewIn3D);
		}		
	}
	
	private void cameraOnLastRouteWaypoint (boolean viewIn3D) {
		if (myRoute.getNumberOfWayPoints()>0) {
			cameraOnWayPoint(myRoute.getNumberOfWayPoints()-1, viewIn3D);
		}		
	}
	
	private void positionCamera (LatLng cameraPos, LatLng beforePos, boolean viewIn3D) {
		if (viewIn3D) {
			CameraPosition cameraPosition = new CameraPosition.Builder()
			    .target(cameraPos)      		// Camera-Fokus
			    .zoom(17)                   // Sets the zoom
			    .bearing(cameraAngleFollowingBehind(beforePos, cameraPos))// View-Direction
			    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
			    .build();                   // Creates a CameraPosition from the builder
		
			getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));					
		} else { //TODO: Reset to 2D view
				CameraPosition cameraPosition = new CameraPosition.Builder()
			    .target(cameraPos)      		// Camera-Fokus
			    .zoom(17)                   // Sets the zoom
			    .tilt(0)                   
			    .build();                   // Creates a CameraPosition from the builder

			getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}
}
	
	private float cameraAngleFollowingBehind (LatLng before, LatLng after) {
		if (before.latitude==after.latitude
				&& before.longitude==after.longitude) { // if before and after are the same loc..
			return 0; // allways look north..
		} else {
			Location location1 = new Location("");
			location1.setLatitude(before.latitude);
			location1.setLongitude(before.longitude);
			Location location2 = new Location("");
			location2.setLatitude(after.latitude);
			location2.setLongitude(after.longitude);
			return location1.bearingTo(location2);
		}
	}
	
	// callback für Marker-Info-Feld
	public void onInfoWindowClick(Marker clickedMarker) {
		Log.d ("debug","Map2Activity: poi-clicked markerid: "+clickedMarker.getId());
		if (myRoutePlayer!=null) {
			myRoutePlayer.pausePlayer();
			routePlayerUpcommmingWayPointRestored=myRoutePlayer.upcommingWayPointIndex;
			Log.d ("debug","Map2A: onInfoWindowClick: restore PlayerWP id:"+routePlayerUpcommmingWayPointRestored);
		}
		Poi poiWithClickedMaker = myRoute.getPoiWithMarkerID(clickedMarker.getId());
		if (poiWithClickedMaker!=null) {
			startActivityForResult(
					new Intent(this, PoiEditor.class).putExtra(Poi.INTENT_ID, poiWithClickedMaker),
					PoiEditor.POI_RETURN_INTEND_ID
					);
			Log.d ("debug","Map2Activity: poi-clicked: poi-marker-id:"+poiWithClickedMaker.getPoiID());
		}
		
	}


	private class RoutePlayer {
		public int upcommingWayPointIndex=0;
		public boolean playerPaused=true;
		public int waitingTimeInMilSec=800;
		public boolean viewPlayerIn3D=true;
		private Handler handler;
		
		public RoutePlayer() {
			handler = new Handler();
		}

		private void startTimerForNextViewpoint () {
	        handler.postDelayed(
	                new Runnable() {
	                      @Override
	                      public void run() {
	      					if (!RoutePlayer.this.playerPaused) {
		    						cameraOnWayPoint(RoutePlayer.this.upcommingWayPointIndex, RoutePlayer.this.viewPlayerIn3D);
		    						// set next WayPoint if not end..
		    						if (RoutePlayer.this.upcommingWayPointIndex<(myRoute.getNumberOfWayPoints()-1)) {
		    							RoutePlayer.this.upcommingWayPointIndex++;
		    							RoutePlayer.this.startTimerForNextViewpoint();
		    						} else { // otherwise pause (at end)
		    							RoutePlayer.this.playerPaused=true;
		    							RoutePlayer.this.upcommingWayPointIndex=0;
		    						}
		    					}
	                      }
	                    }, this.waitingTimeInMilSec);

		}

		public void playWayPoint (int wayPointIndex) {
			this.upcommingWayPointIndex=wayPointIndex;
			startTimerForNextViewpoint();
		}
		
		public void togglePlayPause () {
			playerPaused=!playerPaused;
			if (!playerPaused) {
				playWayPoint(this.upcommingWayPointIndex);
			}
		}
		
		public void pausePlayer() {
			playerPaused=true;
		}
		
		public void resumePlayer() {
			playerPaused=false;
			playWayPoint(this.upcommingWayPointIndex);
		}
		
		public void jumpToNext () {
			playerPaused=true;
			upcommingWayPointIndex=getNextWayPointWithPoi(upcommingWayPointIndex);
			cameraOnWayPoint(upcommingWayPointIndex, viewPlayerIn3D);			
		}

		public void jumpToPrevious () {
			playerPaused=true;
			upcommingWayPointIndex=getPreviousWayPointWithPoi(upcommingWayPointIndex);
			cameraOnWayPoint(upcommingWayPointIndex, viewPlayerIn3D);			
		}
		
		private int getNextWayPointWithPoi (int fromThisWayPoint) {
			int nextWP =	myRoute.getNextWayPointIndexWithPoi(fromThisWayPoint);
			if (nextWP>0) {
				return nextWP;
			}
			return fromThisWayPoint;
		}

		private int getPreviousWayPointWithPoi (int fromThisWayPoint) {
			int nextWP =	myRoute.getPreviousWayPointIndexWithPoi(fromThisWayPoint);
			if (nextWP>0) {
				return nextWP;
			}
			return fromThisWayPoint;
		}

	}
	
	private GoogleMap getMap() {
		if (mMap == null) {
			mMap = mMapFragment.getMap();
		}
		return mMap;
	}
	
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_play:
			myRoutePlayer.togglePlayPause();
			break;

		case R.id.btn_next:
			myRoutePlayer.jumpToNext();
			break;

		case R.id.btn_previous:
			myRoutePlayer.jumpToPrevious();
			break;

		case R.id.btn_pauseTracking:
			myTrackingServiceManager.toggleTracking();
			break;

		case R.id.btn_addPoi:
			WayPoint wayPointWithNewPoi = myRoute.getLastWayPoint();
			// allow only WayPoints without Poi
			if (wayPointWithNewPoi!=null) {
				if (wayPointWithNewPoi.getPoiID()<0) {
					startActivityForResult(
							new Intent(this, PoiEditor.class).putExtra("wayPointID",wayPointWithNewPoi.getWayPointID()),
							PoiEditor.POI_RETURN_INTEND_ID					
							);
				} else {
					showToastWithMessage(getString(R.string.map_msg_allready_poi_set));
				}
			}
			break;

		case R.id.btn_stropTracking:
			myTrackingServiceManager.stopTrackingService();
			myRoute.finishRoute();
		    finish();
			break;
		}
	}
	
}
