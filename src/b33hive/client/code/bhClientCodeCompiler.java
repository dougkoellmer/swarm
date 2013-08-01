package com.b33hive.client.code;

import com.b33hive.shared.code.bhA_CodeCompiler;
import com.b33hive.shared.code.bhCompilerResult;

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
