package cmu.xprize.comp_writing.constants;

/**
 * WR_FEATURES
 * <p>These are called as args to "publishFeature"</p>
 * Created by kevindeland on 9/4/19.
 */

public class WR_FEATURES {

    // data source types
    public static final String FTR_WORDS             = "FTR_WORDS";
    public static final String FTR_MISSING_LTR         = "FTR_MISSING_LTR";


    // data source types -- sentence
    public static final String FTR_SEN_PREFIX           = "FTR_SEN";
    public static final String FTR_SEN_LTR              = "FTR_SEN_LTR";
    public static final String FTR_SEN_CORR              = "FTR_SEN_CORR";
    public static final String FTR_SEN_WORD             = "FTR_SEN_WORD";
    public static final String FTR_SEN_WRD             = "FTR_SEN_WRD";
    public static final String FTR_SEN_SEN              = "FTR_SEN_SEN";
    public static final String FTR_SEN_COPY              = "FTR_SEN_COPY";

    public static final String FTR_SPACE_REPLAY         = "FTR_SPACE_REPLAY";
    public static final String FTR_SPACE_SAMPLE         = "FTR_SPACE_SAMPLE";

    public static final String FTR_ATTEMPT_1            = "FTR_ATTEMPT_1";
    public static final String FTR_ATTEMPT_2            = "FTR_ATTEMPT_2";
    public static final String FTR_ATTEMPT_3            = "FTR_ATTEMPT_3";
    public static final String FTR_ATTEMPT_4            = "FTR_ATTEMPT_4";

    //amogh added
    public static final String FTR_SEN_ATTEMPT_1        = "FTR_SEN_ATTEMPT_1";
    public static final String FTR_SEN_ATTEMPT_2        = "FTR_SEN_ATTEMPT_2";
    public static final String FTR_SEN_ATTEMPT_3        = "FTR_SEN_ATTEMPT_3";
    public static final String FTR_SEN_ATTEMPT_4        = "FTR_SEN_ATTEMPT_4";

    public static final String FTR_HESITATION_1            = "FTR_HESITATION_1";
    public static final String FTR_HESITATION_2            = "FTR_HESITATION_2";
    public static final String FTR_HESITATION_3            = "FTR_HESITATION_3";
    public static final String FTR_HESITATION_4            = "FTR_HESITATION_4";
    public static final String FTR_HESITATION_5            = "FTR_HESITATION_5";

    public static final String FTR_WORD_CORRECT            = "FTR_WORD_CORRECT";
    public static final String FTR_SEN_EVAL                = "FTR_SEN_EVAL";

    //amogh added audio features
    public static final String FTR_AUDIO_CAP = "FTR_AUDIO_CAP";
    public static final String FTR_AUDIO_PUNC = "FTR_AUDIO_PUNC";
    public static final String FTR_AUDIO_LTR = "FTR_AUDIO_LTR";
    public static final String FTR_AUDIO_SPACE = "FTR_AUDIO_SPACE";
    public static final String FTR_INSERT = "FTR_INSERT";
    public static final String FTR_DELETE = "FTR_DELETE";
    public static final String FTR_REPLACE = "FTR_REPLACE";
    public static final String FTR_PERIOD = "FTR_PERIOD";
    public static final String FTR_EXCLAIM = "FTR_EXCLAIM";
    public static final String FTR_QUESTION = "FTR_QUESTION";
    public static final String FTR_COMMA = "FTR_COMMA";
    public static final String FTR_AUDIO_NO_ERROR = "FTR_AUDIO_NO_ERROR0";

    // these might go better with the constant vars that are around them in usage
    public static final String FTR_STIM_1_CONCAT            = "FTR_STIM_1_CONCAT";
    public static final String FTR_STIM_3_CONCAT            = "FTR_STIM_3_CONCAT";
    public static final String FTR_ANS_CONCAT            = "FTR_ANS_CONCAT";

    //
    public static final String FTR_INPUT_STALLED        = "FTR_INPUT_STALLED";
    public static final String FTR_HAD_ERRORS           = "FTR_HAD_ERRORS";

    public static final String ERROR_METRIC             = "FTR_ERROR_METRIC";
    public static final String ERROR_CHAR               = "FTR_ERROR_CHAR";

}
