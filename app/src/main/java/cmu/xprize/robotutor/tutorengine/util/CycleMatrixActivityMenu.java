package cmu.xprize.robotutor.tutorengine.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cmu.xprize.comp_ask.CAskElement;
import cmu.xprize.comp_ask.CAsk_Data;
import cmu.xprize.comp_session.AS_CONST;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.util.CAt_Data;
import cmu.xprize.util.CPlacementTest_Tutor;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_MATH;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_0;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_1;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_OPTION_2;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_STORIES;
import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.comp_session.AS_CONST.REPEAT_DEBUG_TAG;
import static cmu.xprize.comp_session.AS_CONST.SELECT_REPEAT;
import static cmu.xprize.util.TCONST.MENU_BUG_TAG;

/**
 * CycleMatrixActivityMenu
 *
 * an Activity Menu style where the matrix cycles each time,
 * and the student chooses from one of two activities in the same matrix
 * <p>
 * Created by kevindeland on 9/25/18.
 */

public class CycleMatrixActivityMenu implements IActivityMenu {

    TransitionMatrixModel _matrix;
    IStudentDataModel _student;
    PromotionMechanism _promotionMechanism;
    private static final int MIN_NUM_ATTEMPTS = 3;

    public CycleMatrixActivityMenu(TransitionMatrixModel matrix, IStudentDataModel student) {
        this._matrix = matrix;
        this._student = student;
        this._promotionMechanism = new PromotionMechanism(this._student, this._matrix);
    }

    @Override
    public String getLayoutName() {
        if(this._promotionMechanism.performance.getNumberAttempts() >= MIN_NUM_ATTEMPTS
                &&
                this._promotionMechanism.performance.getTotalNumberQuestions() >= 3
                &&
                this._promotionMechanism.performance.getNumberCorrect() / this._promotionMechanism.performance.getNumberAttempts() > PlacementPromotionRules.HIGH_PERFORMANCE_THRESHOLD
        )
            return "ask_activity_selector_2x2_elevate";
        else
            return "ask_activity_selector_2x2";
    }

