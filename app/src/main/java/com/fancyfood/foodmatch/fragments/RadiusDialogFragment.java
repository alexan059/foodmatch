package com.fancyfood.foodmatch.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.fancyfood.foodmatch.R;

public class RadiusDialogFragment extends DialogFragment implements OnSeekBarChangeListener {

    private static final String TAG = RadiusDialogFragment.class.getSimpleName();

    private int radius;
    private TextView tvRadius;
    private SeekBar seekBar;

    // Interface for callback options exchange
    public interface RadiusDialogListener {
        public void onDialogPositiveClick(RadiusDialogFragment dialog);
    }

    public RadiusDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use builder class for dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set text messages
        builder.setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Accept changes
                        listener.onDialogPositiveClick(RadiusDialogFragment.this);
                        Log.d(TAG, "Positive Click");
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel
                        RadiusDialogFragment.this.getDialog().cancel();
                        Log.d(TAG, "Negative Click");
                    }
                });

        // Inflate custom view
        View view = inflater.inflate(R.layout.action_view_seekbar, null);
        builder.setView(view);

        // Initialize seekbar
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgress(radius);

        // Initialize textview
        tvRadius = (TextView) view.findViewById(R.id.tvRadius);
        tvRadius.setText("Umkreis: " + Integer.toString(radius) + "00 m");


        // Return new dialog
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the RadiusDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the RadiusDialogListener so we can send events to the host
            listener = (RadiusDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement RadiusDialogListener");
        }
    }

    /* Getter and Setter */

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    /* Seek bar listener */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.radius = progress;

        if (tvRadius != null)
            tvRadius.setText("Umkreis: " + Integer.toString(progress) + "00 m");

        Log.d(TAG, "Umkreis: " + Integer.toString(radius) + "00 m");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
