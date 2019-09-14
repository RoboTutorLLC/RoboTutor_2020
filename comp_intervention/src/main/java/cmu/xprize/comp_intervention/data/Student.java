package cmu.xprize.comp_intervention.data;

import java.util.HashMap;
import java.util.Map;

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

public class Student {

    String id;
    String photoFile;
    Map<String, Integer> levels;
    Map<String, Boolean> tutors;

    /**
     * Student constructed from a single csv line
     * @param csvLine an array of Strings from a single CSV parsing
     */
    Student(String[] csvLine) {
        String id = csvLine[0];
        String photoFile = csvLine[1];

        Map<String, Integer> lvls = new HashMap<>();
        lvls.put(MATH, Integer.parseInt(csvLine[2]));
        lvls.put(STORY, Integer.parseInt(csvLine[3]));
        lvls.put(LIT, Integer.parseInt(csvLine[4]));

        Map<String, Boolean> played = new HashMap<>();
        played.put(BPOP, csvLine[5].equalsIgnoreCase("y"));
        played.put(SPELL, csvLine[6].equalsIgnoreCase("y"));
        played.put(PICMATCH, csvLine[7].equalsIgnoreCase("y"));
        played.put(AKIRA, csvLine[8].equalsIgnoreCase("y"));
        played.put(WRITE, csvLine[9].equalsIgnoreCase("y"));
        played.put(NUMCOMPARE, csvLine[10].equalsIgnoreCase("y"));

        new Student(id, photoFile, lvls, played);
    }

    /**
     * Student constructed
     * @param id student ID
     * @param photoFile location of Student file
     * @param levels map of domains (LIT, MATH, STORY) to the student integer levels
     * @param tutors map of tutor names (bpop, numcompare, etc) to booleans (have they played?)
     */
    private Student(String id,
            String photoFile,
            Map<String, Integer> levels,
            Map<String, Boolean> tutors) {
        this.id = id;
        this.photoFile = photoFile;
        this.levels = levels;
        this.tutors = tutors;
    }

    @Override
    public String toString() {
        StringBuilder x = (new StringBuilder())
                .append("id=").append(id)
                .append(";photo=").append(photoFile);

        for (String k : levels.keySet()) {
            x.append(";lvl-").append(k)
                    .append("=").append(levels.get(k));
        }

        for (String k: tutors.keySet()) {
            x.append(";tut-").append(k)
                    .append("=").append(tutors.get(k));
        }
        return x.toString();
    }
}