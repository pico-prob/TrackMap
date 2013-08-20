package de.mmtech.trackmap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayer extends Activity implements OnClickListener {

	public static final String VIDEOPLAYER_INTEND_ID = "de.mmtech.trackmap.VideoPlayer.videopath";
	String myVideoPath ="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myVideoPath=getIntent().getStringExtra(VIDEOPLAYER_INTEND_ID);
		if (!myVideoPath.isEmpty()) {
			setContentView(R.layout.activity_video_player);
	        VideoView videoView = (VideoView)findViewById(R.id.videoView);
	        getWindow().clearFlags(WindowManager
	                            .LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	                            WindowManager.LayoutParams.FLAG_FULLSCREEN);

	        MediaController myMediaController = new MediaController(this);
		    myMediaController.setAnchorView(videoView);
		    videoView.setMediaController(myMediaController);
		    videoView.setVideoPath(myVideoPath);
		    videoView.start();
		}
	}

	@Override
    public void onClick(View v) {
            finishActivity(0);
            finish();
    }

}
