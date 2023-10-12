package com.siberika.idea.pascal.lang;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.jps.compiler.CompilerMessager;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 04/12/2019
 */
class PascalSyntaxCheckResult implements CompilerMessager {
    public static final Logger LOG = Logger.getInstance(PascalSyntaxCheckResult.class.getName());

    private static final List<String> MSG_ID_FILTERED = Arrays.asList("2013", "10022");
    
    private final int lineCount;
    private final Path path;

    PascalSyntaxCheckResult(PascalAnnotatorInfo annotatorInfo) {
        this.lineCount = annotatorInfo.getLineCount();
        this.path = new File(annotatorInfo.getFile().getVirtualFile().getPath()).toPath();
    }

    enum SEVERITY {ERROR, WARNING, INFO, HINT}

    private List<AnnotationItem> results = null;

    @Override
    public void hint(String msgId, String msg, String path, long line, long column) {
        addItem(SEVERITY.HINT, msgId, msg, path, line, column);
    }

    @Override
    public void info(String msgId, String msg, String path, long line, long column) {
        addItem(SEVERITY.INFO, msgId, msg, path, line, column);
    }

    @Override
    public void warning(String msgId, String msg, String path, long line, long column) {
        addItem(SEVERITY.WARNING, msgId, msg, path, line, column);
    }

    @Override
    public void error(String msgId, String msg, String path, long line, long column) {
        addItem(SEVERITY.ERROR, msgId, msg, path, line, column);
    }

    private void addItem(SEVERITY severity, String msgId, String msg, String path, long line, long column) {
        if (!isSameFilename(path) || isFiltered(msgId)) {
            LOG.debug(String.format("Filtered compiler output. msgId: %s, path: %s", msgId, path));
            return;
        }
        if ((line >= 0) && (line < lineCount)) {
            if (null == results) {
                results = new SmartList<>();
            }
            results.add(new AnnotationItem(severity, msg, line-1, column > 0 ? column-1 : 0));
        }
    }

    private boolean isSameFilename(String path) {
        return (path != null) && this.path.equals(new File(path).toPath());
    }

    private boolean isFiltered(String msgId) {
        return MSG_ID_FILTERED.contains(msgId);
    }

    class AnnotationItem {
        final SEVERITY severity;
        final String msg;
        final long line;
        final long column;

        private AnnotationItem(SEVERITY severity, String msg, long line, long column) {
            this.severity = severity;
            this.msg = msg;
            this.line = line;
            this.column = column;
        }
    }

    List<AnnotationItem> getResults() {
        return results;
    }
}
