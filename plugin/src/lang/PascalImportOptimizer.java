package com.siberika.idea.pascal.lang;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasNamespaceIdent;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.impl.PascalModule;
import com.siberika.idea.pascal.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Author: George Bakhtadze
 * Date: 17/12/2015
 */
public class PascalImportOptimizer implements ImportOptimizer {
    static boolean isExcludedFromCheck(PasNamespaceIdent usedUnitName) {
        PsiElement prev = usedUnitName.getPrevSibling();
        return (prev instanceof PsiComment) && "{!}".equals(prev.getText());
    }

    public static UsedUnitStatus getUsedUnitStatus(PasNamespaceIdent usedUnitName) {
        if (isExcludedFromCheck(usedUnitName)) {
            return UsedUnitStatus.USED;
        }
        PascalModule module = PsiUtil.getElementPasModule(usedUnitName);
        if ((module != null)) {
            Pair<List<PascalNamedElement>, List<PascalNamedElement>> idents = module.getIdentsFrom(usedUnitName.getName());
            if (PsiUtil.belongsToInterface(usedUnitName)) {
                if (idents.getFirst().size() + idents.getSecond().size() == 0) {
                    return UsedUnitStatus.UNUSED;
                } else if (idents.getFirst().size() == 0) {
                    return UsedUnitStatus.USED_IN_IMPL;
                }
            } else if (idents.getSecond().size() == 0) {
                return UsedUnitStatus.UNUSED;
            }
        }
        return UsedUnitStatus.USED;
    }

    @Override
    public boolean supports(PsiFile file) {
        return (file instanceof PascalFile) && (file.getFileType() == PascalFileType.INSTANCE);
    }

    @NotNull
    @Override
    public Runnable processFile(PsiFile file) {
        final Map<PasNamespaceIdent, UsedUnitStatus> units = new TreeMap<PasNamespaceIdent, UsedUnitStatus>(new ByOffsetComparator<PasNamespaceIdent>().reversed());
        Collection<PasUsesClause> usesClauses = PsiTreeUtil.findChildrenOfType(file, PasUsesClause.class);

        for (PasNamespaceIdent usedUnitName : PsiUtil.findChildrenOfAnyType(PsiUtil.getElementPasModule(file), PasNamespaceIdent.class)) {
            if (PsiUtil.isUsedUnitName(usedUnitName)) {
                UsedUnitStatus status = PascalImportOptimizer.getUsedUnitStatus(usedUnitName);
                if (status != UsedUnitStatus.USED) {
                    units.put(usedUnitName, status);
                }
            }
        }

        PasUsesClause usesIntf = null;
        PasUsesClause usesImpl = null;
        for (PasUsesClause usesClause : usesClauses) {
            if (PsiUtil.belongsToInterface(usesClause)) {
                usesIntf = usesClause;
            } else {
                usesImpl = usesClause;
            }
        }
        final PasUsesClause usesInterface = usesIntf;
        final PasUsesClause usesImplementation = usesImpl;

        final Document doc = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);

        return new Runnable() {
            @Override
            public void run() {
                if (null == doc) {
                    return;
                }
                try {
                    int rIntf = usesInterface != null ? usesInterface.getNamespaceIdentList().size() : 0;
                    int rImpl = usesImplementation != null ? usesImplementation.getNamespaceIdentList().size() : 0;
                    int remIntf = rIntf;
                    int remImpl = rImpl;
                    for (Map.Entry<PasNamespaceIdent, UsedUnitStatus> unit : units.entrySet()) {                                // Check if one of the sections need to be removed completely
                        if (unit.getValue() == UsedUnitStatus.UNUSED) {
                            int index = getUnitIndex(usesInterface, unit.getKey());
                            if (index >= 0) {
                                rIntf--;
                            } else {
                                index = getUnitIndex(usesImplementation, unit.getKey());
                                if (index >= 0) {
                                    rImpl--;
                                }
                            }
                        }
                    }
                    if ((usesImplementation != null) && (0 == rImpl)) {                                                         //
                        remImpl = 0;
                        doc.deleteString(usesImplementation.getTextRange().getStartOffset(), getLfEndOffset(doc, usesImplementation.getTextRange().getEndOffset()));
                    }
                    if (0 == rIntf) {
                        remIntf = 0;
                    }
                    for (Map.Entry<PasNamespaceIdent, UsedUnitStatus> unit : units.entrySet()) {
                        System.out.println(String.format("=== optimize: %s (%s)", unit.getKey().getName(), unit.getValue()));   // do removal
                        if (unit.getValue() == UsedUnitStatus.UNUSED) {
                            if (removeUnitFromSection(doc, unit.getKey(), usesInterface, remIntf)) {
                                remIntf--;
                            } else {
                                if (removeUnitFromSection(doc, unit.getKey(), usesImplementation, remImpl)) {
                                    remImpl--;
                                }
                            }
                        }
                    }
                    if ((usesInterface != null) && (0 == rIntf)) {
                        doc.deleteString(usesInterface.getTextRange().getStartOffset(), getLfEndOffset(doc, usesInterface.getTextRange().getEndOffset()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private int getLfEndOffset(Document doc, int endOffset) {
        while ("\n".equals(doc.getText(TextRange.create(endOffset, endOffset + 1)))) {
            endOffset++;
        }
        return endOffset;
    }

    private static final Pattern UNITNAME_PREFIX = Pattern.compile("[{}!]");

    private boolean removeUnitFromSection(Document doc, PasNamespaceIdent usedUnit, PasUsesClause uses, int remainingInSection) {
        if ((0 == remainingInSection) || (null == uses)) {
            return false;
        }
        int index = getUnitIndex(uses, usedUnit);
        if (index < 0) {
            return false;
        }
        int start = expandRangeStart(doc, uses.getNamespaceIdentList().get(index).getTextRange().getStartOffset(), UNITNAME_PREFIX);
        int end = uses.getNamespaceIdentList().get(index).getTextRange().getEndOffset();                        // Single

        if (index > 0) {
            if (index == remainingInSection - 1) {                                             // Right
                start = uses.getNamespaceIdentList().get(index - 1).getTextRange().getEndOffset();
            } else {
                if (index < remainingInSection - 1) {                                          // Middle
                    end = expandRangeStart(doc, uses.getNamespaceIdentList().get(index + 1).getTextRange().getStartOffset(), UNITNAME_PREFIX);
                }
            }
        } else {
            if (index < remainingInSection - 1) {                                              // Left
                end = expandRangeStart(doc, uses.getNamespaceIdentList().get(index + 1).getTextRange().getStartOffset(), UNITNAME_PREFIX);
            }
        }
        doc.deleteString(start, end);
        return true;
    }

    // Expands range's start for symbols matching pattern
    private int expandRangeStart(Document doc, int start, Pattern pattern) {
        while ((start > 0) && (pattern.matcher(doc.getText(TextRange.create(start-1, start)))).matches()) {
            start--;
        }
        return start;
    }

    private int getUnitIndex(PasUsesClause uses, PasNamespaceIdent usedUnit) {
        if (null == uses) {
            return -1;
        }
        for (int i = 0; i < uses.getNamespaceIdentList().size(); i++) {
            if (usedUnit.equals(uses.getNamespaceIdentList().get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static class ByOffsetComparator<T extends PsiElement> implements Comparator<T> {
        @Override
        public int compare(PsiElement o1, PsiElement o2) {
            return o1.getTextRange().getStartOffset() - o2.getTextRange().getStartOffset();
        }
    }

}
