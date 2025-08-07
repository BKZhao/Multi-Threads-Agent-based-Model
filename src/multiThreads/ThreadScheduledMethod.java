
package multiThreads;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that should execute during specific phases 
 * in a multi-threaded agent-based model.
 * 
 * <p>This annotation is applied to methods in agent classes to specify which
 * phase the method should execute in. Methods annotated with this will be
 * automatically invoked during their designated phase when the agent manager
 * processes that phase.</p>
 * 
 * <p>Phases are executed in ascending numerical order (0, 1, 2, 3, ...).
 * Each agent can have multiple methods annotated for different phases,
 * enabling complex multi-step processing workflows.</p>
 * 
 * <p>Example usage:
 * <pre>
 * public class Agent {
 *     { @ThreadScheduledMethod(phase = 0)}
 *     public void action1() {
 *         
 *     }
 *     
 *     { @ThreadScheduledMethod(phase = 1)}
 *     public void action2() {
 *         
 *     }
 *     
 *     { @ThreadScheduledMethod(phase = 2)}
 *     public void action3() {
 *     
 *     }
 * }
 * </pre>
 * </p>
 * 
 * <p>Note: Methods annotated with { @ThreadScheduledMethod } should be
 * public and typically take no parameters. The execution of these methods
 * is controlled by the corresponding {@link ThreadScheduledField} in the
 * same agent class.</p>
 * 
 * @author Zhongkui Ma - Original author
 * @author Bingkun Zhao - Enhancements and improvements
 * @see ThreadScheduledField
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThreadScheduledMethod {

    /** 
     * Common phase constants for convenience and code readability.
     * Users can use any non-negative integer for phase numbers.
     */
    public static final int FIRST_PHASE = 0;
    
    public static final int SECOND_PHASE = 1;
    
    public static final int THIRD_PHASE = 2;
    
    public static final int FOURTH_PHASE = 3;
    
    public static final int FIFTH_PHASE = 4;
    
    /** 
     * Specifies the processing phase number for this method.
     * 
     * <p>The phase number determines when this method will be executed:
     * <ul>
     *   <li>Phases are executed in ascending numerical order (0, 1, 2, 3, ...)</li>
     *   <li>Methods execute only if the corresponding {@link ThreadScheduledField} 
     *       is {@code true} for the agent instance</li>
     *   <li>Multiple methods can be associated with the same phase</li>
     *   <li>An agent can have methods for multiple different phases</li>
     * </ul>
     * </p>
     * 
     * @return the phase number (must be non-negative)
     * @see #FIRST_PHASE
     * @see #SECOND_PHASE
     * @see #THIRD_PHASE
     * @see #FOURTH_PHASE
     * @see #FIFTH_PHASE
     */
    public int phase();

}
