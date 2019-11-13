package cmu.xprize.util.consts;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-11-04.
 */
public class INTERVENTION_CONST {

    // whether to show the intervention button
    public static final boolean CONFIG_INTERVENTION = true;
    public static final boolean CONFIG_INTERVENTION_DEBUGGER = false;

    // broadcasting intervention updates
    public static final String EXTRA_TIME_EXPECT = "TIME_EXPECT";
    public static final String BROADCAST_STUCK_UPDATE = "STUCK_UPDATE";
    public static final String BROADCAST_HESITATION_UPDATE = "HESITATE_UPDATE";
    public static final String BROADCAST_GESTURE_UPDATE = "GESTURE_UPDATE";
    public static final String BROADCAST_FAILURE_UPDATE = "FAILURE_UPDATE";
    public static final String FAILS_NEEDED = "FAILS_NEEDED";
    public static final String FAILS_HAPPENED = "FAILS_HAPPENED";
}
