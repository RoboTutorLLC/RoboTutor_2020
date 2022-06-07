package cmu.xprize.util;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Date;

import static cmu.xprize.util.consts.INTERVENTION_CONST.BROADCAST_GESTURE_UPDATE;
import static cmu.xprize.util.consts.INTERVENTION_CONST.BROADCAST_HESITATION_UPDATE;
import static cmu.xprize.util.consts.INTERVENTION_CONST.BROADCAST_STUCK_UPDATE;
import static cmu.xprize.util.consts.INTERVENTION_CONST.EXTRA_TIME_EXPECT;
import static cmu.xprize.util.TCONST.I_CANCEL_GESTURE;
import static cmu.xprize.util.TCONST.I_CANCEL_HESITATE;
import static cmu.xprize.util.TCONST.I_CANCEL_STUCK;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;

/**
 * TimerMaster
 * <p>Use this class to manage timers for Interventions</p>
 * Created by kevindeland on 9/4/19.
 */

public class TimerMaster {

    private IInterventionSource _intervention;
    private CMessageQueueFactory _queue;
    private LocalBroadcastManager _manager;
    private long _hesitateDelay, _stuckDelay, _gestureDelay;

    private static final String HESITATION_TIMER_RUNNABLE = "HESITATION_TIMER";
    private static final String STUCK_TIMER_RUNNABLE = "STUCK_TIMER";
    private static final String GESTURE_TIMER_RUNNABLE = "GESTURE_TIMER";

    // need a boolean so we only trigger this once
    private boolean gestureTriggered;

    private String _TAG;


    /**
     * Constructor
     * @param intervention has a "triggerIntervention" command
     * @param queue responsible for posting delayed commands to the Queue. Has an IMessageQueueRunner
     *              associated with it that will know what to do when the timer has expired.
     * @param hesitateDelay delay time to trigger HESITATE
     * @param stuckDelay delay time to trigger STUCK
     * @param gestureDelay delay time to trigger GESTURE
     */
    public TimerMaster(IInterventionSource intervention, CMessageQueueFactory queue,
                       LocalBroadcastManager manager, String TAG,
                       long hesitateDelay, long stuckDelay, long gestureDelay) {

        this._intervention = intervention;
        this._queue = queue;
        this._manager = manager;
        this._hesitateDelay = hesitateDelay;
        this._stuckDelay = stuckDelay;
        this._gestureDelay = gestureDelay;
        this._TAG = TAG;
    }


    /**
     * Stuck Timer only reset when a new problem begins
     */
    public void resetStuckTimer() {
        cancelStuckTimer();
        triggerStuckTimer();
    }

    private void cancelStuckTimer() {
        Log.v(_TAG, "cancel stuck timer");
        _queue.cancelPost(STUCK_TIMER_RUNNABLE);
        _intervention.triggerIntervention(I_CANCEL_STUCK);
    }

    private void triggerStuckTimer() {
        Log.v(_TAG, String.format("trigger stuck timer: %s, %s, %d",
                STUCK_TIMER_RUNNABLE, I_TRIGGER_STUCK, _stuckDelay));
        _queue.postNamed(STUCK_TIMER_RUNNABLE, I_TRIGGER_STUCK, _stuckDelay);

        long expectedTrigger = (new Date()).getTime() + _stuckDelay;

        Intent stuckIntent = new Intent(BROADCAST_STUCK_UPDATE);

        stuckIntent.putExtra(EXTRA_TIME_EXPECT, expectedTrigger);
        _manager.sendBroadcast(stuckIntent);
    }

    /**
     * This should be called whenever ANY View is touched... without overriding existing functions.
     */
    public void resetHesitationTimer() {
        cancelHesitationTimer();
        triggerHesitationTimer();
    }

    private void cancelHesitationTimer() {
        Log.v(_TAG, "cancel hesitation timer");
        _queue.cancelPost(HESITATION_TIMER_RUNNABLE);
        _intervention.triggerIntervention(I_CANCEL_HESITATE);
    }

    private void triggerHesitationTimer() {
        Log.v(_TAG, String.format("trigger hesitation timer: %s, %s, %d",
                HESITATION_TIMER_RUNNABLE, I_TRIGGER_HESITATE, _hesitateDelay));
        _queue.postNamed(HESITATION_TIMER_RUNNABLE, I_TRIGGER_HESITATE, _hesitateDelay);

        long expectedTrigger = (new Date()).getTime() + _hesitateDelay;

        Intent hesitateIntent = new Intent(BROADCAST_HESITATION_UPDATE);

        hesitateIntent.putExtra(EXTRA_TIME_EXPECT, expectedTrigger);
        _manager.sendBroadcast(hesitateIntent);
    }

    /**
     * This should be called when a normal gesture is made
     */
    public void resetGestureTimer() {
        _queue.cancelPost(GESTURE_TIMER_RUNNABLE);
        _intervention.triggerIntervention(I_CANCEL_GESTURE);

        // note: must remove all gesture timers
        _queue.postNamed(GESTURE_TIMER_RUNNABLE, I_TRIGGER_GESTURE, _gestureDelay);
        gestureTriggered = true;
    }

    /**
     * trigger gesture timer
     */
    public void triggerGestureTimer() {
        if (gestureTriggered) return; // only trigger once

        _queue.postNamed(GESTURE_TIMER_RUNNABLE, I_TRIGGER_GESTURE, _gestureDelay);
        gestureTriggered = true;

        Intent gestureIntent = new Intent(BROADCAST_GESTURE_UPDATE);
        long expectedTrigger = (new Date()).getTime() + _gestureDelay;
        gestureIntent.putExtra(EXTRA_TIME_EXPECT, expectedTrigger);
        _manager.sendBroadcast(gestureIntent);
    }

    /**
     * cancel gesture timer
     */
    public void cancelGestureTimer() {
        if (!gestureTriggered) return; // don't need to cancel if not triggered

        _queue.cancelPost(GESTURE_TIMER_RUNNABLE);
        _intervention.triggerIntervention(I_CANCEL_GESTURE);
        gestureTriggered = false;
    }
}
