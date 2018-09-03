package com.siberika.idea.pascal.lang.lexer;

import com.google.common.base.Preconditions;
import com.intellij.ide.DataManager;
import com.intellij.lexer.FlexAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.intellij.util.containers.HashMap;
import com.intellij.util.io.BaseInputStreamReader;
import com.siberika.idea.pascal.lang.psi.PasTypes;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.Define;
import com.siberika.idea.pascal.util.StrUtil;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Author: George Bakhtadze
 * Date: 05/04/2013
 */
public class PascalFlexLexerImpl extends _PascalLexer {

    private static final Logger LOG = Logger.getInstance(PascalFlexLexerImpl.class);
    // Files of size less than this will be re-lexed on edit to correctly highlight potentially affected conditional blocks of code
    private static final int DEFINE_CORRECT_HIGHLIGHT_THRESHOLD = 120000;

    // current conditional compilation level
    private int curLevel = 0;
    // level on which inactive code branch started
    private int inactiveLevel = 0;
    // IF with True condition flag
    private int conditionStack = 0;

    // (Offset(32), ifValueStack(16), curLevel(8), inactiveLevel(8)) - offset, stack of IF condition values, current conditional compilation level, level on which inactive code branch started
    private List<Long> levels = new SmartList<Long>();
    // (Offset, defineName). Negative offset - undefine.
    private List<Pair<Integer, String>> defines = new SmartList<Pair<Integer, String>>();

    private Set<String> actualDefines;
    // TODO: replace with defines
    private Map<String, Define> allDefines;

    private VirtualFile virtualFile;
    private Project project;
    private final boolean incremental;
    private AsyncResult<DataContext> dataContextResult;
    private DataContext dataContext;

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public PascalFlexLexerImpl(Reader in, Project project, VirtualFile virtualFile, boolean incremental) {
        super(in);
        Preconditions.checkArgument((project != null) || incremental, "No project in non-incremental lexer");
        this.virtualFile = virtualFile;
        this.project = project;
        this.incremental = incremental;
        if ((null == virtualFile) && incremental) {
            getDataContext();
        } else if (null == project) {
            this.project = ProjectLocator.getInstance().guessProjectForFile(virtualFile);
        }
    }

    @Override
    public void reset(CharSequence buffer, int start, int end, int initialState) {
        super.reset(buffer, start, end, initialState);
//        System.out.println(String.format("===reset: [%d - %d], %d", start, end, initialState));
//        super.reset(buffer, 0, end, YYINITIAL);
        levels = levels.subList(0, getLevelIndex(start));
        if (levels.isEmpty()) {
            curLevel = 0;
            inactiveLevel = 0;
            conditionStack = 0;
        } else {
            int value = levels.get(levels.size() - 1).intValue();
            conditionStack = (value >> 16) & 0xFFFF;
            curLevel = (value >> 8) & 0xFF;
            inactiveLevel = value & 0xFF;
        }
        actualDefines = null;
        allDefines = null;
        actualDefines = getActualDefines();
        defines = adjustDefines(actualDefines, defines, start);
    }

    private List<Pair<Integer, String>> adjustDefines(Set<String> defines, List<Pair<Integer, String>> events, int offset) {
        for (int i = 0; i < events.size(); i++) {
            int ofs = events.get(i).getFirst();
            if (Math.abs(ofs) >= offset) {
                return events.subList(0, i);
            }
            if (ofs >= 0) {
                defines.add(events.get(i).getSecond());
            } else {
                defines.remove(events.get(i).getSecond());
            }
        }
        return events;
    }

