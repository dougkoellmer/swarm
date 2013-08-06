package b33hive.client.states.code;

import b33hive.client.managers.bhCellCodeManager;
import b33hive.client.managers.bhClientAccountManager;
import b33hive.client.managers.bhUserManager;
import b33hive.client.app.bh_c;
import b33hive.client.code.bhCompilerErrorMessageGenerator;
import b33hive.client.entities.bhBufferCell;
import b33hive.client.entities.bhA_ClientUser;
import b33hive.client.entities.bhE_CodeStatus;
import b33hive.client.states.StateMachine_Base;
import b33hive.client.states.State_AsyncDialog;
import b33hive.client.states.State_GenericDialog;
import b33hive.client.states.camera.State_ViewingCell;
import b33hive.client.states.code.State_EditingCodeBlocker.Reason;
import b33hive.shared.code.bhA_CodeCompiler;
import b33hive.shared.code.bhCompilerResult;
import b33hive.shared.code.bhE_CompilationStatus;
import b33hive.shared.debugging.bhU_Debug;
import b33hive.shared.entities.bhE_CodeType;
import b33hive.shared.statemachine.bhA_Action;
import b33hive.shared.statemachine.bhA_ActionArgs;
import b33hive.shared.statemachine.bhA_StateConstructor;

import b33hive.shared.statemachine.bhA_State;
import b33hive.shared.structs.bhCode;
import b33hive.shared.structs.bhGridCoordinate;


/**
 * ...
 * @author 
 */
public class State_EditingCode extends bhA_State
{
	public static class Save extends bhA_Action
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			performCommitOrPreview(this);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			return isCommitOrPreviewPerformable(false);
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_EditingCode.class;
		}
	}
	
	public static class CodeChanged extends bhA_Action 
	{
		public static class Args extends bhA_ActionArgs
		{
			private String m_changedCode = null;
			
			public void setChangedCode(String code)
			{
				m_changedCode = code;
			}
		}
		
		@Override
		public void perform(bhA_ActionArgs args)
		{
			String code = ((Args) args).m_changedCode;
			
			State_ViewingCell viewingState = (State_ViewingCell) bhA_State.getForegroundedInstance(State_ViewingCell.class);
			bhBufferCell viewedCell = viewingState.getCell();
			
			bhA_ClientUser user = bh_c.userMngr.getUser();
			
			user.onSourceCodeChanged(viewedCell.getCoordinate(), code);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			State_ViewingCell state = (State_ViewingCell) bhA_State.getForegroundedInstance(State_ViewingCell.class);
			
			if( state == null )
			{
				return false;
			}

			//--- DRK > Just to make extra sure.
			bhA_ClientUser user = bh_c.userMngr.getUser();
			if( !user.isEditable(state.getCell().getCoordinate()))
			{
				return false;
			}
			
			return true;
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_EditingCode.class;
		}
	}
	
	public static class Preview extends bhA_Action 
	{
		@Override
		public void perform(bhA_ActionArgs args)
		{
			performCommitOrPreview(this);
		}
		
		@Override
		public boolean isPerformable(bhA_ActionArgs args)
		{
			return isCommitOrPreviewPerformable(true);
		}
		
		@Override
		public Class<? extends bhA_State> getStateAssociation()
		{
			return State_EditingCode.class;
		}
	}
	
	
	private static void performCommitOrPreview(bhA_Action thisArg)
	{
		State_ViewingCell viewingState = bhA_State.getForegroundedInstance(State_ViewingCell.class);
		bhBufferCell viewedCell = viewingState.getCell();
		bhGridCoordinate coord = viewedCell.getCoordinate();
		
		bhA_ClientUser user = bh_c.userMngr.getUser();
		bhCode sourceCode = user.getCode(coord, bhE_CodeType.SOURCE);
		
		bhCompilerResult compilerResult = bh_c.codeCompiler.compile(sourceCode, viewedCell.getCodePrivileges(), /*namespace=*/null);
		
		if( compilerResult.getStatus() == bhE_CompilationStatus.NO_ERROR )
		{
			if( thisArg instanceof Save )
			{
				bhCellCodeManager.getInstance().syncCell(viewedCell, sourceCode);
				//((StateMachine_EditingCode) thisArg.getState().getParent()).setBlockerReason(State_EditingCodeBlocker.Reason.SYNCING);
			}
			else if( thisArg instanceof Preview )
			{
				bhCellCodeManager.getInstance().previewCell(viewedCell, sourceCode);
				//((StateMachine_EditingCode) thisArg.getState().getParent()).setBlockerReason(State_EditingCodeBlocker.Reason.PREVIEWING);
			}
			else
			{
				bhU_Debug.ASSERT(false, "performCommitOrPreview");
			}
		}
		else
		{
			String title = "Compiler Error";
			String body = bhCompilerErrorMessageGenerator.getInstance().generate(compilerResult);
			
			StateMachine_Base baseController = bhA_State.getEnteredInstance(StateMachine_Base.class);
			
			State_GenericDialog.Constructor constructor = new State_GenericDialog.Constructor(title, body);
			
			baseController.queueAsyncDialog(State_AsyncDialog.class, constructor);
		}
	}
	
	private static boolean isCommitOrPreviewPerformable(boolean isPreview)
	{
		State_ViewingCell state = (State_ViewingCell) bhA_State.getForegroundedInstance(State_ViewingCell.class);
		
		if( state == null )
		{
			return false;
		}
		
		bhA_ClientUser user = bh_c.userMngr.getUser();
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
		bhClientAccountManager.E_WaitReason waitReason = bh_c.accountMngr.getWaitReason();
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
		bhA_Action.register(new Save());
		bhA_Action.register(new CodeChanged());
		bhA_Action.register(new Preview());
	}
	
	@Override
	protected void didEnter(bhA_StateConstructor constructor)
	{
		m_mostRecentBlockerReason = null;
	}
	
	@Override
	protected void didForeground(Class<? extends bhA_State> revealingState, Object[] argsFromRevealingState)
	{
		if( revealingState == State_EditingCodeBlocker.class )
		{
			Object arg1 = argsFromRevealingState[0];
			
			m_mostRecentBlockerReason = arg1 != null ? (Reason) arg1 : null;
		}
	}
	
	public bhCode getCode()
	{
		bhCode code = ((StateMachine_EditingCode)getParent()).getCode();
		return code != null ? code : new bhCode("", bhE_CodeType.SOURCE);
	}
	
	public State_EditingCodeBlocker.Reason getMostRecentBlockerReason()
	{
		return m_mostRecentBlockerReason;
	}
}