package com.example.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Globally used variables
    ArrayList<File> listOfSongs = new ArrayList<File>();
    MediaPlayer mediaPlayer;
    ImageView PlayPause;
    int count = -1;
    AudioManager audioManager;
    SeekBar VolumeSeekBar;
    ImageButton LoopingButton;
    Boolean loopStatus = false;
    TextView LoopingText;
    //........................................................End of globally used variables


    // Request the access to the internal storage of the device

    public final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public final int EXTERNAL_REQUEST = 138;
    public boolean requestForPermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }
    public boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }

    //..........................................................End of request access.


    // The recursive extractor function which extracts the mp3 files.
    public void extract(String string){
        File path = new File(string);
        File[] listOfFiles = path.listFiles();
        if(listOfFiles == null){}
        else{
            for(File file:listOfFiles){
                if(file.isDirectory() || !file.canRead()){
                    extract(file.getPath());
                }
                else if(file.isHidden()){}
                else if(file.getName().endsWith(".mp3")){
                    listOfSongs.add(file);
                }
            }
        }
    }

    //Globally changing mediaPlayer variable
    public MediaPlayer play(File file){
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(file));
        return mediaPlayer;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestForPermission();

        extract("/storage/emulated/0");

        ArrayList<String> songs = new ArrayList<String>();

        for(File file:listOfSongs){
            songs.add(file.getName());
        }

        ListView listView = findViewById(R.id.Songs);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.custom_layout,songs);
        listView.setAdapter(arrayAdapter);


        VolumeSeekBar = findViewById(R.id.VolumeSeekBar);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        VolumeSeekBar.setMax(maxVolume);

        LoopingButton = findViewById(R.id.LoopingButton);

        LoopingText = (TextView)findViewById(R.id.LoopingText);
        LoopingText.setTextColor(Integer.parseInt(String.valueOf(Color.parseColor("#FF0000"))));

        SeekBar SongSeekBar = findViewById(R.id.SongSeekBar);
        PlayPause = (ImageView) findViewById(R.id.PlayPause);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                count = position;
                File file = listOfSongs.get(position);
                play(file);

                TextView textView = (TextView)findViewById(R.id.CurrentSong);
                textView.setText(songs.get(position));
                PlayPause.setImageResource(R.drawable.pause);

                SongSeekBar.setMax(mediaPlayer.getDuration());

                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        SongSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                        if(mediaPlayer!=null){
                            mediaPlayer.setLooping(loopStatus);
                        }
                    }
                },0,1000);

                mediaPlayer.start();
            }
        });

        SongSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("Touched",""+SongSeekBar.getProgress());
                mediaPlayer.seekTo(SongSeekBar.getProgress());
                return false;
            }
        });

        //.................Play and pause button in the footer for playing songs

        PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer == null){}
                else if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    PlayPause.setImageResource(R.drawable.play);
                }
                else{
                    PlayPause.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                }
            }
        });

        //...................................End of play pause button


        // ..................................Forward and backward button

        ImageView forward = (ImageView) findViewById(R.id.forward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count != -1){
                    count = (count+1)%listOfSongs.size();
                    File file = listOfSongs.get(count);
                    play(file);
                    TextView textView = (TextView)findViewById(R.id.CurrentSong);
                    textView.setText(songs.get(count));
                    mediaPlayer.start();
                }
            }
        });

        ImageView backward = (ImageView) findViewById(R.id.backward);
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count != -1){
                    count = count-1;
                    if(count == -1){
                        count = listOfSongs.size()-1;
                    }
                    File file = listOfSongs.get(count);
                    play(file);
                    TextView textView = (TextView)findViewById(R.id.CurrentSong);
                    textView.setText(songs.get(count));
                    mediaPlayer.start();
                }
            }
        });
        //......................................End of forward and backward button

        VolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        LoopingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loopStatus == true){
                    LoopingText.setTextColor(Integer.parseInt(String.valueOf(Color.parseColor("#FF0000"))));
                    loopStatus = false;
                }
                else{
                    LoopingText.setTextColor(Integer.parseInt(String.valueOf(Color.parseColor("#1DB954"))));
                    loopStatus = true;
                }
            }
        });
    }
}