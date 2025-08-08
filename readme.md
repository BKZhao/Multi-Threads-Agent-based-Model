# 🧬 Multi-threads Agent-based Simulation Framework

## 📋 Project Introduction
This is a multi-threaded Agent-Based Model framework designed for simulating complex systems such as disease transmission and information diffusion in social networks. Developed using Repast Simphony, the model efficiently handles dynamic interactions in large-scale agent populations through parallel computing technology.

## ✨Key Advantages 
- Phase-based parallel execution of agent behaviors for improved efficiency
- Supports loading network data from external files for flexible topology construction
- Annotation-based behavior scheduling for simplified extension
- Thread-safe design ensuring consistency in multi-phase computations
## 🏗️Framework Structure 
```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
      'primaryColor': '#e6f7ff',
      'primaryTextColor': '#003a8c',
      'primaryBorderColor': '#1890ff',
      'lineColor': '#1890ff',
      'secondaryColor': '#fff7e6',
      'secondaryTextColor': '#ad4e00',
      'secondaryBorderColor': '#fa8c16',
      'tertiaryColor': '#f6ffed',
      'tertiaryTextColor': '#135200',
      'tertiaryBorderColor': '#52c41a',
      'background': '#ffffff'
    }
  }
}%%
flowchart TD
    A([Simulation Begins]) --> B[[Phase 0: Action 1]]
    B --> C[[Phase 1: Action 2]]
    C --> D[[Phase 2: Action 3]]
    D --> E[[Phase N: Action N]]
    E --> F([Simulation Ends])

    subgraph Parallel Execution by Thread Pool
        direction LR
        B -.->|Thread Pool| B1([Thread 1..M])
        C -.->|Thread Pool| C1([Thread 1..M])
        D -.->|Thread Pool| D1([Thread 1..M])
        E -.->|Thread Pool| E1([Thread 1..M])
    end

    style A fill:#1890ff,color:#fff,stroke:#096dd9
    style F fill:#52c41a,color:#fff,stroke:#389e0d
```
## 🧩Core Components
| Component Name               | Package             | Core Responsibility                                                                 |
|------------------------------|------------------------------|--------------------------------------------------------------------------------------|
| **Agent**                       | model                         | Represents individual entities in disease transmission simulation, achieves phase-based execution through annotation coordination with multi-thread scheduling.|
| **Monitor**                      | model                         | Tracks simulation progress in real-time, collects key metrics, provides interfaces to access these statistics. |
| **ModelContextBuilder**          | model                         | Acts as the model initialization entry point, coordinating component creation, configuration loading, and network setup. |
| **AgentManager**                 | multithread                   | Manages multi-threaded execution, controls phase-based scheduling of agent behaviors, and handles thread pool synchronization. |
| **@ThreadScheduledMethod**       | multithread                   | Marks methods to be executed in specific simulation phases (e.g., decision or interaction phases) for parallel processing. |
| **@ThreadScheduledField**        | multithread                   | Annotates boolean fields that control whether an agent participates in specific simulation phases. |
| **DataLoader**                   | networkDataLoader             | Loads and parses edge list files into structured edge maps, handles invalid entries/comments, and calculates total nodes and network statistics. |
| **CustomizedNetworkGenerator**   | networkCreator                | Generates network topology using parsed edge maps, creates edges between nodes in the Repast Simphony framework, and supports directed networks with optional symmetrical edges. |

### ⚙️ Phase Annotation System 
The annotation-driven phase control system enables declarative scheduling of agent behaviors in multi-phase simulations. The framework consists of two complementary annotations:

```java
// Field annotation: Controls agent participation in specific phases
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadScheduledField {
    int phase(); // Specifies the associated phase number
}

// Method annotation: Marks methods to be executed in specific phases
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadScheduledMethod {
    int phase(); // Specifies the execution phase number
}
```
### ⚙️ Phase Execution Engine (AgentManager)
The AgentManager coordinates phase execution, leveraging thread pools for parallelism while maintaining phase order:
```java
public class AgentManager<T> {

    private Field[] phaseFields; 
    private Method[] phaseMethods; 
    
    public void step() {
        for (int phase = 0; phase < MAX_PHASES; phase++) {
            if (phaseMethods[phase] == null) continue;
            
            ExecutorService pool = Executors.newFixedThreadPool(numThread);
            for (T agent : agents) {
                if (phaseFields[phase].getBoolean(agent)) {
                    pool.submit(new AgentRunnable(agent, phaseMethods[phase]));
                }
            }
            pool.shutdown();
            while (!pool.isTerminated()); 
        }
    }
}
```

