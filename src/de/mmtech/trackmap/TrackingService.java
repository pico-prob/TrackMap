package de.mmtech.trackmap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;
/**
 * 
 */
public class TrackingService extends Service implements LocationListener
{
	private DataController myDataController;
	private NotificationManager notificationManager;
	private LocationManager locationManager;
	private final int serviceId = 666999;
	
	@Override
	public void onCreate()
	{
		initComponents();
		startLocationTracking();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		startForeground(
				serviceId,
				buildNotification(getString(R.string.trackingService_msg_started), true));
		return START_REDELIVER_INTENT;
	}

	
	@Override
	public void onDestroy()
	{
		notificationManager.cancel(serviceId);
		locationManager.removeUpdates(this);
		locationManager = null;
		notificationManager.notify(
				serviceId, 
				buildNotification(getString(R.string.trackingService_msg_stopped), false));
	}

	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	
	public void showNotification(CharSequence text, boolean offerCallback)
	{
		notificationManager.notify(serviceId, buildNotification(text, offerCallback));
	}

	
	public void onLocationChanged(Location location) {
		if (location!=null) {
			int wayPointID = saveLocationToDBAndReturnWayPointID(location);
			if (wayPointID>=0) {
				Log.d ("debug","Got Loc: wpID: "+wayPointID+" Lat "+ location.getLatitude() +" Long "+ location.getLongitude());
				sendLocationToListener(location,wayPointID);	
			}
		} else {
			showToast (getString(R.string.trackingService_msg_gps_missing));
		}
	}
	
	
	public void onProviderDisabled(String provider)
	{
		showToast (getString(R.string.trackingService_msg_gps_missing));
	}

	
	public void onProviderEnabled(String provider)
	{
		showToast (getString(R.string.trackingService_msg_gps_active));
	}

	
	public void onStatusChanged(String provider, int status, Bundle extras)
	{}


	private void initComponents()
	{
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		myDataController = new DataController();
		myDataController.initDB(getApplicationContext());
	}


	private void startLocationTracking()
	{
		locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 
				Integer.parseInt(getString(R.string.tracking_interval)),
				Integer.parseInt(getString(R.string.min_tracking_distance)),
				this);
		Location firstLoc =locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		onLocationChanged(firstLoc);
	}

	
	private Notification buildNotification(CharSequence text, boolean offerCallback)
	{
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(getString(R.string.app_name))
		        .setContentText(text);
		if (offerCallback) {
			Intent resultIntent = new Intent(this, Map2Activity.class).putExtra("routeID", -1);
			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(Map2Activity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			        );
			mBuilder.setContentIntent(resultPendingIntent);
		}
		return mBuilder.build();
	}

	
	private void sendLocationToListener(Location location,int wayPointID)
	{
		Intent intent = new Intent("locationUpdate");
		intent.putExtra("waypoint_id", wayPointID);
		intent.putExtra("latitude", location.getLatitude());
		intent.putExtra("longitude", location.getLongitude());
		intent.putExtra("time", location.getTime());
		sendBroadcast(intent, null);
	}
	
	private int saveLocationToDBAndReturnWayPointID (Location location){
		int wayPointID=myDataController.saveWayPointAndReturnID(
				location.getLatitude(),
				location.getLongitude(), 
				location.getTime());
		if (wayPointID<0) {
			showToast(getString(R.string.trackingService_msg_route_missing));
		}
		return wayPointID;
	}
	
	private void showToast (String text) {
		Toast.makeText(this, getString(R.string.trackingService_msg_gps_missing), Toast.LENGTH_SHORT).show();
	}
}
