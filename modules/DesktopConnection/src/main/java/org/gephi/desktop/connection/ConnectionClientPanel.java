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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;


/**
 * A JPanel to fill Graph database information in order to connect to a database.
 * Inspired by streaming plugin
 *
 * @author Xia Zhu
 */
public class ConnectionClientPanel extends javax.swing.JPanel {
    /** The configuration files directory path constant. */
    private static final String GRAPH_DIR_PATH = "/etc/gephi-tinkerpop.json";
    private static final String BASE_URI = "base-uri";
    private static final String SERVER_PORT = "server-port";
    private static final String GRAPHS = "graphs";
    private static final String VERSION = "version";
    private static final String API_KEY = "api-key";
    private static final Set<String> SUPPORTED_VERSIONS =
        new HashSet<String>(Arrays.asList("v1"));
    private static TinkerpopConfig config = new TinkerpopConfig();
    private static TinkerpopFilterCommon tinkerpopFilterCommon = new TinkerpopFilterCommon();
    private static String baseUri = "";
    private static String baseUriWithPort = "";
    private static List<String> graphNames;
    private boolean fromConfigFile = true;


    /** Creates new form ConnectionClientPanel */
    public ConnectionClientPanel() {
        getDatabaseInfoFromJSONFile();
        initComponents();
        setup();
    }

    public static ValidationPanel createValidationPanel(final ConnectionClientPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            throw new NullPointerException();
        }
        validationPanel.setInnerComponent(innerPanel);
        ValidationGroup group = validationPanel.getValidationGroup();
        group.add(innerPanel.streamUrlTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.streamUrlTextField, Validators.URL_MUST_BE_VALID);
        group.add(innerPanel.graphNameTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        return validationPanel;
    }



    public static void getDatabaseInfoFromJSONFile() {
        final String fullPath = System.getProperty("user.dir") + GRAPH_DIR_PATH;
        String supportedVersions = "";
        for (String s:SUPPORTED_VERSIONS){
            supportedVersions +=  s + "\t";
        }
        graphNames = new ArrayList<String>();
        JSONParser parser = new JSONParser();
        JSONObject inputConfig;

        try{
            inputConfig = (JSONObject) parser.parse(new FileReader(fullPath));
        } catch (FileNotFoundException e) {
            tinkerpopFilterCommon.notifyUser("Could not find configuration file! Please edit " +
                "configurations to file $GEPHI_BIN_HOME/etc/graph/config.json");
            e.printStackTrace();
            throw new IllegalArgumentException("FileNotFoundException when parsing JSON config file " + e.toString());
        } catch (ParseException e){
            tinkerpopFilterCommon.notifyUser("ParseException when parsing JSON config file!");
            e.printStackTrace();
            throw new IllegalArgumentException("ParseException when parsing JSON config file! " +
                "ErrorPosition: " + e.getPosition() + ", ErrorType: " + e.getErrorType());
        } catch (IOException e) {
            tinkerpopFilterCommon.notifyUser("IOException when parsing JSON config file!");
            e.printStackTrace();
            throw new IllegalArgumentException("IOException when parsing JSON config file " + e.toString());
        }

        baseUri = (String) inputConfig.get(BASE_URI);
        if (StringUtils.isBlank(baseUri)) {
            baseUri = "Unknown base uri - check configuration element " + BASE_URI;
        }
        System.out.println("base URI:\t" + baseUri);
        String[] uriString =  baseUri.split("://");
        config.setSchema(uriString[0]);
        String[] secondPart =  uriString[1].split(":");
        config.setHost(secondPart[0]);

        String serverPort = (String) inputConfig.get(SERVER_PORT);
        if (StringUtils.isBlank(serverPort)) {
            tinkerpopFilterCommon.notifyUser("Unknown server port - check configuration element " +
                SERVER_PORT);
            throw new IllegalArgumentException("Unknown server port - check configuration element "
                + SERVER_PORT);
        }
        config.setServerPort(serverPort);
        System.out.println("Server Port:\t" + serverPort);

        JSONArray graphs = (JSONArray) inputConfig.get(GRAPHS);
        if (graphs.size() == 0) {
            tinkerpopFilterCommon.notifyUser("No graphs configured - check configuration element " +
                GRAPHS);
            throw new IllegalArgumentException("No graphs configured - " +
                "check configuration element " + GRAPHS);
        } else {
            graphNames.add("");
            System.out.println("Number of graph is:\t" + graphs.size());
            for(Object name : graphs){
                graphNames.add(name.toString());
                System.out.println("Graph Name:\t" + name);
            }
        }
    }

