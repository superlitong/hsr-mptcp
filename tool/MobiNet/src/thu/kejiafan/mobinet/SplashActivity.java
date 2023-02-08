package thu.kejiafan.mobinet;

import java.io.FileNotFoundException;
import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Window;

public class SplashActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash_screen);
	    // Make sure the splash screen is shown in portrait orientation
	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
	    
	    
	    createFilePath();// 创建日志路径		
        getTelephoneInfo();
		writeLog();
		
	    new Handler().postDelayed(new Runnable() {  
            public void run() {  
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(mainIntent);  
                SplashActivity.this.finish();  
            }
        }, 2000);
	}
	
	private void createFilePath() {
		try {
			Config.fosMobile = this.openFileOutput("Mobile.txt", Context.MODE_PRIVATE);
			Config.fosSignal = this.openFileOutput("Signal.txt", Context.MODE_PRIVATE);
			Config.fosSpeed = this.openFileOutput("Speed.txt", Context.MODE_PRIVATE);
			Config.fosCell = this.openFileOutput("Cell.txt", Context.MODE_PRIVATE);
			Config.fosUplink = this.openFileOutput("Uplink.txt", Context.MODE_PRIVATE);
			Config.fosDownlink = this.openFileOutput("Downlink.txt", Context.MODE_PRIVATE);
			Config.fosPing = this.openFileOutput("Ping.txt", Context.MODE_PRIVATE);
			Config.fosDNS = this.openFileOutput("DNS.txt", Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getTelephoneInfo() {
    	Config.phoneModel = Build.MODEL + "  Inc:" + Build.MANUFACTURER;
		Config.osVersion = Build.VERSION.RELEASE + "  Level:" + Build.VERSION.SDK_INT;
		
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Config.providerName = telephonyManager.getNetworkOperatorName();
		Config.IMSI = telephonyManager.getSubscriberId();
		if (Config.providerName == null) {
			if (Config.IMSI.startsWith("46000")
					|| Config.IMSI.startsWith("46002")
					|| Config.IMSI.startsWith("46007")) {
				Config.providerName = "中国移动";
			} else if (Config.IMSI.startsWith("46001")) {
				Config.providerName = "中国联通";
			} else if (Config.IMSI.startsWith("46003")) {
				Config.providerName = "中国电信";
			} else {
				Config.providerName = "非大陆用户";
			}
		}
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		Config.subtypeName = networkInfo.getSubtypeName();
  	}
	
	void writeLog() {
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String infoString = "PhoneModel=" + Config.phoneModel + 
				"\nosVersion=" + Config.osVersion + 
				"\nProviderName=" + Config.providerName + 
				"\nDetailedState=" + networkInfo.getDetailedState() + 
				"\nReason=" + networkInfo.getReason() + 
				"\nSubtypeName=" + networkInfo.getSubtypeName() + 
				"\nExtraInfo=" + networkInfo.getExtraInfo() + 
				"\nTypeName=" + networkInfo.getTypeName() + 
				"\nIMEI=" + telephonyManager.getDeviceId() + 
				"\nIMSI=" + telephonyManager.getSubscriberId() + 
				"\nNetworkOperatorName=" + telephonyManager.getNetworkOperatorName() + 
				"\nSimOperatorName=" + telephonyManager.getSimOperatorName() + 
				"\nSimSerialNumber=" + telephonyManager.getSimSerialNumber();
		try {
			if (Config.fosMobile != null) {
				Config.fosMobile.write(infoString.getBytes());
				Config.fosMobile.write(System.getProperty("line.separator").getBytes());
			}
//			Config.summaryFileName = Config.phoneModel + Config.dirDateFormat.format(new Date(System.currentTimeMillis())) + ".txt";
//			Config.fosSummary = this.openFileOutput(Config.summaryFileName, Context.MODE_APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
