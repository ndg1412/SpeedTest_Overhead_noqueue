package com.noqueue10.speedtest_overhead.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ngodi on 3/2/2016.
 */
public class RuntimeCmd {
    private static final String TAG = "RuntimeCmd";
    static Process process = null;

    public static void Download_Start() {
        Runtime runtime = Runtime.getRuntime();
        try {

            String command = String.format("sh %s", Config.DOWNLOAD_PATH_SCRIPT_START);
            process = runtime.exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Download_Stop() {
        try {
            if(process != null) {
                String command = String.format("sh %s", Config.DOWNLOAD_PATH_SCRIPT_STOP);
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(command);
                process.destroy();
                process = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getLimit() {
        float wifi = 0, lte = 0;
        Runtime runtime = Runtime.getRuntime();
        try {
            String cmd = String.format("/system/bin/cat %s", Config.LIMIT_PATH_FILE);
            Process process = runtime.exec(cmd);
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;

            while ((line = reader.readLine ()) != null) {
                String[] tmp = line.split(",");
                for(String s : tmp) {
                    if(s.contains("LTE-MAX"))
                        lte = Float.valueOf(s.split(":")[1].trim());
                    else if(s.contains("WIFI-MAX")) {
                        wifi = Float.valueOf(s.split(":")[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "wifi: " + wifi + ", lte: " + lte);
        Config.WIFI_LIMIT = wifi;
        Config.LTE_LIMIT = lte;
    }

}
