package swarm.client.code;

import swarm.shared.code.bhA_CodeCompiler;
import swarm.shared.code.bhCompilerResult;

public class bhClientCodeCompiler extends bhA_CodeCompiler
{
	public bhClientCodeCompiler()
	{
		super();
	}
	
	protected bhCompilerResult createResult()
	{
		return new bhCompilerResult();
	}
}
