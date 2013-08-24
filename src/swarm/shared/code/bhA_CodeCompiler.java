package swarm.shared.code;

import java.util.logging.Logger;

import swarm.shared.app.bhS_App;
import swarm.shared.entities.bhE_CharacterQuota;
import swarm.shared.structs.bhCode;
import swarm.shared.structs.bhCodePrivileges;

public abstract class bhA_CodeCompiler
{
	private static final Logger s_logger = Logger.getLogger(bhA_CodeCompiler.class.getName());
	
	protected bhA_CodeCompiler()
	{
	}
	
	protected abstract bhCompilerResult createResult();
	
	public bhCompilerResult compile(bhCode sourceCode, bhCodePrivileges privileges, String namespace /*?????*/)
	{
		bhCompilerResult result = this.createResult();
		
		if( sourceCode.isEmpty() )
		{
			return result.onSuccess();
		}
		
		if( privileges.getCharacterQuota() != bhE_CharacterQuota.UNLIMITED )
		if( sourceCode.getRawCodeLength() > privileges.getCharacterQuota().getMaxCharacters() )
		{
			return result.onFailure(bhE_CompilationStatus.TOO_LONG);
		}
		
		return result.onSuccess();
	}
}
