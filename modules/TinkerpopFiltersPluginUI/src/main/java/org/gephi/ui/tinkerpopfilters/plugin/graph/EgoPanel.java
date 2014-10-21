/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.tinkerpopfilters.plugin.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.tinkerpopfilters.plugin.graph.EgoBuilder.EgoFilter;

/**
 *
 * @author Mathieu Bastian, Xia Zhu
 */
public class EgoPanel extends javax.swing.JPanel {

    private EgoFilter egoFilter;

    /** Creates new form EgoPanel */
    public EgoPanel() {
        initComponents();
        attributeKeyTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                egoFilter.getProperties()[0].setValue(attributeKeyTextField.getText());
            }
        });
        attributeKeyTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                egoFilter.getProperties()[0].setValue(attributeKeyTextField.getText());
            }
            public void removeUpdate(DocumentEvent e) {
                egoFilter.getProperties()[0].setValue(attributeKeyTextField.getText());
            }
            public void insertUpdate(DocumentEvent e) {
                egoFilter.getProperties()[0].setValue(attributeKeyTextField.getText());
            }
        });

        attributeValueTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                egoFilter.getProperties()[1].setValue(attributeValueTextField.getText());
            }
        });
        attributeValueTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                egoFilter.getProperties()[1].setValue(attributeValueTextField.getText());
            }
            public void removeUpdate(DocumentEvent e) {
                egoFilter.getProperties()[1].setValue(attributeValueTextField.getText());
            }
            public void insertUpdate(DocumentEvent e) {
                egoFilter.getProperties()[1].setValue(attributeValueTextField.getText());
            }
        });

        numVerticesTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                egoFilter.getProperties()[4].setValue(numVerticesTextField.getText());
            }
        });
        numVerticesTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                egoFilter.getProperties()[4].setValue(numVerticesTextField.getText());
            }
            public void removeUpdate(DocumentEvent e) {
                egoFilter.getProperties()[4].setValue(numVerticesTextField.getText());
            }
            public void insertUpdate(DocumentEvent e) {
                egoFilter.getProperties()[4].setValue(numVerticesTextField.getText());
            }
        });

        depthComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int index = depthComboBox.getSelectedIndex();
                int depth = index + 1;
                if (!egoFilter.getDepth().equals(depth)) {
                    egoFilter.getProperties()[2].setValue(depth);
                }
                switch (depth) {
                    case 1:
                        distributionTextField.setText("100");
                        egoFilter.getProperties()[3].setValue("100");
                        break;
                    case 2:
                        distributionTextField.setText("50,50");
                        egoFilter.getProperties()[3].setValue("50,50");
                        break;
                    case 3:
                        distributionTextField.setText("33,33,34");
                        egoFilter.getProperties()[3].setValue("33,33,34");
                        break;
                    case 4:
                        distributionTextField.setText("25,25,25,25");
                        egoFilter.getProperties()[3].setValue("25,25,25,25");
                        break;
                    case 5:
                        distributionTextField.setText("20,20,20,20,20");
                        egoFilter.getProperties()[3].setValue("20,20,20,20,20");
                        break;
                    default:
                        break;
                }
            }
        });


        distributionTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                egoFilter.getProperties()[3].setValue(distributionTextField.getText());
            }
        });

        distributionTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                egoFilter.getProperties()[3].setValue(distributionTextField.getText());
            }
            public void removeUpdate(DocumentEvent e) {
                egoFilter.getProperties()[3].setValue(distributionTextField.getText());
            }
            public void insertUpdate(DocumentEvent e) {
                egoFilter.getProperties()[3].setValue(distributionTextField.getText());
            }
        });

        withSelfCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (!egoFilter.isSelf() == withSelfCheckbox.isSelected()) {
                    egoFilter.getProperties()[5].setValue(withSelfCheckbox.isSelected());
                }
            }
        });


        newImportCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (!egoFilter.isNewImport() == newImportCheckbox.isSelected()) {
                    egoFilter.getProperties()[6].setValue(newImportCheckbox.isSelected());
                }
            }
        });
    }

    public void setup(EgoFilter egoFilter) {
        this.egoFilter = egoFilter;
        attributeKeyTextField.setText(egoFilter.getAttributeKey());
        attributeValueTextField.setText(egoFilter.getAttributeValue());
        distributionTextField.setText(egoFilter.getDistribution());
        numVerticesTextField.setText(egoFilter.getNumVertices());

        int depth = egoFilter.getDepth();
        if (depth == Integer.MAX_VALUE) {
            depthComboBox.setSelectedIndex(depthComboBox.getModel().getSize() - 1);
        } else {
            depthComboBox.setSelectedIndex(depth - 1);
        }

        withSelfCheckbox.setSelected(egoFilter.isSelf());
        newImportCheckbox.setSelected(egoFilter.isNewImport());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelAttributeKey = new javax.swing.JLabel();
        attributeKeyTextField = new javax.swing.JTextField();
        labelAttributeValue = new javax.swing.JLabel();
        attributeValueTextField = new javax.swing.JTextField();
        labelDistribution = new javax.swing.JLabel();
        distributionTextField = new javax.swing.JTextField();
        labelNumVertices = new javax.swing.JLabel();
        numVerticesTextField = new javax.swing.JTextField();
        labelDepth = new javax.swing.JLabel();
        depthComboBox = new javax.swing.JComboBox();
        withSelfCheckbox = new javax.swing.JCheckBox();
        newImportCheckbox = new javax.swing.JCheckBox();

        labelAttributeKey.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelAttributeKey.text")); // NOI18N
        attributeKeyTextField.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.attributeKeyTextField.text")); // NOI18N
        attributeKeyTextField.setToolTipText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.attributeKeyTextField.toolTipText")); // NOI18N

        labelAttributeValue.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelAttributeValue.text")); // NOI18N
        attributeValueTextField.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.attributeValueTextField.text")); // NOI18N
        attributeValueTextField.setToolTipText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.attributeValueTextField.toolTipText")); // NOI18N

        labelDistribution.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelDistribution.text")); // NOI18N
        distributionTextField.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.distributionTextField.text")); // NOI18N
        distributionTextField.setToolTipText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.distributionTextField.toolTipText")); // NOI18N

        labelNumVertices.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelNumVertices.text")); // NOI18N
        numVerticesTextField.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.numVerticesTextField.text")); // NOI18N
        numVerticesTextField.setToolTipText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.numVerticesTextField.toolTipText")); // NOI18N

        labelDepth.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelDepth.text")); // NOI18N

        depthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3",
            "4", "5"
        }));

        withSelfCheckbox.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.withSelfCheckbox.text")); // NOI18N
        newImportCheckbox.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.newImportCheckbox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelAttributeKey)
                    .addComponent(labelAttributeValue)
                    .addComponent(labelDepth)
                    .addComponent(labelDistribution)
                    .addComponent(labelNumVertices)
                    .addComponent(withSelfCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newImportCheckbox)
                    .addComponent(depthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(attributeKeyTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        )
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(attributeValueTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        )
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(distributionTextField, javax.swing.GroupLayout.DEFAULT_SIZE,117, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        )
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(numVerticesTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        ))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAttributeKey)
                    .addComponent(attributeKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    )
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAttributeValue)
                    .addComponent(attributeValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    )
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(depthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelDepth)
                     )
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDistribution)
                    .addComponent(distributionTextField, javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    )
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelNumVertices)
                    .addComponent(numVerticesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    )
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(withSelfCheckbox)
                    .addComponent(newImportCheckbox)
                    )
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox depthComboBox;
    private javax.swing.JLabel labelDepth;
    private javax.swing.JLabel labelAttributeKey;
    private javax.swing.JLabel labelAttributeValue;
    private javax.swing.JLabel labelDistribution;
    private javax.swing.JLabel labelNumVertices;
    private javax.swing.JTextField attributeKeyTextField;
    private javax.swing.JTextField attributeValueTextField;
    private javax.swing.JTextField numVerticesTextField;
    private javax.swing.JTextField distributionTextField;
    private javax.swing.JCheckBox withSelfCheckbox;
    private javax.swing.JCheckBox newImportCheckbox;
    // End of variables declaration//GEN-END:variables
}