    // Index of actual level change for offset. 0 - no changes.
    private int getLevelIndex(int start) {
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i) >> 32 > start) {
                return i;
            }
        }
        return 0;
    }

    private DataContext getDataContext() {
        try {
            if (dataContext != null) {
                return dataContext;
            }
            if (null == dataContextResult) {
                dataContextResult = DataManager.getInstance().getDataContextFromFocus();
            }
            if (dataContextResult.isDone()) {
                dataContext = dataContextResult.getResult();
            } else if (dataContextResult.isRejected()) {
                dataContextResult = DataManager.getInstance().getDataContextFromFocus();
            }
            return dataContext;
        } catch (Throwable t) {
            LOG.warn("-=Error=-", t);
            return null;
        }
    }

    private <T> T getData(String s) {
        DataContext dataContext = getDataContext();
        if (dataContext != null) {
            return (T) dataContext.getData(s);
        }
        return null;
    }

    private Set<String> getActualDefines() {
        if ((null == actualDefines) || (actualDefines.isEmpty())) {
            initDefines(getProject(), getVirtualFile());
        }
        return actualDefines;
    }

    public Map<String, Define> getAllDefines() {
        if ((null == allDefines) || (allDefines.isEmpty())) {
            initDefines(getProject(), getVirtualFile());
        }
        return allDefines;
    }

    private Project getProject() {
        if (isValidProject(project) || !incremental) {
            return project;
        }
        project = getData(PlatformDataKeys.PROJECT.getName());
        if (!isValidProject(project)) {
            project = null;
        }
        return project;
    }

    private VirtualFile getVirtualFile() {
        if ((virtualFile != null) || !incremental) {
            return virtualFile;
        }
        virtualFile = getData(PlatformDataKeys.VIRTUAL_FILE.getName());
        if (!isValidFile(virtualFile)) {
            virtualFile = null;
        }
        return virtualFile;
    }

    private static boolean isValidFile(VirtualFile result) {
        return result != null;
    }

    private static boolean isValidProject(Project project) {
        return (project != null) && !project.isDisposed() && (ProjectRootManager.getInstance(project) != null);
    }

    @Override
    public void define(int pos, CharSequence sequence) {
        String name = extractDefineName(sequence);
        if (StringUtils.isNotEmpty(name)) {
            String key = name.toUpperCase();
            getActualDefines().add(key);
            defines.add(Pair.create(pos, key));
            Map<String, Define> defs = allDefines;
            if (!BasePascalSdkType.DEFINE_IDE_PARSER.equals(key) || !defs.containsKey(key)) {
                defs.put(key, new Define(name, virtualFile, pos));
            }
            //if (incremental)System.out.println("Define: " + name);
        }
    }

    @Override
    public void unDefine(int pos, CharSequence sequence) {
        String name = extractDefineName(sequence);
        if (StringUtils.isNotEmpty(name)) {
            String key = name.toUpperCase();
            getActualDefines().remove(key);
            defines.add(Pair.create(-pos, key));
            allDefines.put(key, new Define(name, virtualFile, pos));
            //if (incremental)System.out.println("Undefine: " + name);
        }
    }

    synchronized private void initDefines(Project project, VirtualFile virtualFile) {
        actualDefines = new HashSet<String>();
        allDefines = new HashMap<String, Define>();
        if ((project != null)) {
            final Sdk sdk = com.siberika.idea.pascal.util.ModuleUtil.getSdk(project, virtualFile);
            if ((sdk != null) && (sdk.getVersionString() != null)) {
                allDefines.putAll(BasePascalSdkType.getDefaultDefines(sdk, sdk.getVersionString()));
            }
            for (Map.Entry<String, Define> entry : allDefines.entrySet()) {
                actualDefines.add(entry.getKey());
            }
        }
    }

    private IElementType doHandleIfDef(int pos, CharSequence sequence, boolean negate) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        String name = extractDefineName(sequence);
        curLevel++;
        if (!isInactive()) {
            if (StringUtils.isNotEmpty(name) && (!getActualDefines().contains(name.toUpperCase()) ^ negate)) {
                inactiveLevel = curLevel;
                pushCondition(false);
                yybegin(INACTIVE_BRANCH);
            } else {
                pushCondition(true);
            }
        } else {
            pushCondition(false);    // to balance with $endif directives
        }
        pushLevels(pos);
        return CT_DEFINE;
    }

    private IElementType doHandleIf(int pos, CharSequence sequence) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        curLevel++;
        String condition = extractCondition(sequence);
        if (!isInactive()) {
            if (StringUtils.isNotEmpty(condition) && (!ConditionParser.checkCondition(condition, getActualDefines()))) {
                inactiveLevel = curLevel;
                pushCondition(false);
                yybegin(INACTIVE_BRANCH);
            } else {
                pushCondition(true);
            }
        } else {
            pushCondition(false);    // to balance with $endif directives
        }
        pushLevels(pos);
        return CT_DEFINE;
    }

    @Override
    public IElementType handleIf(int pos, CharSequence sequence) {
        return doHandleIf(pos, sequence);
    }

    @Override
    public IElementType handleElseIf(int pos, CharSequence sequence) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        if (0 == curLevel) {
            VirtualFile virtualFile = getVirtualFile();
            getVFName(virtualFile);
            LOG.info(String.format("ERROR: $ELSEIF w/o $IF. Text: %s, file: %s", sequence, getVFName(virtualFile)));
            return doHandleIf(pos, sequence);
        }
        if (isLastConditionTrue()) {
            if (!isInactive()) {
                inactiveLevel = curLevel;
                yybegin(INACTIVE_BRANCH);
                pushLevels(pos);
            }
        } else {
            String condition = extractCondition(sequence);
            if (isInactive() && StringUtils.isNotEmpty(condition) && ConditionParser.checkCondition(condition, getActualDefines())) {
                if (curLevel == inactiveLevel) {
                    yybegin(YYINITIAL);
                    pushCondition(true);
                    pushLevels(pos);
                }
            }
        }
        return CT_DEFINE;
    }

    @Override
    public IElementType handleIfDef(int pos, CharSequence sequence) {
        return doHandleIfDef(pos, sequence, false);
    }

    @Override
    public IElementType handleIfNDef(int pos, CharSequence sequence) {
        return doHandleIfDef(pos, sequence, true);
    }

    @Override
    public IElementType handleIfOpt(int pos, CharSequence sequence) {
        return doHandleIfDef(pos, "NOT DEFINED", true);
    }

    @Override
    public IElementType handleElse(int pos) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        if (curLevel <= 0) {
            return TokenType.BAD_CHARACTER;
        }
        if (isInactive()) {
            if (!isLastConditionTrue() && (curLevel == inactiveLevel)) {
                yybegin(YYINITIAL);
            }
        } else {
            inactiveLevel = curLevel;
            yybegin(INACTIVE_BRANCH);
            pushLevels(pos);
        }
        return CT_DEFINE;
    }

    @Override
    public IElementType handleEndIf(int pos) {
        if (isConditionalsDisabled()) {
            return PasTypes.COMMENT;
        }
        if (curLevel <= 0) {
            return TokenType.BAD_CHARACTER;
        }
        if (curLevel == inactiveLevel) {
            yybegin(YYINITIAL);
        }
        popCondition();
        curLevel--;
        pushLevels(pos);
        return CT_DEFINE;
    }

    @Override
    public IElementType handleInclude(int pos, CharSequence sequence) {
        String name = extractIncludeName(sequence);
        Project project = getProject();
        VirtualFile virtualFile = getVirtualFile();
        if ((!StringUtils.isEmpty(name)) && (project != null)) {
            VirtualFile file = com.siberika.idea.pascal.util.ModuleUtil.getIncludedFile(project, virtualFile, name);
            PascalFlexLexerImpl lexer = !ObjectUtils.equals(virtualFile, file) ? processFile(project, file) : null;
            if (lexer != null) {
                getActualDefines().addAll(lexer.getActualDefines());
                allDefines.putAll(lexer.getAllDefines());
                for (Pair<Integer, String> define : lexer.defines) {
                    defines.add(Pair.create(define.first > 0 ? pos : -pos, define.second));
                }
                //TODO: put in levels
            } else {
                LOG.info(String.format("WARNING: Include %s referenced from %s not found", name, getVFName(virtualFile)));
            }
        }
        return INCLUDE;
    }

    private void pushCondition(boolean result) {
        conditionStack = (conditionStack << 1) | (result ? 1 : 0);
    }

    private void popCondition() {
        conditionStack = (conditionStack >> 1);
    }

    private boolean isLastConditionTrue() {
        return (conditionStack & 1) == 1;
    }

    private boolean isConditionalsDisabled() {
        return getActualDefines().contains(BasePascalSdkType.DEFINE_IDE_DISABLE_CONDITIONALS_);
    }

    private void pushLevels(int pos) {
        levels.add((long) (pos) << 32 + (conditionStack & 0xFF) << 16 + (curLevel << 8) + inactiveLevel);
    }

    // Process the file and return the new instance of lexer which processed it
    public static PascalFlexLexerImpl processFile(Project project, VirtualFile file) {
        Reader reader = null;
        try {
            if ((file != null) && (file.getCanonicalPath() != null)) {
                reader = new BaseInputStreamReader(file.getInputStream());
                PascalFlexLexerImpl lexer = new PascalFlexLexerImpl(reader, project, file, false);
                Document doc = FileDocumentManager.getInstance().getDocument(file);
                if (doc != null) {
                    lexer.reset(doc.getCharsSequence(), 0, doc.getTextLength(), YYINITIAL);
                    lexer.setVirtualFile(file);
                    FlexAdapter flexAdapter = new FlexAdapter(lexer);
                    while (flexAdapter.getTokenType() != null) {
                        flexAdapter.advance();
                    }
                    return lexer;
                }
            }
        } catch (IOException e) {
            LOG.info("Error processing file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @NotNull
    private static String getVFName(VirtualFile virtualFile) {
        return virtualFile != null ? virtualFile.getName() : "<unknown>";
    }

    @Override
    public IElementType getElement(IElementType elementType) {
        return elementType;
    }

    private boolean isInactive() {
        return yystate() == INACTIVE_BRANCH;
    }


    private static String extractDefineName(CharSequence sequence) {
        Matcher m = PATTERN_DEFINE.matcher(sequence);
        return m.matches() ? m.group(1) : null;
    }

    private static String extractCondition(CharSequence sequence) {
        Matcher m = PATTERN_CONDITION.matcher(sequence);
        return m.matches() ? m.group(2) : null;
    }

    private static String extractIncludeName(CharSequence sequence) {
        return StrUtil.getIncludeName(sequence.toString());
    }

    // Returns state modified if lexer state can be modified by a conditional define declared in the text
    public int getStateWithConditionals() {
        if ((yylength() > 0) && (virtualFile != null) && (virtualFile.getLength() < DEFINE_CORRECT_HIGHLIGHT_THRESHOLD)) {
            return yystate() + (levels.size() + defines.size()) * 10;
        }
        return yystate();
    }
}
