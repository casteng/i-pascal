package com.siberika.idea.pascal.module;

import com.intellij.openapi.diagnostic.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DPRParser {
    private static final Logger LOG = Logger.getInstance(DPRParser.class);

    private static final Pattern PATTERN_USES_START = Pattern.compile("(?i)uses.*");
//    private static final Pattern PATTERN_UNITS = Pattern.compile("(?i)\\s*(((uses)|,)\\s+)?([\\w.]+(\\s+in '.*?')?)(\\s*,|([\\w.](\\s+in '.*?')?)\\s*)*\\s*;?");
    private static final Pattern PATTERN_UNITS = Pattern.compile("(?i)in\\s*'(.*?)'");
    private static final int MAX_LINES = 1000;

    public static ProjectData parse(File file) {
        ProjectData result = new ProjectData();
        result.addUnitPath(file.getName());
        result.setMainFile(file.getName());
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            int lineCnt = 0;
            boolean usesStarted = false;
            String line = reader.readLine();
            while ((lineCnt < MAX_LINES) && (line != null)) {
                line = line.trim();
                if (!usesStarted) {
                    usesStarted = PATTERN_USES_START.matcher(line).matches();
                }
                if (usesStarted) {
                    Matcher m = PATTERN_UNITS.matcher(line);
                    while (m.find()) {
                        String path = m.group(1);
                        result.addUnitPath(path);
                        LOG.debug(String.format("Found unit path: %s", path));
                    }
                    if (line.endsWith(";")) {
                        break;
                    }
                }
                line = reader.readLine();
                lineCnt++;
            }
            reader.close();
        } catch (IOException e) {
            LOG.error(String.format("Error processing file %s", file.getPath()));
        }
        return result;
    }
}
