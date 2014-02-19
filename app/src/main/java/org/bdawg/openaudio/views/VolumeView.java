package org.bdawg.openaudio.views;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import org.bdawg.openaudio.control.R;

/**
 * Created by breland on 1/4/14.
 */
public class VolumeView  extends RelativeLayout{
    private static final String SUPERSTATE="superState";
    private static final String VOLUME="volume";

    private TextView currentVolumeTextView;
    private VerticalSeekBar currentVolumeSeekBar;
    private int currentVolume;


    public interface OnVolumeChangedListener {

        public void volumeChanged(int newVolume);

    }

    private OnVolumeChangedListener listener;

    public VolumeView(Context context) {
        super(context);
        init();
    }

    public VolumeView(Context context, AttributeSet attrs) {
        super(context,attrs);
        init();
    }

    public VolumeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle state=new Bundle();

        state.putParcelable(SUPERSTATE, super.onSaveInstanceState());
        state.putInt(VOLUME, getVolume());

        return(state);
    }

    @Override
    public void onRestoreInstanceState(Parcelable ss) {
        Bundle state=(Bundle)ss;

        super.onRestoreInstanceState(state.getParcelable(SUPERSTATE));

        setVolume(state.getInt(VOLUME));
    }

    public int getVolume(){
        return this.currentVolume;
    }

    public void setVolume(int newVolume){
        this.currentVolume = newVolume;
        this.currentVolumeTextView.setText(String.valueOf(newVolume));
        this.currentVolumeSeekBar.setProgress(newVolume);
    }

    private void init(){
        ((Activity)getContext())
                .getLayoutInflater()
                .inflate(R.layout.volume_panel, this ,true);
        this.currentVolumeSeekBar = (VerticalSeekBar)findViewById(R.id.vertical_seek_bar_volume);
        this.currentVolumeTextView = (TextView)findViewById(R.id.text_view_volume);
        this.currentVolumeSeekBar.setProgress(currentVolume);
        this.currentVolumeSeekBar.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                VolumeView.this.currentVolumeTextView.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                VolumeView.this.currentVolume = seekBar.getProgress();
                VolumeView.this.currentVolumeTextView.setText(String.valueOf(getVolume()));
                if (VolumeView.this.listener != null){
                    VolumeView.this.listener.volumeChanged(getVolume());
                }
            }
        });

    }

    public void setOnVolumeChangedListener(OnVolumeChangedListener listener){
        this.listener = listener;
    }
}
