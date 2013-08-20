package de.mmtech.trackmap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryActivity extends Activity implements OnItemClickListener, OnClickListener{
	
	public static final String HISTORY_SHOWROUTES = "de.mmtech.trackmap.HistoryActivity.showRoutes";

	private boolean showRoutes;
	private ListView tableView;
    private TextView headLine;
    private boolean anyDataFound;
	private Button btn_showRoutes;
	private Button btn_showPois;
	private DataController myDataController;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		showRoutes=true;
	}

	@Override
	protected void onResume() {
		if (myDataController==null) {
			myDataController = new DataController();
		}
		// init View
		tableView = (ListView)findViewById(R.id.lv_historytable);
		tableView.setOnItemClickListener(this);
		headLine = (TextView)findViewById(R.id.text_ListState);

		btn_showPois= (Button) findViewById(R.id.btn_poihistory);
		btn_showRoutes = (Button) findViewById(R.id.btn_routehistory);
		btn_showPois.setOnClickListener(this);
		btn_showRoutes.setOnClickListener(this);
	
		tableView.setAdapter(loadTableData());
		super.onResume();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}
	
	private ArrayAdapter<String> loadTableData() {
		ArrayList <String> tableData;
		if (showRoutes) {
			headLine.setText(R.string.headline_history_route);
			tableData=myDataController.allsavedRouteTitles();
		} else {
			headLine.setText(R.string.headline_history_poi);
			tableData=myDataController.allsavedPoiNotes();
		}
		anyDataFound=!tableData.isEmpty();
		return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,tableData);
	}

	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        
        if(anyDataFound && showRoutes){ // Route-Table touched
        		int routeID=myDataController.routeIDToLastRouteTitleIndex(position);
        		if (routeID>=0) {
        			startActivity(
        					new Intent(this, Map2Activity.class)
        					.putExtra("routeID", routeID));
			}
        }
   
        if(anyDataFound && !showRoutes){ // Route-Table touched
	    		int poiID=myDataController.poiIDToLastPoiNoteIndex(position);
	    		if (poiID>=0) {
	    			Poi selectedPoi=Poi.fromDBwithPoiID(poiID);
	    			startActivity(
	    					new Intent(this, PoiEditor.class)
	    					.putExtra(Poi.INTENT_ID, selectedPoi)
	    					);
			}
        }
	}
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_poihistory:
			showRoutes=false;
			tableView.setAdapter(loadTableData());
			break;

		case R.id.btn_routehistory:
			showRoutes=true;
			tableView.setAdapter(loadTableData());
			break;
		}
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean(HISTORY_SHOWROUTES, showRoutes);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
            this.showRoutes = savedInstanceState.getBoolean(HISTORY_SHOWROUTES);
    }
}
