package com.example.abhinavjain.nyuhack;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton btnSpeak;
    private DrawerLayout drawerLayout;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private LinearLayout list;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        list = (LinearLayout) findViewById(R.id.list);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        btnSpeak = (FloatingActionButton) findViewById(R.id.mic);

        setTitle("NYUhack");
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);

        if (!checkPermission()) requestPermission();
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    public void addListItem(EmotionModel model) {

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.list_item, null);
        TextView text = (TextView) view.findViewById(R.id.text);
        TextView tag = (TextView) view.findViewById(R.id.tag1);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.linear);
        text.setTypeface(null, Typeface.BOLD);
        text.setText(model.text);
        tag.setTypeface(null, Typeface.BOLD);
        tag.setTypeface(null, Typeface.ITALIC);
        tag.setText("#" + model.tag);

        if (model.tag.equalsIgnoreCase("anger")) {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.red));
        } else if (model.tag.equalsIgnoreCase("happy")) {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.green));
        } else if (model.tag.equalsIgnoreCase("warning")) {
            linearLayout.setBackgroundColor(getResources().getColor(R.color.yellow));

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle("Warning received")
                    .setContentText(model.text)
                    .setVibrate(new long[]{1000, 1000})
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());

        } else {
            text.setTextColor(Color.BLACK);
            tag.setTextColor(Color.BLACK);
        }

        list.addView(view);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        intent.putExtra("android.speech.extra.GET_AUDIO", true);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    Callback<EmotionModel> response = new Callback<EmotionModel>() {
        @Override
        public void onResponse(Call<EmotionModel> call, Response<EmotionModel> response) {
            Log.d("qwe- ", "success");
            EmotionModel model = response.body();
            addListItem(model);
        }

        @Override
        public void onFailure(Call<EmotionModel> call, Throwable t) {
            Log.d("qwe- ", "failure");
        }
    };

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    try {
                        //stopRecording();
                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        //txtSpeechInput.setText(result.get(0));

                        Bundle bundle = data.getExtras();
                        ArrayList<String> matches = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
                        // the recording url is in getData:
                        Uri audioUri = data.getData();
                        Log.d("qwe-", audioUri.toString());
                        ContentResolver contentResolver = getContentResolver();
                        InputStream initialStream = contentResolver.openInputStream(audioUri);
                        byte[] buffer = new byte[initialStream.available()];
                        initialStream.read(buffer);
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                CreateRandomAudioFileName(5) + "AudioRecording.wav";
                        File targetFile = new File(path);
                        Log.d("qwe-", targetFile.getAbsolutePath());
                        OutputStream outStream = new FileOutputStream(targetFile);
                        outStream.write(buffer);
                        RetrofitService.getInstance().uploadFile(path, audioUri, result.get(0), getApplicationContext(), response);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }

        }
    }

    public String CreateRandomAudioFileName(int string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string) {
            stringBuilder.append("ABCDEFGHIJKLMNOP".
                    charAt(new Random().nextInt("ABCDEFGHIJKLMNOP".length())));

            i++;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        //Toast.makeText(MainActivity.this, "Permission Granted",
                        //      Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}