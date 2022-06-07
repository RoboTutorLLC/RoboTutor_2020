package cmu.xprize.comp_intervention.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.util.consts.INTERVENTION_CONST;

import static cmu.xprize.util.consts.INTERVENTION_CONST.BROADCAST_FAILURE_UPDATE;
import static cmu.xprize.util.consts.INTERVENTION_CONST.BROADCAST_GESTURE_UPDATE;
import static cmu.xprize.util.consts.INTERVENTION_CONST.BROADCAST_HESITATION_UPDATE;
import static cmu.xprize.util.consts.INTERVENTION_CONST.BROADCAST_STUCK_UPDATE;
import static cmu.xprize.util.consts.INTERVENTION_CONST.EXTRA_TIME_EXPECT;
import static cmu.xprize.util.consts.INTERVENTION_CONST.FAILS_HAPPENED;
import static cmu.xprize.util.consts.INTERVENTION_CONST.FAILS_NEEDED;
import static cmu.xprize.util.TCONST.I_CANCEL_GESTURE;
import static cmu.xprize.util.TCONST.I_CANCEL_HESITATE;
import static cmu.xprize.util.TCONST.I_CANCEL_STUCK;
import static cmu.xprize.util.TCONST.I_TRIGGER_FAILURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;

/**
 * CInterventionDebuggerMenu
 * <p>
 * Created by kevindeland on 2019-11-04.
 */
public class CInterventionDebuggerMenu extends LinearLayout {

    private Context mContext;

    // TextViews and their Labels
    private TextView mTextHesitate, mTextStuck, mTextGesture, mTextFailure;
    private static final String LABEL_HESITATE = "HESITATE";
    private static final String LABEL_STUCK = "STUCK";
    private static final String LABEL_GESTURE = "GESTURE";
    private static final String LABEL_FAILURE = "FAILURE";

    private static final String LABEL_NULL = "NOT INITIATED";
    private static final String LABEL_TRIGGERED = "TRIGGERED";

    // mainHandler for looping
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final long cycleTime = 1000;

    // BroadcastReceiver for updates
    private LocalBroadcastManager mManager;
    private BroadcastReceiver mReceiver;

    // Variables that change
    private boolean triggeredHesitate, triggeredStuck, triggeredGesture, triggeredFailure;
    private long hesitateTimeExpected = -1;
    private long stuckTimeExpected = -1;
    private long gestureTimeExpected;

    private int failsNeeded = -1;
    private int failsHappened = 0;


    public CInterventionDebuggerMenu(Context context) {
        super(context);
        init(context);
    }

    public CInterventionDebuggerMenu(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CInterventionDebuggerMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        mManager = LocalBroadcastManager.getInstance(context);

        IntentFilter filter = new IntentFilter();

        filter.addAction(BROADCAST_STUCK_UPDATE);
        filter.addAction(BROADCAST_HESITATION_UPDATE);
        filter.addAction(BROADCAST_GESTURE_UPDATE);
        filter.addAction(BROADCAST_FAILURE_UPDATE);

        filter.addAction(I_TRIGGER_GESTURE);
        filter.addAction(I_TRIGGER_HESITATE);
        filter.addAction(I_TRIGGER_STUCK);
        filter.addAction(I_TRIGGER_FAILURE);

        filter.addAction(I_CANCEL_STUCK);
        filter.addAction(I_CANCEL_HESITATE);
        filter.addAction(I_CANCEL_GESTURE);

        /*filter.addAction(EXIT_FROM_INTERVENTION);*/


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() == null) return;

