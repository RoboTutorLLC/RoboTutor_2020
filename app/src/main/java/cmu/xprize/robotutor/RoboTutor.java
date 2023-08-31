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

package cmu.xprize.robotutor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.RequiresApi;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

//import com.nanchen.screenrecordhelper.ScreenRecordHelper;
//import com.RoboTutorLLC.ScreenRecordHelper.ScreenrecordHelper;
import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;
import com.nanchen.screenrecordhelper.ScreenRecordHelper;
//com.github.RoboTutorLLC:ScreenRecordHelper

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import cmu.xprize.comp_intervention.data.CInterventionStudentData;
import cmu.xprize.comp_intervention.CInterventionTimes;
import cmu.xprize.comp_intervention.data.CUpdateInterventionStudentData;
import cmu.xprize.comp_intervention.data.Student;
import cmu.xprize.comp_logging.CErrorManager;
import cmu.xprize.comp_logging.CInterventionLogManager;
import cmu.xprize.comp_logging.CLogManager;
import cmu.xprize.comp_logging.CPerfLogManager;
import cmu.xprize.comp_logging.CPreferenceCache;
import cmu.xprize.comp_logging.ILogManager;
import cmu.xprize.comp_logging.IPerfLogManager;
import cmu.xprize.ltkplus.CRecognizerPlus;
import cmu.xprize.ltkplus.GCONST;
import cmu.xprize.ltkplus.IGlyphSink;
import cmu.xprize.robotutor.startup.CStartView;
import cmu.xprize.util.configuration.Configuration;
import cmu.xprize.util.configuration.ConfigurationItems;
import cmu.xprize.util.configuration.ConfigurationQuickOptions;
import cmu.xprize.robotutor.tutorengine.CMediaController;
import cmu.xprize.robotutor.tutorengine.CTutorAssetManager;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.robotutor.tutorengine.ITutorManager;
import cmu.xprize.robotutor.tutorengine.QuickDebugTutor;
import cmu.xprize.robotutor.tutorengine.QuickDebugTutorList;
import cmu.xprize.robotutor.tutorengine.util.CAssetObject;
import cmu.xprize.robotutor.tutorengine.util.CrashHandler;
import cmu.xprize.robotutor.tutorengine.widgets.core.IGuidView;
import cmu.xprize.util.CDisplayMetrics;
import cmu.xprize.util.CLoaderView;
import cmu.xprize.util.IReadyListener;
import cmu.xprize.util.IRoboTutor;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;
import cmu.xprize.util.TTSsynthesizer;
import edu.cmu.xprize.listener.ListenerBase;

import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.LITERACY_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.MATH_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.SONGS_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.STORIES_MATRIX;
import static cmu.xprize.comp_logging.PerformanceLogItem.MATRIX_TYPE.UNKNOWN_MATRIX;
import static cmu.xprize.robotutor.tutorengine.QuickDebugTutorList.INTERVENTION_BPOP;
import static cmu.xprize.util.TCONST.ENGLISH_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.GRAPH_MSG;
import static cmu.xprize.util.TCONST.INTERVENTION_STUDENT_FILE;
import static cmu.xprize.util.TCONST.INTERVENTION_TIMES_FILE;
import static cmu.xprize.util.TCONST.MATH_PLACEMENT;
import static cmu.xprize.util.TCONST.SWAHILI_ASSET_PATTERN;
import static cmu.xprize.util.TCONST.UPDATE_INTERVENTION_FILE;
import static cmu.xprize.util.TCONST.WRITING_PLACEMENT;
import static com.hbisoft.hbrecorder.Constants.MAX_FILE_SIZE_REACHED_ERROR;
import static com.hbisoft.hbrecorder.Constants.SETTINGS_ERROR;


