package com.work.speedtest_overhead.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Enumeration;

import com.work.speedtest_overhead.object.PingData;

/**
 * Created by Giang on 6/1/2015.
 */
public class Network {
	private static final String TAG = "Network";

	public static boolean CheckHost(String host) {
		Runtime runtime = Runtime.getRuntime();
		try {

			Process ipProcess = runtime.exec("/system/bin/ping -c 1 " + host);
			int     exitValue = ipProcess.waitFor();
			return (exitValue == 0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	public static boolean isAlive(String ifname) {
		Runtime runtime = Runtime.getRuntime();
		try {

			String cmd = String.format("su -c /system/bin/ping -c 1 -I %s 8.8.8.8", ifname);
			Log.d(TAG, "giang dbg cmd isAlive: " + cmd);
			Process ipProcess = runtime.exec(cmd);

			int     exitValue = ipProcess.waitFor();

			Log.d(TAG, "exitValue: " + exitValue);
			ipProcess.destroy();
			return (exitValue == 0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	public static boolean isIP(String str) {
		return str.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	}

	public static boolean isNumeric(String str) {
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

	public static float ping(String host) {
		Runtime runtime = Runtime.getRuntime();
		float time = Integer.MAX_VALUE;
		try {
			Process ipProcess = runtime.exec("/system/bin/ping -c 1 " + host);
			InputStream stdout = ipProcess.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			String line;
			float[] afTime = new float[10];
			String sLoss = "";
			while ((line = reader.readLine ()) != null) {
				if(line.contains("icmp_seq")) {
					PingData data = new PingData(line);
					time = data.getTime();
					Log.d(TAG, "giang dbg ping host: " + host + ", time: " + time);
				}
			}
			ipProcess.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;
	}

	public static long pingUrl(String address) {
		try {
			URL url = new URL("http://" + address);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setConnectTimeout(1000 * 2); // mTimeout is in seconds
			long startTime = System.currentTimeMillis();
			urlConn.connect();
			long endTime = System.currentTimeMillis();
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Log.d(TAG, "Ping to " + address + ", Time (ms) : " + (endTime - startTime));
				return (endTime - startTime);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Integer.MAX_VALUE;
	}

	public static long getRxByte(String ifname) {
		if(ifname == null)
			return 0;
		long val = 0;
		Runtime runtime = Runtime.getRuntime();
		try {
			String cmd = String.format("/system/bin/cat /sys/class/net/%s/statistics/rx_bytes", ifname);
			Process ipProcess = runtime.exec(cmd);
			InputStream stdout = ipProcess.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			String line;
			while ((line = reader.readLine ()) != null) {
				if(line.length() > 0) {
					val = Long.valueOf(line.trim());
					break;
				}
			}
//			ipProcess.waitFor();
			ipProcess.destroy();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public static long getTxByte(String ifname) {
		if(ifname == null)
			return 0;
		long val = 0;
		Runtime runtime = Runtime.getRuntime();
		try {
			String cmd = String.format("/system/bin/cat /sys/class/net/%s/statistics/tx_bytes", ifname);
			Process ipProcess = runtime.exec(cmd);
			InputStream stdout = ipProcess.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			String line;
			while ((line = reader.readLine ()) != null) {
				if(line.length() > 0) {
					val = Long.valueOf(line.trim());
					break;
				}
			}
			//ipProcess.waitFor();
			ipProcess.destroy();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public static String[] getLTEIfName() {
		String[] tmp = new String[10];
		int count = 0;
		try {
			for (Enumeration<NetworkInterface> en =
                 NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                // Iterate over all IP addresses in each network interface.
                for (Enumeration<InetAddress> enumIPAddr =
                     intf.getInetAddresses(); enumIPAddr.hasMoreElements();)
                {
                    InetAddress iNetAddress = enumIPAddr.nextElement();

                    // Loop back address (127.0.0.1) doesn't count as an in-use IP address.
                    if (!iNetAddress.isLoopbackAddress())
                    {
                        String sLocalIP = iNetAddress.getHostAddress().toString();
						String sInterfaceName = intf.getName();
						if((sInterfaceName != null) && (sLocalIP != null) && isIP(sLocalIP) && (!sInterfaceName.equals(Config.WLAN_IF))) {

							tmp[count] = sInterfaceName;
							count++;
						}
                    }
                }
            }
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if(count == 0)
			return null;
		else {
			String[] ifName = new String[count];
			System.arraycopy(tmp, 0, ifName, 0, count);
			return ifName;
		}

	}

	public static void Download_Start() {
		Runtime runtime = Runtime.getRuntime();
		try {
			String cmd = "sh run.sh";
			runtime.exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void Download_Stop() {
		Runtime runtime = Runtime.getRuntime();
		try {
			String cmd = "sh stop.sh";
			runtime.exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
