package cmu.xprize.robotutor.tutorengine.util;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-10-04.
 */
public interface IStudentDataModel {

    String getStudentId();

    void initializeTutorPositions(TransitionMatrixModel matrix);

    void createNewStudent();

    String getHasPlayed();

    String getWritingTutorID();

    void updateWritingTutorID(String id, boolean saves);

    String getStoryTutorID();

    void updateStoryTutorID(String id, boolean save);

    String getMathTutorID();

    void updateMathTutorID(String id, boolean save);

    String getActiveSkill();

    void updateActiveSkill(String skill, boolean save);

    void incrementActiveSkill(boolean save);

    String getLastSkill();

    boolean getWritingPlacement();

    int getWritingPlacementIndex();

    boolean getMathPlacement();

    int getMathPlacementIndex();

    void updateLastTutor(String activeTutorId, boolean save);

    String getLastTutor();

    void updateMathPlacement(boolean b, boolean save);

    void updateMathPlacementIndex(Integer i, boolean save);

    void updateWritingPlacement(boolean b, boolean save);

    void updateWritingPlacementIndex(Integer i, boolean save);

    int getTimesPlayedTutor(String tutor);

    void updateTimesPlayedTutor(String tutor, int i, boolean save);

    void saveAll();

    String toLogString();
}
