package de.mmtech.trackmap;

import java.util.Date;

import com.google.android.gms.maps.model.LatLng;

public class WayPoint {
	private int wayPointID;
	private LatLng position;
	private Date time;
	private int routeID;
	private int poiID=-1; // only for caching: not persisted
	
	public WayPoint(int routeID,double latitude,double longitude, long timestamp) {
		this.position=new LatLng(latitude,longitude);
		this.time=new Date (timestamp);
		this.routeID=routeID;
		createWayPointInDB();
	}
	
	public WayPoint(DBobjWayPoint dbObject) {
		loadWayPointFromDBobj(dbObject);
	}
	
	public WayPoint(int wayPointID, int routeID,double latitude,double longitude, long timestamp) {
		this.wayPointID=wayPointID;
		this.position=new LatLng(latitude,longitude);
		this.time=new Date (timestamp);
		this.routeID=routeID;
	}
	
	private void createWayPointInDB () {
		loadWayPointFromDBobj(DBmodel.INSTANCE.createWayPoint(routeID, position.latitude, position.longitude, time.getTime()));
	}
	
	private void loadWayPointFromDBobj (DBobjWayPoint dbObject) {
		this.wayPointID = dbObject.getwayPointId();
		this.position = new LatLng(
				dbObject.getLatitude(),
				dbObject.getLongitude());
		this.time= dbObject.getTimeStamp();
		this.routeID=dbObject.getTripId();
	}
	
	/**
	 * @return the wayPointID
	 */
	public int getWayPointID() {
		return wayPointID;
	}

	/**
	 * @return the position
	 */
	public LatLng getPosition() {
		return position;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @return the routeID
	 */
	public int getRouteID() {
		return routeID;
	}

	/**
	 * @return the poiID
	 */
	public int getPoiID() {
		return poiID;
	}

	/**
	 * @param poiID the poiID to set
	 */
	public void setPoiID(int poiID) {
		this.poiID = poiID;
	}

}