    @Override
    public CAt_Data[] getTutorsToShow() {

        CAt_Data[] nextTutors = new CAt_Data[2];
        HashMap transitionMap = _matrix.storyTransitions;
        String tutorId = "";

        CPlacementTest_Tutor[] placement = null;
        boolean isPlacementMode = false;
        int placementIndex = 0;


        // active skill dependent...
        switch(_student.getActiveSkill()) {
            case SELECT_WRITING:
                transitionMap = _matrix.writeTransitions;

                isPlacementMode = _student.getWritingPlacement();
                if (isPlacementMode) {
                    placement = _matrix.writePlacement;
                    placementIndex = _student.getWritingPlacementIndex();
                    tutorId = placement[placementIndex].tutor;
                } else {
                    tutorId = _student.getWritingTutorID();
                }
                break;

            case SELECT_STORIES:
                transitionMap = _matrix.storyTransitions;
                tutorId = _student.getStoryTutorID();
                isPlacementMode = false;
                break;

            case SELECT_MATH:
                transitionMap = _matrix.mathTransitions;

                isPlacementMode = _student.getMathPlacement();
                if (isPlacementMode) {
                    placement = _matrix.mathPlacement;
                    placementIndex = _student.getMathPlacementIndex();
                    tutorId = placement[placementIndex].tutor;
                } else {
                    tutorId = _student.getMathTutorID();
                }
                break;
        }

        // iff in placement mode... show the same tutor twice
        // SUPER_PLACEMENT if the same, do two different icons
        if (isPlacementMode && placement != null) {
            Log.e("SUPER_PLACEMENT", "not ready yet");
            nextTutors[0] = (CAt_Data) transitionMap.get(tutorId);
            nextTutors[1] = nextTutors[0];

            // Add null check for nextTutors[0]
            if (nextTutors[0] == null) {
                Log.e(MENU_BUG_TAG, "Could not find tutor with ID: " + tutorId + " in placement mode");
                // Try to get root tutor as fallback
                String rootTutor = "";
                switch(_student.getActiveSkill()) {
                    case SELECT_WRITING:
                        rootTutor = _matrix.getRootSkillByContentArea(SELECT_WRITING);
                        break;
                    case SELECT_STORIES:
                        rootTutor = _matrix.getRootSkillByContentArea(SELECT_STORIES);
                        break;
                    case SELECT_MATH:
                        rootTutor = _matrix.getRootSkillByContentArea(SELECT_MATH);
                        break;
                }
                nextTutors[0] = (CAt_Data) transitionMap.get(rootTutor);
                nextTutors[1] = nextTutors[0];
            }
        } else {
            nextTutors[0] = (CAt_Data) transitionMap.get(tutorId); // N

            // Add null checking here
            if (nextTutors[0] != null && nextTutors[0].next != null) {
                CAt_Data nextTutor = (CAt_Data) transitionMap.get(nextTutors[0].next);
                nextTutors[1] = isPlacementMode ? nextTutors[0] : nextTutor;

                // Add null check for nextTutors[1]
                if (nextTutors[1] == null) {
                    nextTutors[1] = nextTutors[0]; // Use the same tutor if next is null
                    Log.e(MENU_BUG_TAG, "Next tutor is null, using same tutor twice: " + tutorId);
                }
            } else {
                // If there's no valid next tutor, use the same tutor twice
                if (nextTutors[0] == null) {
                    Log.e(MENU_BUG_TAG, "Current tutor is null for ID: " + tutorId);
                    // Try to get root tutor as fallback
                    String rootTutor = "";
                    switch(_student.getActiveSkill()) {
                        case SELECT_WRITING:
                            rootTutor = _matrix.getRootSkillByContentArea(SELECT_WRITING);
                            break;
                        case SELECT_STORIES:
                            rootTutor = _matrix.getRootSkillByContentArea(SELECT_STORIES);
                            break;
                        case SELECT_MATH:
                            rootTutor = _matrix.getRootSkillByContentArea(SELECT_MATH);
                            break;
                    }
                    nextTutors[0] = (CAt_Data) transitionMap.get(rootTutor);
                }

                nextTutors[1] = nextTutors[0];
                Log.d(MENU_BUG_TAG, "No next tutor available, using the same tutor twice");
            }
        }

        return nextTutors;
    }

    @Override
    public CAsk_Data initializeActiveLayout() {
        CAsk_Data activeLayout = new CAsk_Data();

        activeLayout.items = new CAskElement[4];
        // both options will have the same content area, same prompt
        activeLayout.items[0] = new CAskElement();
        activeLayout.items[0].componentID = "SbuttonOption1";
        activeLayout.items[0].behavior = SELECT_OPTION_0;

        activeLayout.items[1] = new CAskElement();
        activeLayout.items[1].componentID = "SbuttonOption2";
        activeLayout.items[1].behavior = SELECT_OPTION_1;

        String prompt = null;
        switch(_student.getActiveSkill()) {
            case SELECT_WRITING:
                prompt = "reading and writing";
                break;

            case SELECT_STORIES:
                prompt = "stories";
                break;

            case SELECT_MATH:
                prompt = "numbers and math";
                break;
        }
        activeLayout.items[0].prompt = prompt;
        activeLayout.items[0].help = prompt;

        activeLayout.items[1].prompt = "something different";
        activeLayout.items[1].help = "something different";

        activeLayout.items[2] =  new CAskElement();
        if(this._promotionMechanism.performance.getNumberAttempts() >= MIN_NUM_ATTEMPTS
                &&
                this._promotionMechanism.performance.getTotalNumberQuestions() >= 3
                &&
                this._promotionMechanism.performance.getNumberCorrect() / this._promotionMechanism.performance.getNumberAttempts() > PlacementPromotionRules.HIGH_PERFORMANCE_THRESHOLD){
            activeLayout.items[2].componentID = "Sbutton1";
            activeLayout.items[2].behavior = AS_CONST.ELEVATE;
            activeLayout.items[2].prompt = "I want something harder";
            activeLayout.items[2].help = "something harder";
        }
        else{
            activeLayout.items[2].componentID = "SbuttonRepeat";
            activeLayout.items[2].behavior = AS_CONST.SELECT_REPEAT;
            activeLayout.items[2].prompt = "lets do it again";
            activeLayout.items[2].help = "lets do it again";
        }

        activeLayout.items[3] =  new CAskElement();
        activeLayout.items[3].componentID = "SbuttonExit";
        activeLayout.items[3].behavior = AS_CONST.SELECT_EXIT;
        activeLayout.items[3].prompt = "I want to stop using RoboTutor";
        activeLayout.items[3].help = "I want to stop using RoboTutor";

        return activeLayout;
    }

