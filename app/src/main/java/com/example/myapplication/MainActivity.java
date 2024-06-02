package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String fileName;
    private Button button;
    private static final int REQUEST_CODE_SAVE_AUDIO = 1; // Используйте уникальные коды запросов
    private static final int REQUEST_CODE_PICK_AUDIO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.stopRecord);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        releasePlayer();
        releaseRecorder();
    }

    private void releaseRecorder(){
        if(mediaRecorder != null){
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    public void recordStart(View view){
        try{
            releaseRecorder();
            // Генерируем уникальное имя файла для каждой записи
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            fileName = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/recording_" + timeStamp + ".3gpp";
            File outFile = new File(fileName);
            if(outFile.exists()) outFile.delete();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(fileName);
            mediaRecorder.prepare();
            mediaRecorder.start();
            button.setEnabled(true);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void recordStop(View view){
        if(mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            button.setEnabled(false);
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/3gpp");
        startActivityForResult(intent, REQUEST_CODE_SAVE_AUDIO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_CODE_SAVE_AUDIO) {
                saveRecordingToFile(uri);
            } else if (requestCode == REQUEST_CODE_PICK_AUDIO) {
                playAudioFromUri(uri);
            }
        }
    }

    private void saveRecordingToFile(Uri uri) {
        try {
            InputStream in = new FileInputStream(new File(fileName));
            OutputStream out = getContentResolver().openOutputStream(uri);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releasePlayer(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void playStart(View view){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/3gpp");
        startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO);
    }

    private void playAudioFromUri(Uri audioUri) {
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void playStop(View view){
        if(mediaPlayer != null) mediaPlayer.stop();
    }
}
