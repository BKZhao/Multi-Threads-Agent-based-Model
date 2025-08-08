/* Copyright 2025 Bingkun Zhao & Zhongkui Ma. All rights reserved.*/
package multiThreads;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * @time Last update time: 08/8/2025
 */
public class AgentManagerTest<T> {

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

    
    // 性能统计相关变量
    private Map<Integer, Long> phaseExecutionTimes = new HashMap<>(); // 阶段耗时: phase -> 毫秒
    private long totalExecutionTime; // 总耗时（毫秒）
  
    private int timer;
    
    
    // 扩展数据结构，存储总时间+各阶段时间的列表
    private static class PhaseStats {
        List<Long> totalTimes = new ArrayList<>();      // 总时间列表
        List<Long> phase0Times = new ArrayList<>();     // Phase 0时间列表
        List<Long> phase1Times = new ArrayList<>();     // Phase 1时间列表
    }
    private static Map<Integer, PhaseStats> threadPerformanceData = new HashMap<>();  // 线程数 -> 多次测试的总耗时列表
    
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
        
        threadPerformanceData.putIfAbsent(numThread, new PhaseStats());
        
        methods = new Method[5];
        fields = new Field[5];
        obtainMethods(cl); // Get the thread-scheduled methods of agents.
        obtainFields(cl); // Get the thread-scheduled fields of agents.

    }

    /**
     * 设置线程数（用于动态调整测试参数）
     */
    public void setNumThread(int numThread) {
        this.numThread = numThread;
        threadPerformanceData.putIfAbsent(numThread, new PhaseStats());
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
    	
    	timer++; 
    	
        if (agents.size() == 0) return; // If there is no agent, the manager does nothing.

        long totalStart = System.currentTimeMillis(); // 总计时开始
        phaseExecutionTimes.clear(); // 清空上一轮阶段数据
        
        for (var i = 0; i < methods.length; i++) { // Loop different phase to run different methods.

        	// 只有当method和field都存在时才执行
            if (methods[i] == null || fields[i] == null) continue;
            
            long phaseStart = System.currentTimeMillis(); // 阶段计时开始
            
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
            long phaseEnd = System.currentTimeMillis();
            phaseExecutionTimes.put(i, phaseEnd - phaseStart); // 记录阶段耗时
        }
        
        totalExecutionTime = System.currentTimeMillis() - totalStart;
        // 同时记录总时间、Phase0、Phase1的耗时到对应列表
        PhaseStats stats = threadPerformanceData.get(numThread);
        stats.totalTimes.add(totalExecutionTime);
        stats.phase0Times.add(phaseExecutionTimes.getOrDefault(0, 0L));  // 取Phase0时间（默认0）
        stats.phase1Times.add(phaseExecutionTimes.getOrDefault(1, 0L));  // 取Phase1时间（默认0）
       
        if (timer == 100) {
            printPerformanceStats();
            printAllThreadStats();
        }

    }
    
    /**
     * 打印当前线程数的性能统计结果
     */
    public void printPerformanceStats() {
        System.out.println("\n===== Thread Performance (Threads: " + numThread + ") =====");
        System.out.println("Total execution time: " + totalExecutionTime + "ms");
        System.out.println("Phase breakdown:");  // 新增阶段标题
        for (Map.Entry<Integer, Long> entry : phaseExecutionTimes.entrySet()) {
            System.out.println("  Phase " + entry.getKey() + " time: " + entry.getValue() + "ms");
        }
    }

    /**
     * 打印所有线程数的汇总对比（多次测试的平均值）
     */
    public static void printAllThreadStats() {
        System.out.println("\n===== All Thread Count Comparison =====");
        // 改动5：扩展表格列，包含Phase 0和Phase 1
        System.out.printf("%-12s | %-20s | %-20s | %-20s | %-20s%n",
                "Thread Count", "Total Avg Time (ms)", "Phase 0 Avg (ms)", "Phase 1 Avg (ms)", "Speedup vs 2 Threads");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        
        // 计算2线程的总时间平均值作为基准
        double baseTotalTime = threadPerformanceData.getOrDefault(2, new PhaseStats()).totalTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(1);

        // 按线程数升序输出
        List<Integer> sortedThreads = new ArrayList<>(threadPerformanceData.keySet());
        Collections.sort(sortedThreads);

        for (int threadCount : sortedThreads) {
            PhaseStats stats = threadPerformanceData.get(threadCount);
            if (stats.totalTimes.isEmpty()) continue;

            // 计算各阶段平均值
            double avgTotal = stats.totalTimes.stream().mapToLong(Long::longValue).average().getAsDouble();
            double avgPhase0 = stats.phase0Times.stream().mapToLong(Long::longValue).average().orElse(0);
            double avgPhase1 = stats.phase1Times.stream().mapToLong(Long::longValue).average().orElse(0);
            double speedup = baseTotalTime / avgTotal;

            // 格式化输出所有指标
            System.out.printf("%-12d | %-20.2f | %-20.2f | %-20.2f | %.2fx%n",
                    threadCount, avgTotal, avgPhase0, avgPhase1, speedup);
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