    public void setup() {
        fromConfFileCheckBox.setSelected(this.fromConfigFile());

        fromConfFileCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (fromConfigFile() != fromConfFileCheckBox.isSelected()) {
                    setFromConfigFile(fromConfFileCheckBox.isSelected());
                }
            }
        });

        graphNameComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int index = graphNameComboBox.getSelectedIndex();
                if (index > 0){
                    String graphName = graphNames.get(index);
                    //update URL and GraphName on panel
                    baseUriWithPort = baseUri + ":" + config.getServerPort();
                    streamUrlTextField.setText(baseUriWithPort);
                    graphNameTextField.setText(graphName);
                }
            }
        });
    }

    public boolean fromConfigFile() {
        return fromConfigFile;
    }

    public void setFromConfigFile(boolean fromConfigFile) {
        this.fromConfigFile = fromConfigFile;
    }



    /**
     * Set base URI from the filled information
     * if no configuration file is provided by user.
     * expected input in "{schema}://{host}:{serverPort}" format
     *
     */
    public void setBaseUriWithPort() {
        if(!fromConfigFile){
            baseUriWithPort = streamUrlTextField.getText();
            String[] stringArray1 = baseUriWithPort.split("://");
            config.setSchema(stringArray1[0]);
            String[] stringArray2 =  stringArray1[1].split(":");
            config.setHost(stringArray2[0]);
            config.setServerPort(stringArray2[1]);
        }

    }

    /**
     * Set graph name from the filled information.
     *
     */
    public void setGraphName() {
        config.setGraphName(graphNameTextField.getText());
    }

    /**
     * Get configuration from the selected/filled information.
     *
     * @return the configuration of the connection
     */
    public TinkerpopConfig getConfig() {
        return config;
    }




    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        fromConfFileCheckBox = new javax.swing.JCheckBox();
        graphNameComboBox = new javax.swing.JComboBox();
        streamUrlLabel = new javax.swing.JLabel();
        streamUrlTextField = new javax.swing.JTextField();
        graphNameLabel = new javax.swing.JLabel();
        graphNameTextField = new javax.swing.JTextField();

        fromConfFileCheckBox.setText(org.openide.util.NbBundle.getMessage(ConnectionClientPanel.class, "ConnectionClientPanel.fromConfFileCheckBox.text")); // NOI18N

        graphNameComboBox.setModel(new javax.swing.DefaultComboBoxModel(graphNames.toArray(new String[graphNames.size()])));

        streamUrlLabel.setText(org.openide.util.NbBundle.getMessage(ConnectionClientPanel.class, "ConnectionClientPanel.streamUrlLabel.text")); // NOI18N

        streamUrlTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionClientPanel.class, "ConnectionClientPanel.sourceURL.text")); // NOI18N
        streamUrlTextField.setName("databaseURL"); // NOI18N

        graphNameLabel.setText(org.openide.util.NbBundle.getMessage(ConnectionClientPanel.class, "ConnectionClientPanel.graphNameLabel.text")); // NOI18N

        graphNameTextField.setText(org.openide.util.NbBundle.getMessage(ConnectionClientPanel.class, "ConnectionClientPanel.graphName.text")); // NOI18N
        graphNameTextField.setName("graphName"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(fromConfFileCheckBox)
                        .addComponent(graphNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(streamUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                        .addComponent(streamUrlLabel)
                        .addComponent(graphNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                        .addComponent(graphNameLabel)
                )
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fromConfFileCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(graphNameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(streamUrlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(streamUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(graphNameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(graphNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox fromConfFileCheckBox;
    private javax.swing.JComboBox graphNameComboBox;
    private javax.swing.JLabel streamUrlLabel;
    private javax.swing.JTextField streamUrlTextField;
    private javax.swing.JLabel graphNameLabel;
    private javax.swing.JTextField graphNameTextField;
    // End of variables declaration//GEN-END:variables

}
