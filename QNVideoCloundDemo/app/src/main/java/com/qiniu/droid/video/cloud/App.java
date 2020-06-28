package com.qiniu.droid.video.cloud;

import android.app.Application;

import com.bugsnag.android.Bugsnag;
import com.qiniu.droid.rtc.QNLogLevel;
import com.qiniu.droid.rtc.QNRTCEnv;
import com.qiniu.pili.droid.streaming.StreamingEnv;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * streming init must be called before any other func
         */
        StreamingEnv.init(getApplicationContext());
        Bugsnag.init(this);

        /**
         * rtc init must be called before any other func
         */
        QNRTCEnv.setLogLevel(QNLogLevel.INFO);
        QNRTCEnv.init(getApplicationContext());
        QNRTCEnv.setLogFileEnabled(true);
    }
}
