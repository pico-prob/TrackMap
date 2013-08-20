package de.mmtech.trackmap;

public class DBobjPoi
{
	private String fotoPath;
	private String videoPath;
	private String noteText;
	private int poiID;
	private int wayPointID;

	public int getPoiID()
	{
		return poiID;
	}

	public void setPoiId(int new_poi_id)
	{
		poiID = new_poi_id;
	}

	public int getWayPointID()
	{
		return wayPointID;
	}

	public void setWayPointID(int new_wayPointID)
	{
		wayPointID = new_wayPointID;
	}

	public String getFotopath()
	{
		return fotoPath;
	}

	public void setFotoPath(String new_foto)
	{
		fotoPath = new_foto;
	}

	public String getVideoPath()
	{
		return videoPath;
	}

	public void setVideoPath(String new_video)
	{
		videoPath = new_video;
		
	}

	public String getNoteText()
	{
		return noteText;
	}

	public void setNoteText(String new_note)
	{
		noteText = new_note;
	}

	@Override
	public String toString()
	{
		return noteText;
	}
}
