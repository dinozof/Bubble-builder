package com.co.dinofox.bubblebuilder.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.co.dinofox.bubblebuilder.R;
import com.co.dinofox.bubblebuilder.singletons.MySingleton;
import com.google.android.gms.common.SignInButton;


/**
 * Created by stefano on 20/11/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MyPreferenceFragment extends PreferenceFragment{

    private SwitchPreference saveOnDrivePref;
    private ListPreference listPreference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencescreen);


        saveOnDrivePref = (SwitchPreference) findPreference(getString(R.string.saveOnDrivePreference));
        listPreference = (ListPreference)      findPreference("priority_saving");


    }

    public void enableSaveOnDrive(){
            saveOnDrivePref.setEnabled(true);
            listPreference.setEnabled(true);

    }

    public void disableSaveOnDrive(){

            saveOnDrivePref.setEnabled(false);
            listPreference.setEnabled(false);
    }

}

