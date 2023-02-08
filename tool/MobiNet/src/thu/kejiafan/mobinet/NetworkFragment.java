package thu.kejiafan.mobinet;

import thu.kejiafan.mobinet.R.id;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class NetworkFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.network, container, false);
		initWidget(view);
		handler4Wifi.post(runnable4Wifi);
		
		return view;
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub		
		super.onPause();
		
		Config.statistics.pageviewEnd(this, "NetworkFragment");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub		
		super.onResume();
		
		Config.statistics.pageviewStart(this, "NetworkFragment");
	}
	
	private void initWidget(View view) {
    	Config.btnRun = (Button) view.findViewById(id.btnRun);
    	Config.tvDataConnectionState = (TextView) view.findViewById(id.dataConnection);
    	Config.tvWiFiConnection = (TextView) view.findViewById(id.wifiConnection);
		Config.tvMacAddress = (TextView) view.findViewById(id.macAddress);
		Config.tvIPAddress = (TextView) view.findViewById(id.ipAddress); 
		Config.tvWiFiInfo = (TextView) view.findViewById(id.wifiInfo);
		Config.tvPingLatency = (TextView) view.findViewById(id.pingLatency);
		Config.tvDNSLatency = (TextView) view.findViewById(id.dnsLatency);
		Config.tvTestReport = (TextView) view.findViewById(id.testState);
		Config.tvThroughput = (TextView) view.findViewById(id.throughput);
	    
	    Config.btnRun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub

				if (Config.wifiState.equals("Disconnected")) {
					if (Config.dataConnectionState.equals("Disconnected")
							|| Config.dataConnectionState.equals("Unknown")) {
						Config.tvTestReport.setText("网络已断开，请检查网络连接");
						return;
					}
				}
				
				Config.testFlag = 0;
				Config.isBtnRun = true;
				Config.mDialog = new ProgressDialog(getActivity());
				Config.mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				Config.mDialog.setTitle("MobiNet");
				Config.mDialog.setIcon(R.drawable.ic_launcher);
				Config.mDialog.setMessage("testing...");
				Config.mDialog.setIndeterminate(false);
				Config.mDialog.setCancelable(true);
				Config.mDialog.getWindow().setGravity(Gravity.BOTTOM);
				Config.mDialog.show();
				
				Config.tvTestReport.setText("DNS lookup testing...");
				Config.mDialog.setMessage("DNS lookup testing...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler4Test.removeCallbacks(runnable4Test);
				handler4Test.post(runnable4Test);
				Measurement.dnsLookupTest(Config.testServerip, 10);
				
				Config.tvTestReport.setText("Ping testing...");
				Config.mDialog.setMessage("Ping testing...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler4Test.removeCallbacks(runnable4Test);
				handler4Test.post(runnable4Test);
				Measurement.pingCmdTest(Config.testServerip, 10);
			}

		});
	}
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				Config.btnRun.setEnabled(false);
				Config.tvTestReport.setText("Connecting...");
			} else if (msg.what == 1) {
				Config.tvTestReport.setText("Client has connected to server");
			} else if (msg.what == 2) {
				Config.tvTestReport.setText("Reconnecting...");
			} else if (msg.what == 3) {
				Config.tvTestReport.setText("TCP downlink test finished");
				if (Config.mDialog.isShowing()) {
					Config.mDialog.setMessage("TCP downlink test finished");
				}				
				handler4Show.removeCallbacks(runnable4Show);
				Config.tvTestReport.setText("TCP uplink testing...");
				if (Config.mDialog.isShowing()) {
					Config.mDialog.setMessage("TCP uplink testing...");
				}				
				Config.myTcpTest = new TCPTest(mHandler, Config.testServerip, "60",
						"5", Config.fosUplink, 2);
				handler4Show2.post(runnable4Show2);
			} else if (msg.what == 4) {
				Config.mDialog.dismiss();
				Config.mDialog.cancel();
				handler4Show2.removeCallbacks(runnable4Show2);
				Config.tvThroughput.setText("平均上行:"
						+ Config.mAvgUplinkThroughput + " 平均下行:"
						+ Config.mAvgDownlinkThroughput + " kbps");
				int tp = Integer.valueOf(Config.mAvgDownlinkThroughput) + Integer.valueOf(Config.mAvgUplinkThroughput);
				if (tp > 600) {
					Config.tvTestReport.setText("您的网速太快了...不敢相信");
				} else if (tp > 400) {
					Config.tvTestReport.setText("您的网速不错了...要知足啦");
				} else if (tp > 200) {
					Config.tvTestReport.setText("您的网速还能用...继续加油");
				} else if (tp <= 200) {
					Config.tvTestReport.setText("您的网速有点惨...不要伤心");
				}
				Config.btnRun.setEnabled(true);
			}
		};
	};
	
	private Handler handler4Show = new Handler();

	private Runnable runnable4Show = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4Show.postDelayed(runnable4Show, 1000);
			if (Config.mDialog.isShowing()) {
				Config.mDialog.setMessage("下行速率: " + Config.myTcpTest.mDownlinkThroughput + " kbps");
			}			
			Config.tvThroughput.setText("下行:" + Config.myTcpTest.mDownlinkThroughput + " kbps");
		}
	};
	
	private Handler handler4Show2 = new Handler();

	private Runnable runnable4Show2 = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4Show2.postDelayed(runnable4Show2, 1000);
			if (Config.mDialog.isShowing()) {
				Config.mDialog.setMessage("上行速率: " + Config.myTcpTest.mUplinkThroughput + " kbps");
			}			
			Config.tvThroughput.setText("上行:" + Config.myTcpTest.mUplinkThroughput + " 下行:" 	+ Config.mAvgDownlinkThroughput + " kbps");
		}
	};
	
	private Handler handler4Wifi = new Handler();

	private Runnable runnable4Wifi = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Config.tvDataConnectionState.setText(Config.dataConnectionState);
			/**
			 * 是否连接Wifi
			 */
			try {
				ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

				if (activeNetInfo != null
						&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					
					WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					Config.wifiState = "Connected:" + wifiInfo.getSSID();
					Config.wifiInfo = "RSSI:" + wifiInfo.getRssi() + " LinkSpeed:" + wifiInfo.getLinkSpeed();
					Config.macAddress = wifiInfo.getMacAddress();
					Config.ipAddress = SignalUtil.int2IP(wifiInfo.getIpAddress());
					Config.tvWiFiConnection.setText(Config.wifiState);
					Config.tvMacAddress.setText(Config.macAddress);
					Config.tvIPAddress.setText(Config.ipAddress);
					
				} else {
					Config.wifiState = "Disconnected";
					Config.tvWiFiConnection.setText(Config.wifiState);
					Config.wifiInfo = "WiFi连接后有效";
				}	
				Config.tvWiFiInfo.setText(Config.wifiInfo);
			} catch (Exception e) {
				// TODO: handle exception
			}

			handler4Wifi.postDelayed(runnable4Wifi, 5000);
		}
	};
	
	private Handler handler4Test = new Handler();

	private Runnable runnable4Test = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4Test.postDelayed(runnable4Test, 1000);
			if (Config.testFlag == 11) {
				Config.tvPingLatency.setText(Config.pingInfo + " ms");
				if (Config.mDialog.isShowing()) {
					Config.mDialog.setMessage("Ping时延: " + Config.pingInfo + " ms");
				}
				Config.tvTestReport.setText("Ping test finished");
				Config.testFlag = 10;
				
				Config.tvTestReport.setText("TCP downlink testing...");
				if (Config.mDialog.isShowing()) {
					Config.mDialog.setMessage("TCP downlink testing...");
				}				
				Config.myTcpTest = new TCPTest(mHandler, Config.testServerip, "60",
						"5", Config.fosDownlink, 1);
				handler4Show.post(runnable4Show);
			} else if (Config.testFlag == 12) {
				if (Config.mDialog.isShowing()) {
					Config.mDialog.setMessage("Ping test failed");
				}
				Config.tvTestReport.setText("Ping test failed");
				Config.testFlag = 10;
				
				Config.tvTestReport.setText("TCP downlink testing...");
				if (Config.mDialog.isShowing()) {
					Config.mDialog.setMessage("TCP downlink testing...");
				}				
				Config.myTcpTest = new TCPTest(mHandler, Config.testServerip, "60",
						"5", Config.fosDownlink, 1);
				handler4Show.post(runnable4Show);
			} else if (Config.testFlag == 13) {
				Config.tvTestReport.setText("您的机型暂不支持 请关注后续版本");
				Config.testFlag = 10;
			} else if (Config.testFlag == 21) {
				Config.tvDNSLatency.setText(Config.dnsLookupInfo + " ms");
//				if (Config.mDialog.isShowing()) {
//					Config.mDialog.setMessage("DNS lookup时延: " + Config.dnsLookupInfo + " ms");
//				}
				Config.tvTestReport.setText("DNS lookup test finished");
				Config.testFlag = 20;
			} else if (Config.testFlag == 22) {
				if (Config.mDialog.isShowing()) {
					Config.mDialog.setMessage("DNS lookup test failed");
				}
				Config.tvTestReport.setText("DNS lookup test failed");
				Config.testFlag = 20;
			}
		}
	};
}