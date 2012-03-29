package com.cyanogenmod.settings.device;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.SystemProperties;

public class DeviceSettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static final String PREFERENCE_KEYBOARD_LAYOUT = "keyboard_layout";
    private static final int COPY_FILE_BUFFER_SIZE = 1024;

    private static final String DATA_PREFIX = "/data/data/";
    private static final String KEYBOARD_KCM_FILE = "asusec.kcm";
    private static final String KEYBOARD_KL_FILE = "asusec.kl";

    private static final String KEYSWAP_PROPERTY = "sys.dockkeys.change";

    private ListPreference mKeyboardLayout;
    private ListPreference mCpuMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mKeyboardLayout = (ListPreference) getPreferenceScreen().findPreference(
                        PREFERENCE_KEYBOARD_LAYOUT);
        mKeyboardLayout.setOnPreferenceChangeListener(this);

    }

    private String getDataDir() {
        return DATA_PREFIX + getPackageName();
    }

    private int getCpuModeOffser(String mode) {
        if (mode.equals("0"))
            return 0;
                else if (mode.equals("2"))
            return 2;
                else
            return 1;
    }

    private void copyAsset (String fileIn, String fileOut) {
        try {
            InputStream inputStream = getAssets().open(fileIn);
            OutputStream outputStream;

            File outFile = new File(getDataDir(),fileOut);
            outputStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[COPY_FILE_BUFFER_SIZE];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference.equals(mKeyboardLayout)) {
            final String newLanguage = (String) value;

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.caution_keyboard_layout)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    setNewKeyboardLanguage(newLanguage);
                                }
                            }).setNegativeButton(android.R.string.no, null)
                    .create();

            dialog.show();
        }

        return false;
    }

    private void setNewKeyboardLanguage(String language) {
        String keyboardKcm = language + ".kcm";
        String keyboardKl = language + ".kl";

        copyAsset(keyboardKcm, KEYBOARD_KCM_FILE);
        copyAsset(keyboardKl, KEYBOARD_KL_FILE);

        SystemProperties.set(KEYSWAP_PROPERTY, "1");
        while(!SystemProperties.get(KEYSWAP_PROPERTY).equals("2")) {
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        pm.reboot("");
    }
}
