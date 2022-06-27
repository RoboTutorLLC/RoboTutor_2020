//*********************************************************************************
//
//    Copyright(c) 2016-2017  Kevin Willows All Rights Reserved
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//*********************************************************************************

package cmu.xprize.robotutor.tutorengine.graph.vars;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.xprize.robotutor.tutorengine.ILoadableObject2;
import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;

public class TFloat extends TVarBase implements ILoadableObject2, IScriptable2 {

    private double _value;

    public TFloat() {

    }

    public TFloat(String string) {
        _value = Double.parseDouble(string);
    }

    @Override
    public void set(String value) {
        _value = Double.parseDouble(value);
    }

    @Override
    public Object evaluate(boolean negate) {
        return _value;
    }


    @Override
    public Object getValue() { return _value; }


    @Override
    public String toString() {

        return String.valueOf(_value);
    };



    // *** Serialization


    /**
     * As TVar objects are loaded from JSON the TVarBase initializes the type and name
     * while the values are set per class polymorphically
     *
     * @param jsonObj
     * @param scope
     */
    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        super.loadJSON(jsonObj, scope);

        try {
            _value = jsonObj.getDouble("value");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String resolve(int index) {
        return null;
    }

    @Override
    public int getIntValue() {
        return 0;
    }
}
