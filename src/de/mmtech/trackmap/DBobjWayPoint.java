package de.mmtech.trackmap;

import java.util.Date;


public class DBobjWayPoint
{
	private int wayPointID;
	private int tripID;
	private double latitude;
	private double longitude;
	private double altitude;
	private Date timestamp;
	public int getwayPointId()
	{
		return wayPointID;
	}

	
	public void setwayPointId(int new_wayPointid)
	{
		wayPointID = new_wayPointid;
	}

	
	public int getTripId()
	{
		return tripID;
	}

	
	public void setTripId(int new_trip)
	{
		tripID = new_trip;
	}

	
	public double getLatitude()
	{
		return latitude;
	}

	
	public void setLatitude(double new_latitude)
	{
		latitude = new_latitude;
	}

	
	public double getLongitude()
	{
		return longitude;
	}

	
	public void setLongitude(double newLongitude)
	{
		longitude = newLongitude;
	}

	
	public double getAltitude()
	{
		return altitude;
	}

	
	public void setAltitude(double new_altitude)
	{
		altitude = new_altitude;
	}

	
	public Date getTimeStamp()
	{
		return timestamp;
	}

	
	public void setTimeStamp(Date date)
	{
		timestamp = date;
	}

	
	@Override
	public String toString()
	{
		return latitude + ", " + longitude;
	}
}
