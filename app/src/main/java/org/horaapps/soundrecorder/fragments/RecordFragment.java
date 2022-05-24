package org.horaapps.soundrecorder.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.horaapps.soundrecorder.R;
import org.horaapps.soundrecorder.RecordingService;

public class RecordFragment extends Fragment {

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }


    private Chronometer chronometer;
    private FloatingActionButton recordButton;
    private TextView recordingPrompt;
    private ProgressBar progressBar;

    private boolean startRecording = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        chronometer = recordView.findViewById(R.id.chronometer);
        recordButton = recordView.findViewById(R.id.btnRecord);
        recordingPrompt = recordView.findViewById(R.id.recording_status_text);
        progressBar = recordView.findViewById(R.id.recordProgressBar);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(startRecording);
                startRecording = !startRecording;

            }
        });

        return recordView;
    }

    private int promptCount = 0;

    private void onRecord(boolean start) {
        Intent intent = new Intent(getActivity(), RecordingService.class);

        if(start){
            recordButton.setImageResource(R.drawable.ic_baseline_stop_24);

            Toast.makeText(getActivity(),getString(R.string.toast_recording_start),Toast.LENGTH_SHORT).show();

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if(promptCount==0){
                        recordingPrompt.setText(getString(R.string.record_in_progress) + " .");
                    }else if (promptCount==1){
                        recordingPrompt.setText(getString(R.string.record_in_progress) + " . .");
                    }else if (promptCount==2){
                        recordingPrompt.setText(getString(R.string.record_in_progress) + " . . .");
                        promptCount=-1;
                    }

                    promptCount++;
                }
            });
            recordingPrompt.setText(getString(R.string.record_in_progress) + " .");
            promptCount++;

            //start recordingService
            getActivity().startService(intent);
            //keep the screen on
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }else{
            recordButton.setImageResource(R.drawable.ic_baseline_mic_24);

            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());

            recordingPrompt.setText(getString(R.string.record_prompt));

            //stop recordingService
            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}