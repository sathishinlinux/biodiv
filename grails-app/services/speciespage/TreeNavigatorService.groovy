package speciespage
/*
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;
*/

class TreeNavigatorService {

    static transactional = true
	//TODO : write DB PATH
/*
    static final String DB_PATH = "/home/rahulk/graphdb";
    static final int MAX_DEPTH = 75;
    static GraphDatabaseService treeDb = null ;
    static ExecutionEngine engine = null ;
    
    def serviceMethod() {

    }
    
    private synchronized void getGraphDatabaseService () {
        println "===========GRAPH DB SERVICE ========== " + DB_PATH
        if(treeDb == null) {
            treeDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        }
        println "=========================="
        return
    }

    private synchronized void getExecutionEngine () {
        println "========EXECUTION ENGINE ==========="
        if(engine == null) {
            engine = new ExecutionEngine( treeDb );
        }
        println "++++++++++++++++++++++"
        return
    }

    def  countOfAllInnerNodes (long nodeId) {
        return countAllInnerNodes(nodeId);
    }

    def countOfAllInnerLeafNodes (long nodeId) {
        return countAllLeafNodes(nodeId);
    }

    def getInnerNodesId(long nodeId) {
        println "=====NAV SERVICE =========="
        getGraphDatabaseService();
        getExecutionEngine();
        return allInnerNodesId(nodeId)
    }

    def getInnerLeafNodesId(long nodeId){
        return allLeafNodesId(nodeId)    
    }

    def getCurator(long nodeId) {
        Node end = treeDb.getNodeById(nodeId)
        Iterable<Path> paths = getPaths ( start, end)
        return getCuratorsId ( paths );
    }

    def getContibutor( long nodeId) {
        Node end = treeDb.getNodeById(nodeId)
        Iterable<Path> paths = getPaths ( start, end)
        return getContributorsId ( paths );
    }

    def isCuratorOf( long nodeId ) {
        return isCuratorOfId(nodeId) 
    }

    def isContributorOf( long nodeId ) {
        return isContributorOfId(nodeId) 
    }

    
    private Traverser childrenTraversal( Node snode ) {
    	TraversalDescription td = Traversal.description().breadthFirst().relationships( RelTypes.DAUGHTER, Direction.OUTGOING ).evaluator( Evaluators.excludeStartPosition() );
    	return td.traverse( snode);
    }

    private int countAllInnerNodes (long nodeId) {
    	int numOfChild = 0;
        Node snode = treeDb.getNodeById(nodeId)
        Traverser childTraverser = childrenTraversal( snode );
    	for ( Path path : childTraverser )
    	{
    	    numOfChild++;
    	}
        return numOfChild;
    }

    private int countAllLeafNodes(long nodeId) {
    	int numAllLeafNodes = 0;
    	Map<String, Object> params = new HashMap<String, Object>();
		params.put( "nodeId", nodeId );
		ExecutionResult result = engine.execute( "START n1=node({nodeId}) MATCH n1-[:DAUGHTER*]->x RETURN x ",params);
		Iterator<Node> n_column = result.columnAs( "x" );
		for ( Node node : IteratorUtil.asIterable( n_column ) ) {
			if(node.hasProperty("LEAF_NODE")) {
                numAllLeafNodes++;
            }
		}
		return noAllLeafNodes;
    }

    private List allInnerNodesId (long nodeId) {
        def nodesId = []
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "nodeId", nodeId );
        println "================= " + nodeId +"============ "+ treeDb; 
        Node n = treeDb.getNodeById(nodeId);
        println "============================= " + params + " ========== " + n;
        ExecutionResult result = engine.execute( "START n1=node({nodeId}) MATCH n1-[:DAUGHTER*]->x RETURN x ",params);
        Iterator<Node> n_column = result.columnAs( "x" );
        for ( Node node : IteratorUtil.asIterable( n_column ) ) {
            nodesId.add(node.getId());
        }
        return nodesId;
    }
 
    private List allLeafNodesId(long nodeId) {
    	def nodesId = []
    	Map<String, Object> params = new HashMap<String, Object>();
		params.put( "nodeId", nodeId );
		ExecutionResult result = engine.execute( "START n1=node({nodeId}) MATCH n1-[:DAUGHTER*]->x RETURN x ",params);
		Iterator<Node> n_column = result.columnAs( "x" );
		for ( Node node : IteratorUtil.asIterable( n_column ) ) {
			if(node.hasProperty("LEAF_NODE")) {
                nodesId.add(node.getId());
             }
		 }
		return nodesId;
    }
    
    private Iterable<Path> getPaths ( Node start, Node end) {
    	PathFinder<Path> finder = GraphAlgoFactory.allSimplePaths(
                Traversal.expanderForTypes( RelTypes.DAUGHTER, Direction.OUTGOING ), MAX_DEPTH );
    	Iterable<Path> paths = finder.findAllPaths( start, end );
    	return paths;
    }
    
    private List getCuratorsId (Iterable<Path> paths) {
    	Map<String, Object> params = new HashMap<String, Object>();
    	def cur = [];
        if(!paths.iterator().hasNext()) {
    		return cur;
    	}
        Path path = paths.iterator().next();
    	Iterator<Node> nodes = path.nodes().iterator();
     	while(nodes.hasNext()) {
    		Node n = nodes.next();
     		if(n.hasRelationship(RelTypes.HAS_CURATOR)) {
    			params.put( "node", n );
    			ExecutionResult result = engine.execute( "START n1=node({node}) MATCH n1-[:HAS_CURATOR]->x RETURN x ",params);
    			Iterator<Node> n_column = result.columnAs( "x" );
    			for ( Node node : IteratorUtil.asIterable( n_column ) )
     			{
    			    cur.add(node.getId())
    			}
    		}
    		
    	}
        return cur;	
    }

    private List getContributorsId (Iterable<Path> paths) {
    	Map<String, Object> params = new HashMap<String, Object>();
    	def con = [];
         if(!paths.iterator().hasNext()) {
    		return con;
    	}
        Path path = paths.iterator().next();
    	Iterator<Node> nodes = path.nodes().iterator();
     	while(nodes.hasNext()) {
    		Node n = nodes.next();
    		if(n.hasRelationship(RelTypes.HAS_CONTRIBUTOR)) {
    			params.put( "node", n );
    			ExecutionResult result = engine.execute( "START n1=node({node}) MATCH n1-[:HAS_CONTRIBUTOR]->x RETURN x ",params);
    			Iterator<Node> n_column = result.columnAs( "x" );
    			for ( Node node : IteratorUtil.asIterable( n_column ) )
    			{
    			    con.add(node.getId())
    			}
    		}
    		
    	}
        return con;	
    }

    private List isCuratorOfId( long nodeId ) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "nodeId", nodeId );
        def cur = [];
        ExecutionResult result = engine.execute( "START n1=node({nodeId}) MATCH n1-[:IS_CURATOR]->x RETURN x ", params);
        Iterator<Node> n_column = result.columnAs( "x" );
         for ( Node node : IteratorUtil.asIterable( n_column ) ) {
            cur.add(node.getId())
        }
        return cur;
    }

    private List isContributorOfId( long nodeId ) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "nodeId", nodeId );
        def con = [];
        ExecutionResult result = engine.execute( "START n1=node({nodeId}) MATCH n1-[:IS_CONTRIBUTOR]->x RETURN x ", params);
        Iterator<Node> n_column = result.columnAs( "x" );
        for ( Node node : IteratorUtil.asIterable( n_column ) ) {
            con.add(node.getId())
        }
        return con;
    }
    */
}
