package speedtest;

import android.util.Log;

import com.work.speedtest_overhead.Interface.IUploadListener;
import com.work.speedtest_overhead.object.SpeedUpdateObj;
import com.work.speedtest_overhead.util.Config;
import com.work.speedtest_overhead.util.Network;

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
    long timeCheckTimer;
    boolean bCheck = false;
    String[] sLte;
    int count_wlan = 0, count_lte = 0;

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
        sLte = Network.getLTEIfName();
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
        timeStart_Calc = timeStart = System.currentTimeMillis();
        timeCheckTimer = System.currentTimeMillis();
        wlan_tx = wlan_tx_first = Network.getRxByte(Config.WLAN_IF);
        if(sLte == null)
            lte_tx = lte_tx_first = 0;
        else {
            long tmp = 0;
            for(String s : sLte)
                tmp += Network.getRxByte(s);
            lte_tx = lte_tx_first = tmp;
        }

        for(int size : sizes) {
            Do_Upload up = new Do_Upload(size);
            up.start();
        }

        tiUpload = new Timer();
        tiUpload.schedule(new TimerTask() {
            @Override
            public void run() {
                long tmp_wlan = Network.getTxByte(Config.WLAN_IF);
                long wlan = count_wlan*Config.ULONG_MAX + tmp_wlan;
                long lte = 0, tmp_lte = 0;;
                if(sLte == null) {
                    lte = 0;
                }
                else {

                    for(String s : sLte)
                        tmp_lte += Network.getTxByte(s);
                    lte = count_lte*Config.ULONG_MAX + tmp_lte;
                }

                if(wlan < wlan_tx) {
                    count_wlan++;
                    wlan = count_wlan*Config.ULONG_MAX + tmp_wlan;
                }
                if(lte < lte_tx) {
                    count_lte++;
                    lte = count_lte*Config.ULONG_MAX + tmp_lte;
                }

                long timeTimer = System.currentTimeMillis();
                long time = timeTimer - timeStart;
                long time_curr = timeTimer - timeCheckTimer;
                timeCheckTimer = timeTimer;
                Log.d(TAG, "giang debug upload time timer check: " + time_curr);
                timeCheckTimer = System.currentTimeMillis();

                float speed_wlan, speed_wlan_avg;
                float speed_lte, speed_lte_avg, speed;

                speed_wlan = (float)(wlan - wlan_tx) * 8 / (time_curr / 1000f)/1000000;
                speed_wlan_avg = (float)(wlan - wlan_tx_first) * 8 / ((timeTimer - timeStart_Calc) / 1000f)/1000000;
                speed_lte = (float)(lte - lte_tx) * 8 / (time_curr / 1000f)/1000000;
                speed_lte_avg = (float)(lte - lte_tx_first) * 8 / ((timeTimer - timeStart_Calc) / 1000f)/1000000;

                speed = speed_wlan + speed_lte;

                lMax_wifi.add(speed_wlan);
                lMax_lte.add(speed_lte);
                lMax.add(speed);

                float max_speed_wifi = getBandwidth(lMax_wifi);
                float max_speed_lte = getBandwidth(lMax_lte);
                SpeedUpdateObj curData = new SpeedUpdateObj(speed, max_speed_wifi, speed_wlan_avg, max_speed_lte, speed_lte_avg);

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

                SpeedUpdateObj lastData;
                if(max_speed < avg_speed)
                    lastData = new SpeedUpdateObj(avg_speed, avg_speed, max_wifi, speed_wlan_avg, max_lte, speed_lte_avg);
                else
                    lastData = new SpeedUpdateObj(avg_speed, max_speed, max_wifi, speed_wlan_avg, max_lte, speed_lte_avg);

                uploadTestListenerList.onUploadProgress((int) (100 * (timeTimer - timeStart) / Config.TIME_STOP));
                if((time >= Config.TIME_START) && (!bCheck)) {
                    bCheck = true;
                    wlan_tx_first = wlan;
                    lte_tx_first = lte;
                    timeStart_Calc = timeCheckTimer;
                }
                if(time >= Config.TIME_STOP) {
                    uploadTestListenerList.onUploadPacketsReceived(lastData);
                    tiUpload.cancel();
                    tiUpload = null;
                    return;
                }

                uploadTestListenerList.onUploadUpdate(curData);
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
