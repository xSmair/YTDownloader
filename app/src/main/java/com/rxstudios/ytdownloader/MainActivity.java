package com.rxstudios.ytdownloader;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    EditText editTextUrl;
    Button buttonDownload;
    ProgressBar waitingProgressbar;
    ProgressBar downloadProgressbar;
    TextView remainingTime;
    ImageView settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        try {
            YoutubeDL.getInstance().init(getApplicationContext());
            FFmpeg.getInstance().init(getApplicationContext());
            Log.d(TAG, "onCreate: Initialized");
        } catch (YoutubeDLException e) {
            Log.d(TAG, "onCreate: " + e.getCause());
        }
        editTextUrl = findViewById(R.id.editTextUrl);
        try {
            editTextUrl.setText(getIntent().getExtras().getString(Intent.EXTRA_TEXT));
        } catch (NullPointerException ignored) {
        }
        waitingProgressbar = findViewById(R.id.waitingProgressbar);
        downloadProgressbar = findViewById(R.id.downloadProgressbar);
        remainingTime = findViewById(R.id.remainingtime);
        settingsButton = findViewById(R.id.settingsbutton);
        buttonDownload = findViewById(R.id.buttonDownload);
        buttonDownload.setOnClickListener(view -> {
            requestPermission();
            runOnUiThread(() -> waitingProgressbar.setVisibility(View.VISIBLE));

            startDownload();

           remainingTime.setText(getString(R.string.done));
            Log.d(TAG, "onCreate: Done");
        });
        boolean pref = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("auto_download_on_share", false);
        String text = "";
        try {
            text = editTextUrl.getText().toString();
        }catch (Exception ignored){}
        if(pref&&!text.equals("")){
            buttonDownload.setVisibility(View.GONE);
            requestPermission();
            waitingProgressbar.setVisibility(View.VISIBLE);

            startDownload();


        }
        settingsButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    private void requestPermission() {
        String[] perms = new String[2];
        perms[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        perms[1] = Manifest.permission.READ_EXTERNAL_STORAGE;
        requestPermissions(perms, 1);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startDownload(){
        String url = editTextUrl.getText().toString().replace("youtu.be/", "youtube.com/watch?v=");
        //File youtubeDLDir = Environment.getDataDirectory();
        //File youtubeDLDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YoutubeDL");
        File youtubeDLDir = new File(PreferenceManager.getDefaultSharedPreferences(this).getString("downloadpath", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()));
        Log.d(TAG, "onCreate: Saving file to " + youtubeDLDir.getAbsolutePath());
        YoutubeDLRequest request = new YoutubeDLRequest(url);
        request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
        request.addOption("-f", "bestaudio[ext=m4a]/best[ext=mp3]/best");
        try {
            YoutubeDL.getInstance().execute(request, (progress, etaInSeconds, line) -> {
                    updateProgress(progress, etaInSeconds);
                    Log.d(TAG, "onCreate: " + line);
            });
        } catch (YoutubeDLException | InterruptedException e) {
            Log.d(TAG, "onCreate: " + e.getCause());
            Log.d(TAG, "onCreate: " + e.getMessage());
        }
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            remainingTime.setText(getString(R.string.done));
            Log.d(TAG, "onCreate: Done");
        }).start();
    }

    private void updateProgress(float progress, long etaInSeconds){
        new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    waitingProgressbar.setVisibility(View.GONE);
                    downloadProgressbar.setProgress((int) progress * 10);
                    remainingTime.setText(etaInSeconds + getString(R.string.remainingtime));
                });
            }catch (Exception x){
                Log.d(TAG, "run: " + x);
            }
        }).start();
    }
}