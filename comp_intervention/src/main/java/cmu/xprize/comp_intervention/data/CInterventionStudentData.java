package cmu.xprize.comp_intervention.data;

import android.util.Log;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 9/13/19.
 */

public class CInterventionStudentData {

    private static CInterventionStudentData singleton;
    private static List<Student> studentData;

    private static final String TAG = "STUDENT_INTERVENTION";

    private static final String TEACHER_IMAGE = "teacher.jpg";

    private CInterventionStudentData(String filename) {
        Log.i(TAG, "filename = " + filename);

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
    static public CInterventionStudentData initialize(String filename) {

        if(singleton == null) {
            singleton = new CInterventionStudentData(filename);
        }

        return singleton;
    }

    /**
     * Return the photo filename of a student who can give knowledge support
     * of a student at level {@code level} in domain {@code domain}
     *
     * @param domain which domain does the student need help in?
     * @param level which level is the student at?
     * @return filename of a student's photo
     */
    public static String getPhotoForKnowledgeSupport(String domain, int level) {

        List<String> possibles = new ArrayList<String>();

        for (Student s : studentData) {
            if (s.levels.get(domain) > level) // could be > or >=
                possibles.add(s.photoFile); // add student photo to list
        }

        if (possibles.size() == 0)
            return TEACHER_IMAGE;

        // get random student image
        else return possibles.get((new Random()).nextInt(possibles.size()));
    }

    /**
     * Return the photo filename of a student who can give application support
     * of a student on the tutor {@code tutor}
     *
     * @param tutor which tutor does the student need help with?
     * @return filename of a student's photo
     */
    public static String getPhotoForApplicationSupport(String tutor) {

        List<String> possibles = new ArrayList<String>();

        for (Student s : studentData) {
            if (s.tutors.get(tutor))
                possibles.add(s.photoFile);
        }

        if (possibles.size() == 0)
            return TEACHER_IMAGE;

        else return possibles.get((new Random()).nextInt(possibles.size()));
    }




}
