package cmu.xprize.robotutor.tutorengine;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/13/19.
 */

public class QuickDebugInterventionTutor extends QuickDebugTutor {

    private String gesture;
    private String stuck;

    private String hesitate;
    private String failure;

    public QuickDebugInterventionTutor (String tutorVariant, String tutorId, String tutorFile) {
        super(tutorVariant, tutorId, tutorFile);
    }

    public String getGesture() {
        return gesture;
    }

    public void setGesture(String gesture) {
        this.gesture = gesture;
    }

    public String getStuck() {
        return stuck;
    }

    public void setStuck(String stuck) {
        this.stuck = stuck;
    }

    public String getHesitate() {
        return hesitate;
    }

    public void setHesitate(String hesitate) {
        this.hesitate = hesitate;
    }

    public String getFailure() {
        return failure;
    }

    public void setFailure(String failure) {
        this.failure = failure;
    }
}
