package cmu.xprize.comp_nd;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_nd.ui.CNd_LayoutManagerInterface;
import cmu.xprize.comp_nd.ui.CNd_LayoutManager_BaseTen;
import cmu.xprize.util.CMessageQueueFactory;
import cmu.xprize.util.FailureInterventionHelper;
import cmu.xprize.util.GlobalStaticsEngine;
import cmu.xprize.util.IInterventionSource;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IMessageQueueRunner;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TimerMaster;
import cmu.xprize.util.gesture.ExpectTapGestureListener;

import static cmu.xprize.comp_nd.ND_CONST.CANCEL_HIGHLIGHT_HUNS;
import static cmu.xprize.comp_nd.ND_CONST.CANCEL_HIGHLIGHT_ONES;
import static cmu.xprize.comp_nd.ND_CONST.CANCEL_HIGHLIGHT_TENS;
import static cmu.xprize.comp_nd.ND_CONST.CANCEL_INDICATE_CORRECT;
import static cmu.xprize.comp_nd.ND_CONST.FTR_ONE_FIRST;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_HUNS;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_NA_ONES;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_NA_TENS;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_ONES;
import static cmu.xprize.comp_nd.ND_CONST.FTR_SAY_TENS;
import static cmu.xprize.comp_nd.ND_CONST.FTR_TEN_FIRST;
import static cmu.xprize.comp_nd.ND_CONST.HESITATION_PROMPT;
import static cmu.xprize.comp_nd.ND_CONST.HIGHLIGHT_HUNS;
import static cmu.xprize.comp_nd.ND_CONST.HIGHLIGHT_ONES;
import static cmu.xprize.comp_nd.ND_CONST.HIGHLIGHT_TENS;
import static cmu.xprize.comp_nd.ND_CONST.HUN_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.INDICATE_CORRECT;
import static cmu.xprize.comp_nd.ND_CONST.INPUT_HESITATION_FEEDBACK;
import static cmu.xprize.comp_nd.ND_CONST.NO_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.ONE_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.TEN_DIGIT;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_DIGIT_COMPARE;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_DIGIT_LESS;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_DIGIT_MORE;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_HUN;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_ONE;
import static cmu.xprize.comp_nd.ND_CONST.VALUE_TEN;
import static cmu.xprize.util.MathUtil.getHunsDigit;
import static cmu.xprize.util.MathUtil.getOnesDigit;
import static cmu.xprize.util.MathUtil.getTensDigit;
import static cmu.xprize.util.TCONST.DEBUG_HESITATE;
import static cmu.xprize.util.TCONST.GESTURE_TIME_NUMCOMPARE;
import static cmu.xprize.util.TCONST.STUCK_TIME_NUMCOMPARE;

/**
 * Generated automatically w/ code written by Kevin DeLand
 */

