package org.kendar;

import org.kendar.utils.GD2Runner;
import org.kendar.utils.GD2ConnectedFunction;
import org.kendar.utils.GD2Exception;

public interface GD2Connection {
    <T> T runGoogle(String errorMessage, GD2ConnectedFunction action) throws GD2Exception;
    void run(GD2Runner action);
    boolean areJobsRunning();
    void waitForJobs();
    void waitAll(GD2Runner ... actions);
}
