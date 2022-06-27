package cmu.xprize.comp_writing;

import android.view.View;

import cmu.xprize.ltkplus.CGlyphMetrics;
import cmu.xprize.ltkplus.CRecResult;

public interface IWritingComponent {

    void onCreate();

    void deleteItem(View child);
    void addItemAt(View child, int inc);
    void autoScroll(IGlyphController glyph);

    void stimulusClicked(int touchIndex);
    void onErase(int eraseIndex);
    boolean scanForPendingRecognition(IGlyphController source);
    void inhibitInput(IGlyphController source, boolean inhibit);

    boolean applyBehavior(String event);

    void updateGlyphStats(CRecResult[] ltkPlusResult, CRecResult[] ltkresult, CGlyphMetrics metricsA, CGlyphMetrics metricsB);
    boolean updateStatus(IGlyphController child, CRecResult[] _ltkPlusCandidates );
    void resetResponse(IGlyphController child );
}
