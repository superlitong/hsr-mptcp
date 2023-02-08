package thu.kejiafan.mobinet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Date;
import android.os.Handler;

public class TCPTest {
	private Handler mHandler;
	public String mUplinkThroughput = "0";
	public String mDownlinkThroughput = "0";
//	public String mAvgUplinkThroughput = "0";
//	public String mAvgDownlinkThroughput = "0";

	public TCPTest(Handler _mHandler, String serverIP, String measuretime,
			String interval, FileOutputStream fos, int mode) {

		measureIP = serverIP;
		measureTime = measuretime;
		measureInterval = interval;
		if (mode == 2) {
			fosUplink = fos;
		} else if (mode == 1) {
			fosDownlink = fos;
		}
		testmode = mode;

		this.mHandler = _mHandler;

		(new myThread()).start();
	}

	class myThread extends Thread {

		@Override
		public void run() {
			if (testmode == 2) {
				connect2server();
			} else if (testmode == 1) {
				server2client();
			}
		}
	}

	/*
	 * Uplink
	 */
	private static long mStartTime;
	private static long mEndTime; // ��������ʱ��
	private static long mTime; // ����ʱ��
	private static long mInterval; // ��������

	private static long packetTime;
	private static long mLastTime; // ��һ������������ʱ��
	private static long mNextTime; // ��һ������������ʱ��
	private static long mTotalTime; // �ӿ�ʼ��д�����һ��buf��ʱ��

	private static long mTotalLen; // �ͻ�����TCP���ڣ����棩��д���������������mTotalTimeʱ����У�TCP���ڣ����棩�п��ܻ���Щ����û�з��ͳ�ȥ;����ʱ��Խ�������ԽС
	private static long mLastTotalLen;
	private static NumberFormat numF;

	/*
	 * Downlink
	 */
	private static long mStartTimed;
	private static long mEndTimed; // ��������ʱ��

	private static long packetTimed;
	private static long mLastTimed; // ��һ������������ʱ��
	private static long mNextTimed; // ��һ������������ʱ��
	private static long mTotalTimed; // �ӿ�ʼ��д�����һ��buf��ʱ��

	private static long mTotalLend; // �ͻ�����TCP���ڣ����棩��д���������������mTotalTimeʱ����У�TCP���ڣ����棩�п��ܻ���Щ����û�з��ͳ�ȥ;����ʱ��Խ�������ԽС
	private static long mLastTotalLend;

	private static Socket clientSocketDown;
	// ////////////////////////////////

	private static String measureIP;
	private static String measureTime;
	private static String measureInterval;
	private static Socket clientSocketUp;
	private static String disconnectTime;
	private static int testmode = 0;

	FileOutputStream fosUplink = null;
	FileOutputStream fosDownlink = null;

	static boolean boolthd = false;

