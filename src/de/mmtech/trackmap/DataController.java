package de.mmtech.trackmap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class DataController {
	
	private List <Integer> lastRequestedPoiNotesTablePoiIDsList;
	private List <Integer> lastRequestedRouteTitleTableRouteIDsList;

	public void initDB (Context context) {
		DBmodel.INSTANCE.initDBmodel(context);
	}

	public Route createNewRoute (String routeTitle, String routeNote) {
		if (DBmodel.INSTANCE!=null){
			return new Route(routeTitle,routeNote);
		}
		return null;
	}
	
	public Route getRouteWithID (int savedRouteID) {
		Route foundRoute=null;
		if (DBmodel.INSTANCE!=null){
			foundRoute= new Route(savedRouteID);
		}
		return foundRoute;
	}
	
	private String removeEmptyTitle (String titleToCheck) {
		if (titleToCheck.isEmpty()) {
			return "   [Title missing]";
		}
		return titleToCheck;
	}
	public ArrayList<String> allsavedRouteTitles (){
		List<String>allsavedRouteTitleList=new ArrayList<String>();
		List<DBobjRoute> allsavedRouteList = DBmodel.INSTANCE.getAllCompletedTrips();
		lastRequestedRouteTitleTableRouteIDsList=new ArrayList<Integer>();
		if (allsavedRouteList!=null && !allsavedRouteList.isEmpty()) {
			for (DBobjRoute looper : allsavedRouteList) {
				allsavedRouteTitleList.add(
						removeEmptyTitle(looper.getTitelText()));
				lastRequestedRouteTitleTableRouteIDsList.add(new Integer(looper.getTripId()));
			}
		}
		return (ArrayList) allsavedRouteTitleList;
	}
	
	public int poiIDToLastPoiNoteIndex (int indexOfPoiNote) {
		if (lastRequestedRouteTitleTableRouteIDsList!=null
				&&lastRequestedPoiNotesTablePoiIDsList.size()-1>=indexOfPoiNote) {
			return lastRequestedPoiNotesTablePoiIDsList.get(indexOfPoiNote);
		}
		return -1;
	}

	public ArrayList<String> allsavedPoiNotes (){
		List<String>allsavedPoiNotesList=new ArrayList<String>();
		List<DBobjPoi> allSavedPois = DBmodel.INSTANCE.getAllPois();
		lastRequestedPoiNotesTablePoiIDsList=new ArrayList<Integer>();
		if (allSavedPois!=null && !allSavedPois.isEmpty()) {
			for (DBobjPoi looper : allSavedPois) {
				allsavedPoiNotesList.add(
						removeEmptyTitle(looper.getNoteText()));
				lastRequestedPoiNotesTablePoiIDsList.add(new Integer(looper.getPoiID()));
			}
		}
		return (ArrayList) allsavedPoiNotesList;
	}
	public int routeIDToLastRouteTitleIndex (int indexOfRouteTitle) {
		if (lastRequestedRouteTitleTableRouteIDsList!=null
				&&lastRequestedRouteTitleTableRouteIDsList.size()-1>=indexOfRouteTitle) {
			return lastRequestedRouteTitleTableRouteIDsList.get(indexOfRouteTitle);
		}
		return -1;
	}

	
	public Route getCurrentlyTrackedRoute (){
	int currentRouteID=DBmodel.INSTANCE.getLiveTripId();
		if (currentRouteID>=0) { // route running?
			return getRouteWithID(currentRouteID);
	}
		return null;
	}

	public int getCurrentlyTrackedRouteID (){
		return DBmodel.INSTANCE.getLiveTripId();
	}


	public int saveWayPointAndReturnID (double latitude, double longitude, long time){
		int currentRouteID=DBmodel.INSTANCE.getLiveTripId();
		if (currentRouteID>=0) {
			WayPoint trackedWayPoint = new WayPoint(
					currentRouteID,
					latitude, 
					longitude,
					time);
			return trackedWayPoint.getWayPointID();
		}
		return -1;
	}
	
}
