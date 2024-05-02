package cmu.xprize.util;

import org.json.JSONException;
import org.json.JSONObject;


public interface ILoadableObject {
    public void loadJSON(JSONObject jsonObj, IScope scope) throws JSONException;
}
