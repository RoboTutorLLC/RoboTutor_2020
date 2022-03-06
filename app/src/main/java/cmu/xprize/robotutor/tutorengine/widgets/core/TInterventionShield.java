package cmu.xprize.robotutor.tutorengine.widgets.core;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import cmu.xprize.comp_intervention.views.CInterventionShield;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.tutorengine.CTutor;
import cmu.xprize.robotutor.tutorengine.ITutorGraph;
import cmu.xprize.robotutor.tutorengine.ITutorObject;
import cmu.xprize.robotutor.tutorengine.ITutorSceneImpl;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-11-14.
 */
public class TInterventionShield extends CInterventionShield implements ITutorObject {
    public TInterventionShield(Context context) {
        super(context);
    }

    public TInterventionShield(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TInterventionShield(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /* This is bad, freakin' ITutorObject */
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
