# Gephi-Tinkerpop:  Query and Visualize Blueprints Graphs in Gephi

## Introduction

[Gephi](http://gephi.org) is an award-winning open-source platform for visualizing and 
manipulating large graphs. It is powered by a built-in OpenGL engine, 
and with modular design. It is easy to install and 
get started.

[Tinkerpop](http://www.tinkerpopbook.com/) is an open-source stack for the emerging graph 
landscape. Its [Blueprints API](http://blueprints.tinkerpop.com/) is a property graph model and 
the foundation of the stack. Its [Rexster](http://rexster.tinkerpop.com/) is a multi-faceted graph server that exposes any
 Blueprints graph through several mechanisms with a general focus on REST. [Many graph databases](http://en.wikipedia.org/wiki/Graph_database)
 supports blueprints API, for example, [Neo4j](http://www.neo4j.org), 
 [Titan](http://thinkaurelius.github.io/titan/), 
 [OrientDB](http://www.orientechnologies.com/orientdb),
 [InfinityGraph](http://infinitegraph.com/),
 [DEX](http://www.sparsity-technologies.com/dex) etc.
 
[Gephi-Tinkerpop](https://github.com/xiazhu/gephi-tinkerpop) builds a bridge between these two 
excellent open-source projects. It enables users to query Blueprints graphs created by any graph 
databases, either remotely or from the same machine, via Rexster REST API, 
and explore/visualize the queried graphs or subgraphs in Gephi. 

[Gephi-Tinkerpop](https://github.com/xiazhu/gephi-tinkerpop) enables users to query Blueprints 
graphs via [Gremlin](http://gremlindocs.com/). It currently supports vertex-oriented Gremlin 
queries, edge-oriented Gremlin queries, and Ego network query.
 

## Install and run Gephi-Tinkerpop
                                                                                                                    
### Requirements

- Java JDK 6 or 7 with preferably [Oracle Java JDK](http://java.com/en/).

- [Apache Maven](http://maven.apache.org/) version 3.0.3 or later

### Checkout and Build the sources

- Get a copy of source code by git clone

        git clone git@github.com:username/gephi-tinkerpop.git

- Build

        mvn clean install -DskipTests --also-make --projects modules/application

- Once built, the binary is ready for run

		The directory $GEPHI_TAARGET=$GEPHI_TINKERPOP_SRC_HOME/modules/application/target/gephi
		 is the build target. 

- Configure Gephi-Tinkerpop

        Gephi-Tinkerpop expects users to write configuration file $GEPHI_TARGET/etc/gephi-tinkerpop.json
        So, before running Gephi-Tinkerpop, please edit the file to tell Gephi-Tinkerpop the IP 
        and port number of the Tinkerpop Rexster server, as well as the names of graphs serving 
        by the Rexster server, in [JSON](http://www.json.org) format. 
        
        An example of Gephi-Tinkerpop is as follows:
        
        
        {
         "base-uri":"http://fake.fake.fake.com",
         "server-port":"8182",
         "graphs":["newLBP", "MovieLens", "TitanGods"]
        }

- Run Gephi-Tinkerpop

        At Linux, you can start to use Gephi-Tinkerpop simply by the following command: 
		bin/gephi          

- Default Gephi configuration. 
		Since Gephi-Tinkerpop is based on Gephi project. Sometimes you may also want to configure 
		Gephi itself. 
		By default, the Gephi configuration file is at $GEPHI_TINKERPOP_SRC_HOME/modules/application/gephi/etc/gephi.conf 		
		An example Gephi configuration file is as follows:
		
		# ${HOME} will be replaced by user home directory according to platform
        default_userdir="${HOME}/.${APPNAME}/0.8.2-SNAPSHOT/dev"
        default_mac_userdir="${HOME}/Library/Application Support/${APPNAME}/0.8.2-SNAPSHOT/dev"
        
        # options used by the launcher by default, can be overridden by explicit
        # command line switches
        default_options="--branding gephi -J-Xms64m -J-Xmx8192m -J-Xverify:none -J-Dsun.java2d
        .noddraw=true -J-Dsun.awt.noerasebackground=true -J-Dnetbeans.indexing.noFileRefresh=true -J-Dplugin.manager.check.interval=EVERY_DAY"
        # for development purposes you may wish to append: -J-Dnetbeans.logger.console=true -J-ea
        
        # default location of JDK/JRE, can be overridden by using --jdkhome <dir> switch
        #jdkhome="/path/to/jdk"
        
        # clusters' paths separated by path.separator (semicolon on Windows, colon on Unices)
        #extra_clusters=
		
- Change Gephi configuration when needed.	
	
		"default_userdir" is where Gephi runtime configuration and log file are saved.  The log	file is at $default_useridr/var/log/message.log  
		So, if you want to re-direct the log file to different location, please edit "default_userdir". 
		
		"default_options" is the JVM configurations. You can enlarge "-J-Xmx" when visualizing large graphs. 

## Gephi-Tinkerpop Tutorial

### Connect to Graph Database
Before submitting queries to Tinkerpop Rexster server. The first step is to edit the configuration file $GEPHI_TARGET/etc/gephi-tinkerpop.json as mentioned in above section. The second step is to select graph from configuration panel. Please see below video on how to do the second step.

[![Connect to Graph Database](http://img.youtube.com/vi/Jz0t7wue5qc/0.jpg)](https://www.youtube.com/watch?v=Jz0t7wue5qc)
   
   
### Vertex-Oriented Query
Vertex-Oriented Query are for users to submit abitraty/customerized Vertex related queries. There are three parameters for Vertex-Oriented Queries. 
    
- Gremlin Script:

      Users can write their customerized [Gremlin](http://gremlindocs.com) script. For 
      Vertex-Oriented Queries, we expect the results of the Gremlin script are set of vertices. 
      Some examples are:
      
      
       "g.V[0..9]" is to find the first 10 vertices.
       
       "g.V.filter{it.title=="Toy Story (1995)"}.inE.filter{it.rating > 3}.outV.outE.filter{it
       .rating > 3}.inV[0..99]"
         is to find for users who related "Toy Story (1995)" as score 3, 
         what are other movies they also rated as score 3, and output the first 100 such movies.
         
- WithSubgraph:

      When it is checked, the edges among the resulting vertices will also be queried and shown 
      in Gephi GUI.

- NewImport:
    
      When it is checked, it means the query results become the base graph in Gephi.
      
         
    Please see below video on how to submit Vertex-Oriented Query. The Gremlin Query in the 
    example is to get the first 500 vertices from a graph named "MovieLens".   

[![Vertex-Oriented Query](http://img.youtube.com/vi/juRIJxBAXVI/0.jpg)](https://www.youtube.com/watch?v=juRIJxBAXVI)
   
### Edge-Oriented Query
   Edge-Oriented Query are for users to submit abitraty/customerized Edge related queries. There 
   are two parameters for Vertex-Oriented Queries. 
    
- Gremlin Script:

      Users can write their customerized [Gremlin](http://gremlindocs.com) script. For 
      Edge-Oriented Queries, we expect the results of the Gremlin script are set of edges. 
      Some examples are:
      
      
       "g.E[0..99]" is to find the first 100 edges.
       
       "g.E.filter{it.rating==3}.dedup[0..1999]"
         is to find for the edges with rating/score 3, and output the first 2000 such edges.
         
- NewImport:
    
      When it is checked, it means the query results become the base graph in Gephi.
      
             
    Please see below video on how to submit Edge-Oriented Query. The Gremlin Query in the 
    example is to get the 1000 edges with index in range of [1..1000] from a graph named 
    "MovieLens".
    
[![Edge-Oriented Query](http://img.youtube.com/vi/7a8wMweMq3I/0.jpg)](https://www.youtube.com/watch?v=7a8wMweMq3I)
 
### Ego-Network Query
   Ego-Network Query is to firstly find a user interested vertex, 
    and then get teh Ego Network of this vertex. There are seven parameters for 
    Vertex-Oriented Queries. 
     
- Attribute Key:
 
    Expect users to input a string which is the name of a Vertex Attribute/Property. For example, if a user want to get a vertex based on its title, "title" should be entered.
          
- Attribute Value:
     
    Expect users to input value to the Attribute Key to find the interested vertex.For example, if a user want to find a movie with title "Toy Story (1995)", then "Toy Story (1995)" should be entered for Attribute Value field.
      
- Depth:
     
    How many level of Ego Network to show. Supported level is 1 to 5.
       
- Vertex Distribution:
     
    What is the maximum percentage of vertices to show in each level. By default, equal percentage will be shown for each level. Users can edit this field to customize the vertex distribution.
    
    Expect comma separate list of percentage numbers, one for each level, and the sum of them should be 100.   

- Max Number Vertices:

    What is the maximum number of vertices to show. The resulting number of vertices can be smaller than the configured number, depending on how many vertices are available on each level. 
         
- WithSelf:    
   Whether to include the interested Vertex which was identifed by Attribute Key and Attribute Value.

- NewImport:    
   When it is checked, it means the query results become the base graph in Gephi.
      
         
              
     Please see below video on how to submit Ego Network Query. The example is to firstly find a 
     movie with title "Toy Story (1995)", and then find the three level Ego Network centered on 
     this vertex. The maximum number of vertices to show is 3600, with 33%, 33%, 
     and 34% of vertices shown at level 1 to 3, respectively.
     
[![Ego-Network Query](http://img.youtube.com/vi/At7vDTUFFW0/0.jpg)](https://www.youtube.com/watch?v=At7vDTUFFW0)
  
### Nested Queries
  
  Users can also submit nested queries by draging a new query to the location of "Drag subfilter 
  here" of an existing Query. The nested queries will be executed from the leaf of the query 
  tree, and going to upper level of the tree step by step. When nested queries are used, 
  it is implied that the query at the leaf is "NewImport", while queries on non-leaf location are
   based on existing queries. In this case, users have to guarantee the "NewImport" is correctly 
   set for each level. The internal software will automatically set it correctly.
   
   Please see below video on how to submit Nested Queries. The example is to nest the following 
   three queries.
   
   - find the first 500 vertices
   
   - find 1000 edges with index [1..1000]
   
   - find the 3-level Ego Network of "Toy Story (1995)", with maximally 3600 vertices output
  
[![Nested Query](http://img.youtube.com/vi/5fPtZJqpfjA/0.jpg)](https://www.youtube.com/watch?v=5fPtZJqpfjA)
  
  
### Query Example

 Please see the result of an example Vertex-Oriented Query as follows. It is to firstly find for users who rated "Toy Story (1995)" as score 5, what are other movies they also rated score 5. Then output the top 30 such movies. Movies are colored by their genre.
  
![Vertex Query Example](./modules/DesktopConnection/src/main/resources/org/gephi/desktop/connection/resources/Query_Example.png?raw=true "Vertex Query Example")

  
### Gephi Tutorial

 Since Gephi-Tinkerpop is based on Gephi project. You will find Gephi tutorials be very 
 useful on graph layout and many other things.
 Get started with the [Gephi Quick Start](http://gephi.org/users/quick-start/) and follow the 
 [Tutorials](http://gephi.org/users/). Load a sample [dataset](https://wiki.gephi.org/index.php?title=Datasets) and start to play with the data.


## License

Same as Gephi main source code, this work is distributed under the dual license [CDDL 1.0]
(http://www.opensource.org/licenses/CDDL-1.0) and [GNU General Public License v3](http://www.gnu.org/licenses/gpl.html). Read the [Legal FAQs](https://gephi.org/about/legal/faq/)  to learn more.

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
