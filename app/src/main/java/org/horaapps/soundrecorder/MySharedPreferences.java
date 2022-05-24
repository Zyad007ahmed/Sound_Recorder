package org.horaapps.soundrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MySharedPreferences {
    private static final String PREF_HIGH_QUALITY_NAME = "pref_high_quality";

    public static void setPrefHighQuality (Context context , boolean isEnabled){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_HIGH_QUALITY_NAME,isEnabled);
        editor.apply();
    }

    public static boolean getPrefHighQuality(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isEnabled = sp.getBoolean(PREF_HIGH_QUALITY_NAME,false);

        return isEnabled;
    }
}
