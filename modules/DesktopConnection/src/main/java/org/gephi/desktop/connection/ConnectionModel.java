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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Object used to group the Client node and database node
 * Inspired by streaming plugin
 *
 * @author Xia Zhu
 *
 */
public class ConnectionModel {

    private static final Logger logger = Logger.getLogger(ConnectionModel.class.getName());

    private Image clientImage = ImageUtilities.loadImage("org/gephi/desktop/connection/resources/database.jpg", true);
    private String serverContext;
    private Node clientNode;

    /**
     * Create a ConnectionModel object with information about the GUI state:
     * The Client node and the connection to graph database.
     */
    public ConnectionModel() {
        clientNode = new ClientNode();
    }

    /**
     * Add a ConnectionConnection to the list of outgoing connections.
     * @param baseUriWithPort the base URI
     * @param graphName the graph name
     */
    public void addConnection(TinkerpopConfig config) {
        DatabaseNode node = new DatabaseNode(config);
        clientNode.getChildren().add(new Node[]{node});
    }

    /**
     * Used to get the node that represents the Client
     * @return the Client node
     */
    public Node getClientNode() {
        return clientNode;
    }

    /**
     * A class that represents the Client node information.
     */
    public class ClientNode extends AbstractNode {

        private final Action addConnectionAction;

        public ClientNode() {
            super(new Children.Array());
            setDisplayName("Graph Database");
            addConnectionAction = new AbstractAction("Connect to graph database") {

                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            ConnectionUIController controller = Lookup.getDefault().lookup(ConnectionUIController.class);
                            controller.configAndConnectToStream();
                        }
                    });
                }
            };
        }
        @Override
        public Action[] getActions(boolean popup) {
            return new Action[]{addConnectionAction};
        }
        @Override
        public Image getIcon(int type) {
            return clientImage;
        }

        @Override
        public Image getOpenedIcon(int i) {
            return getIcon(i);
        }
        @Override
        public String getHtmlDisplayName() {
            return "<b>Graph Database<b>";
        }

        public void removeDatabaseNode(DatabaseNode node) {
            getChildren().remove(new Node[]{node});
        }
    }

}
