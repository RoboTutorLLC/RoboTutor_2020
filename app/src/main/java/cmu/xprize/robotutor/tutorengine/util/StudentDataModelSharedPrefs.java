package cmu.xprize.robotutor.tutorengine.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.util.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.util.TCONST.LANG_SW;
import static cmu.xprize.util.TCONST.MENU_BUG_TAG;

/**
 * RoboTutor
 * <p>
 *     Uses SharedPrefs to store StudentDataModel
 * Created by kevindeland on 9/20/18.
 * </p>
 * this *may* be too slow... change to a JSON object, and store all at once
 * see https://stackoverflow.com/questions/7145606/how-android-sharedpreferences-save-store-object
 */

public class StudentDataModelSharedPrefs extends AbstractStudentDataModel implements IStudentDataModel {

    private static final String TAG = "StudentDataModel";

    private static SharedPreferences _preferences;
    private static SharedPreferences.Editor _editor;

    private String _studentId;
    private boolean _editorOpen;

    /**
     * Constructor
     * @param context needed to call getSharedPreferences
     * @param prefsID the ID of the student
     */
    public StudentDataModelSharedPrefs(Context context, String prefsID) {
        _studentId = prefsID;
        _preferences = context.getSharedPreferences(prefsID, Context.MODE_PRIVATE);
    }

    @Override
    public String getStudentId() {
        return _studentId;
    }

    /**
     * Initializes the student with beginning values
     */
    @Override
    public void createNewStudent() {
        _editor = _preferences.edit();
        _editor.putString(HAS_PLAYED_KEY, String.valueOf(true));

        // writing: Placement = true. Placement Index starts at 0
        _editor.putBoolean(WRITING_PLACEMENT_KEY, CTutorEngine.language.equals(LANG_SW) && Configuration.usePlacement(RoboTutor.ACTIVITY));
        _editor.putInt(WRITING_PLACEMENT_INDEX_KEY, 0);

        // math: Placement = true. Placement Index starts at 0
        _editor.putBoolean(MATH_PLACEMENT_KEY, CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY));
        _editor.putInt(MATH_PLACEMENT_INDEX_KEY, 0);

        if(CYCLE_MATRIX) {
            _editor.putString(SKILL_SELECTED_KEY, SELECT_WRITING);
        }

