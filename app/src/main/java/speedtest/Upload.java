package speedtest;

import android.net.TrafficStats;
import android.util.Log;

import com.noqueue10.speedtest_overhead.Interface.IUploadListener;
import com.noqueue10.speedtest_overhead.object.SpeedUpdateObj;
import com.noqueue10.speedtest_overhead.util.Config;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import fr.bmartel.protocol.http.HttpFrame;
import fr.bmartel.protocol.http.states.HttpStates;


/**
 * Created by ngodi on 2/20/2016.
 */
public class Upload {
    private static final String TAG = "Upload";
    String host;
    int port;
    String uri;
    int[] sizes;
    long wlan_tx = 0, lte_tx;
    long wlan_tx_first = 0, lte_tx_first;
    Timer tiUpload = null;
    List<Float> lMax = new ArrayList<Float>();
    List<Float> lMax_wifi = new ArrayList<Float>();
    List<Float> lMax_lte = new ArrayList<Float>();
    long timeStart = 0, timeStart_Calc = 0;
    boolean bCheck = false;

    private IUploadListener uploadTestListenerList;
    public void addUploadTestListener(IUploadListener listener) {
        uploadTestListenerList = listener;
    }

    public Upload(String host, int port, String uri, int[] sizes) {
        this.host = host;
        this.port = port;
        this.uri = uri;
        this.sizes = sizes;
        Log.d(TAG, "size: leng: " + sizes.length);
        if(tiUpload != null) {
            tiUpload.cancel();
        }
    }

    public String Create_Head(int size) {
        String uploadRequest = "POST " + uri + " HTTP/1.1\r\n" + "Host: " + host + "\r\nAccept: */*\r\nContent-Length: " + size + "\r\n\r\n";

        /*StringBuilder sb = new StringBuilder();
        sb.append("POST %s HTTP/1.1 \r\n");
        sb.append("Host :%s \r\n");
        sb.append("Accept: text*//*\r\n");
        sb.append("Content-Length: %s \r\n");
        sb.append("Content-Type: application/x-www-form-urlencoded\r\n");
        sb.append("Expect: 100-continue\r\n");
        sb.append("\r\n");

        String uploadRequest = String.format(sb.toString(), uri, host, size);*/

        return uploadRequest;
    }

    public void Upload_Run() {
        uploadTestListenerList.onUploadProgress(0);
        wlan_tx = wlan_tx_first = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
        lte_tx = lte_tx_first = TrafficStats.getMobileTxBytes();
        timeStart_Calc = timeStart = System.currentTimeMillis();
        for(int size : sizes) {
            Do_Upload up = new Do_Upload(size);
            up.start();
        }

        tiUpload = new Timer();
        tiUpload.schedule(new TimerTask() {
            @Override
            public void run() {
                long timeTimer = System.currentTimeMillis();
                long time = System.currentTimeMillis() - timeStart;
                long wlan = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
                long lte = TrafficStats.getMobileTxBytes();
                float speed_wlan = ((wlan - wlan_tx) * 8 / 1000000 * (1000f / Config.TIMER_SLEEP));
                float speed_lte = ((lte - lte_tx) * 8 / 1000000 * (1000f / Config.TIMER_SLEEP));
                float speed_wlan_avg = (wlan - wlan_tx_first) * 8 / ((timeTimer - timeStart_Calc) / 1000f)/1000000;
                float speed_lte_avg = (lte - lte_tx_first) * 8 / ((timeTimer - timeStart_Calc) / 1000f)/1000000;
                if(speed_wlan > Config.WIFI_LIMIT)
                    speed_wlan = Config.WIFI_LIMIT;
                if(speed_lte > Config.LTE_LIMIT)
                    speed_lte = Config.LTE_LIMIT;
                if(speed_wlan_avg > Config.WIFI_LIMIT)
                    speed_wlan_avg = Config.WIFI_LIMIT;
                if(speed_lte_avg > Config.LTE_LIMIT)
                    speed_lte_avg = Config.LTE_LIMIT;
                float speed = speed_wlan + speed_lte;
                lMax_wifi.add(speed_wlan);
                lMax_lte.add(speed_lte);
                lMax.add(speed);

                float max_speed_wifi = getBandwidth(lMax_wifi);
                float max_speed_lte = getBandwidth(lMax_lte);
                if(max_speed_wifi < speed_wlan_avg) {
                    max_speed_wifi = speed_wlan_avg;
                    lMax_wifi.add(max_speed_wifi);
                    if(time >= Config.TIME_START)
                        lMax_wifi.add(max_speed_wifi);
                }
                if(max_speed_lte < speed_lte_avg) {
                    max_speed_lte = speed_lte_avg;
                    lMax_lte.add(max_speed_lte);
                    if(time >= Config.TIME_START)
                        lMax_lte.add(max_speed_lte);
                }
                uploadTestListenerList.onUploadProgress((int) (100 * (timeTimer - timeStart) / Config.TIME_STOP));
                if((time >= Config.TIME_START) && (!bCheck)) {
                    bCheck = true;
                    wlan_tx_first = wlan;
                    lte_tx_first = lte;
                    timeStart_Calc = timeTimer;
                }

                if(time >= Config.TIME_STOP) {
                    Log.d(TAG, "==========================================complete ======================>");

                    float max_wifi = getBandwidth(lMax_wifi);
                    float max_lte = getBandwidth(lMax_lte);
                    float max_speed = getBandwidth(lMax);
                    float avg_wifi = ((wlan - wlan_tx_first) * 8) / ((timeTimer - timeStart_Calc) / 1000f) /1000000;
                    float avg_lte = ((lte - lte_tx_first) * 8) / ((timeTimer - timeStart_Calc) / 1000f) /1000000;
                    if(avg_wifi > Config.WIFI_LIMIT)
                        avg_wifi = Config.WIFI_LIMIT;
                    if(avg_lte > Config.LTE_LIMIT)
                        avg_lte = Config.LTE_LIMIT;
                    float avg_speed = avg_wifi + avg_lte;
                    SpeedUpdateObj data;
                    if(max_speed < avg_speed)
                        data = new SpeedUpdateObj(avg_speed, avg_speed, max_wifi, avg_wifi, max_lte, avg_lte);
                    else
                        data = new SpeedUpdateObj(avg_speed, max_speed, max_wifi, avg_wifi, max_lte, avg_lte);
                    uploadTestListenerList.onUploadPacketsReceived(data);
                    tiUpload.cancel();
                    tiUpload = null;
                    return;
                }

                SpeedUpdateObj data = new SpeedUpdateObj(speed, max_speed_wifi, speed_wlan_avg, max_speed_lte,
                        speed_lte_avg);
                uploadTestListenerList.onUploadUpdate(data);
                wlan_tx = wlan;
                lte_tx = lte;
            }
        }, 0, Config.TIMER_SLEEP);

    }

