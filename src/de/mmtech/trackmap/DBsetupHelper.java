package de.mmtech.trackmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBsetupHelper extends SQLiteOpenHelper
{
	
	private static final String DATABASE_NAME = "TrackMap.db";

	
	private static final int DATABASE_VERSION = 1;

	
	private static final String CREATE_TABLE_TRIP = 
			"CREATE TABLE Trip (" +
			"trip_id  INTEGER NOT NULL PRIMARY KEY, " +
			"title text NOT NULL, " +
			"comment text, " +
			"live integer(1) DEFAULT 1 NOT NULL" +
			")";

	
	private static final String CREATE_TABLE_GPSCOORDINATE = 
			"CREATE TABLE GPSCoordinate (" +
			"gps_id INTEGER NOT NULL PRIMARY KEY, " +
			"trip INTEGER REFERENCES reise_id ON DELETE CASCADE, " +
			"latitude real(10) DEFAULT 0.0 NOT NULL, " +
			"longitude real(10) DEFAULT 0.0 NOT NULL, " +
			"altitude real(10) DEFAULT 0.0 NOT NULL, " +
//			"time INTEGER DEFAULT 0.0 NOT NULL, " +
			"recording text NOT NULL" +
			")";

	
	private static final String CREATE_TABLE_MEMORY = 
			"CREATE TABLE Memory (" +
			"memory_id INTEGER NOT NULL PRIMARY KEY, " +
			"gpscoordinate INTEGER REFERENCES gps_id ON DELETE CASCADE, " +
			"photo text, video text, note text" +
			")";

	
	private static final String DROP_TABLE_TRIP = 
			"DROP TABLE IF EXISTS Trip";

	
	private static final String DROP_TABLE_GPSCOORDINATE = 
			"DROP TABLE IF EXISTS GPSCoordinate";

	
	private static final String DROP_TABLE_MEMORY = 
			"DROP TABLE IF EXISTS memory";

	
	public DBsetupHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	
	@Override
	public void onCreate(SQLiteDatabase database)
	{
		database.execSQL("PRAGMA foreign_keys=ON;");
		database.execSQL(CREATE_TABLE_TRIP);
		database.execSQL(CREATE_TABLE_MEMORY);
		database.execSQL(CREATE_TABLE_GPSCOORDINATE);
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(DROP_TABLE_GPSCOORDINATE);
		db.execSQL(DROP_TABLE_MEMORY);
		db.execSQL(DROP_TABLE_TRIP);
		onCreate(db);
	}
}