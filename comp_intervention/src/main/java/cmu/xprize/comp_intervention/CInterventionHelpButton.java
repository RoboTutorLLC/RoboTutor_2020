package cmu.xprize.comp_intervention;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Date;

import cmu.xprize.comp_intervention.data.CInterventionStudentData;
import cmu.xprize.comp_logging.CInterventionLogManager;
import cmu.xprize.comp_logging.InterventionLogItem;
import cmu.xprize.util.CMessageQueueFactory;
import cmu.xprize.util.GlobalStaticsEngine;
import cmu.xprize.util.IMessageQueueRunner;
import me.delandbeforeti.comp_intervention.R;

import static cmu.xprize.util.TCONST.EXIT_FROM_INTERVENTION;
import static cmu.xprize.util.TCONST.I_CANCEL_GESTURE;
import static cmu.xprize.util.TCONST.I_CANCEL_HESITATE;
import static cmu.xprize.util.TCONST.I_CANCEL_STUCK;
import static cmu.xprize.util.TCONST.I_MODAL_EXTRA;
import static cmu.xprize.util.TCONST.I_TRIGGER_FAILURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 8/5/19.
 */

public class CInterventionHelpButton extends android.support.v7.widget.AppCompatImageButton
        implements IMessageQueueRunner {


    private static final boolean CONFIG_INTERVENTION = true;
    private LocalBroadcastManager bManager;

    // tracks which intervention has been triggered
    private boolean hasBeenTriggered;
    private boolean isFlashing;
    private String currentIntervention;

    private boolean popupIsShowing;

    private CMessageQueueFactory _queue;

    private static final String FLASH_ON = "FLASH_ON";
    private static final String FLASH_OFF = "FLASH_OFF";
    private static final long FLASH_ON_TIME = 500L;
    private static final long FLASH_OFF_TIME = 500L;
    private int COLOR_ON, COLOR_OFF;

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

        bManager = LocalBroadcastManager.getInstance(context);

        // actual triggers
        IntentFilter filter = new IntentFilter();
        filter.addAction(I_TRIGGER_GESTURE);
        filter.addAction(I_TRIGGER_HESITATE);
        filter.addAction(I_TRIGGER_STUCK);
        filter.addAction(I_TRIGGER_FAILURE);

        filter.addAction(I_CANCEL_STUCK);
        filter.addAction(I_CANCEL_HESITATE);

        filter.addAction(EXIT_FROM_INTERVENTION);

        bManager.registerReceiver(new InterventionButtonMessageReceiver(), filter);

        this.setOnTouchListener(new HelpButtonTouchListener());

        _queue = new CMessageQueueFactory(this, "CIntervention");

        COLOR_ON = getResources().getColor(R.color.helpButtonHighlight);
        COLOR_OFF = getResources().getColor(R.color.helpButtonNormal);

        if (!CONFIG_INTERVENTION) {
            this.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Start the button flashing
     * @param currentIntervention
     */
    private void startFlashing(String currentIntervention) {
        Log.v("FLASH", "begin");
        this.isFlashing = true;

        int iconId;
        switch(currentIntervention) {
            case I_TRIGGER_FAILURE:
                iconId = R.drawable.handraise_icon_failure;
                break;
            case I_TRIGGER_GESTURE:
                iconId = R.drawable.handraise_icon_gesture;
                break;

            case I_TRIGGER_HESITATE:
                iconId = R.drawable.handraise_icon_hesitate;
                break;

            case I_TRIGGER_STUCK:
                iconId = R.drawable.handraise_icon_stuck;
                break;
            default:
                iconId = R.drawable.handraise_icon;
        }
        this.setImageDrawable(getResources().getDrawable(iconId));

        _queue.post(FLASH_ON, FLASH_ON_TIME);
    }

    /**
     * Set button state back to regular, and cancel any flash commands
     */
    private void cancelFlashing() {
        _queue.cancelPost(FLASH_ON);
        _queue.cancelPost(FLASH_OFF);
        isFlashing = false;
        this.setBackgroundColor(COLOR_OFF);
        this.setImageDrawable(getResources().getDrawable(R.drawable.handraise_icon));
    }

    // For dealing with delayed flashing
    @Override
    public void runCommand(String command) {

        if (!isFlashing) return;

        switch(command) {
            case FLASH_ON:
                Log.v("FLASH", "flash on");
                this.setBackgroundColor(COLOR_ON);
                _queue.post(FLASH_OFF, FLASH_OFF_TIME);
                break;

            case FLASH_OFF:
                Log.v("FLASH", "flash off");
                this.setBackgroundColor(COLOR_OFF);
                _queue.post(FLASH_ON, FLASH_ON_TIME);
                break;
        }
    }

    @Override
    public void runCommand(String command, Object target) {
        // not useds
    }

    @Override
    public void runCommand(String command, String target) {
        // not used
    }

    /**
     * Message Receiver for Interventions
     * When receiving an Intervention message:
     *      - set the current action
     *      - start flashing the Help Button
     */
    class InterventionButtonMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // for turning off intervention mode
            if (!CONFIG_INTERVENTION) {
                return;
            }

            Log.wtf("trigger", "REceived trigger: " + intent.getAction());

            String action = intent.getAction();
            boolean modal = intent.getBooleanExtra(I_MODAL_EXTRA, false);
            if (modal) return;
            if (action == null) return;

            // DO THIS THING where it's looking for the thing
            switch(action) {

                // all these are legit
                case I_TRIGGER_GESTURE:
                case I_TRIGGER_HESITATE:
                case I_TRIGGER_STUCK:
                case I_TRIGGER_FAILURE:
                    // don't start flashing if the popup is already showing // NEXT test this...
                    if (popupIsShowing) return;
                    hasBeenTriggered = true;
                    if(currentIntervention == null)
                        currentIntervention = action;

                    startFlashing(currentIntervention);
                    break;


                // JUDITH - CANCEL_INTERVENTION:
                case I_CANCEL_STUCK:
                    Log.wtf("trigger", "Cancelling stuck");
                    Log.wtf("trigger", "currentIntervention: " + currentIntervention);
                    if (currentIntervention != null && currentIntervention.equals(I_TRIGGER_STUCK)) {
                        currentIntervention = null;
                        hasBeenTriggered = false;
                        cancelFlashing();
                    }

                    break;

                case I_CANCEL_HESITATE:
                    // this is triggered when they tap
                    Log.wtf("trigger", "Cancelling hesitate");
                    Log.wtf("trigger", "currentIntervention: " + currentIntervention);
                    if (currentIntervention != null && currentIntervention.equals(I_TRIGGER_HESITATE)) {
                        currentIntervention = null;
                        hasBeenTriggered = false;
                        cancelFlashing();
                    }
                    break;

                case I_CANCEL_GESTURE:
                    // this is triggered when they do correct gesture
                    break;

                case EXIT_FROM_INTERVENTION:
                    popupIsShowing = false;
                    break;

            }

            CInterventionLogManager.getInstance().postInterventionLog(new InterventionLogItem(
                    (new Date()).getTime(),
                    CInterventionStudentData.getCurrentStudentId(),
                    null,
                    GlobalStaticsEngine.getCurrentTutorId(),
                    null,
                    action,
                    null
            ));
        }
    }

    // TODO make new option for if kid self-chooses to ask for help without a trigger
    /**
     * Touch Listener for when user touches this button.
     * Do two things:
     *      - Broadcast a message to make the Intervention popup show with appropriate message
     *      - Stop this button from flashing
     */
    class HelpButtonTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //
            if (hasBeenTriggered) {
                // pass the current intervention as the intent message
                Intent launchIntervention = new Intent(currentIntervention);
                launchIntervention.putExtra(I_MODAL_EXTRA, true);
                bManager.sendBroadcast(launchIntervention);

                popupIsShowing = true;
                hasBeenTriggered = false;
                currentIntervention = null;

                cancelFlashing();

            }

            return true;

        }
    }


}
