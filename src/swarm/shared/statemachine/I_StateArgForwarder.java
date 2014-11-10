package swarm.shared.statemachine;

/**
 * 
 * @author dougkoellmer
 */
public interface I_StateArgForwarder
{
	<T extends StateArgs> T getArgs();
	
	<T extends Object> T getArg(int index);

	<T extends Object> T getArg();
	
	<T extends Object> T getArg(Class<T> paramType);
	
	<T extends Object> T getArg(Class<T> paramType, int index);
}
