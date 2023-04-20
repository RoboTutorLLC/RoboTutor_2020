package cmu.xprize.robotutor.tutorengine.util;

import android.util.Log;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.util.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.util.CPlacementTest_Tutor;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.util.TCONST.LANG_SW;
import static cmu.xprize.util.TCONST.PLACEMENT_TAG;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-10-04.
 */
public abstract class AbstractStudentDataModel implements IStudentDataModel{

    protected final static String HAS_PLAYED_KEY = "HAS_PLAYED";
    protected final static String MATH_PLACEMENT_KEY = "MATH_PLACEMENT";
    protected final static String MATH_PLACEMENT_INDEX_KEY = "MATH_PLACEMENT_INDEX";
    protected final static String WRITING_PLACEMENT_KEY = "WRITING_PLACEMENT";
    protected final static String WRITING_PLACEMENT_INDEX_KEY = "WRITING_PLACEMENT_INDEX";

    // these match TCONST
    protected static final String CURRENT_WRITING_TUTOR_KEY   = "letters";
    protected static final String CURRENT_STORIES_TUTOR_KEY    = "stories";
    protected static final String CURRENT_MATH_TUTOR_KEY       = "numbers";

    protected static final String LAST_TUTOR_PLAYED_KEY = "LAST_TUTOR_PLAYED";

    protected static final boolean CYCLE_MATRIX = true;
    protected static final String SKILL_SELECTED_KEY = "SKILL_SELECTED";

    protected static final String[] SKILL_CYCLE = new String[4];

    protected static int SKILL_INDEX = 0;

    static {
        SKILL_CYCLE[0] = SELECT_WRITING;
        SKILL_CYCLE[1] = SELECT_MATH;
        SKILL_CYCLE[2] = SELECT_STORIES;
        SKILL_CYCLE[3] = SELECT_MATH;
    }

    /**
     * This sets the tutor IDs
     * @param matrix
     */
    @Override
    public void initializeTutorPositions(TransitionMatrixModel matrix) {

        // initialize math placement
        boolean useMathPlacement = getMathPlacement() && CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY);

        RoboTutor.logManager.postEvent_V(PLACEMENT_TAG, String.format("useMathPlacement = %s", useMathPlacement));
        if(useMathPlacement) {
            int mathPlacementIndex = getMathPlacementIndex();
            CPlacementTest_Tutor mathPlacementTutor = matrix.mathPlacement[mathPlacementIndex];
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("mathPlacementIndex = %d", mathPlacementIndex));
            String mathTutorID = mathPlacementTutor.tutor; // does this need to happen every time???
            updateMathTutorID(mathTutorID, false);
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("mathTutorID = %s", mathTutorID));
        } else {
            updateMathTutorID(matrix.rootSkillMath, false);
        }

        // initialize writing placement
        boolean useWritingPlacement = getWritingPlacement() && CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY);
        RoboTutor.logManager.postEvent_V(PLACEMENT_TAG, String.format("useWritingPlacement = %s", useWritingPlacement));
        if (useWritingPlacement) {
            int writingPlacementIndex = getWritingPlacementIndex();
            CPlacementTest_Tutor writePlacementTutor = matrix.writePlacement[writingPlacementIndex];
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writePlacementIndex = %d", writingPlacementIndex));
            String writingTutorID = writePlacementTutor.tutor;
            updateWritingTutorID(writingTutorID, false); // MENU_LOGIC (XXX) why is updateWritingTutorID("story.hear::story_1")
            RoboTutor.logManager.postEvent_I(PLACEMENT_TAG, String.format("writingTutorID = %s", writingTutorID));
        } else {
            updateWritingTutorID(matrix.rootSkillWrite, false); // MENU_LOGIC (XXX) why is updateWritingTutorID("story.hear::story_1")
        }

        // stories doesn't have placement testing, so initialize at root
        if (getStoryTutorID() == null) {
            updateStoryTutorID(matrix.rootSkillStories, false);
        }

        saveAll();
    }

    /**
     * This is needed to perform a repeat.
     *
     * This should behave differently for each Menu
     *
     * @return
     */
    @Override
    public String getLastSkill() {


        switch(CTutorEngine.menuType) {
            case CYCLE_CONTENT:
                int index = SKILL_INDEX == 0 ? SKILL_CYCLE.length -1 : SKILL_INDEX - 1;

                Log.d("REPEAT_ME", "SKILL_INDEX=" + SKILL_INDEX + ", new_index=" + index); // MENU_LOGIC "0", "3"
                return SKILL_CYCLE[index];

            case STUDENT_CHOICE:

                return getActiveSkill();
        }

        return null;

    }

    /**
     * move on to the next skill in cycle
     * @return
     */
    @Override
    public void incrementActiveSkill(boolean save) {
        SKILL_INDEX = (SKILL_INDEX + 1) % SKILL_CYCLE.length; // 0, 1, 2, 3, 0...
        updateActiveSkill(SKILL_CYCLE[SKILL_INDEX], save);

    }

    protected String getTimesPlayedKey(String tutor) {
        return tutor + "_TIMES_PLAYED";
    }
}
