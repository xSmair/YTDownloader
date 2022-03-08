package com.rxstudios.ytdownloader;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button buttonDownload;
    ProgressBar waitingProgressbar;
    ProgressBar downloadProgressbar;
    TextView remainingtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        try {
            YoutubeDL.getInstance().init(getApplicationContext());
            FFmpeg.getInstance().init(getApplicationContext());
            Log.d(TAG, "onCreate: Initialized");
        } catch (YoutubeDLException e) {
            Log.d(TAG, "onCreate: " + e.getCause());
        }
        editText = findViewById(R.id.editTextUrl);
        try {
            editText.setText(getIntent().getExtras().getString(Intent.EXTRA_TEXT));
        } catch (NullPointerException ignored) {
        }
        waitingProgressbar = findViewById(R.id.waitingProgressbar);
        downloadProgressbar = findViewById(R.id.downloadProgressbar);
        remainingtime = findViewById(R.id.remainingtime);

        buttonDownload = findViewById(R.id.buttonDownload);
        buttonDownload.setOnClickListener(view -> {
            requestPermission();
            runOnUiThread(() -> waitingProgressbar.setVisibility(View.VISIBLE));

            String url = editText.getText().toString().replace("youtu.be/", "youtube.com/watch?v=");
            //File youtubeDLDir = Environment.getDataDirectory();
            File youtubeDLDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YoutubeDL");
            Log.d(TAG, "onCreate: Saving file to " + youtubeDLDir.getAbsolutePath());
            YoutubeDLRequest request = new YoutubeDLRequest(url);
            request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");
            request.addOption("-f", "bestaudio[ext=m4a]/best[ext=mp3]/best");
            try {
                YoutubeDL.getInstance().execute(request, (progress, etaInSeconds, line) -> {
                    runOnUiThread(() -> {
                        waitingProgressbar.setVisibility(View.GONE);
                        downloadProgressbar.setProgress((int) progress * 10);
                        remainingtime.setText(etaInSeconds + getString(R.string.remainingtime));
                    });
                    Log.d(TAG, "onCreate: " + line);
                });
            } catch (YoutubeDLException | InterruptedException e) {
                Log.d(TAG, "onCreate: " + e.getCause());
                Log.d(TAG, "onCreate: " + e.getMessage());
            }
            runOnUiThread(() -> remainingtime.setText(getString(R.string.done)));
            Log.d(TAG, "onCreate: Done");
        });
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
}