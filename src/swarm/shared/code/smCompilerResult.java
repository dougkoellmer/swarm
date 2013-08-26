package swarm.shared.code;

import java.util.ArrayList;
import java.util.List;

import swarm.shared.app.sm;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.json.smA_JsonEncodable;
import swarm.shared.json.smE_JsonKey;
import swarm.shared.json.smI_JsonArray;
import swarm.shared.json.smI_JsonObject;
import swarm.shared.json.smJsonHelper;
import swarm.shared.structs.smCode;

public class smCompilerResult extends smA_JsonEncodable
{
	private smCompilerCell m_codeCell = null;
	
	private smE_CompilationStatus m_status;
	
	private ArrayList<smCompilerMessage> m_compilerMessages = null;
	
	public smCompilerResult()
	{
		m_status = null;
	}
	
	private void initCell()
	{
		m_codeCell = new smCompilerCell();
	}
	
	public smCode getStandInCode(smE_CodeType type)
	{
		return m_codeCell.getStandInCode(type);
	}
	
	public smCode getCode(smE_CodeType eType)
	{
		return m_codeCell.getCode(eType);
	}
	
	public List<smCompilerMessage> getMessages()
	{
		return m_compilerMessages;
	}
	
	public smE_CompilationStatus getStatus()
	{
		return m_status;
	}
	
	public void addMessage(smCompilerMessage compilerError)
	{
		m_compilerMessages = m_compilerMessages != null ? m_compilerMessages : new ArrayList<smCompilerMessage>();
		m_compilerMessages.add(compilerError);
		
		if( compilerError.getLevel() == smE_CompilerMessageLevel.ERROR )
		{
			m_status = smE_CompilationStatus.COMPILATION_ERRORS;
		}
	}
	
	/**
	 * Should only be called by default compilers to indicate no immediate problems
	 * with length or format or whatever.  Shouldn't be used by actual compilers.
	 * 
	 * @return
	 */
	public smCompilerResult onSuccess()
	{
		initCell();
		
		smCode emptyCode = new smCode((String)null, smE_CodeType.values());
		
		this.m_codeCell.setCode(smE_CodeType.SOURCE, null);
		
		this.m_status = smE_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public smCompilerResult onSuccess(smCode splashAndStandInForCompiled)
	{
		initCell();
		
		this.m_codeCell.setCode(smE_CodeType.SPLASH, splashAndStandInForCompiled);

		this.m_status = smE_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public smCompilerResult onSuccess(smCode splash, smCode compiled)
	{
		initCell();
		
		this.m_codeCell.setCode(smE_CodeType.SPLASH, splash);
		this.m_codeCell.setCode(smE_CodeType.COMPILED, compiled);

		this.m_status = smE_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public smCompilerResult onFailure(smE_CompilationStatus error)
	{
		m_codeCell = null;
		m_compilerMessages = null;
		this.m_status = error;
		
		return this;
	}

	@Override
	public void writeJson(smI_JsonObject json)
	{
		if( m_codeCell != null )
		{
			m_codeCell.writeJson(json);
		}
		
		if( m_compilerMessages != null )
		{
			sm.jsonFactory.getHelper().putList(json, smE_JsonKey.compilationErrors, m_compilerMessages);
		}
		else
		{
			smU_Debug.ASSERT(m_status != smE_CompilationStatus.COMPILATION_ERRORS, "Expected compiler errors while writing result json.");
		}
		
		if( m_status == null )
		{
			smU_Debug.ASSERT(false, "Response error of compilation result should never be null when writing to json.");
			
			m_status = smE_CompilationStatus.COMPILER_EXCEPTION;
		}
		
		sm.jsonFactory.getHelper().putEnum(json, smE_JsonKey.compilationStatusCode, m_status);
	}

	@Override
	public void readJson(smI_JsonObject json)
	{
		initCell();
		
		m_codeCell.readJson(json);
		
		m_status = sm.jsonFactory.getHelper().getEnum(json, smE_JsonKey.compilationStatusCode, smE_CompilationStatus.values());
		
		smI_JsonArray compilerMessageJsonArray = sm.jsonFactory.getHelper().getJsonArray(json, smE_JsonKey.compilationErrors);
		
		if( compilerMessageJsonArray != null )
		{
			m_compilerMessages = new ArrayList<smCompilerMessage>();
			
			for( int i = 0; i < compilerMessageJsonArray.getSize(); i++ )
			{
				smI_JsonObject compilerErrorJson = compilerMessageJsonArray.getObject(i);
				smCompilerMessage error = new smCompilerMessage(compilerErrorJson);
				
				m_compilerMessages.add(error);
			}
		}
		else
		{
			m_compilerMessages = null;
			smU_Debug.ASSERT(m_status != smE_CompilationStatus.COMPILATION_ERRORS, "Expected compiler error messages.");
		}
	}
}
