package cmu.xprize.comp_intervention;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.FileNotFoundException;

import cmu.xprize.util.ImageLoader;
import cmu.xprize.util.TCONST;
import me.delandbeforeti.comp_intervention.R;

import static cmu.xprize.util.TCONST.HIDE_INTERVENTION;
import static cmu.xprize.util.TCONST.INTERVENTION_1;
import static cmu.xprize.util.TCONST.INTERVENTION_2;
import static cmu.xprize.util.TCONST.INTERVENTION_3;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 6/10/19.
 */

public class CIntervention extends RelativeLayout {

    public Context  mContext;

    private ImageView interventionImage;

    private LocalBroadcastManager bManager;
    private ChangeReceiver        bReceiver;


    private static String[] imgPath = {"image1.jpg", "image2.jpg", "image3.jpg"};

    public CIntervention(Context context) {
        super(context);
        init(context, null);
    }

    public CIntervention(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CIntervention(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Log.d("INTERVENTION", "Initializing intervention.");

        inflate(getContext(), R.layout.intervention_layout, this);

        mContext = context;

        interventionImage = findViewById(R.id.SInterventionImage);


        bManager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter filter = new IntentFilter(INTERVENTION_1);
        filter.addAction(INTERVENTION_2);
        filter.addAction(INTERVENTION_3);
        filter.addAction(TCONST.HIDE_INTERVENTION);

        bReceiver = new ChangeReceiver();

        bManager.registerReceiver(bReceiver, filter);
    }

    class ChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("INTERVENTION", "Received broadcast.");

            switch(intent.getAction()) {

                case INTERVENTION_1:
                    displayImage(imgPath[0]);
                    break;

                case INTERVENTION_2:
                    displayImage(imgPath[1]);
                    break;

                case INTERVENTION_3:
                    displayImage(imgPath[2]);
                    break;

                case HIDE_INTERVENTION:
                    hideIntervention();
            }

        }
    }

    private void displayImage(String imageRef) {
        Log.d("INTERVENTION", "Displaying image: " + imageRef);

        String imgPath;
        try {
            ImageLoader.makeBitmapLoader(TCONST.INTERVENTION_FOLDER + "/")
                    .loadBitmap(imageRef)
                    .into(interventionImage);

            interventionImage.setVisibility(View.VISIBLE);

        } catch (FileNotFoundException e) {
            Log.e("INTERVENTION", "Image " + imageRef + " not found");
            e.printStackTrace();
        }
    }

    private void hideIntervention() {
        Log.d("INTERVENTION", "Hiding intervention");

        interventionImage.setVisibility(View.GONE);
    }
}
