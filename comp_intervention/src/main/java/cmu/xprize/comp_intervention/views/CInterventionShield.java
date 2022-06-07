package cmu.xprize.comp_intervention.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.View;

import static cmu.xprize.util.TCONST.EXIT_FROM_INTERVENTION;
import static cmu.xprize.util.TCONST.I_CANCEL_GESTURE;
import static cmu.xprize.util.TCONST.I_CANCEL_HESITATE;
import static cmu.xprize.util.TCONST.I_CANCEL_STUCK;
import static cmu.xprize.util.TCONST.I_TRIGGER_FAILURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_GESTURE;
import static cmu.xprize.util.TCONST.I_TRIGGER_HESITATE;
import static cmu.xprize.util.TCONST.I_TRIGGER_STUCK;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-11-14.
 */
public class CInterventionShield extends View {

    private Context  mContext;
    private LocalBroadcastManager bManager;

    private final static float OPACITY = 0.6f;

    public CInterventionShield(Context context) {
        super(context);
        init(context);
    }

    public CInterventionShield(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CInterventionShield(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;

        initView();
        initReceiver();
    }

    /**
     * Initialize View values
     */
    private void initView() {
        this.setVisibility(INVISIBLE);
        this.setBackgroundColor(Color.BLACK);
        this.setAlpha(OPACITY);
    }

    /**
     * Initialize Receiver values
     */
    private void initReceiver() {
        bManager = LocalBroadcastManager.getInstance(mContext);

        IntentFilter filter = new IntentFilter();
        filter.addAction(I_TRIGGER_GESTURE);
        filter.addAction(I_TRIGGER_HESITATE);
        filter.addAction(I_TRIGGER_STUCK);
        filter.addAction(I_TRIGGER_FAILURE);

        filter.addAction(I_CANCEL_STUCK);
        filter.addAction(I_CANCEL_HESITATE);
        filter.addAction(I_CANCEL_GESTURE);

        filter.addAction(EXIT_FROM_INTERVENTION);

        bManager.registerReceiver(new InterventionShieldReceiver(), filter);
    }

    private void hideMe() {
        this.setVisibility(View.INVISIBLE);
    }

    private void showMe() {
        this.setVisibility(View.VISIBLE);
    }

    private class InterventionShieldReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null) return;

            switch(action) {
                case I_TRIGGER_GESTURE:
                case I_TRIGGER_HESITATE:
                case I_TRIGGER_STUCK:
                case I_TRIGGER_FAILURE:
                    showMe();
                    break;

                case I_CANCEL_HESITATE:
                case I_CANCEL_STUCK:
                case I_CANCEL_GESTURE:
                case EXIT_FROM_INTERVENTION:
                    hideMe();
                    break;
            }
        }
    }
}
