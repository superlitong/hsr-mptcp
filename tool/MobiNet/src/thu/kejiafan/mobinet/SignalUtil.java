package thu.kejiafan.mobinet;

import android.telephony.TelephonyManager;

public class SignalUtil {
	public static void getCurrentnetworkTypeString(int type) {
		switch (type) {
		case TelephonyManager.NETWORK_TYPE_GPRS:// 2.5G
			Config.networkTypeString = "GPRS";
			Config.netTypeID = 2;
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:// 2.5G
			Config.networkTypeString = "EDGE";
			Config.netTypeID = 2;
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:// 3G
			Config.networkTypeString = "UMTS";
			Config.netTypeID = 3;
			break;
		case TelephonyManager.NETWORK_TYPE_HSDPA:// 3.5G
			Config.networkTypeString = "HSDPA";
			Config.netTypeID = 3;
			break;
		case TelephonyManager.NETWORK_TYPE_HSUPA:// 3.5G
			Config.networkTypeString = "HSUPA";
			Config.netTypeID = 3;
			break;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			Config.networkTypeString = "HSPA";
			Config.netTypeID = 3;
			break;
		case TelephonyManager.NETWORK_TYPE_HSPAP:// 3.75G
			Config.networkTypeString = "HSPA+";
			Config.netTypeID = 3;
			break;
		case TelephonyManager.NETWORK_TYPE_CDMA:// 2G
			Config.networkTypeString = "CDMA";
			Config.netTypeID = 4;
			break;
		case TelephonyManager.NETWORK_TYPE_1xRTT:// 2.5G
			Config.networkTypeString = "1xRTT";
			Config.netTypeID = 4;
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			Config.networkTypeString = "EVDO0";
			Config.netTypeID = 4;
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:// 3.5G
			Config.networkTypeString = "EVDOA";
			Config.netTypeID = 4;
			break;
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			Config.networkTypeString = "EVDOB";
			Config.netTypeID = 4;
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:// 4G
			Config.networkTypeString = "LTE";
			Config.netTypeID = 5;
			break;
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			Config.networkTypeString = "eHRPD";
			break;
		case TelephonyManager.NETWORK_TYPE_IDEN:// 2G
			Config.networkTypeString = "iDen";
			break;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			Config.networkTypeString = "Unknown";
			break;
		default:
			Config.networkTypeString = "Other";
			break;
		}
	}

	public static int getCurrentLevel(boolean isGsm) {
		int level;
		if (isGsm) {
			if ((Config.lteSignalStrength == -1) && (Config.lteRsrp == -1)
					&& (Config.lteRsrq == -1) && (Config.lteRssnr == -1)
					&& (Config.lteCqi == -1)) {
				level = getGsmLevel();
			} else {
				level = getLteLevel();
			}
		} else {
			int cdmaLevel = getCdmaLevel();
			int evdoLevel = getEvdoLevel();
			if (evdoLevel == 0) {
				/** We don't know evdo, use cdma */
				level = getCdmaLevel();
			} else if (cdmaLevel == 0) {
				/** We don't know cdma, use evdo */
				level = getEvdoLevel();
			} else {
				/** We know both, use the lowest level */
				level = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
			}
		}
		return level;
	}

	public static int getGsmLevel() {
		int level;
		// ASU ranges from 0 to 31 - TS 27.007 Sec 8.5
		// asu = 0 (-113dB or less) is very weak
		// signal, its better to show 0 bars to the user in such cases.
		// asu = 99 is a special case, where the signal strength is unknown.
		int asu = Config.gsmSignalStrength;
		if (asu <= 2 || asu == 99)
			level = 0;
		else if (asu >= 12)
			level = 4;
		else if (asu >= 8)
			level = 3;
		else if (asu >= 5)
			level = 2;
		else
			level = 1;
		return level;
	}

	public static int getCdmaLevel() {
		final int cdmaDbm = Config.cdmaDbm;
		final int cdmaEcio = Config.cdmaEcio;
		int levelDbm;
		int levelEcio;
		if (cdmaDbm >= -75)
			levelDbm = 4;
		else if (cdmaDbm >= -85)
			levelDbm = 3;
		else if (cdmaDbm >= -95)
			levelDbm = 2;
		else if (cdmaDbm >= -100)
			levelDbm = 1;
		else
			levelDbm = 0;
		// Ec/Io are in dB*10
		if (cdmaEcio >= -90)
			levelEcio = 4;
		else if (cdmaEcio >= -110)
			levelEcio = 3;
		else if (cdmaEcio >= -130)
			levelEcio = 2;
		else if (cdmaEcio >= -150)
			levelEcio = 1;
		else
			levelEcio = 0;
		int level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
		return level;
	}

	public static int getEvdoLevel() {
		int evdoDbm = Config.evdoDbm;
		int evdoSnr = Config.evdoSnr;
		int levelEvdoDbm;
		int levelEvdoSnr;
		if (evdoDbm >= -65)
			levelEvdoDbm = 4;
		else if (evdoDbm >= -75)
			levelEvdoDbm = 3;
		else if (evdoDbm >= -90)
			levelEvdoDbm = 2;
		else if (evdoDbm >= -105)
			levelEvdoDbm = 1;
		else
			levelEvdoDbm = 0;

		if (evdoSnr >= 7)
			levelEvdoSnr = 4;
		else if (evdoSnr >= 5)
			levelEvdoSnr = 3;
		else if (evdoSnr >= 3)
			levelEvdoSnr = 2;
		else if (evdoSnr >= 1)
			levelEvdoSnr = 1;
		else
			levelEvdoSnr = 0;
		int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm : levelEvdoSnr;
		return level;
	}

	public static int getLteLevel() {
		int levelLteRsrp = 0;
		if (Config.lteRsrp == -1)
			levelLteRsrp = 0;
		else if (Config.lteRsrp >= -85)
			levelLteRsrp = 4;
		else if (Config.lteRsrp >= -95)
			levelLteRsrp = 3;
		else if (Config.lteRsrp >= -105)
			levelLteRsrp = 2;
		else if (Config.lteRsrp >= -115)
			levelLteRsrp = 1;
		else
			levelLteRsrp = 0;
		return levelLteRsrp;
	}
	
	public static String int2IP(int i) {
		return (i & 0xFF) + "." 
				+ ((i >> 8) & 0xFF) + "."
				+ ((i >> 16) & 0xFF)+ "." 
				+ (i >> 24 & 0xFF);
	}
	
	public static String byte2MAC(byte[] b) {
		StringBuffer hs = new StringBuffer(b.length);
		String tmp = "";
		int len = b.length;
		for (int n = 0; n < len; n++) {
			tmp = Integer.toHexString(b[n] & 0xFF);
			if (tmp.length() == 1)
				hs = hs.append("0").append(tmp);
			else {
				hs = hs.append(tmp);
			}
		}
		return String.valueOf(hs);
	}
}
