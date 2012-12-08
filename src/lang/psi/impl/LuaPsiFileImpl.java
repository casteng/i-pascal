/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.siberika.idea.pascal.lang.psi.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.PsiFileEx;
import com.intellij.psi.impl.source.PsiFileWithStubSupport;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.siberika.idea.pascal.PascalFileType;
import com.siberika.idea.pascal.lang.psi.LuaPsiElement;
import com.siberika.idea.pascal.lang.psi.LuaPsiFile;
import com.siberika.idea.pascal.lang.psi.LuaPsiFileBase;
import com.siberika.idea.pascal.lang.psi.visitor.LuaElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 10, 2010
 * Time: 12:19:03 PM
 */
public class LuaPsiFileImpl extends LuaPsiFileBaseImpl implements LuaPsiFile, PsiFileWithStubSupport, PsiFileEx, LuaPsiFileBase {
    private boolean sdkFile;

    private static final Logger log = Logger.getInstance("Lua.LuaPsiFileImp");

    public LuaPsiFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, PascalFileType.PASCAL_LANGUAGE);
    }

    @NotNull
    public FileType getFileType() {
        return PascalFileType.PASCAL_FILE_TYPE;
    }


    @Override
    public String toString() {
        return "Lua script: " + getName();
    }

    @Override
    public boolean processDeclarations(PsiScopeProcessor processor,
                                                   ResolveState state, PsiElement lastParent,
                                                   PsiElement place) {
        PsiElement run = lastParent == null ? getLastChild() : lastParent.getPrevSibling();
        if (run != null && run.getParent() != this) run = null;
        while (run != null) {
            if (!run.processDeclarations(processor, state, null, place)) return false;
            run = run.getPrevSibling();
        }

        return true;
    }

    public void accept(LuaElementVisitor visitor) {
        visitor.visitFile(this);
    }

    public void acceptChildren(LuaElementVisitor visitor) {
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof LuaPsiElement) {
                ((LuaPsiElement) child).accept(visitor);
            }

            child = child.getNextSibling();
        }
    }

    public String getPresentationText() {
        return null;
    }


    @Nullable
    public String getModuleNameAtOffset(int offset) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
