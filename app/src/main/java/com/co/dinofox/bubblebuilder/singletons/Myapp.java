package com.co.dinofox.bubblebuilder.singletons;

import android.content.Context;

import com.co.dinofox.bubblebuilder.R;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(mailTo = "zofoxof@gmail.com",
        customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE, ReportField.LOGCAT },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
/**
 * Classe app per la gestione del singleton
 */
public class Myapp extends android.app.Application
{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Initialize the singletons so their instances
        // are bound to the application process.


        initSingletons();
    }

    protected void initSingletons()
    {
        // Initialize the instance of MySingleton
        MySingleton.initInstance();
    }


}