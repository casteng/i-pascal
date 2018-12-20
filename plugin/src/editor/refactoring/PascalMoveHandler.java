package com.siberika.idea.pascal.editor.refactoring;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.move.MoveCallback;
import com.intellij.refactoring.move.MoveHandlerDelegate;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;

public class PascalMoveHandler extends MoveHandlerDelegate {

    @Override
    public boolean canMove(PsiElement[] elements, @Nullable PsiElement targetContainer) {
        System.out.println(String.format("canMove: [%s] => %s", Arrays.toString(elements), StrUtil.toDebugString(targetContainer)));
        return super.canMove(elements, targetContainer);
    }

    @Override
    public boolean canMove(DataContext dataContext) {
        System.out.println("canMode()");
        return super.canMove(dataContext);
    }

    @Override
    public boolean isValidTarget(@Nullable PsiElement targetElement, PsiElement[] sources) {
        System.out.println(String.format("isValidTarget: [%s] <= %s", StrUtil.toDebugString(targetElement), Arrays.toString(sources)));
        return super.isValidTarget(targetElement, sources);
    }

    @Override
    public void doMove(Project project, PsiElement[] elements, @Nullable PsiElement targetContainer, @Nullable MoveCallback callback) {
        System.out.println("doMove");
        super.doMove(project, elements, targetContainer, callback);
    }

    @Override
    public PsiElement adjustTargetForMove(DataContext dataContext, PsiElement targetContainer) {
        System.out.println("adjustTargetForMove");
        return super.adjustTargetForMove(dataContext, targetContainer);
    }

    @Nullable
    @Override
    public PsiElement[] adjustForMove(Project project, PsiElement[] sourceElements, PsiElement targetElement) {
        System.out.println("adjustForMove");
        return super.adjustForMove(project, sourceElements, targetElement);
    }

    @Override
    public boolean tryToMove(PsiElement element, Project project, DataContext dataContext, @Nullable PsiReference reference, Editor editor) {
        System.out.println(String.format("tryToMove: %s, ref: %s", StrUtil.toDebugString(element), reference != null ? StrUtil.toDebugString(reference.getElement()) : "<null>"));
        return super.tryToMove(element, project, dataContext, reference, editor);
    }

    @Override
    public void collectFilesOrDirsFromContext(DataContext dataContext, Set<PsiElement> filesOrDirs) {
        System.out.println("collectFilesOrDirsFromContext: " + Arrays.toString(filesOrDirs.toArray()));
        super.collectFilesOrDirsFromContext(dataContext, filesOrDirs);
    }

    @Override
    public boolean isMoveRedundant(PsiElement source, PsiElement target) {
        System.out.println("isMoveRedundant");
        return super.isMoveRedundant(source, target);
    }
}
