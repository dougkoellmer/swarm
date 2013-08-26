package swarm.client.states.code;

import swarm.client.managers.smCellCodeManager;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smUserManager;
import swarm.client.app.sm_c;
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
import swarm.shared.statemachine.smA_ActionArgs;
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
	public static class Save extends smA_Action
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			performCommitOrPreview(this);
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			return isCommitOrPreviewPerformable(false);
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_EditingCode.class;
		}
	}
	
	public static class CodeChanged extends smA_Action 
	{
		public static class Args extends smA_ActionArgs
		{
			private String m_changedCode = null;
			
			public void setChangedCode(String code)
			{
				m_changedCode = code;
			}
		}
		
		@Override
		public void perform(smA_ActionArgs args)
		{
			String code = ((Args) args).m_changedCode;
			
			State_ViewingCell viewingState = (State_ViewingCell) smA_State.getForegroundedInstance(State_ViewingCell.class);
			smBufferCell viewedCell = viewingState.getCell();
			
			smA_ClientUser user = sm_c.userMngr.getUser();
			
			user.onSourceCodeChanged(viewedCell.getCoordinate(), code);
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			State_ViewingCell state = (State_ViewingCell) smA_State.getForegroundedInstance(State_ViewingCell.class);
			
			if( state == null )
			{
				return false;
			}

			//--- DRK > Just to make extra sure.
			smA_ClientUser user = sm_c.userMngr.getUser();
			if( !user.isEditable(state.getCell().getCoordinate()))
			{
				return false;
			}
			
			return true;
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_EditingCode.class;
		}
	}
	
	public static class Preview extends smA_Action 
	{
		@Override
		public void perform(smA_ActionArgs args)
		{
			performCommitOrPreview(this);
		}
		
		@Override
		public boolean isPerformable(smA_ActionArgs args)
		{
			return isCommitOrPreviewPerformable(true);
		}
		
		@Override
		public Class<? extends smA_State> getStateAssociation()
		{
			return State_EditingCode.class;
		}
	}
	
	
	private static void performCommitOrPreview(smA_Action thisArg)
	{
		State_ViewingCell viewingState = smA_State.getForegroundedInstance(State_ViewingCell.class);
		smBufferCell viewedCell = viewingState.getCell();
		smGridCoordinate coord = viewedCell.getCoordinate();
		
		smA_ClientUser user = sm_c.userMngr.getUser();
		smCode sourceCode = user.getCode(coord, smE_CodeType.SOURCE);
		
		smCompilerResult compilerResult = sm_c.codeCompiler.compile(sourceCode, viewedCell.getCodePrivileges(), /*namespace=*/null);
		
		if( compilerResult.getStatus() == smE_CompilationStatus.NO_ERROR )
		{
			if( thisArg instanceof Save )
			{
				smCellCodeManager.getInstance().syncCell(viewedCell, sourceCode);
				//((StateMachine_EditingCode) thisArg.getState().getParent()).setBlockerReason(State_EditingCodeBlocker.Reason.SYNCING);
			}
			else if( thisArg instanceof Preview )
			{
				smCellCodeManager.getInstance().previewCell(viewedCell, sourceCode);
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
	
	private static boolean isCommitOrPreviewPerformable(boolean isPreview)
	{
		State_ViewingCell state = (State_ViewingCell) smA_State.getForegroundedInstance(State_ViewingCell.class);
		
		if( state == null )
		{
			return false;
		}
		
		smA_ClientUser user = sm_c.userMngr.getUser();
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
		smClientAccountManager.E_WaitReason waitReason = sm_c.accountMngr.getWaitReason();
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
	
	public State_EditingCode()
	{
		smA_Action.register(new Save());
		smA_Action.register(new CodeChanged());
		smA_Action.register(new Preview());
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