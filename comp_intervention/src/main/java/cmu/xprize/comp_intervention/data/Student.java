package cmu.xprize.comp_intervention.data;

import android.util.Log;

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

    public String id;
    public String photoFile;
    public String groupId;
    Map<String, Integer> levels;
    Map<String, Boolean> tutors;

    /**
     * Student constructed from a single csv line
     * @param csvLine an array of Strings from a single CSV parsing
     */
    public Student (String[] csvLine) {
        String id = csvLine[0];
        String photoFile = csvLine[1];
        String groupId = csvLine[2];


        Log.i("9_14", "initializing Student " + id + photoFile);

        Map<String, Integer> lvls = new HashMap<>();
        lvls.put(MATH, Integer.parseInt(csvLine[3]));
        lvls.put(STORY, Integer.parseInt(csvLine[4]));
        lvls.put(LIT, Integer.parseInt(csvLine[5]));

        Map<String, Boolean> played = new HashMap<>();
        played.put(BPOP, csvLine[6].equalsIgnoreCase("y"));
        played.put(SPELL, csvLine[7].equalsIgnoreCase("y"));
        played.put(PICMATCH, csvLine[8].equalsIgnoreCase("y"));
        played.put(AKIRA, csvLine[9].equalsIgnoreCase("y"));
        played.put(WRITE, csvLine[10].equalsIgnoreCase("y"));
        played.put(NUMCOMPARE, csvLine[11].equalsIgnoreCase("y"));

        this.setVars(id, photoFile, groupId, lvls, played);
    }

    /**
     * Student constructed
     * @param id student ID
     * @param photoFile location of Student file
     * @param levels map of domains (LIT, MATH, STORY) to the student integer levels
     * @param tutors map of tutor names (bpop, numcompare, etc) to booleans (have they played?)
     */
    private void setVars(String id,
            String photoFile,
            String groupId,
            Map<String, Integer> levels,
            Map<String, Boolean> tutors) {
        this.id = id;
        this.photoFile = photoFile;
        this.groupId = groupId;
        this.levels = levels;
        this.tutors = tutors;
    }

    @Override
    public String toString() {

        StringBuilder x = (new StringBuilder())
                .append("id=").append(id)
                .append(";photo=").append(photoFile)
                .append(";group=").append(groupId);

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