/**
 * <h2>Class Overview</h2>
 * <hr>
 * This class represents the root activity for a Tutor Manager that can display one of many
 * instructional tutors.  Tutors may also link to other Activities that themselves represent
 * Tutor Managers and can vector to specific tutors contained therein.
 * <br>
 * <h3>Developer Overview</h3>
 *
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RoboTutor extends Activity implements IReadyListener, IRoboTutor, HBRecorderListener {


    // DEVELOPER VARIABLES FOR QUICK DEBUG LAUNCH
    private static final boolean QUICK_DEBUG_TUTOR = false;
    private static final String QUICK_DEBUG_TUTOR_KEY = INTERVENTION_BPOP;
    public HBRecorder hbRecorder;
    // for devs, this is faster than changing the config file
    private static final boolean QUICK_DEBUG_CONFIG = false;
    private static final ConfigurationItems QUICK_DEBUG_CONFIG_OPTION = ConfigurationQuickOptions.DEBUG_EN;

    public static final String MATRIX_FILE = "dev_data.open.json";
    public static final String ARM_WEIGHTS_FILE = "arm-weights.json";

    private static final String LOG_SEQUENCE_ID = "LOG_SEQUENCE_ID";

    // deprecated variable , see issue #427
    public static final boolean OLD_MENU = true;
    public static final int REQUEST_CODE = 1024;


    private CMediaController    mMediaController;

    private CLoaderView         progressView;
    private CStartView          startView;

    public TTSsynthesizer       TTS = null;
    public ListenerBase         ASR;
    public IGlyphSink           LTKPlus = null;

    static public ITutorManager masterContainer;
    static public ILogManager   logManager;
    static public IPerfLogManager perfLogManager;

    static CTutorAssetManager   tutorAssetManager;
    static public String        VERSION_RT;
    static public ArrayList     VERSION_SPEC;

    static public CDisplayMetrics displayMetrics;

    static public String        APP_PRIVATE_FILES;
    static public String        LOG_ID = "STARTUP";

    static public RoboTutor      ACTIVITY;
    static public String        PACKAGE_NAME;
    static public boolean       DELETE_INSTALLED_ASSETS = false;

    static public String        STUDENT_ID; // received from FaceLogin
    static public Student       STUDENT_INTERVENTION_PROFILE;
    static public String        SESSION_ID; // received from FaceLogin
    static public String        SEQUENCE_ID_STRING;

    final static public  String CacheSource = TCONST.ASSETS;                // assets or extern

    private boolean                 isReady       = false;
    private boolean                 engineStarted = false;
    static public boolean           STANDALONE    = false;
    static public String            SELECTOR_MODE = TCONST.FTR_TUTOR_SELECT; // this is only used as a feature, when launching TActivitySelector...
    static public boolean           STUDENT_CHOSE_REPEAT = false;
//    static public String        SELECTOR_MODE = TCONST.FTR_DEBUG_SELECT;

    static private String[] videoNames = new String[]{"video1", "video2"};
    private int videoNamesIterator = 0;

    // TODO: This is a temporary log update mechanism - see below
    //
    static private IGuidView    guidCallBack;

    String hotLogPath;
    String hotLogPathPerf;
    String readyLogPath;
    String readyLogPathPerf;
    String audioLogPath;
    String interventionLogPath;
    public final static String  DOWNLOAD_PATH  = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS;
    public final static String  EXT_ASSET_PATH = Environment.getExternalStorageDirectory() + File.separator + TCONST.ROBOTUTOR_ASSET_FOLDER;

    private final  String  TAG = "CRoboTutor";
    private final String ID_TAG = "StudentId";
    ScreenRecordHelper screenRecordHelper;
    private ScreenRecorder screenRecorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Note = we don't want the system to try and recreate any of our views- always pass null
        //
        super.onCreate(null);

        APP_PRIVATE_FILES = getApplicationContext().getExternalFilesDir("").getPath();

        // Initialize the JSON Helper STATICS - just throw away the object.
        //
        new JSON_Helper(getAssets(), CacheSource, RoboTutor.APP_PRIVATE_FILES);

        // Gives the dev the option to override the stored config file.
        ConfigurationItems configurationItems = QUICK_DEBUG_CONFIG ? QUICK_DEBUG_CONFIG_OPTION : new ConfigurationItems(); // OPEN_SOURCE opt to switch here.
        Configuration.saveConfigurationItems(this, configurationItems);

        // Catch all errors and cause a clean exit -
        // TODO: this doesn't work as expected
        //

        ACTIVITY     = this;
        PACKAGE_NAME = getApplicationContext().getPackageName();

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(hotLogPath, ACTIVITY));




        // Prep the CPreferenceCache
        // Update the globally accessible id object for this engine instance.
        //
        LOG_ID = CPreferenceCache.initLogPreference(this);

        // RoboTutor Version spec - positional element meaning 0.1.2.3
        // Given 4.23.2.3
        // Major release 4 | Feature release 23 | Fix release 2 | compatible Asset Version 3
        //
        tutorAssetManager = new CTutorAssetManager(getApplicationContext());

        VERSION_RT   = BuildConfig.VERSION_NAME;
        VERSION_SPEC = CAssetObject.parseVersionSpec(VERSION_RT);

        initializeAndStartLogs();

        //Log current config data
        //
        Configuration.logConfigurationItems(this);

        Log.v(TAG, "External_Download:" + DOWNLOAD_PATH);

        // Get the primary container for tutors
        //
        setContentView(R.layout.robo_tutor);
        masterContainer = (ITutorManager)findViewById(R.id.master_container);
        hbRecorder = new HBRecorder(this, this);
        // Set fullscreen and then get the screen metrics
        //
        setFullScreen();

        // get the multiplier used for drawables at the current screen density and calc the
        // correction rescale factor for design scale
        // This initializes the static object
        //
        displayMetrics = CDisplayMetrics.getInstance(this);

        // Initialize the media manager singleton - it needs access to the App assets.
        //
        mMediaController = CMediaController.getInstance();
        AssetManager mAssetManager = getApplicationContext().getAssets();
        mMediaController.setAssetManager(mAssetManager);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Create the start dialog
        // TODO: This is a temporary log update mechanism - see below
        //
        startView = (CStartView)inflater.inflate(R.layout.start_layout, null );
        startView.setCallback(this);

        // Show the Indeterminate loader
        //
        progressView = (CLoaderView)inflater.inflate(R.layout.progress_layout, null );

        masterContainer.addAndShow(progressView);

        // testCrashHandler();
        // if we are recording whole session start recording here
        String session_or_activity=Configuration.getRecordingSessionOrActivity(getApplicationContext());
        Log.i("ConfigurationItems", "Inside App, session or activity flag is:"+session_or_activity);
        if(Objects.equals(session_or_activity, "session")) {
            startRecordingScreen();
        }
        //startRecordingScreen();


    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startRecordingScreen() {
        hbRecorder.enableCustomSettings();
        customSettings();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, 777);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == 777) {
                if (resultCode == RESULT_OK) {
                    //Set file path or Uri depending on SDK version
                    setOutputPath();
                    //Start screen recording
                    hbRecorder.startScreenRecording(data, resultCode, this);

                }
            }
        }
    }

    /**
     * create log paths.
     * initialize times and other IDs
     * start logging
     */
    private void initializeAndStartLogs() {

        hotLogPath   = Environment.getExternalStorageDirectory() + TCONST.HOT_LOG_FOLDER;
        readyLogPath = Environment.getExternalStorageDirectory() + TCONST.READY_LOG_FOLDER;

        hotLogPathPerf = Environment.getExternalStorageDirectory() + TCONST.HOT_LOG_FOLDER_PERF;
        readyLogPathPerf = Environment.getExternalStorageDirectory() + TCONST.READY_LOG_FOLDER_PERF;

        audioLogPath = Environment.getExternalStorageDirectory() + TCONST.AUDIO_LOG_FOLDER;

        interventionLogPath = Environment.getExternalStorageDirectory() + TCONST.INTERVENTION_LOG_FOLDER;

        Calendar calendar = Calendar.getInstance(Locale.US);
        String initTime     = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(calendar.getTime());
        SEQUENCE_ID_STRING = String.format(Locale.US, "%06d", getNextLogSequenceId());


        String logFilename  = "RoboTutor_" + // TODO TODO TODO there should be a version name in here!!!
                Configuration.configVersion(this) + "_" + BuildConfig.VERSION_NAME + "_" + SEQUENCE_ID_STRING +
                "_" + initTime + "_" + Build.SERIAL;

        Log.w("LOG_DEBUG", "Beginning new session with LOG_FILENAME = " + logFilename);

        logManager = CLogManager.getInstance();
        logManager.transferHotLogs(hotLogPath, readyLogPath);
        logManager.transferHotLogs(hotLogPathPerf, readyLogPathPerf);

        logManager.startLogging(hotLogPath, logFilename);
        CErrorManager.setLogManager(logManager);

        perfLogManager = CPerfLogManager.getInstance();
        perfLogManager.startLogging(hotLogPathPerf, "PERF_" + logFilename);

        CInterventionLogManager.getInstance().startLogging(interventionLogPath,
                "INT_" + logFilename);

        // TODO : implement time stamps
        logManager.postDateTimeStamp(GRAPH_MSG, "RoboTutor:SessionStart");
        logManager.postEvent_I(GRAPH_MSG, "EngineVersion:" + VERSION_RT);
    }

    /**
     * Load student intervention data
     */
    private void initalizeInterventionData() {
        // initialize InterventionTimes singleton (this is sort of like a read-only database)
        String interventionTimesFile = TCONST.INTERVENTION_FOLDER + File.separator + INTERVENTION_TIMES_FILE;
        CInterventionTimes.initialize(interventionTimesFile);

        // initialize singleton
        String interventionStudentFile = TCONST.INTERVENTION_FOLDER + File.separator + INTERVENTION_STUDENT_FILE;
        CInterventionStudentData.initialize(interventionStudentFile);

        String updateInterventionFile = TCONST.INTERVENTION_FOLDER + File.separator + UPDATE_INTERVENTION_FILE;
        CUpdateInterventionStudentData.initialize(updateInterventionFile);

        Log.wtf("MY_ID", "STUDENT_ID = " + RoboTutor.STUDENT_ID);
        Student me = CInterventionStudentData.getStudentById(RoboTutor.STUDENT_ID);
        STUDENT_INTERVENTION_PROFILE = me;
        CInterventionStudentData.setCurrentStudentId(RoboTutor.STUDENT_ID);
        Log.wtf("MY_ID", me != null ? me.toString() : "null");
    }


    /**
     * just a fun little method that will throw a null handler exception (when on the right screen)
     */
    private void testCrashHandler() {

        TextView x = findViewById(R.id.SBPopWords);
        x.setText("AYY LMAO");
    }
    /**
     * This file gets the Extras that are passed from FaceLogin and uses them to set the uniqueIDs,
     * SessionID and StudentID
     *
     */
    private void setUniqueIdentifiers() {
        String BUNDLE_TAG = "BUNDLE";

        logManager.postEvent_I(ID_TAG, "RoboTutor:setUniqueIdentifiers");



        if(getIntent() != null && getIntent().getExtras() != null) {

            for (String key : getIntent().getExtras().keySet()) {
                Log.i(BUNDLE_TAG, "INTENT_KEY_FOUND: " + key + " -- " + getIntent().getExtras().get(key));
            }

            STUDENT_ID = getIntent().getExtras().getString(TCONST.STUDENT_ID_VAR);

            if(STUDENT_ID != null) {
                Log.i(BUNDLE_TAG, "studentId passed! " + STUDENT_ID);
                logManager.postEvent_I(ID_TAG, "StudentID:" + STUDENT_ID);
            } else {
                logManager.postEvent_I(ID_TAG, "NoStudentFound:settingDefault");
                STUDENT_ID = TCONST.DEFAULT_STUDENT_ID;
                logManager.postEvent_I(ID_TAG, "StudentID:" + STUDENT_ID);
            }

            SESSION_ID = getIntent().getExtras().getString(TCONST.SESSION_ID_VAR);


        } else {
            Log.w(BUNDLE_TAG, "no extras passed!");
            logManager.postEvent_I(ID_TAG, "NoStudentFound:settingDefault");
            STUDENT_ID = TCONST.DEFAULT_STUDENT_ID;
            logManager.postEvent_I(ID_TAG, "StudentID:" + STUDENT_ID);

        }
    }


    /**
     * Ignore the state bundle
     *
     * @param bundle
     */
    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        //super.onRestoreInstanceState(bundle);
        logManager.postEvent_V(TAG, "RoboTutor:onRestoreInstanceState");
    }


    public void reBoot() {

        try {

            Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "reboot" });
            proc.waitFor();

        } catch (Exception ex) {

            logManager.postEvent_V(TAG, "RoboTutor:Could not reboot");
        }

    }

    private void setFullScreen() {

        ((View) masterContainer).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        boolean result = super.dispatchTouchEvent(event);

        switch (event.getAction()) {

            case MotionEvent.ACTION_UP:
                logManager.postEvent_V(TAG, "RT_SCREEN_RELEASE: X:" + event.getX() + "  Y:" + event.getY());
                break;

            case MotionEvent.ACTION_MOVE:
                logManager.postEvent_V(TAG, "RT_SCREEN_MOVE X:" + event.getX() + "  Y:" + event.getY());
                break;

            case MotionEvent.ACTION_DOWN:
                logManager.postEvent_V(TAG, "RT_SCREEN_TOUCH X:" + event.getX() + "  Y:" + event.getY());
                break;
        }

        // Manage system levelFolder timeout here

        return result;
    }
    ContentResolver resolver;
    ContentValues contentValues;
    Uri mUri;
    @Override
    public void HBRecorderOnStart() {
        Log.d("HBRecorder","HBRecorder Recording Started");
    }

    @Override
    public void HBRecorderOnComplete() {
        Toast.makeText(getApplicationContext(),"Recording Saved Successfully", Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Update gallery depending on SDK Level
            if (hbRecorder.wasUriSet()) {
                if (Build.VERSION.SDK_INT >= 29 ) {
                    updateGalleryUri();
                } else {
                    refreshGalleryFile();
                }
            }else{
                refreshGalleryFile();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void refreshGalleryFile() {
        MediaScannerConnection.scanFile(this,
                new String[]{hbRecorder.getFilePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    @RequiresApi(api = 29)
    private void updateGalleryUri(){
        contentValues.clear();
        //contentValues.put(MediaStore.Video.Media.IS_PRIVATE, 0);
        getContentResolver().update(mUri, contentValues, null, null);
    }

    @Override
    public void HBRecorderOnError(int errorCode, String reason) {
        // Error 38 happens when
        // - the selected video encoder is not supported
        // - the output format is not supported
        // - if another app is using the microphone

        //It is best to use device default

        if (errorCode == SETTINGS_ERROR) {
            Toast.makeText(getApplicationContext(),"Settings not Supported", Toast.LENGTH_LONG).show();
        } else if ( errorCode == MAX_FILE_SIZE_REACHED_ERROR) {
            Toast.makeText(getApplicationContext(),"Max file size reached", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),"General Recording Error", Toast.LENGTH_LONG).show();
            Log.e("HBRecorderOnError", reason);
        }


    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void customSettings() {
        int audio_bitrate = Configuration.getRecordingAudioBitrate(getApplicationContext());
        int audio_sampling_rate = Configuration.getRecordingAudioSamplingRate(getApplicationContext());
        int fps = Configuration.getRecordingFPS(getApplicationContext());
        int width = Configuration.getRecordingPixelsWide(getApplicationContext());
        int height = Configuration.getRecordingPixelsHigh(getApplicationContext());

        Log.d("hbrecorder","audio bitrate is "+audio_bitrate);
        Log.d("hbrecorder","audio sampling rate is "+audio_sampling_rate);
        Log.d("hbrecorder","FPS is "+fps);
        Log.d("hbrecorder","Screen Dimensions are "+width +" * "+ height);

        hbRecorder.setAudioBitrate(audio_bitrate);
        hbRecorder.setAudioSamplingRate(audio_sampling_rate);
        hbRecorder.recordHDVideo(false);
        hbRecorder.isAudioEnabled(true);
        hbRecorder.setScreenDimensions(height,width);
        hbRecorder.setVideoFrameRate(fps);

        //Customise Notification
//        hbRecorder.setNotificationSmallIcon(R.drawable.icon);
//        hbRecorder.setNotificationTitle(getString(R.string.stop_recording_notification_title));
//        hbRecorder.setNotificationDescription(getString(R.string.stop_recording_notification_message));
    }
    private void createFolder() {
        File f1 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), Configuration.getBaseDirectory(getApplicationContext()));
        if (!f1.exists()) {
            if (f1.mkdirs()) {
                Log.i("Folder ", "created");
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setOutputPath() {
        String filename = "Robotutor Log Video "+STUDENT_ID+" "+generateFileName();
        if (Build.VERSION.SDK_INT >= 29) {
            resolver = getContentResolver();
            contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + Configuration.getBaseDirectory(getApplicationContext()));
            contentValues.put(MediaStore.Video.Media.TITLE, filename);
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            mUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            //FILE NAME SHOULD BE THE SAME
            hbRecorder.setFileName(filename);
            hbRecorder.setOutputUri(mUri);
        }else{
            createFolder();
            hbRecorder.setOutputPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) +"/"+Configuration.getBaseDirectory(getApplicationContext()));
        }
    }

    //Generate a timestamp to be used as a file name
    private String generateFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate).replace(" ", "");
    }
    /**
     * Moves new assets to an external storyFolder so the Sphinx code can access it.
     *
     */
    class tutorConfigTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... unused) {

            boolean result = false;

            try {
                // TODO: Don't do this in production
                // At the moment we always reinstall the tutor spec data - for


                if(CacheSource.equals(TCONST.EXTERN)) {
                    tutorAssetManager.installAssets(TCONST.TUTORROOT);
                    logManager.postEvent_V(TAG, "INFO:Tutor Assets installed");
                }

                if(!tutorAssetManager.fileCheck(TCONST.LTK_PROJECT_ASSETS) ||
                        tutorAssetManager.fileIsStale(TCONST.LTK_PROJEXCTS, TCONST.LTK_PROJECT_ASSETS)) {
                    tutorAssetManager.installAssets(TCONST.LTK_PROJEXCTS);
                    logManager.postEvent_V(TAG, "INFO:LTK Projects installed");

                    // Note the Projects Zip file is anticipated to contain a storyFolder called "projects"
                    // containing the ltk data - this is unpacked to RoboTutor.APP_PRIVATE_FILES + TCONST.LTK_DATA_FOLDER
                    //
                    tutorAssetManager.extractAsset(TCONST.LTK_PROJEXCTS, TCONST.LTK_DATA_FOLDER);
                    logManager.postEvent_V(TAG, "INFO:LTK Projects extracted");
                }

                if(!tutorAssetManager.fileCheck(TCONST.LTK_GLYPH_ASSETS) ||
                tutorAssetManager.fileIsStale(TCONST.LTK_GLYPHS, TCONST.LTK_GLYPH_ASSETS)) {
                    tutorAssetManager.installAssets(TCONST.LTK_GLYPHS);
                    logManager.postEvent_V(TAG, "INFO:LTK Glyphs installed");

                    // Note the Glyphs Zip file is anticipated to contain a storyFolder called "glyphs"
                    // containing the ltk glyph data - this is unpacked to RoboTutor.APP_PRIVATE_FILES + TCONST.LTK_DATA_FOLDER
                    //
                    tutorAssetManager.extractAsset(TCONST.LTK_GLYPHS, TCONST.LTK_DATA_FOLDER);
                    logManager.postEvent_V(TAG, "INFO:LTK Glyphs extracted");
                }

                // Find and install (move to ext_asset_path) any new or updated audio/story assets
                //
                tutorAssetManager.updateAssetPackages(SWAHILI_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH);
                tutorAssetManager.updateAssetPackages(ENGLISH_ASSET_PATTERN, RoboTutor.EXT_ASSET_PATH);

                // Create the one system levelFolder LTKPLUS recognizer
                //
                LTKPlus = CRecognizerPlus.getInstance();
                LTKPlus.initialize(getApplicationContext(), GCONST.ALPHABET);

                result = true;

            } catch (IOException e) {
                // TODO: Manage exceptions
                e.printStackTrace();
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            isReady = result;

            onServiceReady("ROOT", result ? 1 : 0);
        }
    }


    /**
     * Callback used by services to announce ready state
     * @param serviceName
     */
    @Override
    public void onServiceReady(String serviceName, int status) {

        logManager.postEvent_V(TAG, "onServiceReady:" + serviceName + "status:" + status);

        // As the services come online push a global reference to CTutor
        //
        switch(serviceName) {
            case TCONST.TTS:
                logManager.postEvent_V(TAG, "flite:attaching");

                mMediaController.setTTS(TTS);
                break;
        }


        // check whether TTS, ASR, and ROOT are ready before starting engine
        if((TTS != null && TTS.isReady()) && (ASR != null && ASR.isReady()) && isReady) {

            startEngine();
        }
    }


    /**
     * Start the tutor engine once everything is intialized.
     *
     * There are several async init tasks and they all call this when they're finished.
     * The last one ready passes all the tests and starts the engine.
     *
     * TODO: Manage initialization failures
     *
     */
    private void startEngine() {

        if(!engineStarted) {
            engineStarted = true;

            logManager.postEvent_V(TAG, "TutorEngine:Starting");

            // Delete the asset loader utility ASR object
            ASR = null;

            masterContainer.removeView(progressView);

            // Initialize the Engine - set the EXTERN File path for file installs
            // Load the default tutor defined in assets/tutors/engine_descriptor.json
            // TODO: Handle tutor creation failure
            //
            CTutorEngine.getTutorEngine(RoboTutor.this);

            // If running without built-in home screen add a start screen
            //
            if(STANDALONE) {

                // TODO: This is a temporary log update mechanism - see below
                //
                masterContainer.addAndShow(startView);
                startView.startTapTutor();
                setFullScreen();
            }
            // QUICK DEBUG LAUNCH
            else if (QUICK_DEBUG_TUTOR) {

                startQuickLaunch();

                // start whatever tutor we're debugging
            }
            // Otherwise go directly to the sessionManager
            //
            else {
                onStartTutor();
            }

        }
        // Note that it is possible for the masterContainer to be recreated without the
        // engine begin destroyed so we must maintain sync here.
        else {
            logManager.postEvent_V(TAG, "TutorEngine:Restarting");
        }

    }


    // TODO: This is a temporary log update mechanism - see below
    //
    static public void setGUIDCallBack(IGuidView callBack) {

        guidCallBack = callBack;
    }


    // TODO: This is a temporary log update mechanism - see below
    //
    public void onStartTutor() {

        logManager.postEvent_V(TAG, "LOG_GUID:" + LOG_ID );
        LOG_ID = CPreferenceCache.initLogPreference(this);

        CTutorEngine.startSessionManager();

        startView.stopTapTutor();
        masterContainer.removeView(startView);
        setFullScreen();

        // Disable screen sleep while in a session
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     *
     * This launches a new tutor immediately at startup. Useful for quick debugging.
     */
    private void startQuickLaunch() {
        logManager.postEvent_V(TAG, "LOG_GUID:" + LOG_ID );
        LOG_ID = CPreferenceCache.initLogPreference(this);

        QuickDebugTutor debugMe = QuickDebugTutorList.toFixBugMap.get(QUICK_DEBUG_TUTOR_KEY);
        // CTutorEngine.quickLaunch(debugTutorVariant, debugTutorId, debugTutorFile);
        CTutorEngine.quickLaunch(debugMe.tutorVariant, debugMe.tutorId, debugMe.tutorFile);

        startView.stopTapTutor();
        masterContainer.removeView(startView);
        setFullScreen();

        // Disable screen sleep while in a session
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /**
     * TODO: Manage the back button
     */
    @Override
    public void onBackPressed() {
        logManager.postEvent_V(TAG, "RoboTuTor:onBackPressed");

        CTutorEngine.killActiveTutor();

        // Allow the screen to sleep when not in a session
        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // TODO: This is a temporary log update mechanism - see below
        //
        masterContainer.addAndShow(startView);
        startView.startTapTutor();
        setFullScreen();
    }



    /***  State Management  ****************/


    /**
     *
     */
    @Override
    protected void onStart() {

        super.onStart();

        // On-Screen
        logManager.postEvent_V(TAG, "Robotutor:onStart");

        setUniqueIdentifiers();
        initalizeInterventionData();

        // We only want to run the engine start sequence once per onStart call
        //
        engineStarted = false;

        // Debug - determine platform dependent memory limit
        //
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memAvail       = am.getMemoryClass();

        logManager.postEvent_V(TAG, "AvailableMemory:" + memAvail);

        // Create the common TTS service
        // Async
        //
        if(TTS == null) {

            logManager.postEvent_V(TAG, "Creating:TTS");

            TTS = new TTSsynthesizer(this);
            TTS.initializeTTS(this);
        }

        // Create an inert listener for asset initialization only
        // Start the configListener async task to update the listener assets only if required.
        // This moves the listener assets to a local storyFolder where they are accessible by the
        // NDK code (PocketSphinx)
        //
        if(ASR == null) {

            logManager.postEvent_V(TAG, "Creating:ASR");

            ASR = new ListenerBase("configassets");
            ASR.configListener(this);
        }

        // Start the async task to initialize the tutor
        //
        new tutorConfigTask().execute();
    }


    /**
     *  requery DB Cursors here
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        logManager.postEvent_V(TAG, "RoboTutor:onRestart");
    }


    /**
     *  Deactivate DB Cursors here
     */
    @Override
    protected void onStop() {
        hbRecorder.stopScreenRecording();
        super.onStop();
        // Off-Screen
        logManager.postEvent_V(TAG, "Robotutor:onStop");

        // Need to do this before releasing TTS
        //
        CTutorEngine.killActiveTutor();

        if(TTS != null && TTS.isReady()) {

            logManager.postEvent_V(TAG, "flite:release");

            // TODO: This seems to cause a Flite internal problem???
            TTS.shutDown();
            TTS = null;
        }
    }


    /**
     * This callback is mostly used for saving any persistent state the activity is editing, to
     * present a "edit in place" model to the user and making sure nothing is lost if there are
     * not enough resources to start the new activity without first killing this one. This is also
     * a good place to do things like stop animations and other things that consume a noticeable
     * amount of CPU in order to make the switch to the next activity as fast as possible, or to
     * close resources that are exclusive access such as the camera.
     *
     */
    @Override
    protected void onPause() {

        super.onPause();
        logManager.postEvent_V(TAG, "RoboTutor:onPause");
    }


    /**
     *
     */
    @Override
    protected void onResume() {

        super.onResume();
        logManager.postEvent_V(TAG, "Robotutor:onResume");

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        String restoredText = prefs.getString("text", null);

        if (restoredText != null) {
        }

        if (Configuration.getPinningMode(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // start lock task mode if it's not already active
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            // ActivityManager.getLockTaskModeState api is not available in pre-M
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (!am.isInLockTaskMode()) {
                    startLockTask();
                }
            } else {
                if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                    startLockTask();
                }
            }
        }
    }


    /**
     * In general onSaveInstanceState(Bundle) is used to save per-instance state in the activity
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState (Bundle outState) {

        super.onSaveInstanceState(outState);
        logManager.postEvent_V(TAG, "Robotutor:onSaveInstanceState");

//        SharedPreferences prefs = RoboTutor.ACTIVITY.getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//
//        int assetFullOrdinal = prefs.getInt(assetName + TCONST.ASSET_RELEASE_VERSION, 0);
//        int assetIncrOrdinal = prefs.getInt(assetName + TCONST.ASSET_UPDATE_VERSION, 0);
//
//        editor.putInt(assetName + TCONST.ASSET_UPDATE_VERSION , mAssetObject.getVersionField(INDEX_UPDATE, TCONST.ASSET_UPDATE_VERSION));
//        editor.apply();
    }


    @Override
    protected void onDestroy() {
        hbRecorder.stopScreenRecording();
        logManager.postEvent_V(TAG, "RoboTutor:onDestroy");

        Log.v(TAG, "isfinishing:" + isFinishing());

        super.onDestroy();

        if(TTS != null) {
            logManager.postEvent_V(TAG, "flite:release");

            TTS.shutDown();
            TTS = null;
        }

        logManager.postDateTimeStamp(GRAPH_MSG, "RoboTutor:SessionEnd");
        logManager.stopLogging();
        perfLogManager.stopLogging();

        // after logging, transfer logs to READY folder
        logManager.transferHotLogs(hotLogPath, readyLogPath);
        logManager.transferHotLogs(hotLogPathPerf, readyLogPathPerf);

    }

    private int getNextLogSequenceId() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        // grab the current sequence id (the one we should use for this current run
        // of the app
        final int logSequenceId = prefs.getInt(LOG_SEQUENCE_ID, 0);

        // increase the log sequence id by 1 for the next usage
        prefs.edit()
                .putInt(LOG_SEQUENCE_ID, logSequenceId + 1)
                .apply();

        return logSequenceId;
    }

    /**
     * gets the stored data for each student based on STUDENT_ID.
     * YYY if this is a student's first time logging in, use PLACEMENT
     */
    public static SharedPreferences getStudentSharedPreferences() {
        // each ID name is composed of the STUDENT_ID plus the language i.e. EN or SW
        String prefsName = "";
        if(RoboTutor.STUDENT_ID != null) {
            prefsName += RoboTutor.STUDENT_ID + "_";
        }
        prefsName += CTutorEngine.language;

        //RoboTutor.logManager.postEvent_I(TAG, "Getting SharedPreferences: " + prefsName);
        return RoboTutor.ACTIVITY.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

    /**
     * Get the promotion mode the student is currently in
     * @param matrix
     * @return
     */
    public static String getPromotionMode(String matrix) {

        SharedPreferences prefs = getStudentSharedPreferences();

        boolean placement;
        switch (matrix) {
            case MATH_MATRIX:
                placement = prefs.getBoolean(MATH_PLACEMENT, true);
                break;

            case LITERACY_MATRIX:
                placement = prefs.getBoolean(WRITING_PLACEMENT, true);
                break;

            case STORIES_MATRIX:
            case UNKNOWN_MATRIX:
            case SONGS_MATRIX:
            default:
                placement = false;
        }


        return placement ? "PLACEMENT" : "PROMOTION";

    }
}
