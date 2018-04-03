package com.siberika.idea.pascal.run;

import com.intellij.execution.filters.AbstractFileHyperlinkFilter;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.FileHyperlinkRawData;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.util.ModuleUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 07/05/2017
 */
public class HeaptrcConsoleFilterProvider implements ConsoleFilterProvider {

    private static final Pattern PATTERN_HEAPTRC = Pattern.compile("\\s*\\$[0-9A-F]+ (\\w+, )?line (\\d+) of (.+)\n?");

    @NotNull
    @Override
    public Filter[] getDefaultFilters(@NotNull Project project) {
        if (ModuleUtil.hasPascalModules(project)) {
            return new Filter[]{
                    new AbstractFileHyperlinkFilter(project, FileUtil.expandUserHome("~/")) {
                        @NotNull
                        @Override
                        public List<FileHyperlinkRawData> parse(@NotNull String line) {
                            Matcher m = PATTERN_HEAPTRC.matcher(line);
                            if (m.matches()) {
                                List<FileHyperlinkRawData> res = new SmartList<FileHyperlinkRawData>();
                                String lineStr = m.group(2);
                                int lineNum = !StringUtils.isEmpty(lineStr) ? Integer.parseInt(lineStr) - 1 : 0;
                                res.add(new FileHyperlinkRawData(m.group(3), lineNum, 0, m.start(3), m.end(3)));
                                return res;
                            } else {
                                return Collections.emptyList();
                            }
                        }
                    }
            };
        } else {
            return new Filter[0];
        }
    }
}
