package swarm.shared.code;

import java.util.ArrayList;
import java.util.List;

import swarm.shared.app.sm;
import swarm.shared.debugging.bhU_Debug;
import swarm.shared.entities.bhE_CodeType;
import swarm.shared.json.bhA_JsonEncodable;
import swarm.shared.json.bhE_JsonKey;
import swarm.shared.json.bhI_JsonArray;
import swarm.shared.json.bhI_JsonObject;
import swarm.shared.json.bhJsonHelper;
import swarm.shared.structs.bhCode;

public class bhCompilerResult extends bhA_JsonEncodable
{
	private bhCompilerCell m_codeCell = null;
	
	private bhE_CompilationStatus m_status;
	
	private ArrayList<bhCompilerMessage> m_compilerMessages = null;
	
	public bhCompilerResult()
	{
		m_status = null;
	}
	
	private void initCell()
	{
		m_codeCell = new bhCompilerCell();
	}
	
	public bhCode getStandInCode(bhE_CodeType type)
	{
		return m_codeCell.getStandInCode(type);
	}
	
	public bhCode getCode(bhE_CodeType eType)
	{
		return m_codeCell.getCode(eType);
	}
	
	public List<bhCompilerMessage> getMessages()
	{
		return m_compilerMessages;
	}
	
	public bhE_CompilationStatus getStatus()
	{
		return m_status;
	}
	
	public void addMessage(bhCompilerMessage compilerError)
	{
		m_compilerMessages = m_compilerMessages != null ? m_compilerMessages : new ArrayList<bhCompilerMessage>();
		m_compilerMessages.add(compilerError);
		
		if( compilerError.getLevel() == bhE_CompilerMessageLevel.ERROR )
		{
			m_status = bhE_CompilationStatus.COMPILATION_ERRORS;
		}
	}
	
	/**
	 * Should only be called by default compilers to indicate no immediate problems
	 * with length or format or whatever.  Shouldn't be used by actual compilers.
	 * 
	 * @return
	 */
	public bhCompilerResult onSuccess()
	{
		initCell();
		
		bhCode emptyCode = new bhCode((String)null, bhE_CodeType.values());
		
		this.m_codeCell.setCode(bhE_CodeType.SOURCE, null);
		
		this.m_status = bhE_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public bhCompilerResult onSuccess(bhCode splashAndStandInForCompiled)
	{
		initCell();
		
		this.m_codeCell.setCode(bhE_CodeType.SPLASH, splashAndStandInForCompiled);

		this.m_status = bhE_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public bhCompilerResult onSuccess(bhCode splash, bhCode compiled)
	{
		initCell();
		
		this.m_codeCell.setCode(bhE_CodeType.SPLASH, splash);
		this.m_codeCell.setCode(bhE_CodeType.COMPILED, compiled);

		this.m_status = bhE_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public bhCompilerResult onFailure(bhE_CompilationStatus error)
	{
		m_codeCell = null;
		m_compilerMessages = null;
		this.m_status = error;
		
		return this;
	}

	@Override
	public void writeJson(bhI_JsonObject json)
	{
		if( m_codeCell != null )
		{
			m_codeCell.writeJson(json);
		}
		
		if( m_compilerMessages != null )
		{
			sm.jsonFactory.getHelper().putList(json, bhE_JsonKey.compilationErrors, m_compilerMessages);
		}
		else
		{
			bhU_Debug.ASSERT(m_status != bhE_CompilationStatus.COMPILATION_ERRORS, "Expected compiler errors while writing result json.");
		}
		
		if( m_status == null )
		{
			bhU_Debug.ASSERT(false, "Response error of compilation result should never be null when writing to json.");
			
			m_status = bhE_CompilationStatus.COMPILER_EXCEPTION;
		}
		
		sm.jsonFactory.getHelper().putEnum(json, bhE_JsonKey.compilationStatusCode, m_status);
	}

	@Override
	public void readJson(bhI_JsonObject json)
	{
		initCell();
		
		m_codeCell.readJson(json);
		
		m_status = sm.jsonFactory.getHelper().getEnum(json, bhE_JsonKey.compilationStatusCode, bhE_CompilationStatus.values());
		
		bhI_JsonArray compilerMessageJsonArray = sm.jsonFactory.getHelper().getJsonArray(json, bhE_JsonKey.compilationErrors);
		
		if( compilerMessageJsonArray != null )
		{
			m_compilerMessages = new ArrayList<bhCompilerMessage>();
			
			for( int i = 0; i < compilerMessageJsonArray.getSize(); i++ )
			{
				bhI_JsonObject compilerErrorJson = compilerMessageJsonArray.getObject(i);
				bhCompilerMessage error = new bhCompilerMessage(compilerErrorJson);
				
				m_compilerMessages.add(error);
			}
		}
		else
		{
			m_compilerMessages = null;
			bhU_Debug.ASSERT(m_status != bhE_CompilationStatus.COMPILATION_ERRORS, "Expected compiler error messages.");
		}
	}
}
