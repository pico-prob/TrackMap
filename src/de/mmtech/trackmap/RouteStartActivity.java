package de.mmtech.trackmap;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RouteStartActivity extends Activity implements OnClickListener
{
	private DataController myDataController;
	private EditText textFieldTitle;
	private EditText textFieldRouteNote;
	private Button btn_routeStart;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_start);
		initComponents();
	}

	private void initComponents()
	{
		textFieldTitle = (EditText) findViewById(R.id.et_routeTitle);
		textFieldRouteNote = (EditText) findViewById(R.id.et_routeNote);
		btn_routeStart = (Button) findViewById(R.id.btn_startNewRoute);
		btn_routeStart.setOnClickListener(this);
		myDataController = new DataController();
	}

	public void onClick(View v)
	{
		if (v.getId() == btn_routeStart.getId()) {
			// Name entered?
			if (textFieldTitle.getText().length() != 0)	{
				// Location available?
				LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			    	// allready tracking a route?
					if (myDataController.getCurrentlyTrackedRouteID()<0) {
						// prepare a new route..
						Route newRoute=myDataController.createNewRoute(textFieldTitle.getText().toString(), textFieldRouteNote.getText().toString());
						if (newRoute!=null) {
							//.. and load it into the map
							startActivity(
									new Intent(this, Map2Activity.class)
									.putExtra("routeID", newRoute.getRouteID()));
							finish();
						} else {
							Toast.makeText(this, getString(R.string.comp_msg_dberror), Toast.LENGTH_LONG).show();
						}
					} else {
						Toast.makeText(this, getString(R.string.routeStarter_msg_allready_tracking), Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(this, getString(R.string.routeStarter_msg_missing_gps), Toast.LENGTH_LONG).show();
				} 
			} else
				Toast.makeText(this, getString(R.string.routeStarter_msg_missing_title), Toast.LENGTH_LONG).show();
		}
	}	
}
