package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.siberika.idea.pascal.lang.psi.PasClassParent;
import com.siberika.idea.pascal.lang.psi.PasTypeID;
import com.siberika.idea.pascal.lang.psi.PascalHelperDecl;
import com.siberika.idea.pascal.lang.stub.struct.PasHelperDeclStub;
import com.siberika.idea.pascal.util.StrUtil;
import org.jetbrains.annotations.Nullable;

public abstract class PascalClassHelperDeclImpl extends PasStubStructTypeImpl<PascalHelperDecl, PasHelperDeclStub> implements PascalHelperDecl {

    volatile private String target;

    PascalClassHelperDeclImpl(ASTNode node) {
        super(node);
    }

    PascalClassHelperDeclImpl(PasHelperDeclStub stub, IStubElementType nodeType) {
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
            PasTypeID typeId = ((PasClassHelperDeclImpl) this).getTypeID();
            target = typeId != null ? StrUtil.getNamePart(typeId.getFullyQualifiedIdent().getName()) : "";
        }
        return target;
    }
}
