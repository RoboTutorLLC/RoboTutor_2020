package cmu.xprize.comp_writing;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/4/19.
 */

public class DebugTouchListener implements View.OnTouchListener {

    String TAG;
    DebugTouchListener(String tag) {
        this.TAG = tag;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.wtf(TAG, "touched!!!");
        return false;
    }
}
