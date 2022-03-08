package com.rxstudios.ytdownloader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.codekidlabs.storagechooser.StorageChooser;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }

    private Context getAcitvity(){
        return this;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private static final String TAG = "asd";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onStart() {
            super.onStart();
            findPreference("downloadpath").setOnPreferenceClickListener(preference -> {
                StorageChooser storageChooser = new StorageChooser.Builder()
                        .withActivity(getActivity())
                        .withFragmentManager(getActivity().getFragmentManager())
                        .withMemoryBar(true)
                        .allowCustomPath(true)
                        .allowAddFolder(true)
                        .setType(StorageChooser.DIRECTORY_CHOOSER)
                        .build();

                storageChooser.setOnSelectListener(path -> {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("downloadpath", path);
                    editor.commit();
                    Log.d(TAG, "onStart: Set path to: " + path);
                    //findPreference("downloadpath").setDefaultValue(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YoutubeDL").getAbsoluteFile());
                });
                storageChooser.show();
            return true;
            });
        }
    }
}