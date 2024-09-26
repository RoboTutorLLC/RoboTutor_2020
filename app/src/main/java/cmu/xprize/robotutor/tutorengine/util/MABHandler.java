package cmu.xprize.robotutor.tutorengine.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Handler for MAB (Multi-Arm Bandit)
 * MAB is used to improve RoboTutor over time by adjusting the probabilities of different arms.
 */
public class MABHandler {

    /*
    Algorithm ->
    1. Load arm_weights.json specifying each arm's name, numerical weight, and pathname to the activity matrix for that arm.
    2. Pick arm with probability proportional to its weight in arm_weights.json
    3. Include arm name as part of session log filename
    4. Select activity matrix associated with arm.
     */

    static String KEY_ARRAY = "arms";
    private static final String TAG = "MABHandler";

    public static String getArm(String dataSource, IScope2 scope) {
        List<Arm> arms = getarms(dataSource, scope);
        Arm selectedArm = selectArm(arms);
        Log.d(TAG, "getArm: list = " + arms);
        Log.d(TAG, "getArm: selected = " + selectedArm);
        return "";
    }

    // Selects an arm from a list of arms
    private static Arm selectArm(List<Arm> arms) {
        float sum = 0;
        for (Arm arm : arms) {
            sum += arm.weight;
        }

        // Select random number between 0 and sum
        float p = getRandom(0, sum);

        // find out where p lies
        float bottom = 0;
        for (Arm arm : arms) {
            float top = bottom + arm.weight;
            if (bottom <= p && p <= top) {
                return arm;
            }
            bottom = top;
        }
        return null;
    }

    // Returns random number between [min, max]
    private static float getRandom(float min, float max) {
        return (float) (min + Math.random() * (max - min));
    }


    private static List<Arm> getarms(String dataSource, IScope2 scope) {
        String jsonData = JSON_Helper.cacheData(dataSource);
        List<Arm> arms = new ArrayList<>();
        try {
            JSONObject rootObject = new JSONObject(jsonData);
            JSONArray rootArray = rootObject.getJSONArray(KEY_ARRAY);
            arms = parseArray(rootArray, scope);
    
            // Adding logging to print arms
            for (Arm arm : arms) {
                Log.d(TAG, "Arm: " + arm.name + ", Weight: " + arm.weight + ", Matrix Path"  + arm.matrix); 
            }
    
        } catch (Exception e) {
            Log.e(TAG, "Error in getarms: " + e.getMessage());
        }
        return arms;
}


    private static List<Arm> parseArray(JSONArray array, IScope2 scope) throws JSONException {
        List<Arm> arms = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject armJSON = array.getJSONObject(i);
            Arm arm = new Arm(armJSON, scope);
            arms.add(arm);
        }
        return arms;
    }


}
