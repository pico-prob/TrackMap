package de.mmtech.trackmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					startActivity(new Intent(SplashScreenActivity.this, MainMenu.class));
					finish();
				}
			}
		};
		timer.start();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
	}
}