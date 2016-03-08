package com.work.speedtest_overhead.Interface;

import com.work.speedtest_overhead.object.SpeedUpdateObj;

/**
 * Created by ngodi on 2/20/2016.
 */
public interface IUploadListener {
    public void onUploadPacketsReceived(SpeedUpdateObj data);

    public void onUploadError(int errorCode, String message);

    public void onUploadProgress(int percent);
    public void onUploadUpdate(SpeedUpdateObj data);
}
