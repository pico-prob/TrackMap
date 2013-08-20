package de.mmtech.trackmap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


public class PoiEditor extends Activity implements OnClickListener {

	private static final int PHOTO_RETURN_INTEND_ID = 0;
	private static final int VIDEO_RETURN_INTEND_ID = 1;
	public static final int POI_RETURN_INTEND_ID = 1;
	public static final String PHOTO_PATH_BACKUP_ID = "de.mmtech.trackmap.poiEditor.photo";
	public static final String POI_BACKUP_ID = "de.mmtech.trackmap.poiEditor.poi";
 
	private File bufferFile;
	private String fotoPath;
	private String videoPath;
	private Poi myPoi;
	private EditText noteTextField;
	private boolean readOnlyModus=false;
	private boolean removePoiIfCancel=false;
	private boolean isVideoRecorded=false;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.poi_editor);
		noteTextField=(EditText) findViewById(R.id.txt_poiNoteText);
        findViewById(R.id.btn_takeFoto).setOnClickListener(this);
        findViewById(R.id.btn_takeVideo).setOnClickListener(this);
        findViewById(R.id.btn_poi_save).setOnClickListener(this);
        findViewById(R.id.btn_poi_cancel).setOnClickListener(this);
        
        int newPoiWayPointID=getIntent().getIntExtra("wayPointID", -2);
        if (newPoiWayPointID==-2) {
            myPoi=Poi.fromIntent(getIntent());
            removePoiIfCancel=false;
		} else {
			myPoi=new Poi (newPoiWayPointID);
			removePoiIfCancel=true;
		}
        if (myPoi!=null) {
        	Log.d ("debug","PoiEditor: loaded Poi with ID: "+myPoi.getPoiID());
			if (myPoi.isEmpty()) {
				editPoi();
			} else {
				showPoi();
			}
		} else {
			finish();
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		if (!myPoi.getVideoPath().isEmpty()) {
			// let show-video-button appear
	        ((Button)findViewById(R.id.btn_takeVideo))
    		.setText(getString(R.string.label_btnVideoPlay));
            ((Button)findViewById(R.id.btn_takeVideo))
    			.setVisibility(View.VISIBLE);
		}
		super.onResume();
	}
	
	private File prepareFileForMedia (String mediaFileExtension){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		if (mediaFileExtension.equals(".jpg")) {
			String filename = "IMG_" + dateFormat.format(new Date()) + mediaFileExtension;
			return new File(Environment.getExternalStorageDirectory() + getString(R.string.app_path_image), filename);
		}
		if (mediaFileExtension.equals(".mp4")) {
			String filename = "MOV_" + dateFormat.format(new Date()) + mediaFileExtension;
			return new File(Environment.getExternalStorageDirectory() + getString(R.string.app_path_video), filename);
		}
		return null;
	}
	
	@SuppressLint("SimpleDateFormat")
	private void startTakePhoto () {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		bufferFile= prepareFileForMedia(".jpg");
		this.fotoPath = bufferFile.getAbsolutePath();
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(bufferFile));
		startActivityForResult(cameraIntent, PHOTO_RETURN_INTEND_ID);
		Log.d ("debug","PoiEditor: new Foto");		
	}
	
	@SuppressLint("SimpleDateFormat")
	private void startVideoRecorder () {
		Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		bufferFile= prepareFileForMedia(".mp4");
		this.videoPath = bufferFile.getAbsolutePath();
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(bufferFile));
		startActivityForResult(cameraIntent, VIDEO_RETURN_INTEND_ID);
		Log.d ("debug","PoiEditor: new Video");		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent){
		if (resultCode == RESULT_OK){
			if (bufferFile.exists()){
				if (requestCode == PHOTO_RETURN_INTEND_ID) {
					myPoi.setFotoPath(fotoPath);
					displayFoto();
				}
				if (requestCode == VIDEO_RETURN_INTEND_ID){
					myPoi.setVideoPath(videoPath);
				}
			} else {
				Toast.makeText(this, getString(R.string.file_not_exists), Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	private void displayFoto () {
		Bitmap rawPic = BitmapFactory.decodeFile(this.fotoPath);
		Bitmap picResized = Bitmap.createScaledBitmap(rawPic, 512, 512, false);
//		BitmapDrawable picDrawable = new BitmapDrawable(getResources(), picResized);
		ImageView view = (ImageView) findViewById(R.id.image_view_for_poi);
		view.setImageBitmap(picResized);
	}
	
	private void showVideo() {
		startActivity(new Intent(
				this,
				VideoPlayer.class)
				.putExtra(VideoPlayer.VIDEOPLAYER_INTEND_ID, myPoi.getVideoPath())
				);
	}
	
	private void removePoi(){
		if (!myPoi.getFotoPath().equals("")) {
			new File (myPoi.getFotoPath()).delete();
		}
		if (!myPoi.getVideoPath().equals("")) {
			new File (myPoi.getVideoPath()).delete();
		}
		myPoi.removeMe();
	}

	
	private void savePoiAndQuit() {
		String newNoteText = noteTextField.getText().toString();
		myPoi.setNoteText(newNoteText);
		Intent resultIntent = new Intent();
		resultIntent.putExtra(Poi.INTENT_ID, myPoi);
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
	
	private void editPoi () {
		noteTextField.setText(myPoi.getNoteText());
	}
	
	private void showPoi () {
        ((Button)findViewById(R.id.btn_poi_cancel))
        		.setText(getString(R.string.label_btnPoiOK));
        ((Button)findViewById(R.id.btn_poi_save))
			.setVisibility(View.GONE);
        ((Button)findViewById(R.id.btn_takeFoto))
			.setVisibility(View.GONE);
        ((Button)findViewById(R.id.btn_takeVideo))
			.setVisibility(View.GONE);
    	noteTextField.setEnabled(false);
    	noteTextField.setText(myPoi.getNoteText());
    	fotoPath=myPoi.getFotoPath();
    	if (!fotoPath.isEmpty()) {
    		displayFoto();
		}
	}
	
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_takeFoto:
			this.startTakePhoto();
			break;

		case R.id.btn_poi_save:
			savePoiAndQuit();
			break;
			
		case R.id.btn_poi_cancel:
			if (removePoiIfCancel) {
				removePoi();
			}
			finish();
			break;
			
		case R.id.btn_takeVideo:
			if (myPoi.getVideoPath().isEmpty()) {
				startVideoRecorder();
			} else {
				showVideo();
			}
			break;
		}
	}
//	@Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        
//        outState.putString(PHOTO_PATH_BACKUP_ID, bufferFile.getAbsolutePath().toString());
//        outState.putSerializable(POI_BACKUP_ID, myPoi);
//        //outState.putString(TEXT_NOTE, newN)
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        this.fotoPath = savedInstanceState.getString(PHOTO_PATH_BACKUP_ID);
//        this.myPoi = (Poi) savedInstanceState.getSerializable(POI_BACKUP_ID);
//    }
}
