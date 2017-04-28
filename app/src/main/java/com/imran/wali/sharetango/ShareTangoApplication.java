package com.imran.wali.sharetango;

import android.app.Application;

import com.karumi.dexter.Dexter;

/**
 * Created by junze on 2016-12-21.
 */

public class ShareTangoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Dexter.initialize(this); // handle runtime permissions
    }
}
