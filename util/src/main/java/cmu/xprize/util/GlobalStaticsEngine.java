package cmu.xprize.util;

/**
 * GlobalStaticsEngine
 * <p>CTutorEngine and RoboTutor hold some important variables that are impossible for
 * classes to access if they are lower on the dependency tree hierarchy. This class is for holding
 * variables that need to be accessed by those classes.</p>
 * Created by kevindeland on 2019-10-27.
 */
public class GlobalStaticsEngine {

    private static String currentTutorId;

    private static String currentDomain;

    private static int currentLevel;

    private static String currentTutorType;

    public static String getCurrentTutorId() {
        return currentTutorId;
    }

    public static void setCurrentTutorId(String currentTutorId) {
        GlobalStaticsEngine.currentTutorId = currentTutorId;
    }

    public static String getCurrentDomain() {
        return currentDomain;
    }

    public static void setCurrentDomain(String currentDomain) {
        GlobalStaticsEngine.currentDomain = currentDomain;
    }

    public static int getCurrentLevel() {
        return currentLevel;
    }

    public static void setCurrentLevel(int currentLevel) {
        GlobalStaticsEngine.currentLevel = currentLevel;
    }

    public static String getCurrentTutorType() {
        return currentTutorType;
    }

    public static void setCurrentTutorType(String currentTutorType) {
        GlobalStaticsEngine.currentTutorType = currentTutorType;
    }
}
