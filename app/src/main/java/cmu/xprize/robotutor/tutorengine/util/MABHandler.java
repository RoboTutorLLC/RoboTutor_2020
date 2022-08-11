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
        List<ArmWeight> armWeights = getArmWeights(dataSource, scope);
        ArmWeight selectedArm = selectArm(armWeights);
        Log.d(TAG, "getArm: list = "+armWeights);
        Log.d(TAG, "getArm: selected = "+selectedArm);
        return "";
    }

    // Selects an arm from a list of arms
    private static ArmWeight selectArm(List<ArmWeight> armWeights) {
        float sum = 0;
        for (ArmWeight arm : armWeights) {
            sum += arm.weight;
        }

        // Select random number between 0 and sum
        float p = getRandom(0, sum);

        // find out where p lies
        float bottom = 0;
        for (ArmWeight arm : armWeights) {
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


    private static List<ArmWeight> getArmWeights(String dataSource, IScope2 scope) {
        String jsonData = JSON_Helper.cacheData(dataSource);
        List<ArmWeight> armWeights = new ArrayList<>();
        try {
            JSONObject rootObject = new JSONObject(jsonData);
            JSONArray rootArray = rootObject.getJSONArray(KEY_ARRAY);
            armWeights = parseArray(rootArray, scope);
        } catch (Exception e) {
            Log.d(TAG, "getArmWeights: "+e);
        }
        return armWeights;
    }

    private static List<ArmWeight> parseArray(JSONArray array, IScope2 scope) throws JSONException {
        List<ArmWeight> armWeights = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject armWeightJSON = array.getJSONObject(i);
            ArmWeight armWeight = new ArmWeight(armWeightJSON, scope);
            armWeights.add(armWeight);
        }
        return armWeights;
    }


}
