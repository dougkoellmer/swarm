package swarm.client.view.tabs.code;

import swarm.client.app.smAppContext;
import swarm.client.entities.smBufferCell;
import swarm.client.entities.smCamera;
import swarm.client.entities.smA_ClientUser;
import swarm.client.managers.smClientAccountManager;
import swarm.client.managers.smUserManager;
import swarm.client.navigation.smBrowserNavigator;
import swarm.client.states.camera.Action_Camera_SetCameraTarget;
import swarm.client.states.camera.Action_Camera_SnapToAddress;
import swarm.client.states.camera.Action_Camera_SnapToCoordinate;
import swarm.client.states.camera.Action_ViewingCell_Refresh;
import swarm.client.states.camera.StateMachine_Camera;
import swarm.client.states.camera.State_ViewingCell;
import swarm.client.structs.smAccountInfo;
import swarm.client.view.cell.smAlertManager;
import swarm.shared.debugging.smU_Debug;
import swarm.shared.entities.smA_Grid;
import swarm.shared.statemachine.smA_Action;
import swarm.shared.statemachine.smA_State;
import swarm.shared.structs.smCellAddress;
import swarm.shared.structs.smE_CellAddressParseError;
import swarm.shared.structs.smGridCoordinate;
import swarm.shared.structs.smPoint;


