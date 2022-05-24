package org.horaapps.soundrecorder.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.horaapps.soundrecorder.R;
import org.horaapps.soundrecorder.RecordingItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class PlaybackFragment extends DialogFragment {

    private static final String ARG_ITEM = "recording_item";

    public static PlaybackFragment newInstance(RecordingItem item){
        PlaybackFragment f = new PlaybackFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_ITEM,item);
        f.setArguments(b);
        return f;
    }

    private RecordingItem item;

    private long hours=0;
    private long minutes =0;
    private long seconds =0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        item = getArguments().getParcelable(ARG_ITEM);

        long itemDuration = item.getmLength();
        hours = TimeUnit.MILLISECONDS.toHours(itemDuration);
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration) - TimeUnit.HOURS.toMinutes(hours);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);
    }

    private TextView fileNameTV;
    private TextView fileLengthTV;
    private TextView currentTimeTV;
    private SeekBar seekBar;
    private FloatingActionButton playBtn;

    private MediaPlayer mediaPlayer;
    //stores whether or not the mediaplayer is currently playing audio
    private boolean isPlaying = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_media_playback,null);

        fileNameTV = view.findViewById(R.id.playback_file_name);
        currentTimeTV = view.findViewById(R.id.current_time);
        fileLengthTV = view.findViewById(R.id.file_length_playback);

        seekBar= view.findViewById(R.id.seekbar);
        ColorFilter filter = new LightingColorFilter
                (ContextCompat.getColor(getContext(),R.color.colorPrimary), ContextCompat.getColor(getContext(),R.color.colorPrimary));
        seekBar.getProgressDrawable().setColorFilter(filter);
        seekBar.getThumb().setColorFilter(filter);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress , boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    seekHandler.removeCallbacks(seekRunnable);
                    mediaPlayer.seekTo(progress);

                    long hours = TimeUnit.MILLISECONDS.toHours(progress);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(progress) - TimeUnit.HOURS.toMinutes(hours);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(progress) - TimeUnit.MINUTES.toSeconds(minutes);
                    if(hours==0){
                        currentTimeTV.setText(String.format("%02d:%02d",minutes,seconds));
                    }else{
                        currentTimeTV.setText(String.format("%02d:%02d:%02d",hours,minutes,seconds));
                    }

                    updateSeekbar();
                }else if(mediaPlayer == null && fromUser ){
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekbar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null) {
                    // remove message Handler from updating progress bar
                    seekHandler.removeCallbacks(seekRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());

                    long hours = TimeUnit.MILLISECONDS.toHours(seekBar.getProgress());
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(seekBar.getProgress()) - TimeUnit.HOURS.toMinutes(hours);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(seekBar.getProgress()) - TimeUnit.MINUTES.toSeconds(minutes);
                    if(hours==0){
                        currentTimeTV.setText(String.format("%02d:%02d",minutes,seconds));
                    }else{
                        currentTimeTV.setText(String.format("%02d:%02d:%02d",hours,minutes,seconds));
                    }

                    updateSeekbar();
                }
            }
        });

        playBtn = view.findViewById(R.id.fab_play);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay(isPlaying);
                isPlaying = !isPlaying;
            }
        });

        fileNameTV.setText(item.getmName());
        if(hours==0){
            fileLengthTV.setText(String.format("%02d:%02d",minutes,seconds));
        }else{
            fileLengthTV.setText(String.format("%02d:%02d:%02d",hours,minutes,seconds));
        }

        builder.setView(view);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
    }

    private void prepareMediaPlayerFromPoint(int progress) {
        //set mediaPlayer to start from middle of the audio file

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(item.getmFilePath());
            mediaPlayer.prepare();
            seekBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.seekTo(progress);

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void onPlay(boolean isPlaying) {
        if( ! isPlaying ){
            if(mediaPlayer==null){
                //it will start from beginning
                startPlaying();
            }else {
                resumePlaying();
            }
        }else{
            pausePlaying();
        }
    }

    private void pausePlaying() {
        playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        seekHandler.removeCallbacks(seekRunnable);
        mediaPlayer.pause();
    }

    private void resumePlaying() {
        playBtn.setImageResource(R.drawable.ic_baseline_pause_24);
        mediaPlayer.start();
        updateSeekbar();
    }

    private void startPlaying() {
        playBtn.setImageResource(R.drawable.ic_baseline_pause_24);

        mediaPlayer= new MediaPlayer();
        try {
            mediaPlayer.setDataSource(item.getmFilePath());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });

        updateSeekbar();

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void stopPlaying() {
        playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);

        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer=null;

        seekBar.setProgress(seekBar.getMax());
        currentTimeTV.setText(fileLengthTV.getText().toString());

        isPlaying = !isPlaying;

        //allow the screen to turn off again once audio is finished playing
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    Handler seekHandler = new Handler(Looper.getMainLooper());
    Runnable seekRunnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer!=null){
                int currentProgress = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentProgress);

                long hours = TimeUnit.MILLISECONDS.toHours(currentProgress);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(currentProgress) - TimeUnit.HOURS.toMinutes(hours);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(currentProgress) - TimeUnit.MINUTES.toSeconds(minutes);
                if(hours==0){
                    currentTimeTV.setText(String.format("%02d:%02d",minutes,seconds));
                }else{
                    currentTimeTV.setText(String.format("%02d:%02d:%02d",hours,minutes,seconds));
                }
            }

            seekHandler.postDelayed(seekRunnable,1000);
        }
    };

    private void updateSeekbar(){
        seekHandler.postDelayed(seekRunnable,1000);
    }

    @Override
    public void onStart() {
        super.onStart();

        //set transparent background
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mediaPlayer != null) {
            pausePlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            stopPlaying();
        }
    }
}
