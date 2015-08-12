package com.siberika.idea.pascal.editor;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.siberika.idea.pascal.ide.actions.SectionToggle;
import com.siberika.idea.pascal.lang.psi.PasBlockGlobal;
import com.siberika.idea.pascal.lang.psi.PasImplDeclSection;
import com.siberika.idea.pascal.lang.psi.PasInterfaceDecl;
import com.siberika.idea.pascal.lang.psi.PasProcBodyBlock;
import com.siberika.idea.pascal.lang.psi.PasRoutineImplDecl;
import com.siberika.idea.pascal.lang.psi.PascalNamedElement;
import com.siberika.idea.pascal.lang.psi.PascalStructType;
import com.siberika.idea.pascal.lang.psi.impl.PasExportedRoutineImpl;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.lang.psi.impl.PasRoutineImplDeclImpl;
import com.siberika.idea.pascal.lang.psi.impl.PascalRoutineImpl;
import com.siberika.idea.pascal.util.PsiUtil;

import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 06/10/2013
 */
public class PascalRoutineActions {

    public static class ActionDeclare extends PascalActionDeclare {
        public ActionDeclare(String name, PascalNamedElement element) {
            super(name, element);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            PasRoutineImplDeclImpl routine = (PasRoutineImplDeclImpl) data.element;

            String prefix = routine.getNamespace() + ".";
            PasProcBodyBlock block = routine.getProcBodyBlock();
            int endoffs = block != null ? block.getStartOffsetInParent() : routine.getTextLength();
            data.text = "\n" + routine.getText().substring(0, endoffs);
            if (prefix.length() > 1) {
                data.text = data.text.replaceFirst(prefix, "");
            }

            data.parent = routine.getContainingScope();
            data.offset = SectionToggle.findIntfPos(routine);
            if (data.offset < 0) {
                if (data.parent instanceof PascalStructType) {
                    PsiElement pos = PsiUtil.findEndSibling(data.parent.getFirstChild());
                    data.offset = pos != null ? pos.getTextRange().getStartOffset() : -1;
                } else {
                    PsiElement pos = PsiUtil.getModuleInterfaceSection(routine.getContainingFile());
                    pos = pos != null ? PsiTreeUtil.findChildOfType(pos, PasInterfaceDecl.class) : null;
                    if (null != pos) {
                        data.offset = pos.getTextRange().getEndOffset();
                    } else {                                            // program or library. Should not go here.
                        data.offset = getModuleMainDeclSection(routine.getContainingFile());
                    }
                }
            }
            if (data.offset < 0) {
                data.parent = null;
            }
        }
    }

    public static class ActionImplement extends PascalActionDeclare {
        public ActionImplement(String name, PascalNamedElement element) {
            super(name, element);
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            PascalRoutineImpl routine = (PascalRoutineImpl) data.element;
            String prefix = SectionToggle.getPrefix(routine);
            data.text = data.element.getText();
            String name = PsiUtil.getFieldName(data.element);
            data.text = "\n\n" + StringUtil.replace(data.text, name, prefix + name) + "\nbegin\n\nend;\n\n";
            data.offset = SectionToggle.findImplPos(routine);
            data.parent = routine;
            if (data.offset < 0) {
                data.parent = PsiUtil.getModuleImplementationSection(data.element.getContainingFile());
                data.parent = data.parent != null ? PsiTreeUtil.findChildOfType(data.parent, PasImplDeclSection.class) : null;
                if (null != data.parent) {
                    data.offset = data.parent.getTextRange().getEndOffset();
                } else {                                                // program or library
                    data.offset = getModuleMainDeclSection(routine.getContainingFile());
                    if (data.offset >= 0) {
                        data.parent = routine.getContainingFile();
                    }
                }
            }
            if (data.offset < 0) {
                data.parent = null;
            }
        }
    }

    public static class ActionImplementAll extends ActionImplement {
        public ActionImplementAll(String name, PascalNamedElement element) {
            super(name, element);
            PascalRoutineImpl routine = (PascalRoutineImpl) element;
            List<PasExportedRoutineImpl> fields = SectionToggle.collectFields(SectionToggle.getDeclFields(routine.getContainingScope()), PasField.FieldType.ROUTINE, new SectionToggle.PasFilter<PasField>() {
                @Override
                public boolean allow(PasField value) {
                    return value.element instanceof PasExportedRoutineImpl;
                }
            });
            for (PasExportedRoutineImpl field : fields) {
                if ((field != routine) && (PsiUtil.needImplementation(field)) && (null == SectionToggle.retrieveImplementation(field))) {
                    addData(new FixActionData(field));
                }
            }
        }

        @Override
        void calcData(final PsiFile file, final FixActionData data) {
            PascalRoutineImpl routine = (PascalRoutineImpl) data.element;
            String prefix = SectionToggle.getPrefix(routine);
            data.text = data.element.getText();
            String name = PsiUtil.getFieldName(data.element);
            data.text = "\n\n" + StringUtil.replace(data.text, name, prefix + name) + "\nbegin\n\nend;\n\n";
            data.offset = SectionToggle.findImplPos(routine);
            data.parent = routine;
            if (data.offset < 0) {
                data.parent = PsiUtil.getModuleImplementationSection(data.element.getContainingFile());
                data.parent = data.parent != null ? PsiTreeUtil.findChildOfType(data.parent, PasImplDeclSection.class) : null;
                if (null != data.parent) {
                    data.offset = data.parent.getTextRange().getEndOffset();
                } else {                                                // program or library
                    data.offset = getModuleMainDeclSection(routine.getContainingFile());
                    if (data.offset >= 0) {
                        data.parent = routine.getContainingFile();
                    }
                }
            }
            if (data.offset < 0) {
                data.parent = null;
            }
        }
    }

    private static int getModuleMainDeclSection(PsiFile section) {
        PasBlockGlobal block = PsiTreeUtil.findChildOfType(section, PasBlockGlobal.class);
        if (block != null) {
            List<PasRoutineImplDecl> impls = block.getRoutineImplDeclList();
            if (!impls.isEmpty()) {
                return impls.get(impls.size() - 1).getTextRange().getEndOffset();
            }
            return block.getBlockBody().getTextOffset();
        }
        return -1;
    }

}
