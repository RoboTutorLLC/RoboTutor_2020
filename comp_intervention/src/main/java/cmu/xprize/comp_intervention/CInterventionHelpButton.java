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

import cmu.xprize.util.CMessageQueueFactory;
import cmu.xprize.util.IMessageQueueRunner;
import cmu.xprize.util.TCONST;
import me.delandbeforeti.comp_intervention.R;

import static cmu.xprize.util.TCONST.HIDE_INTERVENTION;
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

    CInterventionHelpButton self;
    private Context mContext;

    private LocalBroadcastManager bManager;
    private ChangeReceiver bReceiver;

    // tracks which intervention has been triggered
    private boolean hasBeenTriggered;
    private boolean isFlashing;
    private String currentIntervention;

    private boolean blinkOn;
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
        this.self = this;
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

        this.setOnTouchListener(new HelpButtonTouchListener());

        _queue = new CMessageQueueFactory(this, "CIntervention");

        COLOR_ON = getResources().getColor(R.color.helpButtonHighlight);
        COLOR_OFF = getResources().getColor(R.color.helpButtonNormal);

    }

    private void startFlashing() {
        Log.wtf("FLASH", "begin");
        this.isFlashing = true;

        _queue.post(FLASH_ON, FLASH_ON_TIME);
    }

    private void cancelFlashing() {
        _queue.cancelPost(FLASH_ON);
        _queue.cancelPost(FLASH_OFF);
        isFlashing = false;
        self.setBackgroundColor(COLOR_OFF);
    }

    // For dealing with delayed flashing
    @Override
    public void runCommand(String command) {

        if (!isFlashing) return;

        switch(command) {
            case FLASH_ON:
                Log.wtf("FLASH", "flash on");
                this.setBackgroundColor(COLOR_ON);
                _queue.post(FLASH_OFF, FLASH_OFF_TIME);
                break;

            case FLASH_OFF:
                Log.wtf("FLASH", "flash off");
                this.setBackgroundColor(COLOR_OFF);
                _queue.post(FLASH_ON, FLASH_ON_TIME);
                break;
        }
    }

    @Override
    public void runCommand(String command, Object target) {
        // do nothing
    }

    @Override
    public void runCommand(String command, String target) {
        // do nothing
    }

    class ChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


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
                    hasBeenTriggered = true;
                    currentIntervention = action;

                    // INT_POPUP make this trigger a flash
                    startFlashing();
                    break;


                // cancel
                case HIDE_INTERVENTION:
                    hasBeenTriggered = false;
                    break;

            }
        }
    }

    class HelpButtonTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (hasBeenTriggered) {
                Intent launchIntervention = new Intent(currentIntervention);
                launchIntervention.putExtra(I_MODAL_EXTRA, true);
                bManager.sendBroadcast(launchIntervention);

                hasBeenTriggered = false;

                cancelFlashing();

            }

            return true;

        }
    }


}
