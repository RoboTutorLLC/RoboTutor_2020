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

package cmu.xprize.robotutor.tutorengine;

        import android.util.Log;

        import org.json.JSONObject;

        import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
        import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
        import cmu.xprize.util.IScope;
        import cmu.xprize.util.JSON_Helper;
        import cmu.xprize.util.TCONST;

public class CMediaPackage implements ILoadableObject2 {

    // json loadable
    public String   language;
    public String   path;
    public String   location = TCONST.EXTERNAL;          // Either internal - external
    public String   srcpath  = TCONST.ROBOTUTOR_ASSETS;  // Base path to resource

    static private final String TAG = "CMediaPackage";


    public CMediaPackage() {
    }

    public CMediaPackage(String _language, String _path) {
        language = _language;
        path     = _path;
    }

    public CMediaPackage(String _language, String _path, String _srcpath) {
        language = _language;
        path     = _path;
        srcpath  = _srcpath;
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }
}
