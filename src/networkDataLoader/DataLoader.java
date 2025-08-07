/* Copyright 2025 Bingkun Zhao & Zhongkui Ma. All rights reserved.*/
package networkDataLoader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Network Data Loader - Reads and processes network data from text files
 * 
 * This class is responsible for loading network topology data from edge list files
 * and providing utilities for network analysis and processing.
 * 
 * @author Zhongkui Ma - Original Author
 * @author Bingkun Zhao - Enhanced node counting accuracy, Fixed node counting logic and added debugging features
 * @version 2.0
 * @lastModifiedTime 07/8/2025 
 */
public class DataLoader {

    /** The edge map of the network. */
    private HashMap<Integer, Set<Integer>> edgeMap;

    /** The actual amount of nodes of the network. */
    private int numNodes;

    /**
     * Construct a data loader with a name of data file.
     *
     * @param filename the name of the data file.
     * @throws RuntimeException if file reading fails
     */
    public DataLoader(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        // Load edge data from file
        edgeMap = readEdgeMap(filename);
        
        // Calculate the number of nodes needed for the network
        numNodes = calculateNumNodes(edgeMap);
    }

    /**
     * Return the edge map that represents the network structure.
     *
     * The returned HashMap contains:
     * - Keys: Source node IDs
     * - Values: Sets of target node IDs (outgoing connections)
     *
     * @return the edgeMap representing network connections
     */
    public HashMap<Integer, Set<Integer>> getEdgeMap() { 
        return edgeMap; 
    }

    /**
     * Return the amount of nodes in the network.
     *
     * @return the numNodes
     */
    public int getNumNodes() { 
        return numNodes; 
    }

    /**
     * Read and parse network edge data from a file.
     *
     * This method processes a text file containing edge list data and
     * constructs a HashMap representation of the network.
     *
     * Processing features:
     * - Skips empty lines and comments (lines starting with #)
     * - Handles whitespace-separated node IDs
     * - Skips self-loop edges (source == target)
     * - Provides detailed loading statistics
     *
     * @param  fileName the path and name of the file containing connection data
     * @return          A HashMap representing the network connections
     * @throws RuntimeException if file reading fails or file cannot be accessed
     * @Modified by Bingkun Zhao, 07/8/2025
     */
    public HashMap<Integer, Set<Integer>> readEdgeMap(String fileName) {
        System.out.println("Reading network data from file: " + fileName);
        long startTime = System.currentTimeMillis();
        
        HashMap<Integer, Set<Integer>> edgeMap = new HashMap<>();
        int lineCount = 0;  	// Total lines processed
        int edgeCount = 0;		// Valid edges loaded

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new BufferedInputStream(new FileInputStream(new File(fileName)))))) {

            String line;
            // Process each line in the file
            while ((line = reader.readLine()) != null) {
                lineCount++;
                
                // Skip empty lines and comments (lines starting with #)
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\\s+"); // Split by any whitespace
                if (parts.length < 2) {
                    System.err.println("Warning: Invalid line " + lineCount + ": " + line);
                    continue;
                }

                try {
                	 // Parse source and target node IDs
                    int source = Integer.parseInt(parts[0]);
                    int target = Integer.parseInt(parts[1]);

                    // Skip self-loops
                    if (source == target) {
                        continue;
                    }

                    // Add edge: source -> target
                    edgeMap.computeIfAbsent(source, k -> new HashSet<>()).add(target);
                    edgeCount++;

                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid number format on line " + lineCount + ": " + line);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + fileName);
            throw new RuntimeException("Failed to read network data file", e);
        }

        // Display loading statistics
        System.out.println("Network data loaded successfully:");
        System.out.println("    Lines processed: " + lineCount);
        System.out.println("    Edges loaded: " + edgeCount);
        System.out.println("    Unique nodes: " + calculateNumNodes(edgeMap));
        System.out.println("    Time taken: " + (System.currentTimeMillis() - startTime) + "ms");
        
        return edgeMap;
    }

    /**
     * Calculate the number of nodes needed to accommodate all node IDs in the network.
     * 
     * This method determines the minimum number of nodes required by finding
     * the maximum node ID and adding 1 (assuming 0-based indexing).
     * This approach ensures that all node IDs in the data can be properly mapped,
     * even if node IDs are not continuous or don't start from 0.
     *
     * @param  map the edge map containing network connections
     * @return     the number of nodes needed (maxNodeId + 1)
     */
    private int calculateNumNodes(HashMap<Integer, Set<Integer>> map) {
        if (map == null || map.isEmpty()) {
            return 0;
        }

        // Find the maximum node ID in the entire network
        int maxNodeId = -1;
        
        // Check all source node IDs
        for (Integer sourceId : map.keySet()) {
            if (sourceId != null && sourceId > maxNodeId) {
                maxNodeId = sourceId;
            }
        }

        // Check all target node IDs
        for (Set<Integer> targetIds : map.values()) {
            if (targetIds != null) {
                for (Integer targetId : targetIds) {
                    if (targetId != null && targetId > maxNodeId) {
                        maxNodeId = targetId;
                    }
                }
            }
        }

        // Return the number of nodes needed (max ID + 1 for 0-based indexing)
        return maxNodeId >= 0 ? maxNodeId + 1 : 0;
    }

    /**
     * Get the range of node IDs present in the network data.
     * 
     * This utility method returns the minimum and maximum node IDs found
     * in the network data, which is useful for debugging and validation.
     *
     * @return int array where [0] = minimum node ID, [1] = maximum node ID
     */
    public int[] getNodeIdRange() {
        if (edgeMap == null || edgeMap.isEmpty()) {
            return new int[]{0, -1};
        }

        int minNodeId = Integer.MAX_VALUE;
        int maxNodeId = -1;
        
        for (Integer sourceId : edgeMap.keySet()) {
            if (sourceId != null) {
                minNodeId = Math.min(minNodeId, sourceId);
                maxNodeId = Math.max(maxNodeId, sourceId);
            }
        }

        for (Set<Integer> targetIds : edgeMap.values()) {
            if (targetIds != null) {
                for (Integer targetId : targetIds) {
                    if (targetId != null) {
                        minNodeId = Math.min(minNodeId, targetId);
                        maxNodeId = Math.max(maxNodeId, targetId);
                    }
                }
            }
        }

        return new int[]{minNodeId, maxNodeId};
    }

    /**
     * Generate network statistics summary.
     * 
     * Provides a formatted string containing key network metrics including
     * node count, edge count, and average node degree.
     *
     * @return formatted string with network statistics
     */
    public String getNetworkStatistics() {
        if (edgeMap == null) {
            return "No network data loaded";
        }

        int totalEdges = 0;
        for (Set<Integer> targets : edgeMap.values()) {
            if (targets != null) {
                totalEdges += targets.size();
            }
        }

        return String.format("Network Statistics:\n" +
                           "  Nodes: %d\n" +
                           "  Edges: %d\n" +
                           "  Average degree: %.2f",
                           numNodes, totalEdges, 
                           numNodes > 0 ? (double) totalEdges / numNodes : 0.0);
    }
    
 
}