package cmu.xprize.comp_intervention.data;

import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cmu.xprize.comp_intervention.data.IDATA_CONST.AKIRA;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.BPOP;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.LIT;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.MATH;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.NUMCOMPARE;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.PICMATCH;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.SPELL;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.STORY;
import static cmu.xprize.comp_intervention.data.IDATA_CONST.WRITE;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/14/19.
 */

public class CUpdateInterventionStudentData {


    private static CUpdateInterventionStudentData singleton;
    private static List<Student> studentData;

    private static String filename;
    private static Student thisStudent;

    private static final String TAG = "UPDATE_INTERVENTION";

    private CUpdateInterventionStudentData(String filename) {
        Log.i(TAG, "filename = " + filename);

        CUpdateInterventionStudentData.filename = filename;

        studentData = new ArrayList<>();

        try {
            CSVReader reader = new CSVReader(new FileReader(filename));
            String[] nextLine;
            boolean skippedHeader = false;

            while ((nextLine = reader.readNext()) != null) {
                Log.i(TAG, "nextLine = " + nextLine[0] + ", " + nextLine[1]);

                // skip the header
                if (!skippedHeader) {
                    skippedHeader = true;
                    continue;
                }

                try {
                    // -----------------------------------------------------------
                    // - HERE IS WHERE WE LOAD ONE LINE OF STUDENT DATA FROM CSV -
                    // -----------------------------------------------------------
                    Student addme = new Student(nextLine);
                    Log.wtf(TAG, addme.toString());
                    studentData.add(addme);

                } catch (Exception e) {
                    Log.v(TAG, "skipped this row " + nextLine[0]);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    static public CUpdateInterventionStudentData initialize(String filename) {

        if(singleton == null) {
            singleton = new CUpdateInterventionStudentData(filename);
        }

        return singleton;
    }

    /**
     * initialize a new student
     *
     * @param studentId the student id
     */
    static public void writeNewStudent(String studentId) {

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename), ',');
            String[] entries = {
                    studentId,
                    "PLACEHOLDER",
                    "none", // group
                    "1", // lvl_math
                    "1", // lvl_story
                    "1", // lvl_lit
                    "n", // lvl_bpop
                    "n", // lvl_spell
                    "n", // lvl_picmatch
                    "n", // lvl_akira
                    "n", // lvl_write
                    "n", // lvl_numcompare
            };

            thisStudent = new Student(entries);

            writer.writeNext(entries);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the current student to file
     * (this will create duplicates but that's fine for now... we can just take the last line)
     */
    static private void writeThisStudent() {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename), ',');
            String[] entries = {
                    thisStudent.id,
                    thisStudent.photoFile,
                    String.valueOf(thisStudent.levels.get(MATH)),
                    String.valueOf(thisStudent.levels.get(STORY)),
                    String.valueOf(thisStudent.levels.get(LIT)),

                    String.valueOf(thisStudent.tutors.get(BPOP)),
                    String.valueOf(thisStudent.tutors.get(SPELL)),
                    String.valueOf(thisStudent.tutors.get(PICMATCH)),
                    String.valueOf(thisStudent.tutors.get(AKIRA)),
                    String.valueOf(thisStudent.tutors.get(WRITE)),
                    String.valueOf(thisStudent.tutors.get(NUMCOMPARE)),
            };

            writer.writeNext(entries);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * when the student level updates
     * @param key MATH, STORY, LIT
     * @param level integer 1, 2, 3, etc
     */
    static public void updateStudentLevel(String key, int level) {

        int prevLevel = thisStudent.levels.get(key);
        Log.wtf(TAG, "Changing " + key + " level from " + prevLevel + " to " + level);
        thisStudent.levels.put(key, level);

        writeThisStudent();
    }

    /**
     * when the student tutor has played value updates
     *
     * @param key BPOP, SPELL, PICMATCH, etc
     * @param played true or false, y or n
     */
    static public void updateStudentTutor(String key, boolean played) {
        boolean prevPlayed = thisStudent.tutors.get(key);
        Log.wtf(TAG, "Changing " + key + " tutor from " + prevPlayed + " to " + played);
        thisStudent.tutors.put(key, played);

        writeThisStudent();
    }
}
