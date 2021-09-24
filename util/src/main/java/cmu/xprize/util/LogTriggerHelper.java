package cmu.xprize.util;

import android.view.MotionEvent;

import cmu.xprize.comp_logging.CLogManager;

/**
 * <p>For logging triggers a.k.a Gesture, Hesitation & Stuck</p>
 * <p>
 * Created by Vishnu<t.v.s10123@gmail.com> on 29/5/2021
 */
public class LogTriggerHelper {

    public static void logGestureEvent(String action, MotionEvent motionEvent) {

        logEvent(
                "GESTURE_" + action,
                "MotionEvent Action" + ":" + motionEvent.getAction() + "," + //@JackMostow: Should we keep this? It's always 0!
                        "MotionEvent X Coordinate" + ":" + motionEvent.getX() + "," +
                        "MotionEvent Y Coordinate" + ":" + motionEvent.getY() + "," +
                        "MotionEvent EventTime" + ":" + motionEvent.getEventTime() + "," +
                        "MotionEvent DownTime" + ":" + motionEvent.getDownTime()
        );

    }

    public static void logFlingGesture(String action, float velocityX, float velocityY) {

        logEvent(
                "GESTURE_" + action,
                "Velocity X (px/s)" + ":" + velocityX + "," +
                        "Velocity Y (px/s)" + ":" + velocityY
        );

        /*

        @JackMostow:

        */

    }

    public static void logActionEvent(String action, String currentTutorId, String currentStudentId) {

        currentTutorId = currentTutorId.replace(":", "-"); // Doing this hack since colons contain special meanings in logs!

        logEvent(
                action,
                "CURRENT_TUTOR" + ":" + currentTutorId + "," +
                        "CURRENT_STUDENT" + ":" + currentStudentId
        );

    }

    private static void logEvent(String action, String log) {

//        Uncomment this to log these to LogCat too :)
        android.util.Log.d("LogTriggerHelperTest",
                "logEvent() called with: " +
                        "action = [" + action + "], " +
                        "log = [" + log + "]"
        );

        CLogManager.getInstance().postEvent_T(action, log);
    }
}