package com.b33hive.shared.code;

public enum bhE_CompilationStatus
{
	NO_ERROR,
	TOO_LONG,
	RESPONSE_ERROR, // only set on client if transaction itself fails, not on server.
	COMPILER_EXCEPTION, // for when the compiler itself breaks...shouldn't happen
	COMPILATION_ERRORS
}
