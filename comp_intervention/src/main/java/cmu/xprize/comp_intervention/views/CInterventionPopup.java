package cmu.xprize.comp_intervention.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;

import cmu.xprize.comp_intervention.data.CInterventionStudentData;
import cmu.xprize.comp_logging.CInterventionLogManager;
import cmu.xprize.comp_logging.CPerfLogManager;
import cmu.xprize.comp_logging.InterventionLogItem;
import cmu.xprize.util.GlobalStaticsEngine;
import cmu.xprize.util.ImageLoader;
import cmu.xprize.util.TCONST;
import me.delandbeforeti.comp_intervention.R;

import static cmu.xprize.util.TCONST.EXIT_FROM_INTERVENTION;
import static cmu.xprize.util.TCONST.I_MODAL_EXTRA;
import static cmu.xprize.util.TCONST.I_TRIGGER_FAILURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;
import static cmu.xprize.util.TCONST.I_TYPE_APPLICATION;
import static cmu.xprize.util.TCONST.I_TYPE_KNOWLEDGE;

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

    // interventions will provide progressively less information
    protected HashMap<String, Integer> taperLevels;


    public CInterventionPopup(Context context) {
        super(context);
        init(context);
    }

    public CInterventionPopup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CInterventionPopup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
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

        bManager.registerReceiver(new InterventionPopupMessageReceiver(), filter);

        taperLevels = new HashMap<>();
        taperLevels.put(I_TYPE_KNOWLEDGE, 0);
        taperLevels.put(I_TYPE_APPLICATION, 0);
    }

    private View.OnClickListener exitListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideIntervention();

            Intent msg = new Intent(EXIT_FROM_INTERVENTION);
            bManager.sendBroadcast(msg);
        }
    };

    class InterventionPopupMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("INTERVENTION", "Received broadcast.");

            String imgRef;
            // only display if the "modal" is set
            String action = intent.getAction();
            boolean modal = intent.getBooleanExtra(I_MODAL_EXTRA, false);
            if (!modal) return;
            if (action == null) return;

            int taperedLevel = -1;
            switch(action) {

                case I_TRIGGER_GESTURE:
                case I_TRIGGER_STUCK:
                    taperedLevel = taperLevels.get(I_TYPE_APPLICATION);
                    // increment to next level
                    Log.wtf("TAPER", "Application support level: " + taperedLevel);
                    taperLevels.put(I_TYPE_APPLICATION, taperedLevel + 1);

                    break;


                case I_TRIGGER_HESITATE:
                case I_TRIGGER_FAILURE:
                    taperedLevel = taperLevels.get(I_TYPE_KNOWLEDGE);
                    // increment to next level
                    Log.wtf("TAPER", "Knowledge support level: " + taperedLevel);
                    taperLevels.put(I_TYPE_KNOWLEDGE, taperedLevel + 1);

                    break;

            }
            Log.d("INTERVENTION", "Received " + action);
            imgRef = getChildPhoto(action);
            displayImage(imgRef);
            playHelpAudio(action, taperedLevel);
            interventionLabel.setText(action);

            CInterventionLogManager.getInstance().postInterventionLog(new InterventionLogItem(
                    (new Date()).getTime(),
                    CInterventionStudentData.getCurrentStudentId(),
                    null,
                    GlobalStaticsEngine.getCurrentTutorId(),
                    imgRef,
                    action,
                    true
            ));

        }
    }

    /**
     * Display an Image in the Intervention Modal
     * @param imageRef path to child photo
     */
    private void displayImage(String imageRef) {
        Log.d("INTERVENTION", "Displaying image: " + imageRef);

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
    private void playHelpAudio(String intervention, int level) {

        int[] audioPlayback = {
          R.raw.intervention_audio
        };

        int[] appSupportAudio = {
                R.raw.aud1,
                R.raw.aud2,
                R.raw.aud3,
        };

        int[] knowledgeSupportAudio = {
                R.raw.aud5,
                R.raw.aud4,
                R.raw.aud3,
        };

        level = level > 2 ? 2 : level; // level max out at 2
        int audio;
        switch(intervention) {
            case I_TRIGGER_GESTURE:
            case I_TRIGGER_STUCK:
                audio = appSupportAudio[level];
                break;

            case I_TRIGGER_FAILURE:
            case I_TRIGGER_HESITATE:
            default:
                audio = knowledgeSupportAudio[level];
                break;
        }

        // NEXT continue here!
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, audio);
        mediaPlayer.start();
    }

    // should hide when the student taps on the image (no exit button)

    private void hideIntervention() {
        Log.d("INTERVENTION", "Hiding intervention");

        interventionContainer.setVisibility(View.GONE);
    }

    // Placeholder for eventual image selection code.
    /*private static HashMap<String, String> imgPath;
    static {
        imgPath = new HashMap<>();
        imgPath.put(I_TRIGGER_GESTURE, "child1.png");
        imgPath.put(I_TRIGGER_FAILURE, "child2.png");
        imgPath.put(I_TRIGGER_STUCK, "child3.png");
        imgPath.put(I_TRIGGER_HESITATE, "child4.png");
    }*/

    /**
     * Get a String image filename based on intervention type and domain
     *
     * @param interventionType GESTURE, FAILURE, STUCK, or HESITATE
     * @return String path to child's photo
     */
    private String getChildPhoto(String interventionType) {

        /*String imgRef = imgPath.get(interventionType);
        if (imgRef == null) imgRef = "image1.jpg"; // default placeholder*/

        String imgRef;
        switch (interventionType) {
            case I_TRIGGER_GESTURE:
            case I_TRIGGER_STUCK:
                imgRef = CInterventionStudentData.getPhotoForApplicationSupport(
                        GlobalStaticsEngine.getCurrentTutorType());
                break;

            case I_TRIGGER_FAILURE:
            case I_TRIGGER_HESITATE:
            default:
                imgRef = CInterventionStudentData.getPhotoForKnowledgeSupport(
                        GlobalStaticsEngine.getCurrentDomain());
                break;
        }

        return imgRef;

    }
}
