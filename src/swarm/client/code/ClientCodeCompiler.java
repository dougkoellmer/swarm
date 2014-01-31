package swarm.client.code;

import swarm.shared.code.A_CodeCompiler;
import swarm.shared.code.CompilerResult;

public class ClientCodeCompiler extends A_CodeCompiler
{
	public ClientCodeCompiler()
	{
		super();
	}
	
	protected CompilerResult createResult()
	{
		return new CompilerResult();
	}
}
