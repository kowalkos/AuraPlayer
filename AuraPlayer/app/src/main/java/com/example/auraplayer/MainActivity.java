package com.example.auraplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private long downloadID;
    ArrayList<String> listLinks;
    ArrayList<String> pathsToFiles;
    VideoView videoView;
    TextView textView;

    private static int currentvideo=0;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.textView2);
        videoView = findViewById(R.id.videoView);

        pathsToFiles = new ArrayList<>();


        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        if (isConnected()) {
            listLinks = new ArrayList<>();
            final AuraDataService auraDataService = new AuraDataService(this);
            auraDataService.getData(new AuraDataService.VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Toast.makeText(MainActivity.this, "SOMETHINGS WRONG", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onResponse(JSONObject jsonObject) {

                    try {
                        JSONObject jsonObject1 = jsonObject.getJSONObject("itemsAndReviewsExperience");
                        jsonObject1 = jsonObject1.getJSONObject("pagesById");
                        jsonObject1 = jsonObject1.getJSONObject("264743");
                        JSONArray jsonArray = jsonObject1.getJSONArray("things");
                        jsonObject1 = jsonArray.getJSONObject(0);
                        jsonArray = jsonObject1.getJSONArray("slides");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            if (object.getString("type").equals("VIDEO")) {
                                listLinks.add(object.getString("path"));
                            }
                        }

                        pathsToFiles.clear();
                        String filename;
                        for (int i = 0; i < listLinks.size(); i++) {
                            filename = listLinks.get(i).substring(listLinks.get(i).lastIndexOf('/') + 1);
                            File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/" + filename);

                            if (!tempFile.isFile()) {
                                Toast.makeText(MainActivity.this, "Downloading files", Toast.LENGTH_SHORT).show();
                                downloading_and_playing(i, filename);
                            } else {
                                play_video(filename);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            videoView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private BroadcastReceiver onDownloadComplete=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID-1 == id) {
                Uri uri=Uri.parse(pathsToFiles.get(currentvideo));
                videoView.setVideoURI(uri);

                MediaController mediacontroller = new MediaController(
                        MainActivity.this);
                mediacontroller.setAnchorView(videoView);
                videoView.setMediaController(mediacontroller);
                currentvideo++;
                videoView.requestFocus();
                videoView.start();
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        videoView.stopPlayback();


                        Uri nextUri=Uri.parse(pathsToFiles.get(currentvideo++));
                        videoView.setVideoURI(nextUri);
                        videoView.start();
                        if(currentvideo==pathsToFiles.size())
                        {
                            currentvideo=0;
                        }
                    }
                });

            }
        }
    };
    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
        }
        return connected;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    private void play_video(String filename) {
        Uri uri;
        pathsToFiles.add(Environment.getExternalStorageDirectory().getPath()+"/Movies/"+ filename);
        uri=Uri.parse(pathsToFiles.get(0));
        MediaController mediaController=new MediaController(MainActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setVideoURI(uri);
        videoView.setMediaController(mediaController);
        currentvideo=1;
        videoView.requestFocus();
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.stopPlayback();
                Uri nextUri=Uri.parse(pathsToFiles.get(currentvideo++));
                videoView.setVideoURI(nextUri);
                videoView.start();
                if(currentvideo==pathsToFiles.size())
                {
                    currentvideo=0;
                }
            }
        });
    }

    private void downloading_and_playing(int i, String filename) {
        if(!pathsToFiles.contains(Environment.getExternalStorageDirectory().getPath()+"/Movies/"+ filename)) {
            Uri downloadUri = Uri.parse(listLinks.get(i));
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);
            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("Aura files")
                    .setDescription(filename)

                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, filename);
            DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

            downloadID = mgr.enqueue(request);

        }
        pathsToFiles.add(Environment.getExternalStorageDirectory().getPath()+"/Movies/"+ filename);
    }
}