    public class Do_Upload extends Thread {
        int size;
        int upload_size = 0;
        long lTime_start;
        Socket socket = null;
        OutputStream outputStream = null;

        public Do_Upload(int size) {
            this.size = size;

        }
        @Override
        public void run() {
            String request = Create_Head(size);
            long total_packet = 0;
            /*RandomGen random = new RandomGen(size);
            byte[] buf = random.getBuf();*/
            try {
                socket = new Socket();
                socket.setTcpNoDelay(false);
                //socket.setSoTimeout(Config.TIME_OUT);
                socket.setReuseAddress(true);
                socket.setKeepAlive(true);
                socket.connect(new InetSocketAddress(host, port));
                outputStream = socket.getOutputStream();
                outputStream.write(request.getBytes());
                outputStream.flush();
                /*outputStream.write(buf);
                outputStream.flush();*/

                lTime_start = System.currentTimeMillis();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                Thread.sleep(100);
                                long time_current = System.currentTimeMillis();
                                if((time_current - lTime_start) > Config.TIME_OUT) {
                                    outputStream.close();
                                    outputStream = null;
                                    interrupt();
                                    break;
                                } else if((System.currentTimeMillis() - timeStart) >= Config.TIME_STOP ) {
                                    outputStream.close();
                                    outputStream = null;
                                    interrupt();
                                    break;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }).start();
                for(int i = 0; i < size; i = i + 50000) {
                    byte[] buf = new byte[50000];
                    outputStream.write(buf);
                    outputStream.flush();
                    lTime_start = System.currentTimeMillis();
                    total_packet += 50000;

                }
                HttpFrame frame = new HttpFrame();

                HttpStates httpStates = frame.parseHttp(socket.getInputStream());
                if (httpStates == HttpStates.HTTP_FRAME_OK) {
                    if (frame.getStatusCode() == 200 && frame.getReasonPhrase().toLowerCase().equals("ok")) {
                        //upload_size = size;

//                        Log.d(TAG, "upload complete==============================>");

                    }

                } else if (httpStates == HttpStates.HTTP_READING_ERROR) {
                    //upload_size = size;
                }
                Log.d(TAG, "total_packet: " + total_packet);

            } catch (Exception e) {
                e.printStackTrace();
            }
            upload_size = size;
            try {
                if(outputStream != null)
                    outputStream.close();
                if(socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public int getUploadSize() {
            return upload_size;
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
}