        _editor.apply();
    }

    /**
     * Whether the student has played RoboTutor before.
     * If not, will be updated
     *
     * @return "true" or null.
     */
    @Override
    public String getHasPlayed() {
        return _preferences.getString(HAS_PLAYED_KEY, null);
    }

    @Override
    public String getWritingTutorID() {
        return _preferences.getString(CURRENT_WRITING_TUTOR_KEY, null);
    }

    @Override
    public void updateWritingTutorID(String id, boolean save) {
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        String Method2 = Thread.currentThread().getStackTrace()[3].getMethodName();
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, Method2 + " --> " + Method + "(" + id + ")");

        openEditor();
        _editor.putString(CURRENT_WRITING_TUTOR_KEY, id);
        applyEditor(save);
    }

    @Override
    public String getStoryTutorID() {
        return _preferences.getString(CURRENT_STORIES_TUTOR_KEY, null);
    }

    @Override
    public void updateStoryTutorID(String id, boolean save) {
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        String Method2 = Thread.currentThread().getStackTrace()[3].getMethodName();
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, Method2 + " --> " + Method + "(" + id + ")");

        openEditor();
        _editor.putString(CURRENT_STORIES_TUTOR_KEY, id);
        applyEditor(save);
    }

    @Override
    public String getMathTutorID() {
        return _preferences.getString(CURRENT_MATH_TUTOR_KEY, null);
    }

    @Override
    public void updateMathTutorID(String id, boolean save) {
        String Method = Thread.currentThread().getStackTrace()[2].getMethodName();
        String Method2 = Thread.currentThread().getStackTrace()[3].getMethodName();
        RoboTutor.logManager.postEvent_I(MENU_BUG_TAG, Method2 + " --> " + Method + "(" + id + ")");

        openEditor();
        _editor.putString(CURRENT_MATH_TUTOR_KEY, id);
        applyEditor(save);
    }

    @Override
    public String getActiveSkill() {
        String activeSkill = _preferences.getString(SKILL_SELECTED_KEY, SELECT_WRITING); // MENU_LOGIC should this have a default???
        Log.wtf("ACTIVE_SKILL", "get=" + activeSkill);
        return activeSkill;
    }

    @Override
    public void updateActiveSkill(String skill, boolean save) {
        openEditor();
        _editor.putString(SKILL_SELECTED_KEY, skill);
        applyEditor(save);
        Log.wtf("ACTIVE_SKILL", "update=" + skill);
    }

    @Override
    public boolean getWritingPlacement() {
        return _preferences.getBoolean(WRITING_PLACEMENT_KEY, false);
    }

    @Override
    public int getWritingPlacementIndex() {
        return _preferences.getInt(WRITING_PLACEMENT_INDEX_KEY, 0);
    }

    @Override
    public boolean getMathPlacement() {
        return _preferences.getBoolean(MATH_PLACEMENT_KEY, false);
    }

    @Override
    public int getMathPlacementIndex() {
        return  _preferences.getInt(MATH_PLACEMENT_INDEX_KEY, 0);
    }


    @Override
    public void updateLastTutor(String activeTutorId, boolean save) {
        openEditor();
        _editor.putString (LAST_TUTOR_PLAYED_KEY, activeTutorId);
        applyEditor(save);
    }

    // MENU_LOGIC only called once
    @Override
    public String getLastTutor() {
        return _preferences.getString(LAST_TUTOR_PLAYED_KEY, null);
    }

    @Override
    public void updateMathPlacement(boolean b, boolean save) {
        openEditor();
        _editor.putBoolean(MATH_PLACEMENT_KEY, b);
        applyEditor(save);
    }

    @Override
    public void updateMathPlacementIndex(Integer i, boolean save) {
        openEditor();
        if (i == null) {
            _editor.remove(MATH_PLACEMENT_INDEX_KEY);
        } else {
            _editor.putInt(MATH_PLACEMENT_INDEX_KEY, i);
        }
        applyEditor(save);
    }

    @Override
    public void updateWritingPlacement(boolean b, boolean save) {
        openEditor();
        _editor.putBoolean(WRITING_PLACEMENT_KEY, b);
        applyEditor(save);
    }

    @Override
    public void updateWritingPlacementIndex(Integer i, boolean save) {
        openEditor();
        if (i == null) {
            _editor.remove(WRITING_PLACEMENT_INDEX_KEY);
        } else {
            _editor.putInt(WRITING_PLACEMENT_INDEX_KEY, i);
        }
        applyEditor(save);
    }

    /**
     * Gets how many times a tutor has been played, to determine whether or not to play a video
     * @param tutor
     * @return
     */
    @Override
    public int getTimesPlayedTutor(String tutor) {
        String key = getTimesPlayedKey(tutor);
        return _preferences.getInt(key, 0);
    }

    // TODO mimic here
    @Override
    public void updateTimesPlayedTutor(String tutor, int i, boolean save) {
        openEditor();
        _editor.putInt(getTimesPlayedKey(tutor), i);
        applyEditor(save);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        Map prefsMap = _preferences.getAll();
        for (Object k : prefsMap.keySet()) {
            builder.append(k)
                    .append("=")
                    .append(prefsMap.get(k))
                    .append(";\n");
        }

        return builder.toString();
    }

    @Override
    public String toLogString() {
        StringBuilder builder = new StringBuilder();

        Map prefsMap = _preferences.getAll();
        for (Object k : prefsMap.keySet()) {
            builder.append(k)
                    .append("-")
                    .append(prefsMap.get(k))
                    .append("---");
        }

        return builder.toString();
    }

    @Override
    public void saveAll() {
        applyEditor(true);
    }

    private void applyEditor(boolean save) {
        if(!save) return;
        if (_editorOpen) {
            _editor.apply();
            _editorOpen = false;
        }
    }

    private void openEditor() {
        if (!_editorOpen) {
            _editor = _preferences.edit();
            _editorOpen = true;
        }
    }
}
