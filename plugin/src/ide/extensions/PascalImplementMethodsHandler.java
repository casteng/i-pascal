package com.siberika.idea.pascal.ide.extensions;

import com.intellij.lang.ContextAwareActionHandler;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siberika.idea.pascal.ide.actions.ActionImplement;
import org.jetbrains.annotations.NotNull;

/**
 * User: LPUser
 * Date: 03/07/2018
 * Time: 16:35
 */
public class PascalImplementMethodsHandler implements ContextAwareActionHandler, LanguageCodeInsightActionHandler
{
  private ActionImplement getActionImplement()
  {
    AnAction action = ActionManager.getInstance().getAction("Pascal.OverrideMethod");
    if (action == null || !(action instanceof ActionImplement))
    {
      return null;
    }
    return (ActionImplement)action;
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file)
  {
    ActionImplement actionImplement = getActionImplement();
    if (actionImplement == null)
    {
      return;
    }

    PsiElement elementAt = file.findElementAt(editor.getCaretModel().getOffset());
    if (elementAt != null)
    {
      actionImplement.showOverrideDialog(elementAt, editor);
    }
  }

  @Override
  public boolean isAvailableForQuickList(@NotNull Editor editor, @NotNull PsiFile file, @NotNull DataContext dataContext)
  {
    return true;
  }

  @Override
  public boolean isValidFor(Editor editor, PsiFile file)
  {
    return true;
  }
}
