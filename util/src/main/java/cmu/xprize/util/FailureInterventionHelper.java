package cmu.xprize.util;

import android.util.Log;

/**
 * FailureInterventionHelper
 *
 * How to add FailureInterventionHelper to a tutor
 * 1. declare a `protected` Helper in a CComponent for a Tutor
 * 2. construct the Helper in the `init` function, passing the proper Tutor enum and dataSource size
 * 3. look in `trackAndLogPerformance`, and call `shouldTriggerIntervention` when the student gets a question incorrect
 *
 * Created by kevindeland on 9/20/19.
 */

public class FailureInterventionHelper {

    public enum Tutor {BPOP, AKIRA, SPELL, WRITE, PICMATCH, NUMCOMPARE}

    private Tutor _tutorType;
    private int _dataSize;

    public FailureInterventionHelper(Tutor _tutorType, int _dataSize) {
        this._tutorType = _tutorType;
        this._dataSize = _dataSize;
    }

    /**
     * Returns whether a failure intervention should be triggered, based on the number of wrong
     * attempts that have been committed.
     * FAILSON Note that this should be changed to be dependent on the passing logic for each tutor,
     * FAILSON and the total number of problems
     *
     * @param anyWrongAttempts how many the student has gotten wrong in total
     * @return TRUE if an intervention should be triggered
     */
    public boolean shouldTriggerIntervention(int anyWrongAttempts) {

        switch(_tutorType) {
            case BPOP:
                return anyWrongAttempts == 9;

            case AKIRA:
                return anyWrongAttempts == TCONST.FAILURE_COUNT_AKIRA;

            case SPELL:
                return anyWrongAttempts == 3;

            case WRITE:
                return anyWrongAttempts == 9;

            case PICMATCH:
                return anyWrongAttempts == 4;

            case NUMCOMPARE:
                return anyWrongAttempts == 4;

            default:
                Log.wtf("FailureInterventionHelper", "_tutorType not found: " + _tutorType);
                return false;
        }
    }
}
