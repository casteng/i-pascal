package com.siberika.idea.pascal.debugger.settings;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.execution.util.StringWithNewLinesCellEditor;
import com.intellij.icons.AllIcons;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.siberika.idea.pascal.PascalBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableCellEditor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class TypeRenderersTable extends ListTableWithButtons<TypeRenderer> {
    public TypeRenderersTable() {
        getTableView().getEmptyText().setText(PascalBundle.message("debug.settings.typeRenderers.empty"));
    }

    @Override
    protected ListTableModel createListModel() {
        final ColumnInfo name = new ElementsColumnInfoBase<TypeRenderer>(PascalBundle.message("debug.settings.typeRenderers.type")) {
            @Override
            public String valueOf(TypeRenderer TypeRenderer) {
                return TypeRenderer.getType();
            }

            @Override
            public boolean isCellEditable(TypeRenderer TypeRenderer) {
                return TypeRenderer.getNameIsWriteable();
            }

            @Override
            public void setValue(TypeRenderer TypeRenderer, String s) {
                if (s.equals(valueOf(TypeRenderer))) {
                    return;
                }
                TypeRenderer.setType(s);
                setModified();
            }

            @Override
            protected String getDescription(TypeRenderer TypeRenderer) {
                return TypeRenderer.getDescription();
            }
        };

        final ColumnInfo value = new ElementsColumnInfoBase<TypeRenderer>(PascalBundle.message("debug.settings.typeRenderers.value")) {
            @Override
            public String valueOf(TypeRenderer typeRenderer) {
                return typeRenderer.getValue();
            }

            @Override
            public boolean isCellEditable(TypeRenderer typeRenderer) {
                return true;
            }

            @Override
            public void setValue(TypeRenderer TypeRenderer, String s) {
                if (s.equals(valueOf(TypeRenderer))) {
                    return;
                }
                TypeRenderer.setValue(s);
                setModified();
            }

            @Nullable
            @Override
            protected String getDescription(TypeRenderer typeRenderer) {
                return typeRenderer.getDescription();
            }

            @Nullable
            @Override
            public TableCellEditor getEditor(TypeRenderer typeRenderer) {
                StringWithNewLinesCellEditor editor = new StringWithNewLinesCellEditor();
                editor.setClickCountToStart(1);
                return editor;
            }
        };

        return new ListTableModel((new ColumnInfo[]{name, value}));
    }

    public List<TypeRenderer> getTypeRenderers() {
        return getElements();
    }

    @Override
    protected TypeRenderer createElement() {
        return new TypeRenderer("", "");
    }

    @Override
    protected boolean isEmpty(TypeRenderer element) {
        return element.getType().isEmpty() && element.getValue().isEmpty();
    }

    @NotNull
    @Override
    protected AnActionButton[] createExtraActions() {
        AnActionButton copyButton = new AnActionButton(ActionsBundle.message("action.EditorCopy.text"), AllIcons.Actions.Copy) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                stopEditing();
                StringBuilder sb = new StringBuilder();
                List<TypeRenderer> renderers = getSelection();
                for (TypeRenderer TypeRenderer : renderers) {
                    if (isEmpty(TypeRenderer)) continue;
                    if (sb.length() > 0) sb.append('\n');
                    sb.append(StringUtil.escapeChar(TypeRenderer.getType(), '=')).append('=')
                            .append(StringUtil.escapeChar(TypeRenderer.getValue(), '='));
                }
                CopyPasteManager.getInstance().setContents(new StringSelection(sb.toString()));
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !getSelection().isEmpty();
            }
        };
        AnActionButton pasteButton = new AnActionButton(ActionsBundle.message("action.EditorPaste.text"), AllIcons.Actions.Menu_paste) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                removeSelected();
                stopEditing();
                String content = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
                if (content == null || !content.contains("=")) return;
                List<TypeRenderer> parsed = new ArrayList<>();
                List<String> lines = StringUtil.split(content, "\n");
                for (String line : lines) {
                    int pos = line.indexOf('=');
                    if (pos == -1) continue;
                    while (pos > 0 && line.charAt(pos - 1) == '\\') {
                        pos = line.indexOf('=', pos + 1);
                    }
                    line = line.replaceAll("[\\\\]{1}","\\\\\\\\");
                    parsed.add(new TypeRenderer(
                            StringUtil.unescapeStringCharacters(line.substring(0, pos)),
                            StringUtil.unescapeStringCharacters(line.substring(pos + 1))
                    ));
                }
                List<TypeRenderer> typeRenderers = new ArrayList<>(getTypeRenderers());
                typeRenderers.addAll(parsed);
                setValues(typeRenderers);
            }
        };
        return new AnActionButton[]{copyButton, pasteButton};
    }

    @Override
    protected TypeRenderer cloneElement(TypeRenderer typeRenderer) {
        return typeRenderer.clone();
    }

    @Override
    protected boolean canDeleteElement(TypeRenderer selection) {
        return true;
    }
}
