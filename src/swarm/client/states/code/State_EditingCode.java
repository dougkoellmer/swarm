package swarm.client.states.code;

import swarm.client.managers.smCellCodeManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smUserManager;
import swarm.client.app.smAppContext;
import swarm.client.code.smCompilerErrorMessageGenerator;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smA_ClientUser;
import swarm.client.entities.smE_CodeStatus;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.State_EditingCodeBlocker.Reason;
import swarm.shared.code.smA_CodeCompiler;
import swarm.shared.code.smCompilerResult;
import swarm.shared.code.smE_CompilationStatus;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smE_CodeType;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_StateConstructor;

import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCode;
import swarm.shared.structs.smGridCoordinate;


/**
 * ...
 * @author 
 */
public class State_EditingCode extends smA_State
{
	void performCommitOrPreview(smA_Action thisArg)
	{
		State_ViewingCell viewingState = smA_State.getForegroundedInstance(State_ViewingCell.class);
		smBufferCell viewedCell = viewingState.getCell();
		smGridCoordinate coord = viewedCell.getCoordinate();
		
		smA_ClientUser user = m_appContext.userMngr.getUser();
		smCode sourceCode = user.getCode(coord, smE_CodeType.SOURCE);
		
		smCompilerResult compilerResult = m_appContext.codeCompiler.compile(sourceCode, viewedCell.getCodePrivileges(), /*namespace=*/null);
		
		if( compilerResult.getStatus() == smE_CompilationStatus.NO_ERROR )
		{
			if( thisArg instanceof Action_EditingCode_Save )
			{
				m_appContext.codeMngr.syncCell(viewedCell, sourceCode);
				//((StateMachine_EditingCode) thisArg.getState().getParent()).setBlockerReason(State_EditingCodeBlocker.Reason.SYNCING);
			}
			else if( thisArg instanceof Action_EditingCode_Preview )
			{
				m_appContext.codeMngr.previewCell(viewedCell, sourceCode);
				//((StateMachine_EditingCode) thisArg.getState().getParent()).setBlockerReason(State_EditingCodeBlocker.Reason.PREVIEWING);
			}
			else
			{
				smU_Debug.ASSERT(false, "performCommitOrPreview");
			}
		}
		else
		{
			String title = "Compiler Error";
			String body = smCompilerErrorMessageGenerator.getInstance().generate(compilerResult);
			
			StateMachine_Base baseController = smA_State.getEnteredInstance(StateMachine_Base.class);
			
			State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor(title, body);
			
			baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
		}
	}
	
	boolean isCommitOrPreviewPerformable(boolean isPreview)
	{
		State_ViewingCell state = (State_ViewingCell) smA_State.getForegroundedInstance(State_ViewingCell.class);
		
		if( state == null )
		{
			return false;
		}
		
		smA_ClientUser user = m_appContext.userMngr.getUser();
		if( !user.isCellOwner(state.getCell().getCoordinate()) )
		{
			return false;
		}
		
		//--- DRK > Just to make extra sure.
		if( isPreview && !user.isSourceCodeChanged(state.getCell().getCoordinate()))
		{
			return false;
		}
		else if (!isPreview && !user.isSourceCodeChanged(state.getCell().getCoordinate()) )
		{
			return false;
		}
		
		//--- DRK > There are other mechanisms in place to prevent syncing/previewing from clashing with
		//---		account management transactions, but the more the merrier I say.
		smClientAccountManager.E_WaitReason waitReason = m_appContext.accountMngr.getWaitReason();
		switch(waitReason)
		{
			case SIGNING_IN:
			case SIGNING_UP:
			case SIGNING_OUT:
			case SETTING_NEW_PASSWORD:
			{
				return false;
			}
			
			case NONE:
		}
		
		return true;
	}
	
	private State_EditingCodeBlocker.Reason m_mostRecentBlockerReason = null;
	
	private final smAppContext m_appContext;
	
	public State_EditingCode(smAppContext appContext)
	{
		m_appContext = appContext;
		
		smA_Action.register(new Action_EditingCode_Save());
		smA_Action.register(new Action_EditingCode_Edit(m_appContext.userMngr));
		smA_Action.register(new Action_EditingCode_Preview());
	}
	
	@Override
	protected void didEnter(smA_StateConstructor constructor)
	{
		m_mostRecentBlockerReason = null;
	}
	
	@Override
	protected void didForeground(Class<? extends smA_State> revealingState, Object[] argsFromRevealingState)
	{
		if( revealingState == State_EditingCodeBlocker.class )
		{
			Object arg1 = argsFromRevealingState[0];
			
			m_mostRecentBlockerReason = arg1 != null ? (Reason) arg1 : null;
		}
	}
	
	public smCode getCode()
	{
		smCode code = ((StateMachine_EditingCode)getParent()).getCode();
		return code != null ? code : new smCode("", smE_CodeType.SOURCE);
	}
	
	public State_EditingCodeBlocker.Reason getMostRecentBlockerReason()
	{
		return m_mostRecentBlockerReason;
	}
}