package cmu.xprize.comp_intervention;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.HashMap;

import cmu.xprize.util.ImageLoader;
import cmu.xprize.util.TCONST;
import me.delandbeforeti.comp_intervention.R;

import static cmu.xprize.util.TCONST.EXIT_FROM_INTERVENTION;
import static cmu.xprize.util.TCONST.HIDE_INTERVENTION;
import static cmu.xprize.util.TCONST.I_MODAL_EXTRA;
import static cmu.xprize.util.TCONST.I_TRIGGER_FAILURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 6/10/19.
 */

public class CInterventionPopup extends RelativeLayout {

    public Context  mContext;

    private LinearLayout interventionContainer;
    private ImageView interventionImage;

    private TextView interventionLabel;

    private LocalBroadcastManager bManager;
    private ChangeReceiver        bReceiver;


    public CInterventionPopup(Context context) {
        super(context);
        init(context, null);
    }

    public CInterventionPopup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CInterventionPopup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Log.d("INTERVENTION", "Initializing intervention.");

        inflate(getContext(), R.layout.intervention_layout, this);

        mContext = context;

        interventionImage = findViewById(R.id.SInterventionImage);
        interventionImage.setOnClickListener(exitListener);
        interventionContainer = findViewById(R.id.SInterventionContainer);


        interventionLabel = findViewById(R.id.interventionLabel);


        bManager = LocalBroadcastManager.getInstance(getContext());

        // actual triggers
        IntentFilter filter = new IntentFilter();
        filter.addAction(I_TRIGGER_GESTURE);
        filter.addAction(I_TRIGGER_HESITATE);
        filter.addAction(I_TRIGGER_STUCK);
        filter.addAction(I_TRIGGER_FAILURE);

        filter.addAction(TCONST.HIDE_INTERVENTION);

        bReceiver = new ChangeReceiver();

        bManager.registerReceiver(bReceiver, filter);
    }

    private View.OnClickListener exitListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideIntervention();

            Intent msg = new Intent(EXIT_FROM_INTERVENTION);
            bManager.sendBroadcast(msg);
        }
    };

    class ChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("INTERVENTION", "Received broadcast.");

            String imgRef;
            // only display if the "modal" is set
            String action = intent.getAction();
            boolean modal = intent.getBooleanExtra(I_MODAL_EXTRA, false);
            if (!modal) return;
            if (action == null) return;

            switch(action) {

                case I_TRIGGER_HESITATE:
                case I_TRIGGER_GESTURE:
                case I_TRIGGER_STUCK:
                case I_TRIGGER_FAILURE:
                    Log.d("INTERVENTION", "Received " + action);
                    imgRef = getChildPhoto(action, null);
                    displayImage(imgRef);
                    playHelpAudio();
                    interventionLabel.setText(action);
                    // flashHandRaise();
                    break;

                case HIDE_INTERVENTION:
                default:
                    hideIntervention();
            }

        }
    }

    // TODO make new option for if kid self-chooses to ask for help without a trigger

    /**
     * Flash the Hand Raise thing in the top right corner
     */
    private void flashHandRaise() {
        // how do i make the thing flash by ID without needing a dependency on the comp_banner component???
        // broadcast???
    }

    /**
     * Display an Image in the Intervention Modal
     * @param imageRef path to child photo
     */
    private void displayImage(String imageRef) {
        Log.d("INTERVENTION", "Displaying image: " + imageRef);

        String imgPath;
        try {
            ImageLoader.makeBitmapLoader(TCONST.INTERVENTION_FOLDER + "/")
                    .loadBitmap(imageRef)
                    .into(interventionImage);

            interventionContainer.setVisibility(View.VISIBLE);

        } catch (FileNotFoundException e) {
            Log.e("INTERVENTION", "Image " + imageRef + " not found");
            e.printStackTrace();
        }
    }

    // INT_POPUP make sure this only plays once
    private void playHelpAudio() {

        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, R.raw.intervention_audio);
        mediaPlayer.start();
    }

    // should hide when the student taps on the image (no exit button)

    private void hideIntervention() {
        Log.d("INTERVENTION", "Hiding intervention");

        interventionContainer.setVisibility(View.GONE);
    }

    // Placeholder for eventual image selection code.
    private static HashMap<String, String> imgPath;
    static {
        imgPath = new HashMap<>();
        imgPath.put(I_TRIGGER_GESTURE, "child1.png");
        imgPath.put(I_TRIGGER_FAILURE, "child2.png");
        imgPath.put(I_TRIGGER_STUCK, "child3.png");
        imgPath.put(I_TRIGGER_HESITATE, "child4.png");
    }

    /**
     * Get a String image filename based on intervention type and domain
     *
     * @param interventionType GESTURE, FAILURE, STUCK, or HESITATE
     * @param activityOrDomain MATH, LIT, STORIES, etc
     * @return String path to child's photo
     */
    private String getChildPhoto(String interventionType, String activityOrDomain) {

        String imgRef = imgPath.get(interventionType);
        if (imgRef == null) imgRef = "image1.jpg"; // default placeholder

        return imgRef;

    }
}
