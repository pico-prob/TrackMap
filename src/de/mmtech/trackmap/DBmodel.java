package de.mmtech.trackmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public enum DBmodel {
	INSTANCE;
	
	private Context myContext;
	private boolean contextIsSet=false;	
	private static final String[] allColumnsTrip =
	{ "TRIP_ID", "TITLE", "COMMENT", "LIVE" };

	private static final String[] allColumnsGPSCoordinate =
	{ "GPS_ID", "TRIP", "LATITUDE", "LONGITUDE", "ALTITUDE", "RECORDING" };

	private static final String[] allColumnsMemory =
	{ "MEMORY_ID", "GPSCOORDINATE", "PHOTO", "VIDEO", "NOTE" };

	private DBsetupHelper myDBSetupHelper;
	private SQLiteDatabase sqlDB;

	DBmodel() {
		contextIsSet=false;
	}

	public void initDBmodel (Context context)
	{
		if (!contextIsSet) {
			myContext=context;
			contextIsSet=true;
			myDBSetupHelper = new DBsetupHelper(context);
		}
	}
	
	public void showToastWithMessage (String message) {
		Toast.makeText(myContext, message , Toast.LENGTH_LONG).show();
	}

	public void openForRead() throws SQLException
	{
		sqlDB = myDBSetupHelper.getReadableDatabase();
	}

	
	public void openForWrite() throws SQLException
	{
		sqlDB = myDBSetupHelper.getWritableDatabase();
	}

	
	public void close()
	{
		myDBSetupHelper.close();
	}

	
	public DBobjRoute createTrip(String title, String comment, int live)
	{
		openForWrite();

		ContentValues values = new ContentValues();
		values.put("LIVE", live);
		values.put("COMMENT", comment);
		values.put("TITLE", title);

		long insertId = sqlDB.insert("Trip", null, values);

		Cursor cursor = sqlDB.query("Trip", allColumnsTrip, "TRIP_ID = " + insertId, null, null, null, null);
		cursor.moveToFirst();

		close();

		return cursorToTrip(cursor);
	}

	
	public DBobjWayPoint createWayPoint(int trip, double latitude, double longitude, long timestamp)
	{
		ContentValues values = new ContentValues();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);

		values.put("TRIP", trip);
		values.put("LATITUDE", latitude);
		values.put("LONGITUDE", longitude);
		values.put("RECORDING", dateFormat.format(new Date(timestamp)));

		openForWrite();

		long insertId = sqlDB.insert("GPSCoordinate", null, values);

		Cursor cursor = sqlDB.query("GPSCoordinate", allColumnsGPSCoordinate, "GPS_ID = " + insertId, null, null, null, null);
		cursor.moveToFirst();

		close();

		return cursorToWayPoint(cursor);
	}

	
	public DBobjPoi createPoi (int gpsCoordinate)
	{
		ContentValues values = new ContentValues();
		values.put("GPSCOORDINATE", gpsCoordinate);

		openForWrite();

		long insertId = sqlDB.insert("Memory", "PHOTO, VIDEO, NOTE", values);

		Cursor cursor = sqlDB.query("Memory", allColumnsMemory, "MEMORY_ID=" + insertId, null, null, null, null);
		cursor.moveToFirst();

		close();

		return cursorToPoi(cursor);
	}

	

	
	public int getLiveTripId()
	{
		String sqlQuery = "SELECT * FROM Trip WHERE LIVE=1";
		int livetrip = -1;

		openForRead();

		Cursor cursor = sqlDB.rawQuery(sqlQuery, null);
		cursor.moveToLast();

		if (cursor.getCount() != 0)
		{
			livetrip = cursorToTrip(cursor).getTripId();
			cursor.close();
		}

		close();

		return livetrip;
	}


	
	public List<DBobjRoute> getAllCompletedTrips()
	{
		List<DBobjRoute> tripList = new ArrayList<DBobjRoute>();
		String sqlQuery = "SELECT * FROM Trip WHERE LIVE=0";

		openForRead();

		Cursor cursor = sqlDB.rawQuery(sqlQuery, null);
		cursor.moveToFirst();

		if (cursor.getCount() > 0)
		{
			while (cursor.isAfterLast() == false)
			{
				DBobjRoute trip = cursorToTrip(cursor);
				tripList.add(trip);
				cursor.moveToNext();
			}

			cursor.close();
		}

		close();

		return tripList;
	}


	
	public List<DBobjWayPoint> getAllWayPointsForTrip(int tripId)
	{
		List<DBobjWayPoint> gpsCoordinateList = new ArrayList<DBobjWayPoint>();

		openForRead();

		Cursor cursor = sqlDB.query("GPScoordinate", allColumnsGPSCoordinate, "TRIP = " + tripId, null, null, null, null);
		cursor.moveToFirst();

		if (cursor.getCount() > 0)
		{
			while (cursor.isAfterLast() == false)
			{
				DBobjWayPoint gpsCoordinate = cursorToWayPoint(cursor);
				gpsCoordinateList.add(gpsCoordinate);
				cursor.moveToNext();
			}

			cursor.close();
		}

		close();

		return gpsCoordinateList;
	}

	public List<DBobjPoi> getAllPois()
	{
		List<DBobjPoi> memoryList = new ArrayList<DBobjPoi>();

		openForRead();

		Cursor cursor = sqlDB.query("Memory", allColumnsMemory, null, null, null, null, null);
		cursor.moveToFirst();

		if (cursor.getCount() > 0)
		{
			while (cursor.isAfterLast() == false)
			{
				DBobjPoi memory = cursorToPoi(cursor);
				memoryList.add(memory);
				cursor.moveToNext();
			}

			cursor.close();
		}

		close();

		return memoryList;
	}

	
	public List<DBobjPoi> getAllPoisFromTrip(int tripId)
	{
		List<DBobjPoi> memoryList = new ArrayList<DBobjPoi>();
		List<DBobjWayPoint> gpsCoordinates = new ArrayList<DBobjWayPoint>();

		gpsCoordinates = getAllWayPointsForTrip(tripId);

		if (gpsCoordinates.size() > 0)
		{
			openForRead();

			Cursor cursor;

			for (int i = 0; i < gpsCoordinates.size(); ++i)
			{
				cursor = sqlDB.query("Memory", allColumnsMemory, "GPSCOORDINATE=" + gpsCoordinates.get(i).getwayPointId(), null, null, null, null);
				cursor.moveToFirst();

				if (cursor.getCount() == 1)
				{
					while (cursor.isAfterLast() == false)
					{
						DBobjPoi memory = cursorToPoi(cursor);
						memoryList.add(memory);
						cursor.moveToNext();
					}

					cursor.close();
				}
			}

			close();
		}

		return memoryList;
	}

	

	
	public DBobjPoi getPoiWithID(int memoryId)
	{
		DBobjPoi memory = null;

		openForRead();

		Cursor cursor = sqlDB.query("Memory", allColumnsMemory, "MEMORY_ID=" + memoryId, null, null, null, null);
		cursor.moveToFirst();

		if (cursor.getCount() == 1)
			memory = cursorToPoi(cursor);

		cursor.close();
		close();

		return memory;
	}

	
	public DBobjRoute getTrip(int tripId)
	{
		DBobjRoute trip = null;

		openForRead();

		Cursor cursor = sqlDB.query("TRIP", allColumnsTrip, "TRIP_ID=" + tripId, null, null, null, null);
		cursor.moveToFirst();

		if (cursor.getCount() == 1)
			trip = cursorToTrip(cursor);

		cursor.close();
		close();

		return trip;
	}



	
	public void updateTrip(int tripId, String title, String comment, int live)
	{
		ContentValues values = new ContentValues(3);
		values.put("TITLE", title);
		values.put("COMMENT", comment);
		values.put("LIVE", live);

		openForWrite();

		sqlDB.update("Trip", values, "TRIP_ID=" + tripId, null);

		close();
	}

	
	public void updatePoi(int memoryId, int gpsCoordinate, String photo, String video, String note)
	{
		ContentValues values = new ContentValues(4);
		values.put("GPSCOORDINATE", gpsCoordinate);
		values.put("PHOTO", photo);
		values.put("VIDEO", video);
		values.put("NOTE", note);

		openForWrite();

		sqlDB.update("Memory", values, "MEMORY_ID=" + memoryId, null);

		close();
	}


	
	public void deleteTrip(int tripId)
	{
		openForWrite();

		sqlDB.delete("Trip", "TRIP_ID=" + tripId, null);

		close();
	}

	
	public void deleteTrip(String title, String comment)
	{
		openForWrite();

		sqlDB.delete("Trip", "TITLE='" + title + "' AND COMMENT='" + comment + "'", null);

		close();
	}

	
	public void deleteWayPointWithID(int gpsCoordinateId)
	{
		openForWrite();

		sqlDB.delete("GPSCoordinate", "GPS_ID=" + gpsCoordinateId, null);

		close();
	}

	
	public void deleteGPSCoordinateByTrip(int tripId)
	{
		openForWrite();

		sqlDB.delete("GPSCoordinate", "TRIP=" + tripId, null);

		close();
	}

	
	public void deletePoiWithID(int memoryId)
	{
		openForWrite();

		sqlDB.delete("Memory", "MEMORY_ID=" + memoryId, null);

		close();
	}

	public DBobjRoute cursorToTrip(Cursor cursor)
	{
		DBobjRoute trip = new DBobjRoute();
		trip.setTripId(cursor.getInt(0));
		if (cursor.getString(1)==null) {
			trip.setTitelText("");
		} else {
			trip.setTitelText(cursor.getString(1));
		}
		if (cursor.getString(2)==null) {
			trip.setNoteText("");
		} else {
			trip.setNoteText(cursor.getString(2));
		}
		trip.setIsTracked(cursor.getInt(3));

		return trip;
	}

	
	public DBobjWayPoint cursorToWayPoint(Cursor cursor)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
		DBobjWayPoint gpsCoordinate = new DBobjWayPoint();
		gpsCoordinate.setwayPointId(cursor.getInt(0));
		gpsCoordinate.setTripId(cursor.getInt(1));
		gpsCoordinate.setLatitude(cursor.getDouble(2));
		gpsCoordinate.setLongitude(cursor.getDouble(3));
		gpsCoordinate.setAltitude(cursor.getDouble(4));
		try
		{
			gpsCoordinate.setTimeStamp(dateFormat.parse(cursor.getString(5)));
		} catch (ParseException e)
		{}

		return gpsCoordinate;
	}

	
	public DBobjPoi cursorToPoi(Cursor cursor)
	{
		DBobjPoi newPoi = new DBobjPoi();
		newPoi.setPoiId(cursor.getInt(0));
		newPoi.setWayPointID(cursor.getInt(1));
		if (cursor.getString(2)==null) {
			newPoi.setFotoPath("");
		} else {
			newPoi.setFotoPath(cursor.getString(2));
		}
		if (cursor.getString(3)==null) {
			newPoi.setVideoPath("");
		} else {
			newPoi.setVideoPath(cursor.getString(3));
		}
		if (cursor.getString(4)==null) {
			newPoi.setNoteText("");
		} else {
			newPoi.setNoteText(cursor.getString(4));
		}
		return newPoi;
	}
}