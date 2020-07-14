package com.example.choturemote.ui.main;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choturemote.MainActivity;
import com.example.choturemote.R;

import java.util.ArrayList;

/**
 * @file SpeakingFragment.java
 * @brief Extends Fragment
 * @details
 * @li A Fragment is a piece of an application's user interface or behavior that can be placed in an Activity.
 * @li This one deals with the Speaking aspect
 */


/**
 * @brief Speaking Fragment
 * @details
 * @li A Fragment is a piece of an application's user interface or behavior that can be placed in an Activity.
 * @li This one deals with the Speaking aspect
 */
public class SpeakingFragment extends Fragment {

    /**
     * @var PITCH_FACTOR
     * @brief Fixed value
     * @details Dividing the actual pitch value
     *
     * @var DEFAULT_PITCH
     * @brief Default Pitch
     * @details Default Pitch as a float
     *
     * @var myPitch
     * @brief Float to hold current pitch
     * @details Holds the current pitch so tp display in the slider
     *
     * @var defaultPercentage
     * @brief Pitch is displayed as a percentage
     * @details DEFAULT_PITCH/PITCH_FACTOR is how the default pitch percentage is calculated
     *
     */
    private static float PITCH_FACTOR = 0.025f;
    private static float DEFAULT_PITCH = 1.0f; //is actually 0.01 * 100% //
    private static float myPitch;
    int defaultPercentage;

    /**
     * @brief Constructor
     * @details Required empty public constructor
     */
    public SpeakingFragment() {
    }

    /**
     * @brief Called to have the fragment instantiate its user interface view
     * @details
     * @li This fragment has a View inflated from the speaking_view.xml file
     * @li This View has a GridView with the resourceID "grid"; an EditText; a few buttons; a TextView for percentage display; and a seekBar
     * @li This GridView is associated with an Adapter which is an ExpressionsWordAdapter object
     * @li The ExpressionsWordAdapter object returns custom itemView object to populate the GridView
     * @li The EditView works with the speakNowButton to send custom text to be spoken
     * @li The SeekBar works with two buttons to handle pitch settings
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container          This is the parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflating speaking_view.xml (modified grid_view.xml) //
        View rootView = inflater.inflate(R.layout.speaking_view, container, false);

        // Entering custom text for speaking option //
        final EditText editText = rootView.findViewById(R.id.editText);
        ImageButton speakNowButton = rootView.findViewById(R.id.speak_now);

        // A SeekBar for changing pitch//
        final SeekBar pitchBar = rootView.findViewById(R.id.pitch_bar);
        final TextView pitchPercentage = rootView.findViewById(R.id.pitch_percentage);

        // Resetting and saving pitch settings //
        ImageButton resetPitch = rootView.findViewById(R.id.reset_pitch);
        ImageButton setDefaultPitch = rootView.findViewById(R.id.set_default_pitch);

        // A list of Word objects //
        final ArrayList<Word> words = new ArrayList<Word>();
        words.add(new Word(R.string.HI, R.drawable.speech_bubble));
        words.add(new Word(R.string.HELLO, R.drawable.speech_bubble));
        words.add(new Word(R.string.HOW_ARE_YOU, R.drawable.speech_bubble));
        words.add(new Word(R.string.WHATS_YOUR_NAME, R.drawable.speech_bubble));
        words.add(new Word(R.string.MY_NAME_IS, R.drawable.speech_bubble));
        words.add(new Word(R.string.IM_YOUR_FRIEND, R.drawable.speech_bubble));
        words.add(new Word(R.string.THANK_YOU,R.drawable.speech_bubble));
        words.add(new Word(R.string.YOU_ARE_WELCOME,R.drawable.speech_bubble));
        words.add(new Word(R.string.GOOD_JOB,R.drawable.speech_bubble));
        words.add(new Word(R.string.VERY_GOOD,R.drawable.speech_bubble));
        words.add(new Word(R.string.WOW,R.drawable.speech_bubble));
        words.add(new Word(R.string.BYE, R.drawable.speech_bubble));
        words.add(new Word(R.string.FINISH,R.drawable.speech_bubble));
        words.add(new Word(R.string.YES, R.drawable.speech_bubble));
        words.add(new Word(R.string.NO, R.drawable.speech_bubble));

        // Command String variables and calculations for expressions //
        myPitch = DEFAULT_PITCH;
        defaultPercentage = (int) (DEFAULT_PITCH/PITCH_FACTOR);
        pitchBar.setProgress(defaultPercentage);
        pitchPercentage.setText(getString(R.string.PITCH_SLIDER_INFO) + ": " + defaultPercentage + "%");
        final String SPEAK_OUT_CLASSIFIER = getString(R.string.SPEAK_OUT_CLASSIFIER);

        // An ExpressionsWordAdapter whose data source is a list of Words //
        // The adapter knows how to create custom itemViews for each item in the list //
        ExpressionsWordAdapter adapter = new ExpressionsWordAdapter(getActivity(), words, R.color.category_expressions);

        // Find the GridView view hierarchy of the Activity with the ID "grid" //
        GridView gridView = rootView.findViewById(R.id.grid);

        // Number of columns in the gridView //
        gridView.setNumColumns(1);

        // Make gridView use the SpeakingWordAdapter object we created above, so that the gridView will display items for each Word in the list.
        gridView.setAdapter(adapter);

        // Set a click listener to send command String when the list item is clicked //
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Word word = words.get(position);
                MainActivity.writeToBluetooth(SPEAK_OUT_CLASSIFIER, getString(word.getStringResourceId()),  String.valueOf(myPitch));
            }
        });

        // Set a click listener to send command String when the speakNowButton is clicked //
        speakNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.writeToBluetooth(SPEAK_OUT_CLASSIFIER, editText.getText().toString(), String.valueOf(myPitch));
                //editText.getText().clear();
            }
        });

        // Set a click listener to reset pitch //
        resetPitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pitchBar.setProgress(defaultPercentage);
                Toast.makeText(getContext(), "Pitch reset",Toast.LENGTH_SHORT).show();
            }
        });

        // Set a long click listener to reset pitch to default if updated //
        resetPitch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                defaultPercentage = (int) (DEFAULT_PITCH/PITCH_FACTOR);
                pitchBar.setProgress(defaultPercentage);
                Toast.makeText(getContext(), "Default pitch set to "+ defaultPercentage + "%",Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        // Set a long click listener to update default pitch //
        setDefaultPitch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                defaultPercentage = pitchBar.getProgress();
                Toast.makeText(getContext(), "Default pitch set to "+ defaultPercentage + "%", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        // Set a listener to update when seekBar updated by user//
        pitchBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) progress = 1;
                myPitch = progress * PITCH_FACTOR;
                pitchPercentage.setText(getString(R.string.PITCH_SLIDER_INFO) + ": " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return rootView;
    }
}
