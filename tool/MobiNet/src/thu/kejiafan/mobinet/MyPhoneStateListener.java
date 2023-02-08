package thu.kejiafan.mobinet;

import java.io.IOException;
import java.util.Date;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class MyPhoneStateListener extends PhoneStateListener {
	
	@Override
	public void onCellLocationChanged(CellLocation location) {
		// TODO Auto-generated method stub
		String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));
		
		int cid = -1;
		int lac = -1;
		int psc = -1;
		double cellLatitude = -1;
		double cellLongitude = -1;	
		if (location instanceof GsmCellLocation) {
			cid = ((GsmCellLocation) location).getCid() & 0xffff;
			lac = ((GsmCellLocation) location).getLac();
			psc = ((GsmCellLocation) location).getPsc();
		} else if (location instanceof CdmaCellLocation) {
			cid = ((CdmaCellLocation) location).getBaseStationId();
			lac = ((CdmaCellLocation) location).getNetworkId();
			psc = ((CdmaCellLocation) location).getSystemId();
			cellLatitude = ((CdmaCellLocation) location).getBaseStationLatitude();
			cellLongitude = ((CdmaCellLocation) location).getBaseStationLongitude();
			Config.tvCurrentLocation.setText(cellLatitude + " " + cellLongitude);
		}
		Config.tvCurrentCell.setText("Cid:" + cid + " Lac:" + lac);
		if (cid == Config.lastcellid) {

		} else {
			Config.lastcellid = cid;
			Config.handoffNumber++;
			Config.tvHandoffNumber.setText("切换次数:"
					+ String.valueOf(Config.handoffNumber) + " 断网次数:"
					+ String.valueOf(Config.disconnectNumber));
		}
		
		String cellInfoContent = Config.networkTypeString + " "
				+ cid + " " + lac + " " + psc + " " + cellLatitude + " "
				+ cellLongitude;
		try {
			if (cellInfoContent.equals(Config.lastCellInfoContent)
					|| Config.networkTypeString.equals("")) {
				
			} else {
				Config.lastCellInfoContent = cellInfoContent;
				cellInfoContent = date + " " + cellInfoContent;
				if (Config.fosCell != null) {
					Config.fosCell.write(cellInfoContent.getBytes());
					Config.fosCell.write(System.getProperty("line.separator").getBytes());
				}
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.onCellLocationChanged(location);
	}

	@Override
	public void onDataActivity(int direction) {
		// TODO Auto-generated method stub
		super.onDataActivity(direction);
	}

	@Override
	public void onDataConnectionStateChanged(int state, int networkType) {
		// TODO Auto-generated method stub
		String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));
		
		switch (state) {
		case TelephonyManager.DATA_DISCONNECTED:		
			Config.dataConnectionState = "Disconnected";
			if (Config.lastConnect) {
				Config.disconnectNumber++;
				Config.lastConnect = false;
				Config.tvHandoffNumber.setText("切换次数:"
						+ String.valueOf(Config.handoffNumber) + " 断网次数:"
						+ String.valueOf(Config.disconnectNumber));
			}
			break;
		case TelephonyManager.DATA_CONNECTING:
			Config.dataConnectionState = "Connecting";
			break;
		case TelephonyManager.DATA_CONNECTED:
			Config.dataConnectionState = "Connected";
			Config.lastConnect = true;
			break;
		default:
			Config.dataConnectionState = "Unknown";
			break;
		}
		Config.tvDataConnection.setText(Config.dataConnectionState);
		
		SignalUtil.getCurrentnetworkTypeString(networkType);
		Config.tvNetworkType.setText(Config.networkTypeString);
		
		
		String dataContentString = Config.dataConnectionState + " " + Config.networkTypeString;
		try {
			if (dataContentString.equals(Config.lastDataContentString)) {
				
			} else {
				Config.lastDataContentString = dataContentString;
				dataContentString = date + " " + dataContentString;
				if (Config.fosMobile != null) {
					Config.fosMobile.write(dataContentString.getBytes());
					Config.fosMobile.write(System.getProperty("line.separator").getBytes());
				}			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.onDataConnectionStateChanged(state, networkType);
	}

	@Override
	public void onServiceStateChanged(ServiceState serviceState) {
		// TODO Auto-generated method stub
		super.onServiceStateChanged(serviceState);
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		// TODO Auto-generated method stub
		super.onSignalStrengthsChanged(signalStrength);
		
		String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));
		/**
		 * 获取信号强度参数
		 */
		Config.gsmSignalStrength = signalStrength.getGsmSignalStrength();
		Config.gsmBitErrorRate = signalStrength.getGsmBitErrorRate();
		Config.cdmaDbm = signalStrength.getCdmaDbm();
		Config.cdmaEcio = signalStrength.getCdmaEcio();
		Config.evdoDbm = signalStrength.getEvdoDbm();
		Config.evdoEcio = signalStrength.getEvdoEcio();
		Config.evdoSnr = signalStrength.getEvdoSnr();
		/**
		 * http://www.oschina.net/code/explore/android-4.0.1/telephony/java/android/telephony/SignalStrength.java
		 * 0: GsmSignalStrength(0-31) GsmBitErrorRate(0-7)
		 * 2: CdmaDbm CdmaEcio EvdoDbm EvdoEcio EvdoSnr(0-8)
		 * 7: LteSignalStrength LteRsrp LteRsrq LteRssnr LteCqi 非4G则全为-1
		 * getGsmLevel getLteLevel getCdmaLevel getEvdoLevel
		 */
		String allSignal = signalStrength.toString();
		try {
			String[] parts = allSignal.split(" ");			
			Config.lteSignalStrength = Integer.parseInt(parts[8]); //asuLTE
			Config.lteRsrp = Integer.parseInt(parts[9]);
			Config.lteRsrq = Integer.parseInt(parts[10]);
			Config.lteRssnr = Integer.parseInt(parts[11]);
			Config.lteCqi = Integer.parseInt(parts[12]);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		int level = SignalUtil.getCurrentLevel(signalStrength.isGsm());
		switch (Config.netTypeID) {
		case 4:
			Config.signalStrengthString = "1x:" + Config.cdmaDbm + "  3G:"
					+ Config.evdoDbm + "  Level:" + level;
			Config.SignalParameterString = "Ecio:" + Config.cdmaEcio + "/"
					+ Config.evdoEcio + " SNR:" + Config.evdoSnr;
			break;
		case 5:
			Config.signalStrengthString = "2G:" + Config.gsmSignalStrength
					+ "  4G:" + Config.lteSignalStrength + "  Level:" + level;
			Config.SignalParameterString = "RSRP:" + Config.lteRsrp + " RSRQ:"
					+ Config.lteRsrq + " SNR:" + Config.lteRssnr;
			break;
		default:
			Config.signalStrengthString = Config.gsmSignalStrength + "  Level:" + level;
			Config.SignalParameterString = "BER:" + Config.gsmBitErrorRate;			
			break;
		}
		Config.tvSignalStrength.setText(Config.signalStrengthString);

		String signalContent = Config.networkTypeString + " "
				+ Config.signalStrengthString + " " + Config.SignalParameterString;
		try {
			if (signalContent.equals(Config.lastSignalContent)) {
				
			} else {
				Config.lastSignalContent = signalContent;
				signalContent = date + " " + signalContent;
				if (Config.fosSignal != null) {
					Config.fosSignal.write(signalContent.getBytes());
					Config.fosSignal.write(System.getProperty("line.separator").getBytes());
				}
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
