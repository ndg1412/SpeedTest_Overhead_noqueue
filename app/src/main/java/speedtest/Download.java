package speedtest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.noqueue10.speedtest_overhead.Interface.IDownloadListener;
import com.noqueue10.speedtest_overhead.object.SpeedUpdateObj;
import com.noqueue10.speedtest_overhead.util.Config;
import com.noqueue10.speedtest_overhead.util.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.states.HttpStates;

/**
 * Created by ngodi on 2/22/2016.
 */
public class Download {
    private static final String TAG = "Download";
    Context context;
    String host;
    int port;
    String uri;
    String[] files;
    Timer tiDownload = null;
    List<Float> lMax = new ArrayList<Float>();
    List<Float> lMax_wifi = new ArrayList<Float>();
    List<Float> lMax_lte = new ArrayList<Float>();
    long wlan_rx = 0, lte_rx;
    long wlan_rx_first = 0, lte_rx_first;
    long timeStart = 0, timeStart_Calc = 0;
    long timeCheckTimer;
    boolean bCheck = false;
    String[] sLte;
    SpeedUpdateObj lastData = null, curData = null;

    int count_wlan = 0, count_lte = 0;

    private IDownloadListener downloadTestListenerList;
    public void addDownloadTestListener(IDownloadListener listener) {
        downloadTestListenerList = listener;
    }

    public Download(Context context, String host, int port, String uri, String[] files) {
        this.context = context;
        this.host = host;
        this.port = port;
        this.uri = uri;
        this.files = files;
        sLte = Network.getLTEIfName();
    }

    public String Create_Head(String file) {
        String url = this.uri + file;
        String downloadRequest = "GET " + url + " HTTP/1.1\r\n" + "Host: " + this.host + "\r\n\r\n";

        return downloadRequest;
    }

    public void Download_Run() {
        downloadTestListenerList.onDownloadProgress(0);
        timeStart_Calc = timeStart = System.currentTimeMillis();
        timeCheckTimer = System.currentTimeMillis();
        /*wlan_rx = wlan_rx_first = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
        lte_rx = lte_rx_first = TrafficStats.getMobileRxBytes();*/

        wlan_rx = wlan_rx_first = Network.getRxByte(Config.WLAN_IF);
        if(sLte == null)
            lte_rx = lte_rx_first = 0;
        else {
            long tmp = 0;
            for(String s : sLte)
                tmp += Network.getRxByte(s);
            lte_rx = lte_rx_first = tmp;
        }

        for(String file : files) {
            Do_Download down = new Do_Download(file);
            down.start();
        }
        tiDownload = new Timer();
        tiDownload.schedule(new TimerTask() {
            @Override
            public void run() {

                /*long wlan = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                long lte = TrafficStats.getMobileRxBytes();*/
                long tmp_wlan = Network.getRxByte(Config.WLAN_IF);
                long wlan = count_wlan*Config.ULONG_MAX + tmp_wlan;
                long lte = 0, tmp_lte = 0;;
                if(sLte == null) {
                    lte = 0;
                }
                else {

                    for(String s : sLte)
                        tmp_lte += Network.getRxByte(s);
                    lte = count_lte*Config.ULONG_MAX + tmp_lte;
                }


                if(wlan < wlan_rx) {
                    count_wlan++;
                    wlan = count_wlan*Config.ULONG_MAX + tmp_wlan;
                }
                if(lte < lte_rx) {
                    count_lte++;
                    lte = count_lte*Config.ULONG_MAX + tmp_lte;
                }

                long timeTimer = System.currentTimeMillis();
                long time = timeTimer - timeStart;
                long time_curr = timeTimer - timeCheckTimer;
                timeCheckTimer = timeTimer;
                Log.d(TAG, "giang debug time timer check: " + time_curr);
                timeCheckTimer = System.currentTimeMillis();

                float speed_wlan, speed_wlan_avg;
                float speed_lte, speed_lte_avg, speed;

                speed_wlan = (float)(wlan - wlan_rx) * 8 / (time_curr / 1000f)/1000000;
                speed_wlan_avg = (float)(wlan - wlan_rx_first) * 8 / ((timeTimer - timeStart_Calc) / 1000f)/1000000;
                speed_lte = (float)(lte - lte_rx) * 8 / (time_curr / 1000f)/1000000;
                speed_lte_avg = (float)(lte - lte_rx_first) * 8 / ((timeTimer - timeStart_Calc) / 1000f)/1000000;

                /*if(speed_wlan > Config.WIFI_LIMIT)
                    speed_wlan = Config.WIFI_LIMIT;
                if(speed_lte > Config.LTE_LIMIT)
                    speed_lte = Config.LTE_LIMIT;
                if(speed_wlan_avg > Config.WIFI_LIMIT)
                    speed_wlan_avg = Config.WIFI_LIMIT;
                if(speed_lte_avg > Config.LTE_LIMIT)
                    speed_lte_avg = Config.LTE_LIMIT;*/


                speed = speed_wlan + speed_lte;

                lMax_wifi.add(speed_wlan);
                lMax_lte.add(speed_lte);
                lMax.add(speed);

                float max_speed_wifi = getBandwidth(lMax_wifi);
                float max_speed_lte = getBandwidth(lMax_lte);
                curData = new SpeedUpdateObj(speed, max_speed_wifi, speed_wlan_avg, max_speed_lte, speed_lte_avg);

                if(max_speed_wifi < speed_wlan_avg) {
                    max_speed_wifi = speed_wlan_avg;
                    lMax_wifi.add(max_speed_wifi);
                }
                if(max_speed_lte < speed_lte_avg) {
                    max_speed_lte = speed_lte_avg;
                    lMax_lte.add(max_speed_lte);
                }

                float max_wifi = getBandwidth(lMax_wifi);
                float max_lte = getBandwidth(lMax_lte);
                float max_speed = getBandwidth(lMax);
                float avg_speed = speed_wlan_avg + speed_lte_avg;



                if(max_speed < avg_speed)
                    lastData = new SpeedUpdateObj(avg_speed, avg_speed, max_wifi, speed_wlan_avg, max_lte, speed_lte_avg);
                else
                    lastData = new SpeedUpdateObj(avg_speed, max_speed, max_wifi, speed_wlan_avg, max_lte, speed_lte_avg);

                downloadTestListenerList.onDownloadProgress((int) (100 * (timeTimer - timeStart) / Config.TIME_STOP));
                if((time >= Config.TIME_START) && (!bCheck)) {
                    bCheck = true;
                    wlan_rx_first = wlan;
                    lte_rx_first = lte;
                    timeStart_Calc = timeCheckTimer;
                }
                if(time >= Config.TIME_STOP) {

                    downloadTestListenerList.onDownloadPacketsReceived(lastData);
                    tiDownload.cancel();
                    tiDownload = null;
                    return;
                }


                downloadTestListenerList.onDownloadUpdate(curData);
                wlan_rx = wlan;
                lte_rx = lte;
            }
        }, 0, Config.TIMER_SLEEP);
        /*while(true) {
            try {
                Thread.sleep(200);
                if((System.currentTimeMillis() - timeStart) >= Config.TIME_STOP) {
                    tiDownload.cancel();
                    tiDownload = null;
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }*/
    }

