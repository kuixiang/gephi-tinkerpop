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
package org.gephi.desktop.connection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author Xia Zhu
 */
public class TinkerpopFilterCommon {
    private static final String RESULT = "result";
    private static final String RESULTS = "results";

    public TinkerpopFilterCommon() {
    }

    public TinkerpopConfig getDatabaseInfo() {
        //get graph database info from ConnectionUIController
        ConnectionUIController controller = Lookup.getDefault().lookup(ConnectionUIController.class);
        return controller.getConfig();
    }

    public JSONObject getGraphInfo(TinkerpopConfig config) {
        String uri = generateTinkerpopGetUri(config);
        JSONObject responseObj = new JSONObject();
        if(uri != "" && uri != null && !uri.isEmpty()){
            HttpResponse response = sendTinkerpopGetRequest(uri, config, true);
            responseObj = getResponseObj(response, true);
        }
        return  responseObj;
    }

    public boolean checkConfig(TinkerpopConfig config){
        if(config.getSchema() == ""){
            notifyUser("No schema is configured for ATK server. Please configure it from " +
                "$GEPHI_HOME/etc/graph/config.json. And select it from Window->" +
                "\"Connect to Intel ATK\"->\"+\"");
            return false;
        }
        if(config.getHost() == ""){
            notifyUser("No host is configured for ATK server. Please configure it from " +
                "$GEPHI_HOME/etc/graph/config.json. And select it from Window->" +
                "\"Connect to Intel ATK\"->\"+\"");
            return false;
        }
        if(config.getServerPort() == ""){
            notifyUser("No server port number is configured for ATK server. " +
                "Please configure it from " +
                "$GEPHI_HOME/etc/graph/config.json. And select it from Window->" +
                "\"Connect to Intel ATK\"->\"+\"");
            return false;
        }
        return true;
    }


    public String generateTitanRequest(String source, TinkerpopConfig config) {
        URI uri = null;
        if(!checkConfig(config)){
            return null;
        }
        String schema = config.getSchema();
        String host = config.getHost();
        String serverPort = config.getServerPort();
        String requestPath = "/graphs/" + config.getGraphName() + "/tp/gremlin";
        String requestStr = schema + "://" + host + ":" + serverPort + requestPath +
            "?script=" + source;
        URIBuilder uriBuilder = new URIBuilder();

        //need to encode gremlin scripts
        uriBuilder.setScheme(schema).setHost(host).setPort(Integer.parseInt(serverPort))
            .setPath(requestPath).setParameter("script", source);
        try {
            uri = uriBuilder.build();
            /*
            System.out.println("URI request " + requestStr + " is OK, " +
                "converted to " + uri.toString());
            */
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URI request " + requestStr + " has syntax error", e);
        }
        return  uri.toString();
    }

    public Set<Integer> tinkerpopVertexQuery(String gremlinScript, Graph graph,
                                           TinkerpopConfig config,
                                Set<Integer> resultIds,
                                String name) {
        JSONArray results = tinkerpopNewQuery(gremlinScript, config, true, name);
        parseForVertexFilter(results, graph, resultIds);
        return resultIds;
    }

    public Set<String> tinkerpopEdgeQuery(String gremlinScript, Graph graph,
                                            TinkerpopConfig config,
                                            Set<String> resultIds,
                                            String name) {
        JSONArray results = tinkerpopNewQuery(gremlinScript, config, false, name);
        Set<String> edgeIds = parseForEdgeFilter(results, graph, resultIds);
        return edgeIds;
    }

    public JSONArray tinkerpopNewQuery(String gremlinScript, TinkerpopConfig config,
                                       boolean vertexQuery, String name) {
        JSONObject responseObj = new JSONObject();
        JSONArray results = new JSONArray();
        String requestStr = generateTitanRequest(gremlinScript, config);
        if(requestStr == null || requestStr == "" || requestStr.isEmpty()){
            return results;
        }
        HttpResponse response = sendTinkerpopGetRequest(requestStr, config, false);
        if(response == null){
            return results;
        }
        responseObj = getResponseObj(response, false);
        //when there is message field, it means no query result returned
        if (responseObj.has("error")) {
            String msg = responseObj.getString("error");
            notifyUser("Query returned error message: " + msg);
            return results;
        } else if (responseObj.has("message")) {
            String msg = responseObj.getString("message");
            notifyUser("Query returned error message: " + msg);
            return results;
        }

        try{
            results = responseObj.getJSONArray(RESULTS);
        } catch (JSONException e) {
            notifyUser("No query result returned for " + name);
        }

        if (results.length() == 0){
            if(vertexQuery){
                notifyUser("No vertices match " + name);
            } else {
                notifyUser("No edges match " + name);
            }
        } else {
            //System.out.println("Number of results:\t" + size);
        }

        return results;
    }


