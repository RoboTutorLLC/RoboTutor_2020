package cmu.xprize.comp_intervention;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.HIDE_INTERVENTION;
import static cmu.xprize.util.TCONST.I_TRIGGER_FAILURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 8/5/19.
 */

public class CInterventionHelpButton extends android.support.v7.widget.AppCompatImageButton {

    private Context mContext;

    private LocalBroadcastManager bManager;
    private ChangeReceiver bReceiver;

    // tracks which intervention has been triggered
    private boolean hasBeenTriggered;
    private String currentIntervention;

    public CInterventionHelpButton(Context context) {
        super(context);

        init(context);
    }

    public CInterventionHelpButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public CInterventionHelpButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void init(Context context) {
        this.mContext = context;

        bManager = LocalBroadcastManager.getInstance(mContext);

        bReceiver = new ChangeReceiver();

        // actual triggers
        IntentFilter filter = new IntentFilter();
        filter.addAction(I_TRIGGER_GESTURE);
        filter.addAction(I_TRIGGER_HESITATE);
        filter.addAction(I_TRIGGER_STUCK);
        filter.addAction(I_TRIGGER_FAILURE);

        filter.addAction(TCONST.HIDE_INTERVENTION);


        bManager.registerReceiver(bReceiver, filter);

        this.setOnTouchListener(new MyTouchListener());

    }

    class ChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getBooleanExtra("IS_MODAL", false)) return;

            // DO THIS THING where it's looking for the thing
            switch(intent.getAction()) {

                // all these are legit
                case I_TRIGGER_GESTURE:
                case I_TRIGGER_HESITATE:
                case I_TRIGGER_STUCK:
                case I_TRIGGER_FAILURE:
                    hasBeenTriggered = true;
                    break;


                // cancel
                case HIDE_INTERVENTION:
                    hasBeenTriggered = false;
                    break;

            }
        }
    }

    class MyTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!hasBeenTriggered) {
                return false;
            } else {

                // here is where the Modal should be triggered


                return true;
            }

        }
    }


}