                long expectedTime;
                switch(intent.getAction()) {
                    case BROADCAST_STUCK_UPDATE:
                        expectedTime = intent.getLongExtra(EXTRA_TIME_EXPECT, -1);
                        Log.wtf("SNEEZY", String.valueOf(expectedTime));
                        if (expectedTime != -1) stuckTimeExpected = expectedTime;

                        break;

                    case BROADCAST_HESITATION_UPDATE:
                        triggeredHesitate = false;
                        expectedTime = intent.getLongExtra(EXTRA_TIME_EXPECT, -1);
                        Log.wtf("SNEEZY", String.valueOf(expectedTime));
                        if (expectedTime != -1) hesitateTimeExpected = expectedTime;

                        break;

                    case BROADCAST_GESTURE_UPDATE:
                        triggeredGesture = false;
                        expectedTime = intent.getLongExtra(EXTRA_TIME_EXPECT, -1);
                        Log.wtf("SNEEZY", String.valueOf(expectedTime));
                        if (expectedTime != -1) gestureTimeExpected = expectedTime;

                        break;

                    case BROADCAST_FAILURE_UPDATE:
                        triggeredFailure = false;
                        int numWrong = intent.getIntExtra(FAILS_HAPPENED, -1);
                        int numExpected = intent.getIntExtra(FAILS_NEEDED, -1);
                        if (numWrong != -1 && numExpected != -1) {
                            failsHappened = numWrong;
                            failsNeeded = numExpected;
                        }
                        break;


                    /*   ------         // BEGIN TRIGGERS **/
                    case I_TRIGGER_HESITATE:
                        triggerHesitateText();
                        triggeredHesitate = true;
                        hesitateTimeExpected = -1;
                        break;

                    case I_TRIGGER_STUCK:
                        triggerStuckText();
                        triggeredStuck = true;
                        stuckTimeExpected = -1;
                        break;

                    case I_TRIGGER_GESTURE:
                        triggerGestureText();
                        triggeredGesture = true;
                        gestureTimeExpected = -1;
                        break;

                    /*   -------        // BEGIN CANCELS **/
                    case I_CANCEL_HESITATE:
                        triggeredHesitate = false;
                        break;

                    case I_CANCEL_STUCK:
                        triggeredStuck = false;
                        break;

                    case I_CANCEL_GESTURE:
                        triggeredGesture = false;
                        break;
                }
            }
        };

        mManager.registerReceiver(mReceiver, filter);

        // only show ths if we're configged to do so
        if (!INTERVENTION_CONST.CONFIG_INTERVENTION_DEBUGGER) {
            setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTextHesitate = (TextView) getChildAt(0);
        mTextStuck = (TextView) getChildAt(1);
        mTextGesture = (TextView) getChildAt(2);
        mTextFailure = (TextView) getChildAt(3);

        startCycleTimer();
    }

    /**
     * Change the Hesitate Text
     * @param expectedTime
     */
    private void updateHesitateText(long currentTime, long expectedTime) {

        if (expectedTime == -1) {
            mTextHesitate.setText(String.format(Locale.getDefault(),
                    "%s:\t\t%s",
                    LABEL_HESITATE, LABEL_NULL));

            return;
        }

        long timeLeft = (expectedTime - currentTime) / 1000;
        mTextHesitate.setText(String.format(Locale.getDefault(),
                "%s:\t\t%d",
                LABEL_HESITATE, timeLeft));
    }

    /**
     * Change hesitate text to triggered
     */
    private void triggerHesitateText() {
        mTextHesitate.setText(String.format(Locale.getDefault(),
                "%s:\t\t%s",
                LABEL_HESITATE, LABEL_TRIGGERED));
    }

    /**
     * Change the Update Text
     */
    private void updateStuckText(long currentTime, long expectedTime) {
        if (expectedTime == -1) {
            mTextStuck.setText(String.format(Locale.getDefault(),
                    "%s:\t\t%s",
                    LABEL_STUCK, LABEL_NULL));

            return;
        }

        long timeLeft = (expectedTime - currentTime) / 1000;
        mTextStuck.setText(String.format(Locale.getDefault(),
                "%s:\t\t%d",
                LABEL_STUCK, timeLeft));
    }

    /**
     * Change stuck text to triggered
     */
    private void triggerStuckText() {
        mTextStuck.setText(String.format(Locale.getDefault(),
                "%s:\t\t%s",
                LABEL_STUCK, LABEL_TRIGGERED));
    }

    private void startCycleTimer() {
        mainHandler.postDelayed(new CyclicalUpdate(), cycleTime);
    }

    /**
     * Change the Gesture Text
     * @param expectedTime
     */
    private void updateGestureText(long currentTime, long expectedTime) {

        if (expectedTime == -1) {
            mTextGesture.setText(String.format(Locale.getDefault(),
                    "%s:\t\t%s",
                    LABEL_GESTURE, LABEL_NULL));

            return;
        }

        long timeLeft = (expectedTime - currentTime) / 1000;
        mTextGesture.setText(String.format(Locale.getDefault(),
                "%s:\t\t%d",
                LABEL_GESTURE, timeLeft));
    }

    /**
     * Change hesitate text to triggered
     */
    private void triggerGestureText() {
        mTextGesture.setText(String.format(Locale.getDefault(),
                "%s:\t\t%s",
                LABEL_GESTURE, LABEL_TRIGGERED));
    }

    private void updateFailureText(int failsHappened, int failsNeeded) {
        if (failsNeeded == -1) {
            mTextFailure.setText(String.format(Locale.getDefault(),
                    "%s:\t\t%s",
                    LABEL_FAILURE, LABEL_NULL));
            return;
        }

        mTextFailure.setText(String.format(Locale.getDefault(),
                "%s:\t\t%d / %d",
                LABEL_FAILURE, failsHappened, failsNeeded));
    }

    /**
     * Change hesitate text to triggered
     */
    private void triggerFailureText() {
        mTextFailure.setText(String.format(Locale.getDefault(),
                "%s:\t\t%s",
                LABEL_FAILURE, LABEL_TRIGGERED));
    }
  
    class CyclicalUpdate implements Runnable {

        @Override
        public void run() {

            long currentTime = (new Date()).getTime();
            if (!triggeredHesitate) updateHesitateText(currentTime, hesitateTimeExpected);
            if (!triggeredStuck) updateStuckText(currentTime, stuckTimeExpected);
            if (!triggeredGesture) updateGestureText(currentTime, gestureTimeExpected);

            if (!triggeredFailure) updateFailureText(failsHappened, failsNeeded);


            startCycleTimer();
        }
    }
}
