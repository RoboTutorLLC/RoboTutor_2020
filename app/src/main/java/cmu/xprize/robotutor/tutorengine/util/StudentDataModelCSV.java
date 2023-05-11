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
import cmu.xprize.util.configuration.Configuration;
import cmu.xprize.robotutor.tutorengine.CTutorEngine;

import static cmu.xprize.comp_session.AS_CONST.BEHAVIOR_KEYS.SELECT_WRITING;
import static cmu.xprize.util.TCONST.DEBUG_CSV;
import static cmu.xprize.util.TCONST.LANG_SW;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-10-04.
 */
public class StudentDataModelCSV extends AbstractStudentDataModel implements IStudentDataModel {

    private static final String ROOTFOLDER =  "/sdcard/RoboTutor/";
    private static final String SUBFOLDER = "robotutor_studentModels";
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
    private static String[] KEYS_LIST = new String[]{"STUDENT_ID", "HAS_PLAYED",
            "MATH_PLACEMENT", "MATH_PLACEMENT_INDEX",
            "WRITING_PLACEMENT", "WRITING_PLACEMENT_INDEX",
            "SKILL_SELECTED", "letters", "stories", "numbers", "LAST_TUTOR_PLAYED",
            "LOG_SEQUENCE_ID",
            "akira_TIMES_PLAYED", "bpop_TIMES_PLAYED", "countingx_TIMES_PLAYED",
            "math_TIMES_PLAYED", "numberscale_TIMES_PLAYED", "story_TIMES_PLAYED",
            "write_TIMES_PLAYED",
            "cloze_TIMES_PLAYED", "story_pic_TIMES_PLAYED", "sentence_writing_TIMES_PLAYED",
            "picmatch_TIMES_PLAYED", "spelling_TIMES_PLAYED", "bigmath_add_TIMES_PLAYED",
            "bigmath_sub_TIMES_PLAYED", "numcompare_TIMES_PLAYED", "pv1_TIMES_PLAYED",
            "pv2_TIMES_PLAYED", "pv3_TIMES_PLAYED"
    };

