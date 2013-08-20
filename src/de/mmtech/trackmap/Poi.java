package de.mmtech.trackmap;

import java.io.Serializable;

import android.content.Intent;



public class Poi implements Serializable {
	public static final String INTENT_ID = "POI";
	private int poiID;
	private String noteText;
	private String videoPath;
	private String fotoPath;
	private int wayPointID;
	public String mapMarkerID;
	
	public Poi(int wayPointID) {
		this.wayPointID=wayPointID;
		createPoiInDB();
	}
	
	public Poi(DBobjPoi dbObject) {
		loadPoiFromDBobj(dbObject);
	}
	
	private void createPoiInDB () {
		loadPoiFromDBobj(DBmodel.INSTANCE.createPoi(wayPointID));
	}

	public static Poi fromIntent(Intent intentWithPoi) {
		return (Poi) intentWithPoi.getSerializableExtra(INTENT_ID);
	}

	public static Poi fromDBwithPoiID(int idToLookFor) {
		DBobjPoi poiFoundInDB = DBmodel.INSTANCE.getPoiWithID(idToLookFor);
		if (poiFoundInDB!=null) {
			return new Poi(poiFoundInDB);
		}
		return null; 
	}
	
	public void removeMe () {
		DBmodel.INSTANCE.deletePoiWithID(poiID);
	}

	public boolean isEmpty (){
		if (noteText.equals("")
			&& fotoPath.equals("")
			&& videoPath.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	
	private void loadPoiFromDBobj (DBobjPoi dbObject) {
		this.poiID=dbObject.getPoiID();
		this.wayPointID=dbObject.getWayPointID();
		this.noteText=dbObject.getNoteText();
		this.videoPath=dbObject.getVideoPath();
		this.fotoPath=dbObject.getFotopath();
	}
	
	private void savePoiToDB () {
		DBmodel.INSTANCE.updatePoi(poiID, wayPointID, fotoPath, videoPath, noteText);
	}
	
	public int getPoiID() {
		return poiID;
	}
	public int getWayPointID() {
		return wayPointID;
	}

	public String getNoteText() {
		return noteText;
	}

	public String getVideoPath() {
		return videoPath;
	}

	public String getFotoPath() {
		return fotoPath;
	}

	public void setNoteText(String noteText) {
		this.noteText = noteText;
		savePoiToDB();
	}

	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
		savePoiToDB();
	}

	public void setFotoPath(String fotoPath) {
		this.fotoPath = fotoPath;
		savePoiToDB();
	}


}