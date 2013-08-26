package swarm.client.code;

import swarm.shared.code.smA_CodeCompiler;
import swarm.shared.code.smCompilerResult;

public class smClientCodeCompiler extends smA_CodeCompiler
{
	public smClientCodeCompiler()
	{
		super();
	}
	
	protected smCompilerResult createResult()
	{
		return new smCompilerResult();
	}
}
