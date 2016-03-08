package com.work.speedtest_overhead.object;

/**
 * Created by ngodi on 3/5/2016.
 */
public class Limit {
    float wifi;
    float lte;

    public Limit(float wifi, float lte) {
        this.wifi = wifi;
        this.lte = lte;
    }

    public float getWifi() {
        return wifi;
    }

    public float getLte() {
        return lte;
    }
}