import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class smU_CellApi
{
	private static final Action_Camera_SnapToCoordinate.Args m_snapCoordArgs = new Action_Camera_SnapToCoordinate.Args();
	private static final Action_Camera_SnapToAddress.Args m_snapAddressArgs = new Action_Camera_SnapToAddress.Args();
	private static final Action_Camera_SetCameraTarget.Args m_snapPointArgs = new Action_Camera_SetCameraTarget.Args();
	
	
	static native void registerApi(String namespace)
	/*-{
		
		function isInt(value)
		{ 
		    return !isNaN(parseInt(value,10)) && (parseFloat(value,10) == parseInt(value,10)); 
		}
		
		function isString(value)
		{
			return (typeof value) == "string";
		}
		
		function isObject(value)
		{
			return (typeof value) == "object";
		}
		
		function isNumeric(value)
		{
			return !isNaN(parseFloat(value)) && isFinite(value);
		}
		
		function isCoordinate()
		{
			if( arguments.length == 1 )
			{
				if( isObject(arguments[0]) )
				{
					return isInt(arguments[0].m) && isInt(arguments[0].n);
				}
			}
			else if( arguments.length == 2 && isInt(arguments[0]) && isInt(arguments[1]) )
			{
				return true;
			}
			
			return false;
		}
		
		function isPoint()
		{
			if( arguments.length == 1 )
			{
				if( isObject(arguments[0]) )
				{
					if( isNumeric(arguments[0].x) && isNumeric(arguments[0].y) )
					{
						if( ('z' in arguments[0]) == false )
						{
							arguments[0].z = 0.0;
							
							return true;
						}
						else
						{
							return isNumeric(arguments[0].z);
						}
					}
				}
			}
			else if( arguments.length == 2 && isNumeric(arguments[0]) && isNumeric(arguments[1]) )
			{
				return true;
			}
			else if( arguments.length == 3 && isNumeric(arguments[0]) && isNumeric(arguments[1]) && (isNumeric(arguments[2]) || arguments[2] === undefined) )
			{
				return true;
			}
			
			return false;
		}
		
		function string_contract(string)
		{
			if( arguments.length == 1 && isString(string) )
			{
				return true;
			}
			
			@swarm.client.view.tabs.code.smU_CellApi::logError(Ljava/lang/String;)("Expected string value.");
			return false;
		}
		
		$wnd[namespace] = 
		{
			snap:function()
			{
				var argumentsLength = arguments.length;
				
				if( argumentsLength == 1 )
				{
					if( isString(arguments[0]) )
					{
						@swarm.client.view.tabs.code.smU_CellApi::snapToAddress(Ljava/lang/String;)(arguments[0]);
					}
					else if( isInt(arguments[0]) )
					{
						@swarm.client.view.tabs.code.smU_CellApi::snapToHistory(D)(arguments[0]);
					}
					else if( isCoordinate(arguments[0]) )
					{
						@swarm.client.view.tabs.code.smU_CellApi::snapToCoordinate(DD)(arguments[0].m, arguments[0].n);
					}
					else if( isPoint(arguments[0]) )
					{
						@swarm.client.view.tabs.code.smU_CellApi::snapToPoint(DDD)(arguments[0].x, arguments[0].y, arguments[0].z);
					}
				}
				else if( argumentsLength == 2 )
				{
					var args_modified = [arguments[0], arguments[1]];
					if( isCoordinate.apply(null, args_modified) )
					{
						@swarm.client.view.tabs.code.smU_CellApi::snapToCoordinate(DD)(arguments[0], arguments[1]);
					}
					else if( isPoint.apply(null, args_modified) )
					{
						@swarm.client.view.tabs.code.smU_CellApi::snapToPoint(DDD)(arguments[0], arguments[1], 0.0);
					}
				}
				else if( argumentsLength == 3 )
				{
					var args_modified = [arguments[0], arguments[1], arguments[2]];
					if( isPoint.apply(null, args_modified) )
					{
						@swarm.client.view.tabs.code.smU_CellApi::snapToPoint(DDD)(arguments[0], arguments[1], arguments[2]);
					}
				}
			},			
			getCoordinate:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var m = @swarm.client.view.tabs.code.smU_CellApi::getCoordinateM()();
				var n = @swarm.client.view.tabs.code.smU_CellApi::getCoordinateN()();
				
				var coord = {m:m, n:n, toString:function(){return "{m:"+this.m+", n:"+this.n+"}"}};
				
				return coord;
			},
			getAddress:function()
			{
				var address = @swarm.client.view.tabs.code.smU_CellApi::getAddress()();
				
				return address;
			},
			getPosition:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var x = @swarm.client.view.tabs.code.smU_CellApi::getPositionX()();
				var y = @swarm.client.view.tabs.code.smU_CellApi::getPositionY()();
				var z = @swarm.client.view.tabs.code.smU_CellApi::getPositionZ()();
				
				var point = {x:x, y:y, z:z};
				
				return point;
			},
			getUsername:function()
			{
				var username = @swarm.client.view.tabs.code.smU_CellApi::getUsername()();
				
				return username;
			},
			getGridWidth:function()
			{
				var width = @swarm.client.view.tabs.code.smU_CellApi::getGridWidth()();
				
				return width;
			},
			getGridHeight:function()
			{
				var height = @swarm.client.view.tabs.code.smU_CellApi::getGridHeight()();
				
				return height;
			}
		};
		
		$wnd[namespace+"_alert"] = function(message)
		{
			if( string_contract(message) )
			{
				@swarm.client.view.tabs.code.smU_CellApi::alert(Ljava/lang/String;)(message);
			}
		}
		
	}-*/;
	
	private static void snapToHistory(double delta)
	{
		smAppContext.navigator.getBrowserNavigator().go((int)delta);
	}
	
	private static void snapToAddress(String rawAddress)
	{
		if( rawAddress.equals("~") )
		{
			smA_Action.performAction(Action_ViewingCell_Refresh.class);
			
			return;
		}
		
		smCellAddress address = new smCellAddress(rawAddress);
		if( address.getParseError() != smE_CellAddressParseError.NO_ERROR )
		{
			logError("Invalid cell address format.");
			return;
		}
		
		m_snapAddressArgs.init(address);
		smA_Action.performAction(Action_Camera_SnapToAddress.class, m_snapAddressArgs);
	}
	
	private static void alert(String message)
	{
		smAlertManager.getInstance().queue(message);
	}
	
	private static void snapToPoint(double x, double y, double z)
	{
		smPoint point = new smPoint(x, y, z);

		m_snapPointArgs.init(point, false);
		smA_Action.performAction(Action_Camera_SetCameraTarget.class, m_snapPointArgs);
	}
	
	/*private static void snapToRelativePoint(double x, double y, double z)
	{
		smPoint point = sm_c.cameraMngr.getCamera().getPosition();
		
		snapToPoint(point.getX() + x, point.getY() + y, point.getZ() + z);
	}*/
	
	private static void snapToCoordinate(double m, double n)
	{
		smGridCoordinate coordinate = new smGridCoordinate((int)m, (int)n);
		
		smA_Grid grid = smAppContext.gridMngr.getGrid();
		
		if( !grid.isInBounds(coordinate) )
		{
			logError("Coordinates are out of bounds.");
			return;
		}
		
		m_snapCoordArgs.setCoordinate(coordinate);
		smA_Action.performAction(Action_Camera_SnapToCoordinate.class, m_snapCoordArgs);
	}
	
	private static void snapToRelativeCoordinate(double m, double n)
	{
		smBufferCell cell = getCurrentCell();
		snapToCoordinate(cell.getCoordinate().getM() + m, cell.getCoordinate().getN() + n);
	}
	
	private static int getCoordinateM()
	{
		smBufferCell cell = getCurrentCell();

		return cell.getCoordinate().getM();
	}
	
	private static int getCoordinateN()
	{
		smBufferCell cell = getCurrentCell();

		return cell.getCoordinate().getN();
	}
	
	private static smPoint getPosition()
	{
		StateMachine_Camera machine = smA_State.getEnteredState(StateMachine_Camera.class);
		
		return machine.getCamera().getPosition();
	}
	
	private static double getPositionX()
	{
		return getPosition().getX();
	}
	
	private static double getPositionY()
	{
		return getPosition().getY();
	}
	
	private static double getPositionZ()
	{
		return getPosition().getZ();
	}
	
	private static String getAddress()
	{
		return getCurrentCell().getCellAddress().getRawAddress();
	}
	
	private static String getUsername()
	{
		smClientAccountManager accountManager = smAppContext.accountMngr;
		
		smAccountInfo accountInfo = accountManager.getAccountInfo();
		
		if( accountInfo == null )
		{
			return null;
		}
		else
		{
			return accountInfo.get(smAccountInfo.Type.USERNAME);
		}
	}
	
	private static int getGridWidth()
	{
		return getCurrentCell().getGrid().getWidth();
	}
	
	private static int getGridHeight()
	{
		return getCurrentCell().getGrid().getHeight();
	}
	
	static State_ViewingCell getViewingState()
	{
		State_ViewingCell viewingState = smA_State.getEnteredState(State_ViewingCell.class);
		
		if( viewingState == null )
		{
			smU_Debug.ASSERT(false, "Expected to be in viewing state for caja api- or uri-related action.");
		}
		
		return viewingState;
	}
	
	static smBufferCell getCurrentCell()
	{
		return getViewingState().getCell();
	}
	
	private static native void logError(String message)
	/*-{
		console.log(message);
	}-*/;
}
