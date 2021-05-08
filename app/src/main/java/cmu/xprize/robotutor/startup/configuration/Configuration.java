package cmu.xprize.robotutor.startup.configuration;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static void logConfigurationItems(Context context) {
        String config = "\n" + ConfigurationItems.CONFIG_VERSION + " - " + configVersion(context) + "\n" +
                ConfigurationItems.LANGUAGE_OVERRIDE + " - " + languageOverride(context) + "\n" +
                ConfigurationItems.SHOW_TUTOR_VERSION + " - " + showTutorVersion(context) + "\n" +
                ConfigurationItems.SHOW_DEBUG_LAUNCHER + " - " + showDebugLauncher(context) + "\n" +
                ConfigurationItems.LANGUAGE_SWITCHER + " - " + getLanguageSwitcher(context) + "\n" +
                ConfigurationItems.NO_ASR_APPS + " - " + noAsrApps(context) + "\n" +
                ConfigurationItems.LANGUAGE_FEATURE_ID + " - " + getLanguageFeatureID(context) + "\n" +
                ConfigurationItems.SHOW_DEMO_VIDS + " - " + showDemoVids(context) + "\n" +
                ConfigurationItems.USE_PLACEMENT + " - " + usePlacement(context) + "\n" +
                ConfigurationItems.RECORD_AUDIO + " - " + recordAudio(context) + "\n" +
                ConfigurationItems.MENU_TYPE + " - " + getMenuType(context);
        CLogManager.getInstance().postEvent_I(ROBOTUTOR_CONFIGURATION, config);
    }
}
