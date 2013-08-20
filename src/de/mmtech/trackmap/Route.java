package de.mmtech.trackmap;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class Route {
	private List<WayPoint> wayPointList;
	private int numberOfWayPoints;
	private List<Poi> poiList;
	private int numberOfPois;
	private String title;
	private String note;
	private int routeID;
	private boolean activeRoute=false;
	
	public Route (int newRouteID) {
		loadRouteFromDB(newRouteID);
	}
	
	public Route (String newTitle, String newNote) {
		this.routeID= DBmodel.INSTANCE.createTrip(newTitle, newNote, 1).getTripId();
		Log.d ("debug","Route: createReise: id: "+routeID);
		loadRouteFromDB(routeID);
	}
		
	private void loadRouteFromDB (int newRouteID){
		// get Route-Info
		DBobjRoute dbObject=DBmodel.INSTANCE.getTrip(newRouteID);
		if (dbObject!=null) {
			// route-object
			this.routeID=dbObject.getTripId();
			this.title=dbObject.getTitelText();
			this.note=dbObject.getNoteText();
			Log.d ("debug","Route: loaded with id: "+routeID);
			if (dbObject.getIsTracked()==1) {
				activeRoute=true;
			} else {
				activeRoute=false;
			}
			wayPointList=new ArrayList <WayPoint>();
			poiList=new ArrayList <Poi>();
			loadWayPoints();
			loadPois();
		} else {
			DBmodel.INSTANCE.showToastWithMessage("Trackmap: Accessing Database Error");
		}
	}
	
	private void loadWayPoints() {
		this.numberOfWayPoints=0;
		List<DBobjWayPoint> dbCoordsList = DBmodel.INSTANCE.getAllWayPointsForTrip(routeID); 
		for (DBobjWayPoint looper : dbCoordsList) {
			wayPointList.add(new WayPoint(looper));
			this.numberOfWayPoints++;
		}			
	}
	
	private void loadPois() {
		this.numberOfPois=0;
		List<DBobjPoi> dbPoiList = DBmodel.INSTANCE.getAllPoisFromTrip(routeID);
		if (dbPoiList!=null) {
			Log.d ("debug","Route: loaded  Poi found: "+dbPoiList.size());
			for (DBobjPoi looper : dbPoiList) {
				Poi newPoi = new Poi(looper);
				Log.d ("debug","Route: loaded  Poi found: id: "+newPoi.getPoiID()+" note: "+newPoi.getNoteText());
				WayPoint wayPointWithPoi = getWayPointWithID(newPoi.getWayPointID());
				wayPointWithPoi.setPoiID(newPoi.getPoiID()); // cache poi at WayPoint
				poiList.add(newPoi);
				numberOfPois++;								
			}
		}
		
	}
	
	public void addWayPoint (WayPoint newWayPoint) {
		wayPointList.add(newWayPoint);
		numberOfWayPoints++;
	}
	
	public void addPoiToRoute(Poi newPoi) {
		if(newPoi!=null) {
			if (!updatedEditedPoiSuccessfully(newPoi)) {
				addNewPoiToRoute(newPoi);
			}
		}
	}
	
	private void addNewPoiToRoute(Poi newPoi) {
		WayPoint wayPointWithPoi = getWayPointWithID(newPoi.getWayPointID());
		if (wayPointWithPoi!=null) {
			wayPointWithPoi.setPoiID(newPoi.getPoiID()); // for quick ref only: cache poi at WayPoint
			poiList.add(newPoi);
			numberOfPois++;	
		}
	}
	
	private boolean updatedEditedPoiSuccessfully (Poi updatedPoi) {
		boolean foundPoi=false;
		for (int i = 0; i < poiList.size(); i++) {
			Poi looper = poiList.get(i);
			if (looper.getPoiID()==updatedPoi.getPoiID()) {
				poiList.set(i, updatedPoi);
				foundPoi=true;
			}
		}
		return foundPoi;
	}
	
	public WayPoint getWayPointWithID (int idToLookFor) {
		if (wayPointList!=null) {
			for (WayPoint looper : wayPointList) {
				if (looper.getWayPointID()==idToLookFor) {
					return looper;
				}
			}
		}
		return null;
	}
	
	public int getListIndexForWayPoint (WayPoint wayPointToLookFor) {
		if (wayPointToLookFor!=null) {
			for (int i = 0; i < wayPointList.size(); i++) {
				WayPoint looper = wayPointList.get(i);
				if (looper.getWayPointID()==wayPointToLookFor.getWayPointID()) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public int getNextWayPointIndexWithPoi (int startingFromThisWayPointIndex) {
		if (startingFromThisWayPointIndex>=0  // check if WayPoint is part of this route
				&& startingFromThisWayPointIndex< wayPointList.size())
		{
			for (int i = startingFromThisWayPointIndex+1 ; i < wayPointList.size(); i++) {
				WayPoint looper = wayPointList.get(i);
				if (looper.getPoiID()>=0) return i;
			}
		}
		return -1;
	}

	public int getPreviousWayPointIndexWithPoi (int startingFromThisWayPointIndex) {
		if (startingFromThisWayPointIndex>=1  // check if WayPoint is part of this route
				&& startingFromThisWayPointIndex< wayPointList.size())
		{
			for (int i = startingFromThisWayPointIndex-1 ; i >= 0; i--) {
				WayPoint looper = wayPointList.get(i);
				if (looper.getPoiID()>=0) return i;
			}
		}
		return -1;
	}
	
	public Poi getPoiWithID (int idToLookFor) {
		if (poiList!=null) {
			for (Poi looper : poiList) {
				if (looper.getPoiID()==idToLookFor) {
					return looper;
				}
			}			
		}
		return null;
	}
	
	public Poi getPoiWithMarkerID (String idToLookFor) {
		if (poiList!=null) {
			for (Poi looper : poiList) {
				if (looper.mapMarkerID.equals(idToLookFor)) {
					return looper;
				}
			}			
		}
		return null;
	}
	
	public void finishRoute () {
		DBmodel.INSTANCE.updateTrip(routeID, title, note, 0);
	}

	public List<WayPoint> getWayPointList() {
		return wayPointList;
	}

	public List<Poi> getPoiList() {
		return poiList;
	}
	
	public int getNumberOfPois() {
		return numberOfPois;
	}

	public int getNumberOfWayPoints() {
		return numberOfWayPoints;
	}

	public WayPoint getLastWayPoint() {
		if (numberOfWayPoints>=1) {
			return wayPointList.get(numberOfWayPoints-1);
		} else {
			return null;
		}
	}
	
	public WayPoint getSecondLastWayPoint() {
		if (numberOfWayPoints>=2) {
			return wayPointList.get(numberOfWayPoints-2);
		} else {
			return null;
		}
	}

	public String getTitle() {
		return title;
	}

	public String getNote() {
		return note;
	}

	public int getRouteID() {
		return routeID;
	}

	public boolean isTrackingRunning() {
		return activeRoute;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
