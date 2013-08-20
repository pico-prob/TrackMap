package de.mmtech.trackmap;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ShowHelpActivity extends Activity{

	private WebView myWebView;
	private WifiManager myWifiManager;
	
	private class WebClient extends WebViewClient
	{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			view.loadUrl(url);
			return true;
		}
	}

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_help);
		initComponents();
		if (checkIfDeviceIsOnline()){
			myWebView.loadUrl(getString(R.string.webhelp_url));
		} else {
			Toast.makeText(this, getString(R.string.help_msg_offline), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void initComponents()
	{
		myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		myWebView = (WebView) findViewById(R.id.webView_help);
		myWebView.setWebViewClient(new WebClient());
	}

	private boolean checkIfDeviceIsOnline()
	{
		if (myWifiManager.getConnectionInfo().getNetworkId() == -1)	{
			return false;
		} else{
			return true;
		}
	}
}
