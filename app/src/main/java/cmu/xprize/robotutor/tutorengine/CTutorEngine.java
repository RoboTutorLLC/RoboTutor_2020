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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import android.os.Handler;

import android.os.PowerManager;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cmu.xprize.comp_intervention.data.CUpdateInterventionStudentData;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.robotutor.R;
import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.util.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.graph.databinding;
import cmu.xprize.robotutor.tutorengine.graph.defdata_scenes;
import cmu.xprize.robotutor.tutorengine.graph.defdata_tutor;
import cmu.xprize.robotutor.tutorengine.graph.defvar_tutor;
import cmu.xprize.robotutor.tutorengine.graph.vars.IScope2;
import cmu.xprize.robotutor.tutorengine.graph.vars.TScope;
import cmu.xprize.robotutor.tutorengine.util.CClassMap2;
import cmu.xprize.robotutor.tutorengine.util.IStudentDataModel;
import cmu.xprize.robotutor.tutorengine.util.MABHandler;
import cmu.xprize.robotutor.tutorengine.util.PromotionMechanism;
import cmu.xprize.robotutor.tutorengine.util.StudentDataModelCSV;
import cmu.xprize.robotutor.tutorengine.util.StudentDataModelSharedPrefs;
import cmu.xprize.robotutor.tutorengine.util.TransitionMatrixModel;
import cmu.xprize.robotutor.tutorengine.widgets.core.TSceneAnimatorLayout;
import cmu.xprize.util.GlobalStaticsEngine;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

import static cmu.xprize.util.TCONST.DEBUG_CSV;

/**
 * The tutor engine provides top-levelFolder control over the tutor lifecycle and can support multiple
 * simultaneous tutors.  On creation the tutor engine will instantiate and launch the DefTutor
 * specified in the TCONST.EDESC Json tutor engine specification file.
 *
 * CTutorEngine is a singleton
 *
 */
public class CTutorEngine implements ILoadableObject2 {

    private static TScope                   mRootScope;

    private static CTutorEngine             singletonTutorEngine;

    private CMediaManager                   mMediaManager;

    public static IStudentDataModel         studentModel;
    public static TransitionMatrixModel     matrix;
    public static PromotionMechanism        promotionMechanism;
    public enum MenuType {STUDENT_CHOICE, CYCLE_CONTENT};
    public static MenuType menuType;

    static public  RoboTutor                Activity;
    static public  ILogManager              TutorLogManager;

    static private HashMap<String,CTutor>   tutorMap        = new HashMap<>();
    static private CTutor                   activeTutor     = null;
    static private CTutor                   deadTutor       = null;

    // You can override the language used in all tutors by placing a
    // "language":"LANG_EN", spec in the TCONST.EDESC replacing EN with
    // the desired language id

    // must match incoming json data
    //
    private String                          EXPECTED_VERSION = "1.0";

    // json loadable
    static public String                         descr_version;                 //
    static public String                         defTutor; // defined in engine_descriptor.json
    static public HashMap<String, defvar_tutor>  tutorVariants;
    static public HashMap<String, defdata_tutor> bindingPatterns;
    static public String                         language;                       // Accessed from a static context


    final static private String TAG         = "CTutorEngine";


    /**
     * TutorEngine is a Singleton
     *
     * Load and generate the root tutor - This root tutor may be a single-topic tutor
     * or a mananger interface "tutor" permitting access to other sub-tutors.  So if you have complex
     * content management (i.e. student models) it/they should be embodied in the manager interface
     * component logic.
     *
     * @param context
     */
    private CTutorEngine(RoboTutor context) {

        mRootScope      = new TScope(null, "root", null);

        Activity        = context;
        TutorLogManager = CLogManager.getInstance();

        // Load the TCONST.EDESC and generate the root tutor
        //
        loadEngineDescr();
    }


