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
import cmu.xprize.comp_logging.PerformanceLogItem;

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
        Arm selectedArm = selectArm(arms, 0.1F);
        // Ensure that selected arm is not null
        if (selectedArm != null) {
            Log.d(TAG, "getArm: selected = " + selectedArm.name);
            return selectedArm.name;
        } else {
            Log.e(TAG, "getArm: No arm was selected");
            return "default_arm";
        }
    }

    public static Float getArmWeight(String armName, List<Arm> arms) {
        for (Arm arm : arms) {
            if (arm.name.equals(armName)) {
                return arm.weight;
            }
        }
        return null;
    }

    public static String getMatrixName(String armName, List<Arm> arms) {
        for (Arm arm : arms) {
            if (arm.name.equals(armName)) {
                return arm.matrix;
            }
        }
        return null;
    }


    // Selects an arm from a list of arms using ε-greedy algorithm
    private static Arm selectArm(List<Arm> arms, float epsilon) {
        // Random number to decide between exploration and exploitation
        float randomV = getRandom(0, 1);
        if (randomV < epsilon) {
            // Exploration:choose a random arm
            int randomIndex = (int) getRandom(0, arms.size());
            Arm randomArm = arms.get(randomIndex);
            return randomArm;
        } else {
            // Exploitation:choose the arm with highest weight
            Arm bestArm = null;
            float maxWeight = Float.NEGATIVE_INFINITY;
            for (Arm arm : arms) {
                if (arm.weight > maxWeight) {
                    maxWeight = arm.weight;
                    bestArm = arm;
                }
            }
            return bestArm;
        }
    }


    // Returns random number between [min, max]
    private static float getRandom(float min, float max) {
        return (float) (min + Math.random() * (max - min));
    }


    public static List<Arm> getarms(String dataSource, IScope2 scope) {
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