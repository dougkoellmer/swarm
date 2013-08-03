package b33hive.shared.code;

import java.util.logging.Logger;

import b33hive.shared.app.bhS_App;
import b33hive.shared.entities.bhE_CharacterQuota;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhCodePrivileges;

public abstract class bhA_CodeCompiler
{
	private static final Logger s_logger = Logger.getLogger(bhA_CodeCompiler.class.getName());
	
	private static bhA_CodeCompiler s_instance = null;
	
	protected bhA_CodeCompiler()
	{
		s_instance = this;
	}
	
	public static bhA_CodeCompiler getInstance()
	{
		return s_instance;
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
