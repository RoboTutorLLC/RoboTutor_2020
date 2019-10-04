package cmu.xprize.robotutor.tutorengine.util;

import android.os.Environment;
import android.util.Log;

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
import static cmu.xprize.util.TCONST.DEBUG_CSV;
import static cmu.xprize.util.TCONST.I_CANCEL_HESITATE;
import static cmu.xprize.util.TCONST.LANG_SW;
import static cmu.xprize.util.TCONST.PLACEMENT_TAG;
import static cmu.xprize.util.TCONST.WRITING_PLACEMENT_INDEX;

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
                "SKILL_SELECTED", "letters", "stories", "numbers", "LAST_TUTOR_PLAYED",
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

        // append = true
        return new CSVWriter(
                new BufferedWriter(new FileWriter(getFile(UPDATED_CSV), true)),
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

        Log.i(DEBUG_CSV, "Constructor");
        _studentId = studentId;

        // just try these to see if the files exist... if they don't, use SharedPrefs
        _readerOriginal = getReaderOriginal();
        _readerUpdated = getReaderUpdated();

        try {
            String[] studentRow = _readerOriginal.readNext();

            while ((studentRow = _readerOriginal.readNext()) != null) {

                if (studentRow[col("STUDENT_ID")].equals(_studentId)) {
                    _cachedRow = studentRow;
                    Log.i(DEBUG_CSV, "found existing student with ID " + _studentId);
                    break; // found it, break out of table
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_cachedRow == null) {
            _cachedRow = new String[KEYS_LIST.length]; // initialize empty row
        }
    }

    @Override
    public String getStudentId() {
        return _studentId;
    }

    @Override
    public void createNewStudent() {

        Log.i(DEBUG_CSV, "createNewStudent()");

        _cachedRow[col("STUDENT_ID")] = _studentId;
        _cachedRow[col(HAS_PLAYED_KEY)] = String.valueOf(true);
        _cachedRow[col(WRITING_PLACEMENT_KEY)] = String.valueOf(CTutorEngine.language.equals(LANG_SW) && Configuration.usePlacement(RoboTutor.ACTIVITY));

        _cachedRow[col(WRITING_PLACEMENT_INDEX_KEY)] = String.valueOf(0);

        _cachedRow[col(MATH_PLACEMENT_KEY)] = String.valueOf(CTutorEngine.language.equals(LANG_SW) &&  Configuration.usePlacement(RoboTutor.ACTIVITY));
        _cachedRow[col(MATH_PLACEMENT_INDEX_KEY)] =  String.valueOf(0);

        if (CYCLE_MATRIX) {
            _cachedRow[col(SKILL_SELECTED_KEY)] = SELECT_WRITING;
        }

        writeToFile();
        _hasWrittenOnce = true;
    }

    @Override
    public String getHasPlayed() {
        Log.i(DEBUG_CSV, "getHasPlayed()");
        if (_cachedRow == null) return null;
        return _cachedRow[col("HAS_PLAYED")];
    }

    @Override
    public String getWritingTutorID() {
        Log.i(DEBUG_CSV, "getWritingTutorId()");
        return _cachedRow[col(CURRENT_WRITING_TUTOR_KEY)];
    }

    @Override
    public void updateWritingTutorID(String id, boolean save) {
        Log.i(DEBUG_CSV, "updateWritingTutorId()");
        _cachedRow[col(CURRENT_WRITING_TUTOR_KEY)] = id;
        if (save) writeToFile();
    }

    @Override
    public String getStoryTutorID() {
        Log.i(DEBUG_CSV, "getStoryTutorId()");
        return _cachedRow[col(CURRENT_STORIES_TUTOR_KEY)];
    }

    @Override
    public void updateStoryTutorID(String id, boolean save) {
        Log.i(DEBUG_CSV, "updateStoryTutorId()");
        _cachedRow[col(CURRENT_STORIES_TUTOR_KEY)] = id;
        if (save) writeToFile();
    }

    @Override
    public String getMathTutorID() {
        Log.i(DEBUG_CSV, "getMathTutorId()");
        return _cachedRow[col(CURRENT_MATH_TUTOR_KEY)];
    }

    @Override
    public void updateMathTutorID(String id, boolean save) {
        Log.i(DEBUG_CSV, "updateMathTutorId()");
        _cachedRow[col(CURRENT_MATH_TUTOR_KEY)] = id;
        if (save) writeToFile();
    }

    @Override
    public String getActiveSkill() {
        Log.i(DEBUG_CSV, "getActiveSkill()");
        String s = _cachedRow[col(SKILL_SELECTED_KEY)];
        return s != null ? s : SELECT_WRITING;
    }

    @Override
    public void updateActiveSkill(String skill, boolean save) {
        Log.i(DEBUG_CSV, "updateActiveSkill()");
        _cachedRow[col(SKILL_SELECTED_KEY)] = skill;
        if (save) writeToFile();
    }

    @Override
    public boolean getWritingPlacement() {
        Log.i(DEBUG_CSV, "getWritingPlacement()");
        return String.valueOf(_cachedRow[col(WRITING_PLACEMENT_KEY)])
                .equalsIgnoreCase("TRUE");
    }

    // KIDSMGMT next... try it with actual file.
    // Log in with DigitalLogBook (create a new student)
    // try playing, see what happens... does it update to xyz
    @Override
    public int getWritingPlacementIndex() {
        Log.i(DEBUG_CSV, "getWritingPlacementIndex()");
        String s = _cachedRow[col(WRITING_PLACEMENT_INDEX_KEY)];
        return (s == null || s.length() == 0) ? 0 : Integer.parseInt(s);
    }

    @Override
    public boolean getMathPlacement() {
        Log.i(DEBUG_CSV, "getMathPlacement()");
        return String.valueOf(_cachedRow[col(MATH_PLACEMENT_KEY)])
                .equalsIgnoreCase("TRUE");
    }

    @Override
    public int getMathPlacementIndex() {
        Log.i(DEBUG_CSV, "getMathPlacementIndex()");
        String s = _cachedRow[col(MATH_PLACEMENT_INDEX_KEY)];
        return (s == null || s.length() == 0) ? 0 : Integer.parseInt(s);
    }

    @Override
    public void updateLastTutor(String activeTutorId, boolean save) {
        Log.i(DEBUG_CSV, "updateLastTutor()");
        _cachedRow[col(LAST_TUTOR_PLAYED_KEY)] = activeTutorId;
        if (save) writeToFile();
    }

    @Override
    public String getLastTutor() {

        Log.i(DEBUG_CSV, "getLastTutor()");
        return _cachedRow[col(LAST_TUTOR_PLAYED_KEY)];
    }

    @Override
    public void updateMathPlacement(boolean b, boolean save) {
        _cachedRow[col(MATH_PLACEMENT_KEY)] = String.valueOf(b);
        if (save) writeToFile();
    }

    @Override
    public void updateMathPlacementIndex(Integer i, boolean save) {
        _cachedRow[col(MATH_PLACEMENT_INDEX_KEY)] = String.valueOf(i);
        if (save) writeToFile();
    }

    @Override
    public void updateWritingPlacement(boolean b, boolean save) {
        _cachedRow[col(WRITING_PLACEMENT_INDEX)] = String.valueOf(b);
        if (save) writeToFile();
    }

    @Override
    public void updateWritingPlacementIndex(Integer i, boolean save) {
        _cachedRow[col(WRITING_PLACEMENT_INDEX_KEY)] = String.valueOf(i);
        if (save) writeToFile();
    }

    @Override
    public int getTimesPlayedTutor(String tutor) {
        String key = getTimesPlayedKey(tutor);
        String s = _cachedRow[col(key)];
        return (s == null || s.length() == 0) ? 0 : Integer.parseInt(s);
    }

    @Override
    public void updateTimesPlayedTutor(String tutor, int i, boolean save) {
        String key = getTimesPlayedKey(tutor);
        _cachedRow[col(key)] = String.valueOf(i);
        if (save) writeToFile();
    }

    /**
     * Writes all of the cachedRow to CSV.
     */
    @Override
    public void saveAll() {
        Log.i(DEBUG_CSV, "saveAll()");
        writeToFile();
    }

    private void writeToFile() {
        try {
            _writerUpdated = getWriterUpdated();
            _writerUpdated.writeNext(_cachedRow);
            _writerUpdated.close();
            _hasWrittenOnce = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toLogString() {
        return null;
    }
}
