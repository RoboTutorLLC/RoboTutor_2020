package cmu.xprize.util.gesture;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import cmu.xprize.util.LogTriggerHelper;
import cmu.xprize.util.TimerMaster;

/**
 * ExpectTapGestureListener
 * <p>Expects a tap </p>
 * Created by kevindeland on 9/1/19.
 */

public class ExpectTapGestureListener extends GestureDetector.SimpleOnGestureListener {

    private TimerMaster iTimer;
    private String TAG = "TAP_GESTURE";

    /**
     * Intervention should not be triggered immediately, so the listener should trigger the
     * gesture timer within the TimerMaster.
     *
     * @param timer a TimerMaster that triggers and resets the gesture timer
     */
    public ExpectTapGestureListener(TimerMaster timer) {
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
        iTimer.cancelGestureTimer();

        /*LogTriggerHelper.logScrollEvent(
                "SCREEN_TAP",
                e
        );*/

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i(TAG, "onLongPress: ");
        iTimer.triggerGestureTimer();

        LogTriggerHelper.logGestureEvent(
                "SCREEN_LONG_PRESS",
                e
        );

    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i(TAG, "onDoubleTap: ");
        iTimer.triggerGestureTimer();

        LogTriggerHelper.logGestureEvent(
                "SCREEN_DOUBLE_TAP",
                e
        );

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        Log.i(TAG, "onScroll: ");

        /*LogTriggerHelper.logScrollEvent(
                "SCREEN_SCROLL",
                distanceX,
                distanceY
        );*/

        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(TAG, "onFling: ");
        iTimer.triggerGestureTimer();

        LogTriggerHelper.logFlingGesture(
                "SCREEN_FLING",
                velocityX,
                velocityY
        );

        return true;
    }
}