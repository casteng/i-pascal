package com.siberika.idea.pascal.module;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectData {
    private String name;
    private List<String> units = new ArrayList<>();
    private String includeFilesPath;
    private String otherUnitFilesPath;
    private String unitOutputDirectory;
    private String mainFile;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addUnitPath(String filename) {
        if (filename != null) {
            units.add(filename.replace("\\", File.separator));
        }
    }

    public List<String> getUnits() {
        return units;
    }

    public void setIncludeFilesPath(String includeFilesPath) {
        this.includeFilesPath = includeFilesPath;
    }

    public String getIncludeFilesPath() {
        return includeFilesPath;
    }

    public void setOtherUnitFilesPath(String otherUnitFilesPath) {
        this.otherUnitFilesPath = otherUnitFilesPath;
    }

    public String getOtherUnitFilesPath() {
        return otherUnitFilesPath;
    }

    public void setUnitOutputDirectory(String unitOutputDirectory) {
        this.unitOutputDirectory = unitOutputDirectory;
    }

    public String getUnitOutputDirectory() {
        return unitOutputDirectory;
    }

    public String getMainFile() {
        return mainFile;
    }

    public void setMainFile(String mainFile) {
        this.mainFile = mainFile;
    }
}
