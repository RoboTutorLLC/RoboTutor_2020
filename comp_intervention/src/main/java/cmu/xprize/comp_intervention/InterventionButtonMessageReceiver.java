package cmu.xprize.comp_intervention;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

import cmu.xprize.comp_intervention.data.CInterventionStudentData;
import cmu.xprize.comp_intervention.views.CInterventionHelpButton;
import cmu.xprize.comp_logging.CInterventionLogManager;
import cmu.xprize.comp_logging.InterventionLogItem;
import cmu.xprize.util.GlobalStaticsEngine;

import static cmu.xprize.util.TCONST.EXIT_FROM_INTERVENTION;
import static cmu.xprize.util.TCONST.I_CANCEL_GESTURE;
import static cmu.xprize.util.TCONST.I_CANCEL_HESITATE;
import static cmu.xprize.util.TCONST.I_CANCEL_STUCK;
import static cmu.xprize.util.TCONST.I_MODAL_EXTRA;
import static cmu.xprize.util.TCONST.I_TRIGGER_FAILURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;

/**
 * InterventionButtonMessageReceiver
 * <p>Had to move this to a singleton class so there wouldn't be like a hundred of them</p>
 * Created by kevindeland on 2019-11-03.
 */
public class InterventionButtonMessageReceiver extends BroadcastReceiver {

    private static InterventionButtonMessageReceiver singleton;

    private InterventionButtonMessageReceiver() {

    }

    public static InterventionButtonMessageReceiver getInstance() {
        if (singleton == null) {
            singleton = new InterventionButtonMessageReceiver();
        }

        return singleton;
    }

    public static boolean CONFIG_INTERVENTION = true;

    private CInterventionHelpButton helpButton;

    public void setHelpButton(CInterventionHelpButton helpButton) {
        this.helpButton = helpButton;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // for turning off intervention mode
        if (!CONFIG_INTERVENTION) {
            return;
        }

        Log.wtf("trigger", "REceived trigger: " + intent.getAction());

        String action = intent.getAction();
        boolean modal = intent.getBooleanExtra(I_MODAL_EXTRA, false);
        if (modal) return;
        if (action == null) return;

        // DO THIS THING where it's looking for the thing
        switch(action) {

            // all these are legit
            case I_TRIGGER_GESTURE:
            case I_TRIGGER_HESITATE:
            case I_TRIGGER_STUCK:
            case I_TRIGGER_FAILURE:
                // don't start flashing if the popup is already showing // NEXT test this...
                helpButton.triggerIntervention(action);
                break;


            // JUDITH - CANCEL_INTERVENTION:
            case I_CANCEL_STUCK:
                helpButton.cancelStuck();

                break;

            case I_CANCEL_HESITATE:
                helpButton.cancelHesitate();
                break;

            case I_CANCEL_GESTURE:
                // this is triggered when they do correct gesture
                break;

            case EXIT_FROM_INTERVENTION:
                helpButton.setPopupShowing(false);
                break;

        }

        CInterventionLogManager.getInstance().postInterventionLog(new InterventionLogItem(
                (new Date()).getTime(),
                CInterventionStudentData.getCurrentStudentId(),
                null,
                GlobalStaticsEngine.getCurrentTutorId(),
                null,
                action,
                null
        ));
    }
}
