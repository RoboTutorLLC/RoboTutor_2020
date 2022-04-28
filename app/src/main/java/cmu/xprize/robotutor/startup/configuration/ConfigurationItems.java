package cmu.xprize.robotutor.startup.configuration;

import android.util.Log;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;
import cmu.xprize.util.TCONST;

public class ConfigurationItems implements ILoadableObject {

    private static final String TAG = "ConfigurationItems";

    public static final String CONFIG_VERSION = "CONFIG_VERSION";
    public static final String LANGUAGE_OVERRIDE = "LANGUAGE_OVERRIDE";
    public static final String SHOW_TUTOR_VERSION = "SHOW_TUTOR_VERSION";
    public static final String SHOW_DEBUG_LAUNCHER = "SHOW_DEBUG_LAUNCHER";
    public static final String LANGUAGE_SWITCHER = "LANGUAGE_SWITCHER";
    public static final String NO_ASR_APPS = "NO_ASR_APPS";
    public static final String LANGUAGE_FEATURE_ID = "LANGUAGE_FEATURE_ID";
    public static final String SHOW_DEMO_VIDS = "SHOW_DEMO_VIDS";
    public static final String USE_PLACEMENT = "USE_PLACEMENT";
    public static final String RECORD_AUDIO = "RECORD_AUDIO";
    public static final String MENU_TYPE = "MENU_TYPE";
    public static final String RECORD_SCREEN_VIDEO = "RECORD_SCREEN_VIDEO";
    public static final String INCLUDE_AUDIO_OUTPUT_IN_SCREEN_VIDEO = "INCLUDE_AUDIO_OUTPUT_IN_SCREEN_VIDEO";
    public static final String SHOW_HELPER_BUTTON = "SHOW_HELPER_BUTTON";
    public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
    public static final String PINNING_MODE = "PINNING_MODE";
    public static final String STORY_NSP_MODE = "STORY_NSP_MODE";
    public static final String NSP_CHOICE_PROBABILITIES = "NSP_CHOICE_PROBABILITIES";

    public String config_version;
    public boolean language_override;
    public boolean show_tutorversion;
    public boolean show_debug_launcher;
    public boolean language_switcher;
    public boolean no_asr_apps;
    public String language_feature_id;
    public boolean show_demo_vids;
    public boolean use_placement;
    public boolean record_audio;
    public String menu_type;
    public boolean record_screen_video;
    public boolean show_helper_button;
    public String baseDirectory;
    public boolean include_audio_output_in_screen_video;
    public boolean pinning_mode;
//    public String story_nsp_mode;
//    public String[] nsp_choice_probabilities;

    public ConfigurationItems() {
        String dataPath = TCONST.DOWNLOAD_PATH + "/config.json";
        String jsonData = JSON_Helper.cacheDataByName(dataPath);

        try {
            loadJSON(new JSONObject(jsonData), null);
            /*
            The JSON object is logged here to make the app logs more identifiable
            and searchable.
            */
            Log.i(TAG, new JSONObject(jsonData).toString(4));
            this.setConfigVersion();
        } catch (Exception e) {
            Log.e(TAG, "Invalid Data Source for : " + dataPath, e);
            setDefaults();
        }
    }

    // used for QuickOptions
    public ConfigurationItems(String config_version, boolean language_override,
                              boolean show_tutorversion, boolean show_debug_launcher,
                              boolean language_switcher, boolean no_asr_apps,
                              String language_feature_id, boolean show_demo_vids,
                              boolean use_placement, boolean record_audio,
                              String menu_type, boolean record_screen_video, boolean include_audio_output_in_screen_video,
                              boolean show_helper_button, String baseDirectory, boolean pinning_mode) {
//, String story_nsp_mode, String[] nsp_choice_probabilities
//        this.config_version = config_version;
        this.setConfigVersion();
        this.language_override = language_override;
        this.show_tutorversion = show_tutorversion;
        this.show_debug_launcher = show_debug_launcher;
        this.language_switcher = language_switcher;
        this.no_asr_apps = no_asr_apps;
        this.language_feature_id = language_feature_id;
        this.show_demo_vids = show_demo_vids;
        this.use_placement = use_placement;
        this.record_audio = record_audio;
        this.menu_type = menu_type;
        this.record_screen_video = record_screen_video;
        this.include_audio_output_in_screen_video = include_audio_output_in_screen_video;
        this.show_helper_button = show_helper_button;
        this.baseDirectory = baseDirectory;
        this.pinning_mode = pinning_mode;
//        this.story_nsp_mode = story_nsp_mode;
//        this.nsp_choice_probabilities = nsp_choice_probabilities;
    }

    public void setDefaults() {
        // use the swahili versions as default
//        config_version = "release_sw"; // shouldn't it be fttt...?
        this.setConfigVersion();
        language_override = true;
        show_tutorversion = true;
        show_debug_launcher = false;
        language_switcher = false;
        no_asr_apps = false;
        language_feature_id = "LANG_SW";
        show_demo_vids = true;
        use_placement = true;
        record_audio = false;
        menu_type = "CD1";
        show_helper_button = false;
        baseDirectory = "roboscreen";
        record_screen_video = true;
        include_audio_output_in_screen_video = false;
        pinning_mode = false;
//        story_nsp_mode = "TF";
//        nsp_choice_probabilities = new String[] {"correct", "easy", "random"};
    }

    private void setConfigVersion() {
        String dataPath = TCONST.DOWNLOAD_PATH + "/config.json";
        String jsonData = JSON_Helper.cacheDataByName(dataPath);
        String configAcronym = JSON_Helper.createValueAcronym(jsonData);
        this.config_version = configAcronym;
    }

    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, ConfigurationClassMap.classMap, scope);
    }
}
