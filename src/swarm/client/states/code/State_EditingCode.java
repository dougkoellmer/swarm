package swarm.client.states.code;

import swarm.client.managers.CellCodeManager;
import swarm.client.managers.ClientAccountManager;
import swarm.client.managers.UserManager;
import swarm.client.app.AppContext;
import swarm.client.code.CompilerErrorMessageGenerator;
import swarm.client.entities.BufferCell;
import swarm.client.entities.A_ClientUser;
import swarm.client.entities.E_CodeStatus;
import swarm.client.states.StateMachine_Base;
import swarm.client.states.State_AsyncDialog;
import swarm.client.states.State_GenericDialog;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.states.code.State_EditingCodeBlocker.Reason;
import swarm.shared.code.A_CodeCompiler;
import swarm.shared.code.CompilerResult;
import swarm.shared.code.E_CompilationStatus;
import swarm.shared.debugging.U_Debug;
import swarm.shared.entities.E_CodeType;
import swarm.shared.statemachine.A_Action;
import swarm.shared.statemachine.A_State;
import swarm.shared.statemachine.StateArgs;
import swarm.shared.structs.Code;
import swarm.shared.structs.GridCoordinate;


/**
 * ...
 * @author 
 */
public class State_EditingCode extends A_State
{	
	private State_EditingCodeBlocker.Reason m_mostRecentBlockerReason = null;
	
	private final AppContext m_appContext;
	
	public State_EditingCode(AppContext appContext)
	{
		m_appContext = appContext;
		
		register(new Action_EditingCode_Save());
		register(new Action_EditingCode_Edit(m_appContext.userMngr));
		register(new Action_EditingCode_Preview());
	}
	
	void performCommitOrPreview(A_Action thisArg)
	{
		State_ViewingCell viewingState = getContext().getForegroundedState(State_ViewingCell.class);
		BufferCell viewedCell = viewingState.getCell();
		GridCoordinate coord = viewedCell.getCoordinate();
		
		A_ClientUser user = m_appContext.userMngr.getUser();
		Code sourceCode = user.getCode(coord, E_CodeType.SOURCE);
		
		CompilerResult compilerResult = m_appContext.codeCompiler.compile(sourceCode, viewedCell.getCodePrivileges(), /*cellNamespace=*/null, /*apiNamespace=*/null);
		
		if( compilerResult.getStatus() == E_CompilationStatus.NO_ERROR )
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
				U_Debug.ASSERT(false, "performCommitOrPreview");
			}
		}
		else
		{
			String title = "Compiler Error";
			String body = m_appContext.compilerErrorMsgGenerator.generate(compilerResult);
			
			StateMachine_Base baseController = getContext().getEnteredState(StateMachine_Base.class);
			
			State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor(title, body);
			
			baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
		}
	}
	
	boolean isCommitOrPreviewPerformable(boolean isPreview)
	{
		State_ViewingCell state = getContext().getForegroundedState(State_ViewingCell.class);
		
		if( state == null )
		{
			return false;
		}
		
		A_ClientUser user = m_appContext.userMngr.getUser();
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
		ClientAccountManager.E_WaitReason waitReason = m_appContext.accountMngr.getWaitReason();
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
	
	@Override
	protected void didEnter(StateArgs constructor)
	{
		m_mostRecentBlockerReason = null;
	}
	
	@Override
	protected void didForeground(Class<? extends A_State> revealingState, Object[] argsFromRevealingState)
	{
		if( revealingState == State_EditingCodeBlocker.class )
		{
			Object arg1 = argsFromRevealingState[0];
			
			m_mostRecentBlockerReason = arg1 != null ? (Reason) arg1 : null;
		}
	}
	
	public Code getCode()
	{
		Code code = ((StateMachine_EditingCode)getParent()).getCode();
		return code != null ? code : new Code("", E_CodeType.SOURCE);
	}
	
	public State_EditingCodeBlocker.Reason getMostRecentBlockerReason()
	{
		return m_mostRecentBlockerReason;
	}
}