    private static HashMap<String, Integer> KEY_TO_COLUMN;
    static {
        KEY_TO_COLUMN = new HashMap<>();
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

    private static CSVWriter getWriterUpdated(boolean append) throws IOException {

        // append = true
        return new CSVWriter(
                new BufferedWriter(new FileWriter(getFile(UPDATED_CSV), append)),
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

        _readerUpdated = getReaderUpdated();
        boolean foundInUpdated = false;
        String[] updateHeader, updateRow;

        // map each STUDENT_ID --> {line0, line1, line2, ...}
        // where lineN is a String[]
        HashMap<String, String[]> duplicateStudents = new HashMap<String, String[]>();
        try {
            // skip the header
            updateHeader = _readerUpdated.readNext();

            Log.i(DEBUG_CSV, "Checking in UPDATED");

            // iterate through each line
            while ((updateRow = _readerUpdated.readNext()) != null) {

                // this block of code is designed to only keep the last line for each student
                // so that they can be written out later to save space

                String id = updateRow[col("STUDENT_ID")];
                // if our map already contains this Student
                if (duplicateStudents.containsKey(id)) {
                    // put most recent row
                    duplicateStudents.put(id, updateRow);
                } else {
                    Log.i(DEBUG_CSV, "Adding key " + id + " to duplicateStudents");
                    // these are the same...
                    duplicateStudents.put(id, updateRow);
                }
            }
            _readerUpdated.close(); // close after reading them all!
        } catch (IOException e) {

            e.printStackTrace();
            return; // something isn't right, but we don't want to overwrite
        }

        rewriteUpdate(updateHeader, duplicateStudents);

        Log.i(DEBUG_CSV, "Checking duplicateStudents for " + _studentId);
        if (duplicateStudents.containsKey(_studentId)) {

            Log.i(DEBUG_CSV, "Found " + _studentId + " in duplicateStudents");
            _cachedRow = duplicateStudents.get(_studentId);
            Log.i(DEBUG_CSV, _cachedRow[col(WRITING_PLACEMENT_INDEX_KEY)]);
            foundInUpdated = true;

        } else {
            Log.i(DEBUG_CSV, "Didn't find " + _studentId + " in duplicateStudents");
            _readerOriginal = getReaderOriginal();

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
        }

        if (_cachedRow == null) {
            _cachedRow = new String[KEYS_LIST.length]; // initialize empty row
        }
    }

    /**
     * This is used for rewriting update without repeats
     * @param updateHeader
     * @param duplicateStudents
     */
    private void rewriteUpdate(String[] updateHeader, HashMap<String, String[]> duplicateStudents) {

        try {
            CSVWriter writer = getWriterUpdated(false);
            writer.writeNext(updateHeader); // write header

            for (String student : duplicateStudents.keySet()) {
                writer.writeNext(duplicateStudents.get(student)); // write each student
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        Log.i(DEBUG_CSV, "getHasPlayed() " + _studentId);
        if (_cachedRow == null) return null;
        return _cachedRow[col("HAS_PLAYED")];
    }

    @Override
    public String getWritingTutorID() {
        Log.i(DEBUG_CSV, "getWritingTutorId() " + _studentId);
        String writingTutor = _cachedRow[col(CURRENT_WRITING_TUTOR_KEY)];
        Log.wtf(DEBUG_CSV, writingTutor);
        return writingTutor;
    }

    @Override
    public void updateWritingTutorID(String id, boolean save) {
        Log.i(DEBUG_CSV, "updateWritingTutorId(" + id + ") " + _studentId);
        _cachedRow[col(CURRENT_WRITING_TUTOR_KEY)] = id;
        if (save) writeToFile();
    }

    @Override
    public String getStoryTutorID() {
        Log.i(DEBUG_CSV, "getStoryTutorId() " + _studentId);
        return _cachedRow[col(CURRENT_STORIES_TUTOR_KEY)];
    }

    @Override
    public void updateStoryTutorID(String id, boolean save) {
        Log.i(DEBUG_CSV, "updateStoryTutorId() " + _studentId);
        _cachedRow[col(CURRENT_STORIES_TUTOR_KEY)] = id;
        if (save) writeToFile();
    }

    @Override
    public String getMathTutorID() {
        Log.i(DEBUG_CSV, "getMathTutorId() " + _studentId);
        return _cachedRow[col(CURRENT_MATH_TUTOR_KEY)];
    }

    @Override
    public void updateMathTutorID(String id, boolean save) {
        Log.i(DEBUG_CSV, "updateMathTutorId() " + _studentId);
        _cachedRow[col(CURRENT_MATH_TUTOR_KEY)] = id;
        if (save) writeToFile();
    }

    @Override
    public String getActiveSkill() {
        Log.i(DEBUG_CSV, "getActiveSkill() " + _studentId);
        String s = _cachedRow[col(SKILL_SELECTED_KEY)];
        return s != null ? s : SELECT_WRITING;
    }

    @Override
    public void updateActiveSkill(String skill, boolean save) {
        Log.i(DEBUG_CSV, "updateActiveSkill() " + _studentId);
        _cachedRow[col(SKILL_SELECTED_KEY)] = skill;
        if (save) writeToFile();
    }

    @Override
    public boolean getWritingPlacement() {
        Log.i(DEBUG_CSV, "getWritingPlacement() " + _studentId);
        return String.valueOf(_cachedRow[col(WRITING_PLACEMENT_KEY)])
                .equalsIgnoreCase("TRUE");
    }

    @Override
    public int getWritingPlacementIndex() {
        Log.i(DEBUG_CSV, "getWritingPlacementIndex() " + _studentId);
        String s = _cachedRow[col(WRITING_PLACEMENT_INDEX_KEY)];
        int x = (s == null || s.length() == 0 || s.equals("null")) ? 0 : Integer.parseInt(s);
        Log.w(DEBUG_CSV, "getWritingPlacementIndex = " + x);
        return x;
    }

    @Override
    public boolean getMathPlacement() {
        Log.i(DEBUG_CSV, "getMathPlacement() " + _studentId);
        return String.valueOf(_cachedRow[col(MATH_PLACEMENT_KEY)])
                .equalsIgnoreCase("TRUE");
    }

    @Override
    public int getMathPlacementIndex() {
        Log.i(DEBUG_CSV, "getMathPlacementIndex() " + _studentId);
        String s = _cachedRow[col(MATH_PLACEMENT_INDEX_KEY)];
        int x = (s == null || s.length() == 0 || s.equals("null")) ? 0 : Integer.parseInt(s);
        Log.w(DEBUG_CSV, "getMathPlacementIndex = " + x);
        return x;
    }

    @Override
    public void updateLastTutor(String activeTutorId, boolean save) {
        Log.i(DEBUG_CSV, "updateLastTutor() " + _studentId);
        _cachedRow[col(LAST_TUTOR_PLAYED_KEY)] = activeTutorId;
        if (save) writeToFile();
    }

    @Override
    public String getLastTutor() {

        Log.i(DEBUG_CSV, "getLastTutor() " + _studentId);
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
        _cachedRow[col(WRITING_PLACEMENT_KEY)] = String.valueOf(b);
        if (save) writeToFile();
    }

    @Override
    public void updateWritingPlacementIndex(Integer i, boolean save) {
        _cachedRow[col(WRITING_PLACEMENT_INDEX_KEY)] = String.valueOf(i);
        if (save) writeToFile();
    }

    @Override
    public int getTimesPlayedTutor(String videoId) {
        String timesPlayedKey = getTimesPlayedKey(videoId);
        Log.d("DEBUG_FRIDAY", "timesPlayedKey=" + timesPlayedKey
                + "; col=" + col(timesPlayedKey));
        String s = _cachedRow[col(timesPlayedKey)]; // fix... just check for the exception
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
            _writerUpdated = getWriterUpdated(true);
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