    public String generateTinkerpopGetUri(TinkerpopConfig config) {
        String requestStr = "";
        if(!checkConfig(config)){
            return  requestStr;
        }
        String schema = config.getSchema();
        String host = config.getHost();
        String serverPort = config.getServerPort();
        String graphName = config.getGraphName();
        String baseUriWithPort = schema + "://" + host + ":" + serverPort;
        requestStr = baseUriWithPort + "/graphs/" + graphName;
        return  requestStr;
    }

    public HttpResponse sendTinkerpopGetRequest(String uri, TinkerpopConfig config,
                                               boolean connect) {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(uri);
        HttpResponse response = null;
        if(!checkConfig(config)){
            return  response;
        }
        String baseUriWithPort = config.getSchema() + "://" +
            config.getHost() + ":" + config.getServerPort();


        try{
            //System.out.println("sending HTTP GET " + uri);
            response = client.execute(request);
        } catch (final HttpResponseException e){
            if(connect){
                notifyError("Could not connect to Tinkerpop server " + baseUriWithPort, e);
            } else {
                notifyError("Could not get http get response frome " + baseUriWithPort, e);
            }
            e.printStackTrace();
            throw new IllegalArgumentException(
                "ClientProtocolException when getting http post response");
        } catch (final ClientProtocolException e){
            if(connect){
                notifyError("Could not connect to Tinkerpop server " + baseUriWithPort, e);
            } else {
                notifyError("Could not get http get response frome " + baseUriWithPort, e);
            }
            e.printStackTrace();
            throw new IllegalArgumentException("ClientProtocolException when getting http " +
                "get response");
        } catch (IOException e) {
            if(connect){
                notifyError("Could not connect to Tinkerpop server " + baseUriWithPort, e);
            } else {
                notifyError("Could not get http get response frome " + baseUriWithPort, e);
            }
            e.printStackTrace();
            throw new IllegalArgumentException("IOException when getting http get response");
        }
        return response;
    }

    public JSONObject getResponseObj(HttpResponse response, boolean connect) {
        String wholeReponse = "";
        JSONObject responseObj = new JSONObject();

        try{
            BufferedReader rd = new BufferedReader (
                new InputStreamReader(response.getEntity().getContent()));

            String line;
            //parse results
            while ((line = rd.readLine()) != null) {
                //System.out.println(line);
                wholeReponse += line;
            }
        } catch (HttpResponseException e){
            if(connect){
                notifyError("Could not get graph info. " , e);
            } else {
                notifyError("When parsing http response got ", e);
            }
            e.printStackTrace();
            throw new IllegalArgumentException(
                "HttpResponseException when parsing http response");
        } catch (IOException e) {
            if(connect){
                notifyError("Could not get graph info. ", e);
            } else {
                notifyError("When parsing http response got ", e);
            }
            e.printStackTrace();
            throw new IllegalArgumentException("IOException when parsing http response");
        }

        try{
            responseObj = new JSONObject(wholeReponse);
        } catch (JSONException e) {
            notifyError("JSONException ", e);
            e.printStackTrace();
            throw new IllegalArgumentException("Couldn't parse JSON object from response " +
                response);
        }
        return responseObj;
    }

    public static Map<String,Object> parse(JSONObject json , Map<String,Object> out) {
        Iterator<String> keys = json.keys();
        while(keys.hasNext()){
            String key = keys.next();
            Object val;

            try{
                val = json.get(key);
                out.put(key, val);
            } catch (JSONException e) {
                throw new JSONException("Couldn't parse JSON value for key " + key + e.toString());
            }
        }
        return out;
    }

    public Graph getGraph(){
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        return graphModel.getHierarchicalMixedGraph();
    }

