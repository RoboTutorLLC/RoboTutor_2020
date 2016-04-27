//*********************************************************************************
//
//    Copyright(c) 2016 Carnegie Mellon University. All Rights Reserved.
//    Copyright(c) Kevin Willows All Rights Reserved
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


import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.robotutor.tutorengine.graph.scene_descriptor;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;


public class CTutorGraph implements ITutorGraph, ILoadableObject2, Animation.AnimationListener {

    private TScope                            mRootScope;

    private boolean                           traceMode     = false;
    private int                               _sceneCnt     = 0;
    private boolean                           _inNavigation = false;
    private String                            _xType;

    protected ITutorScene                     mParent;
    protected CTutor                          mTutor;
    protected String                          mTutorName;
    protected ITutorManager                   mTutorContainer;
    protected ITutorLogManager                mLogManager;
    protected CSceneGraph                     mSceneAnimator;

    private final Handler                     mainHandler = new Handler(Looper.getMainLooper());

    // json loadable
    public scene_descriptor[]                navigatedata;


    // State data
    private HashMap<String, scene_descriptor> _navMap = new HashMap<String, scene_descriptor>();
    private int                               _scenePrev;
    private int                               _sceneCurr;
    private boolean                           _push = false;


    final private String       TAG       = "CTutorGraph";


    /**
     *
     *
     *
     * @param tutor
     * @param name
     * @param tutorScope
     */
    public CTutorGraph(CTutor tutor, String name, ITutorManager tutorContainer, TScope tutorScope) {

        mRootScope = new TScope(tutor, name + "-SceneNavigator", tutorScope);      // Use a unique namespace

        mTutor          = tutor;
        mTutorName      = name;
        mTutorContainer = tutorContainer;

        _sceneCurr = 0;
        _scenePrev = 0;

        loadNavigatorDescr();

        mSceneAnimator = new CSceneGraph(mTutor, tutorScope, this);
    }


    /**
     * Walk the scene descriptors and kill off any remaining scenes
     */
    public void onDestroy() {

        for(scene_descriptor scene : navigatedata) {

            // Do the destruction depth first
            //
            if(scene.instance != null) {

                // If the scene has children - allow them to shutdown gracefully
                //
                if(scene.children != null) {
                    Iterator<?> tObjects = scene.children.entrySet().iterator();

                    // Perform component level cleanup first
                    //
                    while(tObjects.hasNext() ) {
                        Map.Entry entry = (Map.Entry) tObjects.next();

                        ((ITutorObject)(entry.getValue())).onDestroy();
                    }
                }

                // Then tell the container to destruct
                //
                scene.instance.onDestroy();
            }
        }
    }


    public class Queue implements Runnable {

        protected String _command;

        public Queue(String command) {
            _command = command;
        }

        @Override
        public void run() {

            switch(_command) {

                case TCONST.FIRST_SCENE:

                    gotoNextScene(true);
                    break;

                case TCONST.NEXTSCENE:

                    if(gotoNextScene(false).equals(TCONST.ENDTUTOR)) {

                        mainHandler.post(mTutor.new Queue(TCONST.ENDTUTOR));
                    }
                    break;


            }
        }
    }

    /**
     * Post a command to the tutorgraph queue
     *
     * @param command
     */
    public void post(String command) {

        mainHandler.post(new Queue(command));
    }


    // Initialize the pointer to the tutor root scene
    //
    public void initTutorContainer(ITutorSceneImpl rootScene) {

        navigatedata[0].instance = rootScene;
    }

    @Override
    public CSceneGraph getAnimator() {
        return mSceneAnimator;
    }


    /**
     *
     * @return The result maps child names to Views
     */
    @Override
    public HashMap getChildMap() {

        return navigatedata[_sceneCurr].children;
    }


    /**
     *
     * @param sceneName
     * @return  The result maps child names to Views
     */
    @Override
    public HashMap getChildMapByName(String sceneName) {

        return _navMap.get(sceneName).children;
    }



//***************** Navigation Behaviors *******************************



    //*********************************************
    //*********************************************
    //*********************************************
    // Inter Scene Navigation
    //

    //*************** Navigator getter setters -
    // these within a subclass to set the root of a navigation sequence

    protected int getScenePrev() {
        return _scenePrev;
    }
    protected void setScenePrev(int scenePrevINT) {
        _scenePrev = scenePrevINT;
    }


    protected int  getSceneCurr() {
        return _sceneCurr;
    }
    protected void setSceneCurr(int sceneCurrINT) {
        _sceneCurr = sceneCurrINT;
    }


    protected int  sceneCurrINC() {
        String             features;
        ArrayList<String>  featSet= new ArrayList<String>();
        Boolean            match = false;

        _sceneCurr++;

        // If new scene has features, check that it is being used in the current tutor feature set
        // Note: You must ensure that there is a match for the last scene in the sequence

        while((features = navigatedata[_sceneCurr].features) != null)
        {
            // If this scene is not in the feature set for the tutor then check the next one.

            if(!mTutor.testFeatureSet(features)) _sceneCurr++;
            else break;
        }

        return _sceneCurr;
    }