### ⚙️ Agent Implementation
Agents define phase-specific behaviors using the annotation system, with boolean fields controlling participation:
```java
public class Agent {
    // Phase 0: 
    @ThreadScheduledField(phase = 0)
    private boolean shouldAction1 = true;
    
    // Phase 0: 
    @ThreadScheduledMethod(phase = 0)
    public void action1() {

    }
    
    // Phase 1: 
    @ThreadScheduledField(phase = 1)
    private boolean shouldAction2 = true;
    
    // Phase 1: 
    @ThreadScheduledMethod(phase = 1)
    public void action2() {

    }
    
    // Phase 2: 
    @ThreadScheduledField(phase = 2)
    private boolean shouldAction3 = true;
    
    // Phase 2: 
    @ThreadScheduledMethod(phase = 2)
    public void action3() {

    }
}
```
## 🌐 Customized Network Generator
The **CustomizedNetworkGenerator** is a core component for constructing network topologies from pre-defined edge lists, enabling flexible integration of external network data (e.g., social network edges, contact networks) into the simulation. It supports both directed and undirected networks, with optional symmetrical edges for directed structures.
### 💡Core Functionality
Generates a network by translating a structured edge map (source-target node relationships) into a Repast Simphony Network object. Key capabilities include:
- Validating node count consistency between the input edge map and simulation agents.
- Creating edges between agents based on the edge map, with support for directed/undirected relationships.
- Adding symmetrical bidirectional edges for directed networks (when enabled).
- Providing error handling for missing nodes and debugging statistics (e.g., total edges added).
### 💡Key Features
- **Edge Map Integration:** Accepts a HashMap<Integer, Set<Integer>> where keys are source node IDs and values are sets of target node IDs, directly translating external edge list data into agent connections.
- **Flexible Topologies:** Works with both directed (e.g., one-way interactions) and undirected (e.g., mutual connections) networks, determined by the Network object’s configuration.
- **Symmetry Control:** For directed networks, setting isSymmetrical = true automatically adds reverse edges (target → source) to create bidirectional relationships.
- **Validation & Debugging:** Checks for node count mismatches (critical for simulation consistency) and logs warnings for missing nodes, with runtime statistics on edges added.

### 💡Usage Workflow
To use **CustomizedNetworkGenerator** , follow this typical integration with DataLoader (which provides the edge map):
1. **Load Edge Data:** Use DataLoader to parse an edge list file into an edgeMap.
```java
DataLoader dataLoader = new DataLoader("data/network_edges.txt");
HashMap<Integer, Set<Integer>> edgeMap = dataLoader.getEdgeMap();
int numNodes = dataLoader.getNumNodes();
```
2. **Initialize the Generator:** Configure with the edge map, node count, and symmetry flag.
 ```java
// For a directed network with symmetrical edges
NetworkGenerator<Agent> netGenerator = new CustomizedNetworkGenerator<>(
    edgeMap, 
    numNodes, 
    true // Enable symmetrical edges
);
```
3. **Build the Network:** Integrate with Repast’s NetworkBuilder to attach the network to the simulation context.
 ```java
NetworkBuilder<Agent> netBuilder = new NetworkBuilder<>(
    "simulation-network", // Network ID
    context, // Repast context containing agents
    true // Set to true for directed networks
);
netBuilder.setGenerator(netGenerator);
Network<Agent> network = netBuilder.buildNetwork();
```
## 📥 Network Data Loader
The **DataLoader** class provides robust network data processing capabilities, efficiently reading and analyzing complex network topologies from edge list files. Designed for large-scale simulations, it offers comprehensive data validation, detailed statistics, and flexible analysis tools.
### 🧱Key Features
1. **Robust File Parsing：**
- Reads edge list files with whitespace-separated node IDs.
- Skips comments (lines starting with #) and empty lines to handle human-readable data files.
- Ignores self-loop edges (where sourceId == targetId) to avoid invalid network connections.
2. **Data Validation & Cleaning**
- Validates file paths to prevent null/empty input errors.
- Logs warnings for invalid lines (e.g., non-numeric IDs, incomplete entries) without stopping execution.
- Handles IO exceptions gracefully with descriptive error messages.
3. **Network Metrics Calculation**
- **calculateNumNodes():** Determines total nodes using the maximum node ID (ensures compatibility with non-contiguous IDs).
- **getNodeIdRange():** Returns min/max node IDs for debugging and validation.
- **getNetworkStatistics():** Provides summary metrics (node count, edge count, average degree).
  
### 🧱Usage Example
 ```java
// Load network data from file
DataLoader loader = new DataLoader("edges.txt");

// Get the edge map for network generation
HashMap<Integer, Set<Integer>> edgeMap = loader.getEdgeMap();

// Get network statistics
System.out.println(loader.getNetworkStatistics());

// Use with CustomizedNetworkGenerator
int nodeCount = loader.getNumNodes();
CustomizedNetworkGenerator<Agent> generator = 
    new CustomizedNetworkGenerator<>(edgeMap, nodeCount, false);
 ```

## 🔧Prerequisites
- Java JDK 11+ (with JAVA_HOME configured)
- Repast Simphony 2.9+ (download from [Repast Official Site](https://repast.github.io/))
- Maven 3.6+ (optional, for dependency management)

## 🙏 Acknowledgments  

- This model builds on the foundational work by [**Zhongkui Ma**](https://zhongkuima.github.io/), who developed the initial version in 2021. 
- In 2025, **Bingkun Zhao** led the update and further development, including enhancements to multi-threaded phase execution, improved network data handling, and expanded debugging capabilities. Subsequent updates and maintenance of the model will be managed by **Bingkun Zhao**. 

Copyright ©2021-2025 Zhongkui Ma & Bingkun Zhao. All rights reserved.  

For questions or collaboration inquiries, please contact:  
- Zhongkui Ma: zhongkuima0419@gmail.com
- Bingkun Zhao: zhaobingkun01@outlook.com  

Developed by Bingkun Zhao (2025).
Thanks to the Repast development team for simulation infrastructure.
