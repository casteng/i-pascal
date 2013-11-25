package com.siberika.idea.pascal.lang.compiled;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.fileTypes.ContentBasedFileSubstitutor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.siberika.idea.pascal.PPUFileType;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.sdk.FPCSdkType;
import com.siberika.idea.pascal.util.SysUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Author: George Bakhtadze
 * Date: 13/11/2013
 */
public class PPUFileDecompiler implements BinaryFileDecompiler {

    private static final Logger LOG = Logger.getInstance(PPUFileDecompiler.class);

    public static final String PPUDUMP_OPTIONS_COMMON = "-Vhisd";
    public static final String PPUDUMP_OPTIONS_FORMAT = "-Fx ";

    @NotNull
    @Override
    public CharSequence decompile(VirtualFile file) {
        assert file.getFileType() == PPUFileType.INSTANCE;

        final Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length == 0) return "";
        final Project project = projects[0];

        final ContentBasedFileSubstitutor[] processors = Extensions.getExtensions(ContentBasedFileSubstitutor.EP_NAME);
        for (ContentBasedFileSubstitutor processor : processors) {
            if (processor.isApplicable(project, file)) {
                return processor.obtainFileText(project, file);
            }
        }

        return PPUFileImpl.decompile(PsiManager.getInstance(project), file);
    }

    public static String decompileText(VirtualFile file, Sdk sdk) {
        if (null == sdk) { return PascalBundle.message("decompile.wrong.sdk"); }
        if ((sdk.getHomePath() == null) || !(sdk.getSdkType() instanceof FPCSdkType)) {
            return PascalBundle.message("decompile.wrong.sdktype");
        }
        File ppuDump = FPCSdkType.getPPUDumpExecutable(sdk.getHomePath());
        try {
            if (!ppuDump.isFile() || !ppuDump.canExecute()) {
                return PascalBundle.message("decompile.wrong.ppudump", ppuDump.getCanonicalPath());
            }
            //TODO: check version
            return PPUDumpParser.parse(SysUtils.runAndGetStdOut(sdk.getHomePath(), ppuDump.getCanonicalPath(),
                    PPUDUMP_OPTIONS_COMMON, PPUDUMP_OPTIONS_FORMAT, file.getCanonicalPath()));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return PascalBundle.message("decompile.io.error");
        } catch (ParseException e) {
            LOG.error(e.getMessage(), e);
            return PascalBundle.message("decompile.parse.error");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return PascalBundle.message("decompile.unknown.error");
        }
    }
}
