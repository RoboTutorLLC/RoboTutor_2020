package cmu.xprize.util.gesture;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import cmu.xprize.util.IInterventionSource;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TimerMaster;

/**
 * ExpectWriteGestureListener
 * <p>Expects a write (aka fling/scroll)</p>
 * Created by kevindeland on 9/1/19.
 */

public class ExpectWriteGestureListener extends GestureDetector.SimpleOnGestureListener {

    private TimerMaster iTimer;
    private String TAG = "WRITE_GESTURE";

    /**
     * Intervention should not be triggered immediately, so the listener should trigger the
     * gesture timer within the TimerMaster.
     *
     * @param timer a TimerMaster that triggers and resets the gesture timer
     */
    public ExpectWriteGestureListener(TimerMaster timer) {
        this.iTimer = timer;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(TAG,"onDown: ");

        // don't return false here or else none of the other
        // gestures will work
        return true;
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.i(TAG, "onSingleTapConfirmed: ");
        iTimer.triggerGestureTimer();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i(TAG, "onLongPress: ");
        iTimer.triggerGestureTimer();
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i(TAG, "onDoubleTap: ");
        iTimer.triggerGestureTimer();

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        Log.i(TAG, "onScroll: ");


        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(TAG, "onFling: ");
        iTimer.resetGestureTimer();

        return true;
    }
}
