package cmu.xprize.comp_logging;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-10-25.
 */
public class CInterventionLogManager extends CLogManagerBase {

    private static String TAG = "CInterventionLogManager";

    private CInterventionLogManager() {
        super.TAG = TAG;
    }

    // Singleton
    private static CInterventionLogManager ourInstance = new CInterventionLogManager();

    public static CInterventionLogManager getInstance() {
        return ourInstance;
    }

    public void postInterventionLog(InterventionLogItem event) {
        postEvent_I(TLOG_CONST.PERFORMANCE_TAG, event.toString());
    }
}
