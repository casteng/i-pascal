package com.siberika.idea.pascal.ui;

import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.SmartList;
import com.intellij.util.ui.UIUtil;
import com.siberika.idea.pascal.lang.psi.PasEntityScope;
import com.siberika.idea.pascal.lang.psi.impl.PasField;
import com.siberika.idea.pascal.util.Filter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * Author: George Bakhtadze
 * Date: 26/11/2015
 */
public class TreeViewStruct extends DialogWrapper {
    private final Collection<PasEntityScope> structs;
    private final Filter<PasField> filter;
    private Tree myTree;
    private List<PasField> selected = new SmartList<PasField>();

    public TreeViewStruct(Project project, String title, Collection<PasEntityScope> structs, Filter<PasField> filter) {
        super(project, true);
        setTitle(title);
        this.structs = structs;
        this.filter = filter;
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());

        MutableTreeNode root = new DefaultMutableTreeNode();
        model.setRoot(root);
        for (PasEntityScope struct : structs) {
            MutableTreeNode child = new DefaultMutableTreeNode(struct);
            for (PasField field : struct.getAllFields()) {
                if (filter.allow(field)) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(field);
                    child.insert(node, child.getChildCount());
                }
            }
            if (child.getChildCount() > 0) {
                model.insertNodeInto(child, root, root.getChildCount());
            }
        }

        myTree = new Tree(model);
        myTree.setRootVisible(false);
        myTree.expandRow(0);
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        myTree.setCellRenderer(new NodeRenderer());
        UIUtil.setLineStyleAngled(myTree);

        final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myTree);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        myTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                    doOKAction();
                }
            }
        });

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent e) {
                final TreePath path = myTree.getPathForLocation(e.getX(), e.getY());
                if (path != null && myTree.isPathSelected(path)) {
                    doOKAction();
                    return true;
                }
                return false;
            }
        }.installOn(myTree);

        myTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(final TreeSelectionEvent e) {
                        handleSelectionChanged();
                    }
                }
        );

        new TreeSpeedSearch(myTree);

        return scrollPane;
    }

    private void handleSelectionChanged(){
        setOKActionEnabled(true);
    }

    @Override
    protected void doOKAction() {
        MutableTreeNode[] nodes = myTree.getSelectedNodes(MutableTreeNode.class, null);
        for (MutableTreeNode node : nodes) {
            selected.add((PasField) ((DefaultMutableTreeNode) node).getUserObject());
        }
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        selected.clear();
        super.doCancelAction();
    }

    public List<PasField> getSelected() {
        return selected;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myTree;
    }

}
