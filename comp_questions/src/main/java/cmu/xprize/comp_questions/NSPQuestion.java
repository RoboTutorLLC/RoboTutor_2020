package cmu.xprize.comp_questions;

import org.json.JSONObject;

import java.util.List;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;


/**
 * uhq: Class used to parse the nsp.json data for NSP options
 */
public class NSPQuestion implements ILoadableObject {
    // json loadable
    public NSPContextSentence context_sentence;
    public NSPChoice choices;

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);

    }
}