    public class Do_Download extends Thread {
        String file;
        long lTime_start;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;

        public Do_Download(String file) {
            this.file = file;

        }
        @Override
        public void run() {

            String request = Create_Head(file);

            long totalPackets = 0;
            int frameLength = Config.DOWNLOAD_FILE_SIZE;
            try {
                socket = new Socket();
                socket.setTcpNoDelay(false);
                socket.setSoTimeout(Config.TIME_OUT);
                socket.setReuseAddress(true);
                socket.setKeepAlive(true);

                socket.connect(new InetSocketAddress(host, port));
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                outputStream.write(request.getBytes());
//                total_download += request.length();
                outputStream.flush();

                HttpFrame httpFrame = new HttpFrame();
                HttpStates errorCode = httpFrame.decodeFrame(inputStream);
                HttpStates headerError = httpFrame.parseHeader(inputStream);
                if(headerError == HttpStates.HTTP_FRAME_OK) {

                    int read = 0;
                    byte[] buffer = new byte[10000];


                    frameLength = httpFrame.getContentLength();
                    Log.d(TAG, "frameLength: " + frameLength);
                    if(frameLength < Config.DOWNLOAD_FILE_SIZE) {
                        try {
                            if(inputStream != null)
                                inputStream.close();
                            if(outputStream != null)
                                outputStream.close();
                            if(socket != null)
                                socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    lTime_start = System.currentTimeMillis();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true) {
                                try {
                                    Thread.sleep(100);
                                    long time = System.currentTimeMillis() - lTime_start;
                                    if((time) > Config.TIME_OUT) {
                                        inputStream.close();
                                        inputStream = null;
                                        interrupt();
                                        break;
                                    } else if((System.currentTimeMillis() - timeStart) >= Config.TIME_STOP ) {
                                        inputStream.close();
                                        inputStream = null;
                                        interrupt();
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    }).start();
                    while ((read = inputStream.read(buffer)) != -1) {
                        lTime_start = System.currentTimeMillis();
                        totalPackets += read;
                        if (totalPackets >= frameLength) {
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if(inputStream != null)
                    inputStream.close();
                if(outputStream != null)
                    outputStream.close();
                if(socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public float getBandwidth(List<Float>  list) {
        float max = list.get(0);
        for(int i = 1; i < list.size(); i++) {
            if(max < list.get(i))
                max = list.get(i);
        }
        return max;
    }

    public int getUrlSize(String url) {
        int size = 0;
        try
        {
            URL uri = new URL(url);
            URLConnection ucon;
            ucon = uri.openConnection();
            ucon.connect();
            String contentLengthStr = ucon.getHeaderField("content-length");
            Log.d(TAG, "getUrlSize: " + contentLengthStr);
            if(contentLengthStr != null)
                size = Integer.valueOf(contentLengthStr);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    static boolean isRun = false;
    public static void setInterrupt(boolean is) {
        isRun = is;
    }

    public boolean isMobileConnected() {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Log.d(TAG, "3G " + netInfo);
        return ((netInfo != null) && netInfo.isConnected());
    }
}
