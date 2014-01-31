package swarm.shared.code;

import java.util.logging.Logger;

import swarm.shared.app.S_CommonApp;
import swarm.shared.entities.E_CharacterQuota;
import swarm.shared.structs.Code;
import swarm.shared.structs.CodePrivileges;

public abstract class A_CodeCompiler
{
	private static final Logger s_logger = Logger.getLogger(A_CodeCompiler.class.getName());
	
	protected A_CodeCompiler()
	{
	}
	
	protected abstract CompilerResult createResult();
	
	public CompilerResult compile(Code sourceCode, CodePrivileges privileges, String cellNamespace, String apiNamespace)
	{
		CompilerResult result = this.createResult();
		
		if( sourceCode.isEmpty() )
		{
			return result.onSuccess();
		}
		
		if( privileges.getCharacterQuota() != E_CharacterQuota.UNLIMITED )
		if( sourceCode.getRawCodeLength() > privileges.getCharacterQuota().getMaxCharacters() )
		{
			return result.onFailure(E_CompilationStatus.TOO_LONG);
		}
		
		return result.onSuccess();
	}
}
