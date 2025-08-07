/* Copyright 2025 Bingkun Zhao & Zhongkui Ma. All rights reserved.*/
package networkCreator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;

import edu.uci.ics.jung.algorithms.util.Indexer;
import repast.simphony.context.space.graph.AbstractGenerator;
import repast.simphony.space.graph.Network;

/**
 * This generator is to generate a network according to a edge map.
 * <p>
 * References: repast.simphony.context.space.graph.WattsBetaSmallWorldGenerator
 * <p>
 * Last edited time: 07/8/2025
 *
 * @author  Zhongkui Ma (Original Author)
 * @author  Bingkun Zhao (Updater and Modifier)
 *
 * @version 2.0.0 (Modified version)
 *
 * @see     repast.simphony.context.space.graph.WattsBetaSmallWorldGenerator
 */
public class CustomizedNetworkGenerator<T> extends AbstractGenerator<T> {

	/** The edge map representing network connections: source node ID -> set of target node IDs */
    private HashMap<Integer, Set<Integer>> edgeMap;

    /** Whether the generated edges will be symmetrical (only affects directed networks) */
    private boolean isSymmetrical;

    private int numNodes;

    /**
     * Constructs the customized network generator.
     *
     * @param edgeMap     the edgeMap of edges of the network, where key is source node ID 
     *                    and value is set of connected target node IDs
     * @param numAgent    the expected number of agents/nodes in the network
     * @param symmetrical whether or not the generated edges will be symmetrical. 
     *                    This has no effect on a non-directed network. For directed networks,
     *                    if symmetrical is true, bidirectional edges will be created.
     */
    public CustomizedNetworkGenerator(HashMap<Integer, Set<Integer>> edgeMap, int numAgent,
        boolean symmetrical) {

        this.isSymmetrical = symmetrical;
        this.edgeMap = edgeMap;
        this.numNodes = numAgent;

    }

    /**
     * Generates a network from the provided edge map.
     * <p>
     * The undirected networks and the directed works use the same method, the differences depending
     * on the connection information of the data file. <br>
     * That is to say, the file should record the directed edge for generating a directed network.
     * <br>
     * If the network is symmetrical, there will be two directed edges with the opposite directions
     * between two nodes. But this situation is only for the directed network.
     * </p>
     * <p>
     * Modified by Bingkun Zhao: Fixed node count validation logic, added proper error handling,
     * improved null checking, and added edge counting for debugging purposes.
     * </p>
     *
     * @param network the network to be populated with edges
     * @return the populated network model
     * @throws IllegalArgumentException if the node count doesn't match expected size
     */
    @Override
    public Network<T> createNetwork(Network<T> network) {

        // Validate that the number of nodes matches the expected count
        if (numNodes != network.size()) {
            String errorMsg = "Node count mismatch: expected " + numNodes + 
                             " but found " + network.size();
            throw new IllegalArgumentException(errorMsg);
        }
        
        // Collect all nodes from the network into a set
        Set<T> set = new HashSet<>();
        for (T node : network.getNodes())
            set.add(node);
        
        // Create bidirectional mapping between node objects and their integer indices
        BidiMap<T, Integer> map = Indexer.create(set);
        boolean isDirected = network.isDirected();
        
        // Build the network by creating edges according to the edgeMap
        int edgesAdded = 0;
        for (Integer sourceIndex : edgeMap.keySet()) {
            T source = map.getKey(sourceIndex);
            Set<Integer> targetSet = edgeMap.get(sourceIndex);

            if (source == null) {
                System.err.println("Warning: Source node with index " + sourceIndex + " not found");
                continue;
            }

            for (Integer targetIndex : targetSet) {
                T target = map.getKey(targetIndex);
                
                // Skip if target node doesn't exist in the network
                if (target == null) {
                    System.err.println("Warning: Target node with index " + targetIndex + " not found");
                    continue;
                }
                
                // Add the edge from source to target
                if (source != null && target != null) {
                    network.addEdge(source, target);
                    edgesAdded++;
                    
                    // For directed symmetrical networks, also add the reverse edge
                    // This creates bidirectional connections in directed networks
                    if (isDirected && isSymmetrical) {
                        network.addEdge(target, source);
                        edgesAdded++;
                    }
                }
            }
        }
        
        // Output statistics for debugging purposes
        System.out.println("Created network with " + edgesAdded + " edges");
        return network;
    }
}
