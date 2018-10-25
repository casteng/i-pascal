package com.siberika.idea.pascal.lang;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.siberika.idea.pascal.PascalBundle;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.context.ContextUtil;
import com.siberika.idea.pascal.lang.parser.PascalFile;
import com.siberika.idea.pascal.lang.psi.PasLibraryModuleHead;
import com.siberika.idea.pascal.lang.psi.PasModule;
import com.siberika.idea.pascal.lang.psi.PasPackageModuleHead;
import com.siberika.idea.pascal.lang.psi.PasProgramModuleHead;
import com.siberika.idea.pascal.lang.psi.PasUsesClause;
import com.siberika.idea.pascal.lang.psi.PascalModule;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalQualifiedIdent;
import com.siberika.idea.pascal.lang.references.ResolveUtil;
import com.siberika.idea.pascal.util.DocUtil;
import com.siberika.idea.pascal.util.ModuleUtil;
import com.siberika.idea.pascal.util.PsiUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

    private static final Logger LOG = Logger.getInstance(PascalImportOptimizer.class);

    private static final Pattern RE_UNITNAME_PREFIX = Pattern.compile("[{}!]");

    private static final List<String> EXCLUDED_UNITS = Arrays.asList("CMEM", "HEAPTRC", "CTHREADS", "CWSTRING", "FASTMM4");

    public static UsedUnitStatus getUsedUnitStatus(PascalQualifiedIdent usedUnitName, Module module) {
        Project project = usedUnitName.getProject();
        Collection<PascalModule> units = ResolveUtil.findUnitsWithStub(project, module, usedUnitName.getName());
        PascalModule mod = units.isEmpty() ? null : units.iterator().next();
        if (null == mod) {
            return UsedUnitStatus.UNKNOWN;
        }
        UsedUnitStatus res = UsedUnitStatus.USED;
        if (EXCLUDED_UNITS.contains(usedUnitName.getName().toUpperCase())) {
            return res;
        }
        PascalModule pasModule = PsiUtil.getElementPasModule(usedUnitName);
        if ((pasModule != null)) {
            Pair<List<PascalNamedElement>, List<PascalNamedElement>> idents = pasModule.getIdentsFrom(usedUnitName.getName(), true, ModuleUtil.retrieveUnitNamespaces(module, project));
            if (ContextUtil.belongsToInterface(usedUnitName)) {
                if (idents.getFirst().size() + idents.getSecond().size() == 0) {
                    res = UsedUnitStatus.UNUSED;
                } else if (idents.getFirst().size() == 0) {
                    res = UsedUnitStatus.USED_IN_IMPL;
                }
            } else if (idents.getSecond().size() == 0) {
                res = UsedUnitStatus.UNUSED;
            }
        }
        return res;
    }

    @Override
    public boolean supports(PsiFile file) {
        return supportsOptimization(file);
    }

    private static boolean supportsOptimization(PsiFile file) {
        return (file instanceof PascalFile) && (file.getFileType() == PascalFileType.INSTANCE);
    }

    @NotNull
    @Override
    public Runnable processFile(final PsiFile file) {
        return doProcess(file);
    }

    public static Runnable doProcess(final PsiFile file) {
        final Map<PascalQualifiedIdent, UsedUnitStatus> units = new TreeMap<PascalQualifiedIdent, UsedUnitStatus>(new ByOffsetComparator<PascalQualifiedIdent>());
        Collection<PasUsesClause> usesClauses = PsiTreeUtil.findChildrenOfType(file, PasUsesClause.class);

        Module module = ModuleUtilCore.findModuleForPsiElement(file);
        //noinspection unchecked
        for (PascalQualifiedIdent usedUnitName : PsiUtil.findChildrenOfAnyType(PsiUtil.getElementPasModule(file), PascalQualifiedIdent.class)) {
            if (PsiUtil.isUsedUnitName(usedUnitName)) {
                UsedUnitStatus status = PascalImportOptimizer.getUsedUnitStatus(usedUnitName, module);
                if (status != UsedUnitStatus.USED) {
                    units.put(usedUnitName, status);
                }
            }
        }

        PasUsesClause usesIntf = null;
        PasUsesClause usesImpl = null;
        for (PasUsesClause usesClause : usesClauses) {
            if (ContextUtil.belongsToInterface(usesClause)) {
                usesIntf = usesClause;
            } else {
                usesImpl = usesClause;
            }
        }
        final PasUsesClause usesInterface = usesIntf;
        final PasUsesClause usesImplementation = usesImpl;

        final Document doc = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);

        return new CollectingInfoRunnable() {

            private String status;

            @Nullable
            @Override
            public String getUserNotificationInfo() {
                return status;
            }

            @Override
            public void run() {
                if (null == doc) {
                    return;
                }
                try {
                    int remIntf = usesInterface != null ? usesInterface.getNamespaceIdentList().size() : 0;
                    int remImpl = usesImplementation != null ? usesImplementation.getNamespaceIdentList().size() : 0;
                    List<TextRange> toRemoveIntf = new SmartList<TextRange>();
                    List<TextRange> toRemoveImpl = new SmartList<TextRange>();
                    List<TextRange> unitRangesIntf = getUnitRanges(usesInterface);
                    List<TextRange> unitRangesImpl = getUnitRanges(usesImplementation);
                    List<String> toMove = new SmartList<String>();
                    for (Map.Entry<PascalQualifiedIdent, UsedUnitStatus> unit : units.entrySet()) {                                // perform add operations
                        if (unit.getValue() == UsedUnitStatus.USED_IN_IMPL) {                                                   // move from interface to implementation
                            toMove.add(unit.getKey().getName());
                            remImpl++;
                        }
                    }
                    TextRange addedRange = addUnitToSection(PsiUtil.getElementPasModule(file), toMove, false);
                    if (addedRange != null) {
                        unitRangesImpl.add(addedRange);
                    }
                    int unknown = 0;
                    for (Map.Entry<PascalQualifiedIdent, UsedUnitStatus> unit : units.entrySet()) {                                // collect all removal ranges
                        if (unit.getValue() == UsedUnitStatus.USED_IN_IMPL) {                                                   // remove due to moving to implementation
                            TextRange range = removeUnitFromSection(unit.getKey(), usesInterface, unitRangesIntf, remIntf);
                            if (range != null) {
                                toRemoveIntf.add(range);
                                remIntf--;
                            }
                        } else if (unit.getValue() == UsedUnitStatus.UNUSED) {
                            TextRange range = removeUnitFromSection(unit.getKey(), usesInterface, unitRangesIntf, remIntf);
                            if (range != null) {
                                remIntf--;
                                toRemoveIntf.add(range);
                            } else {
                                range = removeUnitFromSection(unit.getKey(), usesImplementation, unitRangesImpl, remImpl);
                                if (range != null) {
                                    remImpl--;
                                    toRemoveImpl.add(range);
                                }
                            }
                        } else if (unit.getValue() == UsedUnitStatus.UNKNOWN) {
                            unknown++;
                        }
                    }
                    removeUnits(doc, usesImplementation, toRemoveImpl, remImpl); // remove implementation uses clause before other modifications
                    removeUnits(doc, usesInterface, toRemoveIntf, remIntf);
                    PsiDocumentManager.getInstance(file.getProject()).commitDocument(doc);
                    status = String.format("%d units moved to implementation, %d removed, %d unknown",
                            toMove.size(), toRemoveImpl.size() + toRemoveIntf.size() - toMove.size(), unknown);
                } catch (Exception e) {
                    LOG.info("Error", e);
                }
            }
        };
    }

    private static void removeUnits(Document doc, PasUsesClause clause, List<TextRange> toRemove, int remaining) {
        if ((clause != null) && (0 == remaining)) {
            doc.deleteString(clause.getTextRange().getStartOffset(), DocUtil.expandRangeEnd(doc, clause.getTextRange().getEndOffset(), DocUtil.RE_LF));
        } else {
            Collections.sort(toRemove, new ByOffsetComparator2());
            for (TextRange textRange : toRemove) {
                doc.deleteString(textRange.getStartOffset(), textRange.getEndOffset());
            }
        }
    }

    public static List<TextRange> getUnitRanges(PasUsesClause usesClause) {
        if (null == usesClause) {
            return new SmartList<TextRange>();
        }
        List<TextRange> res = new ArrayList<TextRange>(usesClause.getNamespaceIdentList().size());
        for (PascalQualifiedIdent ident : usesClause.getNamespaceIdentList()) {
            res.add(ident.getTextRange());
        }
        return res;
    }

    public static TextRange addUnitToSection(final PasModule module, List<String> names, boolean toInterface) {
        if ((null == module) || (names.isEmpty())) {
            return null;
        }
        assert (!toInterface || (module.getModuleType() == PascalModule.ModuleType.UNIT));
        final PasUsesClause uses;
        if (toInterface) {
            uses = PsiTreeUtil.findChildOfType(PsiUtil.getModuleInterfaceSection(module), PasUsesClause.class);
        } else {
            uses = PsiTreeUtil.findChildOfType((module.getModuleType() == PascalModule.ModuleType.UNIT) ? PsiUtil.getModuleImplementationSection(module) : module, PasUsesClause.class);
        }
        int offs = 0;
        String content = StringUtils.join(names, ", ");
        if (uses != null) {
            offs = uses.getTextRange().getEndOffset() - 1;
            content = ",\n" + content + ";";
        } else {
            content = "\n\nuses\n" + content + ";";
            @SuppressWarnings("unchecked") PsiElement prev = PsiTreeUtil.findChildOfAnyType(module, PasProgramModuleHead.class, PasLibraryModuleHead.class, PasPackageModuleHead.class);
            if (calcOffset(prev) >= 0) {
                offs = calcOffset(prev);
            } else {
                if (toInterface) {
                    PsiElement section = PsiUtil.getModuleInterfaceSection(module);
                    offs = section != null ? section.getTextRange().getStartOffset() + "interface".length() : offs;
                } else {
                    PsiElement section = PsiUtil.getModuleImplementationSection(module);
                    offs = section != null ? section.getTextRange().getStartOffset() + "implementation".length(): offs;
                }
            }
        }
        final PsiFile file = module.getContainingFile();
        Document doc = PsiDocumentManager.getInstance(module.getProject()).getDocument(file);
        if (doc != null) {
            DocUtil.adjustDocument(doc, offs, content);
            PsiDocumentManager.getInstance(module.getProject()).commitDocument(doc);
        }
        DocUtil.runCommandLaterInWriteAction(module.getProject(), PascalBundle.message("action.reformat"), new Runnable() {
            @Override
            public void run() {
                for (PasUsesClause usesClause : PsiTreeUtil.findChildrenOfType(module, PasUsesClause.class)) {
                    PsiManager manager = usesClause.getManager();
                    if (manager != null) {
                        CodeStyleManager.getInstance(manager).reformatRange(file, usesClause.getTextRange().getStartOffset(), usesClause.getTextRange().getEndOffset(), true);
                    }
                }
            }
        });
        return TextRange.create(offs + 2, offs + content.length());
    }

    private static int calcOffset(PsiElement prev) {
        return prev != null ? prev.getTextRange().getEndOffset() : -1;
    }

    public static TextRange removeUnitFromSection(PascalQualifiedIdent usedUnit, PasUsesClause uses, List<TextRange> unitRanges, int remaining) {
        if ((0 == remaining) || (null == uses) || (null == uses.getContainingFile())) {
            return null;
        }
        Document doc = PsiDocumentManager.getInstance(uses.getProject()).getDocument(uses.getContainingFile());
        int index = getUnitIndex(uses, usedUnit);
        if ((index < 0) || (null == doc)) {
            return null;
        }
        int start = DocUtil.expandRangeStart(doc, unitRanges.get(index).getStartOffset(), RE_UNITNAME_PREFIX);
        int end = unitRanges.get(index).getEndOffset();                        // Single

        if (index > 0) {
            if (index == remaining - 1) {                                                              // Right
                start = unitRanges.get(index - 1).getEndOffset();
            } else {
                if (index < remaining - 1) {                                                           // Middle
                    end = DocUtil.expandRangeStart(doc, unitRanges.get(index + 1).getStartOffset(), RE_UNITNAME_PREFIX);
                }
            }
        } else {
            if (index < unitRanges.size() - 1) {                                                               // Left
                end = DocUtil.expandRangeStart(doc, unitRanges.get(index + 1).getStartOffset(), RE_UNITNAME_PREFIX);
            }
        }
        return TextRange.create(start, end);
    }

    private static int getUnitIndex(PasUsesClause uses, PascalQualifiedIdent usedUnit) {
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
            return o2.getTextRange().getStartOffset() - o1.getTextRange().getStartOffset();
        }
    }

    private static class ByOffsetComparator2 implements Comparator<TextRange> {
        @Override
        public int compare(TextRange o1, TextRange o2) {
            return o2.getStartOffset() - o1.getStartOffset();
        }
    }


}
