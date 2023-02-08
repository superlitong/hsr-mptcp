package thu.kejiafan.mobinet;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.baidu.frontia.api.FrontiaStatistics;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Config {
	// Baidu API
	public static FrontiaStatistics statistics;
	public static final String reportId = "3b70a11423";
	public final static String APIKEY = "73kSsln0gDCir7PINzkzNtjy";
	public static boolean isBtnRun = false;
	
	// viewpager
	public static ViewPager mPager;
    public static ArrayList<Fragment> fragmentsList;
    public static ImageView ivBottomLine;
    public static TextView tvTabPhone;
	public static TextView tvTabNetwork;
	public static TextView tvTabAbout;

    public static int currIndex = 0;
    public static int bottomLineWidth;
    public static int offset = 0;
    public static int position_one;
    public static int position_two;
    public static int position_three;
    public static Resources resources;
    
    // file for log
//    static SimpleDateFormat dirDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	static String[] filename = new String[] { "Mobile.txt", "Signal.txt",
			"Speed.txt", "Cell.txt", "Uplink.txt", "Downlink.txt", "Ping.txt",
			"DNS.txt" }; // "Addition.txt"
    static FileOutputStream fosMobile = null;
	static FileOutputStream fosSignal = null;
	static FileOutputStream fosSpeed = null;
	static FileOutputStream fosCell = null;
	static FileOutputStream fosUplink = null;
	static FileOutputStream fosDownlink = null;
	static FileOutputStream fosPing = null;
	static FileOutputStream fosDNS = null;
    
	// variable for signalStrength
 	static int gsmSignalStrength = 99; // Valid values are (0-31, 99) as defined in TS 27.007 8.5
 	static int gsmBitErrorRate = -1;   // bit error rate (0-7, 99) as defined in TS 27.007 8.5
 	static int cdmaDbm = -1;   // This value is the RSSI value
 	static int cdmaEcio = -1;  // This value is the Ec/Io
 	static int evdoDbm = -1;   // This value is the EVDO RSSI value
 	static int evdoEcio = -1;  // This value is the EVDO Ec/Io
 	static int evdoSnr = -1;   // Valid values are 0-8.  8 is the highest signal to noise ratio
 	static int lteSignalStrength = -1;
 	static int lteRsrp = -1;
 	static int lteRsrq = -1;
 	static int lteRssnr = -1;
 	static int lteCqi = -1;
 	static int currentLevel = -1;
 	
 	// phone Information
 	static String phoneModel = null;
	static String osVersion = null;
 	static String providerName = null;	
	static String IMEI = null;
	static String IMSI = null;
	static String subtypeName = null;
	
	// phone widget
	static TextView tvPhoneModel;
	static TextView tvosVersion;
	static TextView tvOperator;
	static TextView tvNetworkType;
	static TextView tvDataConnection;
	static TextView tvSignalStrength;
	static TextView tvCurrentCell;
	static TextView tvCurrentLocation;
	static TextView tvHandoffNumber;
	static TextView tvGpsState; 
	static TextView tvSatellite;
	static TextView tvSystemTime;
	
	// phone state
	static String networkTypeString = "";
	static int netTypeID = 0;
	static String signalStrengthString = "";
	static String SignalParameterString = "";
	static String dataConnectionState = "";
	static int disconnectNumber = 0;
	static int handoffNumber = -1;
	static int lastcellid = -1;
	static boolean lastConnect = false;	
	static TelephonyManager telManager;
	static int phoneEvents = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
			| PhoneStateListener.LISTEN_SERVICE_STATE
			| PhoneStateListener.LISTEN_CELL_LOCATION
			| PhoneStateListener.LISTEN_DATA_ACTIVITY
			| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
	
	// network widget
	static Button btnRun;
	static TextView tvDataConnectionState;
	static TextView tvWiFiConnection;
	static TextView tvWiFiInfo;
	static TextView tvMacAddress;
	static TextView tvIPAddress;
	static TextView tvThroughput;
	static TextView tvDNSLatency;
	static TextView tvPingLatency;
	static TextView tvTestReport;
	static ProgressDialog mDialog;
	
	// network state
	static String testServerip = "202.112.3.74";
	static String testMeasuretime = "2"; //1
	static String testInterval = "5";
	static TCPTest myTcpTest = null;
	static int tcpUploadPort = 5001;
	static int tcpDownloadPort = 5002;
	static int measurementID = 0;
	static String addressSina = "3g.sina.com.cn";
	static String addressBaidu = "m.baidu.com";
	static String wifiState = null;
	static String lastWifiState = null;
	static String wifiInfo = null;
	static String macAddress = null;
	static String ipAddress = null;
	static int testFlag = 0;
	static String pingInfo = "";
	static String dnsLookupInfo = "";
	static String httpInfo = "";
	static String mAvgUplinkThroughput = "0";
	static String mAvgDownlinkThroughput = "0";
	
	// gps content
	static SimpleDateFormat contentDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
	static SimpleDateFormat sysDateFormat = new SimpleDateFormat("HH:mm:ss");
	static long startTime = 0;
	static String totalTime = "";
	static String gpsStateString = "";
	static String mobilitySpeed = "";
	static float speed;
	static double latitude;
	static double longitude;
	static double accuracy;
	static LocationManager locationManager;
	static Location loc;
	static Criteria criteria;
	static String bestProvider = null;
	static String gpsAvailableNumber = "";
	static String gpsFixNumber = "";
	static String gpsTime = null;
	static boolean isGPSPrepared = false;
	
	// write log content
	static String lastSignalContent = "";
	static String lastSpeedContent = "";
	static String lastCellInfoContent = "";
	static String lastDataContentString = "";
}
