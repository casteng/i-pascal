package com.siberika.idea.pascal.lang.lexer;

import com.intellij.ide.DataManager;
import com.intellij.lexer.FlexAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayCharSequence;
import com.siberika.idea.pascal.jps.sdk.PascalSdkData;
import com.siberika.idea.pascal.sdk.BasePascalSdkType;
import com.siberika.idea.pascal.sdk.DefinesParser;
import com.siberika.idea.pascal.util.StrUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 05/04/2013
 */
public class PascalFlexLexerImpl extends _PascalLexer {

    private static final Logger LOG = Logger.getInstance(PascalFlexLexerImpl.class);

    private int curLevel = -1;
    private int inactiveLevel = -1;

    private Set<String> defines;

    private VirtualFile virtualFile;
    private Project project;
    private AsyncResult<DataContext> dataContextResult;
    private DataContext dataContext;

    public void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    PascalFlexLexerImpl(Reader in, Project project, VirtualFile virtualFile) {
        super(in);
        this.virtualFile = virtualFile;
        this.project = project;
        if (null == virtualFile) {
            getDataContext();
        } else if (null == project) {
            this.project = ProjectLocator.getInstance().guessProjectForFile(virtualFile);
        }
    }

    @Override
    public void reset(CharSequence buffer, int start, int end, int initialState) {
        super.reset(buffer, 0, end, YYINITIAL);
        defines = null;
        curLevel = -1;
        inactiveLevel = -1;
    }

    private DataContext getDataContext() {
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
    }

    private <T> T getData(String s) {
        DataContext dataContext = getDataContext();
        if (dataContext != null) {
            return (T) dataContext.getData(s);
        }
        return null;
    }

    private Set<String> getDefines() {
        if ((null == defines) || (defines.isEmpty())) {
            initDefines(getProject(), getVirtualFile());
        }
        return defines;
    }

    private Project getProject() {
        if (isValidProject(project)) {
            return project;
        }
        project = getData(PlatformDataKeys.PROJECT.getName());
        if (!isValidProject(project)) {
            project = null;
        }
        return project;
    }

    private VirtualFile getVirtualFile() {
        if (virtualFile != null) {
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

    private static Sdk getSdk(Project project, VirtualFile virtualFile) {
        if (virtualFile != null) {
            Module module = ModuleUtil.findModuleForFile(virtualFile, project);
            if (module != null) {
                return ModuleRootManager.getInstance(module).getSdk();
            }
        }
        return ProjectRootManager.getInstance(project).getProjectSdk();
    }

    @Override
    public CharSequence getIncludeContent(CharSequence text) {
        return new CharArrayCharSequence("{Some text}".toCharArray());
    }

    @Override
    public void define(CharSequence sequence) {
        String name = extractDefineName(sequence);
        getDefines().add(name);
    }

    @Override
    public void unDefine(CharSequence sequence) {
        String name = extractDefineName(sequence);
        getDefines().remove(name);
    }

    synchronized private void initDefines(Project project, VirtualFile virtualFile) {
        defines = new HashSet<String>();
        if ((project != null)) {
            final Sdk sdk = getSdk(project, virtualFile);
            if ((null != sdk) && (sdk.getSdkType() instanceof BasePascalSdkType)) {
                SdkAdditionalData data = sdk.getSdkAdditionalData();
                if (data instanceof PascalSdkData) {
                    String options = (String) ((PascalSdkData) data).getValue(PascalSdkData.DATA_KEY_COMPILER_OPTIONS);
                    getDefinesFromCmdLine(options);
                    String family = (String) ((PascalSdkData) data).getValue(PascalSdkData.DATA_KEY_COMPILER_FAMILY);
                    defines.addAll(DefinesParser.getDefaultDefines(family, sdk.getVersionString()));
                }
            }
/*            for (String define : defines) {
                LOG.info("===*** def: " + define);
            }*/
        }
    }

    private void getDefinesFromCmdLine(@Nullable String options) {
        if (null == options) {
            return;
        }
        String[] compilerOptions = options.split("\\s+");
        for (String opt : compilerOptions) {
            if (opt.startsWith("-d")) {
                defines.add(opt.substring(2));
            }
        }
    }

    private IElementType doHandleIfDef(CharSequence sequence, boolean negate) {
        String name = extractDefineName(sequence);
        curLevel++;
        if ((!getDefines().contains(name) ^ negate) && (!isInactive())) {
            inactiveLevel = curLevel;
            yybegin(INACTIVE_BRANCH);
        }
        return CT_DEFINE;
    }

    @Override
    public IElementType handleIf(CharSequence sequence) {
        return doHandleIfDef(sequence, false);
    }

    @Override
    public IElementType handleIfDef(CharSequence sequence) {
        return doHandleIfDef(sequence, false);
    }

    @Override
    public IElementType handleIfNDef(CharSequence sequence) {
        return doHandleIfDef(sequence, true);
    }

    @Override
    public IElementType handleIfOpt(CharSequence sequence) {
        return doHandleIfDef("NOT DEFINED", true);
    }

    @Override
    public IElementType handleElse() {
        if (curLevel < 0) { return TokenType.BAD_CHARACTER; }
        if (isInactive()) {
            if (curLevel == inactiveLevel) {
                yybegin(YYINITIAL);
            }
        } else {
            inactiveLevel = curLevel;
            yybegin(INACTIVE_BRANCH);
        }
        return CT_DEFINE;
    }

    @Override
    public IElementType handleEndIf() {
        if (curLevel < 0) { return TokenType.BAD_CHARACTER; }
        if (curLevel == inactiveLevel) {
            yybegin(YYINITIAL);
        }
        curLevel--;
        return CT_DEFINE;
    }

    @Override
    public IElementType handleInclude(CharSequence sequence) {
        String name = extractIncludeName(sequence);
        Project project = getProject();
        VirtualFile virtualFile = getVirtualFile();
        if ((!StringUtils.isEmpty(name)) && (project != null)) {
            Reader reader = null;
            try {
                VirtualFile incFile = com.siberika.idea.pascal.util.ModuleUtil.getIncludedFile(project, virtualFile, name);
                if ((incFile != null) && (incFile.getCanonicalPath() != null)) {
                    reader = new FileReader(incFile.getCanonicalPath());
                    PascalFlexLexerImpl lexer = new PascalFlexLexerImpl(reader, project, incFile);
                    Document doc = FileDocumentManager.getInstance().getDocument(incFile);
                    if (doc != null) {
                        lexer.reset(doc.getCharsSequence(), 0);
                        lexer.setVirtualFile(incFile);
                        FlexAdapter flexAdapter = new FlexAdapter(lexer);
                        while (flexAdapter.getTokenType() != null) {
                            flexAdapter.advance();
                        }
                        getDefines().addAll(lexer.getDefines());
                    }
                } else {
                    LOG.info(String.format("WARNING: Include %s referenced from %s not found", name, getVFName(virtualFile)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return CT_DEFINE;
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

    private static final Pattern PATTERN_DEFINE = Pattern.compile("\\{\\$\\w+\\s+(\\w+)\\}");
    private static String extractDefineName(CharSequence sequence) {
        Matcher m = PATTERN_DEFINE.matcher(sequence);
        return m.matches() ? m.group(1).toUpperCase() : null;
    }

    private static String extractIncludeName(CharSequence sequence) {
        return StrUtil.getIncludeName(sequence.toString());
    }

}
