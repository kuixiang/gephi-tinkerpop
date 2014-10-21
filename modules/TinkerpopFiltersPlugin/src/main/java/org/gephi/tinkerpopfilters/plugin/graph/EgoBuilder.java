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
import org.gephi.desktop.connection.TinkerpopConfig;
import org.gephi.desktop.connection.TinkerpopFilterCommon;
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
import java.lang.Math;

/**
 *
 * @author Xia Zhu
 */
@ServiceProvider(service = FilterBuilder.class)
public class EgoBuilder implements FilterBuilder {
    private static final String TINKERPOP_EDGE_KEY = "_id";

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.description");
    }

    public Filter getFilter() {
        return new EgoFilter();
    }

    public JPanel getPanel(Filter filter) {
        EgoUI ui = Lookup.getDefault().lookup(EgoUI.class);
        if (ui != null) {
            return ui.getPanel((EgoFilter) filter);
        }
        return null;
    }

    public void destroy(Filter filter) {

    }

    public void stop(Filter filter) {
        ((EgoFilter) filter).cancel();
    }

    public void reset(Filter filter) {
        ((EgoFilter) filter).reset();
    }

    public static class EgoFilter implements ComplexFilter {
        volatile boolean stopRun = false;
        private TinkerpopConfig config = new TinkerpopConfig();
        private TinkerpopFilterCommon tinkerpopFilterCommon = new TinkerpopFilterCommon();
        private String attributeKey = "";
        private String attributeValue = "";
        private String distribution = "100";
        private boolean self = true;
        private int depth = 1;
        private int numVertices = Integer.MAX_VALUE;
        boolean newImport = true;

        public Graph filter(Graph graph, boolean leafOnly, boolean isLeaf) {
            HierarchicalGraph hgraph = (HierarchicalGraph) graph;
            String[] distributions = distribution.split("[\\s,\\t]+");
            float[] distributionList = new float[depth];
            if (distributions.length != depth){
                tinkerpopFilterCommon.notifyUser("Expect to have " + depth +
                " percentage number, only got " + distributions.length +
                ". Please update \"distribution\" with " + depth + " percentage numbers.");
                return hgraph;
            } else {
               float sum = 0.0f;
                for(int i = 0; i < depth; i++){
                    distributionList[i] = Float.parseFloat(distributions[i])/100f;
                    sum += Float.parseFloat(distributions[i]);
                }
               if (sum != 100.0f) {
                   tinkerpopFilterCommon.notifyUser("The sum of percentage on each leavel" +
                       " should be 100. Please update \"distribution\" text field.");
                   return hgraph;
               }
            }

            if(attributeKey == "" || attributeKey == null || attributeKey.isEmpty()){
                tinkerpopFilterCommon.notifyUser("attributeKey is empty! " +
                    "Please input attributeKey.");
                return hgraph;
            }
            if(attributeValue == "" || attributeValue == null || attributeValue.isEmpty()){
                tinkerpopFilterCommon.notifyUser("attributeValue is empty! " +
                    "Please input attributeValue.");
                return hgraph;
            }
            if(numVertices < 0){
                tinkerpopFilterCommon.notifyUser("The numVertices is negtive number! " +
                    "Please input positive number for it.");
                return hgraph;
            }
            if(numVertices == Integer.MAX_VALUE){
                tinkerpopFilterCommon.notifyUser("numVertices is the default Integer.MAX_VALUE. " +
                    "Expect long time exeuction time time.");
            }


            int upper = numVertices - 1;
            int[] numPerLevel = new int[depth];
            String[] edgeGremlin = new String[depth];
            String tmpGremlin = "g.V.filter{it." + attributeKey + "==\"" +
                attributeValue + "\"}";
            String vertexGremlin = "x=[];" + tmpGremlin;

            if(self){
                upper -= 1;
                vertexGremlin += ".aggregate(x)";
            }

            //check stopRun periodically to make sure the fitler can be stopped at any time
            if( numVertices != Integer.MAX_VALUE){
                numPerLevel[0] = upper;
                if(depth > 1){
                    numPerLevel[depth - 1] = upper;
                    for (int i = 0; i < (depth - 1); i++){
                        numPerLevel[i] = (int) ((float)numVertices * distributionList[i]) - 1;
                        numPerLevel[depth - 1] -= numPerLevel[i];
                    }
                    numPerLevel[depth - 1] -= 1;
                }
                for(int i = 0; i < depth; i++){
                    if(numPerLevel[i] > 0){
                        vertexGremlin += ".both.dedup[0.." + numPerLevel[i] + "].aggregate(x)";
                        edgeGremlin[i]  = tmpGremlin + ".bothE.dedup[0.." + numPerLevel[i] + "]";
                        tmpGremlin += ".both.dedup[0.." + numPerLevel[i] + "]";
                    }
                }
            } else {
                for(int i = 0; i < depth; i++){
                    vertexGremlin += ".both.dedup.aggregate(x)";
                    edgeGremlin[i]  = tmpGremlin + ".bothE.dedup";
                    tmpGremlin += ".both.dedup";
                }
            }
            upper = numVertices - 1;
            vertexGremlin += ".iterate();g.V.retain(x).dedup[0.." + upper + "]";
            config = tinkerpopFilterCommon.getDatabaseInfo();
            JSONArray results = new JSONArray();

            if(leafOnly && newImport || !leafOnly && isLeaf){
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
                    for(int i = 0; i < depth; i++){
                        if(!stopRun){
                            results = tinkerpopFilterCommon.tinkerpopNewQuery(edgeGremlin[i], config, false,
                                this.getName());
                        }
                        if(!stopRun && results.length() > 0){
                            tinkerpopFilterCommon.getGraphElements(results, graph, false);
                            graph = tinkerpopFilterCommon.getGraph();
                            hgraph = (HierarchicalGraph) graph;
                        }
                    }
                }
            } else {
                //filter based on existing hgraph
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
                        if(!stopRun){
                            for (Edge edge : hgraph.getEdges().toArray()) {
                                if(stopRun){
                                    break;
                                }
                                inputEdgeIds.add(edge.getEdgeData().getAttributes()
                                    .getValue(TINKERPOP_EDGE_KEY).toString());
                            }
                            for(int i = 0; i < depth; i++){
                                if(!stopRun && hgraph.getEdgeCount() > 0){
                                    Set<String> resultIds = new HashSet<String>();
                                    resultIds = tinkerpopFilterCommon.tinkerpopEdgeQuery(edgeGremlin[i], graph, config,
                                        inputEdgeIds, this.getName());
                                    resultEdgeIds.addAll(resultIds);
                                }
                            }

                            if(resultEdgeIds.size() > 0){
                                //remove residual un-needed edges
                                for (Edge edge : hgraph.getEdges().toArray()) {
                                    if(stopRun){
                                        break;
                                    }
                                    String atkEdgeId = edge.getEdgeData().getAttributes()
                                        .getValue(TINKERPOP_EDGE_KEY).toString();
                                    if (!resultEdgeIds.contains(atkEdgeId)) {
                                        hgraph.writeLock();
                                        hgraph.removeEdge(edge);
                                        hgraph.writeUnlock();
                                        //System.out.println("Remove edge " + edge.getEdgeData().getId());
                                    } else {
                                        //System.out.println("Keep edge " + edge.getEdgeData().getId());

                                    }
                                }
                            }  else if (resultEdgeIds.size() == 0){
                                //remove all edges
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
            }
            graph = tinkerpopFilterCommon.getGraph();
            hgraph = (HierarchicalGraph) graph;
            stopRun = true;
            return hgraph;
        }

        public String getName() {
            return NbBundle.getMessage(EgoBuilder.class, "EgoBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, String.class, "attributeKey"),
                            FilterProperty.createProperty(this, String.class, "attributeValue"),
                            FilterProperty.createProperty(this, Integer.class, "depth"),
                            FilterProperty.createProperty(this, String.class, "distribution"),
                            FilterProperty.createProperty(this, String.class, "numVertices"),
                            FilterProperty.createProperty(this, Boolean.class, "self"),
                            FilterProperty.createProperty(this, Boolean.class, "newImport")};
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
            return new FilterProperty[0];
        }

        public String getAttributeKey() {
            return attributeKey;
        }

        public void setAttributeKey(String attributeKey) {
            this.attributeKey = attributeKey;
        }

        public String getAttributeValue() {
            return attributeValue;
        }

        public void setAttributeValue(String attributeValue) {
            this.attributeValue = attributeValue;
        }

        public String getDistribution() {
            return distribution;
        }

        public void setDistribution(String distribution) {
            this.distribution = distribution;
        }

        public Integer getDepth() {
            return depth;
        }

        public void setDepth(Integer depth) {
            this.depth = depth;
        }

        public String getNumVertices() {
            return Integer.toString(numVertices);
        }

        public void setNumVertices(String numVertices) {
            this.numVertices = Integer.parseInt(numVertices);
        }

        public boolean isSelf() {
            return self;
        }

        public void setSelf(boolean self) {
            this.self = self;
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
