package cmu.xprize.util;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/20/19.
 */

public class FailureInterventionHelper {

    private String _tutorType;
    private int _dataSize;

    public FailureInterventionHelper(String _tutorType, int _dataSize) {
        this._tutorType = _tutorType;
        this._dataSize = _dataSize;
    }

    public boolean shouldTriggerIntervention(int anyWrongAttempts) {

        switch(_tutorType) {
            case "BPOP":
                return anyWrongAttempts == 9;

            case "AKIRA":
                return anyWrongAttempts == TCONST.FAILURE_COUNT_AKIRA;

            case "SPELL":
                return anyWrongAttempts == 3;


            case "WRITE":
                // FAILSON next... get where anyWrongAttempts is coming from!
                return false;

            default:


                return false;
        }
    }
}
