package com.noqueue10.speedtest_overhead.Interface;

import com.noqueue10.speedtest_overhead.object.SpeedUpdateObj;

/**
 * Created by ngodi on 2/24/2016.
 */
public interface IDownloadListener {
    public void onDownloadPacketsReceived(SpeedUpdateObj data);

    public void onDownloadError(int errorCode, String message);

    public void onDownloadProgress(int percent);
    public void onDownloadUpdate(SpeedUpdateObj data);
}
