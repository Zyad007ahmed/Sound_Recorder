package org.horaapps.soundrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.horaapps.soundrecorder.activities.MainActivity;

import java.io.File;
import java.io.IOException;

public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";

    public RecordingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null ;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DB = new DBHelper(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        if(recorder != null){
            stopRecording();
        }

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    MediaRecorder recorder = null;

    String fileName =null;
    String filePath = null;

    long startingTimeMillis ;

    private void startRecording() {
        setFileNAmeAndPath();

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFile(filePath);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(192000);
        }

        try {
            recorder.prepare();
            recorder.start();
            startingTimeMillis = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG,"prepare() failed");
        }

    }

    private DBHelper DB;

    private void setFileNAmeAndPath() {
        int count=0;
        File file;

        do {
            count++;

            fileName = getString(R.string.default_file_name) +"_"+ (DB.getCount()+count)+".mp3";
            filePath = MainActivity.folder.toString() +"/"+fileName;

            file = new File(filePath);
        }while (file.exists() && !file.isDirectory());
    }

    private long elapsedMillis;

    public void stopRecording(){
        recorder.stop();
        recorder.release();
        elapsedMillis = System.currentTimeMillis() - startingTimeMillis;

        Toast.makeText(this,getString(R.string.toast_recording_finish) + " " + filePath,Toast.LENGTH_LONG).show();

        recorder=null;

        DB.addRecording(fileName,filePath,elapsedMillis);
    }
}