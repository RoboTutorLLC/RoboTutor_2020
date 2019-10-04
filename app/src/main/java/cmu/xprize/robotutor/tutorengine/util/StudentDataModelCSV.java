package cmu.xprize.robotutor.tutorengine.util;

import android.os.Environment;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import cmu.xprize.robotutor.RoboTutor;
import cmu.xprize.robotutor.startup.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;
import cmu.xprize.util.CPlacementTest_Tutor;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.util.TCONST.I_CANCEL_HESITATE;
import static cmu.xprize.util.TCONST.LANG_SW;
import static cmu.xprize.util.TCONST.PLACEMENT_TAG;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-10-04.
 */
public class StudentDataModelCSV extends AbstractStudentDataModel implements IStudentDataModel {

    private static final String ROOTFOLDER = Environment.DIRECTORY_DOWNLOADS;
    private static final String SUBFOLDER = "robotutor";
    // first load ORIGINAL, then save to and load from UPDATED
    private static final String ORIGINAL_CSV = "students.csv";
    private static final String UPDATED_CSV = "update_students.csv";

    private static CSVReader _readerOriginal;
    private static CSVReader _readerUpdated;
    private static CSVWriter _writerUpdated;

    // starts as false, flips to true once we write once to updated.
    // this saves time, since we only load and save the delta each time.
    private boolean _hasWrittenOnce;

    // For mapping how the data is stored.
    private static String[] KEYS_LIST;
    private static HashMap<String, Integer> KEY_TO_COLUMN;
    static {
        KEY_TO_COLUMN = new HashMap<>();
        KEYS_LIST = new String[]{"STUDENT_ID", "HAS_PLAYED",
                "MATH_PLACEMENT", "MATH_PLACEMENT_INDEX",
                "WRITING_PLACEMENT", "WRITING_PLACEMENT_INDEX",
                "SKILL_SELECTED", "letters", "stories", "numbers",
                "LOG_SEQUENCE_ID",
                "akira_TIMES_PLAYED", "bpop_TIMES_PLAYED", "countingx_TIMES_PLAYED",
                "math_TIMES_PLAYED", "numberscale_TIMES_PLAYED", "story_TIMES_PLAYED",
                "write_TIMES_PLAYED"};
        for(int i=0; i < KEYS_LIST.length; i++) {
            KEY_TO_COLUMN.put(KEYS_LIST[i], i);
        }
    }

    private String _studentId;
    private String[] _cachedRow;

    private static File getFile(String filename) {
        return new File(Environment.getExternalStoragePublicDirectory(
                ROOTFOLDER), SUBFOLDER + "/" + filename);
    }


    // ---------------------
    // -- helper methods ---
    // ---------------------
    private static CSVReader getReaderOriginal() throws FileNotFoundException {
        return new CSVReader(
                new BufferedReader(new FileReader(getFile(ORIGINAL_CSV))));
    }

    private static CSVReader getReaderUpdated() throws FileNotFoundException {
        return new CSVReader(
                new BufferedReader(new FileReader(getFile(UPDATED_CSV))));
    }

