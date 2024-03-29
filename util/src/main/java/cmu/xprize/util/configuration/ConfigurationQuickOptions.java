package cmu.xprize.util.configuration;

/**
 * RoboTutor
 * <p>Similar to QuickDebugTutor, this class offers the developer a way to store multiple pre-set configuration
 * option sets without changing the config file.</p>
 * Created by kevindeland on 5/9/19.
 */

public class ConfigurationQuickOptions {


    // Both SW and EN versions, and they both have the debugger menu.
    public static ConfigurationItems DEBUG_SW_EN = new ConfigurationItems(
            "debug_sw_en",
            false,
            true,
            true,
            true,
            false,
            "LANG_NULL",
            false,
            false,
            false,
            "CD1",
            true,
            false,
            false,
            "roboscreen",
            false,
            480,
            854,
            30,
            "activity",
            16000,
            16000
    );

    // EN version, and they both have the debugger menu.
    public static ConfigurationItems DEBUG_EN = new ConfigurationItems(
            "debug_en",
            true,
            true,
            true,
            false,
            false,
            "LANG_EN",
            false,
            false,
            false,
            "CD1",
            true,
            false,
            false,
            "roboscreen",
            false,
            480,
            854,
            30,
            "activity",
            16000,
            16000
    );
}
