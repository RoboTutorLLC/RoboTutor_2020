package cmu.xprize.comp_questions;

import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

public class NSPContextSentence implements ILoadableObject {
    public String text;
    public int index;
    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        try{
            JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
        } catch(Exception e){
            Log.d("SEND HELP", e.toString());
        }
    }
}
