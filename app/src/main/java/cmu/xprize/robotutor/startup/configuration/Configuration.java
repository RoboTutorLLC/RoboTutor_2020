package cmu.xprize.robotutor.startup.configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

import cmu.xprize.comp_logging.CLogManager;

import static android.content.Context.MODE_PRIVATE;

public class Configuration {

    private static final String ROBOTUTOR_CONFIGURATION = "ROBOTUTOR_CONFIGURATION";

    public static void saveConfigurationItems(Context context, ConfigurationItems configItems) {
        SharedPreferences prefs = context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE);
        prefs.edit()
                .putString(ConfigurationItems.CONFIG_VERSION, configItems.config_version)
                .putBoolean(ConfigurationItems.LANGUAGE_OVERRIDE, configItems.language_override)
                .putBoolean(ConfigurationItems.SHOW_TUTOR_VERSION, configItems.show_tutorversion)
                .putBoolean(ConfigurationItems.SHOW_DEBUG_LAUNCHER, configItems.show_debug_launcher)
                .putBoolean(ConfigurationItems.LANGUAGE_SWITCHER, configItems.language_switcher)
                .putBoolean(ConfigurationItems.NO_ASR_APPS, configItems.no_asr_apps)
                .putString(ConfigurationItems.LANGUAGE_FEATURE_ID, configItems.language_feature_id)
                .putBoolean(ConfigurationItems.SHOW_DEMO_VIDS, configItems.show_demo_vids)
                .putBoolean(ConfigurationItems.USE_PLACEMENT, configItems.use_placement)
                .putBoolean(ConfigurationItems.RECORD_AUDIO, configItems.record_audio)
                .putString(ConfigurationItems.MENU_TYPE, configItems.menu_type)
<<<<<<< HEAD
                .putBoolean(ConfigurationItems.CONTENT_CREATION_MODE, configItems.content_creation_mode)
=======
>>>>>>> development
                .putBoolean(ConfigurationItems.SHOW_HELPER_BUTTON, configItems.show_helper_button)
                .putBoolean(ConfigurationItems.RECORD_SCREEN_VIDEO, configItems.record_screen_video)
                .putString(ConfigurationItems.BASE_DIRECTORY, configItems.baseDirectory)
                .putBoolean(ConfigurationItems.INCLUDE_AUDIO_OUTPUT_IN_SCREEN_VIDEO, configItems.include_audio_output_in_screen_video)
                .putBoolean(ConfigurationItems.PINNING_MODE, configItems.pinning_mode)
                .apply();
    }

    public static String configVersion(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getString(ConfigurationItems.CONFIG_VERSION, "release_sw");
    }

    public static boolean languageOverride(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.LANGUAGE_OVERRIDE, true);
    }

    public static boolean showTutorVersion(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.SHOW_TUTOR_VERSION, true);
    }

    public static boolean showDebugLauncher(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.SHOW_DEBUG_LAUNCHER, false);
    }

    public static boolean getLanguageSwitcher(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.LANGUAGE_SWITCHER, false);
    }

    public static boolean noAsrApps(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.NO_ASR_APPS, false);
    }

    public static String getLanguageFeatureID(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getString(ConfigurationItems.LANGUAGE_FEATURE_ID, "LANG_SW");
    }

    public static boolean showDemoVids(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.SHOW_DEMO_VIDS, true);
    }

    public static boolean usePlacement(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.USE_PLACEMENT, true);
    }

    public static boolean recordAudio(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.RECORD_AUDIO, false);
    }

    public static String getMenuType(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getString(ConfigurationItems.MENU_TYPE, "CD1");
    }

    public static boolean getContentCreationMode(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.CONTENT_CREATION_MODE, false);
    }
    
    public static String getBaseDirectory(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getString(ConfigurationItems.BASE_DIRECTORY, "roboscreen");
    }

    public static boolean getRecordScreenVideo(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.RECORD_SCREEN_VIDEO, true);
    }

    public static boolean getIncludeAudioOutputInScreenVideo(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.INCLUDE_AUDIO_OUTPUT_IN_SCREEN_VIDEO, false);
    }

    public static boolean getShowHelperButton(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.SHOW_HELPER_BUTTON, false);
    }

    public static boolean getPinningMode(Context context) {
        return context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE)
                .getBoolean(ConfigurationItems.PINNING_MODE, false);
    }

    /**
     * logs all the config items.
     */
    public static void logConfigurationItems(Context context) {
        StringBuilder config = new StringBuilder();
        Map<String, ?> allConfig = context.getSharedPreferences(ROBOTUTOR_CONFIGURATION, MODE_PRIVATE).getAll();
        for (Map.Entry<String, ?> entry : allConfig.entrySet()) {
            config.append(entry.getKey().toLowerCase()).append(":");
            config.append(entry.getValue().toString());
            config.append(",");
        }
        config.deleteCharAt(config.length()-1);
        Log.e(ROBOTUTOR_CONFIGURATION, config.toString());
        CLogManager.getInstance().postEvent_I(ROBOTUTOR_CONFIGURATION, config.toString());
    }
}