    /**
     * Retrieve the one and only tutorEngine object
     *
     * @param context
     * @return
     */
    static public CTutorEngine getTutorEngine(RoboTutor context) {

        Log.w(DEBUG_CSV, "getTutorEngine()");
        if(singletonTutorEngine == null) {
            Log.w(DEBUG_CSV, "new CTutorEngine with STUDENT_ID = " + RoboTutor.STUDENT_ID);
            singletonTutorEngine = new CTutorEngine(context);
        } else if (!RoboTutor.STUDENT_ID.equals(studentModel.getStudentId())){
            Log.w(DEBUG_CSV, "Changing StudentModel from " + studentModel.getStudentId()
             + " to " + RoboTutor.STUDENT_ID);

            studentModel = loadStudentModel(matrix);
            // promotion dependent on StudentModel, so we must update this too
            promotionMechanism = new PromotionMechanism(studentModel, matrix);

        }

        return singletonTutorEngine;
    }


    /**
     * This is primarily intended as a development API to allow updating the working language
     * at runtime.
     * @param newLang
     */
    static public void setDefaultLanguage(String newLang) {
        language = newLang;
        //  any time the language changes, so should the Transition Matrix and the Student Model
        matrix = loadTransitionMatrixModel();
        studentModel = loadStudentModel(matrix);
        promotionMechanism = new PromotionMechanism(studentModel, matrix);
        menuType = getMenuType();
    }


    /**
     * This is primarily intended as a development API to allow updating the working language
     * at runtime.
     */
    static public String getDefaultLanguage() {
        return language;
    }


    static public TScope getScope() {

        return mRootScope;
    }


    static public Activity getActivity() {
        return Activity;
    }


    static public void pauseTutor() {

    }


    /**
     *  Used to destroy all tutors when the system calls onDestroy for the app
     *
     */
    static public void killAllTutors() {

        while(tutorMap.size() > 0) {

            Iterator<?> tutorObjects = tutorMap.entrySet().iterator();

            Map.Entry entry = (Map.Entry) tutorObjects.next();

            CTutor tutor = ((CTutor) (entry.getValue()));

            // Note the endTutor call will invalidate this iterator so recreate it
            // on each pass
            //
            //tutor.terminateQueue();
            //tutor.endTutor();
        }

        singletonTutorEngine = null;
    }


    static public void startSessionManager() {

        defdata_tutor tutorBindings = null;

        if(bindingPatterns != null) {
            tutorBindings = bindingPatterns.get(defTutor);
        }

        // These features are based on the current tutor selection model
        // When no tutor has been selected it should run the tutor select
        // and when it finishes it should run the difficulty select until
        // the user wants to select another tutor.
        //

        // Update the tutor id shown in the log stream
        //
        CLogManager.setTutor(defTutor);

        // don't create a new tutor when the screen is off, because on relaunch it will set-in-motion the "KILLTUTOR"
        // https://stackoverflow.com/questions/2474367/how-can-i-tell-if-the-screen-is-on-in-android
        PowerManager powerManager = (PowerManager) Activity.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if (powerManager != null && !powerManager.isInteractive()) {
                return;
            }
        }