    public Graph getGraphElements(JSONArray results, Graph graph, boolean forVertices){
        int size = results.length();
        if(size > 0){
            GraphFactory factory = graph.getGraphModel().factory();
            int numIgnoredNoSrcNode = 0;
            int numIgnoredNoTgtNode = 0;
            for(int i = 0; i < size; i++){
                JSONObject graphObj;
                try{
                    graphObj = results.getJSONObject(i);
                } catch (JSONException e) {
                    throw new JSONException(
                        "Couldn't parse JSON object from vertex results element " + i + e.toString());
                }

                //add vertices
                if (forVertices) {
                    //parse vertex id first
                    int id;
                    try{
                        id = graphObj.getInt("_id");
                    } catch (JSONException e) {
                        throw new JSONException("Couldn't parse vertex Id " + e.toString());
                    }
                    String nodeId = Integer.toString(id);

                    //then parse vertex properties
                    Map<String, Object> attributes = new HashMap<String, Object>();
                    parse(graphObj, attributes);
                    Node node = graph.getNode(nodeId);
                    if (node == null) {
                        node = factory.newNode(nodeId);

                        if (attributes!=null && attributes.size() > 0) {
                            for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                                //this.addNodeAttribute(node, entry.getKey(), entry.getValue());
                                node.getNodeData().getAttributes().setValue(entry.getKey(),
                                    entry.getValue());
                            }
                        }

                        graph.writeLock();
                        graph.setId(node, nodeId);
                        graph.addNode(node);
                        //System.out.println("add new Node : " + nodeId);
                        graph.writeUnlock();
                    } else {
                        graph.writeLock();

                        if (attributes!=null && attributes.size() > 0) {
                            for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                                node.getNodeData().getAttributes().setValue(entry.getKey(),
                                    entry.getValue());
                            }
                        }
                        graph.writeUnlock();
                        //System.out.println("add to Node : " + nodeId);
                    }
                } else {
                    //parse edge id first
                    String id;
                    try{
                        id = graphObj.getString("_id");
                    } catch (JSONException e) {
                        throw new JSONException("Couldn't parse edge Id " + e.toString());
                    }
                    //add edges
                    int source;
                    int target;
                    Edge edge = null;
                    try{
                        source = graphObj.getInt("_outV");
                        target = graphObj.getInt("_inV");
                    } catch (JSONException e) {
                        throw new JSONException("Couldn't parse edge " + e.toString());
                    }
                    String sourceId = Integer.toString(source);
                    String targetId = Integer.toString(target);
                    //add edge properties
                    Map<String,Object> attributes = new HashMap<String, Object>();
                    parse(graphObj, attributes);

                    Node sourceNode = graph.getNode(source);
                    Node targetNode = graph.getNode(target);

                    if (sourceNode==null) {
                        numIgnoredNoSrcNode += 1;
                    } else if (targetNode==null) {
                        numIgnoredNoTgtNode += 1;
                    } else if (graph.getEdge(sourceNode, targetNode) == null) {
                        edge = factory.newEdge(null, sourceNode, targetNode, 1.0f, true);
                        if (graphObj.has("_label")){
                            String label = graphObj.getString("_label");
                            edge.getEdgeData().setLabel(label);
                        }
                        if (attributes!=null && attributes.size() > 0) {
                            for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                                final String propertyKey = entry.getKey();
                                final Object propertyValue = entry.getValue();
                                edge.getEdgeData().getAttributes().setValue(propertyKey, propertyValue);
                            }
                        }
                        graph.writeLock();
                        graph.addEdge(edge);
                        graph.writeUnlock();
                    }
                }
            }
            /*
            if (!forVertices){
                if(numIgnoredNoSrcNode > 0){
                    System.out.println("Ignored " + numIgnoredNoSrcNode + " edges because source Node" +
                        " is not in scope.");
                }
                if(numIgnoredNoTgtNode > 0){
                    System.out.println("Ignored " + numIgnoredNoTgtNode + " edges because target Node" +
                        " is not in scope.");
                }
            } */
        }
        return graph;
    }

    public Set<Integer> parseForVertexFilter(JSONArray results, Graph graph,
                                             Set<Integer> resultIds){
        int size = results.length();
        if(size > 0){
            for(int i = 0; i < size; i++){

                JSONObject resultGraph = results.getJSONObject(i);
                // sample result
                // "result": {
                //    "results": [{
                //        "target": 5242987,
                //        "value": "0.5 0.125 0.125 0.125 0.125",
                //        "vertex_type": "TR",
                //        "_id": 26400004,
                //        "_type": "vertex"
                //        }],
                //     "run_time_seconds": 0.042
                // }
                int id = resultGraph.getInt("_id");
                String nodeId = Integer.toString(id);
                Node node = graph.getNode(nodeId);
                //only keep the node id in existing graphs
                if (node != null){
                    //System.out.println("Add vertex Id " + id);
                    resultIds.add(id);
                }
            }
        }

        return resultIds;
    }

    public Set<String> parseForEdgeFilter(JSONArray results, Graph graph,
                                          Set<String> resultIds){
        Set<String> resultEdgeIds = new HashSet<String>();
        int size = results.length();
        if(size > 0){
            for(int i = 0; i < size; i++){

                JSONObject resultGraph = results.getJSONObject(i);
                //"result":{
                //    "results":[{
                //        "weight":1,
                //            "_id":"16dtqv-TnVi-1U",
                //            "_type":"edge",
                //            "_outV":13200004,
                //            "_inV":68402504,
                //            "_label":"edge"
                //    }],
                //    "run_time_seconds":0.035
                //}
                String id = resultGraph.getString("_id");
                if (resultIds.contains(id)) {
                    resultEdgeIds.add(id);
                }
            }
        }
        return resultEdgeIds;
    }

    /**
     * notify error to users
     *
     * @param userMessage the error message
     * @param t the throwable message
     *
     */
    public void notifyError(String userMessage, Throwable t) {
        if (t instanceof OutOfMemoryError) {
            return;
        }
        String message = message = t.toString();
        NotifyDescriptor.Message msg =
            new NotifyDescriptor.Message(
                userMessage + "\n" + message,
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(msg);
    }

    /**
     * notify users
     *
     * @param userMessage the error message
     * @param t the throwable message
     *
     */
    public void notifyUser(String userMessage) {
        NotifyDescriptor.Message msg =
            new NotifyDescriptor.Message(
                userMessage,
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(msg);
    }
}
