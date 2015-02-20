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
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.text.CharArrayCharSequence;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.jps.util.FileUtil;
import com.siberika.idea.pascal.sdk.DefinesParser;
import com.siberika.idea.pascal.sdk.FPCSdkType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    private Set<String> defines = null;

    private final VirtualFile virtualFile;

    PascalFlexLexerImpl(Reader in, VirtualFile virtualFile) {
        super(in);
        this.virtualFile = virtualFile;
    }

    public Set<String> getDefines() {
        if ((null == defines) || (defines.isEmpty())) {
            initDefines(getProject(), getVirtualFile());
        }
        return defines;
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
            if ((null != sdk) && (sdk.getSdkType() instanceof FPCSdkType)) {
                defines.addAll(DefinesParser.getDefaultDefines(DefinesParser.COMPILER_FPC, sdk.getVersionString()));
            }
        }
    }

    private <T> T getData(String s) {
        DataContext res = DataManager.getInstance().getDataContextFromFocus().getResultSync(100);
        if (res != null) {
            return (T) res.getData(s);
        }
        return null;
    }

    private Project getProject() {
        Project result = getData(PlatformDataKeys.PROJECT.getName());
        if (!isValidProject(result)) {
            LOG.warn("Couldn't determine active project");
        }
        return result;
    }

    private VirtualFile getVirtualFile() {
        if (virtualFile != null) {
            return virtualFile;
        }
        VirtualFile result = getData(PlatformDataKeys.VIRTUAL_FILE.getName());
        if (!isValidFile(result)) {
            LOG.warn("Couldn't determine active file");
        }
        return result;
    }

    private boolean isValidFile(VirtualFile result) {
        return result != null;
    }

    private static boolean isValidProject(Project project) {
        return (project != null) && (ProjectRootManager.getInstance(project) != null);
    }

    private Sdk getSdk(Project project, VirtualFile virtualFile) {
        if (virtualFile != null) {
            Module module = ModuleUtil.findModuleForFile(virtualFile, project);
            if (module != null) {
                return ModuleRootManager.getInstance(module).getSdk();
            }
        }
        return ProjectRootManager.getInstance(project).getProjectSdk();
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
        if ((project != null) && (virtualFile != null)) {
            try {
                VirtualFile incFile = getIncludedFile(project, virtualFile, name);
                if ((incFile != null) && (incFile.getCanonicalPath() != null)) {
                    Reader reader = new FileReader(incFile.getCanonicalPath());
                    PascalFlexLexerImpl lexer = new PascalFlexLexerImpl(reader, incFile);
                    Document doc = FileDocumentManager.getInstance().getDocument(incFile);
                    if (doc != null) {
                        lexer.reset(doc.getCharsSequence(), 0);
                        FlexAdapter flexAdapter = new FlexAdapter(lexer);
                        while (flexAdapter.getTokenType() != null) {
                            flexAdapter.advance();
                        }
                        getDefines().addAll(lexer.getDefines());
                    }
                } else {
                    System.out.println("*** include " + name + " referenced from " + virtualFile.getName() + " not found");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return CT_DEFINE;
    }

    /**
     * Locates file specified in include directive and its full file name trying the following:
     *   1. if name specifies an absolute path return it
     *   2. search in the directory where the current source file is located
     *   3. search in all paths in search path
     *   if name doesn't include file extension and file doesn't exists ".pas" and ".pp" are tried sequentially
     * @param project - used to retrieve list of search paths project
     * @param referencing - file which references to the include
     * @param name - name found in include directive
     * @return file name or null if not found
     */
    private VirtualFile getIncludedFile(Project project, VirtualFile referencing, String name) throws IOException {
        File file = new File(name);
        if (file.isAbsolute()) {
            return tryExtensions(file);
        }

        if (referencing != null) {
            String path = referencing.getParent().getPath();
            VirtualFile result = tryExtensions(new File(path, name));
            if (result != null) {
                return result;
            }

            Module module = ModuleUtil.findModuleForFile(referencing, project);

            return module != null ? trySearchPath(module, name) : null;
        } else {
            return null;
        }
    }

    private  List<String> includeExtensions = Arrays.asList(null, "pas", "pp");

    private VirtualFile trySearchPath(Module module, String name) {
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PascalFileType.INSTANCE,
                GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false));
        for (VirtualFile virtualFile : virtualFiles) {
            String ext = FileUtil.getExtension(name);
            if (ext != null) {
                if (name.equalsIgnoreCase(virtualFile.getName())) {
                    return virtualFile;
                }
            } else if (name.equalsIgnoreCase(virtualFile.getNameWithoutExtension())) {
                if (includeExtensions.contains(virtualFile.getExtension())) {
                    return virtualFile;
                }
            }
        }
        return null;
    }

    private VirtualFile tryExtensions(File file) throws IOException {
        if (!file.isFile() && (FileUtil.getExtension(file.getName()) == null)) {
            String filename = file.getCanonicalPath();
            file = new File(filename + "." + includeExtensions.get(1));
            if (!file.isFile()) {
                file = new File(filename + "." + includeExtensions.get(2));
            }
        }
        return FileUtil.getVirtualFile(file.getCanonicalPath());
    }

    @Override
    public IElementType getElement(IElementType elementType) {
        return elementType;
    }

    private boolean isInactive() {
        return yystate() == INACTIVE_BRANCH;
    }

    private static Pattern patternDefine = Pattern.compile("\\{\\$\\w+\\s+(\\w+)\\}");
    private static String extractDefineName(CharSequence sequence) {
        Matcher m = patternDefine.matcher(sequence);
        return m.matches() ? m.group(1).toUpperCase() : null;
    }

    private static Pattern patternInclude = Pattern.compile("\\{\\$\\w+\\s+([\\w.]+)\\}");
    private static String extractIncludeName(CharSequence sequence) {
        Matcher m = patternInclude.matcher(sequence);
        return m.matches() ? m.group(1) : null;
    }

}