        createAndLaunchTutor(defTutor, RoboTutor.SELECTOR_MODE, null, tutorBindings, null); // where Activity Selector is launched
    }

    /**
     *
     * This launches a new tutor immediately at startup. Used for quick debugging.
     */
    static public void quickLaunch(String tutorVariant, String tutorId, String tutorFile) {

        for (String name: tutorVariants.keySet()){

            String key =name.toString();
            String value = tutorVariants.get(name).tutorName;
            String feats = tutorVariants.get(name).features;
            System.out.println(key + " tutorName: " + value + " Features: " + feats);

        }
        String value = tutorVariants.get(tutorVariant).tutorName;
        String feats = tutorVariants.get(tutorVariant).features;
        System.out.println(tutorVariant + " tutorName: " + value + " Features: " + feats);
        defvar_tutor  tutorDescriptor = tutorVariants.get(tutorVariant);
        defdata_tutor tutorBinding    = bindingPatterns.get(tutorDescriptor.tutorName);

        initializeBindingPattern(tutorBinding, tutorFile);

        GlobalStaticsEngine.setCurrentDomain("LIT"); // for quickLaunch, just hardwire it, whatever

        createAndLaunchTutor(tutorDescriptor.tutorName , tutorDescriptor.features, tutorId, tutorBinding, null);
    }

    static public CTutor getActiveTutor() {
        return activeTutor;
    }

    /**
     * Here a tutor is destroying itself - so we need to manage the follow-on process -
     * i.e. start some other activity / tutor or session mamagement task.
     */
    static public void destroyCurrentTutor() {

        // When using the back button within a native tutor we will be killing the one and
        // only tutor so deadTutor will be null
        //
        deadTutor   = activeTutor;
        activeTutor = null;
        RoboTutor.masterContainer.removeView(deadTutor.getTutorContainer());

        startSessionManager();


        // Get the tutor being killed and do a depth first destruction to allow
        // components to release resources etc.
        //
        deadTutor.onDestroy();
        deadTutor = null;
    }


    /**
     * Here a tutor has been killed off externally and need to be cleaned up.
     */
    static public void killDeadTutor() {

        Log.d(TAG, "killDeadTutor: " + deadTutor.getTutorName());

        // Get the tutor being killed and do a depth first destruction to allow
        // components to release resources etc.
        //
        deadTutor.onDestroy();
        deadTutor = null;
    }


    /**
     * Here a tutor is being destroying externally
     */
    static public void killActiveTutor() {

        // GRAY_SCREEN_BUG
        if(activeTutor != null) {

            deadTutor = activeTutor;

            activeTutor = null;

            Log.d(TAG, "Killing Tutor: " + deadTutor.getTutorName());

            if(deadTutor.getTutorName()=="activity_selector" &&
                    Configuration.getRecordingSessionOrActivity(Activity.getApplicationContext())=="session") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Activity.hbRecorder.resumeScreenRecording();

                }
            }

            RoboTutor.masterContainer.removeView(deadTutor.getTutorContainer());
            deadTutor.post(TCONST.KILLTUTOR);
        }
    }


    /**
     * Create a tutor by name - if a tutor is running already then kill it off first
     *
     * @param tutorName
     * @param features
     */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static private void createAndLaunchTutor(String tutorName, String features, String tutorId, defdata_tutor dataSource, String matrix) {
        killActiveTutor();


        // GRAY_SCREEN_BUG
        Log.d(TAG, "createAndLaunchTutor: " + tutorName + ", " + tutorId);


        if (tutorId != null) {
            if (tutorName != null) {
                if (Configuration.getRecordingSessionOrActivity(Activity.getApplicationContext()).equals("session")) {
                    Log.d("CTutorEngine", "CreateAndLaunchTutor: Session mode");
                    if (tutorName.equals("activity_selector")) {
                        //Log.d("CTutorEngine", "CreateAndLaunchTutor: Activity Selector so pause here after 15 sec");
                        Log.d("CTutorEngine", "Restarted audio recording if it was paused");
                        Activity.hbRecorder.isAudioEnabled(true);

                        //pause recording after 20 seconds on menu to save space
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    // if even after 20 seconds active tutor is activity_selector
                                    if (activeTutor!=null && activeTutor.getTutorName().equals("activity_selector")) {
                                        Log.d("CTutorEngine", "Pausing Screen Recording since inactivity on menu screen");
                                        Activity.hbRecorder.pauseScreenRecording();
                                    }
                                }

                            }, 20000);
                        }
                    } else {
                        Log.d("CTutorEngine", "Resuming Screen Recording since a lesson is selected");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Activity.hbRecorder.resumeScreenRecording();
                        }
                        if (tutorId.contains(".read") || tutorId.contains(".echo") || tutorId.contains(".parrot") || tutorId.contains(".reveal")) {
                            Log.d(TAG, "One of the activities requiring mic, pausing audio recording");
                            Activity.hbRecorder.isAudioEnabled(false);

                        }
                    }

                } else {
                    // recording activity wise
                    if (tutorName == "activity_selector") {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Activity.hbRecorder.stopScreenRecording();
                        }
                    } else {
                        if (tutorId.contains(".read") || tutorId.contains(".echo") || tutorId.contains(".parrot") || tutorId.contains(".reveal")) {
                            Log.d(TAG, "One of the activities requiring mic, pausing audio recording");
                            Activity.hbRecorder.isAudioEnabled(false);

                        }
                        Activity.startRecordingScreen();
                    }

                }
            }
        }

        // Create a new tutor container relative to the masterContainer
        //
        ViewGroup tutorContainer = new TSceneAnimatorLayout(Activity);
        tutorContainer.inflate(Activity, R.layout.scene_layout, null);
        ((ITutorObject)tutorContainer).setName("tutor_container");

        RoboTutor.masterContainer.addView((ITutorManager)tutorContainer);

        activeTutor = new CTutor(Activity, tutorName, tutorId, (ITutorManager)tutorContainer, TutorLogManager, mRootScope, language, features, matrix);
        GlobalStaticsEngine.setCurrentTutorId(tutorId);
        activeTutor.launchTutor(dataSource);
    }


    static private defdata_scenes parseSceneData(defdata_tutor dataPattern, String[] componentSet) {

        defdata_scenes          sceneData = new defdata_scenes();
        ArrayList<databinding>  bindings  = new ArrayList<>();

        String compData   = null;
        String compName   = null;

        for(String component : componentSet) {

            String[] dataSet = component.split(":");

            if (dataSet.length == 1) {

                compName = "*";
                compData = dataSet[0];

            } else {
                compName = dataSet[0];
                compData = dataSet[1];
            }

            bindings.add(new databinding(compName, compData));
        }

        sceneData.databindings = (databinding[]) bindings.toArray(new databinding[bindings.size()]);

        return sceneData;
    }


    static private defdata_tutor parseDataSpec(String dataSpec) {

        defdata_tutor   dataPattern = new defdata_tutor();
        defdata_scenes  sceneData   = null;
        String          sceneName   = null;

        String[] sceneSet = dataSpec.split(";");

        for(String scene : sceneSet) {

            String[] sceneElements = scene.split("\\|");

            // If there is only 1 element then there is only one scene and its name is implied
            //
            if(sceneElements.length == 1) {

                sceneName = "*";
                sceneData = parseSceneData(dataPattern, sceneElements);
            }
            else {
                sceneName     = sceneElements[0];
                sceneElements = Arrays.copyOfRange(sceneElements, 1, sceneElements.length);

                sceneData = parseSceneData(dataPattern, sceneElements);
            }

            dataPattern.scene_bindings.put(sceneName, sceneData);
        }

        return dataPattern;
    }


    static private void  initComponentBindings(databinding[] targetbindings, databinding[] databindings) {

        for(databinding binding : databindings) {

            if(binding.name.equals("*")) {
                if(targetbindings.length == 1) {
                    targetbindings[0].datasource = binding.datasource;
                }
                else {
                    Log.e(TAG, "ERROR: Incompatible datasource");
                }
            }
            else {
                for(databinding tbinding : targetbindings) {
                    if(tbinding.name.equals(binding.name)) {
                        tbinding.datasource = binding.datasource;
                        break;
                    }
                }
            }

        }
    }


    static private void  initSceneBindings(defdata_tutor bindingPattern, String sceneName, databinding[] databindings) {

        if(sceneName.equals("*")) {
            System.out.println(bindingPattern.scene_bindings.isEmpty());
            if(bindingPattern.scene_bindings.size() == 1) {

                Iterator<?> scenes = bindingPattern.scene_bindings.entrySet().iterator();
                while(scenes.hasNext() ) {

                    Map.Entry scene = (Map.Entry) scenes.next();

                    databinding[] scenebindings = ((defdata_scenes)scene.getValue()).databindings;

                    initComponentBindings(scenebindings, databindings);
                }
            }
            else {
                Log.e(TAG, "ERROR: Incompatible datasource");
            }
        }
        else {
            defdata_scenes compData = bindingPattern.scene_bindings.get(sceneName);

            initComponentBindings(compData.databindings, databindings);
        }

    }


    /**
     * The data spec is encoded as:
     *
     *  <dataspec>...
     *  <dataspec>  = scenename|<scenedata>...
     *  <scenedata> = component:datasource
     *
     *  e.g.
     *      tutor_scene1|sceme_compD:[dataencoding]datasource|sceme_compM:[dataencoding]datasource;
     *      tutor_scene2|sceme_compQ:[dataencoding]datasource; ...
     *
     *
     *
     * @param bindingPattern
     * @param dataSpec
     */
    static private void initializeBindingPattern(defdata_tutor bindingPattern, String dataSpec) {
        System.out.print("dataSpec: ");
        System.out.println(dataSpec);
        defdata_tutor dataBindings = parseDataSpec(dataSpec);

        Iterator<?> scenes = dataBindings.scene_bindings.entrySet().iterator();

        while(scenes.hasNext() ) {

            Map.Entry scene = (Map.Entry) scenes.next();

            String sceneName           = (String)scene.getKey();
            databinding[] databindings = ((defdata_scenes)scene.getValue()).databindings;

            initSceneBindings(bindingPattern, sceneName, databindings);
        }
    }


    /**
     *  Scriptable Launch command
     *
     * @param tutorVariant
     * @param intentType
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static public void launch(String intentType, String tutorVariant, String dataSource, String tutorId, String matrix) {
        Log.d(TAG, "launch: tutorId=" + tutorId);
        // start recording when launching a menu screen
        // end recording when entering the menu
        Activity temp_act = getActivity();
        RoboTutor act = (RoboTutor)temp_act;
        String dataPath = TCONST.DOWNLOAD_PATH + "/config.json";
        String jsonData = JSON_Helper.cacheDataByName(dataPath);
        Log.i(TAG, "launch: the screen recording launcher will begin now");



        // if activity wise recording is selected start recording

//        String session_or_activity=Configuration.getRecordingSessionOrActivity(act.getApplicationContext());
//        Log.i("ConfigurationItems", "Inside CTutorEngine, session or activity flag is:"+session_or_activity);
//        if(session_or_activity.equals("activity")) {
//            act.startRecordingScreen();
//            // If activity is of type .read, .echo, .parrot, .reveal we need to stop recording audio
//            if(tutorId.contains(".read") || tutorId.contains(".echo") || tutorId.contains(".parrot") || tutorId.contains(".reveal")) {
//                act.hbRecorder.isAudioEnabled(false);
//            }
//        }
//
//        // if whole session recording is selected, resume recording
//        else {
//
//            if (Activity.hbRecorder.isRecordingPaused()) {
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    Activity.hbRecorder.resumeScreenRecording();
//                    // If activity is of type .read, .echo, .parrot, .reveal we need to pause recording audio
//                    if(tutorId.contains(".read") || tutorId.contains(".echo") || tutorId.contains(".parrot") || tutorId.contains(".reveal")) {
//                        Log.d(TAG, "One of the activities requiring mic, pausing audio recording");
//                        act.hbRecorder.isAudioEnabled(false);
//
//                    }
//                }
//            }
//  }






        Intent extIntent = new Intent();
        String extPackage;

        defvar_tutor  tutorDescriptor = tutorVariants.get(tutorVariant);
        defdata_tutor tutorBinding    = bindingPatterns.get(tutorDescriptor.tutorName);
        Log.d(TAG,tutorDescriptor.tutorName);

        // Initialize the tutorBinding from the dataSource spec - this transfers the
        // datasource fields to the prototype tutorVariant bindingPattern which is then
        // used to initialize the tutor itself.
        //
        initializeBindingPattern(tutorBinding, dataSource);

        switch(intentType) {

            // Create a native tutor with the given base features
            // These features are used to determine basic tutor functionality when
            // multiple tutors share a single scenegraph
            //
            case "native":
                createAndLaunchTutor(tutorDescriptor.tutorName, tutorDescriptor.features, tutorId, tutorBinding, matrix);
                break;

            case "browser":

                extIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("file:///" + tutorVariant));

                getActivity().startActivity(extIntent);

                break;

            default:

                // This a special allowance for MARi which placed their activities in a different
                // package from their app - so we check for intent of the form "<pkgPath>:<appPath>"
                //
                String[] intentParts = tutorVariant.split(":");

                // If it is "<pkgPath>:<appPath>"
                //
                if(intentParts.length > 1) {
                    extPackage = intentParts[0];
                    tutorVariant     = intentParts[1];
                }
                // Otherwise we expect the activities to be right off the package.
                //
                else {
                    extPackage = tutorVariant.substring(0, tutorVariant.lastIndexOf('.'));
                }

                extIntent.setClassName(extPackage, tutorVariant);
                extIntent.putExtra("intentdata", intentType);

                try {
                    getActivity().startActivity(extIntent);
                }
                catch(Exception e) {
                    Log.e(TAG, "Launch Error: " + e + " : " + tutorVariant);
                }
                break;
        }
    }


    //************ Serialization


    /**
     * Load the Tutor engine specification from JSON file data
     * from assets/tutors/engine_descriptor.json
     *
     */
    public void loadEngineDescr() {

        try {
            loadJSON(new JSONObject(JSON_Helper.cacheData(TCONST.TUTORROOT + "/" + TCONST.EDESC)), (IScope2)mRootScope);

            // TODO : Use build Variant to ensure release configurations
            //
            if(Configuration.languageOverride(getActivity())) {
                language = Configuration.getLanguageFeatureID(getActivity());
                //  any time the language changes, so should the Transition Matrix and the Student Model
            } else {
                language = Configuration.getLanguageFeatureID(getActivity());
                if (language.equals("LANG_NULL")) {
                    language = TCONST.LANG_SW;
                }
            }

            matrix = loadTransitionMatrixModel();
            studentModel = loadStudentModel(matrix);
            promotionMechanism = new PromotionMechanism(studentModel, matrix);
            menuType = getMenuType();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static MenuType getMenuType() {
        /*
            let the menu type fully depends on the user setting.
         */
        if (Configuration.getMenuType(getActivity()).equals("CD1")){
            return MenuType.STUDENT_CHOICE;
        } else {
            return MenuType.CYCLE_CONTENT;
        }
    }

    /**
     * Loads the student data model. If the student has not played before, initialize with
     * the default starting positions based on {@code matrix}
     *
     * @param matrix what the activity matrix looks like.
     * @return
     */
    private static IStudentDataModel loadStudentModel(TransitionMatrixModel matrix) {
        // initialize
        String prefsName = "";
        if(RoboTutor.STUDENT_ID != null) {
            prefsName += RoboTutor.STUDENT_ID + "_";
        }
        prefsName += CTutorEngine.language;

        IStudentDataModel model;
        try {
            Log.w(DEBUG_CSV, "new StudentDataModelCSV(" + RoboTutor.STUDENT_ID + ")");
            model = new StudentDataModelCSV(RoboTutor.STUDENT_ID);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            model = new StudentDataModelSharedPrefs(RoboTutor.ACTIVITY, prefsName);
        }


        // JUDITH - initialize update_intervention.csv
        CUpdateInterventionStudentData.writeNewStudent(RoboTutor.STUDENT_ID);

        // if it's the first time playing, we want to initialize our placement values
        String firstTime = model.getHasPlayed();
        if (firstTime == null || !firstTime.equalsIgnoreCase("TRUE")) {
            model.createNewStudent();
            model.initializeTutorPositions(matrix);
        }

        return model;
    }

    /**
     *  load the transition matrix model from JSON
     * @return
     */
    private static TransitionMatrixModel loadTransitionMatrixModel() {

        // this is whack and should be moved... see "activity_selector/tutor_descriptor.json"
        String tutorName = "activity_selector";
        //String dataFile = "dev_data.json";
        String dataFile = RoboTutor.MATRIX_FILE;

        // simpler way to refer to languge
        String lang = TCONST.langMap.get(CTutorEngine.language);

        String dataPath = TCONST.TUTORROOT + "/" + tutorName + "/" + TCONST.TASSETS;
        dataPath += "/" +  TCONST.DATA_PATH + "/" + lang + "/";

        String jsonData = JSON_Helper.cacheData(dataPath + dataFile);

        //
        // Load the datasource into a separate class...
        TransitionMatrixModel matrix = new TransitionMatrixModel(dataPath + dataFile, mRootScope);
        matrix.validateAll();
        return matrix;
    }

    private static void getArm() {
        String tutorName = "activity_selector";
        String dataPath = TCONST.TUTORROOT + "/" + tutorName;
        String dataFile = RoboTutor.ARM_WEIGHTS_FILE;

        MABHandler.getArm(dataPath + "/" + dataFile, mRootScope);
    }


    /**
     * Load the Tutor specification from JSON file data
     *
     * @param jsonData
     */
    @Override
    public void loadJSON(JSONObject jsonData, IScope2 scope) {

      JSON_Helper.parseSelf(jsonData, this, CClassMap2.classMap, scope);
    }
    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        // Log.d(TAG, "Loader iteration");
        loadJSON(jsonObj, (IScope2) scope);
    }

}
