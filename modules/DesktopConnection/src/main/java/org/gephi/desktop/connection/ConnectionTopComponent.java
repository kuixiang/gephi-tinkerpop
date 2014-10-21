/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.connection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays the Connection component.
 * Inspired by streaming plugin
 *
 * @author Xia Zhu
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.connection//Connection//EN",
autostore = false)
@TopComponent.Description(preferredID = "ConnectionTopComponent",
iconBase = "org/gephi/desktop/connection/resources/connect.jpg",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.desktop.connection.ConnectionTopComponent")
@ActionReference(path = "Menu/Window", position = 1300)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ConnectionTopComponent",
preferredID = "ConnectionTopComponent")
public final class ConnectionTopComponent extends TopComponent implements ExplorerManager.Provider {

    private transient ConnectionUIController controller;
    private transient Children clientMasterChildren;
    private transient ConnectionTreeView tree;
    private transient DatabaseNode selectedNode = null;

    public ConnectionTopComponent() {

        tree = new ConnectionTreeView();

        initComponents();
        setName(NbBundle.getMessage(ConnectionTopComponent.class, "CTL_ConnectionTopComponent"));

        clientMasterChildren = new Children.Array();

        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap()));
        AbstractNode topnode = new AbstractNode(clientMasterChildren) {

            @Override
            public Action[] getActions(boolean context) {
                return new Action[0];
            }
        };
        mgr.setRootContext(topnode);

        controller = Lookup.getDefault().lookup(ConnectionUIController.class);
        controller.setTopComponent(this);
    }

    private class ConnectionTreeView extends BeanTreeView {

        public ConnectionTreeView() {
            super();
            setRootVisible(false);
        }
    }

    public synchronized void refreshModel(ConnectionModel model) {
        Node clientNode = model.getClientNode();

        clientMasterChildren.remove(clientMasterChildren.getNodes());
        clientMasterChildren.add(new Node[]{clientNode});
        tree.expandNode(clientNode);

        mgr.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    Node[] selected = (Node[]) evt.getNewValue();
                    if (selected != null && selected.length == 1 && selected[0] instanceof DatabaseNode) {
                        removeButton.setEnabled(true);
                        selectedNode = (DatabaseNode) selected[0];
                    } else {
                        removeButton.setEnabled(false);
                        selectedNode = null;
                    }
                }
            }
        });

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        javax.swing.JScrollPane treeView = tree;

        setLayout(new java.awt.GridBagLayout());

        topPanel.setLayout(new java.awt.GridBagLayout());

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/connection/resources/plus.jpg"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ConnectionTopComponent.class, "ConnectionTopComponent.addButton.text")); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionTopComponent.class, "ConnectionTopComponent.addButton.toolTipText")); // NOI18N
        addButton.setMaximumSize(new java.awt.Dimension(29, 29));
        addButton.setMinimumSize(new java.awt.Dimension(29, 29));
        addButton.setPreferredSize(new java.awt.Dimension(29, 29));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        topPanel.add(addButton, gridBagConstraints);

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/connection/resources/minus.jpg"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ConnectionTopComponent.class, "ConnectionTopComponent.removeButton.text")); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionTopComponent.class, "ConnectionTopComponent.removeButton.toolTipText")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.setMaximumSize(new java.awt.Dimension(29, 29));
        removeButton.setMinimumSize(new java.awt.Dimension(29, 29));
        removeButton.setPreferredSize(new java.awt.Dimension(29, 29));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        topPanel.add(removeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        topPanel.add(separator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(topPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(treeView, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                controller.configAndConnectToStream();
            }
        });
    }//GEN-LAST:event_addActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (selectedNode != null) {
                    selectedNode.getParentNode().getChildren().remove(new Node[]{selectedNode});
                }
            }
        });
    }//GEN-LAST:event_removeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JSeparator separator;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
    private final ExplorerManager mgr = new ExplorerManager();

    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
