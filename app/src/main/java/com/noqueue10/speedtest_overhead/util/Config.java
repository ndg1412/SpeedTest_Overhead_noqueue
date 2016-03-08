package com.noqueue10.speedtest_overhead.util;

/**
 * Created by ngodi on 2/19/2016.
 */
public class Config {
    public final static String PREF_KEY_SERVER_HOST		= "pref_key_server_host";
    public final static String PREF_KEY_SERVER_PORT		= "pref_key_server_port";

    public static	String strServer_Ip = "";
    public static int iServer_Port = 0;
    public static String URL = "/speedtest/100m.bin";
    public static String URI = "/";
    public static String SERVER_STATIC_URL = "http://www.speedtest.net/speedtest-servers.php";
    public static int[] UPLOAD_SIZE = {
            /*250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000,
            250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000,
            250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000,
            250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000, 250000,
            500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000,
            500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000,
            500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000,
            500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000, 500000,*/
            /*10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,
            10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,
            10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,
            10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,
            10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,
            10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,
            10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,
            10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000, 10000000,*/
            //12 thread
            1000000000, 1000000000, 1000000000, 1000000000, 1000000000, 1000000000, 1000000000, 1000000000, 1000000000, 1000000000, 1000000000, 1000000000,

            //24 thread
            /*35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000,
            35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000, 35000000*/

        };
    public static String[] DOWNLOAD_FILE = {
            /*"random350x350.jpg", "random350x350.jpg", "random350x350.jpg", "random350x350.jpg",
            "random500x500.jpg", "random500x500.jpg", "random500x500.jpg", "random500x500.jpg",
            "random750x750.jpg", "random750x750.jpg", "random750x750.jpg", "random750x750.jpg",
            "random1000x1000.jpg", "random1000x1000.jpg", "random1000x1000.jpg", "random1000x1000.jpg",
            "random1500x1500.jpg", "random1500x1500.jpg", "random1500x1500.jpg", "random1500x1500.jpg",
            "random2000x2000.jpg", "random2000x2000.jpg", "random2000x2000.jpg", "random2000x2000.jpg",
            "random2500x2500.jpg", "random2500x2500.jpg", "random2500x2500.jpg", "random2500x2500.jpg",
            "random3000x3000.jpg", "random3000x3000.jpg", "random3000x3000.jpg", "random3000x3000.jpg",
            "random3500x3500.jpg", "random3500x3500.jpg", "random3500x3500.jpg", "random3500x3500.jpg",
            "random4000x4000.jpg", "random4000x4000.jpg", "random4000x4000.jpg", "random4000x4000.jpg"*/
            /*"10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",
            "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",
            "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",
            "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",
            "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",
            "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",
            "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",
            "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin", "10m.bin",*/

            //12 thread
            "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin", "1g.bin"

            //24 thread
            /*"35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin",
            "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin", "35m.bin"*/
        };
    public static String FILE_CONFIG = "speedtest_servers_static.xml";
    public static int MAX_BUFFER = 10240;
    public static String WLAN_IF = "wlan0";
    public static String LTE_IF = "";
    public static long TIMER_SLEEP = 1000;
    public static int RADIUS = 120; //in dpi unit

    public static long ULONG_MAX = 4294967295l;

    public static int NUMBER_QUEUE_THREAD_DOWNLOAD = 12;
    public static int NUMBER_QUEUE_THREAD_UPLOAD = 12;
    public static int TIME_OUT = 10*1000; //in ms
    public static int TIME_START = 1000; //in ms
    public static int TIME_STOP = 10000; //in ms
    public static int DOWNLOAD_FILE_SIZE = 1024*1024*1024;

    public static String DOWNLOAD_PATH_SCRIPT_START = "/data/vision/SpeedTestStart.sh";
    public static String DOWNLOAD_PATH_SCRIPT_STOP = "/data/vision/SpeedTestEnd.sh";
    public static String LIMIT_PATH_FILE = "/data/vision/speedtest.conf";
    public static float WIFI_LIMIT;
    public static float LTE_LIMIT;

}
