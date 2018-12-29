package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalRecordHelperDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasRecordHelperDeclStub;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.Nullable;

public abstract class PascalRecordHelperDeclImpl extends PasStubStructTypeImpl<PascalRecordHelperDecl, PasRecordHelperDeclStub> implements PascalRecordHelperDecl {

    volatile private String target;

    PascalRecordHelperDeclImpl(ASTNode node) {
        super(node);
    }

    PascalRecordHelperDeclImpl(PasRecordHelperDeclStub stub, IStubElementType nodeType) {
        super(stub, nodeType);
        target = stub.getTarget();
    }

    @Override
    public void invalidateCache(boolean subtreeChanged) {
        super.invalidateCache(subtreeChanged);
        target = null;
    }

    @Nullable
    @Override
    public PasClassParent getClassParent() {
        return null;
    }

    @Override
    public String getTarget() {
        if (null == target) {
            PasTypeID typeId = ((PasRecordHelperDeclImpl) this).getTypeID();
            target = typeId != null ? StrUtil.getNamePart(typeId.getFullyQualifiedIdent().getName()) : "";
        }
        return target;
    }
}
