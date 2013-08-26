package swarm.shared.code;

import java.util.logging.Logger;

import swarm.shared.app.smS_App;
import swarm.shared.entities.smE_CharacterQuota;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smCodePrivileges;

public abstract class smA_CodeCompiler
{
	private static final Logger s_logger = Logger.getLogger(smA_CodeCompiler.class.getName());
	
	protected smA_CodeCompiler()
	{
	}
	
	protected abstract smCompilerResult createResult();
	
	public smCompilerResult compile(smCode sourceCode, smCodePrivileges privileges, String namespace /*?????*/)
	{
		smCompilerResult result = this.createResult();
		
		if( sourceCode.isEmpty() )
		{
			return result.onSuccess();
		}
		
		if( privileges.getCharacterQuota() != smE_CharacterQuota.UNLIMITED )
		if( sourceCode.getRawCodeLength() > privileges.getCharacterQuota().getMaxCharacters() )
		{
			return result.onFailure(smE_CompilationStatus.TOO_LONG);
		}
		
		return result.onSuccess();
	}
}
