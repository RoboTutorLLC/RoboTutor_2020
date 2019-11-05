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

import cmu.xprize.util.IRoboTutor;
import cmu.xprize.util.TCONST;

/**
 * CInterventionStudentData
 * <p>Reads a list of students who are involved in an intervention study.</p>
 * Created by kevindeland on 9/13/19.
 */

public class CInterventionStudentData {

    private static CInterventionStudentData singleton;
    private static List<Student> studentData;

    private static String currentStudentId;

    // needed for logging, I guess
    private static String currentTutorId;

    private static final String TAG = "STUDENT_INTERVENTION";

    private CInterventionStudentData(String filename) {
        Log.i(TAG, "filename = " + filename);

        studentData = new ArrayList<>();

        try {
            CSVReader reader = new CSVReader(new FileReader(filename));
            String[] nextLine;
            boolean skippedHeader = false;

            while ((nextLine = reader.readNext()) != null) {
                Log.i(TAG, "nextLine = " + nextLine[0] + ", " + nextLine[1]);
                Log.i("9_14", "nextLine = " + nextLine[0] + ", " + nextLine[1]);

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
                    Log.wtf("9_14", addme.id);
                    Log.wtf(TAG, addme.toString());
                    Log.i("9_14", "Adding: " + addme.toString());
                    studentData.add(addme);

                } catch (Exception e) {
                    Log.e("9_14", "Error: " + e.getMessage());
                    Log.v(TAG, "skipped this row " + nextLine[0]);
                }

            }
        } catch (IOException e) {
            Log.wtf("9_14", "ERROR: " + e.getMessage());
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
     * Making a constructor/initializer that just takes a list, for testing.
     * @param students list of students
     * @return
     */
    private CInterventionStudentData(List<Student> students) {
        studentData = students;
    }
    static public CInterventionStudentData initialize(List<Student> students) {

        if (singleton == null) {
            singleton = new CInterventionStudentData(students);
        }

        return singleton;
    }

    public static Student getStudentById(String id) {

        Student found = null;
        for (Student s : studentData) {
            Log.wtf("MY_ID", "Found student " + s.id);
            if (s.id.equals(id))
                found = s;
        }

        return found;
    }

    /**
     * We need to track this for when we wanna check...
     * @param studentId
     */
    public static void setCurrentStudentId(String studentId) {
        currentStudentId = studentId;
    }

    public static String getCurrentStudentId() {
        return currentStudentId;
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

        List<Student> groupMates = findStudentsInMyGroupNotMe(currentStudentId);

        List<String> possibles = new ArrayList<String>();

        for (Student s : groupMates) {
            Integer sLevel = s.levels.get(domain);
            if (sLevel != null && sLevel > level) // could be > or >=
                possibles.add(s.photoFile); // add student photo to list
        }

        if (possibles.size() == 0)
            return getBackupStudentKnowledgeSupport(domain, groupMates);

        // get random student image
        else return possibles.get((new Random()).nextInt(possibles.size()));
    }

    /**
     * Return the photo filename of a student who can give knowledge support
     * to the current student in domain {@code domain}
     *
     * @param domain which domain does the student need help in?
     *
     * @return filename of a student's photo
     */
    public static String getPhotoForKnowledgeSupport(String domain) {
        int level = getCurrentStudentLevel(domain);
        return getPhotoForKnowledgeSupport(domain, level);
    }

    private static int getCurrentStudentLevel(String domain) {

        int level = -1;
        for (Student s : studentData) {
            if (currentStudentId.equals(s.id)) {
                switch(domain) {
                    case "MATH":
                        level = s.levels.get("MATH");
                        break;

                    case "LIT":
                    default:
                        level = s.levels.get("LIT");
                        break;

                }
            }
        }
        return level;
    }

    /**
     * Backup plan, if nobody is at a higher level than us.
     * @param domain which knowledge domain?
     * @param groupMates list of Students to choose from
     * @return Photo filename from Student w/ highest level
     */
    private static String getBackupStudentKnowledgeSupport(String domain, List<Student> groupMates) {
        int maxLevel = -1;
        String studentPhotoWithHighestLevel = null;

        for (Student s : groupMates) {
            Integer sLevel = s.levels.get(domain);
            if (sLevel != null && sLevel > maxLevel) {
                maxLevel = s.levels.get(domain);
                studentPhotoWithHighestLevel = s.photoFile;
            }
        }

        return studentPhotoWithHighestLevel;
    }

    /**
     * Return the photo filename of a student who can give application support
     * of a student on the tutor {@code tutor}
     *
     * @param tutor which tutor does the student need help with?
     * @return filename of a student's photo
     */
    public static String getPhotoForApplicationSupport(String tutor) {

        // only allow students in my group
        List<Student> groupMates = findStudentsInMyGroupNotMe(currentStudentId);

        List<String> possibles = new ArrayList<String>();
        Log.i("9_14", "getPhotoForAppSupport " + tutor);
        Log.i("9_14", "dataSize=" + studentData.size());

        for (Student s : groupMates) {
            Log.wtf("9_14", s.tutors.toString());
            if (s.tutors.get(tutor))
                possibles.add(s.photoFile);
        }

        // backup plan. If nobody has played this tutor, get the student who's played the most
        if (possibles.size() == 0)
            return getBackupStudentApplicationSupport(groupMates);

        else return possibles.get((new Random()).nextInt(possibles.size()));
    }

    /**
     * If no possible students found by first criteria, use this criteria.
     * Return student who's played the most tutors.
     *
     * @param groupMates list of groupmates
     * @return Student with most tutors played.
     */
    private static String getBackupStudentApplicationSupport(List<Student> groupMates) {
        int maxTutorsPlayed = -1;
        String studentPhotoWithMost = null;

        for (Student s : groupMates) {
            int currentStudentTutors = 0;
            for (Map.Entry<String, Boolean> e : s.tutors.entrySet()) {
                if (e.getValue())
                    currentStudentTutors++;
            }
            if (currentStudentTutors >= maxTutorsPlayed) {
                maxTutorsPlayed = currentStudentTutors;
                studentPhotoWithMost = s.photoFile;
            }
        }

        return studentPhotoWithMost;
    }


    /**
     * Filter list of studentData into only students in my group (not including me)
     * @param studentId the student whose group we're looking for
     * @return list of classmates
     */
    private static List<Student> findStudentsInMyGroupNotMe(String studentId) {

        // if we're in debug mode, just run check with every possible students
        if (studentId.equals(TCONST.DEFAULT_STUDENT_ID)) return studentData;

        List<Student> possibles = new ArrayList<>();

        String groupId = getMyGroupId(studentId);

        for (Student s : studentData) {
            if (s.groupId.equals(groupId) && !s.id.equals(studentId)) {
                possibles.add(s);
            }
        }

        return possibles;
    }

    /**
     * Get the group ID of a student;
     * @param studentId student whose GroupID we want
     * @return string group id
     */
    private static String getMyGroupId(String studentId) {
        for (Student s : studentData) {
            if (s.id.equals(studentId)) {
                return s.groupId;
            }
        }
        return null;
    }



}
