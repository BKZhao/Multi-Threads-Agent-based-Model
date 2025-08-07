/* Copyright 2025 Bingkun Zhao & Zhongkui Ma. All rights reserved.*/
package model;

import java.util.HashMap;
import java.util.Set;
import multiThreads.AgentManager;
import networkCreator.CustomizedNetworkGenerator;
import networkDataLoader.DataLoader;
import repast.simphony.context.Context;
import repast.simphony.context.Contexts;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.graph.NetworkGenerator;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;

public class ModelContextBuilder implements ContextBuilder<Object> {

    @Override
    public Context<Object> build(Context<Object> context) {

        // Get the parameters of the model.
        Parameters para = RunEnvironment.getInstance().getParameters();
        int numThread = para.getInteger("numThread");
        
        // Read the data of the network.
        DataLoader dataLoader = new DataLoader("./data/soc-Epinions1.txt");
        HashMap<Integer, Set<Integer>> edgeMap = dataLoader.getEdgeMap();
        int numAgent = dataLoader.getNumNodes();
        
        // Create the contexts of the model.
        Context<Agent> agentContext = Contexts.createContext(Agent.class, "agentContext");
        context.addSubContext(agentContext);
        // Create the objects of the model.
        AgentManager<Agent> agentManager = new AgentManager<>(Agent.class, numAgent, numThread);
        context.add(agentManager);

        
        Monitor monitor = new Monitor(agentContext);
        context.add(monitor);

        for (int j = 0; j < numAgent; j++) {

            Agent agent = new Agent(j);
            agentContext.add(agent);
            agentManager.addAgent(agent);

        }

        int numInfected = (int) (numAgent * 0.01);
        Iterable<Agent> agents = agentContext.getRandomObjects(Agent.class, numInfected);
        for (Agent agent : agents) {
        	agent.setState(State.INFECTED);
        }
        
        // Generate a network of agents.
        NetworkBuilder<Agent> netBuilder =
            new NetworkBuilder<>("agentNetwork", agentContext, false);
        NetworkGenerator<Agent> gen = new CustomizedNetworkGenerator<>(edgeMap, numAgent, false);
        netBuilder.setGenerator(gen);
        @SuppressWarnings("unused")
		Network<Agent> agentNetwork = netBuilder.buildNetwork();
        
        // Set the ending time of one simulation.
        RunEnvironment.getInstance().endAt((Integer) para.getValue("endTime"));
        return context;

    }

}