package de.mmtech.trackmap;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends Activity implements OnClickListener {

	private DataController myDataController;
	private Button btn_newJourney;
	private Button btn_actJourney;
	private Button btn_history;
	private Button btn_help;
	
	private void initComponents()
	{
		myDataController = new DataController();
		myDataController.initDB(getApplicationContext());
		
		btn_newJourney = (Button) findViewById(R.id.btn_newJourney);
		btn_actJourney = (Button) findViewById(R.id.btn_actualJourney);
		btn_history = (Button) findViewById(R.id.btn_history);
		btn_help = (Button) findViewById(R.id.btn_help);

		btn_newJourney.setOnClickListener(this);
		btn_actJourney.setOnClickListener(this);
		btn_history.setOnClickListener(this);
		btn_help.setOnClickListener(this);
	}

	private void prepareNewRoute() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		initComponents();
	}
	
	@Override
	protected void onResume() {
		// disable button for currently tracked route if none is tracked
		if (myDataController.getCurrentlyTrackedRouteID()<0) {
			btn_actJourney.setEnabled(false);
			btn_newJourney.setEnabled(true);
		} else {
			btn_actJourney.setEnabled(true);
			btn_newJourney.setEnabled(false);
		}
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_newJourney:
			startActivity(
					new Intent(this, RouteStartActivity.class));
			break;

		case R.id.btn_actualJourney:
			startActivity(
					new Intent(this, Map2Activity.class)
					.putExtra("routeID", -1));
			break;

		case R.id.btn_history:
			startActivity(new Intent(this, HistoryActivity.class));
			break;

		case R.id.btn_help:
			startActivity(new Intent(this, ShowHelpActivity.class));
			break;
		}
	}

	
}
