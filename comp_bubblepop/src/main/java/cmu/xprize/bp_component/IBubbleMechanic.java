
package cmu.xprize.bp_component;

import android.graphics.Canvas;

import cmu.xprize.util.CMessageQueueFactory;
import cmu.xprize.util.IInterventionSource;
import cmu.xprize.util.IMessageQueueRunner;

public interface IBubbleMechanic {

    public void onDraw(Canvas canvas);
    public boolean isInitialized();
    public void onDestroy();

    public void enableTouchEvents();

    public void populateView(CBp_Data data);
    public void doLayout(int width, int height, CBp_Data data);
    public void execCommand(String command, Object target );

    void setMessageQueue(CMessageQueueFactory queue);

}
