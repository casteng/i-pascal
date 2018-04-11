package com.siberika.idea.pascal.ide.actions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.siberika.idea.pascal.lang.PascalImportOptimizer;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.siberika.idea.pascal.PascalBundle.message;

public class UsesQuickFixes {

    public static class ExcludeUnitAction extends BaseUsesFix {
        @Nls
        @NotNull
        @Override
        public String getName() {
            return message("action.uses.exclude");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            if (null == usedUnitName) {
                return;
            }
            final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
            if (doc != null) {
                doc.insertString(usedUnitName.getTextRange().getStartOffset(), "{!}");
                PsiDocumentManager.getInstance(project).commitDocument(doc);
            }
        }
    }

    public static class OptimizeUsesAction extends BaseUsesFix {
        @Nls
        @NotNull
        @Override
        public String getName() {
            return message("action.uses.optimize");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            if (usedUnitName != null) {
                PascalImportOptimizer.doProcess(usedUnitName.getContainingFile()).run();
            }
        }
    }

    public static class MoveUnitAction extends BaseUsesFix {
        @Nls
        @NotNull
        @Override
        public String getName() {
            return message("action.uses.move");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            TextRange range = getRangeToRemove(usedUnitName);
            if (range != null) {
                final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                if (doc != null) {
                    PascalImportOptimizer.addUnitToSection(PsiUtil.getElementPasModule(usedUnitName),
                            Collections.singletonList(((PascalQualifiedIdent)usedUnitName).getName()), false);
                    doc.deleteString(range.getStartOffset(), range.getEndOffset());
                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                }
            }
        }
    }

    public static class RemoveUnitAction extends BaseUsesFix {
        @Nls
        @NotNull
        @Override
        public String getName() {
            return message("action.uses.remove");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement usedUnitName = descriptor.getPsiElement();
            TextRange range = getRangeToRemove(usedUnitName);
            if (range != null) {
                final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                if (doc != null) {
                    doc.deleteString(range.getStartOffset(), range.getEndOffset());
                    PsiDocumentManager.getInstance(project).commitDocument(doc);
                }
            }
        }
    }

    private static abstract class BaseUsesFix implements LocalQuickFix {
        @Nls
        @NotNull
        @Override
        public String getFamilyName() {
            return "Pascal";
        }

        static TextRange getRangeToRemove(PsiElement usedUnitName) {
            if ((usedUnitName instanceof PascalQualifiedIdent) && (usedUnitName.getParent() instanceof PasUsesClause)) {
                PasUsesClause usesClause = (PasUsesClause) usedUnitName.getParent();
                List<TextRange> ranges = PascalImportOptimizer.getUnitRanges(usesClause);
                TextRange res = PascalImportOptimizer.removeUnitFromSection((PascalQualifiedIdent) usedUnitName, usesClause, ranges, usesClause.getNamespaceIdentList().size());
                if ((res != null) && (usesClause.getNamespaceIdentList().size() == 1)) {                                                              // Remove whole uses clause if last unit removed
                    final Document doc = PsiDocumentManager.getInstance(usedUnitName.getProject()).getDocument(usedUnitName.getContainingFile());
                    res = TextRange.create(usesClause.getTextRange().getStartOffset(), DocUtil.expandRangeEnd(doc, usesClause.getTextRange().getEndOffset(), DocUtil.RE_LF));
                }
                return res;
            } else {
                return null;
            }
        }
    }

}
