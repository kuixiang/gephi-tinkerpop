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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONObject;
import org.json.JSONException;

import org.junit.Assert;

/**
 * The UI controller to control the UI state.
 * Inspired by streaming plugin
 *
 * @author Xia Zhu
 *
 */
@ServiceProvider(service = ConnectionUIController.class)
public class ConnectionUIController {

    private static final Logger logger = Logger.getLogger(ConnectionUIController.class.getName());
    private static TinkerpopConfig config = new TinkerpopConfig();
    private TinkerpopFilterCommon tinkerpopFilterCommon = new TinkerpopFilterCommon();
    private ConnectionModel model;
    private ConnectionTopComponent component;
    private ConnectionClientPanel clientPanel;
    
    public ConnectionUIController() {

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                model = new ConnectionModel();
                workspace.add(model);
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(ConnectionModel.class);
                if (model == null) {
                    model = new ConnectionModel();
                    workspace.add(model);
                }
                refreshModel();
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
                model = workspace.getLookup().lookup(ConnectionModel.class);
                if (model != null) {
                    refreshModel();
                }
            }

            public void disable() {
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(ConnectionModel.class);
            if (model == null) {
                model = new ConnectionModel();
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    /**
     * Set the top component
     *
     */
    public void setTopComponent(ConnectionTopComponent component) {
        this.component = component;
        refreshModel();
    }

    /**
     * Get configuration of the connection.
     *
     * @return the configuration of the connection
     */

    public void refreshModel() {
        if (model != null) {
            component.refreshModel(model);
        }
    }

    /**
     * Get the connection model
     *
     * @return the connection model
     */
    public ConnectionModel getConnectionModel() {
        return model;
    }

    /**
     * Set the connection client panel
     *
     * @param clientPanel the client panel
     *
     */
    public void setClientPanel(ConnectionClientPanel clientPanel) {
        this.clientPanel = clientPanel;
    }

    /**
     * Get the connection client panel
     *
     * @return the connection client panel
     */
    public ConnectionClientPanel getClientPanel() {
        return clientPanel;
    }

    /**
     * Display the ConnectionClientPanel and
     * connect to a ConnectionEndpoint using the entered info.
     */
    public void configAndConnectToStream() {
        clientPanel = new ConnectionClientPanel();
        ValidationPanel vp = ConnectionClientPanel.createValidationPanel(clientPanel);
        final DialogDescriptor dd = new DialogDescriptor(vp, "Connect to Intel Tinkerpop");
        vp.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                dd.setValid(!((ValidationPanel) e.getSource()).isProblem());
            }
        });
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (!result.equals(NotifyDescriptor.OK_OPTION)) {
            return;
        }
        clientPanel.setBaseUriWithPort();
        clientPanel.setGraphName();

        config = clientPanel.getConfig();
        connectToStream();
    }


    /**
     * Detailed connect to stream operation with configuration
     *
     * @param config the Tinkerpop configuration
     *
     */
    private void connectToStream() {
        String graphName = config.getGraphName();
        JSONObject responseObj = tinkerpopFilterCommon.getGraphInfo(config);

        //check whether graph name matches
        if (!responseObj.has("name")){
            tinkerpopFilterCommon.notifyUser("Graph " + graphName + " does not exist.");
            throw new JSONException(
                "There is no name contained in response JSON object ");
        }
        String getName = responseObj.getString("name");
        if (!getName.equals(graphName)) {
            tinkerpopFilterCommon.notifyUser("Graph " + graphName + " does not exist.");
            throw new IllegalArgumentException("Graph " + graphName + " does not exist.");
        }  else {
            // Get active graph instance - Project and Graph API
            startWorkspace();
            //add node to clientNode
            model.addConnection(config);
        }
    }


    /**
     * Start work space
     *
     */
    private void startWorkspace(){
        // Get active graph instance - Project and Graph API
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        if (project==null)
            projectController.newProject();
        Workspace workspace = projectController.getCurrentWorkspace();
        if (workspace==null)
            workspace = projectController.newWorkspace(projectController.getCurrentProject());

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getHierarchicalMixedGraph();
    }

    /**
     * Get configuration of the connection.
     *
     * @return the configuration of the connection
     */
    public TinkerpopConfig getConfig() {
        return config;
    }
}
