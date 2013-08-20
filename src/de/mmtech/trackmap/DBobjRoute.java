package de.mmtech.trackmap;


public class DBobjRoute
{
	private int tripID;
	private String titelText;
	private String noteText;
	private int isTracked;

	
	public int getTripId()
	{
		return tripID;
	}


	public void setTripId(int new_tripid)
	{
		tripID = new_tripid;
	}


	public String getTitelText()
	{
		return titelText;
	}


	public void setTitelText(String new_titel)
	{
		titelText = new_titel;
	}


	public String getNoteText()
	{
		return noteText;
	}

	
	public void setNoteText(String new_noteText)
	{
		noteText = new_noteText;
	}

	
	public int getIsTracked()
	{
		return isTracked;
	}

	public void setIsTracked(int new_tracked)
	{
		isTracked = new_tracked;
	}

	
	@Override
	public String toString()
	{
		if (getNoteText().length() != 0)
			return titelText + " [" + noteText + "]";
		else
			return titelText;
	}
}
