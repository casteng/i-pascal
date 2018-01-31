package com.siberika.idea.pascal.ide.actions;

import com.google.common.collect.ImmutableSet;
import com.intellij.ide.actions.CreateClassAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateTemplateInPackageAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.PascalIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import javax.swing.*;
import java.util.List;
import java.util.Properties;

/**
 * Author: George Bakhtadze
 * Date: 23/03/2015
 */
public class CreateModuleAction extends CreateTemplateInPackageAction<PsiFile> {
    private static final String PASCAL_TEMPLATE_PREFIX = "Pascal";

    CreateModuleAction() {
        super(PascalBundle.message("action.create.new.module"),
                PascalBundle.message("action.create.new.module"),
                PascalIcons.GENERAL,
                ImmutableSet.of(JavaSourceRootType.SOURCE, JavaSourceRootType.TEST_SOURCE));
    }

    @Nullable
    @Override
    protected PsiElement getNavigationElement(@NotNull PsiFile createdElement) {
        return createdElement.getNavigationElement();
    }

    @Override
    protected boolean checkPackageExists(PsiDirectory directory) {
        return DirectoryIndex.getInstance(directory.getProject()).getPackageName(directory.getVirtualFile()) != null;
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(PascalBundle.message("action.create.new.module"));
        for (FileTemplate fileTemplate : getApplicableTemplates(directory.getProject())) {
            final String templateName = fileTemplate.getName();
            final String shortName = getTemplateShortName(templateName);
            final Icon icon = getTemplateIcon();
            builder.addKind(shortName, icon, templateName);
        }
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName) {
        return PascalBundle.message("progress.creating.module", newName);
    }

    @Nullable
    @Override
    protected PsiFile doCreate(@NotNull PsiDirectory dir, String className, String templateName) throws IncorrectOperationException {
        String packageName = DirectoryIndex.getInstance(dir.getProject()).getPackageName(dir.getVirtualFile());
        try {
            return createClass(className, packageName, dir, templateName).getContainingFile();
        }
        catch (Throwable e) {
            throw new IncorrectOperationException(e.getMessage());
        }
    }

    private static PsiElement createClass(String className, String packageName, @NotNull PsiDirectory directory, final String templateName)
            throws Exception {
        return createClass(className, packageName, directory, templateName, CreateClassAction.class.getClassLoader());
    }

    private static PsiElement createClass(String className, String packageName, PsiDirectory directory, String templateName, @Nullable java.lang.ClassLoader classLoader)
            throws Exception {
        final Properties props = new Properties(FileTemplateManager.getInstance(directory.getProject()).getDefaultProperties());
        props.setProperty(FileTemplate.ATTRIBUTE_NAME, className);
        props.setProperty(FileTemplate.ATTRIBUTE_PACKAGE_NAME, packageName);

        final FileTemplate template = FileTemplateManager.getInstance(directory.getProject()).getInternalTemplate(templateName);

        return FileTemplateUtil.createFromTemplate(template, className, props, directory, classLoader);
    }

    private static List<FileTemplate> getApplicableTemplates(Project project) {
        return getApplicableTemplates(project, new Condition<FileTemplate>() {
            @Override
            public boolean value(FileTemplate fileTemplate) {
                return PascalFileType.INSTANCE.getDefaultExtension().equals(fileTemplate.getExtension());
            }
        });
    }

    private static List<FileTemplate> getApplicableTemplates(Project project, Condition<FileTemplate> filter) {
        List<FileTemplate> applicableTemplates = new SmartList<FileTemplate>();
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getInternalTemplates(), filter));
        applicableTemplates.addAll(ContainerUtil.findAll(FileTemplateManager.getInstance(project).getAllTemplates(), filter));
        return applicableTemplates;
    }

    private static String getTemplateShortName(String templateName) {
        if (templateName.startsWith(PASCAL_TEMPLATE_PREFIX)) {
            return templateName.substring(PASCAL_TEMPLATE_PREFIX.length());
        }
        return templateName;
    }

    @NotNull
    private static Icon getTemplateIcon() {
        return PascalIcons.GENERAL;
    }

}