    protected int sceneCurrDEC() {
        String             features;
        ArrayList<String>  featSet= new ArrayList<String>();
        Boolean            match = false;

        _sceneCurr--;

        // If new scene has features, check that it is being used in the current tutor feature set
        // Note: You must ensure that there is a match for the last scene in the sequence

        while((features = navigatedata[_sceneCurr].features) != null)
        {
            // If this scene is not in the feature set for the tutor then check the next one.

            if(!mTutor.testFeatureSet(features)) _sceneCurr--;
            else break;
        }

        return _sceneCurr;
    }



    private int findSceneOrd(String tarScene) {
        
        if(traceMode) Log.i(TAG, "findSceneOrd: " + tarScene);

        // returns the scene ordinal in the sequence array or 0
        //
        return _navMap.get(tarScene).index;
    }


    /**
     * gotoNextScene manual entry point
     */
    @Override
    public String gotoNextScene(boolean push) {

        String result = TCONST.ENDTUTOR;

        if(traceMode) Log.i(TAG, "gotoNextScene: ");

        String newScene = "";
        String redScene = "";

        // Local push - used in onAnimationEnd to save previous scene on stack when starting
        // a new tutor
        _push = push;

        // TODO: This is a stopgap until we have full tutorgraph capabilities.
        ///
        if (_sceneCurr < _sceneCnt-1) {

            // remember current frame
            //
            if (traceMode)
                Log.d(TAG, "scenePrev: " + _scenePrev + "  - sceneCurr: " + _sceneCurr);

            _scenePrev = _sceneCurr;

            // Do scene Specific termination
            //
            if (traceMode)
                Log.d(TAG, "navigatedata[_sceneCurr]: " + navigatedata[_sceneCurr].id);

            navigatedata[_scenePrev].instance.onExitScene();

            // increment the current scene - this is feature reactive
            sceneCurrINC();

            if (navigatedata[_sceneCurr].instance == null) {
                mTutor.instantiateScene(navigatedata[_sceneCurr]);
            }

            // Update the tutor SceneContainer used to enumerate components.
            //
            mTutor.setSceneContainer((ViewGroup)navigatedata[_sceneCurr].instance);

            //@@ Action Logging
            //            var logData:Object = {'navevent':'navnext', 'curscene':_scenePrev, 'newscene':redScene};
            //            //var xmlVal:XML = <navnext curscene={_scenePrev} newscene={redScene}/>
            //
            //            gLogR.logNavEvent(logData);
            //@@ Action Logging

            // On exit behaviors
            navigatedata[_scenePrev].instance.onExitScene();

            // Do the scene transition - add callback for when IN animation ends
            mTutorContainer.setAnimationListener(this);
            mTutorContainer.addView(navigatedata[_sceneCurr].instance);

            result = TCONST.CONTINUETUTOR;
        }
        return result;
    }


    /** Animation Listener START *************************/
    //

    @Override
    public void onAnimationStart(Animation animation) {

    }

    // Performed immediately after scene is fully onscreen
    //@@ Mod Jul 18 2013 - public -> private
    //
    @Override
    public void onAnimationEnd(Animation animation) {

        if(traceMode) Log.d(TAG, "doEnterScene: " + _sceneCurr);

        mTutorContainer.setAnimationListener(null);

        // increment the global frame ID - for logging

        mTutor.incFrameNdx();
        mTutorContainer.pushView(_push);

        //## Mod Sep 12 2013 - This is a special case to handle the first preenter event for an animationGraph.
        //                     The root node of the animation graph is parsed in the preEnter stage of the scene
        //                     creation so the scene is not yet on stage. This call ensures that the scene
        //                     associated with the animation object has been instantiated.
        //
        //	TODO: This should be rationalized with the standard preEnter when all the preEnter customizations
        //        in CWOZScene derivatives have been moved to the XML (JSON) spec.
        //
        navigatedata[_sceneCurr].instance.onEnterScene();

        mSceneAnimator.post(TCONST.ENTER_SCENE, navigatedata[_sceneCurr].id);
        mSceneAnimator.post(TCONST.NEXT_NODE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    //
    /** Animation Listener END *************************/




    //************ Serialization



    /**
     * Load the Tutor specification from JSON file data
     * from assets/tutors/<tutorname>/navigator_descriptor.json
     *
     * This is only used here until we have the scenegraph implementation in place.
     * This provides a simple linear or mapped access to scenes.
     *
     */
    private void loadNavigatorDescr() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + mTutorName + "/" + TCONST.SNDESC)), (IScope2)mRootScope);

        } catch (JSONException e) {
            Log.d(TAG, "Error" );
        }
    }


    @Override
    public void loadJSON(JSONObject jsonObj, IScope2 scope) {
        int i1 = 0;

        JSON_Helper.parseSelf(jsonObj, this, CClassMap2.classMap, scope);

        // shortcut to length
        _sceneCnt = navigatedata.length;

        // Generate a hash map for all the scenes in the tutor
        for(scene_descriptor scene : navigatedata) {
            scene.index = i1++;
            _navMap.put(scene.id, scene);
        }
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {

        Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
