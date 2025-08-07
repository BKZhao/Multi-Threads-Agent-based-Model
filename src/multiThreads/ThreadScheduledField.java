/* Copyright 2025 Bingkun Zhao & Zhongkui Ma. All rights reserved.*/
package multiThreads;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for controlling multi-thread execution phases in agent-based models.
 * 
 * <p>This annotation is applied to boolean fields in agent classes to determine
 * whether a specific agent should execute during a particular processing phase.
 * Each agent can have multiple fields annotated with different phase numbers,
 * allowing fine-grained control over execution flow.</p>
 * 
 * <p>Phases are executed in ascending numerical order (0, 1, 2, 3, ...).
 * During each phase, only agents with the corresponding field set to {@code true}
 * will execute their associated {@link ThreadScheduledMethod}.</p>
 * 
 * <p>Example usage:
 * <pre>
 * public class Agent {
 *     { @ThreadScheduledField(phase = 0)}
 *     private boolean isAction1 = true;
 *     
 *     { @ThreadScheduledField(phase = 1)}
 *     private boolean isAction2 = true;
 *     
 *     { @ThreadScheduledField(phase = 2)}
 *     private boolean isAction3 = true;
 *     
 *     ......
 * }
 * </pre>
 * </p>
 * 
 * @author Zhongkui Ma - Original author
 * @author Bingkun Zhao - Enhancements and improvements
 * Last update time: 07/8/2025
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadScheduledField {

    /** 
     * Common phase constants for convenience and code readability.
     * Users can use any non-negative integer for phase numbers.
     * These constants represent typical phase sequences in agent-based models.
     */
    public static final int FIRST_PHASE = 0;
    
    public static final int SECOND_PHASE = 1;
    
    public static final int THIRD_PHASE = 2;
    
    public static final int FOURTH_PHASE = 3;
    
    public static final int FIFTH_PHASE = 4;

    /** 
     * The processing phase.
     * Supports any non-negative integer.
     * Phases will be executed in ascending order.
     */
    public int phase();
}
