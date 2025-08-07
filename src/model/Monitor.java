package model;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.IndexedIterable;

/**
*
* @author  Bingkun Zhao
* @data   06/8/2025
*/

public class Monitor {
	
    private Context<Agent> context;
    
    private int infectedCount;
    private int vaccinatedCount;
    private int recoveredCount;
    private int susceptibleCount;
    
    public Monitor(Context<Agent> context) {
        this.context = context;
    }
    
    
	@ScheduledMethod(start = 1, interval = 1, shuffle = true, priority = 1)
    public void collectData() {
		
        infectedCount = 0;
        vaccinatedCount = 0;
        recoveredCount = 0;
        susceptibleCount = 0;
        
        IndexedIterable<Agent> agents = context.getObjects(Agent.class);
        
        for (Object obj : agents) {
            if (obj instanceof Agent) {
                Agent agent = (Agent) obj;
                
                // 统计疾病状态
                switch (agent.getState()) {
                    case SUSCEPTIBLE:
                        susceptibleCount++;
                        break;
                    case INFECTED:
                        infectedCount++;
                        break;
                    case VACCINATED:
                        vaccinatedCount++;
                        break;
                    case RECOVERIED:
                        recoveredCount++;
                        break;
                }
            }
        }
	}
	
	public int getInfectedCount() {
		return infectedCount;
	}

	public void setInfectedCount(int infectedCount) {
		this.infectedCount = infectedCount;
	}

	public int getVaccinatedCount() {
		return vaccinatedCount;
	}

	public void setVaccinatedCount(int vaccinatedCount) {
		this.vaccinatedCount = vaccinatedCount;
	}

	public int getRecoveredCount() {
		return recoveredCount;
	}

	public void setRecoveredCount(int recoveredCount) {
		this.recoveredCount = recoveredCount;
	}

	public int getSusceptibleCount() {
		return susceptibleCount;
	}

	public void setSusceptibleCount(int susceptibleCount) {
		this.susceptibleCount = susceptibleCount;
	}
	
}
