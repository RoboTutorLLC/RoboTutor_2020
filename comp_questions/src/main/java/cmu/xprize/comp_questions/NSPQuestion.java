package cmu.xprize.comp_questions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public ArrayList<NSPChoice> choices;


    public NSPQuestion(JSONObject jsonObject) throws JSONException {

        JSONArray arr = jsonObject.getJSONArray("choices");

        for (int i = 0; i < arr.length(); i++) {

            JSONObject jsonObject1 = arr.getJSONObject(i);

            NSPChoice choice = new NSPChoice(jsonObject1.getString("type"),
                    i,
                    jsonObject1.getString("text"),
                    1.4
            );


            choices.add(choice);
        }


    }



    //    //todo choices is not a list and cannot be accessed as one. You must create an accumulated list of all nsp choices to iterate through NSP options.
//    @Override
//    public void loadJSON(JSONObject jsonObj, IScope scope) {
//        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
//
//    }
    //todo choices is not a list and cannot be accessed as one. You must create an accumulated list of all nsp choices to iterate through NSP options.
    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) throws JSONException {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);



    }
}