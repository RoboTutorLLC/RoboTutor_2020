package cmu.xprize.robotutor.tutorengine.util;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;

public class Arm {

    public String name;
    public Float weight;
    public String matrix;

    public Arm(JSONObject jsonObject, IScope2 scope) {
        try {
            loadJSON(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadJSON(JSONObject jsonObj) throws JSONException {
        name = jsonObj.getString("name");
        weight = (float) jsonObj.getDouble("weight");
        matrix = jsonObj.getString("matrix");
    }

    @Override
    public String toString() {
        return "Arm{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", matrix='" + matrix + '\'' +
                '}';
    }
}
