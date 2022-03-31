package cmu.xprize.comp_questions;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * uhq: Class used to parse data from nsp.json for a single NSP question choice
 */
public class NSPChoice implements ILoadableObject{
    // json loadable
    public String type;
    public int index;
    public String text;
    public double coherence;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        try{
            JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
        } catch(Exception e){
        }

    }
}
