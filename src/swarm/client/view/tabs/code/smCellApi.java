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
import swarm.client.view.smViewContext;
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

public class smCellApi
{
	private static final Action_Camera_SnapToCoordinate.Args m_snapCoordArgs = new Action_Camera_SnapToCoordinate.Args();
	private static final Action_Camera_SnapToAddress.Args m_snapAddressArgs = new Action_Camera_SnapToAddress.Args();
	private static final Action_Camera_SetCameraTarget.Args m_snapPointArgs = new Action_Camera_SetCameraTarget.Args();
	
	private final smViewContext m_viewContext;
	
	public smCellApi(smViewContext viewContext)
	{
		m_viewContext = viewContext;
	}
	
	native void registerApi(String namespace)
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
			
			this.@swarm.client.view.tabs.code.smCellApi::logError(Ljava/lang/String;)("Expected string value.");
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
						this.@swarm.client.view.tabs.code.smCellApi::snapToAddress(Ljava/lang/String;)(arguments[0]);
					}
					else if( isInt(arguments[0]) )
					{
						this.@swarm.client.view.tabs.code.smCellApi::snapToHistory(D)(arguments[0]);
					}
					else if( isCoordinate(arguments[0]) )
					{
						this.@swarm.client.view.tabs.code.smCellApi::snapToCoordinate(DD)(arguments[0].m, arguments[0].n);
					}
					else if( isPoint(arguments[0]) )
					{
						this.@swarm.client.view.tabs.code.smCellApi::snapToPoint(DDD)(arguments[0].x, arguments[0].y, arguments[0].z);
					}
				}
				else if( argumentsLength == 2 )
				{
					var args_modified = [arguments[0], arguments[1]];
					if( isCoordinate.apply(null, args_modified) )
					{
						this.@swarm.client.view.tabs.code.smCellApi::snapToCoordinate(DD)(arguments[0], arguments[1]);
					}
					else if( isPoint.apply(null, args_modified) )
					{
						this.@swarm.client.view.tabs.code.smCellApi::snapToPoint(DDD)(arguments[0], arguments[1], 0.0);
					}
				}
				else if( argumentsLength == 3 )
				{
					var args_modified = [arguments[0], arguments[1], arguments[2]];
					if( isPoint.apply(null, args_modified) )
					{
						this.@swarm.client.view.tabs.code.smCellApi::snapToPoint(DDD)(arguments[0], arguments[1], arguments[2]);
					}
				}
			},			
			getCoordinate:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var m = this.@swarm.client.view.tabs.code.smCellApi::getCoordinateM()();
				var n = this.@swarm.client.view.tabs.code.smCellApi::getCoordinateN()();
				
				var coord = {m:m, n:n, toString:function(){return "{m:"+this.m+", n:"+this.n+"}"}};
				
				return coord;
			},
			getAddress:function()
			{
				var address = this.@swarm.client.view.tabs.code.smCellApi::getAddress()();
				
				return address;
			},
			getPosition:function()
			{
				//--- DRK > Have to do it like this because of weird jsni behavior with
				//---		passing js objects across the interface.
				var x = this.@swarm.client.view.tabs.code.smCellApi::getPositionX()();
				var y = this.@swarm.client.view.tabs.code.smCellApi::getPositionY()();
				var z = this.@swarm.client.view.tabs.code.smCellApi::getPositionZ()();
				
				var point = {x:x, y:y, z:z};
				
				return point;
			},
			getUsername:function()
			{
				var username = this.@swarm.client.view.tabs.code.smCellApi::getUsername()();
				
				return username;
			},
			getGridWidth:function()
			{
				var width = this.@swarm.client.view.tabs.code.smCellApi::getGridWidth()();
				
				return width;
			},
			getGridHeight:function()
			{
				var height = this.@swarm.client.view.tabs.code.smCellApi::getGridHeight()();
				
				return height;
			}
		};
		
		$wnd[namespace+"_alert"] = function(message)
		{
			if( string_contract(message) )
			{
				this.@swarm.client.view.tabs.code.smCellApi::alert(Ljava/lang/String;)(message);
			}
		}
		
	}-*/;
	
	private void snapToHistory(double delta)
	{
		m_viewContext.navigator.getBrowserNavigator().go((int)delta);
	}
	
	private void snapToAddress(String rawAddress)
	{
		if( rawAddress.equals("~") )
		{
			m_viewContext.stateContext.performAction(Action_ViewingCell_Refresh.class);
			
			return;
		}
		
		smCellAddress address = new smCellAddress(rawAddress);
		if( address.getParseError() != smE_CellAddressParseError.NO_ERROR )
		{
			logError("Invalid cell address format.");
			return;
		}
		
		m_snapAddressArgs.init(address);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToAddress.class, m_snapAddressArgs);
	}
	
	private void alert(String message)
	{
		smAlertManager.getInstance().queue(message);
	}
	
	private void snapToPoint(double x, double y, double z)
	{
		smPoint point = new smPoint(x, y, z);

		m_snapPointArgs.init(point, false);
		m_viewContext.stateContext.performAction(Action_Camera_SetCameraTarget.class, m_snapPointArgs);
	}
	
	/*private static void snapToRelativePoint(double x, double y, double z)
	{
		smPoint point = sm_c.cameraMngr.getCamera().getPosition();
		
		snapToPoint(point.getX() + x, point.getY() + y, point.getZ() + z);
	}*/
	
	private void snapToCoordinate(double m, double n)
	{
		smGridCoordinate coordinate = new smGridCoordinate((int)m, (int)n);
		
		smA_Grid grid = m_viewContext.appContext.gridMngr.getGrid();
		
		if( !grid.isInBounds(coordinate) )
		{
			logError("Coordinates are out of bounds.");
			return;
		}
		
		m_snapCoordArgs.setCoordinate(coordinate);
		m_viewContext.stateContext.performAction(Action_Camera_SnapToCoordinate.class, m_snapCoordArgs);
	}
	
	private void snapToRelativeCoordinate(double m, double n)
	{
		smBufferCell cell = getCurrentCell();
		snapToCoordinate(cell.getCoordinate().getM() + m, cell.getCoordinate().getN() + n);
	}
	
	private int getCoordinateM()
	{
		smBufferCell cell = getCurrentCell();

		return cell.getCoordinate().getM();
	}
	
	private int getCoordinateN()
	{
		smBufferCell cell = getCurrentCell();

		return cell.getCoordinate().getN();
	}
	
	private smPoint getPosition()
	{
		smCamera camera = m_viewContext.appContext.cameraMngr.getCamera();
		
		return camera.getPosition();
	}
	
	private double getPositionX()
	{
		return getPosition().getX();
	}
	
	private double getPositionY()
	{
		return getPosition().getY();
	}
	
	private double getPositionZ()
	{
		return getPosition().getZ();
	}
	
	private String getAddress()
	{
		return getCurrentCell().getCellAddress().getRawAddress();
	}
	
	private String getUsername()
	{
		smClientAccountManager accountManager = m_viewContext.appContext.accountMngr;
		
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
	
	private int getGridWidth()
	{
		return getCurrentCell().getGrid().getWidth();
	}
	
	private int getGridHeight()
	{
		return getCurrentCell().getGrid().getHeight();
	}
	
	State_ViewingCell getViewingState()
	{
		State_ViewingCell viewingState = m_viewContext.stateContext.getEnteredState(State_ViewingCell.class);
		
		if( viewingState == null )
		{
			smU_Debug.ASSERT(false, "Expected to be in viewing state for caja api- or uri-related action.");
		}
		
		return viewingState;
	}
	
	smBufferCell getCurrentCell()
	{
		return getViewingState().getCell();
	}
	
	private native void logError(String message)
	/*-{
		console.log(message);
	}-*/;
}
