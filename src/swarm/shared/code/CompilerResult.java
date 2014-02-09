package swarm.shared.code;

import java.util.ArrayList;
import java.util.List;

import swarm.shared.app.BaseAppContext;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeType;
import swarm.shared.json.A_JsonEncodable;
import swarm.shared.json.A_JsonFactory;
import swarm.shared.json.E_JsonKey;
import swarm.shared.json.I_JsonArray;
import swarm.shared.json.I_JsonObject;
import swarm.shared.json.JsonHelper;
import swarm.shared.structs.Code;

public class CompilerResult extends A_JsonEncodable
{
	private CompilerCell m_codeCell;
	
	private Code m_sourceCode = null;
	
	private E_CompilationStatus m_status;
	
	private ArrayList<CompilerMessage> m_compilerMessages = null;
	
	public CompilerResult()
	{
		m_status = null;
	}
	
	public CompilerResult(A_JsonFactory factory, I_JsonObject json)
	{
		super(factory, json);
	}
	
	public void setSource(Code code)
	{
		m_sourceCode = code;
	}
	
	private void initCell()
	{
		m_codeCell = new CompilerCell();
	}
	
	public Code getStandInCode(E_CodeType type)
	{
		return m_codeCell.getStandInCode(type);
	}
	
	public Code getCode(E_CodeType eType)
	{
		return eType == E_CodeType.SOURCE ? m_sourceCode : m_codeCell.getCode(eType);
	}
	
	public List<CompilerMessage> getMessages()
	{
		return m_compilerMessages;
	}
	
	public E_CompilationStatus getStatus()
	{
		return m_status;
	}
	
	public void addMessage(CompilerMessage compilerError)
	{
		m_compilerMessages = m_compilerMessages != null ? m_compilerMessages : new ArrayList<CompilerMessage>();
		m_compilerMessages.add(compilerError);
		
		if( compilerError.getLevel() == E_CompilerMessageLevel.ERROR )
		{
			m_status = E_CompilationStatus.COMPILATION_ERRORS;
		}
	}
	
	/**
	 * Should only be called by default compilers to indicate no immediate problems
	 * with length or format or whatever.  Shouldn't be used by actual compilers.
	 * 
	 * @return
	 */
	public CompilerResult onSuccess()
	{
		initCell();
		
		Code emptyCode = new Code((String)null, E_CodeType.values());
		
		this.m_codeCell.setCode(E_CodeType.SOURCE, null);
		
		this.m_status = E_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public CompilerResult onSuccess(Code splashAndStandInForCompiled)
	{
		initCell();
		
		this.m_codeCell.setCode(E_CodeType.SPLASH, splashAndStandInForCompiled);

		this.m_status = E_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public CompilerResult onSuccess(Code splash, Code compiled)
	{
		initCell();
		
		this.m_codeCell.setCode(E_CodeType.SPLASH, splash);
		this.m_codeCell.setCode(E_CodeType.COMPILED, compiled);

		this.m_status = E_CompilationStatus.NO_ERROR;
		
		return this;
	}
	
	public CompilerResult onFailure(E_CompilationStatus error)
	{
		m_codeCell = null;
		m_compilerMessages = null;
		this.m_status = error;
		
		return this;
	}

	@Override
	public void writeJson(I_JsonObject json_out, A_JsonFactory factory)
	{
		if( m_codeCell != null )
		{
			m_codeCell.writeJson(json_out, factory);
		}
		
		if( m_compilerMessages != null )
		{
			factory.getHelper().putList(factory, json_out, E_JsonKey.compilationErrors, m_compilerMessages);
		}
		else
		{
			U_Debug.ASSERT(m_status != E_CompilationStatus.COMPILATION_ERRORS, "Expected compiler errors while writing result json.");
		}
		
		if( m_status == null )
		{
			U_Debug.ASSERT(false, "Response error of compilation result should never be null when writing to json.");
			
			m_status = E_CompilationStatus.COMPILER_EXCEPTION;
		}
		
		factory.getHelper().putEnum(json_out, E_JsonKey.compilationStatusCode, m_status);
	}

	@Override
	public void readJson(I_JsonObject json, A_JsonFactory factory)
	{
		initCell();
		
		m_codeCell.readJson(json, factory);
		
		m_status = factory.getHelper().getEnum(json, E_JsonKey.compilationStatusCode, E_CompilationStatus.values());
		
		I_JsonArray compilerMessageJsonArray = factory.getHelper().getJsonArray(json, E_JsonKey.compilationErrors);
		
		if( compilerMessageJsonArray != null )
		{
			m_compilerMessages = new ArrayList<CompilerMessage>();
			
			for( int i = 0; i < compilerMessageJsonArray.getSize(); i++ )
			{
				I_JsonObject compilerErrorJson = compilerMessageJsonArray.getObject(i);
				CompilerMessage error = new CompilerMessage(factory, compilerErrorJson);
				
				m_compilerMessages.add(error);
			}
		}
		else
		{
			m_compilerMessages = null;
			U_Debug.ASSERT(m_status != E_CompilationStatus.COMPILATION_ERRORS, "Expected compiler error messages.");
		}
	}
}
