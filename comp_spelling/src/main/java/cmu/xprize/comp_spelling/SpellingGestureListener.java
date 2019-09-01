package cmu.xprize.comp_spelling;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import cmu.xprize.util.IInterventionSource;
import cmu.xprize.util.TCONST;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/1/19.
 */

public class SpellingGestureListener extends GestureDetector.SimpleOnGestureListener {

    IInterventionSource iIntervention;

    public SpellingGestureListener(IInterventionSource iIntervention) {
        this.iIntervention = iIntervention;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.v("GESTURE","onDown: ");

        // don't return false here or else none of the other
        // gestures will work
        return true;
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.v("GESTURE", "onSingleTapConfirmed: ");

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.v("GESTURE", "onLongPress: ");

        // TRIGGER_BPOP - gesture - count 1
        iIntervention.triggerIntervention(TCONST.I_TRIGGER_GESTURE);

    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.v("GESTURE", "onDoubleTap: ");


        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                            float distanceX, float distanceY) {
        Log.v("GESTURE", "onScroll: ");


        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.v("GESTURE", "onFling: ");

        // TRIGGER_BPOP - gesture - count 1
        iIntervention.triggerIntervention(TCONST.I_TRIGGER_GESTURE);

        return true;
    }
}