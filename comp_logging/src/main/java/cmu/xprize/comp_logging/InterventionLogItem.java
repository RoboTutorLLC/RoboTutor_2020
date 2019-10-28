package cmu.xprize.comp_logging;

import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-10-25.
 */
public class InterventionLogItem {

    long timestamp;
    String student;
    String group;
    String tutor;
    String recommendStudent;
    String errorType;
    Boolean helpTaken;

    public InterventionLogItem(
            long timestamp, String student, String group,
            String tutor, String recommendStudent,
            String errorType, Boolean helpTaken) {
        this.timestamp = timestamp;
        this.student = student;
        this.group = group;
        this.tutor = tutor;
        this.recommendStudent = recommendStudent;
        this.errorType = errorType;
        this.helpTaken = helpTaken;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }
}
