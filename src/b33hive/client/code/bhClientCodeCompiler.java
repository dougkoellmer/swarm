package b33hive.client.code;

import b33hive.shared.code.bhA_CodeCompiler;
import b33hive.shared.code.bhCompilerResult;

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
