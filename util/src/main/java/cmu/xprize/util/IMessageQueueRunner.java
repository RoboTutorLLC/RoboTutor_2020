package cmu.xprize.util;

/**
 * RoboTutor
 * <p>
 * Created by kevindeland on 8/27/19.
 */

public interface IMessageQueueRunner {

    void runCommand(String command);

    void runCommand(String command, Object target);

    void runCommand(String command, String target);
}
