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
package org.gephi.tinkerpopfilters.plugin.graph;

import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;

import org.gephi.tinkerpopfilters.api.FilterLibrary;
import org.gephi.tinkerpopfilters.spi.Category;
import org.gephi.tinkerpopfilters.spi.ComplexFilter;
import org.gephi.tinkerpopfilters.spi.Filter;
import org.gephi.tinkerpopfilters.spi.FilterBuilder;
import org.gephi.tinkerpopfilters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import org.json.JSONArray;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import org.gephi.desktop.connection.TinkerpopConfig;
import org.gephi.desktop.connection.TinkerpopFilterCommon;

/**
 *
 * @author Xia Zhu
 */
@ServiceProvider(service = FilterBuilder.class)
public class VertexOrientedBuilder implements FilterBuilder {
    private static final String TINKERPOP_EDGE_KEY = "_id";

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(VertexOrientedBuilder.class, "VertexOrientedBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(VertexOrientedBuilder.class, "VertexOrientedBuilder.description");
    }

    public Filter getFilter() {
        return new VertexOrientedFilter();
    }

    public JPanel getPanel(Filter filter) {
        VertexOrientedUI ui = Lookup.getDefault().lookup(VertexOrientedUI.class);
        if (ui != null) {
            return ui.getPanel((VertexOrientedFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {
    }

    public void stop(Filter filter) {
        ((VertexOrientedFilter) filter).cancel();
    }

    public void reset(Filter filter) {
        ((VertexOrientedFilter) filter).reset();
    }

    public static class VertexOrientedFilter implements ComplexFilter {
        volatile boolean stopRun = false;
        private TinkerpopConfig config = new TinkerpopConfig();
        private TinkerpopFilterCommon tinkerpopFilterCommon = new TinkerpopFilterCommon();
        String gremlinScript = "g.V()";
        boolean withSubgraph = true;
        boolean newImport = true;

        public Graph filter(Graph graph, boolean leafOnly, boolean isLeaf) {
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;
            String vertexGremlin = gremlinScript + ".dedup()";
            String edgeGremlin = "x=[];" + vertexGremlin +
                ".aggregate(x).iterate();g.V.retain(x).outE.as('y').inV.retain(x).back('y')" +
                ".dedup";

            if(gremlinScript == "g.V()" || gremlinScript == "g.V"){
                tinkerpopFilterCommon.notifyUser("gremlinScript is the default " +  gremlinScript +
                    " to get all vertices. Please expect long execution time.");
            }
            if(gremlinScript == "" || gremlinScript == null || gremlinScript.isEmpty()){
                tinkerpopFilterCommon.notifyUser("gremlinScript is empty! " +
                    "Please input gremlin script.");
                return hgraph;
            }
            config = tinkerpopFilterCommon.getDatabaseInfo();
            JSONArray results = new JSONArray();

            //check stopRun periodically to make sure the fitler can be stopped at any time
            if (leafOnly && newImport || !leafOnly && isLeaf){
                if(!stopRun){
                    results = tinkerpopFilterCommon.tinkerpopNewQuery(vertexGremlin, config, true,
                        this.getName());
                }
                if(results.length() > 0){
                    if(!stopRun){
                        graph = tinkerpopFilterCommon.getGraph();
                        hgraph = (HierarchicalGraph) graph;
                    }
                    //clear old graph
                    for (Node node : hgraph.getNodes().toArray()) {
                        if(stopRun){
                            break;
                        }
                        hgraph.writeLock();
                        hgraph.removeNode(node);
                        hgraph.writeUnlock();
                    }
                    //add Vertices
                    if(!stopRun){
                        tinkerpopFilterCommon.getGraphElements(results, graph, true);
                    }

                    //add Edges.
                    if(!stopRun && withSubgraph){
                        results = tinkerpopFilterCommon.tinkerpopNewQuery(edgeGremlin, config, false,
                            this.getName());
                    }
                    if(!stopRun && withSubgraph && results.length() > 0){
                        tinkerpopFilterCommon.getGraphElements(results, graph, false);
                    }
                }
            } else {
                //filter based on existing hgraph
                if(withSubgraph){
                    //with edges.
                    Set<Integer> resultVertexIds = new HashSet<Integer>();
                    Set<String> resultEdgeIds = new HashSet<String>();
                    Set<String> inputEdgeIds = new HashSet<String>();
                    if(!stopRun && hgraph.getNodeCount() > 0){
                        resultVertexIds = tinkerpopFilterCommon.tinkerpopVertexQuery(vertexGremlin, graph,
                            config, resultVertexIds, this.getName());
                        // get required vertices

                        if(resultVertexIds.size() > 0){
                            //remove un-needed vertices
                            //the edges of un-needed vertices will also be removed
                            for (Node node : hgraph.getNodes().toArray()) {
                                //System.out.println("node ID " + node.getId());
                                if(stopRun){
                                    break;
                                }
                                if (!resultVertexIds.contains(node.getId())) {
                                    hgraph.writeLock();
                                    hgraph.removeNode(node);
                                    hgraph.writeUnlock();
                                    //System.out.println("Remove node " + node.getNodeData().getId());
                                } else {
                                    //System.out.println("Keep node " + node.getNodeData().getId());
                                }
                            }
                            if(!stopRun){
                                graph = tinkerpopFilterCommon.getGraph();
                                hgraph = (HierarchicalGraph) graph;
                            }
                            if(!stopRun && hgraph.getEdgeCount() > 0){
                                for (Edge edge : hgraph.getEdges().toArray()) {
                                    if(stopRun){
                                        break;
                                    }
                                    inputEdgeIds.add(edge.getEdgeData().getAttributes()
                                        .getValue(TINKERPOP_EDGE_KEY).toString());
                                }
                                resultEdgeIds = tinkerpopFilterCommon.tinkerpopEdgeQuery(edgeGremlin, graph,
                                    config, inputEdgeIds, this.getName());

                                if(resultEdgeIds.size() > 0){
                                    //remove residual un-needed edges
                                    for (Edge edge : hgraph.getEdges().toArray()) {
                                        if(stopRun){
                                            break;
                                        }
                                        String atkEdgeId = edge.getEdgeData().getAttributes()
                                            .getValue(TINKERPOP_EDGE_KEY).toString();
                                        if (!resultEdgeIds.contains(atkEdgeId)) {
                                            hgraph.removeEdge(edge);
                                            //System.out.println("Remove edge " + edge.getEdgeData().getId());
                                        } else {
                                            //System.out.println("Keep edge " + edge.getEdgeData().getId());

                                        }
                                    }
                                } else if (resultEdgeIds.size() == 0){
                                    //remove all vertices and edges
                                    for (Edge edge : hgraph.getEdges().toArray()) {
                                        if(stopRun){
                                            break;
                                        }
                                        hgraph.writeLock();
                                        hgraph.removeEdge(edge);
                                        hgraph.writeUnlock();
                                    }
                                }
                            }
                        } else if (resultVertexIds.size() == 0){
                            //remove all vertices and edges
                            for (Node node : hgraph.getNodes().toArray()) {
                                if(stopRun){
                                    break;
                                }
                                hgraph.writeLock();
                                hgraph.removeNode(node);
                                hgraph.writeUnlock();
                            }
                        }
                    }
                } else {
                    //vertex only. just need to remove unnecessary vertices
                    Set<Integer> resultIds = new HashSet<Integer>();
                    if(!stopRun){
                        resultIds = tinkerpopFilterCommon.tinkerpopVertexQuery(vertexGremlin, graph, config,
                            resultIds, this.getName());

                        if(resultIds.size() > 0){
                            for (Node node : hgraph.getNodes().toArray()) {
                                //System.out.println("node ID " + node.getId());
                                if(stopRun){
                                    break;
                                }
                                if (!resultIds.contains(node.getId())) {
                                    hgraph.writeLock();
                                    hgraph.removeNode(node);
                                    hgraph.writeUnlock();
                                    //System.out.println("Remove node " + node.getNodeData().getId());
                                } else {
                                    //System.out.println("Keep node " + node.getNodeData().getId());
                                }
                            }
                        } else if (resultIds.size() == 0){
                            //remove all vertices and edges
                            for (Node node : hgraph.getNodes().toArray()) {
                                if(stopRun){
                                    break;
                                }
                                hgraph.writeLock();
                                hgraph.removeNode(node);
                                hgraph.writeUnlock();
                            }
                        }
                    }
                }
            }
            graph = tinkerpopFilterCommon.getGraph();
            hgraph = (HierarchicalGraph) graph;
            stopRun = true;
            return hgraph;
        }

        public String getName() {
            return NbBundle.getMessage(VertexOrientedBuilder.class, "VertexOrientedBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, String.class, "gremlinScript"),
                            FilterProperty.createProperty(this, Boolean.class, "withSubgraph"),
                            FilterProperty.createProperty(this, Boolean.class, "newImport")};
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public String getGremlinScript() {
            return gremlinScript;
        }

        public void setGremlinScript(String gremlinScript) {
            this.gremlinScript = gremlinScript;
        }

        public boolean isWithSubgraph() {
            return withSubgraph;
        }

        public void setWithSubgraph(boolean withSubgraph) {
            this.withSubgraph = withSubgraph;
        }

        public boolean isNewImport() {
            return newImport;
        }

        public void setNewImport(boolean newImport) {
            this.newImport = newImport;
        }

        public void cancel() {
            stopRun = true;
        }

        public void reset() {
            stopRun = false;
        }
    }
}
