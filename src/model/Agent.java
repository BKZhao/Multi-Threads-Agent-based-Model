/* Copyright 2025 Bingkun Zhao & Zhongkui Ma. All rights reserved.*/
package model;
import java.util.ArrayList;
import java.util.List;
import multiThreads.ThreadScheduledField;
import multiThreads.ThreadScheduledMethod;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.graph.Network;
import repast.simphony.util.ContextUtils;

/**
 * Agent class simulating individual behavior in a disease transmission model.
 * Handles state transitions (susceptible, infected, recovered, vaccinated)
 * and decision-making processes related to vaccination and infection risk.
 * 
 * @author Bingkun Zhao
 * @date 06/8/2025
 */
public class Agent {

	private int ID;
	private State state;
	List<Agent> agentNeighbors;

	
    @ThreadScheduledField(phase = ThreadScheduledField.FIRST_PHASE)
    private boolean toDecideVaccine;

    @ThreadScheduledField(phase = ThreadScheduledField.SECOND_PHASE)
    private boolean toUpdateState;
	
	public Agent(int ID) {
		this.ID = ID;
		this.state = State.SUSCEPTIBLE;
		this.agentNeighbors = new ArrayList<>();
		
        this.toDecideVaccine = true; 
        this.toUpdateState = true;
	}
	
	
	
    @ThreadScheduledMethod(phase = ThreadScheduledMethod.FIRST_PHASE)
    public void decideVaccination() {

        if (state == State.SUSCEPTIBLE) {
            double vaccinatedProb = 0.1;
            if (Math.random() < vaccinatedProb) {
                setState(State.VACCINATED); 
                
                toDecideVaccine = false;
                toUpdateState = true;
            }
        }
    }
	
    
    @ThreadScheduledMethod(phase = ThreadScheduledMethod.SECOND_PHASE)
    public void updateDiseaseState() {

        double recoveryRate = 0.1;
        double vaccineWaningRate = 0.01;
        
        switch (this.state) {
            case INFECTED:
                if (Math.random() < recoveryRate) {
                    setState(State.RECOVERIED); 
                }
                break;
            case SUSCEPTIBLE:
                checkInfection(); // 检查是否被感染
                break;
            case VACCINATED:
                if (Math.random() < vaccineWaningRate) {
                    setState(State.SUSCEPTIBLE); 
                }
                break;
        }

        toUpdateState = true;
    }
    
	
	public void checkInfection() {

        
        Parameters params = RunEnvironment.getInstance().getParameters();
        double infectionRate = (Double) params.getValue("infectionRate");
        agentNeighbors = getAgentNeighbors();
        
        int infectedNeighbors = 0;
        for (Agent neighbor : agentNeighbors) {
            if (neighbor.getState() == State.INFECTED) {
                infectedNeighbors++;
            }
        }

        // Calculate actual infection probability: 1 - product of (1 - base rate) for each infected neighbor
        double actualInfectionRate =  1 - Math.pow(1 - infectionRate, infectedNeighbors);

        if (Math.random() < actualInfectionRate) {
        	state = State.INFECTED;
        }
        
        
	}


	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public List<Agent> getAgentNeighbors() {
	    @SuppressWarnings("unchecked")
	    Context<Object> context = ContextUtils.getContext(this);
	    @SuppressWarnings("unchecked")
	    Network<Object> network = (Network<Object>) context.getProjection("agentNetwork");

	    Iterable<Object> neighbors = network.getAdjacent(this);
	    List<Agent> agentNeighbors = new ArrayList<>();
	    
	    for (Object obj : neighbors) {
	        if (obj instanceof Agent) {
	            agentNeighbors.add((Agent) obj);
	        }
	    }
	    
		return agentNeighbors;
	}

	public void setAgentNeighbors(List<Agent> agentNeighbors) {
		this.agentNeighbors = agentNeighbors;
	}
	
	
}
