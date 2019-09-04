package cmu.xprize.util.gesture;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import cmu.xprize.util.IInterventionSource;
import cmu.xprize.util.TCONST;

/**
 * ExpectWriteGestureListener
 * <p>Expects a write (aka fling/scroll)</p>
 * Created by kevindeland on 9/1/19.
 */

public class ExpectWriteGestureListener extends GestureDetector.SimpleOnGestureListener {

    private IInterventionSource iIntervention;
    private String TAG = "GESTURE";

    public ExpectWriteGestureListener(IInterventionSource intervention) {
        this.iIntervention = intervention;
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
        iIntervention.triggerIntervention(TCONST.I_TRIGGER_GESTURE);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i(TAG, "onLongPress: ");
        iIntervention.triggerIntervention(TCONST.I_TRIGGER_GESTURE);

    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i(TAG, "onDoubleTap: ");
        iIntervention.triggerIntervention(TCONST.I_TRIGGER_GESTURE);

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
        // iIntervention.triggerIntervention(TCONST.I_TRIGGER_GESTURE);

        return true;
    }
}