	private void connect2server() {
		// send 0
		mHandler.sendEmptyMessage(0);
		// btnSend.setEnabled(false);
		// btnClose.setEnabled(false);

		// ���ʱ��汣��0λС��
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// ����ʱ���ɲ���argv[2]ָ������λΪmin
		// mTime = Integer.parseInt(measureTime) * 60 * 1000;
		mTime = Integer.parseInt(measureTime) * 1000;
		// ����argv[3]ָ�����������ڣ���λΪs
		mInterval = Integer.parseInt(measureInterval) * 1000;

// 		while (true) {
		try {
			// �������ӣ������в���argv[0]ָʾ��������IP��ַ��������ʹ��5001�Ŷ˿ڼ���
			if (clientSocketUp == null) {
				while (true) {
					try {
						clientSocketUp = new Socket(measureIP,
								Config.tcpUploadPort);
						if (clientSocketUp != null)
							break;
					} catch (Exception ce) {
						continue;
					}
				}
			}

			// send 1
			mHandler.sendEmptyMessage(1);

			mTotalLen = 0;
			mLastTotalLen = 0;

			String connectTimeString = Config.contentDateFormat
					.format(new Date());
			String local = " Local "
					+ clientSocketUp.getLocalAddress().getHostAddress()
					+ " port " + clientSocketUp.getLocalPort();
			String peer = clientSocketUp.getRemoteSocketAddress().toString();
			fosUplink.write((" ConnectTime: " + connectTimeString + local
					+ " connected to " + peer + "\n").getBytes());

			// ÿ�����׽�����д��buf�����ݣ�����Ϊ4K,��СΪ8KB������Ϊȫ'1'
			int bufLen = 1 * 1024;
			// ÿ��д����ֽ���
			int currLen = bufLen * 2;
			String buf = "";
			for (int i = 0; i < bufLen; i++)
				buf += '1';

			DataOutputStream outToServer = new DataOutputStream(
					clientSocketUp.getOutputStream());

			mStartTime = System.currentTimeMillis();
			mEndTime = mStartTime + mTime;
			mLastTime = mStartTime;
			mNextTime = mStartTime + mInterval;

			do {
				outToServer.writeChars(buf);
				packetTime = System.currentTimeMillis();
				disconnectTime = Config.contentDateFormat.format(new Date());

				if (packetTime >= mNextTime) {
					long inBytes = mTotalLen - mLastTotalLen;
					long inStart = mLastTime - mStartTime;
					long inStop = mNextTime - mStartTime;

					// 1KB = 1024B; 1kbps = 1000bps
					double throughput = (double) inBytes * 8
							/ (mInterval / 1000) / 1000;
					String rate = numF.format(throughput);
					fosUplink.write((inStart / 1000 + "-" + inStop / 1000
							+ " sec " + inBytes / 1024 + " KB " + rate
							+ " kbps" + "\n").getBytes());

					mUplinkThroughput = String.valueOf((int) throughput);// �ش�

					mLastTime = mNextTime;
					mNextTime += mInterval;
					mLastTotalLen = mTotalLen;

					while (packetTime > mNextTime) {
						// ReportPeriodicBW();
						inBytes = mTotalLen - mLastTotalLen;
						inStart = mLastTime - mStartTime;
						inStop = mNextTime - mStartTime;
						// 1KB = 1024B; 1kbps = 1000bps
						throughput = (double) inBytes * 8 / (mInterval / 1000)
								/ 1000;
						rate = numF.format(throughput);
						fosUplink.write((inStart / 1000 + "-" + inStop / 1000
								+ " sec " + inBytes / 1024 + " KB " + rate
								+ " kbps" + "\n").getBytes());

						mUplinkThroughput = String.valueOf((int) throughput);// �ش�

						mLastTime = mNextTime;
						mNextTime += mInterval;
						mLastTotalLen = mTotalLen;
					}
				}

				// û���׳�IOException�Ļ���˵��д��ɹ�
				mTotalLen += currLen;
			} while (packetTime <= mEndTime);// add by XQy

			// �������������ڼ�����ݴ�������������
			mTotalTime = packetTime - mStartTime;
			double throughput = (double) mTotalLen * 8 / (mTotalTime / 1000)
					/ 1000;
			String rate = numF.format(throughput);
			String content = "TotalTime	Transfer Throughput uplink:" + "0-"
					+ mTotalTime / 1000 + " sec " + mTotalLen / 1024 + " KB "
					+ rate + " kbps" + "\n";

			Config.mAvgUplinkThroughput = String.valueOf((int) throughput);// �ش�

			try {
				fosUplink.write(content.getBytes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// �ر��׽��ֺ�����
			clientSocketUp.close();

			// while (true) {
			// if (boolthd) {
			// break;
			// }
			// else {
			// Thread.sleep(1000);//add by XQY
			// continue;
			// }
			// }
			//
			// break;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			try {
				fosUplink.write((disconnectTime + " disconnected " + "\n").getBytes());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// send 2
			mHandler.sendEmptyMessage(2);

			// �������ӣ������в���argv[0]ָʾ��������IP��ַ��������ʹ��5001�Ŷ˿ڼ���
			while (true) {
				try {
					clientSocketUp = new Socket(measureIP, Config.tcpUploadPort);
					if (clientSocketUp != null) {
						break;
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
				}
			}
// 			continue;
		}
// }
		// send 4
		mHandler.sendEmptyMessage(4);
	}
	
	private void server2client() {
		// send 0
		mHandler.sendEmptyMessage(0);

		// ���ʱ��汣��0λС��
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// ����ʱ���ɲ���argv[2]ָ������λΪmin
//		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		mTime = Integer.parseInt(measureTime)  * 1000;
		// ����argv[3]ָ�����������ڣ���λΪs
		mInterval = Integer.parseInt(measureInterval) * 1000;
		
		// while (true) {
		try {
			// ��������
			if (clientSocketDown == null) {
				while (true) {
					try {
						clientSocketDown = new Socket(measureIP,
								Config.tcpDownloadPort);
						if (clientSocketDown != null)
							break;
					} catch (Exception ce) {
						continue;
					}
				}
			}

			// send 1
			mHandler.sendEmptyMessage(1);

			mTotalLend = 0;
			mLastTotalLend = 0;

			String connectTimeString = Config.contentDateFormat
					.format(new Date());
			String local = " Local "
					+ clientSocketDown.getLocalAddress().getHostAddress()
					+ " port " + clientSocketDown.getLocalPort();
			String peer = clientSocketDown.getRemoteSocketAddress().toString();
			try {
				fosDownlink.write((" ConnectTime: " + connectTimeString + local
						+ " connect to " + peer + "\n").getBytes());
			} catch (Exception e) {
				// TODO: handle exception
			}

			// ÿ�δ��׽��ֶ������ݵ�buf��buf�ĳ�����bufLenָ��Ϊ4K
			int bufLen = 1 * 1024;
			char buf[] = new char[bufLen];

			// ÿ�ζ�����ֽ���
			int currLen = 0;

			BufferedReader inFromServer = new BufferedReader(
					new InputStreamReader(clientSocketDown.getInputStream()));

			mStartTimed = System.currentTimeMillis();
			mEndTimed = mStartTimed + mTime; // add by XQY
			mLastTimed = mStartTimed;
			mNextTimed = mStartTimed + mInterval;

			do {
				currLen = inFromServer.read(buf);
				// currLen = -1 means reaching the end of the stream
				if (currLen == -1)
					break;
				packetTimed = System.currentTimeMillis();

				// �����Եر������
				if (packetTimed >= mNextTimed) {
					long inBytes = mTotalLend - mLastTotalLend;
					long inStart = mLastTimed - mStartTimed;
					long inStop = mNextTimed - mStartTimed;

					// 1KB = 1024B; 1kbps = 1000bps
					double throughput = (double) inBytes * 8
							/ (mInterval / 1000) / 1000;
					String rate = numF.format(throughput);
					try {
						fosDownlink.write((inStart / 1000 + "-" + inStop / 1000
										+ " sec " + inBytes / 1024 + " KB "
										+ rate + " kbps\n").getBytes());
					} catch (Exception e) {
						// TODO: handle exception
					}

					mDownlinkThroughput = String.valueOf((int) throughput);// �ش�

					mLastTimed = mNextTimed;
					mNextTimed += mInterval;
					mLastTotalLend = mTotalLend;

					while (packetTimed > mNextTimed) {
						inBytes = mTotalLend - mLastTotalLend;
						inStart = mLastTimed - mStartTimed;
						inStop = mNextTimed - mStartTimed;

						// 1KB = 1024B; 1kbps = 1000bps
						throughput = (double) inBytes * 8 / (mInterval / 1000)
								/ 1000;
						rate = numF.format(throughput);
						try {
							fosDownlink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps\n").getBytes());
						} catch (Exception e) {
							// TODO: handle exception
						}

						mDownlinkThroughput = String.valueOf((int) throughput);// �ش�

						mLastTimed = mNextTimed;
						mNextTimed += mInterval;
						mLastTotalLend = mTotalLend;
					}
				}

				// û���׳�IOException�Ļ���˵��д��ɹ�
				mTotalLend += currLen;
			} while (packetTimed <= mEndTimed);// add by XQY

			// �������������ڼ�����ݴ�������������
			mTotalTimed = packetTimed - mStartTimed;
			double throughput = (double) mTotalLend * 8 / (mTotalTimed / 1000)
					/ 1000;
			String rate = numF.format(throughput);
			String content = "TotalTime Transfer Throughput downlink:" + "0-"
					+ mTotalTimed / 1000 + " sec " + mTotalLend / 1024 + " KB "
					+ rate + " kbps" + "\n";

			Config.mAvgDownlinkThroughput = String.valueOf((int) throughput);// �ش�

			try {
				fosDownlink.write(content.getBytes());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			clientSocketDown.close();
			// send 3
			mHandler.sendEmptyMessage(3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			// send 2
			mHandler.sendEmptyMessage(2);

			// �������ӣ������в���argv[0]ָʾ��������IP��ַ��������ʹ��5001�Ŷ˿ڼ���
			while (true) {
				try {
					clientSocketDown = new Socket(measureIP,
							Config.tcpDownloadPort);
					if (clientSocketDown != null) {
						break;
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
				}
			}
			// continue;
		}
	}
	// }
}
