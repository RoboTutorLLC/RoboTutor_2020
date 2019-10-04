package cmu.xprize.robotutor.tutorengine.util;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 2019-10-04.
 */
public interface IStudentDataModel {
    void initializeTutorPositions(TransitionMatrixModel matrix);

    void createNewStudent();

    String getHasPlayed();

    String getWritingTutorID();

    void updateWritingTutorID(String id);

    String getStoryTutorID();

    void updateStoryTutorID(String id);

    String getMathTutorID();

    void updateMathTutorID(String id);

    String getActiveSkill();

    void updateActiveSkill(String skill);

    void incrementActiveSkill();

    String getLastSkill();

    boolean getWritingPlacement();

    int getWritingPlacementIndex();

    boolean getMathPlacement();

    int getMathPlacementIndex();

    void updateLastTutor(String activeTutorId);

    String getLastTutor();

    void updateMathPlacement(boolean b);

    void updateMathPlacementIndex(Integer i);

    void updateWritingPlacement(boolean b);

    void updateWritingPlacementIndex(Integer i);

    int getTimesPlayedTutor(String tutor);

    void updateTimesPlayedTutor(String tutor, int i);

    String toLogString();
}