    @Override
    public Map<String, String> getButtonBehaviorMap() {
        Map<String, String> map = new HashMap<String, String>();

        map.put(SELECT_OPTION_0, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        map.put(SELECT_OPTION_1, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);

        if(this._promotionMechanism.performance.getNumberAttempts() >= MIN_NUM_ATTEMPTS
                &&
                this._promotionMechanism.performance.getTotalNumberQuestions() >= 3
                &&
                this._promotionMechanism.performance.getNumberCorrect() / this._promotionMechanism.performance.getNumberAttempts() > PlacementPromotionRules.HIGH_PERFORMANCE_THRESHOLD){
            map.put(AS_CONST.ELEVATE, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        }
        else{
            map.put(AS_CONST.SELECT_REPEAT, AS_CONST.QUEUEMAP_KEYS.BUTTON_BEHAVIOR);
        }

        map.put(AS_CONST.SELECT_EXIT, AS_CONST.QUEUEMAP_KEYS.EXIT_BUTTON_BEHAVIOR);
        return map;
    }

    @Override
    public CAt_Data getTutorToLaunch(String buttonBehavior) {

        //
        String zeroIndexedTutorId = ""; // the current tutor in the left-top position
        HashMap transitionMap = null;
        String rootTutor = "";

        String activeSkill = null;
        String chosenTutorId = null;

        Log.d("OH_BEHAVE", "some behavior here should be different...");

        if (buttonBehavior.equals(SELECT_REPEAT)) {
            RoboTutor.logManager.postEvent_I(REPEAT_DEBUG_TAG, "repeating behavior");
            RoboTutor.STUDENT_CHOSE_REPEAT = true;
            activeSkill = _student.getLastSkill();
            chosenTutorId = _student.getLastTutor();
            transitionMap = _matrix.getTransitionMapByContentArea(activeSkill);
            rootTutor = _matrix.getRootSkillByContentArea(activeSkill);
            RoboTutor.logManager.postEvent_I(REPEAT_DEBUG_TAG, String.format("activeSkill=%s;chosenTutorId=%s;transitionMap=%s;rootTutor=%s", activeSkill, chosenTutorId, activeSkill, rootTutor));
        }
        else if (buttonBehavior.equals(AS_CONST.ELEVATE)) {
            activeSkill = _student.getLastSkill();
            transitionMap = _matrix.getTransitionMapByContentArea(activeSkill);
            chosenTutorId = _student.getLastTutor();

            if(activeSkill != null) {
                // If the last activity is not null then go to that activity but update the placement index by 1
                if(activeSkill.equals(SELECT_MATH)) {
                    rootTutor = _matrix.getRootSkillByContentArea(SELECT_MATH);
                    CAt_Data transitionData = (CAt_Data) transitionMap.get(chosenTutorId);
                    if (transitionData != null && transitionData.harder != null) {
                        chosenTutorId = transitionData.harder;
                    } else {
                        // If no harder level available, stay at current level
                        Log.e(MENU_BUG_TAG, "No harder math level available for " + chosenTutorId);
                    }
                }
                else if(activeSkill.equals(SELECT_WRITING)) {
                    rootTutor = _matrix.getRootSkillByContentArea(SELECT_WRITING);
                    CAt_Data transitionData = (CAt_Data) transitionMap.get(chosenTutorId);
                    if (transitionData != null && transitionData.harder != null) {
                        chosenTutorId = transitionData.harder;
                    } else {
                        // If no harder level available, stay at current level
                        Log.e(MENU_BUG_TAG, "No harder writing level available for " + chosenTutorId);
                    }
                }
                else if(activeSkill.equals(SELECT_STORIES)) {
                    rootTutor = _matrix.getRootSkillByContentArea(SELECT_STORIES);
                    CAt_Data transitionData = (CAt_Data) transitionMap.get(chosenTutorId);
                    if (transitionData != null && transitionData.harder != null) {
                        chosenTutorId = transitionData.harder;
                    } else {
                        // If no harder level available for stories, stay at current level
                        Log.e(MENU_BUG_TAG, "No harder stories level available for " + chosenTutorId);
                    }
                }
            }
        } else {
            activeSkill = _student.getActiveSkill();
            CAt_Data zeroIndexedTutor;
            String[] nextTutorIds = new String[3];

            boolean inPlacementMode = false;

            switch(activeSkill) {
                case SELECT_WRITING:
                    zeroIndexedTutorId = _student.getWritingTutorID();
                    Log.d("REPEAT_ME", "writingTutor=" + zeroIndexedTutorId);
                    inPlacementMode = _student.getWritingPlacement();
                    break;

                case SELECT_STORIES:
                    zeroIndexedTutorId = _student.getStoryTutorID();
                    if (zeroIndexedTutorId == null) {
                        // If story tutor ID is null, get the root story tutor
                        zeroIndexedTutorId = _matrix.getRootSkillByContentArea(SELECT_STORIES);
                        Log.e(MENU_BUG_TAG, "Story tutor ID was null, falling back to root: " + zeroIndexedTutorId);
                    }
                    Log.d("REPEAT_ME", "storyTutor=" + zeroIndexedTutorId);
                    break;

                case SELECT_MATH:
                    zeroIndexedTutorId = _student.getMathTutorID();
                    Log.d("REPEAT_ME", "mathTutor=" + zeroIndexedTutorId);
                    inPlacementMode = _student.getMathPlacement();
                    break;
            }


            RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, "CycleMatrixActivityMenu: activeSkill=" + activeSkill + " -- activeTutorId=" + zeroIndexedTutorId);
            transitionMap = _matrix.getTransitionMapByContentArea(activeSkill);
            rootTutor = _matrix.getRootSkillByContentArea(activeSkill);

            // Additional safety for stories
            if (activeSkill != null && activeSkill.equals(SELECT_STORIES) && (transitionMap == null || transitionMap.isEmpty())) {
                Log.e(MENU_BUG_TAG, "Story transition map is null or empty!");
                // Create a minimal transition map if none exists
                if (transitionMap == null) {
                    transitionMap = new HashMap();
                }
                // Make sure the root tutor exists in the map
                if (rootTutor != null && !transitionMap.containsKey(rootTutor)) {
                    Log.e(MENU_BUG_TAG, "Adding root story tutor to map as fallback: " + rootTutor);
                    CAt_Data rootData = new CAt_Data();
                    rootData.tutor_id = rootTutor;
                    rootData.next = rootTutor; // Point to itself as fallback
                    rootData.same = rootTutor;
                    transitionMap.put(rootTutor, rootData);
                }
            }

            zeroIndexedTutor = (CAt_Data) transitionMap.get(zeroIndexedTutorId);

            if (zeroIndexedTutor != null) {
                nextTutorIds[0] = zeroIndexedTutor.tutor_id;

                if (zeroIndexedTutor.next != null) {
                    CAt_Data nextTutor = (CAt_Data) transitionMap.get(zeroIndexedTutor.next);
                    nextTutorIds[1] = inPlacementMode ? nextTutorIds[0] : (nextTutor != null ? nextTutor.tutor_id : nextTutorIds[0]);

                    // Initialize nextTutorIds[2] to prevent null when SELECT_OPTION_2 is used
                    if (nextTutor != null && nextTutor.next != null) {
                        CAt_Data nextNextTutor = (CAt_Data) transitionMap.get(nextTutor.next);
                        nextTutorIds[2] = nextNextTutor != null ? nextNextTutor.tutor_id : nextTutorIds[1];
                    } else {
                        nextTutorIds[2] = nextTutorIds[1]; // Use previous tutor if no "next next" is available
                    }
                } else {
                    nextTutorIds[1] = nextTutorIds[0];
                    nextTutorIds[2] = nextTutorIds[0]; // Initialize index 2 as well
                    Log.d(MENU_BUG_TAG, "No next tutor defined, using the same tutor twice");
                }
            } else {
                // Handle case where zeroIndexedTutor is null
                Log.e(MENU_BUG_TAG, "Could not find tutor with ID: " + zeroIndexedTutorId);
                // Use root tutor as fallback
                CAt_Data rootTutorData = (CAt_Data) transitionMap.get(rootTutor);
                if (rootTutorData != null) {
                    nextTutorIds[0] = rootTutorData.tutor_id;
                    nextTutorIds[1] = rootTutorData.tutor_id;
                } else {
                    // Critical error - no valid tutor found
                    Log.e(MENU_BUG_TAG, "Critical error: No valid tutor found in transition map");
                    // Set to null and handle later
                    nextTutorIds[0] = null;
                    nextTutorIds[1] = null;
                }
            }

            switch(buttonBehavior.toUpperCase()) {

                case SELECT_OPTION_0: // TRACE_PROMOTION looks good...
                    chosenTutorId = nextTutorIds[0];
                    break;

                case SELECT_OPTION_1: // TRACE_PROMOTION looks good...
                    // launch the next tutor
                    // something like this...
                    chosenTutorId = nextTutorIds[1];

                    break;

                case SELECT_OPTION_2:
                    // launch the next.next tutor
                    chosenTutorId = nextTutorIds[2];
                    break;

                default:
                    chosenTutorId = zeroIndexedTutorId; // TRACE_PROMOTION does this break anything? // getLastTutor?
                    break;
            }

            // If they choose the second option, update our position in thematrix //
            // will only be updated if SELECT_OPTION_1 or SELECT_OPTION_2 were selected //
            if (!chosenTutorId.equals(zeroIndexedTutorId) && !buttonBehavior.equals(AS_CONST.ELEVATE)) {
                switch (activeSkill) {
                    case SELECT_WRITING:
                        _student.updateWritingTutorID(chosenTutorId, true);
                        break;

                    case SELECT_STORIES:
                        _student.updateStoryTutorID(chosenTutorId, true);
                        break;

                    case SELECT_MATH:
                        _student.updateMathTutorID(chosenTutorId, true);
                        break;
                }
            }
        }


        // TRACE_PROMOTION doesn't save tutorToLaunch when we choose the second option!!!
        // the next tutor to be launched

        // Final check to ensure chosenTutorId is not null
        if (chosenTutorId == null) {
            Log.e(MENU_BUG_TAG, "Critical error: chosenTutorId is null, using root tutor as fallback");
            chosenTutorId = rootTutor;
        }

        CAt_Data tutorToLaunch = (CAt_Data) transitionMap.get(chosenTutorId);
        Log.wtf(MENU_BUG_TAG, chosenTutorId + " " +  activeSkill);

        // This is just to make sure we go somewhere if there is a bad link - which
        // there shuoldn't be :)
        //
        if (tutorToLaunch == null) {
            tutorToLaunch = (CAt_Data) transitionMap.get(rootTutor);
        }
        return tutorToLaunch;
    }

    @Override
    public String getDebugMenuSkill() {
        return RoboTutor.STUDENT_CHOSE_REPEAT ? _student.getLastSkill() : _student.getActiveSkill(); // DEBUG_MENU_LOGIC (x) lastSkill vs ActiveSkill...
    }
}