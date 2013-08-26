package swarm.shared.json;

/*
 * All directly-related-to-application keys should be put here for reasons of key-collision, performance, and obfuscation/minification.
 * The latter is particularly relevant for batched GET requests because of the effective ~2000 character limit.
 * 
 *TODO: Should split this up into separate lists for separate layers of the application, at the very least into application agnostic and application specific.
 */
public enum smE_JsonKey implements smI_JsonKeySource
{
	serverVersion,
	requestArgs,
	requestPath,
	requestList,
	responseList,

	codeType,

	codeSource,
	codeSplash,
	codeCompiled,


	compilationStatusCode,
	compilationErrors,
	ownedCoordinates,
	lastPosition,
	responseError,

	signUpValidationErrors,
	signUpCredentials,
	captchaChallenge,

	signInValidationErrors,
	signInCredentials,

	pointComponents,

	coordComponents,

	rawCode,
	standInFlags,
	codeSafetyLevel,

	rawCellAddress,
	getCellAddressMappingError,
	getCellAddressError,

	accountInfo,

	gridWidth,
	gridHeight,
	gridCellWidth,
	gridCellHeight,
	gridCellPadding,

	bitArray,
	bitArrayLength,
	
	rememberMe,
	
	compilerErrorMessage,
	compilerErrorLevel,
	
	fileRange,
	
	editingPermission,
	
	characterQuota,
	networkPrivilege,
	accountId,
	
	platform,
	assertMessage,
	passwordChangeToken,
	
	createdUser;


	private final String m_compiledKey;

	private smE_JsonKey()
	{
		m_compiledKey = this.ordinal() + "";
	}

	public String getCompiledKey()
	{
		return m_compiledKey;
	}

	public String getVerboseKey()
	{
		return this.name();
	}
}