    private static CSVWriter getWriterUpdated() throws IOException {
        return new CSVWriter(
                new BufferedWriter(new FileWriter(getFile(UPDATED_CSV))),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
    }

    /**
     * Helper method so I don't have to type the whole thing every time
     * @param key look up column number of this key
     * @return column number
     */
    private static int col(String key) {
        return KEY_TO_COLUMN.get(key);
    }


    /**
     * KIDSMGMT -- should check UPDATED first, and only resort to ORIGINAL if necessary
     * Constructor
     * @param studentId studentId to save KIDSMGMT check for _SW on the end
     * @throws FileNotFoundException if the file to read from does not exist... use Shared Prefs
     */
    public StudentDataModelCSV(String studentId) throws FileNotFoundException {

        _studentId = studentId;

        // just try these to see if the files exist... if they don't, use SharedPrefs
        _readerOriginal = getReaderOriginal();
        _readerUpdated = getReaderUpdated();

        try {
            String[] studentRow = _readerOriginal.readNext();

            while ((studentRow = _readerOriginal.readNext()) != null) {

                if (studentRow[col("STUDENT_ID")].equals(studentId)) {
                    _cachedRow = studentRow;
                    break; // found it, break out of table
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createNewStudent() {

        _cachedRow[col(HAS_PLAYED_KEY)] = String.valueOf(true);
        _cachedRow[col(WRITING_PLACEMENT_KEY)] = String.valueOf(CTutorEngine.language.equals(LANG_SW) && Configuration.usePlacement(RoboTutor.ACTIVITY));

        _cachedRow[col(WRITING_PLACEMENT_INDEX_KEY)] = String.valueOf(0);

        _cachedRow[col(MATH_PLACEMENT_KEY)] = String.valueOf(CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY));
        _cachedRow[col(MATH_PLACEMENT_INDEX_KEY)] =  String.valueOf(0);

        if (CYCLE_MATRIX) {
            _cachedRow[col(SKILL_SELECTED_KEY)] = SELECT_WRITING;
        }

        try {
            // KIDSMGMT NEXT copy this to saveAll();... also mimic behavior in StudentDataModelSharedPrefs
            CSVWriter writer = getWriterUpdated();
            writer.writeNext(_cachedRow);
            writer.close();
            _hasWrittenOnce = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getHasPlayed() {
        // KIDSMGMT next: rename updated_info.csv... make "hasPlayed" column empty
        return _cachedRow[col("HAS_PLAYED")];
    }

    @Override
    public String getWritingTutorID() {
        return _cachedRow[col(CURRENT_WRITING_TUTOR_KEY)];
    }

    @Override
    public void updateWritingTutorID(String id, boolean save) {

    }

    @Override
    public String getStoryTutorID() {
        return _cachedRow[col(CURRENT_STORIES_TUTOR_KEY)];
    }

    @Override
    public void updateStoryTutorID(String id, boolean save) {

    }

    @Override
    public String getMathTutorID() {
        return _cachedRow[col(CURRENT_MATH_TUTOR_KEY)];
    }

    @Override
    public void updateMathTutorID(String id, boolean save) {

    }

    @Override
    public String getActiveSkill() {
        String s = _cachedRow[col(SKILL_SELECTED_KEY)];
        return s != null ? s : SELECT_WRITING;
    }

    @Override
    public void updateActiveSkill(String skill, boolean save) {

    }

    @Override
    public boolean getWritingPlacement() {
        return String.valueOf(_cachedRow[col(WRITING_PLACEMENT_KEY)])
                .equalsIgnoreCase("TRUE");
    }

    // KIDSMGMT next continue on these...
    // - make sure the types are coorect
    // - for updates, change to have a boolean value that writes it or not
    @Override
    public int getWritingPlacementIndex() {
        return 0;
    }

    @Override
    public boolean getMathPlacement() {
        return String.valueOf(_cachedRow[col(MATH_PLACEMENT_KEY)])
                .equalsIgnoreCase("TRUE");
    }

    @Override
    public int getMathPlacementIndex() {
        return 0;
    }

    // KIDSMGMT change all "update" to take a boolean 'save' as second var
    // if 'save' is true, write it.
    // KIDSMGMT also make a 'saveAll' function
    @Override
    public void updateLastTutor(String activeTutorId, boolean save) {

    }

    @Override
    public String getLastTutor() {
        return null;
    }

    @Override
    public void updateMathPlacement(boolean b, boolean save) {

    }

    @Override
    public void updateMathPlacementIndex(Integer i, boolean save) {

    }

    @Override
    public void updateWritingPlacement(boolean b, boolean save) {

    }

    @Override
    public void updateWritingPlacementIndex(Integer i, boolean save) {

    }

    @Override
    public int getTimesPlayedTutor(String tutor) {
        return 0;
    }

    @Override
    public void updateTimesPlayedTutor(String tutor, int i, boolean save) {

    }

    @Override
    public void saveAll() {
        // KIDSMGMT write _cachedRow to UPDATE
    }

    @Override
    public String toLogString() {
        return null;
    }
}
