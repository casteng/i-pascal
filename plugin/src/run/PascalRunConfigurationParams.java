package com.siberika.idea.pascal.run;

/**
 * Author: George Bakhtadze
 * Date: 07/01/2013
 */
public interface PascalRunConfigurationParams {
    String getParameters();
    String getWorkingDirectory();
    boolean getFixIOBuffering();
    void setParameters(String parameters);
    void setWorkingDirectory(String workingDirectory);
    void setFixIOBuffering(boolean value);
}
