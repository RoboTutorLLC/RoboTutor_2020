package cmu.xprize.util;

/**
 * IPerformanceTracker
 *
 * <p>currently only implemented by BigMath. Would ideally work for others as well.</p>
 *
 * Created by kevindeland on 10/7/18.
 */

public interface IPerformanceTracker {

    void trackAndLogPerformance(boolean correct, Object expected, Object actual);
}
