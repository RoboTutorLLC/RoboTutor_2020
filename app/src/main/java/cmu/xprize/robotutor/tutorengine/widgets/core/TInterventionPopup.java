package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import android.util.AttributeSet;

import cmu.xprize.comp_intervention.views.CInterventionPopup;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 6/10/19.
 */

public class TInterventionPopup extends CInterventionPopup implements ITutorObject  {
    public TInterventionPopup(Context context) {
        super(context);
    }

    public TInterventionPopup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TInterventionPopup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // JUDITH load thing

    @Override
    public void init(Context context, AttributeSet attrs) {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void setVisibility(String visible) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void setParent(ITutorSceneImpl mParent) {

    }

    @Override
    public void setTutor(CTutor tutor) {

    }

    @Override
    public void setNavigator(ITutorGraph navigator) {

    }

    @Override
    public void setLogManager(ILogManager logManager) {

    }
}
