/* Copyright 2025 Bingkun Zhao & Zhongkui Ma. All rights reserved.*/
package multiThreads;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import repast.simphony.engine.schedule.ScheduledMethod;

/**
 * AgentManager - Multi-threaded agent execution manager for simulation models
 * 
 * <p>This class provides a framework for executing agent methods concurrently
 * using a thread pool. It supports phased execution where different methods
 * are executed in different simulation phases based on annotated fields and methods.</p>
 * 
 * <p>Usage example:
 * <pre>
 * public class Agent {
 *     {@code @ThreadScheduledField(phase = 0)}
 *     private boolean isAction = true;
 *     
 *     {@code @ThreadScheduledMethod(phase = 0)}
 *     public void Action() { ... }
 * }
 * 
 * AgentManager<MyAgent> manager = new AgentManager<>(Agent.class, 100, 4);
 * </pre>
 * </p>
 * 
 * @param <T> the type of agents to manage
 *
 * @author Zhongkui Ma - Original 
 * @author Bingkun Zhao - Modified and improvements
 * 
 * @time Last update time: 07/8/2025
 */
public class AgentManager<T> {

    /** 
     * The collection of agents that will be executed in multi-threaded environment.
     * Uses ArrayList for fast iteration and random access.
     */
    private ArrayList<T> agents;

    /** 
     * Fields that control whether an agent should execute in a specific phase.
     * Array size determines maximum number of execution phases.
     * Each field corresponds to a phase and should be of boolean type.
     */
    private Field[] fields;

    /** 
     * Methods that agents will execute in different phases of the simulation.
     * Array size determines maximum number of execution phases.
     * Methods should be annotated with @ThreadScheduledMethod.
     */
    private Method[] methods;

    /** The total amount of thread. */
    private int numThread;

    /** The thread pool. */
    private ExecutorService threadPool;

    /**
     * Construct a agent manager.
     *
     * @param cl        the class of agent
     * @param numAgent  the amount of agents
     * @param numThread the amount of threads
     */
    public AgentManager(Class<T> cl, int numAgent, int numThread) {
    	
        this.numThread = numThread;
        agents = new ArrayList<>(numAgent);
        threadPool = Executors.newFixedThreadPool(numThread);
        
        methods = new Method[5];
        fields = new Field[5];
        obtainMethods(cl); // Get the thread-scheduled methods of agents.
        obtainFields(cl); // Get the thread-scheduled fields of agents.

    }

    /**
     * Add one agent to the agent manager.
     *
     * @param agent the agent to add
     * @throws IllegalArgumentException if agent is null
     */
    public void addAgent(T agent) {
    	
        if (agent != null) {
            agents.add(agent); 
        }
        
    } 
    
    

    /**
     * The action of agent manager in one round of the simulation model.
     *
     * @throws IllegalArgumentException if the field or method does not exit
     * @throws IllegalAccessException   if the field or method cannot be accessed
     */
    @ScheduledMethod(start = 1, interval = 1, priority = 1)
    public void step() throws IllegalArgumentException, IllegalAccessException { //
    	
    	
        if (agents.size() == 0) return; // If there is no agent, the manager does nothing.

        
        for (var i = 0; i < methods.length; i++) { // Loop different phase to run different methods.

        	// 只有当method和field都存在时才执行
            if (methods[i] == null || fields[i] == null) continue;
            
            // 为每个phase创建临时线程池
            threadPool = Executors.newFixedThreadPool(numThread);
            
            Iterator<T> agentIterator = agents.iterator();

            while (agentIterator.hasNext()) {

                T agent = agentIterator.next();
                fields[i].setAccessible(true);

                if (fields[i].getBoolean(agent)) {// If the agent need to execute the method.

                    AgentRunnable<T> runnable = new AgentRunnable<>(agent, methods[i]);
                    threadPool.submit(runnable);

                }

            }

            threadPool.shutdown(); // Shut down the thread pool.
            while (!threadPool.isTerminated()); // Until all agents have finished processing.

        }

    }

    /**
     * Obtain the thread scheduled fields of agents.
     *
     * @param cl the class of agents
     */
    private void obtainFields(Class<T> cl) {

        for (Field f : cl.getDeclaredFields()) {

            ThreadScheduledField anno = f.getAnnotation(ThreadScheduledField.class);

            if (anno != null) { 
            	int phase = anno.phase(); 
                if (phase >= 0 && phase < fields.length) {  // 添加边界检查
                    fields[phase] = f; 
                }
            }

        }

    }

    /**
     * Obtain the thread scheduled methods of agents
     *
     * @param cl the class of agents.
     */
    private void obtainMethods(Class<T> cl) {

        for (Method m : cl.getDeclaredMethods()) {

            ThreadScheduledMethod anno = m.getAnnotation(ThreadScheduledMethod.class);

            if (anno != null) { 
            	int phase = anno.phase(); 
                if (phase >= 0 && phase < methods.length) {  // 添加边界检查
                    m.setAccessible(true);  // 设置可访问
                    methods[phase] = m; 
                }
            }

        }

    }

    /**
     * The threads of agents.
     *
     * @param <V> the class of agent
     */
    private class AgentRunnable<V> implements Runnable {

        private V agent;

        private Method method;

        public AgentRunnable(V agent, Method method) {

            this.agent = agent;
            this.method = method;

        }

        /**
         * Execute one specific method of the agent.
         */
        @Override
        public void run() {

            try {

                method.invoke(agent);

            } catch (
                IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {

                e.printStackTrace();

            }

        }

    }
    
    /**
     * Shutdown the thread pool.
     */
    public void shutdown() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }

}