public class CNd_Component extends RelativeLayout implements ILoadableObject,
        IInterventionSource, IMessageQueueRunner {

    // DataSource Variables
    protected   int                   _dataIndex = 0;
    protected String level;
    protected String task;
    protected String layout;
    protected int[] dataset;
    protected boolean isWorkedExample;

    // these are hard-coded for now
    protected boolean random = true; // set all to random for now...
    public int questionCount = 10;

    // gets set w/ data source variables
    protected int numDigits;


    // json loadable
    public String bootFeatures;
    public CNd_Data[] dataSource;


    // View Things
    protected Context mContext;

    // so that UI can be changed w/o changing behavior model
    protected CNd_LayoutManagerInterface _layoutManager;

    // TUTOR STATE
    protected String _correctChoice; // can be "left" or "right"

    // Needed for sending broadcasts to RoboFinger
    private LocalBroadcastManager _bManager;


    static final String TAG = "CNd_Component";

    protected CMessageQueueFactory _queue;
    TimerMaster _timer;
    private GestureDetector mDetector;

    protected FailureInterventionHelper _failson;

    class HesitationCancelListener implements OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Log.v("event.thing", "This is a touch");
                _timer.resetHesitationTimer();
            }
            return false;
        }
    }

    /**
     * holy shit how did I not know about this...
     * This will be super useful for gesture listener???
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getAxisValue(MotionEvent.AXIS_X);
        float y = event.getAxisValue(MotionEvent.AXIS_Y);

        Log.i("BAD_TOUCH: ", x + " " + y);
        mDetector.onTouchEvent(event);
        _timer.resetHesitationTimer(); // on touch, reset
        return super.dispatchTouchEvent(event);
    }


    public CNd_Component(Context context) {
        super(context);
        init(context, null);
    }
    public CNd_Component(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public CNd_Component(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

        mContext = context;

        _bManager = LocalBroadcastManager.getInstance(getContext());

        _layoutManager = new CNd_LayoutManager_BaseTen(this, context);

        _layoutManager.initialize();
        _layoutManager.resetView();

        _queue = new CMessageQueueFactory(this, "CNumCompare");

        long hesTime = getTimeForThisTutor();
        _timer = new TimerMaster(this, _queue, _bManager, "NcompareTimer",
                hesTime, STUCK_TIME_NUMCOMPARE, GESTURE_TIME_NUMCOMPARE);
        mDetector = new GestureDetector(mContext, new ExpectTapGestureListener(_timer));

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    // Override in child class
    protected long getTimeForThisTutor() {
        return 0L;
    }

    public void onDestroy() {

        _queue.terminateQueue();
    }

    /**
     * called by AG
     */
    public void next() {

        Log.wtf(TAG, "NEXT_NEXT_NEXT");

        retractFeature(ND_CONST.FTR_CORRECT);
        retractFeature(ND_CONST.FTR_WRONG);

        try {
            if (dataSource != null) {
                if (!random) {
                    updateDataSet(dataSource[_dataIndex]);
                } else {
                    int nextIndex = (new Random()).nextInt(dataSource.length);
                    updateDataSet(dataSource[nextIndex]);

                    boolean replace = true;
                    if (!replace) {
                        ArrayList<CNd_Data> newDataSource = new ArrayList<CNd_Data>();
                        for (int i=0; i < dataSource.length; i++) {
                            if (i != nextIndex) {
                                newDataSource.add(dataSource[i]);
                            }
                        }
                        dataSource = (CNd_Data[]) newDataSource.toArray();
                    }
                }

                _dataIndex++;

            }
        } catch (Exception e) {
            CErrorManager.logEvent(TAG, "Data Exhuasted: call past end of data", e, false);
        }

    }

    /**
     * Return whether data source has run out.
     * @return whether data source has run out.
     */
    public boolean dataExhausted() {
        return random ?
                _dataIndex >= questionCount :
                _dataIndex >= dataSource.length;
    }

    private void updateDataSet(CNd_Data data) {

        // first load dataset into fields
        loadDataSet(data);

        // for now, can only be left or right
        if (dataset[0] > dataset[1]) {
            _correctChoice = "left";
        } else if (dataset[0] < dataset[1]) {
            _correctChoice = "right";
        }

    }

    /**
     * Loads from current dataset into the private DataSource fields
     *
     * @param data the current element in the DataSource array.
     */
    private void loadDataSet(CNd_Data data) {
        try {
            level = data.level;
            task = data.task;
            layout = data.layout;
            dataset = data.dataset;
            isWorkedExample = _dataIndex == 0; //data.isWorkedExample; // begin with example

            // get numDigits
            numDigits = dataset[0] > dataset[1] ? String.valueOf(dataset[0]).length() : String.valueOf(dataset[1]).length();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * points at the correct digit
     */
    public void pointAtCorrectDigit() {
        // point at the left or right guy

        Log.wtf("THIS_IS_A_TEST", "pointing to " + _correctChoice);

        View digitView;
        if (_correctChoice.equals("left")) {
            digitView = findViewById(R.id.symbol_left_num);
        } else {
            digitView = findViewById(R.id.symbol_right_num);
        }

        Log.wtf("THIS_IS_A_TEST", "pointing to " + digitView);

        int[] screenCoord = new int[2];
        digitView.getLocationOnScreen(screenCoord);

        PointF targetPoint = new PointF(screenCoord[0] + digitView.getWidth()/2,
                screenCoord[1] + digitView.getHeight());

        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        _bManager.sendBroadcast(msg);

    }

    /**
     * Point at a view
     */
    public void pointAtSomething() {
        View v = findViewById(R.id.num_discrim_layout); // left ? left : right;


        int[] screenCoord = new int[2];

        PointF targetPoint = new PointF(screenCoord[0] + v.getWidth()/2,
                screenCoord[1] + v.getHeight()/2);
        Intent msg = new Intent(TCONST.POINTAT);
        msg.putExtra(TCONST.SCREENPOINT, new float[]{targetPoint.x, targetPoint.y});

        _bManager.sendBroadcast(msg);
    }

    /**
     * Called by AG: Updates the stimulus.
     */
    public void updateStimulus() {

        try {

            setVisibility(VISIBLE);

            _layoutManager.displayDigits(dataset[0], dataset[1]);
            _layoutManager.displayConcreteRepresentations(dataset[0], dataset[1]);

            _layoutManager.enableChooseNumber(true);

            publishNumberNameAudio();

            // - twenties
            // - ones

            // This controls for which digit we will say "first"
            if (numDigits == 1) {
                publishFeature(FTR_ONE_FIRST);
                retractFeature(FTR_TEN_FIRST);
            } else if (numDigits == 2) {
                retractFeature(FTR_ONE_FIRST);
                publishFeature(FTR_TEN_FIRST);
            } else if (numDigits == 3) {
                retractFeature(FTR_ONE_FIRST);
                retractFeature(FTR_TEN_FIRST);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Send features and values to the animator graph, so it knows how to say the number name audio
     */
    private void publishNumberNameAudio() {

        int correctAnswer = dataset[0] > dataset[1] ? dataset[0] : dataset[1];

        publishValue(VALUE_HUN, String.valueOf(getHunsDigit(correctAnswer) * 100));
        publishValue(VALUE_TEN, String.valueOf(getTensDigit(correctAnswer) * 10));
        publishValue(VALUE_ONE, String.valueOf(getOnesDigit(correctAnswer)));

        // determines how we know which digits to say
        boolean nzH = getHunsDigit(correctAnswer) != 0;
        boolean nzT = getTensDigit(correctAnswer) != 0;
        boolean nzO = getOnesDigit(correctAnswer) != 0;
        // play Huns digit if... (Huns n/e 0)
        // play na ten if.... (Huns n/e 0) and (Tens n/e 0)
        // play Tens digit if... (Tens n/e 0)
        // play na ones if ... (Huns n/e 0 or Tens n/e 0) and (Ones n/e 0)
        // play Ones digit if... (Ones n/e 0)
        publishOrRetractFeature(FTR_SAY_HUNS, nzH);
        publishOrRetractFeature(FTR_SAY_NA_TENS, nzH && nzT);
        publishOrRetractFeature(FTR_SAY_TENS, nzT);
        publishOrRetractFeature(FTR_SAY_NA_ONES, (nzH || nzT) && nzO);
        publishOrRetractFeature(FTR_SAY_ONES, nzO);
    }

    /**
     * if it's a worked example, do the scaffolding. Otherwise, set the hesitation feedback.
     */
    public void playWorkedExampleOrSetHesitation() {
        // begin with example
        if(isWorkedExample) {
            applyBehaviorNode(getStartingHighlightByNumDigits()); // only highlight the necessary digits!!!
        } else {
            triggerHesitationFeedback();
            _timer.resetHesitationTimer();
            _timer.resetStuckTimer();
        }
    }

    public void triggerHesitationFeedback() {
        _queue.postNamed(HESITATION_PROMPT, TCONST.APPLY_BEHAVIOR, INPUT_HESITATION_FEEDBACK, (long) ND_CONST.HESITATION_DELAY);
    }

    /**
     * What is the first QueueMap item to be called?
     * @return one of {HIGHLIGHT_ONES, HIGHLIGHT_TENS, HIGHLIGHT_HUNS}
     */
    private String getStartingHighlightByNumDigits() {
        String[] numDigitsToBehavior = {null, HIGHLIGHT_ONES, HIGHLIGHT_TENS, HIGHLIGHT_HUNS};

        return numDigitsToBehavior[numDigits];
    }


    // (5) prevent user
    // deprecated -- don't lock user input anymore
    public void lockUserInput() {
        _layoutManager.enableChooseNumber(false);
    }

    public void enableUserInput() {
        _layoutManager.enableChooseNumber(true);
    }


    private String _currentHighlightDigit = null;

    /**
     * highlight digit and concretes in huns column
     */
    public void highlightHunsColumn() {
        Log.wtf("THIS_IS_A_TEST", "highlight huns at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(HUN_DIGIT);
        _currentHighlightDigit = HUN_DIGIT;
        publishDigitAudioValues(HUN_DIGIT);
    }


    /**
     * highlight digit and concretes in tens column
     */
    public void highlightTensColumn() {
        Log.wtf("THIS_IS_A_TEST", "highlight tens at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(TEN_DIGIT);
        _currentHighlightDigit = TEN_DIGIT;
        publishDigitAudioValues(TEN_DIGIT);
    }

    /**
     * highlight digit and concretes in ones column
     */
    public void highlightOnesColumn() {
        Log.wtf("THIS_IS_A_TEST", "highlight ones at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(ONE_DIGIT);
        _currentHighlightDigit = ONE_DIGIT;
        publishDigitAudioValues(ONE_DIGIT);
    }

    private void publishDigitAudioValues(String digit) {

        int digitLeft = 0, digitRight = 0;
        switch(digit) {
            case HUN_DIGIT:
                digitLeft = getHunsDigit(dataset[0]);
                digitRight = getHunsDigit(dataset[1]);
                break;

            case TEN_DIGIT:
                digitLeft = getTensDigit(dataset[0]);
                digitRight = getTensDigit(dataset[1]);
                break;

            case ONE_DIGIT:
                digitLeft = getOnesDigit(dataset[0]);
                digitRight = getOnesDigit(dataset[1]);
                break;
        }

        publishValue(VALUE_DIGIT_MORE,
                String.valueOf(digitLeft > digitRight ?
                        digitLeft : digitRight));

        publishValue(VALUE_DIGIT_COMPARE, digitLeft == digitRight ?
                //"is equal to" : "is greater than");
                "is the same as" : "is bigger than"); // temporary placeholder

        publishValue(VALUE_DIGIT_LESS,
                String.valueOf(digitLeft > digitRight ?
                        digitRight: digitLeft));
    }

    /**
     * decide whether to highlight next digit, or indicate the correct answer
     */
    public void highlightNextScaffoldDigit() {

        if (_currentHighlightDigit == null) return;

        _queue.cancelPost(HESITATION_PROMPT); // prevent hesitation prompt

        switch(_currentHighlightDigit) {
            case HUN_DIGIT:
                if (getHunsDigit(dataset[0]) == getHunsDigit(dataset[1])) {
                    applyBehaviorNode(HIGHLIGHT_TENS);
                } else {
                    applyBehaviorNode(INDICATE_CORRECT);
                }
                break;

            case TEN_DIGIT:
                if (getTensDigit(dataset[0]) == getTensDigit(dataset[1])) {
                    applyBehaviorNode(HIGHLIGHT_ONES);
                } else {
                    applyBehaviorNode(INDICATE_CORRECT);
                }
                break;

            case ONE_DIGIT:
                // they are equal... this functionality does not yet exist
                if (getHunsDigit(dataset[0]) == getHunsDigit(dataset[1])) {
                    applyBehaviorNode(INDICATE_CORRECT);
                } else {
                    applyBehaviorNode(INDICATE_CORRECT);
                }
                break;
        }

    }

    public void clearHighlight() {
        Log.wtf("THIS_IS_A_TEST", "clear highlight at " + System.currentTimeMillis());
        _layoutManager.highlightDigit(NO_DIGIT);
    }



    /**
     * Called by LayoutManager
     * Can be left or right
     *
     * @param studentChoice can be "left" or "right".
     */
    public void registerStudentChoice(String studentChoice) {

        Log.d(TAG, String.format(Locale.US, "The student chose the number on the %s. The correct answer is on the %s", studentChoice, _correctChoice));

        if (studentChoice.equals(_correctChoice)) {

            Log.d(TAG, "CORRECT!");
            publishFeature(ND_CONST.FTR_CORRECT);
            trackPerformance(true, studentChoice);
        } else {

            Log.d(TAG, "WRONG!");
            publishFeature(ND_CONST.FTR_WRONG);
            trackPerformance(false, studentChoice);
        }

    }

    /**
     * Called by AG
     */
    public void doTheWrongThing() {
        Log.d(TAG, "Doing the wrong thing");


        // Cancel all the scaffolding queue Maps.
        applyBehaviorNode(CANCEL_HIGHLIGHT_HUNS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_TENS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_ONES);
        applyBehaviorNode(CANCEL_INDICATE_CORRECT);

        // Reset the highlight.
        _layoutManager.highlightDigit(NO_DIGIT);

        // Prevent hesitation prompt.
        _queue.cancelPost(HESITATION_PROMPT);

        // Restart the scaffolding process.
        applyBehaviorNode(getStartingHighlightByNumDigits());

        // Continue to next part of nodeMap.
        applyBehaviorNode(ND_CONST.NEXTNODE);


    }

    /**
     * Called by AG
     */
    public void doTheRightThing() {
        Log.d(TAG, "Doing the right thing");

        // Cancel all the scaffolding queue Maps.
        applyBehaviorNode(CANCEL_HIGHLIGHT_HUNS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_TENS);
        applyBehaviorNode(CANCEL_HIGHLIGHT_ONES);
        applyBehaviorNode(CANCEL_INDICATE_CORRECT);

        // Prevent hesitation prompt.
        _queue.cancelPost(HESITATION_PROMPT); // prevent hesitation prompt

        // Reset the highlight.
        _layoutManager.highlightDigit(NO_DIGIT);

        // Continue to next part of nodeMap.
        applyBehaviorNode(ND_CONST.NEXTNODE);
    }

    // Must override in TClass
   protected void trackPerformance(boolean isCorrect, String choice) {}


    // Must override in TClass
    // TClass domain where TScope lives providing access to tutor scriptables
    //
    public boolean applyBehavior(String event){ return false;}

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void applyBehaviorNode(String event) { }

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void publishFeature(String feature) { }

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void retractFeature(String feature) {}

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void publishOrRetractFeature(String feature, boolean p) {}

    // Overridden in TClass.
    // TODO fix this freakin' architecture...
    public void publishValue(String varName, String value) {
    }

    /**
     * Load the data source
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope scope) {

        JSON_Helper.parseSelf(jsonData, this, CClassMap.classMap, scope);
        _dataIndex = 0;
    }

    @Override
    public void triggerIntervention(String type) {
        GlobalStaticsEngine.setCurrentTutorType("NUMCOMPARE");
        Intent msg = new Intent(type);
        _bManager.sendBroadcast(msg);
    }

    @Override
    public void runCommand(String command) {
        runCommand(command, (String) null);
    }

    @Override
    public void runCommand(String command, Object target) {
        // not used
    }

    @Override
    public void runCommand(String _command, String _target) {
        Log.d("runCommand", _command + " : " + _target);
        switch (_command) {

            case TCONST.I_TRIGGER_STUCK:
                triggerIntervention(TCONST.I_TRIGGER_STUCK);
                break;

            case TCONST.I_TRIGGER_HESITATE:
                triggerIntervention(TCONST.I_TRIGGER_HESITATE);
                break;

            case TCONST.APPLY_BEHAVIOR:

                Log.d(DEBUG_HESITATE, "applybehavior: " + _target);

                applyBehaviorNode(_target);
                break;

            default:
                break;
        }
    }
}
